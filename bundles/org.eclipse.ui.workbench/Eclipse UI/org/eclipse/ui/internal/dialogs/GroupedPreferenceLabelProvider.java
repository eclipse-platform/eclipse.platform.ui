/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;

import org.eclipse.ui.internal.Workbench;

/**
 * The GroupedPreferenceLabelProvider is the label provider
 * for grouped preferences.
 */
public class GroupedPreferenceLabelProvider extends PreferenceLabelProvider implements IColorProvider{
 
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceLabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if(element instanceof IPreferenceNode)
			return super.getImage(element);
		return ((WorkbenchPreferenceGroup) element).getImage();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceLabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if(element instanceof IPreferenceNode)
			return super.getText(element);
		return ((WorkbenchPreferenceGroup) element).getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		boolean highlight = false;
		if(element instanceof WorkbenchPreferenceNode)
			highlight = ((WorkbenchPreferenceNode)element).isHighlighted();
		else
			highlight = ((WorkbenchPreferenceGroup)element).isHighlighted();
		
		if(highlight)
			return Workbench.getInstance().getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		return null;
	}
	
}
