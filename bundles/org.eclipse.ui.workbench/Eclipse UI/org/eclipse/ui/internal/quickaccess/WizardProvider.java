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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * @since 3.3
 * 
 */
public class WizardProvider extends QuickAccessProvider {

	private QuickAccessElement[] cachedElements;
	private Map idToElement = new HashMap();

	public QuickAccessElement getElementForId(String id) {
		getElements();
		return (WizardElement) idToElement.get(id);
	}

	public QuickAccessElement[] getElements() {
		if (cachedElements == null) {
			IWizardCategory rootCategory = WorkbenchPlugin.getDefault()
					.getNewWizardRegistry().getRootCategory();
			List result = new ArrayList();
			collectWizards(rootCategory, result);
			IWizardDescriptor[] wizards = (IWizardDescriptor[]) result
					.toArray(new IWizardDescriptor[result.size()]);
			cachedElements = new QuickAccessElement[wizards.length];
			for (int i = 0; i < wizards.length; i++) {
				WizardElement wizardElement = new WizardElement(wizards[i], this);
				cachedElements[i] = wizardElement;
				idToElement.put(wizardElement.getId(), wizardElement);
			}
		}
		return cachedElements;
	}

	private void collectWizards(IWizardCategory category, List result) {
		result.addAll(Arrays.asList(category.getWizards()));
		IWizardCategory[] childCategories = category.getCategories();
		for (int i = 0; i < childCategories.length; i++) {
			collectWizards(childCategories[i], result);
		}
	}

	public String getId() {
		return "org.eclipse.ui.wizards"; //$NON-NLS-1$
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	public String getName() {
		return QuickAccessMessages.QuickAccess_New;
	}
}
