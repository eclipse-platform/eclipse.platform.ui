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
package org.eclipse.ui.forms.widgets;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.*;
/**
 * A variation of the expandable composite that adds optional description below
 * the title.
 * 
 * TODO (dejan) - spell out subclass contract
 * @since 3.0
 */
public class Section extends ExpandableComposite {
	/**
	 * Description style. If used, description will be rendered below the
	 * title.
	 */
	public static final int DESCRIPTION = 1 << 7;
	private Label descriptionLabel;
	private Control separator;
	/**
	 * Creates a new section instance in the provided parent.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the style to use
	 */
	public Section(Composite parent, int style) {
		super(parent, SWT.NULL, style);
		if ((style & DESCRIPTION) != 0) {
			descriptionLabel = new Label(this, SWT.WRAP);
		}
	}
	protected void internalSetExpanded(boolean expanded) {
		super.internalSetExpanded(expanded);
		reflow();
	}
	protected void reflow() {
		Composite c = this;
		
		while (c!=null) {
			c.setRedraw(false);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				break;
			}
		}
		c=this;
		while (c!=null) {
			c.layout(true);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				((ScrolledForm)c).reflow(true);
				break;
			}
		}
		c=this;
		while (c!=null) {
			c.setRedraw(true);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				break;
			}
		}
	}
	/**
	 * Sets the description text. Has no effect of DESCRIPTION style was not
	 * used to create the control.
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		if (descriptionLabel != null)
			descriptionLabel.setText(description);
	}
	/**
	 * Returns the current description text.
	 * 
	 * @return description text or <code>null</code> if DESCRIPTION style was
	 *         not used to create the control.
	 */
	public String getDescription() {
		if (descriptionLabel != null)
			return descriptionLabel.getText();
		return null;
	}
	/**
	 * Sets the separator control of this section. The separator must not be
	 * <samp>null </samp> and must be a direct child of this container. If
	 * defined, separator will be placed below the title text and will remain
	 * visible regardless of the expansion state.
	 * 
	 * @param separator
	 *            the separator that will be placed below the title text.
	 */
	public void setSeparatorControl(Control separator) {
		Assert.isTrue(separator != null && separator.getParent().equals(this));
		this.separator = separator;
	}
	/**
	 * Returns the control that is used as a separator betweeen the title and
	 * the client, or <samp>null </samp> if not set.
	 * 
	 * @return separator control or <samp>null </samp> if not set.
	 */
	public Control getSeparatorControl() {
		return separator;
	}
	/**
	 * Sets the background of the section.
	 * 
	 * @param bg
	 *            the new background
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (descriptionLabel != null)
			descriptionLabel.setBackground(bg);
	}
	/**
	 * Sets the foreground of the section.
	 * 
	 * @param fg
	 *            the new foreground.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (descriptionLabel != null)
			descriptionLabel.setForeground(fg);
	}
	/**
	 * Returns the control used to render the description.
	 * 
	 * @return description control or <code>null</code> if DESCRIPTION style
	 *         was not used to create the control.
	 */
	protected Control getDescriptionControl() {
		return descriptionLabel;
	}
}