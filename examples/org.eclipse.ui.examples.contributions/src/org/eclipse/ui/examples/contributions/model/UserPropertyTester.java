/*******************************************************************************
 * Copyright (c) 2008, 2023 IBM Corporation and others.
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

package org.eclipse.ui.examples.contributions.model;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Test properties of a Person object provided as a variable.
 *
 * @since 3.4
 */
public class UserPropertyTester extends PropertyTester {
	private static final String IS_ADMIN = "isAdmin"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (!(receiver instanceof Person)) {
			return false;
		}
		Person person = (Person) receiver;
		if (property.equals(IS_ADMIN) && expectedValue instanceof Boolean value) {
			return person.hasAdminRights() == value.booleanValue();
		}
		if (property.equals(ID) && expectedValue instanceof Integer value) {
			return person.getId() == value.intValue();
		}
		return false;
	}
}
