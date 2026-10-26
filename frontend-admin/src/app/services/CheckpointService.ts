import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { GenericHttpService } from './GenericHttpService/GenericHttpService';
import { Checkpoint } from '../contracts/Checkpoint';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CheckpointService extends GenericHttpService<Checkpoint, number> {
  constructor(http: HttpClient) { super(http, `${environment.api.baseUrl}/checkpoints`); }
}
