/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.search;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class EncodedCharacterSearch extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(EncodedCharacterSearch.class);
	}

    public void testIso8859() {
    	SearchTestUtils.searchOneLocale("\u00E1guilaxaxcs", new String[] {"/org.eclipse.ua.tests/data/help/search/testnl8859.htm" }, "en");   	   
    }
    
    public void testIso8859AccentNotIgnored() {
    	SearchTestUtils.searchOneLocale("aguilaxaxcs", new String[0], "en");   	   
    }

    public void testUtf8Accented() {
    	SearchTestUtils.searchOneLocale("acfele\u00F3n", new String[] {"/org.eclipse.ua.tests/data/help/search/testnlUTF8.htm" }, "en");   	   
    }

    public void testUtf8Chinese() {
    	SearchTestUtils.searchOneLocale("\u8FB2\u66C6\u65B0\u5E74", new String[] {"/org.eclipse.ua.tests/data/help/search/testnlUTF8.htm" }, "en");   	   
    }
    
    public void testUtf8Hebrew() {
    	SearchTestUtils.searchOneLocale("\u05D0\u05B7\u05E1\u05B0\u05D8\u05B0\u05E8\u05D5\u05B9\u05E0\u05D5\u05B9\u05DE" 
    			                         + "\u05B0\u05D9\u05B8\u05D4) \u05DC\u05B4\u05E7\u05BC\u05D5\u05BC\u05D9 (\u05D9\u05E8\u05D7 \u05D0\u05D5 \u05E9\u05DE\u05E9", new String[] {"/org.eclipse.ua.tests/data/help/search/testnlUTF8.htm" }, "en");   	   
    }
    
    
}
