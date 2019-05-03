/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.quickaccess.providers;

import java.util.Objects;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * @since 3.3
 *
 */
public class WizardElement extends QuickAccessElement {

	private IWizardDescriptor wizardDescriptor;

	/* package */ WizardElement(IWizardDescriptor wizardDescriptor) {
		this.wizardDescriptor = wizardDescriptor;
	}

	@Override
	public void execute() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			NewWizardShortcutAction wizardAction = new NewWizardShortcutAction(window, wizardDescriptor);
			wizardAction.run();
		}
	}

	@Override
	public String getId() {
		return wizardDescriptor.getId();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return wizardDescriptor.getImageDescriptor();
	}

	@Override
	public String getLabel() {
		return wizardDescriptor.getLabel() + separator + wizardDescriptor.getDescription();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(wizardDescriptor);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final WizardElement other = (WizardElement) obj;
		return Objects.equals(wizardDescriptor, other.wizardDescriptor);
	}
}
