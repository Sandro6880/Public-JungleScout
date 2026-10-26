import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Checkpoint } from '../contracts/Checkpoint';

@Component({
  selector: 'app-local-map', standalone: false,
  template: `
    <div class="map-frame">
      <svg viewBox="0 0 760 360" role="img" aria-label="Local checkpoint route preview"
           data-testid="local-map" (click)="choosePoint($event)">
        <defs><pattern id="local-grid" width="38" height="36" patternUnits="userSpaceOnUse">
          <path d="M 38 0 L 0 0 0 36" fill="none" stroke="#d7e4d9" stroke-width="1"/>
        </pattern></defs>
        <rect width="760" height="360" fill="#eff5ef"/>
        <rect width="760" height="360" fill="url(#local-grid)"/>
        <polyline *ngIf="checkpoints.length > 1" [attr.points]="polyline" fill="none" stroke="#28734c" stroke-width="4"/>
        <g *ngFor="let point of points; let i = index">
          <circle [attr.cx]="point.x" [attr.cy]="point.y" r="13" fill="#205b3c" stroke="white" stroke-width="3"/>
          <text [attr.x]="point.x" [attr.y]="point.y + 4" text-anchor="middle" fill="white" font-size="11">{{i + 1}}</text>
          <title>{{point.cp.name}} ({{point.cp.gpsLat}}, {{point.cp.gpsLng}})</title>
        </g>
        <text *ngIf="checkpoints.length === 0" x="380" y="175" text-anchor="middle" fill="#506657" font-size="17">No checkpoints on this path yet</text>
        <text x="18" y="340" fill="#607365" font-size="12">Click to create a checkpoint · Schematic preview</text>
      </svg>
    </div>
    <p class="muted">A local coordinate preview. It uses straight segments and loads no external map service.</p>
  `,
  styles: [`.map-frame {border:1px solid #ceded1;border-radius:8px;overflow:hidden;margin-bottom:.5rem;}
    svg {display:block;width:100%;height:auto;cursor:crosshair;}`]
})
export class LocalMapComponent {
  @Input() checkpoints: Checkpoint[] = [];
  @Output() pointSelected = new EventEmitter<{ lat: number; lng: number }>();
  private get bounds() {
    const lats = this.checkpoints.map(cp => Number(cp.gpsLat));
    const lngs = this.checkpoints.map(cp => Number(cp.gpsLng));
    const minLat = lats.length ? Math.min(...lats) : 48.26;
    const maxLat = lats.length ? Math.max(...lats) : 48.29;
    const minLng = lngs.length ? Math.min(...lngs) : 14.23;
    const maxLng = lngs.length ? Math.max(...lngs) : 14.27;
    const spanLat = Math.max(maxLat - minLat, .01) * 1.4;
    const spanLng = Math.max(maxLng - minLng, .01) * 1.4;
    return { lat: (minLat + maxLat) / 2 - spanLat / 2, lng: (minLng + maxLng) / 2 - spanLng / 2, spanLat, spanLng };
  }
  get points() {
    const b = this.bounds;
    return this.checkpoints.map(cp => ({ cp,
      x: 40 + (Number(cp.gpsLng) - b.lng) / b.spanLng * 680,
      y: 305 - (Number(cp.gpsLat) - b.lat) / b.spanLat * 270
    }));
  }
  get polyline(): string { return this.points.map(point => `${point.x},${point.y}`).join(' '); }
  choosePoint(event: MouseEvent): void {
    const box = (event.currentTarget as SVGSVGElement).getBoundingClientRect();
    const b = this.bounds;
    const x = (event.clientX - box.left) / box.width * 760;
    const y = (event.clientY - box.top) / box.height * 360;
    const lat = b.lat + (305 - y) / 270 * b.spanLat;
    const lng = b.lng + (x - 40) / 680 * b.spanLng;
    this.pointSelected.emit({ lat: Number(Math.max(-90, Math.min(90, lat)).toFixed(7)), lng: Number(Math.max(-180, Math.min(180, lng)).toFixed(7)) });
  }
}
