/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.texteditor;


import org.eclipse.swt.graphics.Image;


/**
 * Interface of a status field of a text editor. The field that shows up in the 
 * workbench's status line if the contributing editor is active.
 * @since 2.0
 */
public interface IStatusField {
	
	/**
	 * Sets the text of this status field.
	 * 
	 * @param text the text shown in the status field
	 */
	void setText(String text);
	
	/**
	 * Sets the image of this status field.
	 * 
	 * @param image the image shown in the status field
	 */
	void setImage(Image image);
}

