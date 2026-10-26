import { AfterViewInit, Component, NgZone, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AdminStoreService } from './services/admin-store.service';
import { environment } from '../environments/environment';

@Component({ selector: 'app-root', standalone: false, templateUrl: './app.component.html', styleUrls: ['./app.component.css'] })
export class AppComponent implements AfterViewInit, OnDestroy {
  readonly title = 'Jungle Scout Admin';
  private timer?: number;
  private clientId = `ui_${crypto.randomUUID()}`;
  constructor(public store: AdminStoreService, private http: HttpClient, private router: Router, private zone: NgZone) {}
  ngAfterViewInit(): void {
    if (!this.store.localMock) return;
    this.zone.runOutsideAngular(() => {
      this.timer = window.setInterval(() => {
        if (!this.store.connected) return;
        const main = document.querySelector('[data-rendered-revision]');
        const revision = Number(main?.getAttribute('data-rendered-revision') ?? -1);
        if (revision < 0) return;
        const renderedIds = (attribute: string) => [...new Set(Array.from(document.querySelectorAll(`[${attribute}]`))
          .map(element => Number(element.getAttribute(attribute))).filter(value => value > 0))];
        // A receipt is based on the rendered DOM, not merely a successful API GET.
        this.http.post(`${environment.api.baseUrl}/testing/frontend`, {
          clientId: this.clientId, revision, route: this.router.url,
          visiblePathIds: renderedIds('data-path-id'), visibleCheckpointIds: renderedIds('data-checkpoint-id')
        }).subscribe({ error: () => undefined });
      }, 1000);
    });
  }
  ngOnDestroy(): void { if (this.timer !== undefined) window.clearInterval(this.timer); }
}
