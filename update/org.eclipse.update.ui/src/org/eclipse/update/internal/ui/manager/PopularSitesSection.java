package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.parts.*;

public class PopularSitesSection extends UpdateSection {
	public PopularSitesSection(UpdateFormPage page) {
		super(page);
		setHeaderText("Popular Sites");
		setDescription("Use the hyperlinks listed below to go to the Eclipse update sites you visited the most often.");
	}
	
	public Composite createClient(Composite parent, FormWidgetFactory factory) {
		Composite client = factory.createComposite(parent);
		return client;
	}
}