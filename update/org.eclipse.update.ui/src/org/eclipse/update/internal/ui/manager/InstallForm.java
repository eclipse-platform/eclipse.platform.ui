package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;

public class InstallForm extends UpdateForm {
	public InstallForm(UpdateFormPage page) {
		super(page);
		setVerticalFit(true);
	}
	
public void initialize(Object modelObject) {
	setTitle("New Feature Install");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	super.initialize(modelObject);
	getControl().layout(true);
}
}

