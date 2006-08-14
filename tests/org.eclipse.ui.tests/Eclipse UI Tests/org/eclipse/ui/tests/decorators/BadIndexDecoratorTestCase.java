/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.decorators;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;

/**
 * @since 3.2
 *
 */
public class BadIndexDecoratorTestCase extends DecoratorEnablementTestCase {
	
	 /**
	 * @param testName
	 */
	public BadIndexDecoratorTestCase(String testName) {
		super(testName);
	}

	/**
     * Sets up the hierarchy.
     */
    protected void doSetUp() throws Exception {
        super.doSetUp();
        createTestFile();
        showNav();

        WorkbenchPlugin.getDefault().getDecoratorManager().addListener(this);

        DecoratorDefinition[] definitions = WorkbenchPlugin.getDefault()
                .getDecoratorManager().getAllDecoratorDefinitions();
        for (int i = 0; i < definitions.length; i++) {
            if (definitions[i].getId().equals(
                    "org.eclipse.ui.tests.decorators.badIndexDecorator"))
                definition = definitions[i];
        }
    }
    
    /**
     * Turn off an on the bad index decorator without
     * generating an exception.
     */
    public void testNoException() {

        updated = false;
        getDecoratorManager().clearCaches();
        definition.setEnabled(true);
        getDecoratorManager().updateForEnablementChange();
        definition.setEnabled(false);
        getDecoratorManager().updateForEnablementChange();
        updated = false;

    }

}
