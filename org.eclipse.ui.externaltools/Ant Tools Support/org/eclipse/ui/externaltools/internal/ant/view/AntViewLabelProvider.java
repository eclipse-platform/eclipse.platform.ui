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
			ProjectNode project= (ProjectNode) element;
			if (project.isErrorNode()) {
				return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_PROJECT_ERROR);
			} else {
				return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_PROJECT);
			}
		} else if (element instanceof TargetNode) {
			TargetNode target= (TargetNode) element;
			if (target.isErrorNode()) {
				return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_TARGET_ERROR);
			} else if (target.equals(target.getProject().getDefaultTarget())){
				return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_DEFAULT_TARGET);
			} else {
				return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_TARGET);
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof TargetNode) {
			TargetNode node= (TargetNode)element;
			StringBuffer name= new StringBuffer(node.getName());
			if (node.equals(node.getProject().getDefaultTarget())) {
				name.append(AntViewMessages.getString("TargetNode.default")); //$NON-NLS-1$
			} 
			return name.toString();
		} else {
			return element.toString();
		}
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
