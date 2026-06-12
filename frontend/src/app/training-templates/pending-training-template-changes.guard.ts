import {CanDeactivateFn} from '@angular/router';

export interface PendingTrainingTemplateChanges {
    hasPendingChanges(): boolean;

    pendingChangesMessage(): string;
}

export const pendingTrainingTemplateChangesGuard: CanDeactivateFn<PendingTrainingTemplateChanges> = (component) =>
    !component.hasPendingChanges() || window.confirm(component.pendingChangesMessage());
