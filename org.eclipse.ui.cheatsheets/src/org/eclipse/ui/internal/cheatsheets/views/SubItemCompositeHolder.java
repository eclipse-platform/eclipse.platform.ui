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
	private Label checkDoneLabel;
	private boolean skipped;
	private boolean completed;
	protected ImageHyperlink startButton;
	private String thisValue;
	private SubItem subItem;
	private Control skipButton;
	private Control completeButton;
	private Label subitemLabel;
	
	SubItemCompositeHolder(SubItem subItem) {
		super();
		this.subItem = subItem;
	}

	/**
	 * @return Label
	 */
	/*package*/ Label getCheckDone() {
		return checkDoneLabel;
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
	 * Hide or reveal all the action/complete/skip buttons
	 * @param isVisible
	 */
	public void setButtonsVisible(boolean isVisible) {
		if (startButton != null) {
			startButton.setVisible(isVisible);
		}
		if (skipButton != null) {
			skipButton.setVisible(isVisible);
		}
		if (completeButton != null) {
			completeButton.setVisible(isVisible);
		}
	}

	public void setSubitemLabel(Label label) {
		this.subitemLabel = label;
	}
	
	public Label getSubitemLabel() {
		return subitemLabel;
	}
	
	public void setStartButton(ImageHyperlink startButton) {
		this.startButton = startButton;
	}

	public ImageHyperlink getStartButton() {
		return startButton;
	}

	public void setSkipButton(Control skipButton) {
		this.skipButton = skipButton;
	}

	public Control getSkipButton() {
		return skipButton;
	}

	public void setCompleteButton(Control completeButton) {
		this.completeButton = completeButton;
	}

	public Control getCompleteButton() {
		return completeButton;
	}

	public void setCheckDoneLabel(Label checkDoneLabel) {
		this.checkDoneLabel = checkDoneLabel;		
	}
}
