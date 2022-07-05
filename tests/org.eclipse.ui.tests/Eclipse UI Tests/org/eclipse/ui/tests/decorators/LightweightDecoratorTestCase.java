/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @version 1.0
 */
@RunWith(JUnit4.class)
public class LightweightDecoratorTestCase extends DecoratorEnablementTestCase {

	/**
	 * Constructor for DecoratorTestCase.
	 */
	public LightweightDecoratorTestCase() {
		super(LightweightDecoratorTestCase.class.getSimpleName());
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

}
