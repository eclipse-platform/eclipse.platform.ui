/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import org.eclipse.help.internal.webapp.data.PrintData;
import junit.framework.TestCase;

/**
 * Test for methods in PrintData
 */

public class PrintSubtopics extends TestCase {

	public void testHeadingInsertion() {
		String result = PrintData.injectHeading("<body> <p>Title</p>", "1");
		assertEquals("<body> <p><a id=\"section1\">1. </a>Title</p>", result);
	}
	
	public void testHeaderInsertionSkipsWhitespace() {
		checkHeadingInsertion("<body> <p>  \n\r</p><h1>", "Title</h1>");
	}

	public void testAccentedCharacter() {
		checkHeadingInsertion("<body> <p>", "\u00E1guila</p>");
	}

	public void testinvertedQuestionmark() {
		checkHeadingInsertion("<body> <p>", "\u00BFQu\u00E9 es Eclipse?</p>");
	}
	
	public void testSlash() {
		checkHeadingInsertion("<body> <p>", "/usr/bin</p>");
	}

	public void testChineseCharacter() {
		checkHeadingInsertion("<body> <p>", "\u623F\u5B50</p>");
	}
	
	public void testChineseExtbCharacter() {
		checkHeadingInsertion("<body> <p>", "\uD840\uDC06</p>");
	}
	
	/*
	 * Check that insertions occur between preInsert and postInsert 
	 */
	public void checkHeadingInsertion(String preInsert, String postInsert) {
		String result = PrintData.injectHeading(preInsert + postInsert, "1");
		assertEquals(preInsert + "<a id=\"section1\">1. </a>" + postInsert, result);
	}
	
}
