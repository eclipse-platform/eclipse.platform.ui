/*******************************************************************************
 * Copyright (c) 2009, 2016 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 271720)
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 502228
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.fail;

import org.eclipse.jface.internal.databinding.swt.DateTimeSelectionProperty;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class DateTimeSelectionPropertyTest extends AbstractSWTTestCase {
	DateTime dateTime;
	DateTimeSelectionProperty property;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		dateTime = new DateTime(getShell(), SWT.DATE);
		property = new DateTimeSelectionProperty();
	}

	@Test
	public void testSetValue_NullNotThrowingNullPointerException() {
		try {
			property.setValue(dateTime, null);
		} catch (NullPointerException notExpected) {
			fail("No NPE should be thrown, because a null value should cause the method to return silently");
		}
	}
}
