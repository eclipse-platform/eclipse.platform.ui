/*
 * Created on Dec 5, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms;

import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class FormWizardDialog extends WizardDialog {
	FormColors colors;

	public FormWizardDialog(
		Shell shell,
		FormWizard wizard,
		FormColors colors) {
		super(shell, wizard);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.colors = colors;
	}
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		setChildColors(c);
		c.setBackground(colors.getBackground());
		c.setForeground(colors.getForeground());
		return c;
	}
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
