import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { GenericHttpService } from './GenericHttpService/GenericHttpService';
import { RainforestPath } from '../contracts/RainforestPath';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RainforestPathService extends GenericHttpService<RainforestPath, number> {
  constructor(http: HttpClient) { super(http, `${environment.api.baseUrl}/paths`); }
}
