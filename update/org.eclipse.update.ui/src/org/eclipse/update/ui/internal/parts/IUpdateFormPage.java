package org.eclipse.update.ui.internal.parts;

import org.eclipse.jface.action.*;
import org.eclipse.update.ui.forms.IFormPage;

public interface IUpdateFormPage extends IFormPage {
	boolean contextMenuAboutToShow(IMenuManager manager);
	IAction getAction(String id);
	void openTo(Object object);
	void performGlobalAction(String id);
	void init(Object model);
	void update();
}

