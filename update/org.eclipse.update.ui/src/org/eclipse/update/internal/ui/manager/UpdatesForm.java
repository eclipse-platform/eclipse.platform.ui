package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;

public class UpdatesForm extends UpdateForm {
	public UpdatesForm(UpdateFormPage page) {
		super(page);
		setVerticalFit(true);
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
	
	Composite container = factory.createComposite(parent);
	layout = new GridLayout();
	container.setLayout(layout);
	GridData gd = new GridData(GridData.FILL_BOTH);
	container.setLayoutData(gd);
	

	createFeature(container, 
			"Eclipse Platform",
			"12415Kb",
			"The Platform project provides the core frameworks and services upon which all plug-in extensions are created. It also provides the runtime in which plug-ins are loaded, integrated, and executed. The Platform project's purpose is to enable other tool developers to build and deliver really nice integrated tools.");
	createFeature(container, 
			"Java Development Tooling (JDT)",
			"6012Kb",
			"JDT provides tool plug-ins that implement a Java IDE that supports the development of Java applications including Eclipse plug-ins.The JDT project allows Eclipse to be a development environment for itself.");
	createFeature(container, 
			"Plug-in Development Environment (PDE)",
			"4514Kb",
			"The PDE project extends the Platform and the JDT to provide views and editors that make it easier to build plug-ins for Eclipse. The PDE helps you figure out what extension points are available, how to plug into them, and helps you put together your code in plug-in format. The PDE makes integrating plug-ins easy and fun.");
}

private Control createFeature(Composite parent,
		String name, String size, String description) {
	FeatureSection section = new FeatureSection();
	section.setName(name);
	section.setSize(size);
	section.setDescription(description);
	section.setInfoLink("More info");
	Control c = section.createControl(parent, getFactory());
	GridData gd = new GridData(GridData.FILL_HORIZONTAL | 
				GridData.VERTICAL_ALIGN_BEGINNING);
	c.setLayoutData(gd);
	return c;
}

}

