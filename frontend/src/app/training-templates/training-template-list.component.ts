import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {RouterLink} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';

import {TranslatePipe} from '../shared/i18n/translate.pipe';
import {TranslationService} from '../shared/i18n/translation.service';
import {TrainingTemplate} from './training-template.models';
import {TrainingTemplateService} from './training-template.service';

@Component({
    selector: 'app-training-template-list',
    standalone: true,
    imports: [DatePipe, RouterLink, MatButtonModule, TranslatePipe],
    templateUrl: './training-template-list.component.html',
    styleUrl: './training-template-list.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrainingTemplateListComponent {
    private readonly service = inject(TrainingTemplateService);
    protected readonly i18n = inject(TranslationService);
    protected readonly templates = signal<TrainingTemplate[]>([]);
    protected readonly loading = signal(true);
    protected readonly failed = signal(false);

    constructor() {
        void this.load();
    }

    protected async load(): Promise<void> {
        this.loading.set(true);
        this.failed.set(false);
        try {
            this.templates.set(await this.service.list());
        } catch {
            this.failed.set(true);
        } finally {
            this.loading.set(false);
        }
    }
}
