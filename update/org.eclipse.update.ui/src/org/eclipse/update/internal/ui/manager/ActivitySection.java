package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;


public class ActivitySection extends UpdateSection {
// NL resource keys
	private static final String KEY_TITLE = "InstallConfigurationPage.ActivitySection.title";
	private static final String KEY_DESC = "InstallConfigurationPage.ActivitySection.desc";
	private static final String KEY_DATE = "InstallConfigurationPage.ActivitySection.headers.date";
	private static final String KEY_TARGET = "InstallConfigurationPage.ActivitySection.headers.target";
	private static final String KEY_ACTION = "InstallConfigurationPage.ActivitySection.headers.action";
	private static final String KEY_STATUS = "InstallConfigurationPage.ActivitySection.headers.status";
	private static final String KEY_CONFIGURE = "InstallConfigurationPage.ActivitySection.action.configure";
	private static final String KEY_FEATURE_INSTALL = "InstallConfigurationPage.ActivitySection.action.featureInstall";
	private static final String KEY_FEATURE_REMOVE = "InstallConfigurationPage.ActivitySection.action.featureRemove";
	private static final String KEY_SITE_INSTALL = "InstallConfigurationPage.ActivitySection.action.siteInstall";
	private static final String KEY_SITE_REMOVE = "InstallConfigurationPage.ActivitySection.action.siteRemove";	
	private static final String KEY_UNCONFIGURE = "InstallConfigurationPage.ActivitySection.action.unconfigure";
	private static final String KEY_UNKNOWN = "InstallConfigurationPage.ActivitySection.action.unknown";
	private static final String KEY_REVERT = "InstallConfigurationPage.ActivitySection.action.revert";
	private static final String KEY_RECONCILIATION = "InstallConfigurationPage.ActivitySection.action.reconcile";	
	private static final String KEY_OK = "InstallConfigurationPage.ActivitySection.status.ok";
	private static final String KEY_NOK = "InstallConfigurationPage.ActivitySection.status.nok";

	private Composite container;
	private FormWidgetFactory factory;
	private Control [] headers;
	public ActivitySection(UpdateFormPage page) {
		super(page);
		setAddSeparator(false);
		setHeaderText(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESC));
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
		
		headers[0] = createHeader(container, factory, UpdateUIPlugin.getResourceString(KEY_DATE));
		headers[1] = createHeader(container, factory, UpdateUIPlugin.getResourceString(KEY_TARGET));
		headers[2] = createHeader(container, factory, UpdateUIPlugin.getResourceString(KEY_ACTION));
		headers[3] = createHeader(container, factory, UpdateUIPlugin.getResourceString(KEY_STATUS));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		headers[3].setLayoutData(gd);
		Composite separator = factory.createCompositeSeparator(container);
		headers[4] = separator;
		gd = new GridData(GridData.FILL_HORIZONTAL);
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
			createLimitedLabel(container, activity.getLabel(), 300, factory);
			factory.createLabel(container, getActionLabel(activity));
			factory.createLabel(container, getStatusLabel(activity));
		}
		Composite separator = factory.createCompositeSeparator(container);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 1;
		gd.horizontalSpan = 4;
		separator.setLayoutData(gd);
		separator.setBackground(factory.getBorderColor());
		factory.createLabel(container, null);
		container.layout(true);
		container.getParent().layout(true);
	}
	
	private void createLimitedLabel(Composite container, String text, int limit, FormWidgetFactory factory) {
		CLabel clabel = new CLabel(container, SWT.NULL);
		clabel.setBackground(factory.getBackgroundColor());
		clabel.setForeground(factory.getForegroundColor());
		clabel.setText(text);
		GridData gd = new GridData();
		gd.widthHint = limit;
		clabel.setLayoutData(gd);
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
				return UpdateUIPlugin.getResourceString(KEY_CONFIGURE);
			case IActivity.ACTION_FEATURE_INSTALL:
				return UpdateUIPlugin.getResourceString(KEY_FEATURE_INSTALL);
			case IActivity.ACTION_FEATURE_REMOVE:
				return UpdateUIPlugin.getResourceString(KEY_FEATURE_REMOVE);
			case IActivity.ACTION_SITE_INSTALL:
				return UpdateUIPlugin.getResourceString(KEY_SITE_INSTALL);
			case IActivity.ACTION_SITE_REMOVE:
				return UpdateUIPlugin.getResourceString(KEY_SITE_REMOVE);
			case IActivity.ACTION_UNCONFIGURE:
				return UpdateUIPlugin.getResourceString(KEY_UNCONFIGURE);
			case IActivity.ACTION_REVERT:
				return UpdateUIPlugin.getResourceString(KEY_REVERT);
			case IActivity.ACTION_RECONCILIATION:
				return UpdateUIPlugin.getResourceString(KEY_RECONCILIATION);				
			default:
				return UpdateUIPlugin.getResourceString(KEY_UNKNOWN);		
		}
	}
	
	private String getStatusLabel(IActivity activity) {
		switch (activity.getStatus()) {
			case IActivity.STATUS_OK:
				return UpdateUIPlugin.getResourceString(KEY_OK);
			case IActivity.STATUS_NOK:
				return UpdateUIPlugin.getResourceString(KEY_NOK);
		}
		return UpdateUIPlugin.getResourceString(KEY_UNKNOWN);
	}
}