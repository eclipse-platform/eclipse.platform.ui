/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;

public class ToggleImage extends ToggleableControl {
	protected Image theImage;
	protected Rectangle theImageSize;
	protected int toggleImageWidth;
	protected int toggleImageHeight;

	/**
	 * Constructor for MyImageToggle.
	 * @param parent
	 * @param style
	 */
	public ToggleImage(Composite parent, int style, Image newImage) {
		super(parent, style);
		theImage = newImage;

		calculateDimensions();
	}

	protected void calculateDimensions() {
		theImageSize = theImage.getBounds();
		toggleImageWidth = theImageSize.width + marginWidth*2;
		toggleImageHeight = theImageSize.height + marginHeight*2;
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(toggleImageWidth, toggleImageWidth);
	}

	public void setImage(Image myimage){
		theImage = myimage;		
		calculateDimensions();
	}	
	
	public Image getImage(){
		return theImage;	
	}
	
	/**
	 * @see org.eclipse.ui.internal.cheatsheets.views.MyToggle#paint(GC)
	 */
	protected void paint(GC gc) {
				
		if (hover && activeColor!=null)
			gc.setBackground(activeColor);
		else if (decorationColor!=null)
	   	   gc.setBackground(decorationColor);
	   	else
	   			gc.setBackground(getForeground());

		// Find point to center image		
		Point size = getSize();
		int x = (size.x - theImageSize.width)/2;
		int y = (size.y - theImageSize.height)/2;

		gc.drawImage(theImage, 0, 0, theImageSize.width, theImageSize.height, x, y, theImageSize.width, theImageSize.height);
		gc.setBackground(getBackground());
	}

}
