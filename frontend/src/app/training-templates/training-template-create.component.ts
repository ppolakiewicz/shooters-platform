import {ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TranslatePipe} from '../shared/i18n/translate.pipe';
import {TranslationService} from '../shared/i18n/translation.service';
import {PendingTrainingTemplateChanges} from './pending-training-template-changes.guard';
import {TrainingTemplateFormComponent} from './training-template-form.component';
import {toTrainingTemplateRequest, TrainingTemplateFormValue} from './training-template.models';
import {TrainingTemplateService} from './training-template.service';

@Component({
    selector: 'app-training-template-create',
    standalone: true,
    imports: [RouterLink, MatButtonModule, TranslatePipe, TrainingTemplateFormComponent],
    template: '<main class="page"><a mat-button routerLink="/training-templates">{{"trainingTemplates.back"|translate}}</a><h1>{{"trainingTemplates.create"|translate}}</h1><app-training-template-form [saving]="saving()" (submitted)="save($event)"/></main>',
    styles: ['.page{max-width:800px;margin:auto;padding:24px}'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrainingTemplateCreateComponent implements PendingTrainingTemplateChanges {
    private readonly service = inject(TrainingTemplateService);
    private readonly router = inject(Router);
    private readonly snack = inject(MatSnackBar);
    private readonly i18n = inject(TranslationService);
    private readonly form = viewChild.required(TrainingTemplateFormComponent);
    protected readonly saving = signal(false);
    private saved = false;

    protected async save(value: TrainingTemplateFormValue): Promise<void> {
        this.saving.set(true);
        try {
            await this.service.create(toTrainingTemplateRequest(value));
            this.saved = true;
            this.form().resetDirty();
            this.snack.open(this.i18n.translate('trainingTemplates.created'), '', {duration: 3000});
            await this.router.navigateByUrl('/training-templates');
        } catch {
            this.snack.open(this.i18n.translate('trainingTemplates.saveFailed'), '', {duration: 4000});
        } finally {
            this.saving.set(false);
        }
    }

    hasPendingChanges(): boolean {
        return !this.saved && this.form().isDirty();
    }

    pendingChangesMessage(): string {
        return this.i18n.translate('trainingTemplates.unsavedChanges');
    }
}
