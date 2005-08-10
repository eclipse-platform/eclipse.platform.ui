/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.multieditor;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.1.1
 */
public class MultiEditorTestSuite extends TestSuite {

	public static Test suite() {
		return new MultiEditorTestSuite();
	}
    /**
     * Construct the test suite.
     */
    public MultiEditorTestSuite() {
        addTestSuite(MultiEditorTest.class);
    }
}
