/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Lucas Bullen (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.core.expressions.PropertyTester;

public class EnabledPropertyTester extends PropertyTester {

	private static final String PROPERTY_NAME = "enabled";
	private static boolean isEnabled = true;

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (property.equals(PROPERTY_NAME)) {
			return isEnabled;
		}
		return false;
	}

	public static void setEnabled(boolean isEnabled) {
		EnabledPropertyTester.isEnabled = isEnabled;
	}

}
