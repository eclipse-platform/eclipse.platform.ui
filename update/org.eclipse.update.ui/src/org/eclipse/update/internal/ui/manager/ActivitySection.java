package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.ui.forms.FormWidgetFactory;
import org.eclipse.update.core.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;


public class ActivitySection extends UpdateSection {
	private Composite container;
	private FormWidgetFactory factory;
	private Control [] headers;
	public ActivitySection(UpdateFormPage page) {
		super(page);
		setAddSeparator(false);
		setHeaderText("Activities");
		setDescription("The following list shows activities that caused the creation of this configuration");
	}

	public Composite createClient(Composite parent, FormWidgetFactory factory) {
		GridLayout layout = new GridLayout();
		this.factory = factory;
		header.setForeground(factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR));
		layout.marginWidth = 0;
		layout.horizontalSpacing = 10;
		container = factory.createComposite(parent);
		container.setLayout(layout);
		layout.numColumns = 4;
		
		headers = new Control [5];
		
		headers[0] = createHeader(container, factory, "Date");
		headers[1] = createHeader(container, factory, "Target");
		headers[2] = createHeader(container, factory, "Action");
		headers[3] = createHeader(container, factory, "Status");
		Composite separator = factory.createCompositeSeparator(container);
		headers[4] = separator;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 1;
		gd.horizontalSpan = 4;
		separator.setLayoutData(gd);
		separator.setBackground(factory.getBorderColor());
		return container;
	}
	
	private boolean isHeader(Control c) {
		for (int i=0; i<headers.length; i++) {
			if (c.equals(headers[i])) return true;
		}
		return false;
	}
	
	public void configurationChanged(IInstallConfiguration config) {
		Control [] children = container.getChildren();
		for (int i=0; i<children.length; i++) {
			Control child = children[i];
			if (!isHeader(child))
				children[i].dispose();
		}
		
		IActivity [] activities = config.getActivities();
		for (int i=0; i<activities.length; i++) {
			IActivity activity = activities[i];
			factory.createLabel(container, activity.getDate().toString());
			factory.createLabel(container, activity.getLabel());
			factory.createLabel(container, getActionLabel(activity));
			factory.createLabel(container, getStatusLabel(activity));
		}
		container.layout();
		container.getParent().layout();
	}
	
	private Label createHeader(Composite parent, FormWidgetFactory factory, String text) {
		Label label = factory.createLabel(parent, text);
		label.setFont(JFaceResources.getBannerFont());
		return label;
	}
	
	private String getActionLabel(IActivity activity) {
		int action = activity.getAction();
		switch (action) {
			case IActivity.ACTION_CONFIGURE:
				return "Configure";
			case IActivity.ACTION_FEATURE_INSTALL:
				return "Feature Installed";
			case IActivity.ACTION_FEATURE_REMOVE:
				return "Feature Removed";
			case IActivity.ACTION_SITE_INSTALL:
				return "Site Installed";
			case IActivity.ACTION_SITE_REMOVE:
				return "Site Removed";
			case IActivity.ACTION_UNCONFIGURE:
				return "Unconfigure";
			default:
				return "Unknown";
		}
	}
	
	private String getStatusLabel(IActivity activity) {
		switch (activity.getStatus()) {
			case IActivity.STATUS_OK:
				return "Success";
			case IActivity.STATUS_NOK:
				return "Failure";
			case IActivity.STATUS_REVERT:
				return "Revert";
		}
		return "Unknown";
	}
}