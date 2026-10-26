import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Checkpoint } from '../contracts/Checkpoint';
import { CheckpointService } from '../services/CheckpointService';
import { AdminStoreService, apiError } from '../services/admin-store.service';
@Component({ selector: 'app-create-checkpoint', standalone: false, templateUrl: './create-checkpoints.component.html', styleUrls: ['./create-checkpoints.component.css'] })
export class CreateCheckpointsComponent {
  draft: Omit<Checkpoint, 'id'> = { name: '', cpLocation: '', gpsLat: '', gpsLng: '', details: '' };
  error = '';
  busy = false;
  constructor(private service: CheckpointService, private store: AdminStoreService, private router: Router) {}
  async submit(): Promise<void> {
    this.busy = true; this.error = '';
    try {
      await firstValueFrom(this.service.save(this.draft));
      await this.store.refresh();
      await this.router.navigate(['/all-poi']);
    } catch (error) { this.error = apiError(error); }
    finally { this.busy = false; }
  }
}
