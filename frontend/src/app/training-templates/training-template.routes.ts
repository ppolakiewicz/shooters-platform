import {Routes} from '@angular/router';
import {pendingTrainingTemplateChangesGuard} from './pending-training-template-changes.guard';
import {TrainingTemplateCreateComponent} from './training-template-create.component';
import {TrainingTemplateDetailComponent} from './training-template-detail.component';
import {TrainingTemplateEditComponent} from './training-template-edit.component';
import {TrainingTemplateListComponent} from './training-template-list.component';

export const trainingTemplateRoutes: Routes = [
    {path: '', component: TrainingTemplateListComponent},
    {path: 'new', component: TrainingTemplateCreateComponent, canDeactivate: [pendingTrainingTemplateChangesGuard]},
    {path: ':id/edit', component: TrainingTemplateEditComponent, canDeactivate: [pendingTrainingTemplateChangesGuard]},
    {path: ':id', component: TrainingTemplateDetailComponent}
];
