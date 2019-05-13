/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.internal.util.Util;

/**
 * @since 3.0
 */
public class OverriddenTitleView extends EmptyView {

	String overriddenTitle = "OverriddenTitle";

	/**
	 *
	 */
	public OverriddenTitleView() {
		super();
	}

	@Override
	public String getTitle() {
		return overriddenTitle;
	}

	public void customSetTitle(String title) {
		overriddenTitle = Util.safeString(title);

		firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
	}

}
