import { Pipe, PipeTransform, inject } from '@angular/core';

import { TranslationService } from './translation.service';

@Pipe({
  name: 'translate',
  standalone: true,
  pure: false
})
export class TranslatePipe implements PipeTransform {
  private readonly translations = inject(TranslationService);

  transform(key: string | null | undefined, params: Record<string, string | number> = {}): string {
    return key ? this.translations.translate(key, params) : '';
  }
}
