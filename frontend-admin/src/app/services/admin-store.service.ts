import { Injectable, NgZone, OnDestroy } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, firstValueFrom, forkJoin } from 'rxjs';
import { Checkpoint } from '../contracts/Checkpoint';
import { RainforestPath } from '../contracts/RainforestPath';
import { environment } from '../../environments/environment';

export interface AdminSnapshot { revision: number; paths: RainforestPath[]; checkpoints: Checkpoint[]; }
export function apiError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    return error.error?.error || (error.status === 0 ? 'The API is unavailable. Check the server terminal.' : `Request failed (${error.status}).`);
  }
  return error instanceof Error ? error.message : 'The request failed.';
}

@Injectable({ providedIn: 'root' })
export class AdminStoreService implements OnDestroy {
  readonly localMock = environment.localMock;
  readonly snapshot$ = new BehaviorSubject<AdminSnapshot>({ revision: -1, paths: [], checkpoints: [] });
  connected = false;
  error = '';
  private timer = 0;
  private legacyFingerprint = '';
  get state(): AdminSnapshot { return this.snapshot$.value; }

  constructor(private http: HttpClient, private zone: NgZone) {
    this.zone.runOutsideAngular(() => {
      void this.refresh();
      this.timer = window.setInterval(() => void this.refresh(), 1000);
    });
  }
  async refresh(): Promise<void> {
    try {
      let snapshot: AdminSnapshot;
      if (this.localMock) {
        snapshot = await firstValueFrom(this.http.get<AdminSnapshot>(`${environment.api.baseUrl}/state`));
      } else {
        const data = await firstValueFrom(forkJoin({
          paths: this.http.get<RainforestPath[]>(`${environment.api.baseUrl}/paths`),
          checkpoints: this.http.get<Checkpoint[]>(`${environment.api.baseUrl}/checkpoints`)
        }));
        const fingerprint = JSON.stringify(data);
        snapshot = { ...data, revision: this.state.revision + (fingerprint === this.legacyFingerprint ? 0 : 1) };
        this.legacyFingerprint = fingerprint;
      }
      this.zone.run(() => {
        this.connected = true;
        this.error = '';
        if (snapshot.revision > this.state.revision) this.snapshot$.next(snapshot);
      });
    } catch (error) {
      this.zone.run(() => { this.connected = false; this.error = apiError(error); });
    }
  }
  ngOnDestroy(): void { window.clearInterval(this.timer); }
}
