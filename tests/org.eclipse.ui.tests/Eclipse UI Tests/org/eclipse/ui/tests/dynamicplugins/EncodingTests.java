/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.dynamicplugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class EncodingTests extends DynamicTestCase {

	private static final String ENCODING = "Cp1257";

	public EncodingTests() {
		super(EncodingTests.class.getSimpleName());
	}

	@Test
	public void testWorkbenchEncodings() {
		assertFalse(WorkbenchEncoding.getDefinedEncodings().contains(ENCODING));
		getBundle();
		assertTrue(WorkbenchEncoding.getDefinedEncodings().contains(ENCODING));
		removeBundle();
		assertFalse(WorkbenchEncoding.getDefinedEncodings().contains(ENCODING));
	}

	@Test
	public void testIDEEncodings() {
		assertFalse(IDEEncoding.getIDEEncodings().contains(ENCODING));
		getBundle();
		assertTrue(IDEEncoding.getIDEEncodings().contains(ENCODING));
		removeBundle();
		assertFalse(IDEEncoding.getIDEEncodings().contains(ENCODING));
	}

	@Override
	protected String getExtensionId() {
		return "newEncoding1.testDynamicEncodingAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_ENCODINGS;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newEncoding1";
	}
}
