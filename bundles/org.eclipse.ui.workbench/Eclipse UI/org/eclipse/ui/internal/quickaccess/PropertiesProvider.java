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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;

/**
 * @since 3.3
 * 
 */
public class PropertiesProvider extends QuickAccessProvider {

	private Map idToElement;

	public QuickAccessElement getElementForId(String id) {
		getElements();
		return (PropertiesElement) idToElement.get(id);
	}

	public QuickAccessElement[] getElements() {
		if (idToElement == null) {
			idToElement = new HashMap();
			IWorkbenchPage activePage = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			if (activePage != null) {
				PropertyPageManager pageManager = new PropertyPageManager();
				ISelection selection = activePage.getSelection();
				if (selection instanceof IStructuredSelection
						&& !selection.isEmpty()) {
					Object element = ((IStructuredSelection) selection)
							.getFirstElement();
					PropertyPageContributorManager.getManager().contribute(
							pageManager, element);
					List list = pageManager
							.getElements(PreferenceManager.PRE_ORDER);
					IPreferenceNode[] properties = (IPreferenceNode[]) list
							.toArray(new IPreferenceNode[list.size()]);
					for (int i = 0; i < properties.length; i++) {
						PropertiesElement propertiesElement = new PropertiesElement(
								element, properties[i], this);
						idToElement.put(propertiesElement.getId(),
								propertiesElement);
					}
				}
			}
		}
		return (QuickAccessElement[]) idToElement.values().toArray(
				new QuickAccessElement[idToElement.values().size()]);
	}

	public String getId() {
		return "org.eclipse.ui.properties"; //$NON-NLS-1$
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	public String getName() {
		return QuickAccessMessages.QuickAccess_Properties;
	}
}
