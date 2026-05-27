import {enTranslations} from './translations.en';
import {plTranslations} from './translations.pl';

export type Language = 'pl' | 'en';

export const supportedLanguages: readonly Language[] = ['pl', 'en'];

export const translations: Record<Language, Record<string, string>> = {
  pl: plTranslations,
  en: enTranslations,
};
