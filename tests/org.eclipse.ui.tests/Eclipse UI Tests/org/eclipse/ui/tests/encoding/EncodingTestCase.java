/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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
package org.eclipse.ui.tests.encoding;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * The EncodingTestCase is the suite that tests the 3.1
 * encoding support.
 */
public class EncodingTestCase extends UITestCase {

	/**
	 * Create a new instance of the receiver.
	 * @param testName
	 */
	public EncodingTestCase(String testName) {
		super(testName);
	}

	/**
	 * Test that the workbench encodings are all valid. The
	 * suite includes an invalid one.
	 *
	 */
	public void testWorkbenchEncodings() {
		List<String> encodings = WorkbenchEncoding.getDefinedEncodings();

		for (String encoding : encodings) {
			try {
				Assert.isTrue(Charset.isSupported(encoding), "Unsupported charset " + encoding);

			} catch (IllegalCharsetNameException e) {
				Assert.isTrue(false, "Unsupported charset " + encoding);
			}
		}
	}
}
