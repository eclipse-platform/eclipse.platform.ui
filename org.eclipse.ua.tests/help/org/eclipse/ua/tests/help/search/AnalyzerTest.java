/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.search;

import org.eclipse.help.internal.search.AnalyzerDescriptor;

import junit.framework.TestCase;

public class AnalyzerTest extends TestCase {
	
	private final String[] supportedLanguages = { "en", "pt", "ja", "zh", "cs", "de", "el", "fr", "nl", "ru", "ar" };
	 
    public void testEnglishAnalyzer() {
    	checkAnalyzer("en", "en");
    }

    public void testEnglishUsAnalyzer() {
    	checkAnalyzer("en_us", "en");
    }

    public void testGermanAnalyzer_de() {
    	checkAnalyzer("de", "de");
    }
    
    public void testGermanAnalyzer_de_DE() {
    	checkAnalyzer("de_DE", "de");
    }	   

    public void testJapaneseAnalyzer() {
    	checkAnalyzer("ja", "ja");
    }
    
    public void testFrenchAnalyzer() {
    	checkAnalyzer("fr", "fr");
    }
    
    public void testChineseAnalyzer() {
    	checkAnalyzer("zh", "zh");
    }

    // Korean and japanese share an analyzer
    public void testKoreanAnalyzer() {
    	checkAnalyzer("ko", "ja");
    }

    public void testRussianAnalyzer() {
    	checkAnalyzer("ru", "ru");
    }

    public void testGreekAnalyzer() {
    	checkAnalyzer("el", "el");
    }
    
    // Uses default
    public void testSpanishAnalyzer() {
    	checkAnalyzer("es", "ar");
    }

    public void testPortugueseAnalyzer() {
    	checkAnalyzer("pt", "pt");
    }
    
    public void testDutchAnalyzer() {
    	checkAnalyzer("nl", "nl");
    }
    
    public void testCzechAnalyzer() {
    	checkAnalyzer("cs", "cs");
    }

    // Uses default
    public void testArabicAnalyzer() {
    	checkAnalyzer("ar", "ar");
    }
    
    // Use default
    public void testHebrewAnalyzer() {
    	checkAnalyzer("il", "ar");
    }
    
    private void checkAnalyzer(String language, String analyzerKind) {
		AnalyzerDescriptor an = new AnalyzerDescriptor(language);
;
		for (int i = 0; i < supportedLanguages.length; i++) {
			String nextLocale = supportedLanguages[i];
			AnalyzerDescriptor expected = new AnalyzerDescriptor(nextLocale)
;
			if (nextLocale.equals(analyzerKind)) {
				assertEquals("Comparing " + nextLocale + " to " + language, expected.getAnalyzerClassName(), an.getAnalyzerClassName());
			} else {
				assertFalse("Both " + nextLocale + " and " + language + " have value of " + expected.getAnalyzerClassName(), expected.getAnalyzerClassName().equals(an.getAnalyzerClassName()));
				
			}
		}
		
	}

}
