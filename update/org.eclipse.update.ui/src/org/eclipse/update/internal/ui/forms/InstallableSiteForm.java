package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.ISite;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.IConfiguredSiteAdapter;
import org.eclipse.update.internal.ui.pages.UpdateFormPage;
import org.eclipse.update.internal.ui.views.SiteStateAction;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.engine.FormEngine;

public class InstallableSiteForm extends UpdateWebForm {
	private static final String KEY_TITLE = "InstallableSitePage.title";
	private static final String KEY_DESC = "InstallableSitePage.desc";
	private static final String KEY_NEW_LOC = "InstallableSitePage.newLocation";

	private IConfiguredSite currentSite;
	private Label urlLabel;
	private Label stateLabel;
	private Button stateButton;
	private SiteStateAction siteStateAction;

	public InstallableSiteForm(UpdateFormPage page) {
		super(page);
	}

	public void dispose() {
		super.dispose();
	}

	public void initialize(Object modelObject) {
		setHeadingText(UpdateUI.getResourceString(KEY_TITLE));
		super.initialize(modelObject);
		//((Composite)getControl()).layout(true);
	}

	protected void createContents(Composite parent) {
		HTMLTableLayout layout = new HTMLTableLayout();
		parent.setLayout(layout);
		layout.leftMargin = layout.rightMargin = 10;
		layout.topMargin = 10;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 20;
		layout.numColumns = 1;

		FormWidgetFactory factory = getFactory();
		urlLabel = factory.createHeadingLabel(parent, null);
		TableData td = new TableData();
		urlLabel.setLayoutData(td);
		
		FormEngine desc = factory.createFormEngine(parent);
		desc.load(UpdateUI.getResourceString(KEY_DESC), true, true);
		setFocusControl(desc);
		td = new TableData();
		td.align = TableData.FILL;
		td.grabHorizontal = true;
		desc.setLayoutData(td);
		
		stateLabel = factory.createLabel(parent, "");
		
		stateButton = factory.createButton(parent, "", SWT.PUSH);
		stateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				siteStateAction.run();
			}
		});
		siteStateAction = new SiteStateAction();

		WorkbenchHelp.setHelp(
			parent,
			"org.eclipse.update.ui.InstallableSiteForm");
	}

	public void expandTo(Object obj) {
		if (obj instanceof IConfiguredSiteAdapter) {
			inputChanged(((IConfiguredSiteAdapter) obj).getConfigurationSite());
		}
	}

	private String getStateButtonLabel(boolean enabled) {
		return UpdateUI.getResourceString(
			enabled
				? "InstallableSiteForm.stateButton.disable"
				: "InstallableSiteForm.stateButton.enable");
	}
	
	private String getStateLabel(boolean enabled) {
		return UpdateUI.getResourceString(
			enabled
				? "InstallableSiteForm.stateLabel.enabled"
				: "InstallableSiteForm.stateLabel.disabled");
	}

	private void inputChanged(IConfiguredSite csite) {
		ISite site = csite.getSite();
		urlLabel.setText(site.getURL().toString());

		stateLabel.setText(getStateLabel(csite.isEnabled()));
		stateButton.setText(getStateButtonLabel(csite.isEnabled()));
		siteStateAction.setSite(csite);
		urlLabel.getParent().layout();
		((Composite) getControl()).layout();
		getControl().redraw();
		currentSite = csite;
	}
}