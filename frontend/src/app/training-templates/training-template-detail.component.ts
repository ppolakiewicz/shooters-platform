import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {firstValueFrom} from 'rxjs';
import {TranslatePipe} from '../shared/i18n/translate.pipe';
import {TranslationService} from '../shared/i18n/translation.service';
import {TrainingTemplateDeleteDialogComponent} from './training-template-delete-dialog.component';
import {TrainingTemplate} from './training-template.models';
import {TrainingTemplateService} from './training-template.service';

@Component({
    selector: 'app-training-template-detail',
    standalone: true,
    imports: [DatePipe, RouterLink, MatButtonModule, TranslatePipe],
    templateUrl: './training-template-detail.component.html',
    styleUrl: './training-template-detail.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrainingTemplateDetailComponent {
    private readonly service = inject(TrainingTemplateService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly dialog = inject(MatDialog);
    private readonly snack = inject(MatSnackBar);
    protected readonly i18n = inject(TranslationService);
    protected readonly template = signal<TrainingTemplate | null>(null);
    protected readonly notFound = signal(false);

    constructor() {
        void this.load();
    }

    private async load() {
        try {
            this.template.set(await this.service.get(this.route.snapshot.paramMap.get('id') ?? ''));
        } catch {
            this.notFound.set(true);
        }
    }

    protected async remove() {
        const template = this.template();
        if (!template) return;
        const confirmed = await firstValueFrom(this.dialog.open(TrainingTemplateDeleteDialogComponent, {data: {name: template.name}}).afterClosed());
        if (!confirmed) return;
        try {
            await this.service.delete(template.id);
            this.snack.open(this.i18n.translate('trainingTemplates.deleted'), '', {duration: 3000});
            await this.router.navigateByUrl('/training-templates');
        } catch {
            this.snack.open(this.i18n.translate('trainingTemplates.deleteFailed'), '', {duration: 4000});
        }
    }
}
