package org.eclipse.update.internal.ui.parts;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.action.*;


public abstract class UpdateFormPage implements IUpdateFormPage {
	private Form form;
	private Control control;
	private MultiPageEditor editor;
	private String title;

public UpdateFormPage(MultiPageEditor editor, String title) {
	this.editor = editor;
	form = createForm();
	//form.setHeadingImage(PDEPluginImages.get(PDEPluginImages.IMG_FORM_BANNER));
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
	control.setMenu(editor.getContextMenu());
	form.initialize(null);
}

public void dispose() {
	form.dispose();
}

public Control getControl() {
	return control;
}

public MultiPageEditor getEditor() {
	return editor;
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
	return getEditor().getCurrentPage()==this;
}

public void openTo(Object object) {
	getForm().expandTo(object);
}

public IAction getAction(String id) {
	return editor.getAction(id);
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

