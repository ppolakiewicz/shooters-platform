export type WeaponType = 'PISTOL' | 'RIFLE' | 'SHOTGUN';
export type ScoringType = 'IDPA' | 'TARGET';

export type HitScore = Record<string, number>;

export interface ShootingTask {
  id: string;
  runNumber: number;
  weaponType: WeaponType;
  scoringType: ScoringType;
  durationTenths: number;
  score: HitScore;
}

export interface TrainingSummary {
  id: string;
  name: string;
  place: string;
  description: string;
  performedOn: string;
  weaponType: WeaponType;
  scoringType: ScoringType;
  taskCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Training extends Omit<TrainingSummary, 'taskCount'> {
  tasks: ShootingTask[];
}

export interface UpsertTraining {
  name: string;
  place: string;
  description: string;
  performedOn: string;
  weaponType: WeaponType;
  scoringType: ScoringType;
}

export interface UpsertTask {
  weaponType: WeaponType;
  scoringType: ScoringType;
  durationTenths: number;
  score: HitScore;
}

export const scoreKeys = (scoringType: ScoringType): string[] =>
  scoringType === 'IDPA' ? ['alpha', 'charlie', 'delta', 'miss'] : ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];

export const emptyScore = (scoringType: ScoringType): HitScore =>
  Object.fromEntries(scoreKeys(scoringType).map((key) => [key, 0]));
