/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Creates a wizard that hosts one or more pages based on forms.
 * 
 * @since 3.0
 */
public abstract class FormWizard extends Wizard {
	protected FormToolkit toolkit;

	/**
	 * Creates the wizard that will own its own colors.
	 */
	public FormWizard() {
	}
	/**
	 * Creates a wizard that will use shared colors.
	 * 
	 * @param colors
	 *            shared colors
	 */
	public FormWizard(FormColors colors) {
		toolkit = new FormToolkit(colors);
	}
	/**
	 * Creates form toolkit if missing before creating page controls.
	 * 
	 * @param pageContainer
	 *            the page container widget
	 */
	public void createPageControls(Composite pageContainer) {
		if (toolkit == null)
			toolkit = new FormToolkit(pageContainer.getDisplay());
		super.createPageControls(pageContainer);
	}
	/**
	 * Disposes the toolkit and the wizard itself.
	 */
	public void dispose() {
		super.dispose();
		toolkit.dispose();
	}
}
