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

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.dialogs.DialogMessageArea;

/**
 * The TabbedDialogMessageArea is the emulated dialog message
 * area used to update tags.
 */
public class TabbedDialogMessageArea extends DialogMessageArea {
	
	CTabItem tab;
	/**
	 * Create a new instance of the reciever which forwards to tab.
	 * @param tab
	 */
	TabbedDialogMessageArea(CTabItem tab){
		this.tab = tab;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogMessageArea#showTitle(java.lang.String, org.eclipse.swt.graphics.Image)
	 */
	public void showTitle(String titleMessage, Image titleImage) {
		tab.setText(titleMessage + "       ");//$NON-NLS-1$
		tab.setImage(titleImage);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogMessageArea#restoreTitle()
	 */
	public void restoreTitle() {
		
	}

}
