import { Injectable, signal } from '@angular/core';

import { Language, supportedLanguages, translations } from './translations';

const storageKey = 'shooters-platform.language';

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private readonly currentLanguage = signal<Language>(this.initialLanguage());

  readonly language = this.currentLanguage.asReadonly();

  constructor() {
    this.applyDocumentLanguage(this.currentLanguage());
  }

  locale(): string {
    return this.currentLanguage() === 'pl' ? 'pl-PL' : 'en-US';
  }

  setLanguage(language: Language): void {
    this.currentLanguage.set(language);
    window.localStorage.setItem(storageKey, language);
    this.applyDocumentLanguage(language);
  }

  translate(key: string, params: Record<string, string | number> = {}): string {
    const language = this.currentLanguage();
    const text = translations[language][key] ?? translations.en[key] ?? key;
    return text.replace(/\{(\w+)}/g, (match, param: string) => String(params[param] ?? match));
  }

  isSupported(language: string): language is Language {
    return supportedLanguages.includes(language as Language);
  }

  private initialLanguage(): Language {
    const stored = window.localStorage.getItem(storageKey);
    if (stored && this.isSupported(stored)) {
      return stored;
    }

    const browserLanguage = window.navigator.language.toLowerCase();
    return browserLanguage.startsWith('en') ? 'en' : 'pl';
  }

  private applyDocumentLanguage(language: Language): void {
    document.documentElement.lang = language;
  }
}
