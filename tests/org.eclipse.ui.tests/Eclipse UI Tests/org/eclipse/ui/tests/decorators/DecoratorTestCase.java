/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;

/**
 * @version 	1.0
 */
public class DecoratorTestCase extends AbstractNavigatorTest implements
        ILabelProviderListener {

    private DecoratorDefinition definition;

    private boolean updated = false;

    /**
     * Constructor for DecoratorTestCase.
     * @param testName
     */
    public DecoratorTestCase(String testName) {
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
                    "org.eclipse.ui.tests.adaptable.decorator"))
                definition = definitions[i];
        }
    }

    private DecoratorManager getDecoratorManager() {
        return WorkbenchPlugin.getDefault().getDecoratorManager();
    }

    /**
     * Remove the listener.
     */
    protected void doTearDown() throws Exception {
        super.doTearDown();
        getDecoratorManager().removeListener(this);
    }

    /**
     * Make a label changed event for resource.
     */
    private LabelProviderChangedEvent getLabelChangedEvent(IResource resource) {
        return new LabelProviderChangedEvent(getDecoratorManager(), resource);
    }

    /**
     * Test enabling the contributor
     */
    public void testEnableDecorator() throws CoreException {
        getDecoratorManager().clearCaches();
        definition.setEnabled(true);
        getDecoratorManager().updateForEnablementChange();

    }

    /**
     * Test disabling the contributor
     */
    public void testDisableDecorator() throws CoreException {
        getDecoratorManager().clearCaches();
        definition.setEnabled(false);
        getDecoratorManager().updateForEnablementChange();
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

    /*
     * @see ILabelProviderListener#labelProviderChanged(LabelProviderChangedEvent)
     */
    public void labelProviderChanged(LabelProviderChangedEvent event) {
        updated = true;
    }

}