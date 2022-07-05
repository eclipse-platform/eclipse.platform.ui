/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import static org.junit.Assert.assertTrue;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @version 1.0
 */
@RunWith(JUnit4.class)
public class DecoratorTestCase extends AbstractNavigatorTest implements
		ILabelProviderListener {

	private DecoratorDefinition definition;

	private boolean updated = false;

	/**
	 * Constructor for DecoratorTestCase.
	 */
	public DecoratorTestCase() {
		super(DecoratorTestCase.class.getSimpleName());
	}

	/**
	 * Sets up the hierarchy.
	 */
	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();
		showNav();

		WorkbenchPlugin.getDefault().getDecoratorManager().addListener(this);

		DecoratorDefinition[] definitions = WorkbenchPlugin.getDefault()
				.getDecoratorManager().getAllDecoratorDefinitions();
		for (DecoratorDefinition definition2 : definitions) {
			if (definition2.getId().equals(
					"org.eclipse.ui.tests.adaptable.decorator")) {
				definition = definition2;
			}
		}
	}

	private DecoratorManager getDecoratorManager() {
		return WorkbenchPlugin.getDefault().getDecoratorManager();
	}

	/**
	 * Remove the listener.
	 */
	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		getDecoratorManager().removeListener(this);
	}

	/**
	 * Test enabling the contributor
	 */
	@Test
	public void testEnableDecorator() {
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

	/**
	 * Refresh the test decorator.
	 */
	@Test
	public void testRefreshContributor() {

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
	@Override
	public void labelProviderChanged(LabelProviderChangedEvent event) {
		updated = true;
	}

}
