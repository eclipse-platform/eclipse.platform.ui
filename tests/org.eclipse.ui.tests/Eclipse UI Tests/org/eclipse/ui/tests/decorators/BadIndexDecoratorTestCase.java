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

import org.junit.Test;

/**
 * @since 3.2
 */
public class BadIndexDecoratorTestCase extends DecoratorEnablementTestCase {

	@Override
	protected String getTestDecoratorId() {
		return "org.eclipse.ui.tests.decorators.badIndexDecorator";
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
