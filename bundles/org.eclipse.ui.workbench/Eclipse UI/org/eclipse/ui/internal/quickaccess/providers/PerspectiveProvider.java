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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess.providers;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.quickaccess.QuickAccessMessages;
import org.eclipse.ui.internal.quickaccess.QuickAccessProvider;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * @since 3.3
 *
 */
public class PerspectiveProvider extends QuickAccessProvider {

	private QuickAccessElement[] cachedElements;
	private Map<String, PerspectiveElement> idToElement = new HashMap<>();

	@Override
	public String getId() {
		return "org.eclipse.ui.perspectives"; //$NON-NLS-1$
	}

	@Override
	public QuickAccessElement getElementForId(String id) {
		getElements();
		return idToElement.get(id);
	}

	@Override
	public QuickAccessElement[] getElements() {
		if (cachedElements == null) {
			IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry()
					.getPerspectives();
			cachedElements = new QuickAccessElement[perspectives.length];
			for (int i = 0; i < perspectives.length; i++) {
				if (!WorkbenchActivityHelper.filterItem(perspectives[i])) {
					PerspectiveElement perspectiveElement = new PerspectiveElement(perspectives[i]);
					idToElement.put(perspectiveElement.getId(), perspectiveElement);
				}
			}
			cachedElements = idToElement.values().toArray(new QuickAccessElement[idToElement.size()]);
		}
		return cachedElements;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE);
	}

	@Override
	public String getName() {
		return QuickAccessMessages.QuickAccess_Perspectives;
	}

	@Override
	protected void doReset() {
		cachedElements = null;
		idToElement.clear();
	}
}
