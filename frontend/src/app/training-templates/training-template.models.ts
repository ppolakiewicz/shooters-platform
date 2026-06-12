export type TrainingLevel = 'BASIC' | 'INTERMEDIATE' | 'ADVANCED';

export interface TrainingTemplateLocation {
    placeName: string;
    address: string;
    latitude: number;
    longitude: number;
}

export interface TrainingTemplate {
    id: string;
    name: string;
    description: string;
    trainingLevel: TrainingLevel;
    location: TrainingTemplateLocation;
    capacity: number;
    cancellationDeadlineDays: number;
    durationMinutes: number;
    defaultStartTime: string;
    createdAt: string;
    updatedAt: string;
}

export interface TrainingTemplateRequest {
    name: string;
    description: string;
    trainingLevel: TrainingLevel;
    location: TrainingTemplateLocation;
    capacity: number;
    cancellationDeadlineDays: number;
    durationMinutes: number;
    defaultStartTime: string;
}

export interface TrainingTemplateFormValue {
    name: string;
    description: string;
    trainingLevel: TrainingLevel;
    placeName: string;
    address: string;
    latitude: number;
    longitude: number;
    capacity: number;
    cancellationDeadlineDays: number;
    durationHours: number;
    defaultStartTime: string;
}

export function toTrainingTemplateRequest(value: TrainingTemplateFormValue): TrainingTemplateRequest {
    return {
        name: value.name,
        description: value.description,
        trainingLevel: value.trainingLevel,
        location: {
            placeName: value.placeName,
            address: value.address,
            latitude: value.latitude,
            longitude: value.longitude
        },
        capacity: value.capacity,
        cancellationDeadlineDays: value.cancellationDeadlineDays,
        durationMinutes: Math.round(value.durationHours * 60),
        defaultStartTime: value.defaultStartTime
    };
}

export function toTrainingTemplateFormValue(template: TrainingTemplate): TrainingTemplateFormValue {
    return {
        name: template.name,
        description: template.description,
        trainingLevel: template.trainingLevel,
        placeName: template.location.placeName,
        address: template.location.address,
        latitude: template.location.latitude,
        longitude: template.location.longitude,
        capacity: template.capacity,
        cancellationDeadlineDays: template.cancellationDeadlineDays,
        durationHours: template.durationMinutes / 60,
        defaultStartTime: template.defaultStartTime
    };
}
