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

package org.eclipse.ua.tests.help.webapp;

import org.eclipse.help.internal.webapp.data.UrlUtil;

import junit.framework.TestCase;

/**
 * Tests for locale related code in UrlUtil
 */
public class LocaleTest extends TestCase {

	public void testFixLocaleNull() {
		assertNull(UrlUtil.cleanLocale(null));
	}
	
	public void testFixLocaleWithIllegalChars() {
		assertEquals("ab-cd______ef", UrlUtil.cleanLocale("ab-cd\n\r_\"\'_ef"));
	}
	
}
