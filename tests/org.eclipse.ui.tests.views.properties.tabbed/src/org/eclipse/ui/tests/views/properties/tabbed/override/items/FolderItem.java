/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.override.items;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.views.properties.tabbed.model.Folder;

/**
 * An item for when the Folder element is the selected element in the override
 * tests view.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class FolderItem extends InformationItem {

	@Override
	public Class getElement() {
		return Folder.class;
	}

	@Override
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_FOLDER);
	}

	@Override
	public String getText() {
		return "Folder"; //$NON-NLS-1$
	}
}
