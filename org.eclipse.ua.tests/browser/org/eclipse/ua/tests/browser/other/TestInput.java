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

package org.eclipse.ua.tests.browser.other;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import junit.framework.TestCase;

public class TestInput extends TestCase {

	private final String URL1 = "http://www.eclipse.org";
	private final String URL2 = "http://bugs.eclipse.org";
	private static final String ID1 = "browser.id1";
	private static final String ID2 = "browser.id2";

	public void testCompareWithNull() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		assertFalse(input.equals(null));
	}
	
	public void testCompareWithNullURL() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(null,
				0, ID1);
		assertFalse(input.equals(input2));
		assertFalse(input2.equals(input));
	}

	public void testCompareWithSelf() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		assertTrue(input.equals(input));
	}

	public void testCompareWithSimilar() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(new URL(URL1),
						0, ID1);
		assertTrue(input.equals(input2));
		assertTrue(input.hashCode() == input2.hashCode());
	}

	public void testCompareWithDifferentUrl() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(new URL(URL2),
						0, ID1);
		assertFalse(input.equals(input2));
	}

	public void testCompareWithDifferentId() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(new URL(URL1),
						0, ID2);
		assertFalse(input.equals(input2));
	}

	public void testCompareWithDifferentStyle() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(new URL(URL1),
						1, ID1);
		assertTrue(input.equals(input2));
		assertTrue(input.hashCode() == input2.hashCode());
	}
	
	public void testCompareWithStatusbarVisible() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(new URL(URL1),
				IWorkbenchBrowserSupport.STATUS, ID1);
		assertFalse(input.equals(input2));
	}

	public void testHashWithNullURL() {
		WebBrowserEditorInput input = new WebBrowserEditorInput(null,0, ID1);
		input.hashCode();  // Fails if exception thrown
	}
	
	public void testHashWithNullID() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),0, null);
		input.hashCode();  // Fails if exception thrown
	}
	
}
