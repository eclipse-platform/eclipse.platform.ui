package org.eclipse.update.internal.ui.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.update.ui.forms.internal.IFormPage;

public interface IUpdateFormPage extends IFormPage {
	boolean contextMenuAboutToShow(IMenuManager manager);
	IAction getAction(String id);
	void openTo(Object object);
	void performGlobalAction(String id);
	void init(Object model);
	void update();
	void dispose();
	MultiPageView getView();
}

