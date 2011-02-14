/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.views.markers.FilterConfigurationArea;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * @since 3.7
 *
 */
public class ConfigurationEditDialog extends TitleAreaDialog {

	private final MarkerContentGenerator generator;
	private ScrolledForm form;
	private Collection configAreas;
	private GroupFilterConfigurationArea scopeArea = new ScopeArea();
	private final MarkerFieldFilterGroup filterGroup;
	private Text nameText;
	private Collection currentConfigurationNames;

	/**
	 * @param parentShell
	 */
	protected ConfigurationEditDialog(Shell parentShell, MarkerContentGenerator generator, MarkerFieldFilterGroup markerFieldFilterGroup) {
		super(parentShell);
		this.generator = generator;
		this.filterGroup = markerFieldFilterGroup;
		setHelpAvailable(false);
	}
	
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 * 
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite nameComposite = new Composite(composite, SWT.NONE);
		nameComposite.setLayout(new GridLayout(2, false));
		nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(nameComposite, SWT.NONE);
		label.setText(MarkerMessages.configEditDialog_name);
		nameText = new Text(nameComposite, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				validate();				
			}
		});
		
		final FormToolkit toolkit = new FormToolkit(composite.getDisplay());
		composite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});
		form = toolkit.createScrolledForm(composite);
		form.setBackground(composite.getBackground());

		form.getBody().setLayout(new GridLayout());

		configAreas = generator.createFilterConfigurationFields();

		createFieldArea(toolkit, form, scopeArea, true);
		Iterator areas = configAreas.iterator();
		while (areas.hasNext()) {
			createFieldArea(toolkit, form,
					(FilterConfigurationArea) areas.next(), true);
		}

		initUI();

		return container;
	}

	private void initUI() {

		setTitle(MarkerMessages.ConfigurationEditDialog_title);
		setMessage(MarkerMessages.ConfigurationEditDialog_message);

		nameText.setText(filterGroup.getName());
		scopeArea.initializeFromGroup(filterGroup);
		Iterator areas = configAreas.iterator();
		while (areas.hasNext()) {
			FilterConfigurationArea area = (FilterConfigurationArea) areas
					.next();
			if (area instanceof GroupFilterConfigurationArea)
				((GroupFilterConfigurationArea) area)
						.initializeFromGroup(filterGroup);
			area.initialize(filterGroup.getFilter(area.getField()));
		}
	}
	
	/**
	 * Create a field area in the form for the FilterConfigurationArea
	 * 
	 * @param toolkit
	 * @param form
	 * @param area
	 * @param expand
	 *            <code>true</code> if the area should be expanded by default
	 */
	private void createFieldArea(final FormToolkit toolkit,
			final ScrolledForm form, final FilterConfigurationArea area,
			boolean expand) {
		final ExpandableComposite expandable = toolkit
				.createExpandableComposite(form.getBody(),
						ExpandableComposite.TWISTIE);
		expandable.setText(area.getTitle());
		expandable.setBackground(form.getBackground());
		expandable.setLayout(new GridLayout());
		expandable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, area
				.grabExcessVerticalSpace()));
		expandable.addExpansionListener(new IExpansionListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanged
			 * (org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanged(ExpansionEvent e) {
				expandable.getParent().layout(true);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanging
			 * (org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanging(ExpansionEvent e) {

			}
		});

		Composite sectionClient = toolkit.createComposite(expandable);
		sectionClient.setLayout(new GridLayout());
		sectionClient.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false));
		sectionClient.setBackground(form.getBackground());
		area.createContents(sectionClient);
		expandable.setClient(sectionClient);
		expandable.setExpanded(expand);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		applyValues();
		super.okPressed();
	}

	private void applyValues() {
		scopeArea.applyToGroup(filterGroup);
		Iterator areas = configAreas.iterator();
		while (areas.hasNext()) {
			FilterConfigurationArea area = (FilterConfigurationArea) areas
					.next();

			// Handle the internal special cases
			if (area instanceof GroupFilterConfigurationArea)
				((GroupFilterConfigurationArea) area)
						.applyToGroup(filterGroup);
			area.apply(filterGroup.getFilter(area.getField()));
		}
	}
	
	private void validate() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if(okButton == null)
			return;
		String name = nameText.getText();
		if(currentConfigurationNames.contains(name) && !filterGroup.getName().equals(name)) {
			String message = NLS.bind(MarkerMessages.filtersDialog_conflictingName, name);
			setErrorMessage(message);
			okButton.setEnabled(false);
		}else {
			setErrorMessage(null);
			okButton.setEnabled(true);
		}
	}

	public void setCurrentConfigurationNames(
			Collection currentConfigurationNames) {
		this.currentConfigurationNames = currentConfigurationNames;
	}

}
