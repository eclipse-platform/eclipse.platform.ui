package org.eclipse.update.internal.ui.parts;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.action.*;


public abstract class UpdateFormPage implements IUpdateFormPage {
	private Form form;
	private Control control;
	private MultiPageView view;
	private String title;

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
	setFocus();
}

public boolean contextMenuAboutToShow(IMenuManager manager) {
	return true;
}

protected abstract Form createForm();

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

public Form getForm() {
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

public void init(Object model) {
}

public void update() {
	form.update();
}

}

