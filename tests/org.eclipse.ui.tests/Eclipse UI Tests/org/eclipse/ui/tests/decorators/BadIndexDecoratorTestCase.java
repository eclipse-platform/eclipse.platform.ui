/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.decorators;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.2
 *
 */
@RunWith(JUnit4.class)
public class BadIndexDecoratorTestCase extends DecoratorEnablementTestCase {

	public BadIndexDecoratorTestCase() {
		super(BadIndexDecoratorTestCase.class.getSimpleName());
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
					"org.eclipse.ui.tests.decorators.badIndexDecorator")) {
				definition = definition2;
			}
		}
	}

	/**
	 * Turn off an on the bad index decorator without
	 * generating an exception.
	 */
	@Test
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
