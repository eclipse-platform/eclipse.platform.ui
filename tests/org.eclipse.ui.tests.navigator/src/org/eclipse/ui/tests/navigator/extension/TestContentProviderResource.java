/*******************************************************************************
 * Copyright (c) 2009 Oakland Software Incorporated and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software Incorporated - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.internal.navigator.resources.workbench.ResourceExtensionContentProvider;

public class TestContentProviderResource extends
		ResourceExtensionContentProvider {

	public static boolean _returnBadObject;

	public static void resetTest() {
		_returnBadObject = false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {

		if (_returnBadObject && parentElement instanceof IProject)
			return new Object[] { new Object(), new Object(), new Object() };
		return super.getChildren(parentElement);

	}

}
