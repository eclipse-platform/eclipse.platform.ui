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
package org.eclipse.update.internal.ui.pages;
import org.eclipse.jface.action.*;
import org.eclipse.update.internal.ui.views.*;
import org.eclipse.update.ui.forms.internal.IFormPage;

public interface IUpdateFormPage extends IFormPage {
	boolean contextMenuAboutToShow(IMenuManager manager);
	IAction getAction(String id);
	void openTo(Object object);
	void performGlobalAction(String id);
	void update();
	void dispose();
	void setFocus();
	MultiPageView getView();
}

