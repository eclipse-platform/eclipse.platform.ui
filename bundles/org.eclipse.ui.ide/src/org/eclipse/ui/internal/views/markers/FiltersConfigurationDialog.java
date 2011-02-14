/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> 
 * 			- Fix for Bug 214443 Problem view filter created even if I hit Cancel
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.preferences.ViewSettingsDialog;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * FiltersConfigurationDialog is the dialog for configuring the filters for the
 * 
 * @since 3.3
 * 
 */
public class FiltersConfigurationDialog extends ViewSettingsDialog {

	private static final String SELECTED_FILTER_GROUP = "SELECTED_FILTER_GROUP"; //$NON-NLS-1$

	private Collection filterGroups;

	private CheckboxTableViewer configsTable;

	private MarkerFieldFilterGroup selectedFilterGroup;

	private MarkerContentGenerator generator;

	private boolean andFilters = false;

	private Button removeButton;
	private Button editButton;
	private Button allButton;
	private Button andButton;
	private Button orButton;

	private Button limitButton;
	private Spinner limitSpinner;

	/**
	 * Create a new instance of the receiver on builder.
	 * 
	 * @param parentShell
	 * @param generator
	 */
	public FiltersConfigurationDialog(Shell parentShell,
			MarkerContentGenerator generator) {
		super(parentShell);
		filterGroups = makeWorkingCopy(generator.getAllFilters());
		this.generator = generator;
		andFilters = generator.andFilters();
	}

	/**
	 * Return whether or not to AND the filters
	 * 
	 * @return boolean
	 */
	boolean andFilters() {
		return andFilters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.ViewerSettingsAndStatusDialog#
	 * configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(MarkerMessages.configureFiltersDialog_title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	protected boolean isResizable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.ViewerSettingsAndStatusDialog#
	 * createDialogContentArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		Composite container = (Composite) super.createDialogArea(parent);
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createAndOrButtons(composite);
		
		createFilters(composite);
		
		createMarkerLimits(composite);
		
		loadDialogSettings();
		applyDialogFont(container);
		
		initUI();
		
		return container;
	}
	
	private void initUI() {
		
		if (selectedFilterGroup != null) {
			configsTable.setSelection(new StructuredSelection(
					selectedFilterGroup));
		}

		configsTable.setInput(filterGroups);
		Iterator iterator = filterGroups.iterator();
		while (iterator.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) iterator
					.next();
			boolean enabled = group.isEnabled();
			configsTable.setChecked(group, enabled);
		}

		andButton.setSelection(andFilters);
		orButton.setSelection(!andFilters);
		updateRadioButtonsFromTable();
		int limits = generator.getMarkerLimits();
		boolean limitsEnabled = limits != -1;
		limitButton.setSelection(limitsEnabled);
		limitSpinner.setEnabled(limitsEnabled);
		if(limitsEnabled)
			limitSpinner.setSelection(limits);
		else
			limitSpinner.setSelection(1);
		updateButtonEnablement();
		configsTable.getTable().setFocus();
		
	}

	private void updateRadioButtonsFromTable() {
		
		boolean showAll = isShowAll();
		allButton.setSelection(showAll);
		andButton.setEnabled(!showAll);
		orButton.setEnabled(!showAll);
	}

	private void updateShowAll(boolean showAll) {
		
		allButton.setSelection(showAll);
		andButton.setEnabled(!showAll);
		orButton.setEnabled(!showAll);
		if(showAll) {
			configsTable.setAllChecked(false);
		}else {
			// make the first entry checked
			if(filterGroups.size() > 0) {
				Object group = filterGroups.iterator().next();
				configsTable.setChecked(group, true);
			}
		}
	}

