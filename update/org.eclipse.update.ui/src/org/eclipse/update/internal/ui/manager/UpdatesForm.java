package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;

public class UpdatesForm extends UpdateForm {
	private FeatureList featureList;
	
	public UpdatesForm(UpdateFormPage page) {
		super(page);
		setScrollable(false);
	}
	
public void initialize(Object modelObject) {
	setTitle("Feature Update");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	super.initialize(modelObject);
	getControl().layout(true);
}

protected void createFormClient(Composite parent) {
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	layout.marginWidth = 10;
	//layout.makeColumnsEqualWidth = true;
	layout.horizontalSpacing = 15;
	parent.setLayout(layout);

	FormWidgetFactory factory = getFactory();
	featureList = new FeatureList();
	Control c = featureList.createControl(parent);
	
	GridData gd = new GridData(GridData.FILL_BOTH);
	c.setLayoutData(gd);
}

}

