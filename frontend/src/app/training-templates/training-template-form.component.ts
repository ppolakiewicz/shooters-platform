import {ChangeDetectionStrategy, Component, input, output, signal} from '@angular/core';
import {form, FormField, max, maxLength, min, required, submit, validate} from '@angular/forms/signals';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';

import {TranslatePipe} from '../shared/i18n/translate.pipe';
import {TrainingLevel, TrainingTemplateFormValue} from './training-template.models';

const initialValue: TrainingTemplateFormValue = {
    name: '',
    description: '',
    trainingLevel: 'BASIC',
    placeName: '',
    address: '',
    latitude: 52.2297,
    longitude: 21.0122,
    capacity: 8,
    cancellationDeadlineDays: 2,
    durationHours: 1,
    defaultStartTime: '09:00'
};

@Component({
    selector: 'app-training-template-form',
    standalone: true,
    imports: [FormField, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, TranslatePipe],
    templateUrl: './training-template-form.component.html',
    styleUrl: './training-template-form.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrainingTemplateFormComponent {
    readonly saving = input(false);
    readonly submitted = output<TrainingTemplateFormValue>();
    readonly model = signal({...initialValue});
    readonly levels: readonly TrainingLevel[] = ['BASIC', 'INTERMEDIATE', 'ADVANCED'];
    readonly templateForm = form(this.model, (path) => {
        required(path.name);
        maxLength(path.name, 120);
        maxLength(path.description, 2048);
        required(path.trainingLevel);
        required(path.placeName);
        required(path.address);
        min(path.latitude, -90);
        max(path.latitude, 90);
        min(path.longitude, -180);
        max(path.longitude, 180);
        min(path.capacity, 1);
        max(path.capacity, 10);
        min(path.cancellationDeadlineDays, 0);
        max(path.cancellationDeadlineDays, 365);
        min(path.durationHours, 0.5);
        max(path.durationHours, 24);
        validate(path.durationHours, ({value}) => value() * 2 % 1 === 0 ? undefined : {kind: 'durationStep'});
        required(path.defaultStartTime);
        validate(path.defaultStartTime, ({value}) =>
            /^(?:[01]\d|2[0-3]):(?:00|15|30|45)$/.test(value()) ? undefined : {kind: 'startTimeStep'});
    });

    setValue(value: TrainingTemplateFormValue): void {
        this.model.set(value);
    }

    submit(): void {
        submit(this.templateForm, async () => this.submitted.emit(this.model()));
    }

    resetDirty(): void {
        this.templateForm().reset();
    }

    isDirty(): boolean {
        return this.templateForm().dirty();
    }
}