	private boolean isShowAll() {
		return configsTable.getCheckedElements().length == 0;
	}
	/**
	 * @param parent
	 */
	private void createMarkerLimits(Composite parent) {
		
		limitButton = new Button(parent, SWT.CHECK);
		limitButton.setText(MarkerMessages.MarkerPreferences_MarkerLimits);
		limitButton.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent e) {
				limitSpinner.setEnabled(limitButton.getSelection());
			}
		});

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		GridData compositeData = new GridData(GridData.FILL_HORIZONTAL);
		compositeData.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.INDENT);
		composite.setLayoutData(compositeData);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(MarkerMessages.MarkerPreferences_VisibleItems);
		
		limitSpinner = new Spinner(composite, SWT.BORDER);
		limitSpinner.setMinimum(1);
		limitSpinner.setMaximum(Integer.MAX_VALUE);
		limitSpinner.setIncrement(1);
		limitSpinner.setPageIncrement(100);
		GridData spinnerData = new GridData();
		spinnerData.minimumWidth = convertWidthInCharsToPixels(6);
		limitSpinner.setLayoutData(spinnerData);
		
	}

	/**
	 * @param parent
	 */
	private void createFilters(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setBackground(parent.getBackground());
		
		configsTable = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableData.widthHint = convertHorizontalDLUsToPixels(200);
		configsTable.getControl().setLayoutData(tableData);
		
		configsTable.setContentProvider(ArrayContentProvider.getInstance());
		configsTable.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((MarkerFieldFilterGroup) element).getName();
			}
		});

		configsTable.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateRadioButtonsFromTable();
			}
		});

		configsTable
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						updateButtonEnablement();
					}
				});

		configsTable.addDoubleClickListener(new IDoubleClickListener() {
			
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) configsTable.getSelection();
				MarkerFieldFilterGroup configuration = (MarkerFieldFilterGroup) selection.getFirstElement();
				editConfiguration(configuration);
			}
		});

		createButtons(composite);

	}

	/**
	 * @param composite
	 */
	private void createButtons(Composite composite) {
		Composite buttons = new Composite(composite, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = 0;
		buttons.setLayout(buttonLayout);
		GridData buttonsData = new GridData();
		buttonsData.verticalAlignment = GridData.BEGINNING;
		buttons.setLayoutData(buttonsData);

		Button addNew = new Button(buttons, SWT.PUSH);
		addNew.setText(MarkerMessages.MarkerFilter_addFilterName);
		addNew.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addConfiguration();
			}
		});
		setButtonLayoutData(addNew);
		
		editButton = new Button(buttons, SWT.PUSH);
		editButton.setText(MarkerMessages.MarkerFilter_editFilterName);
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) configsTable.getSelection();
				MarkerFieldFilterGroup configuration = (MarkerFieldFilterGroup) selection.getFirstElement();
				editConfiguration(configuration);
			}
		});
		setButtonLayoutData(editButton);

		removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText(MarkerMessages.MarkerFilter_deleteSelectedName);
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeFilters(configsTable.getSelection());
			}
		});
		setButtonLayoutData(removeButton);
	}
	
	private boolean editConfiguration(MarkerFieldFilterGroup configuration) {
		
		ConfigurationEditDialog dialog = new ConfigurationEditDialog(getShell(), generator, configuration);
		dialog.setCurrentConfigurationNames(getCurrentConfigurationNames());
		if(dialog.open() == Window.CANCEL)
			return false;
		return true;

	}

	private void createAndOrButtons(Composite parent) {
	
		allButton = new Button(parent, SWT.CHECK);
		allButton.setText(MarkerMessages.ALL_Title);
		allButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateShowAll(allButton.getSelection());
			}
		});

		andButton = new Button(parent, SWT.RADIO);
		andButton.setText(MarkerMessages.AND_Title);
		andButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				andFilters = true;
			}
		});
		GridData andData = new GridData();
		andData.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.INDENT);
		andButton.setLayoutData(andData);

		orButton = new Button(parent, SWT.RADIO);
		orButton.setText(MarkerMessages.OR_Title);
		orButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				andFilters = false;
			}
		});
		GridData orData = new GridData();
		orData.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.INDENT);
		orButton.setLayoutData(orData);
		
	}

	/**
	 * Get a collection of names of the filters currently in the list
	 * 
	 * @return Collection
	 */
	private Collection getCurrentConfigurationNames() {
		Collection names = new ArrayList();
		Iterator filterIterator = filterGroups.iterator();
		while (filterIterator.hasNext()) {
			names.add(((MarkerFieldFilterGroup) filterIterator.next())
					.getName());
		}
		return names;
	}

	private void addConfiguration() {
		String newName = getNewConfigurationName(getCurrentConfigurationNames(), MarkerMessages.MarkerFilter_newFilterName);
		MarkerFieldFilterGroup configuration = createConfiguration(newName);
		boolean cancelPressed = editConfiguration(configuration);
		if(cancelPressed) {
			filterGroups.add(configuration);
			configsTable.refresh();
			configsTable.setSelection(new StructuredSelection(configuration));
			configsTable.setChecked(configuration, true);
			updateRadioButtonsFromTable();
		}
	}

	private String getNewConfigurationName(final Collection avoidNames,
			String initialName) {
		
		String configName = initialName;
		for (int i = 1; avoidNames.contains(configName); i++) {
			configName = initialName+ ' '+ i;
		}
		return configName;
	}

	private MarkerFieldFilterGroup createConfiguration(String newName) {

		MarkerFieldFilterGroup config = new MarkerFieldFilterGroup(null,
				generator);
		config.setName(newName);
		return config;
	}

	/**
	 * Return the dialog settings for the receiver.
	 * 
	 * @return IDialogSettings
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = IDEWorkbenchPlugin.getDefault()
				.getDialogSettings().getSection(this.getClass().getName());

		if (settings == null) {
			settings = IDEWorkbenchPlugin.getDefault().getDialogSettings()
					.addNewSection(this.getClass().getName());
		}

		return settings;
	}

	/**
	 * Return the filter groups modified by the receiver.
	 * 
	 * @return Collection of {@link MarkerFieldFilterGroup}
	 */
	Collection getFilters() {
		return filterGroups;
	}

	/**
	 * Load the dialog settings.
	 */
	private void loadDialogSettings() {
		IDialogSettings settings = getDialogSettings();

		String selection = settings.get(SELECTED_FILTER_GROUP);

		if (selection != null) {
			Iterator groups = filterGroups.iterator();
			while (groups.hasNext()) {
				MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) groups
						.next();
				if (group.getName().equals(selection)) {
					configsTable.setSelection(new StructuredSelection(group));
					return;
				}
			}
		}

		// If there is no initial selection make one
		if(filterGroups.size() > 0) {
			configsTable.setSelection(new StructuredSelection(filterGroups
					.iterator().next()));
		}
	}

	/**
	 * Make a working copy of the groups.
	 * 
	 * @param groups
	 * @return Collection of MarkerFieldFilterGroup
	 */
	private Collection makeWorkingCopy(Collection groups) {
		Iterator initialFiltersIterator = groups.iterator();
		Collection returnFilters = new ArrayList(groups.size());
		while (initialFiltersIterator.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) initialFiltersIterator
					.next();
			MarkerFieldFilterGroup copy = group.makeWorkingCopy();
			if (copy != null)
				returnFilters.add(copy);
		}
		return returnFilters;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		int limits;
		
		if(limitButton.getSelection())
			limits = limitSpinner.getSelection();
		else
			limits = -1;
		
		generator.setMarkerLimits(limits);
		
		Iterator filterGroupIterator = filterGroups.iterator();
		while (filterGroupIterator.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) filterGroupIterator
					.next();
			group.setEnabled(configsTable.getChecked(group));
		}
		saveDialogSettings();

		super.okPressed();

	}

	protected void performDefaults() {
		
		limitButton.setSelection(true);
		limitSpinner.setSelection(IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getDefaultInt(IDEInternalPreferences.MARKER_LIMITS_VALUE));

		filterGroups.clear();
		filterGroups.addAll(generator.getDeclaredFilters());
		configsTable.refresh();
		configsTable.setSelection(new StructuredSelection(
				filterGroups.size() > 1 ? filterGroups.iterator().next()
						: new Object[0]));
		andFilters = false;
		andButton.setSelection(andFilters);
		orButton.setSelection(!andFilters);
		updateRadioButtonsFromTable();
	}

	/**
	 * Remove the filters in selection.
	 * 
	 * @param selection
	 */
	private void removeFilters(ISelection selection) {
		filterGroups.remove(((IStructuredSelection) selection)
				.getFirstElement());
		configsTable.refresh();
		updateRadioButtonsFromTable();
	}

	/**
	 * Save the dialog settings for the receiver.
	 */
	private void saveDialogSettings() {
		IDialogSettings settings = getDialogSettings();

		if (selectedFilterGroup != null)
			settings.put(SELECTED_FILTER_GROUP, selectedFilterGroup.getName());

	}

	private void updateButtonEnablement() {
		boolean empty = configsTable.getSelection().isEmpty();
		editButton.setEnabled(!empty);
		removeButton.setEnabled(!empty);
	}

}
