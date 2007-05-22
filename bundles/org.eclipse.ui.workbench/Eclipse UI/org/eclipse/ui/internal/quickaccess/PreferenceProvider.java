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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceNode;
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
			List list = new ArrayList(); 
			collectElements("", PlatformUI.getWorkbench().getPreferenceManager().getRootSubNodes(), list); //$NON-NLS-1$
			cachedElements = new PreferenceElement[list.size()];
			for (int i = 0; i < list.size(); i++) {
				PreferenceElement preferenceElement = (PreferenceElement) list.get(i);
				cachedElements[i] = preferenceElement;
				idToElement.put(preferenceElement.getId(), preferenceElement);
			}
		}
		return cachedElements;
	}

	/**
	 * @param subNodes
	 * @return
	 */
	private void collectElements(String prefix, IPreferenceNode[] subNodes, List result) {
		for (int i = 0; i < subNodes.length; i++) {
			PreferenceElement preferenceElement = new PreferenceElement(
					subNodes[i], prefix, this);
			result.add(preferenceElement);
			String nestedPrefix = prefix.length() == 0 ? subNodes[i].getLabelText() : (prefix + "/" + subNodes[i].getLabelText());  //$NON-NLS-1$
			collectElements(nestedPrefix, subNodes[i].getSubNodes(), result);
		}
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	public String getName() {
		return QuickAccessMessages.QuickAccess_Preferences;
	}
}
