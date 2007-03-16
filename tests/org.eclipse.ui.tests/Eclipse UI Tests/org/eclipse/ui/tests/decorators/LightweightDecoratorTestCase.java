/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProviderListener;

/**
 * @version 	1.0
 */
public class LightweightDecoratorTestCase extends DecoratorEnablementTestCase
        implements ILabelProviderListener {

    /**
     * Constructor for DecoratorTestCase.
     * @param testName
     */
    public LightweightDecoratorTestCase(String testName) {
        super(testName);
    }

    /**
     * Refresh the test decorator.
     */
    public void testRefreshContributor() throws CoreException {

        updated = false;
        getDecoratorManager().clearCaches();
        definition.setEnabled(true);
        getDecoratorManager().updateForEnablementChange();

        assertTrue("Got an update", updated);
        updated = false;

    }

}
