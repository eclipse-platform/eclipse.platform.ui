package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;

public class FeatureSelectionBlock {
	private FeatureListSection featureListSection;
	private SelectedFeatureListSection selectedFeatureListSection;
	private DetailsSection detailsSection;
	private UpdateForm form;
	private int mode;
	
	public FeatureSelectionBlock (UpdateForm form, int mode) {
		this.form = form;
		this.mode = mode;
	}
	
	public Control createControl(Composite parent) {
		FormWidgetFactory factory = form.getFactory();
		Composite container = factory.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		featureListSection = new FeatureListSection(form.getPage(), mode);
		Control c = featureListSection.createControl(container, factory);
		GridData gd = new GridData(GridData.FILL_BOTH);
		c.setLayoutData(gd);
		
		selectedFeatureListSection = new 
			SelectedFeatureListSection(form.getPage(), mode);
		c = selectedFeatureListSection.createControl(container, factory);
		gd = new GridData(GridData.FILL_BOTH);
		c.setLayoutData(gd);
		
		detailsSection = new DetailsSection(form.getPage(), mode);
		c = detailsSection.createControl(container, factory);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		c.setLayoutData(gd);
		return container;
	}
}