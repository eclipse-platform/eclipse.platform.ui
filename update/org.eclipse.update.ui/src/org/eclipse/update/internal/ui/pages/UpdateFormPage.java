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
import org.eclipse.update.internal.ui.views.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.action.*;


public abstract class UpdateFormPage implements IUpdateFormPage {
	private IForm form;
	private MultiPageView view;
	private String title;
	private Control control;

public UpdateFormPage(MultiPageView view, String title) {
	this.view = view;
	form = createForm();
	this.title = title;
}

public boolean becomesInvisible(IFormPage newPage) {
	return true;
}

public void becomesVisible(IFormPage oldPage) {
	update();
	//setFocus();
}

public boolean contextMenuAboutToShow(IMenuManager manager) {
	return true;
}

protected abstract IForm createForm();

public void createControl(Composite parent) {
	control = form.createControl(parent);
	control.setMenu(view.getContextMenu());
	form.initialize(null);
}

public void dispose() {
	form.dispose();
}

public Control getControl() {
	return control;
}

public MultiPageView getView() {
	return view;
}

public IForm getForm() {
	return form;
}

public String getLabel() {
	return title;
}

public String getTitle() {
	return title;
}

public boolean isSource() {
	return false;
}

public boolean isVisible() {
	return getView().getCurrentPage()==this;
}

public void openTo(Object object) {
	getForm().expandTo(object);
}

public IAction getAction(String id) {
	return view.getAction(id);
}

public void performGlobalAction(String id) {
	getForm().doGlobalAction(id);
}

public void setFocus() {
	getForm().setFocus();
}

public String toString() {
	return title;
}

public void update() {
	form.update();
}

}

