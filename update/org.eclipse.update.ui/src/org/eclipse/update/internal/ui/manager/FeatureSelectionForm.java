package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public abstract class FeatureSelectionForm extends UpdateForm {
	private ControlSection controlSection;
	private FeatureSelectionBlock featureSelectionBlock;
	private int mode;
	
	public FeatureSelectionForm(UpdateFormPage page, int mode) {
		super(page);
		this.mode = mode;
		setScrollable(false);
	}
	
public void initialize(Object modelObject) {
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	super.initialize(modelObject);
	getControl().layout(true);
}

public void createFormClient(Composite parent) {
	GridLayout layout = new GridLayout();
	layout.marginWidth = 10;
	layout.horizontalSpacing = 15;
	parent.setLayout(layout);
	
	controlSection = new ControlSection(getPage(), mode);
	Control c= controlSection.createControl(parent, getFactory());	
	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	c.setLayoutData(gd);
	
	featureSelectionBlock = new FeatureSelectionBlock(this, mode);
	
	c= featureSelectionBlock.createControl(parent);
	gd = new GridData(GridData.FILL_BOTH);
	c.setLayoutData(gd);
}
	

}

