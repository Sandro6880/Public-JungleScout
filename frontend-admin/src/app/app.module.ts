import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { PathListComponent } from './path-list/path-list.component';
import { CheckpointsListComponent } from './checkpoint-list/checkpoints-list.component';
import { CreatePathComponent } from './create-path/create-path.component';
import { CreateCheckpointsComponent } from './create-checkpoint/create-checkpoints.component';
import { PathViewComponent } from './path-view/path-view.component';
import { LocalMapComponent } from './local-map/local-map.component';

@NgModule({
  declarations: [AppComponent, PathListComponent, CheckpointsListComponent,
    CreatePathComponent, CreateCheckpointsComponent, PathViewComponent, LocalMapComponent],
  imports: [BrowserModule, HttpClientModule, FormsModule, AppRoutingModule],
  bootstrap: [AppComponent]
})
export class AppModule {}
