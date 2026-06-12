import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {TranslatePipe} from '../shared/i18n/translate.pipe';

@Component({
    selector: 'app-training-template-delete-dialog',
    standalone: true,
    imports: [MatDialogModule, MatButtonModule, TranslatePipe],
    template: '<h2 mat-dialog-title>{{"trainingTemplates.deleteTitle"|translate}}</h2><mat-dialog-content>{{"trainingTemplates.deleteConfirm"|translate:{name:data.name} }}</mat-dialog-content><mat-dialog-actions align="end"><button mat-button mat-dialog-close>{{"common.cancel"|translate}}</button><button mat-flat-button [mat-dialog-close]="true">{{"common.delete"|translate}}</button></mat-dialog-actions>',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrainingTemplateDeleteDialogComponent {
    protected readonly data = inject<{ name: string }>(MAT_DIALOG_DATA);
}
