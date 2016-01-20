/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.other;

import static org.junit.Assert.assertEquals;

import org.eclipse.ui.internal.cheatsheets.views.ViewUtilities;
import org.junit.Test;

public class TestEscape {
	@Test
	public void testEscapeLabelEmpty() {
		assertEquals("", ViewUtilities.escapeForLabel(""));
	}

	@Test
	public void testEscapeLabelNonEmpty() {
		assertEquals("abc", ViewUtilities.escapeForLabel("abc"));
	}

	@Test
	public void testEscapeLabelWithAmpersand() {
		assertEquals("ab&&c", ViewUtilities.escapeForLabel("ab&c"));
	}

	@Test
	public void testEscapeLabelMultipleAmpersand() {
		assertEquals("a&&b&&cd&&e", ViewUtilities.escapeForLabel("a&b&cd&e"));
	}

	@Test
	public void testEscapeLabelRepeatedAmpersand() {
		assertEquals("ab&&&&c", ViewUtilities.escapeForLabel("ab&&c"));
	}

	@Test
	public void testEscapeLabelStartsWithAmpersand() {
		assertEquals("&&abc", ViewUtilities.escapeForLabel("&abc"));
	}

	@Test
	public void testEscapeLabelEndsWithAmpersand() {
		assertEquals("abc&&", ViewUtilities.escapeForLabel("abc&"));
	}

}
