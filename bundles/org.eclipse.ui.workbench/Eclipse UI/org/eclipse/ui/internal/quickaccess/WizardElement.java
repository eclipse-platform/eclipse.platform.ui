/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * @since 3.3
 * 
 */
public class WizardElement extends QuickAccessElement {

	private static final String separator = " - "; //$NON-NLS-1$

	private IWizardDescriptor wizardDescriptor;

	/* package */WizardElement(IWizardDescriptor wizardDescriptor, WizardProvider wizardProvider) {
		super(wizardProvider);
		this.wizardDescriptor = wizardDescriptor;
	}

	public void execute() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			NewWizardShortcutAction wizardAction = new NewWizardShortcutAction(
					window, wizardDescriptor);
			wizardAction.run();
		}
	}

	public String getId() {
		return wizardDescriptor.getId();
	}

	public ImageDescriptor getImageDescriptor() {
		return wizardDescriptor.getImageDescriptor();
	}

	public String getLabel() {
		return wizardDescriptor.getLabel() + separator
				+ wizardDescriptor.getDescription();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((wizardDescriptor == null) ? 0 : wizardDescriptor.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final WizardElement other = (WizardElement) obj;
		if (wizardDescriptor == null) {
			if (other.wizardDescriptor != null)
				return false;
		} else if (!wizardDescriptor.equals(other.wizardDescriptor))
			return false;
		return true;
	}
}
