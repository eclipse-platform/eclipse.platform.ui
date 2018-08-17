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

import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.1
 */
public class EncodingTests extends DynamicTestCase {

	private static final String ENCODING = "Cp1257";
	/**
	 * @param testName
	 */
	public EncodingTests(String testName) {
		super(testName);
		// TODO Auto-generated constructor stub
	}

	public void testWorkbenchEncodings() {
		assertFalse(WorkbenchEncoding.getDefinedEncodings().contains(ENCODING));
		getBundle();
		assertTrue(WorkbenchEncoding.getDefinedEncodings().contains(ENCODING));
		removeBundle();
		assertFalse(WorkbenchEncoding.getDefinedEncodings().contains(ENCODING));
	}

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
