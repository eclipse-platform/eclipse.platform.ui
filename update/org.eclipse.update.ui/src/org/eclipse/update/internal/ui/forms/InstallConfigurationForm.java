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

import java.util.Date;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.core.Utilities;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.pages.UpdateFormPage;
import org.eclipse.update.internal.ui.parts.SWTUtil;
import org.eclipse.update.ui.forms.internal.*;

public class InstallConfigurationForm extends PropertyWebForm {
	private static final String KEY_CREATED_ON =
		"InstallConfigurationPage.createdOn";
	private static final String KEY_CURRENT_CONFIG =
		"InstallConfigurationPage.currentConfig";
	private static final String KEY_YES = "InstallConfigurationPage.yes";
	private static final String KEY_NO = "InstallConfigurationPage.no";

	private IInstallConfiguration currentConfiguration;
	private Label dateLabel;
	private Label currentLabel;
	private ActivitySection activitySection;
	private RevertSection revertSection;
	private PreserveSection preserveSection;
	private IUpdateModelChangedListener modelListener;

	public InstallConfigurationForm(UpdateFormPage page) {
		super(page);
	}

	public void dispose() {
		super.dispose();
	}

	public void initialize(Object modelObject) {
		setHeadingText("");
		super.initialize(modelObject);
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		modelListener = new IUpdateModelChangedListener() {
			public void objectsAdded(Object parent, Object[] children) {
			}
			public void objectsRemoved(Object parent, Object[] children) {
			}
			public void objectChanged(Object obj, String property) {
				final boolean preserved;
				if (obj instanceof PreservedConfiguration) {
					preserved = true;
					obj = ((PreservedConfiguration) obj).getConfiguration();
				} else
					preserved = false;
				if (obj.equals(currentConfiguration)) {
					SWTUtil.getStandardDisplay().asyncExec(new Runnable() {
						public void run() {
							inputChanged(currentConfiguration, preserved);
						}
					});
				}
			}
		};
		model.addUpdateModelChangedListener(modelListener);
	}

	protected void createContents(Composite parent) {
		HTMLTableLayout layout = new HTMLTableLayout();
		parent.setLayout(layout);
		layout.leftMargin = 10;
		layout.rightMargin = 0;
		layout.topMargin = 10;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.numColumns = 1;

		FormWidgetFactory factory = getFactory();

		dateLabel =
			createProperty(
				parent,
				UpdateUI.getString(KEY_CREATED_ON));
		currentLabel =
			createProperty(
				parent,
				UpdateUI.getString(KEY_CURRENT_CONFIG));
		factory.createLabel(parent, null);

		activitySection = new ActivitySection((UpdateFormPage) getPage());
		Control control = activitySection.createControl(parent, factory);
		TableData td = new TableData();
		//td.align = TableData.FILL;
		//td.grabHorizontal = true;
		td.valign = TableData.TOP;
		//td.colspan = 2;
		control.setLayoutData(td);

		revertSection = new RevertSection((UpdateFormPage) getPage());
		control = revertSection.createControl(parent, factory);
		td = new TableData();
		td.align = TableData.FILL;
		td.grabHorizontal = true;
		td.valign = TableData.TOP;
		control.setLayoutData(td);
		setFocusControl(revertSection.getFocusControl());

		preserveSection = new PreserveSection((UpdateFormPage) getPage());
		control = preserveSection.createControl(parent, factory);
		td = new TableData();
		td.align = TableData.FILL;
		td.grabHorizontal = true;
		td.valign = TableData.TOP;
		control.setLayoutData(td);

		registerSection(activitySection);
		registerSection(revertSection);
		registerSection(preserveSection);
		WorkbenchHelp.setHelp(
			parent,
			"org.eclipse.update.ui.InstallConfigurationForm");
	}

	protected Object createPropertyLayoutData() {
		TableData td = new TableData();
		//td.indent = 10;
		return td;
	}

	public void expandTo(Object obj) {
		boolean preserved = false;
		if (obj instanceof PreservedConfiguration) {
			preserved = true;
			obj = ((PreservedConfiguration) obj).getConfiguration();
		}
		if (obj instanceof IInstallConfiguration) {
			inputChanged((IInstallConfiguration) obj, preserved);
		}
	}

	private void inputChanged(
		IInstallConfiguration configuration,
		boolean preserved) {
		setHeadingText(configuration.getLabel());
		Date date = configuration.getCreationDate();
		dateLabel.setText(Utilities.format(date));
		String isCurrent =
			configuration.isCurrent()
				? UpdateUI.getString(KEY_YES)
				: UpdateUI.getString(KEY_NO);
		currentLabel.setText(isCurrent);

		activitySection.configurationChanged(configuration);
		revertSection.configurationChanged(configuration);
		preserveSection.configurationChanged(!preserved ? configuration : null);
		// reflow
		dateLabel.getParent().layout(true);
		((Composite) getControl()).layout(true);
		getControl().redraw();
		updateSize();
		currentConfiguration = configuration;
	}

	public void objectChanged(Object object, String property) {
		if (object.equals(currentConfiguration)) {
			expandTo(object);
		}
	}

}
