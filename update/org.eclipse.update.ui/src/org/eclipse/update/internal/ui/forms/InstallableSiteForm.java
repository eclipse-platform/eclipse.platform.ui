/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.forms;

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

public class InstallableSiteForm extends PropertyWebForm {
	private static final String KEY_TITLE = "InstallableSitePage.title";
	private static final String KEY_DESC = "InstallableSitePage.desc";
	private static final String KEY_NEW_LOC = "InstallableSitePage.newLocation";

	private IConfiguredSite currentSite;
	private Label urlLabel;
	private Label typeLabel;
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
		setHeadingText(UpdateUI.getString(KEY_TITLE));
		super.initialize(modelObject);
		//((Composite)getControl()).layout(true);
	}

	protected void createContents(Composite parent) {
		HTMLTableLayout layout = new HTMLTableLayout();
		parent.setLayout(layout);
		layout.leftMargin = 10;
		layout.rightMargin = 0;
		layout.topMargin = 10;
		layout.horizontalSpacing = 0;
		//layout.verticalSpacing = 20;
		layout.numColumns = 1;

		FormWidgetFactory factory = getFactory();
		urlLabel =
			createProperty(
				parent,
				UpdateUI.getString(
					"InstallableSiteForm.url"));
		typeLabel =
			createProperty(
				parent,
					UpdateUI.getString(
						"InstallableSiteForm.type"));
		stateLabel =
			createProperty(
				parent,
					UpdateUI.getString(
						"InstallableSiteForm.state"));

		stateButton = factory.createButton(parent, "", SWT.PUSH);
		stateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				siteStateAction.run();
			}
		});

		factory.createLabel(parent, "");

		FormEngine desc = factory.createFormEngine(parent);
		desc.load(UpdateUI.getString(KEY_DESC), true, true);
		setFocusControl(desc);
		TableData td = new TableData();
		td.align = TableData.FILL;
		td.grabHorizontal = true;
		desc.setLayoutData(td);

		siteStateAction = new SiteStateAction();

		WorkbenchHelp.setHelp(
			parent,
			"org.eclipse.update.ui.InstallableSiteForm");
	}

	protected Object createPropertyLayoutData() {
		TableData td = new TableData();
		//td.indent = 10;
		return td;
	}

	public void expandTo(Object obj) {
		if (obj instanceof IConfiguredSiteAdapter) {
			inputChanged(((IConfiguredSiteAdapter) obj).getConfiguredSite());
		}
	}

	private String getStateButtonLabel(boolean enabled) {
		return UpdateUI.getString(
			enabled
				? "InstallableSiteForm.stateButton.disable"
				: "InstallableSiteForm.stateButton.enable");
	}

	private String getStateLabel(boolean enabled) {
		return UpdateUI.getString(
			enabled
				? "InstallableSiteForm.stateLabel.enabled"
				: "InstallableSiteForm.stateLabel.disabled");
	}

	private String getSiteType(IConfiguredSite csite) {
		if (csite.isProductSite())
			return UpdateUI.getString("InstallableSiteForm.type.product");
		if (csite.isExtensionSite())
			return UpdateUI.getString("InstallableSiteForm.type.extension");
		return UpdateUI.getString("InstallableSiteForm.type.private");
	}

	private void inputChanged(IConfiguredSite csite) {
		ISite site = csite.getSite();
		urlLabel.setText(site.getURL().toString());
		typeLabel.setText(getSiteType(csite));

		stateLabel.setText(getStateLabel(csite.isEnabled()));
		stateButton.setText(getStateButtonLabel(csite.isEnabled()));
		siteStateAction.setSite(csite);
		urlLabel.getParent().layout();
		((Composite) getControl()).layout();
		getControl().redraw();
		currentSite = csite;
	}
}
