/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.wizard.Wizard;

public class NewEngineWizard extends Wizard {
	private EngineTypeDescriptor[] engineTypes;

	private EngineTypeWizardPage selectionPage;

	public NewEngineWizard(EngineTypeDescriptor[] engineTypes) {
		setWindowTitle(Messages.NewEngineWizard_wtitle);
		setDefaultPageImageDescriptor(HelpUIResources
				.getImageDescriptor(IHelpUIConstants.IMAGE_SEARCH_WIZ));
		this.engineTypes = engineTypes;
	}

	public void addPages() {
		selectionPage = new EngineTypeWizardPage(engineTypes);
		addPage(selectionPage);
	}

	public boolean performFinish() {
		return true;
	}

	public EngineTypeDescriptor getSelectedEngineType() {
		return selectionPage.getSelectedEngineType();
	}
}