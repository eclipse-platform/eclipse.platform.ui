/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.preferences;

 
import org.eclipse.debug.internal.ui.preferences.DebugActionGroupsManager.DebugActionGroup;
import org.eclipse.debug.internal.ui.preferences.DebugActionGroupsManager.DebugActionGroupAction;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class DebugActionGroupsLabelProvider extends LabelProvider {

	private String UNKNOWN = DebugPreferencesMessages.getString("DebugActionGroupsLabelProvider.<Unknown>_1"); //$NON-NLS-1$

	public DebugActionGroupsLabelProvider() {
		super();
	}
	
	/**
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		String label = UNKNOWN;
		if (element instanceof DebugActionGroup) {
			label = ((DebugActionGroup) element).getName();
		} else if (element instanceof DebugActionGroupAction) {
			label = ((DebugActionGroupAction) element).getName();
		} else if (element instanceof String) {
			label= (String)element;
		}
		return label;
	}
	
	/**
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		Image image= null;
		if (element instanceof DebugActionGroupAction) {
			image = ((DebugActionGroupAction) element).getImage();
		}
		return image;
	}
}
