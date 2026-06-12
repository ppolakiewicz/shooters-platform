import {ChangeDetectionStrategy, Component, effect, inject, signal, viewChild} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TranslatePipe} from '../shared/i18n/translate.pipe';
import {TranslationService} from '../shared/i18n/translation.service';
import {PendingTrainingTemplateChanges} from './pending-training-template-changes.guard';
import {TrainingTemplateFormComponent} from './training-template-form.component';
import {
    toTrainingTemplateFormValue,
    toTrainingTemplateRequest,
    TrainingTemplateFormValue
} from './training-template.models';
import {TrainingTemplateService} from './training-template.service';

@Component({
    selector: 'app-training-template-edit',
    standalone: true,
    imports: [RouterLink, MatButtonModule, TranslatePipe, TrainingTemplateFormComponent],
    template: '<main class="page"><a mat-button routerLink="/training-templates">{{"trainingTemplates.back"|translate}}</a><h1>{{"trainingTemplates.edit"|translate}}</h1>@if(notFound()){<p>{{"trainingTemplates.notFound"|translate}}</p>}@else if(loaded()){<app-training-template-form [saving]="saving()" (submitted)="save($event)"/>}</main>',
    styles: ['.page{max-width:800px;margin:auto;padding:24px}'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrainingTemplateEditComponent implements PendingTrainingTemplateChanges {
    private readonly service = inject(TrainingTemplateService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly snack = inject(MatSnackBar);
    private readonly i18n = inject(TranslationService);
    private readonly form = viewChild(TrainingTemplateFormComponent);
    protected readonly saving = signal(false);
    protected readonly loaded = signal(false);
    protected readonly notFound = signal(false);
    private readonly loadedValue = signal<TrainingTemplateFormValue | null>(null);
    private saved = false;
    private readonly id = this.route.snapshot.paramMap.get('id') ?? '';

    constructor() {
        effect(() => {
            const form = this.form();
            const value = this.loadedValue();
            if (form && value) {
                form.setValue(value);
            }
        });
        void this.load();
    }

    private async load(): Promise<void> {
        try {
            const template = await this.service.get(this.id);
            this.loadedValue.set(toTrainingTemplateFormValue(template));
            this.loaded.set(true);
        } catch {
            this.notFound.set(true);
        }
    }

    protected async save(value: TrainingTemplateFormValue): Promise<void> {
        this.saving.set(true);
        try {
            await this.service.update(this.id, toTrainingTemplateRequest(value));
            this.saved = true;
            this.form()?.resetDirty();
            this.snack.open(this.i18n.translate('trainingTemplates.updated'), '', {duration: 3000});
            await this.router.navigateByUrl('/training-templates');
        } catch {
            this.snack.open(this.i18n.translate('trainingTemplates.saveFailed'), '', {duration: 4000});
        } finally {
            this.saving.set(false);
        }
    }

    hasPendingChanges(): boolean {
        return !this.saved && (this.form()?.isDirty() ?? false);
    }

    pendingChangesMessage(): string {
        return this.i18n.translate('trainingTemplates.unsavedChanges');
    }
}
