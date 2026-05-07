import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TrainingService } from './training.service';
import { TrainingSummary } from './training.models';

@Component({
  selector: 'app-training-list',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './training-list.component.html',
  styleUrl: './training-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrainingListComponent {
  private readonly trainings = inject(TrainingService);
  private readonly router = inject(Router);

  protected readonly items = signal<TrainingSummary[]>([]);
  protected readonly loading = signal(false);
  protected readonly creating = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    void this.load();
  }

  protected async load(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      this.items.set(await this.trainings.list());
    } catch {
      this.error.set(this.trainings.error() ?? 'Could not load trainings');
    } finally {
      this.loading.set(false);
    }
  }

  protected async create(): Promise<void> {
    this.creating.set(true);
    this.error.set(null);
    try {
      const training = await this.trainings.create({
        name: 'New training',
        place: 'Shooting range',
        description: '',
        performedOn: today(),
        weaponType: 'PISTOL',
        scoringType: 'IDPA'
      });
      await this.router.navigate(['/trainings', training.id]);
    } catch {
      this.error.set(this.trainings.error() ?? 'Could not create training');
    } finally {
      this.creating.set(false);
    }
  }

  protected async remove(training: TrainingSummary): Promise<void> {
    if (!window.confirm(`Delete ${training.name}?`)) {
      return;
    }

    this.error.set(null);
    try {
      await this.trainings.delete(training.id);
      await this.load();
    } catch {
      this.error.set(this.trainings.error() ?? 'Could not delete training');
    }
  }
}

function today(): string {
  return new Date().toISOString().slice(0, 10);
}
