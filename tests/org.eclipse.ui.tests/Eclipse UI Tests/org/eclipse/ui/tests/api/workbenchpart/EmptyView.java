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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @since 3.0
 */
public class EmptyView extends ViewPart {

	public static String ID = "org.eclipse.ui.tests.workbenchpart.EmptyView";

	/**
	 *
	 */
	public EmptyView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {

	}

	@Override
	public void setFocus() {

	}

	@Override
	public void setContentDescription(String description) {
		super.setContentDescription(description);
	}

	@Override
	public void setPartName(String partName) {
		super.setPartName(partName);
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
	}
}
