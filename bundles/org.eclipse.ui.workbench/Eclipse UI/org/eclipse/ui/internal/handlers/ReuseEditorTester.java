/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Test to see if pinning is available.
 *
 */
public class ReuseEditorTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
	}

}
