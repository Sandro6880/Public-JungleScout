import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom, Subscription } from 'rxjs';
import { Checkpoint } from '../contracts/Checkpoint';
import { RainforestPath } from '../contracts/RainforestPath';
import { RainforestPathService } from '../services/RainforestPathService';
import { CheckpointService } from '../services/CheckpointService';
import { AdminStoreService, apiError } from '../services/admin-store.service';

@Component({ selector: 'app-path-view', standalone: false, templateUrl: './path-view.component.html', styleUrls: ['./path-view.component.css'] })
export class PathViewComponent implements OnInit, OnDestroy {
  currentRfp: RainforestPath = { id: 0, name: '', pathLength: '0.00', pathDuration: '00:00:00', checkpoints: [] };
  loaded = false;
  dirty = false;
  busy = false;
  error = '';
  mapDraft: Omit<Checkpoint, 'id'> | null = null;
  private subscriptions = new Subscription();
  constructor(public store: AdminStoreService, private pathService: RainforestPathService,
              private checkpointService: CheckpointService, private route: ActivatedRoute, private router: Router) {}
  ngOnInit(): void {
    const synchronize = () => {
      if (this.store.state.revision < 0) return;
      const id = Number(this.route.snapshot.paramMap.get('id'));
      const path = this.store.state.paths.find(value => value.id === id);
      if (!path) { this.loaded = false; this.error = 'This path no longer exists.'; return; }
      if (!this.dirty || this.currentRfp.id !== id) {
        this.currentRfp = structuredClone(path); this.dirty = false;
      }
      this.loaded = true;
    };
    this.subscriptions.add(this.store.snapshot$.subscribe(synchronize));
    this.subscriptions.add(this.route.paramMap.subscribe(() => { this.dirty = false; synchronize(); }));
  }
  hasCheckpoint(id: number): boolean { return this.currentRfp.checkpoints.some(cp => cp.id === id); }
  toggleCheckpoint(cp: Checkpoint): void {
    this.currentRfp.checkpoints = this.hasCheckpoint(cp.id)
      ? this.currentRfp.checkpoints.filter(value => value.id !== cp.id)
      : [...this.currentRfp.checkpoints, structuredClone(cp)];
    this.dirty = true;
  }
  move(index: number, offset: number): void {
    const target = index + offset;
    if (target < 0 || target >= this.currentRfp.checkpoints.length) return;
    const list = [...this.currentRfp.checkpoints];
    list.splice(target, 0, list.splice(index, 1)[0]);
    this.currentRfp.checkpoints = list; this.dirty = true;
  }
  async savePath(): Promise<void> {
    this.busy = true; this.error = '';
    try {
      await firstValueFrom(this.pathService.update(this.currentRfp.id, this.currentRfp));
      this.dirty = false; await this.store.refresh();
      const saved = this.store.state.paths.find(path => path.id === this.currentRfp.id);
      if (saved) this.currentRfp = structuredClone(saved);
    } catch (error) { this.error = apiError(error); }
    finally { this.busy = false; }
  }
  async updateCheckpoint(cp: Checkpoint): Promise<void> {
    this.busy = true; this.error = '';
    try { await firstValueFrom(this.checkpointService.update(cp.id, cp)); await this.store.refresh(); }
    catch (error) { this.error = apiError(error); }
    finally { this.busy = false; }
  }
  async deletePath(): Promise<void> {
    this.busy = true; this.error = '';
    try {
      await firstValueFrom(this.pathService.delete(this.currentRfp.id));
      await this.store.refresh(); await this.router.navigate(['/paths']);
    } catch (error) { this.error = apiError(error); }
    finally { this.busy = false; }
  }
  prepareCheckpoint(point: { lat: number; lng: number }): void {
    this.mapDraft = { name: '', cpLocation: '', gpsLat: String(point.lat), gpsLng: String(point.lng), details: '' };
  }
  async addMapCheckpoint(): Promise<void> {
    if (!this.mapDraft) return;
    this.busy = true; this.error = '';
    try {
      const created = await firstValueFrom(this.checkpointService.save(this.mapDraft));
      this.currentRfp.checkpoints = [...this.currentRfp.checkpoints, created];
      this.mapDraft = null; this.dirty = true;
      await this.savePath();
    } catch (error) { this.error = apiError(error); }
    finally { this.busy = false; }
  }
  ngOnDestroy(): void { this.subscriptions.unsubscribe(); }
}
