/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * @since 3.3
 *
 */
public class PreferenceProvider extends QuickAccessProvider {

	private QuickAccessElement[] cachedElements;
	private Map<String, PreferenceElement> idToElement = new HashMap<>();

	@Override
	public String getId() {
		return "org.eclipse.ui.preferences"; //$NON-NLS-1$
	}

	@Override
	public QuickAccessElement getElementForId(String id) {
		getElements();
		return idToElement.get(id);
	}

	@Override
	public QuickAccessElement[] getElements() {
		if (cachedElements == null) {
			List<PreferenceElement> list = new ArrayList<>();
			collectElements("", PlatformUI.getWorkbench().getPreferenceManager().getRootSubNodes(), list); //$NON-NLS-1$
			cachedElements = new PreferenceElement[list.size()];
			for (int i = 0; i < list.size(); i++) {
				PreferenceElement preferenceElement = list.get(i);
				cachedElements[i] = preferenceElement;
				idToElement.put(preferenceElement.getId(), preferenceElement);
			}
		}
		return cachedElements;
	}

	private void collectElements(String prefix, IPreferenceNode[] subNodes,
			List<PreferenceElement> result) {
		for (int i = 0; i < subNodes.length; i++) {
			if (!WorkbenchActivityHelper.filterItem(subNodes[i])) {
				PreferenceElement preferenceElement = new PreferenceElement(subNodes[i], prefix,
						this);
				result.add(preferenceElement);
				String nestedPrefix = prefix.length() == 0 ? subNodes[i].getLabelText() : (prefix
						+ "/" + subNodes[i].getLabelText()); //$NON-NLS-1$
				collectElements(nestedPrefix, subNodes[i].getSubNodes(), result);
			}
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	@Override
	public String getName() {
		return QuickAccessMessages.QuickAccess_Preferences;
	}

	@Override
	protected void doReset() {
		cachedElements = null;
		idToElement.clear();
	}
}
