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
import org.eclipse.ui.externaltools.internal.ant.view.elements.DependencyNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ExecutionPathNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

/**
 * A label provider that provides labels for elements displayed in the
 * <code>AntView</code>
 */
public class AntViewLabelProvider implements ILabelProvider {

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof ProjectNode) {
			return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_PROJECT);
		} else if (element instanceof TargetNode) {
			return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_TARGET);
		} else if (element instanceof DependencyNode || element instanceof ExecutionPathNode) {
			return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_TARGET_ELEMENTS);
		} else if (element instanceof String) {
			return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_TARGET_ELEMENT);
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return element.toString();
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}
