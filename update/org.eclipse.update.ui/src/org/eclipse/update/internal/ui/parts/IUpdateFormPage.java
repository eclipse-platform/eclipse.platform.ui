package org.eclipse.update.internal.ui.parts;

import org.eclipse.jface.action.*;
import org.eclipse.update.ui.forms.IFormPage;

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

