/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
 *     Mickael Istria (Red Hat Inc.) - [266030] Allow "others" working set
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.workingsets;

import java.net.URL;
import java.util.Collections;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Provides a text label and icon for Working Sets.
 *
 */
public class WorkingSetsLabelProvider implements ILabelProvider {

	private WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
	private Image workingSetImage;

	@Override
	public Image getImage(Object element) {
		if (element instanceof IWorkingSet)
			return labelProvider.getImage(element);
		if (element == WorkingSetsContentProvider.OTHERS_WORKING_SET) {
			return getWorkingSetImage();
		}
		return null;
	}

	private Image getWorkingSetImage() {
		if (workingSetImage == null) {
			URL iconUrl = FileLocator.find(WorkbenchNavigatorPlugin.getDefault().getBundle(),
					Path.fromPortableString("icons/full/obj16/otherprojects_workingsets.png"), //$NON-NLS-1$
					Collections.emptyMap());
			workingSetImage = ImageDescriptor.createFromURL(iconUrl).createImage();
		}
		return workingSetImage;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IWorkingSet)
			return ((IWorkingSet) element).getLabel();
		if (element == WorkingSetsContentProvider.OTHERS_WORKING_SET) {
			return WorkbenchNavigatorMessages.workingSet_others;
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
		if (workingSetImage != null) {
			workingSetImage.dispose();
		}
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

}
