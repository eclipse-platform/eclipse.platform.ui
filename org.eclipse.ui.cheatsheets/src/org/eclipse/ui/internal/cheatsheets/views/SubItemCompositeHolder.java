/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.internal.cheatsheets.data.SubItem;

public class SubItemCompositeHolder {
	private Label iconLabel;
	private boolean skipped;
	private boolean completed;
	protected ImageHyperlink startButton;
	private String thisValue;
	private SubItem subItem;
	private Control[] buttons;
	private Label subitemLabel;
	
	/**
	 * @param buttonArray 
	 * 
	 */
	/*package*/ SubItemCompositeHolder(Label iconLab, ImageHyperlink startb, String thisValue, SubItem subItem, 
			        Label subitemLabel, Control[] buttonArray) {
		super();
		iconLabel = iconLab;
		startButton = startb;
		this.thisValue = thisValue;
		this.subItem = subItem;
		this.buttons = buttonArray;
		this.subitemLabel = subitemLabel;
	}

	/**
	 * @return Label
	 */
	/*package*/ Label getIconLabel() {
		return iconLabel;
	}

	/**
	 * @return
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * @return
	 */
	public boolean isSkipped() {
		return skipped;
	}

	/**
	 * @param b
	 */
	/*package*/ void setCompleted(boolean b) {
		completed = b;
	}

	/**
	 * @param b
	 */
	/*package*/ void setSkipped(boolean b) {
		skipped = b;
	}

	/**
	 * @return
	 */
	/*package*/ ImageHyperlink getStartButton() {
		return startButton;
	}

	/**
	 * @return Returns the thisValue.
	 */
	public String getThisValue() {
		return thisValue;
	}

	/**
	 * @param thisValue The thisValue to set.
	 */
	public void setThisValue(String thisValue) {
		this.thisValue = thisValue;
	}

	/**
	 * @return Returns the subItem.
	 */
	public SubItem getSubItem() {
		return subItem;
	}

	/**
	 * @param subItem The subItem to set.
	 */
	public void setSubItem(SubItem subItem) {
		this.subItem = subItem;
	}
	
	/**
	 * Hide or reveal all the action/complete/skip buttons
	 * @param isVisible
	 */
	public void setButtonsVisible(boolean isVisible) {
		for (int b = 0; b < buttons.length; b++) {
			buttons[b].setVisible(isVisible);
		}
	}

	public Label getSubitemLabel() {
		return subitemLabel;
	}
}
