import { Component } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AdminStoreService, apiError } from '../services/admin-store.service';
import { CheckpointService } from '../services/CheckpointService';
@Component({ selector: 'app-checkpoints-list', standalone: false, templateUrl: './checkpoints-list.component.html', styleUrls: ['./checkpoints-list.component.css'] })
export class CheckpointsListComponent {
  error = '';
  constructor(public store: AdminStoreService, private service: CheckpointService) {}
  async remove(id: number): Promise<void> {
    try { await firstValueFrom(this.service.delete(id)); await this.store.refresh(); this.error = ''; }
    catch (error) { this.error = apiError(error); }
  }
}
