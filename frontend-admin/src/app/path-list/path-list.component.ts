import { Component } from '@angular/core';
import { AdminStoreService } from '../services/admin-store.service';
@Component({ selector: 'app-path-list', standalone: false, templateUrl: './path-list.component.html', styleUrls: ['./path-list.component.css'] })
export class PathListComponent {
  constructor(public store: AdminStoreService) {}
}
