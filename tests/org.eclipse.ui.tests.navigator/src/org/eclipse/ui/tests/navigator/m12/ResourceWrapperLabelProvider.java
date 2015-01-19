/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *     Fair Isaac Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.m12;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.navigator.m12.model.M1Project;
import org.eclipse.ui.tests.navigator.m12.model.M1Folder;
import org.eclipse.ui.tests.navigator.m12.model.M1File;
import org.eclipse.ui.tests.navigator.m12.model.ResourceWrapper;
import org.eclipse.ui.ide.IDE.SharedImages;

public class ResourceWrapperLabelProvider implements ILabelProvider {

	@Override
	public Image getImage(Object element) {
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		if (element instanceof M1Project) {
			return sharedImages.getImage(SharedImages.IMG_OBJ_PROJECT);
		} else if (element instanceof M1Folder) {
			return sharedImages.getImage(ISharedImages.IMG_OBJ_FOLDER);
		} else if (element instanceof M1File) {
			return sharedImages.getImage(ISharedImages.IMG_OBJ_FILE);
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		ResourceWrapper res = (ResourceWrapper) element;
		return "[" + res.getModelId() + "] " + res.getResource().getName();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

}
