package org.eclipse.ui.externaltools.internal.ant.view;
/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

public class AntTargetsLabelProvider implements ILabelProvider {

	public Image getImage(Object element) {
		if (element instanceof TargetNode) {
			return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_TARGET);
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof TargetNode) {
			StringBuffer buffer= new StringBuffer(element.toString());
			buffer.append(" [").append(((TargetNode) element).getParent().getName()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
			return buffer.toString();
		}
		return element.toString();
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}
