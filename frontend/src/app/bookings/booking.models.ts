export type ReservationStatus =
  | 'CONFIRMED'
  | 'WAITLISTED'
  | 'WAITLIST_OFFERED'
  | 'CANCELLED_BY_PARTICIPANT'
  | 'CANCELLED_BY_INSTRUCTOR'
  | 'WAITLIST_OFFER_EXPIRED';

export interface BookingLocation {
  placeName: string;
  address: string;
  latitude: number;
  longitude: number;
}

export interface TrainingEnrollment {
  id: string;
  name: string;
  description: string;
  location: BookingLocation;
  capacity: number;
  cancellationDeadlineDays: number;
  durationMinutes: number;
  createdAt: string;
  updatedAt: string;
}

export interface Term {
  id: string;
  name: string;
  description: string;
  location: BookingLocation;
  capacity: number;
  availablePlaces: number;
  cancellationDeadlineDays: number;
  durationMinutes: number;
  startsAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReservationSummary {
  id: string;
  termId: string;
  participantUserId: string | null;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  status: ReservationStatus;
  waitlistPosition: number;
  waitlistOfferExpiresAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreatedReservation extends ReservationSummary {
  cancellationToken: string;
}

export interface UpsertTrainingEnrollment {
  name: string;
  description: string;
  location: BookingLocation;
  capacity: number;
  cancellationDeadlineDays: number;
  durationMinutes: number;
}

export interface UpsertTerm extends UpsertTrainingEnrollment {
  startsAt: string;
}

export interface CreateReservation {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  createAccount: boolean;
  username: string | null;
  password: string | null;
}
