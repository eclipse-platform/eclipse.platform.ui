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
package org.eclipse.ui.tests.decorators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;

/**
 * @version 	1.0
 */
public class ExceptionDecoratorTestCase extends DecoratorEnablementTestCase
        implements ILabelProviderListener {
    private Collection problemDecorators = new ArrayList();

    private DecoratorDefinition light;

    /**
     * Constructor for DecoratorTestCase.
     * @param testName
     */
    public ExceptionDecoratorTestCase(String testName) {
        super(testName);
    }

    /**
     * Sets up the hierarchy.
     */
    protected void doSetUp() throws Exception {
        super.doSetUp();
        //reset the static fields so that the decorators will fail
        HeavyNullImageDecorator.fail = true;
        HeavyNullTextDecorator.fail = true;
        NullImageDecorator.fail = true;
        DecoratorDefinition[] definitions = WorkbenchPlugin.getDefault()
                .getDecoratorManager().getAllDecoratorDefinitions();
        for (int i = 0; i < definitions.length; i++) {
            String id = definitions[i].getId();
            if (id.equals("org.eclipse.ui.tests.heavyNullImageDecorator")
                    || id.equals("org.eclipse.ui.tests.heavyNullTextDecorator")) {
                definitions[i].setEnabled(true);
                problemDecorators.add(definitions[i]);
            }

            //Do not cache the light one - the disabling issues
            //still need to be worked out.
            if (id.equals("org.eclipse.ui.tests.lightNullImageDecorator")) {
                definitions[i].setEnabled(true);
                light = definitions[i];
            }
        }
    } /* (non-Javadoc)
     * @see org.eclipse.ui.tests.navigator.LightweightDecoratorTestCase#doTearDown()
     */

    protected void doTearDown() throws Exception {
        super.doTearDown();

        //Need to wait for decoration to end to allow for all 
        //errors to occur
        try {
            Platform.getJobManager().join(DecoratorManager.FAMILY_DECORATE,
                    null);
        } catch (OperationCanceledException e) {
        } catch (InterruptedException e) {
        }

        //Be sure that the decorators were all disabled on errors.
        Iterator problemIterator = problemDecorators.iterator();
        while (problemIterator.hasNext()) {
            DecoratorDefinition next = (DecoratorDefinition) problemIterator
                    .next();
            assertFalse("Enabled " + next.getName(), next.isEnabled());
        }

        //Turnoff the lightweight one so as not to clutter the methods.
        light.setEnabled(false);
    }
}
