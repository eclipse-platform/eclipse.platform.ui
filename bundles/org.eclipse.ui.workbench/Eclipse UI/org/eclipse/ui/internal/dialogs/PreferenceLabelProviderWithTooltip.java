/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.decorators.ContributingPluginDecorator;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExpressionNode;

/**
 * @since 3.5
 *
 */
public class PreferenceLabelProviderWithTooltip extends ColumnLabelProvider {

	ILabelDecorator decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(
			ContributingPluginDecorator.ID);

	protected void initialize(ColumnViewer viewer, ViewerColumn column) {
		super.initialize(viewer, column);
		if (decorator != null) {
			ColumnViewerToolTipSupport.enableFor(viewer);
		}
	}
	/**
	 * copied from org.eclipse.jface.preference.PreferenceLabelProvider
	 * 
	 * @param element
	 *            must be an instance of <code>IPreferenceNode</code>.
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return ((IPreferenceNode) element).getLabelText();
	}

	/**
	 * copied from org.eclipse.jface.preference.PreferenceLabelProvider
	 * 
	 * @param element
	 *            must be an instance of <code>IPreferenceNode</code>.
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return ((IPreferenceNode) element).getLabelImage();
	}

	public String getToolTipText(Object element) {
		if (decorator == null) {
			return null;
		}
		if (element instanceof WorkbenchPreferenceExpressionNode) {
			WorkbenchPreferenceExpressionNode node = (WorkbenchPreferenceExpressionNode) element;
			return decorator.decorateText(getText(element), node.getPluginId());
		}
		return null;
	}

}
