package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;

public class HistoryForm extends UpdateForm {
	
	public HistoryForm(UpdateFormPage page) {
		super(page);
		setVerticalFit(true);
	}
	
public void initialize(Object modelObject) {
	setTitle("Installation History");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	super.initialize(modelObject);
	getControl().layout(true);
}

public void createFormClient(Composite parent) {
	GridLayout layout = new GridLayout();
	//layout.numColumns = 2;
	layout.marginWidth = 10;
	//layout.makeColumnsEqualWidth = true;
	layout.horizontalSpacing = 15;
	parent.setLayout(layout);	
	
	FormWidgetFactory factory = getFactory();
}

}