/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation.
 * Licensed Material - Property of IBM.
 * All rights reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.navigator;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.IDescriptionProvider;

/**
 * Provides a label and icon for objects of type {@link PropertiesTreeData}.
 * @since 3.2
 */
public class PropertiesLabelProvider extends LabelProvider implements
		IDescriptionProvider {


	@Override
	public Image getImage(Object element) {
		if (element instanceof PropertiesTreeData)
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJS_INFO_TSK);
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof PropertiesTreeData) {
			PropertiesTreeData data = (PropertiesTreeData) element;
			return data.getName() + "= " + data.getValue(); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public String getDescription(Object anElement) {
		if (anElement instanceof PropertiesTreeData) {
			PropertiesTreeData data = (PropertiesTreeData) anElement;
			return "Property: " + data.getName(); //$NON-NLS-1$
		}
		return null;
	}

}
