/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test to ensure that <code>WorkbenchPlugin.createExtension()</code> will only 
 * throw <code>CoreException</code>s if there is a problem creating the 
 * extension.
 * 
 * @since 3.0
 */
public class Bug42616Test extends UITestCase {

    /**
     * @param testName
     */
    public Bug42616Test(String testName) {
        super(testName);
    }

    public void testErrorCondition() {
        try {
            WorkbenchPlugin.createExtension(null, null);
            fail("createExtension with nulls succeeded");
        } catch (CoreException e) {
            // ensure that exception has a root cause.
            assertNotNull("Cause is null", e.getStatus().getException());
        } catch (Throwable t) {
            fail("Throwable not wrapped in core exception.");
        }
    }
}
