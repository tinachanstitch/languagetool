/* LanguageTool, a natural language style checker (http://www.languagetool.org) 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
/**
 * Tokenizes Russian text into sentences by looking for typical end-of-sentence markers,
 * but considering exceptions (e.g. abbreviations).
 * 
 * @author Yakov Reztsov 
*/

package de.danielnaber.languagetool.tokenizers.ru;

import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

public class RussianSentenceTokenizer extends SentenceTokenizer {

  private static final String[] ABBREV_LIST = {
    "м", "мм", "в", "вв", "г", "гг", "гл", "др", "д", "ед",
    "к", "кв", "кл", "кол", "коп", "куб", "л", "лл", "мл",
    "млн", "млрд", "наб", "нач", "обл", "обр", "ок", "пер", "ед", "п",
    "пл", "пос", "пр", "просп", "р", "руб", "с", "сб", "св", "см",
    "соч", "ср", "ст", "стр", "т", "тт", "туп", "тыс", "ч", "шт", "экз",
    "мин", "макс"};

  // Month names like "Декабрь" that should not be considered a sentence
  // boundary in string like "13. Декабрь".
   
  private static final String[] MONTH_NAMES = { "Январь", "Февраль", "Март", "Апрель", "Май",
      "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь" };

  public RussianSentenceTokenizer() {
    super(ABBREV_LIST);
    super.monthNames = MONTH_NAMES;
  }
 
}
