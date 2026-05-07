import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { form, FormField, maxLength, min, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

import { emptyScore, HitScore, ScoringType, ShootingTask, Training, UpsertTask, WeaponType } from './training.models';
import { TrainingService } from './training.service';

@Component({
  selector: 'app-training-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormField,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './training-detail.component.html',
  styleUrl: './training-detail.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrainingDetailComponent {
  private readonly trainings = inject(TrainingService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly trainingId = this.route.snapshot.paramMap.get('id') ?? '';

  protected readonly weaponTypes: WeaponType[] = ['PISTOL', 'RIFLE', 'SHOTGUN'];
  protected readonly scoringTypes: ScoringType[] = ['IDPA', 'TARGET'];

  protected readonly training = signal<Training | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly taskSaving = signal(false);
  protected readonly deleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly taskError = signal<string | null>(null);
  protected readonly editingTaskId = signal<string | null>(null);
  protected readonly editingTaskLabel = computed(() => {
    const taskId = this.editingTaskId();
    return taskId ? `Run ${this.training()?.tasks.find((task) => task.id === taskId)?.runNumber ?? ''}` : 'New task';
  });

  protected readonly trainingModel = signal({
    name: '',
    place: '',
    description: '',
    performedOn: '',
    weaponType: 'PISTOL' as WeaponType,
    scoringType: 'IDPA' as ScoringType
  });
  protected readonly trainingForm = form(this.trainingModel, (path) => {
    required(path.name, { message: 'Name is required' });
    maxLength(path.name, 120, { message: 'Use at most 120 characters' });
    required(path.place, { message: 'Place is required' });
    maxLength(path.place, 120, { message: 'Use at most 120 characters' });
    maxLength(path.description, 2048, { message: 'Use at most 2048 characters' });
    required(path.performedOn, { message: 'Date is required' });
    required(path.weaponType, { message: 'Weapon is required' });
    required(path.scoringType, { message: 'Scoring is required' });
  });

  protected readonly taskModel = signal({
    weaponType: 'PISTOL' as WeaponType,
    scoringType: 'IDPA' as ScoringType,
    duration: '0:00.1',
    alpha: 1,
    charlie: 0,
    delta: 0,
    miss: 0,
    score0: 0,
    score1: 0,
    score2: 0,
    score3: 0,
    score4: 0,
    score5: 0,
    score6: 0,
    score7: 0,
    score8: 0,
    score9: 0,
    score10: 0
  });
  protected readonly taskForm = form(this.taskModel, (path) => {
    required(path.weaponType, { message: 'Weapon is required' });
    required(path.scoringType, { message: 'Scoring is required' });
    required(path.duration, { message: 'Duration is required' });
    min(path.alpha, 0, { message: 'Alpha cannot be negative' });
    min(path.charlie, 0, { message: 'Charlie cannot be negative' });
    min(path.delta, 0, { message: 'Delta cannot be negative' });
    min(path.miss, 0, { message: 'Miss cannot be negative' });
    min(path.score0, 0, { message: 'Score 0 cannot be negative' });
    min(path.score1, 0, { message: 'Score 1 cannot be negative' });
    min(path.score2, 0, { message: 'Score 2 cannot be negative' });
    min(path.score3, 0, { message: 'Score 3 cannot be negative' });
    min(path.score4, 0, { message: 'Score 4 cannot be negative' });
    min(path.score5, 0, { message: 'Score 5 cannot be negative' });
    min(path.score6, 0, { message: 'Score 6 cannot be negative' });
    min(path.score7, 0, { message: 'Score 7 cannot be negative' });
    min(path.score8, 0, { message: 'Score 8 cannot be negative' });
    min(path.score9, 0, { message: 'Score 9 cannot be negative' });
    min(path.score10, 0, { message: 'Score 10 cannot be negative' });
  });

  constructor() {
    void this.load();
  }

  protected async load(): Promise<void> {
    if (!this.trainingId) {
      await this.router.navigateByUrl('/trainings');
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    try {
      const training = await this.trainings.get(this.trainingId);
      this.setTraining(training);
    } catch {
      this.error.set(this.trainings.error() ?? 'Could not load training');
    } finally {
      this.loading.set(false);
    }
  }

  protected saveTraining(): void {
    submit(this.trainingForm, async () => {
      this.saving.set(true);
      this.error.set(null);
      try {
        this.setTraining(await this.trainings.update(this.trainingId, this.trainingModel()));
      } catch {
        this.error.set(this.trainings.error() ?? 'Could not save training');
      } finally {
        this.saving.set(false);
      }
    });
  }

  protected saveTask(): void {
    submit(this.taskForm, async () => {
      const request = this.taskRequest();
      if (!request) {
        return;
      }

      this.taskSaving.set(true);
      this.taskError.set(null);
      try {
        const taskId = this.editingTaskId();
        const updated = taskId
          ? await this.trainings.updateTask(this.trainingId, taskId, request)
          : await this.trainings.addTask(this.trainingId, request);
        this.setTraining(updated);
        this.resetTaskForm();
      } catch {
        this.taskError.set(this.trainings.error() ?? 'Could not save task');
      } finally {
        this.taskSaving.set(false);
      }
    });
  }

  protected editTask(task: ShootingTask): void {
    this.editingTaskId.set(task.id);
    this.taskError.set(null);
    this.taskModel.set({
      weaponType: task.weaponType,
      scoringType: task.scoringType,
      duration: this.durationLabel(task.durationTenths),
      alpha: task.score['alpha'] ?? 0,
      charlie: task.score['charlie'] ?? 0,
      delta: task.score['delta'] ?? 0,
      miss: task.score['miss'] ?? 0,
      score0: task.score['0'] ?? 0,
      score1: task.score['1'] ?? 0,
      score2: task.score['2'] ?? 0,
      score3: task.score['3'] ?? 0,
      score4: task.score['4'] ?? 0,
      score5: task.score['5'] ?? 0,
      score6: task.score['6'] ?? 0,
      score7: task.score['7'] ?? 0,
      score8: task.score['8'] ?? 0,
      score9: task.score['9'] ?? 0,
      score10: task.score['10'] ?? 0
    });
  }

  protected async deleteTask(task: ShootingTask): Promise<void> {
    if (!window.confirm(`Delete run ${task.runNumber}?`)) {
      return;
    }

    this.taskError.set(null);
    try {
      this.setTraining(await this.trainings.deleteTask(this.trainingId, task.id));
      if (this.editingTaskId() === task.id) {
        this.resetTaskForm();
      }
    } catch {
      this.taskError.set(this.trainings.error() ?? 'Could not delete task');
    }
  }

  protected async deleteTraining(): Promise<void> {
    const training = this.training();
    if (!training || !window.confirm(`Delete ${training.name}?`)) {
      return;
    }

    this.deleting.set(true);
    this.error.set(null);
    try {
      await this.trainings.delete(training.id);
      await this.router.navigateByUrl('/trainings');
    } catch {
      this.error.set(this.trainings.error() ?? 'Could not delete training');
    } finally {
      this.deleting.set(false);
    }
  }

  protected cancelTaskEdit(): void {
    this.resetTaskForm();
  }

  protected scoreSummary(task: ShootingTask): string {
    if (task.scoringType === 'IDPA') {
      return `A ${task.score['alpha'] ?? 0} / C ${task.score['charlie'] ?? 0} / D ${task.score['delta'] ?? 0} / M ${task.score['miss'] ?? 0}`;
    }

    return Object.entries(task.score)
      .filter(([, count]) => count > 0)
      .map(([score, count]) => `${score}:${count}`)
      .join(' / ');
  }

  protected durationLabel(durationTenths: number): string {
    const minutes = Math.floor(durationTenths / 600);
    const seconds = Math.floor((durationTenths % 600) / 10);
    const tenths = durationTenths % 10;
    return `${minutes}:${seconds.toString().padStart(2, '0')}.${tenths}`;
  }

  private setTraining(training: Training): void {
    this.training.set(training);
    this.trainingModel.set({
      name: training.name,
      place: training.place,
      description: training.description,
      performedOn: training.performedOn,
      weaponType: training.weaponType,
      scoringType: training.scoringType
    });
    if (!this.editingTaskId()) {
      this.resetTaskForm();
    }
  }

  private resetTaskForm(): void {
    const training = this.training();
    this.editingTaskId.set(null);
    this.taskError.set(null);
    this.taskModel.set({
      weaponType: training?.weaponType ?? 'PISTOL',
      scoringType: training?.scoringType ?? 'IDPA',
      duration: '0:00.1',
      alpha: training?.scoringType === 'TARGET' ? 0 : 1,
      charlie: 0,
      delta: 0,
      miss: 0,
      score0: training?.scoringType === 'TARGET' ? 1 : 0,
      score1: 0,
      score2: 0,
      score3: 0,
      score4: 0,
      score5: 0,
      score6: 0,
      score7: 0,
      score8: 0,
      score9: 0,
      score10: 0
    });
  }

  private taskRequest(): UpsertTask | null {
    const model = this.taskModel();
    const durationTenths = parseDurationTenths(model.duration);
    const score: HitScore = emptyScore(model.scoringType);

    if (model.scoringType === 'IDPA') {
      score['alpha'] = model.alpha;
      score['charlie'] = model.charlie;
      score['delta'] = model.delta;
      score['miss'] = model.miss;
    } else {
      score['0'] = model.score0;
      score['1'] = model.score1;
      score['2'] = model.score2;
      score['3'] = model.score3;
      score['4'] = model.score4;
      score['5'] = model.score5;
      score['6'] = model.score6;
      score['7'] = model.score7;
      score['8'] = model.score8;
      score['9'] = model.score9;
      score['10'] = model.score10;
    }

    if (durationTenths === null) {
      this.taskError.set('Use duration format m:ss.t, for example 1:23.4');
      return null;
    }

    if (durationTenths < 1) {
      this.taskError.set('Duration must be greater than zero');
      return null;
    }

    if (!Object.values(score).some((value) => value > 0)) {
      this.taskError.set('Add at least one hit or miss');
      return null;
    }

    return {
      weaponType: model.weaponType,
      scoringType: model.scoringType,
      durationTenths,
      score
    };
  }
}

function parseDurationTenths(value: string): number | null {
  const normalized = value.trim().replace(',', '.');
  if (!normalized) {
    return null;
  }

  const parts = normalized.split(':');
  if (parts.length > 2) {
    return null;
  }

  const minutes = parts.length === 2 ? Number(parts[0]) : 0;
  const secondsPart = parts.length === 2 ? parts[1] : parts[0];
  const match = /^(\d+)(?:\.(\d))?$/.exec(secondsPart);

  if (!Number.isInteger(minutes) || minutes < 0 || !match) {
    return null;
  }

  const seconds = Number(match[1]);
  const tenths = match[2] ? Number(match[2]) : 0;

  if (!Number.isInteger(seconds) || seconds < 0 || !Number.isInteger(tenths)) {
    return null;
  }

  if (parts.length === 2 && seconds > 59) {
    return null;
  }

  return minutes * 600 + seconds * 10 + tenths;
}
