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
package org.eclipse.ui.forms.parts;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.*;

/**
 * A variation of the expandable composite that adds optional
 * description below the title.
 */

public class Section extends ExpandableComposite {
	/**
	 * Description style. If used, description will be rendered below the
	 * title.
	 */
	public static final int DESCRIPTION = 1 << 7;
	private Label descriptionLabel;
	private Control separator;
	
	public Section(Composite parent, int style) {
		super(parent, SWT.NULL, style);
		if ((style & DESCRIPTION)!=0) {
			descriptionLabel = new Label(this, SWT.WRAP);
		}
	}

	protected void reflow() {
		setRedraw(false);
		getParent().setRedraw(false);
		layout(true);
		getParent().layout(true);
		setRedraw(true);
		getParent().setRedraw(true);
	}
	
	public void setDescription(String description) {
		if (descriptionLabel!=null)
			descriptionLabel.setText(description);
	}

	public String getDescription() {
		if (descriptionLabel!=null)
			return descriptionLabel.getText();
		return null;
	}
	/**
	 * Sets the separator control of this section. The separator
	 * must not be <samp>null</samp> and must be a direct child of this
	 * container. If defined, separator will be placed below
	 * the title text and will remain visible regardless of
	 * the expansion state.
	 * 
	 * @param separator
	 *            the separator that will be placed below the title text.
	 */
	public void setSeparatorControl(Control separator) {
		Assert.isTrue(separator != null && separator.getParent().equals(this));
		this.separator = separator;
	}
	
	/**
	 * Returns the control that is used as a separator betweeen
	 * the title and the client, or <samp>null</samp> if not set.
	 * @return separator control or <samp>null</samp> if not set.
	 */
	
	public Control getSeparatorControl() {
		return separator;
	}
	
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (descriptionLabel!=null)
			descriptionLabel.setBackground(bg);
	}

	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (descriptionLabel!=null)
			descriptionLabel.setForeground(fg);
	}
	protected Control getDescriptionControl() {
		return descriptionLabel;
	}
}