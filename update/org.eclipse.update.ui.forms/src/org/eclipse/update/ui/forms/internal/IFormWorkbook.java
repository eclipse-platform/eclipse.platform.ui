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
package org.eclipse.update.ui.forms.internal;

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
	public void selectPage(final IFormPage page, boolean setFocus);
	void setFirstPageSelected(boolean selected);
}
