package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.widgets.*;

public interface IFormWorkbook {

	void addFormSelectionListener(IFormSelectionListener listener);
	public void addPage(IFormPage page);
	public void createControl(Composite parent);
	Control getControl();
	public IFormPage getCurrentPage();
	boolean isFirstPageSelected();
	void removeFormSelectionListener(IFormSelectionListener listener);
	public void removePage(IFormPage page);
	public void selectPage(final IFormPage page);
	void setFirstPageSelected(boolean selected);
}
