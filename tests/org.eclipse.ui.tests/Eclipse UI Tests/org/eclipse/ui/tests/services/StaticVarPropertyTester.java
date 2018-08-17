/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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

package org.eclipse.ui.tests.services;

import org.eclipse.core.expressions.PropertyTester;

/**
 * @since 3.4
 *
 */
public class StaticVarPropertyTester extends PropertyTester {
	public static boolean result = false;

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		return result;
	}

}
