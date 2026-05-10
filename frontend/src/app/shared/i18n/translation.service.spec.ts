import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';

import { TranslationService } from './translation.service';

describe('TranslationService', () => {
  it('uses Polish as the default fallback language', () => {
    const storage = storageMock();
    const service = createService(storage, 'de-DE');

    expect(service.language()).toBe('pl');
    expect(service.translate('home.title')).toBe('Status usługi');
  });

  it('switches to English and persists the choice', () => {
    const storage = storageMock();
    const service = createService(storage, 'pl-PL');

    service.setLanguage('en');

    expect(service.language()).toBe('en');
    expect(service.translate('home.title')).toBe('Service status');
    expect(storage.setItem).toHaveBeenCalledWith('shooters-platform.language', 'en');
  });

  it('interpolates params and leaves unknown backend messages unchanged', () => {
    const service = createService(storageMock(), 'en-US');

    expect(service.translate('bookings.public.cancellationFull', { count: 2 })).toBe('Cancel up to 2 days before start');
    expect(service.translate('Backend validation message')).toBe('Backend validation message');
  });
});

function createService(storage: Storage, browserLanguage: string): TranslationService {
  TestBed.resetTestingModule();
  Object.defineProperty(window.navigator, 'language', {
    value: browserLanguage,
    configurable: true
  });
  Object.defineProperty(window, 'localStorage', {
    value: storage,
    configurable: true
  });
  TestBed.configureTestingModule({});
  return TestBed.inject(TranslationService);
}

function storageMock(): Storage {
  const values = new Map<string, string>();
  return {
    get length() {
      return values.size;
    },
    clear: vi.fn(() => values.clear()),
    getItem: vi.fn((key: string) => values.get(key) ?? null),
    key: vi.fn((index: number) => Array.from(values.keys())[index] ?? null),
    removeItem: vi.fn((key: string) => values.delete(key)),
    setItem: vi.fn((key: string, value: string) => {
      values.set(key, value);
    })
  };
}
