import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';

import { TranslatePipe } from './translate.pipe';
import { Language } from './translations';
import { TranslationService } from './translation.service';

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [MatButtonToggleModule, TranslatePipe],
  templateUrl: './language-switcher.component.html',
  styleUrl: './language-switcher.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LanguageSwitcherComponent {
  protected readonly i18n = inject(TranslationService);

  protected setLanguage(language: Language): void {
    this.i18n.setLanguage(language);
  }
}
