import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AdminStoreService, apiError } from '../services/admin-store.service';
import { RainforestPathService } from '../services/RainforestPathService';
@Component({ selector: 'app-create-path', standalone: false, templateUrl: './create-path.component.html', styleUrls: ['./create-path.component.css'] })
export class CreatePathComponent {
  name = '';
  selectedIds: number[] = [];
  busy = false;
  error = '';
  constructor(public store: AdminStoreService, private service: RainforestPathService, private router: Router) {}
  toggle(id: number): void {
    this.selectedIds = this.selectedIds.includes(id) ? this.selectedIds.filter(value => value !== id) : [...this.selectedIds, id];
  }
  async submit(): Promise<void> {
    this.busy = true; this.error = '';
    try {
      const checkpoints = this.selectedIds.map(id => this.store.state.checkpoints.find(cp => cp.id === id)!).filter(Boolean);
      const created = await firstValueFrom(this.service.save({ name: this.name, checkpoints }));
      await this.store.refresh();
      await this.router.navigate(['/path-view', created.id]);
    } catch (error) { this.error = apiError(error); }
    finally { this.busy = false; }
  }
}
