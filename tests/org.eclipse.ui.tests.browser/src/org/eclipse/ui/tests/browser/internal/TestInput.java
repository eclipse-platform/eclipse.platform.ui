/*******************************************************************************
 * Copyright (c) 2009, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.browser.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.junit.Test;

public class TestInput {

	private final String URL1 = "http://www.eclipse.org";
	private final String URL2 = "http://bugs.eclipse.org";
	private static final String ID1 = "browser.id1";
	private static final String ID2 = "browser.id2";

	@Test
	public void testCompareWithNull() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		assertNotNull(input);
	}

	@Test
	public void testCompareWithNullURL() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(null,
				0, ID1);
		assertNotEquals(input, input2);
		assertNotEquals(input2, input);
	}

	@Test
	public void testCompareWithSelf() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		assertEquals(input, input);
	}

	@Test
	public void testCompareWithSimilar() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(new URL(URL1),
						0, ID1);
		assertEquals(input, input2);
		assertEquals(input.hashCode(), input2.hashCode());
	}

	@Test
	public void testCompareWithDifferentUrl() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(new URL(URL2),
						0, ID1);
		assertNotEquals(input, input2);
	}

	@Test
	public void testCompareWithDifferentId() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(new URL(URL1),
						0, ID2);
		assertNotEquals(input, input2);
	}

	@Test
	public void testCompareWithDifferentStyle() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(new URL(URL1),
						1, ID1);
		assertEquals(input, input2);
		assertEquals(input.hashCode(), input2.hashCode());
	}

	@Test
	public void testCompareWithStatusbarVisible() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),
				0, ID1);
		WebBrowserEditorInput input2 = new WebBrowserEditorInput(new URL(URL1),
				IWorkbenchBrowserSupport.STATUS, ID1);
		assertNotEquals(input, input2);
	}

	@Test
	public void testHashWithNullURL() {
		WebBrowserEditorInput input = new WebBrowserEditorInput(null,0, ID1);
		input.hashCode();  // Fails if exception thrown
	}

	@Test
	public void testHashWithNullID() throws MalformedURLException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(new URL(URL1),0, null);
		input.hashCode();  // Fails if exception thrown
	}

}
