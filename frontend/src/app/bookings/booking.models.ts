export type ReservationStatus =
  | 'CONFIRMED'
  | 'WAITLIST_OFFERED'
  | 'CANCELLED_BY_PARTICIPANT'
  | 'CANCELLED_BY_INSTRUCTOR'
  | 'WAITLIST_OFFER_EXPIRED';

export type TrainingLevel = 'BASIC' | 'INTERMEDIATE' | 'ADVANCED';

export interface BookingLocation {
  placeName: string;
  address: string;
  latitude: number;
  longitude: number;
}

export interface Term {
  id: string;
  name: string;
  description: string;
  trainingLevel: TrainingLevel;
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
  waitlistOfferExpiresAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreatedReservation extends ReservationSummary {
  cancellationToken: string;
}

export interface WaitlistEntrySummary {
  id: string;
  termId: string;
  participantUserId: string | null;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  position: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreatedWaitlistEntry extends WaitlistEntrySummary {
  cancellationToken: string;
}

export type CreatedBooking =
  | { type: 'RESERVATION'; reservation: CreatedReservation; waitlistEntry: null }
  | { type: 'WAITLIST_ENTRY'; reservation: null; waitlistEntry: CreatedWaitlistEntry };

export interface UpsertTerm {
  name: string;
  description: string;
  trainingLevel: TrainingLevel;
  location: BookingLocation;
  capacity: number;
  cancellationDeadlineDays: number;
  durationMinutes: number;
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
