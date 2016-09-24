package org.eclipse.core.tests.internal.databinding;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.internal.databinding.BindingMessages;
import org.junit.Test;

/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

/**
 * @since 3.2
 *
 */
public class BindingMessagesTest {
	@Test
	public void testFormatString() throws Exception {
		String key = "Validate_NumberOutOfRangeError";
		String result = BindingMessages.formatString(key, new Object[] {"1", "2"});
		assertFalse("key should not be returned", key.equals(result));
	}

	@Test
	public void testFormatStringForKeyNotFound() throws Exception {
		String key = "key_that_does_not_exist";
		String result = BindingMessages.formatString(key, null);
		assertTrue(key.equals(result));
	}
}
