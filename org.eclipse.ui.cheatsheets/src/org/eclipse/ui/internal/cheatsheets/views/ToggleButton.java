/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

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
