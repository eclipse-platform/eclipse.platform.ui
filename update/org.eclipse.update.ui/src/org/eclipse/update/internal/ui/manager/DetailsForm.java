package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;

public class DetailsForm extends UpdateForm {
private DetailsSection detailsSection;

	public DetailsForm(UpdateFormPage page) {
		super(page);
		setVerticalFit(true);
		//setHeadingVisible(false);
	}
	
public void initialize(Object modelObject) {
	setHeadingText("Feature Details");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	super.initialize(modelObject);
	((Composite)getControl()).layout(true);
}

public void createFormClient(Composite parent) {
	GridLayout layout = new GridLayout();
	//layout.numColumns = 2;
	layout.marginWidth = 10;
	//layout.makeColumnsEqualWidth = true;
	layout.horizontalSpacing = 15;
	parent.setLayout(layout);	
	
	GridData gd;
	Control child;
	
	FormWidgetFactory factory = getFactory();
	detailsSection = new DetailsSection(this.getPage());
	Control c = detailsSection.createControl(parent, factory);
	gd = new GridData(GridData.FILL_BOTH);
	c.setLayoutData(gd);
	registerSection(detailsSection);
}

public void expandTo(Object obj) {
	detailsSection.expandTo(obj);
}

private void goToPage(String pageId) {
	getPage().getView().showPage(pageId);
}
}