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

public class ToggleButton extends Button {

	protected Image fCollapsedImage;
	protected Image fExpandedImage;

	/**
	 * Creates a new ToggleButton.
	 * 
	 * @param parent
	 * @param style
	 * @param collapsedImage This is image to display when in the collapsed state, in terms
	 *                        of Windows Explorer this would be the + image.
	 * @param expandedImage This is image to display when in the expanded state, in terms
	 *                      of Windows Explorer this would be the - image.
	 */
	/*package*/ ToggleButton(Composite parent, int style, Image collapsedImage, Image expandedImage) {
		super(parent, style, collapsedImage);
		
		fCollapsedImage = collapsedImage;
		fExpandedImage = expandedImage;
	}

	protected void paint(GC gc) {
		setToggle();
		super.paint(gc);
	}


	private void setToggle() {
		if( isCollapsed() )
			setImage(fExpandedImage);
		else
			setImage(fCollapsedImage);
	}

	/**
	 * Returns the ToggledImage.
	 * @return Image
	 */
	public Image getToggledImage() {
		return fExpandedImage;
	}

	/**
	 * Returns the UnToggledImage.
	 * @return Image
	 */
	public Image getUnToggledImage() {
		return fCollapsedImage;
	}

	/**
	 * Sets the ExpandedImage.
	 * @param expandedImage The expandedImage to set
	 */
	public void setExpandedImage(Image expandedImage) {
		this.fExpandedImage = expandedImage;
	}

	/**
	 * Sets the CollapsedImage.
	 * @param collapsedImage The collapsedImage to set
	 */
	public void setCollapsedImage(Image collapsedImage) {
		this.fCollapsedImage = collapsedImage;
	}

	/**
	 * Returns the Collapsed state.
	 * @return boolean
	 */
	public boolean isCollapsed() {
		// default   + = unToggledImage  -> Selection is false
		//           - = toggleImage     -> Selection is true
		// thus isCollapsed is !selection
		return !getSelection();
	}

	/**
	 * Sets the Collapsed state.
	 * @param fCollapsed The fCollapsed to set
	 */
	/*package*/ void setCollapsed(boolean fCollapsed) {
		// default   + = unToggledImage  -> Selection is false
		//           - = toggleImage     -> Selection is true
		// thus selection is !fCollapsed
		setSelection(!fCollapsed);
		setToggle();
		redraw();
	}

}
