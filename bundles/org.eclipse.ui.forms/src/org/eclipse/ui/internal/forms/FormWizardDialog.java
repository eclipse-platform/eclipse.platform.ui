/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms;

import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.FormColors;

/**
 * Modifies the standard wizard dialog to accept form wizards. Modification
 * consists of adjusting colors and layout so that scrollable forms can be
 * hosted.
 * 
 * @since 3.0
 */
public class FormWizardDialog extends WizardDialog {
	protected FormColors colors;

	/**
	 * Creats the wizard dialog. Colors are required to modify the dialog
	 * appearance to fit the forms.
	 * 
	 * @param shell
	 *            the parent shell
	 * @param wizard
	 *            the wizard to host
	 * @param colors
	 *            the colors to use
	 */
	public FormWizardDialog(
		Shell shell,
		FormWizard wizard,
		FormColors colors) {
		super(shell, wizard);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.colors = colors;
	}
	/**
	 * Extends the parent method by adjusting the colors and margins to fit the
	 * forms.
	 * 
	 * @param the
	 *            dialog area parent
	 * @return the dialog area
	 */
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		setChildColors(c);
		c.setBackground(colors.getBackground());
		c.setForeground(colors.getForeground());
		return c;
	}
	/**
	 * Extends the parent method by adjusting the colors of the button bar.
	 * 
	 * @param parent
	 *            the button bar parent
	 * @return the button bar
	 */
	protected Control createButtonBar(Composite parent) {
		Control bar = super.createButtonBar(parent);
		bar.setBackground(colors.getBackground());
		bar.setForeground(colors.getForeground());
		parent.setBackground(colors.getBackground());
		parent.setForeground(colors.getForeground());
		return bar;
	}

	private void setChildColors(Composite parent) {
		Control[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			child.setBackground(colors.getBackground());
			if (child instanceof ProgressMonitorPart)
				setChildColors((ProgressMonitorPart) child);
			if (child instanceof Composite) {
				Layout l = ((Composite) child).getLayout();
				if (l instanceof PageContainerFillLayout) {
					PageContainerFillLayout pl = (PageContainerFillLayout) l;
					pl.marginWidth = 0;
					pl.marginHeight = 0;
				}
			}
		}
	}
}
