/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.encoding;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Iterator;
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
		List encodings = WorkbenchEncoding.getDefinedEncodings();
		Iterator iterator = encodings.iterator();

		while (iterator.hasNext()) {
			String nextEncoding = (String) iterator.next();
			try {
				Assert.isTrue(Charset.isSupported(nextEncoding), "Unsupported charset " + nextEncoding);
				
			} catch (IllegalCharsetNameException e) {
				Assert.isTrue(false, "Unsupported charset " + nextEncoding);
			}
			

		}
	}

}
