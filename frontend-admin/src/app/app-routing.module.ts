import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PathListComponent } from './path-list/path-list.component';
import { CreateCheckpointsComponent } from './create-checkpoint/create-checkpoints.component';
import { CreatePathComponent } from './create-path/create-path.component';
import { CheckpointsListComponent } from './checkpoint-list/checkpoints-list.component';
import { PathViewComponent } from './path-view/path-view.component';

const routes: Routes = [
  { path: '', redirectTo: 'paths', pathMatch: 'full' },
  { path: 'paths', component: PathListComponent },
  { path: 'create-checkpoint', component: CreateCheckpointsComponent },
  { path: 'create-path', component: CreatePathComponent },
  { path: 'all-poi', component: CheckpointsListComponent },
  { path: 'path-view/:id', component: PathViewComponent },
  { path: '**', redirectTo: 'paths' }
];
@NgModule({ imports: [RouterModule.forRoot(routes)], exports: [RouterModule] })
export class AppRoutingModule {}
