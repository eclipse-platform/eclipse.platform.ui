package org.eclipse.update.ui.internal.manager;

import org.eclipse.update.ui.internal.parts.*;
import org.eclipse.update.ui.internal.*;

public class MainForm extends UpdateForm {
	public MainForm(UpdateFormPage page) {
		super(page);
		setVerticalFit(true);
	}
	
public void initialize(Object modelObject) {
	setTitle("Eclipse Update");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	super.initialize(modelObject);
	getControl().layout(true);
}
}

