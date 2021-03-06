/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import junit.framework.TestCase;

public class WikipediaTextFilterTest extends TestCase {

  final SwebleWikipediaTextFilter swebleFilter = new SwebleWikipediaTextFilter();
  
  public void testImageRemoval() throws Exception {
    assertExtract("foo [[Datei:Bundesarchiv Bild 183-1990-0803-017.jpg|miniatur|Mit Lothar de Maizière im August 1990]] bar",
                  "foo bar");
  }

  public void testRemovalOfImageWithLink() throws Exception {
    assertExtract("foo [[Datei:Bundesarchiv Bild 183-1990-0803-017.jpg|miniatur|Mit [[Lothar de Maizière]] im August 1990]] bar [[Link]]",
                  "foo bar Link");
  }

  public void testLink1() throws Exception {
    assertExtract("foo [[Test]] bar", "foo Test bar");
  }

  public void testLink2() throws Exception {
    assertExtract("foo [[Target|visible link]] bar", "foo visible link bar");
  }

  public void testEntity() throws Exception {
    assertExtract("rund 20&nbsp;Kilometer südlich", "rund 20 Kilometer südlich");
  }

  public void testLists() throws Exception {
    assertExtract("# one\n# two\n", "one\n\ntwo");
    assertExtract("* one\n* two\n", "one\n\ntwo");
  }

  public void testOtherStuff() throws Exception {
    assertExtract("Daniel Guerin, ''[http://theanarchistlibrary.org Anarchism: From Theory to Practice]''",
                  "Daniel Guerin, Anarchism: From Theory to Practice");
    assertExtract("foo <ref>\"At the end of the century in France [http://theanarchistlibrary.org] [[Daniel Guérin]]. ''Anarchism'']</ref>",
                  "foo");
    assertExtract("* [http://theanarchistlibrary.org ''Anarchism: From Theory to Practice''] by [[Daniel Guerin]]. Monthly Review Press.\n",
                  "Anarchism: From Theory to Practice by Daniel Guerin. Monthly Review Press.");
    assertExtract("The <code>$pattern</code>", "The $pattern");
    assertExtract("<source lang=\"bash\">some source</source>", "some source");
  }

  private void assertExtract(String input, String expected) {
    assertEquals(expected, swebleFilter.filter(input).getPlainText());
  }

}
