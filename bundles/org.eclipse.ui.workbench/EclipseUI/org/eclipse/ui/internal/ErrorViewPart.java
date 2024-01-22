/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.ui.internal.part.StatusPart;
import org.eclipse.ui.part.ViewPart;

/**
 * This part is shown instead the views with errors.
 *
 * @since 3.3
 */
public class ErrorViewPart extends ViewPart {

	private IStatus error;
	private Composite parentControl;

	/**
	 * Creates instance of the class
	 */
	public ErrorViewPart() {
	}

	/**
	 * Creates instance of the class
	 *
	 * @param error the status
	 */
	public ErrorViewPart(IStatus error) {
		this.error = error;
	}

	@Override
	public void createPartControl(Composite parent) {
		parentControl = parent;
		if (error != null) {
			new StatusPart(parent, error);
		}
	}

	@Override
	public void setPartName(String newName) {
		super.setPartName(newName);
	}

	@Override
	public void setFocus() {
		parentControl.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		parentControl = null;
	}

}
