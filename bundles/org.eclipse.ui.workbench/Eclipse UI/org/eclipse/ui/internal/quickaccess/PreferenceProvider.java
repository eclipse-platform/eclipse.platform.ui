/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * @since 3.3
 * 
 */
public class PreferenceProvider extends QuickAccessProvider {

	private QuickAccessElement[] cachedElements;
	private Map idToElement = new HashMap();

	public String getId() {
		return "org.eclipse.ui.preferences"; //$NON-NLS-1$
	}

	public QuickAccessElement getElementForId(String id) {
		getElements();
		return (PreferenceElement) idToElement.get(id);
	}

	public QuickAccessElement[] getElements() {
		if (cachedElements == null) {
			List list = PlatformUI.getWorkbench().getPreferenceManager().getElements(PreferenceManager.PRE_ORDER);
			Set uniqueElements = new HashSet(list);
			IPreferenceNode[] preferences = (IPreferenceNode[]) uniqueElements.toArray(new IPreferenceNode[uniqueElements.size()]);
			cachedElements = new QuickAccessElement[preferences.length];
			for (int i = 0; i < preferences.length; i++) {
				PreferenceElement preferenceElement = new PreferenceElement(
						preferences[i], this);
				cachedElements[i] = preferenceElement;
				idToElement.put(preferenceElement.getId(), preferenceElement);
			}
		}
		return cachedElements;
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	public String getName() {
		return QuickAccessMessages.QuickAccess_Preferences;
	}
}
