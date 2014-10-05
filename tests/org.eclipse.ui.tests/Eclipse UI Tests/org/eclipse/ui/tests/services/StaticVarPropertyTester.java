/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
