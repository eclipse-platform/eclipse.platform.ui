package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.SWT;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.update.ui.forms.*;

public class FeatureList extends ScrollableForm {
	private Composite container;
	public FeatureList() {
		setVerticalFit(true);
	}
	
	public void createFormClient(Composite parent) {
		FormWidgetFactory factory = getFactory();
		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		
		createFeature(parent,
			"Eclipse Platform",
			"Object Technology International, Inc.",
			"12415Kb",
			"The Platform project provides the core frameworks and services upon which all plug-in extensions are created. It also provides the runtime in which plug-ins are loaded, integrated, and executed. The Platform project's purpose is to enable other tool developers to build and deliver really nice integrated tools.");
		createFeature(parent, 
			"Java Development Tooling (JDT)",
			"Object Technology International, Inc.",
			"6012Kb",
			"JDT provides tool plug-ins that implement a Java IDE that supports the development of Java applications including Eclipse plug-ins.The JDT project allows Eclipse to be a development environment for itself.");
		createFeature(parent, 
			"Plug-in Development Environment (PDE)",
			"International Business Machines Corp.",
			"4514Kb",
			"The PDE project extends the Platform and the JDT to provide views and editors that make it easier to build plug-ins for Eclipse. The PDE helps you figure out what extension points are available, how to plug into them, and helps you put together your code in plug-in format. The PDE makes integrating plug-ins easy and fun.");
	}

private Control createFeature(Composite parent,
		String name, String provider, String size, String description) {
	FeatureSection section = new FeatureSection();
	section.setName(name);
	section.setProviderName(provider);
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

