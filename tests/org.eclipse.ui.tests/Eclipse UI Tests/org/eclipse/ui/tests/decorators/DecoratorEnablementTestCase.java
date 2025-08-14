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
package org.eclipse.ui.tests.decorators;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version 	1.0
 */
public abstract class DecoratorEnablementTestCase extends AbstractNavigatorTest
		implements ILabelProviderListener {

	protected DecoratorDefinition definition;

	protected boolean updated = false;

	protected abstract String getTestDecoratorId();

	@Before
	public final void setUpHierarchy() throws CoreException {
		createTestFile();
		showNav();

		getDecoratorManager().addListener(this);

		DecoratorDefinition[] definitions = getDecoratorManager().getAllDecoratorDefinitions();
		for (DecoratorDefinition definition2 : definitions) {
			if (definition2.getId().equals(getTestDecoratorId())) {
				definition = definition2;
			}
		}
	}

	protected DecoratorManager getDecoratorManager() {
		return WorkbenchPlugin.getDefault().getDecoratorManager();
	}

	@After
	public final void removeListener() throws Exception {
		getDecoratorManager().removeListener(this);
	}

	/**
	 * Test enabling the contributor
	 */
	@Test
	public void testEnableDecorator()  {
		getDecoratorManager().clearCaches();
		definition.setEnabled(true);
		getDecoratorManager().updateForEnablementChange();

	}

	/**
	 * Test disabling the contributor
	 */
	@Test
	public void testDisableDecorator() {
		getDecoratorManager().clearCaches();
		definition.setEnabled(false);
		getDecoratorManager().updateForEnablementChange();
	}

	/*
	 * @see ILabelProviderListener#labelProviderChanged(LabelProviderChangedEvent)
	 */
	@Override
	public void labelProviderChanged(LabelProviderChangedEvent event) {
		updated = true;
	}

}
