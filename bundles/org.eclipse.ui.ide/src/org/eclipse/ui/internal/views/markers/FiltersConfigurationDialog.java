/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 * 			- Fix for Bug 214443 Problem view filter created even if I hit Cancel
 *     Robert Roth <robert.roth.off@gmail.com>
 *          - Fix for Bug 364736 Setting limit to 0 has no effect
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 498056
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 501523
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.preferences.ViewSettingsDialog;
import org.eclipse.ui.views.markers.FilterConfigurationArea;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * FiltersConfigurationDialog is the dialog for configuring the filters for the
 * problems view
 *
 * @since 3.3
 *
 */
public class FiltersConfigurationDialog extends ViewSettingsDialog {

	private static final String SELECTED_FILTER_GROUP = "SELECTED_FILTER_GROUP"; //$NON-NLS-1$
	private static final String PREV_SELECTED_ELEMENTS = "PREV_SELECTED_ELEMENTS"; //$NON-NLS-1$

	private Collection<MarkerFieldFilterGroup> filterGroups;

	private CheckboxTableViewer configsTable;

	private MarkerFieldFilterGroup selectedFilterGroup;

	private MarkerContentGenerator generator;

	private boolean andFilters = false;

	private Button removeButton;
	private Button renameButton;

	private Button allButton;

	private Button limitButton;
	private Text limitText;

	private GroupFilterConfigurationArea scopeArea = new ScopeArea();
	private ScrolledForm form;

	private Collection<FilterConfigurationArea> configAreas;
	private Label limitsLabel;

	private Object[] previouslyChecked = new Object[0];
	private Group configComposite;
	private Composite compositeLimits;

	/**
	 * Create a new instance of the receiver on builder.
	 *
	 * @param parentShell
	 * @param generator
	 */
	public FiltersConfigurationDialog(Shell parentShell, MarkerContentGenerator generator) {
		super(parentShell);
		filterGroups = makeWorkingCopy(generator.getAllFilters());
		this.generator = generator;
		andFilters = false;
	}

	/**
	 * Return whether or not to AND the filters
	 *
	 * @return boolean
	 */
	boolean andFilters() {
		return andFilters;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(MarkerMessages.configureFiltersDialog_title);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setFont(parent.getFont());

		Composite composite = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(container.getBackground());

		createSelectAllButton(composite);

		configComposite = new Group(composite, SWT.NONE);
		configComposite.setText(MarkerMessages.MarkerConfigurationsLabel);

		configComposite.setLayout(new GridLayout(3, false));
		configComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		configComposite.setBackground(composite.getBackground());

		createConfigs(configComposite);

		createConfigDesc(configComposite);

		createMarkerLimits(composite);

		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		applyDialogFont(container);

		initUI();

		return container;
	}

	private void initUI() {
		configsTable.setInput(filterGroups);
		IStructuredSelection selection = getInitialSelection();
		configsTable.setSelection(selection);
		if (selection.isEmpty()) {
			setFieldsEnabled(false);
		}

		Iterator<MarkerFieldFilterGroup> iterator = filterGroups.iterator();
		while (iterator.hasNext()) {
			MarkerFieldFilterGroup group = iterator.next();
			boolean enabled = group.isEnabled();
			configsTable.setChecked(group, enabled);
		}

		updateRadioButtonsFromTable();
		int limits = generator.getMarkerLimits();
		boolean limitsEnabled = generator.isMarkerLimitsEnabled();
		limitButton.setSelection(limitsEnabled);
		updateLimitTextEnablement();
		limitText.setText(Integer.toString(limits));
		configsTable.getTable().setFocus();
	}

	private void updateRadioButtonsFromTable() {
		boolean showAll = isShowAll();
		allButton.setSelection(showAll);
		updateConfigComposite(!showAll);
	}

	private void updateConfigComposite(boolean enabled) {
		if (enabled)
			updateButtonEnablement(getSelectionFromTable());
	}

	/** Update the enablement of limitText */
	private void updateLimitTextEnablement() {
		boolean useLimits = limitButton.getSelection();
		limitsLabel.setEnabled(useLimits);
		limitText.setEnabled(useLimits);
	}

	private void updateShowAll(boolean showAll) {
		allButton.setSelection(showAll);
		updateConfigComposite(!showAll);
		updateLimitTextEnablement();

		if (showAll) {
			previouslyChecked = configsTable.getCheckedElements();
			configsTable.setAllChecked(false);
		} else {
			if (previouslyChecked != null && previouslyChecked.length > 0) {
				configsTable.setCheckedElements(previouslyChecked);
			} else {
				// make the first entry checked
				if (filterGroups.size() > 0) {
					Object group = filterGroups.iterator().next();
					configsTable.setChecked(group, true);
				}
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
		compositeLimits = new Composite(parent, SWT.NONE);
		GridLayout glCompositeLimits = new GridLayout(3, false);
		compositeLimits.setLayout(glCompositeLimits);

		limitButton = new Button(compositeLimits, SWT.CHECK);
		limitButton.setText(MarkerMessages.MarkerPreferences_MarkerLimits);
		limitButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLimitTextEnablement();
			}
		});

		GridData limitData = new GridData();
		limitData.verticalIndent = 5;
		limitButton.setLayoutData(limitData);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData compositeData = new GridData(GridData.FILL_HORIZONTAL);
		compositeData.horizontalIndent = 20;
		composite.setLayoutData(compositeData);

		limitsLabel = new Label(composite, SWT.NONE);
		limitsLabel.setText(MarkerMessages.MarkerPreferences_VisibleItems);

		limitText = new Text(composite, SWT.BORDER);
		GridData textData = new GridData();
		textData.widthHint = convertWidthInCharsToPixels(10);
		limitText.setLayoutData(textData);
		limitText.addVerifyListener(e -> {
			if (e.character != 0 && e.keyCode != SWT.BS
					&& e.keyCode != SWT.DEL
					&& !Character.isDigit(e.character)) {
				e.doit = false;
			}
		});

		limitText.addModifyListener(e -> {
			boolean isInvalid = false;
			try {
				int value = Integer.parseInt(limitText.getText());
				if (value <= 0) {
					isInvalid = true;
				}
			} catch (NumberFormatException ex) {
				isInvalid = true;
			}
			if (isInvalid) {
				limitText.setText(Integer.toString(generator.getMarkerLimits()));
			}
		});
	}

	/**
	 * @param parent
	 */
	private void createConfigs(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setBackground(parent.getBackground());

		configsTable = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableData.widthHint = convertHorizontalDLUsToPixels(120);
		configsTable.getControl().setLayoutData(tableData);

		configsTable.setContentProvider(ArrayContentProvider.getInstance());
		configsTable.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((MarkerFieldFilterGroup) element).getName();
			}
		});

		configsTable.addCheckStateListener(event -> {
			configsTable.setSelection(new StructuredSelection(event.getElement()));
			updateRadioButtonsFromTable();
		});

		configsTable.addSelectionChangedListener(event -> {
			storeConfiguration();
			MarkerFieldFilterGroup group = getSelectionFromTable();
			if (group == null) {
				setFieldsEnabled(false);
			} else {
				setFieldsEnabled(true);
			}
			updateButtonEnablement(group);
			updateConfigDesc(group);
			selectedFilterGroup = group;
		});

		createButtons(composite);
	}

	private void storeConfiguration() {
		if (selectedFilterGroup == null) {
			return;
		}

		scopeArea.applyToGroup(selectedFilterGroup);
		Iterator<FilterConfigurationArea> areas = configAreas.iterator();
		while (areas.hasNext()) {
			FilterConfigurationArea area = areas.next();

			// Handle the internal special cases
			if (area instanceof GroupFilterConfigurationArea) {
				((GroupFilterConfigurationArea) area).applyToGroup(selectedFilterGroup);
			}
			area.apply(selectedFilterGroup.getFilter(area.getField()));
		}
		configsTable.refresh(selectedFilterGroup);
	}

	private void updateConfigDesc(MarkerFieldFilterGroup configuration) {
		if (configuration == null) {
			return;
		}

		scopeArea.initializeFromGroup(configuration);
		Iterator<FilterConfigurationArea> areas = configAreas.iterator();
		while (areas.hasNext()) {
			FilterConfigurationArea area = areas.next();
			if (area instanceof GroupFilterConfigurationArea) {
				((GroupFilterConfigurationArea) area).initializeFromGroup(configuration);
			}
			area.initialize(configuration.getFilter(area.getField()));
		}
	}

	private void createConfigDesc(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
		separator.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Composite descComposite = new Composite(parent, SWT.NONE);
		descComposite.setLayout(new FillLayout());
		descComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		descComposite.setBackground(parent.getBackground());

		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		parent.addDisposeListener(e -> toolkit.dispose());

		form = toolkit.createScrolledForm(descComposite);
		form.setBackground(parent.getBackground());
		form.getBody().setLayout(new GridLayout());

		configAreas = generator.createFilterConfigurationFields();

		createFieldArea(toolkit, form, scopeArea, true);
		Iterator<FilterConfigurationArea> areas = configAreas.iterator();
		while (areas.hasNext()) {
			createFieldArea(toolkit, form, areas.next(), true);
		}
	}

	/**
	 * Create a field area in the form for the FilterConfigurationArea
	 *
	 * @param toolkit
	 * @param scrolledForm
	 * @param area
	 * @param expand
	 *            <code>true</code> if the area should be expanded by default
	 */
	private void createFieldArea(final FormToolkit toolkit, final ScrolledForm scrolledForm,
			final FilterConfigurationArea area, boolean expand) {
		final ExpandableComposite expandable = toolkit.createExpandableComposite(scrolledForm.getBody(),
				ExpandableComposite.TWISTIE);
		expandable.setText(area.getTitle());
		expandable.setBackground(scrolledForm.getBackground());
		expandable.setLayout(new GridLayout());
		expandable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, area.grabExcessVerticalSpace()));
		expandable.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				scrolledForm.reflow(true);
			}
		});

		Composite sectionClient = toolkit.createComposite(expandable);
		GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 3;
		sectionClient.setLayout(gridLayout);
		sectionClient.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		sectionClient.setBackground(scrolledForm.getBackground());

		area.createContents(sectionClient);
		expandable.setClient(sectionClient);
		expandable.setExpanded(expand);
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
			@Override
			public void widgetSelected(SelectionEvent e) {
				addConfiguration();
			}
		});
		setButtonLayoutData(addNew);

		removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText(MarkerMessages.MarkerFilter_deleteSelectedName);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeFilters(configsTable.getStructuredSelection());
			}
		});
		removeButton.setEnabled(false);
		setButtonLayoutData(removeButton);

		renameButton = new Button(buttons, SWT.PUSH);
		renameButton.setText(MarkerMessages.MarkerFilter_renameName);
		renameButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				renameFilter();
			}
		});
		renameButton.setEnabled(false);
		setButtonLayoutData(renameButton);
	}

	private void renameFilter() {
		MarkerFieldFilterGroup filterGroup = getSelectionFromTable();
		IInputValidator nameValidator = getNameValidator(filterGroup.getName(), getCurrentConfigurationNames());
		InputDialog inputDialog = new InputDialog(getShell(),
				MarkerMessages.MarkerFilterDialog_title,
				MarkerMessages.MarkerFilterDialog_message,
				filterGroup.getName(), nameValidator);

		if(inputDialog.open() == Window.OK) {
			filterGroup.setName(inputDialog.getValue());
			configsTable.refresh(filterGroup);
		}
	}

	private IInputValidator getNameValidator(final String currentName, final Collection<String> existingNames) {
		return newText -> {
			newText = newText.trim();
			if (newText.length() == 0) {
				return MarkerMessages.MarkerFilterDialog_emptyMessage;
			}
			if(existingNames.contains(newText) && !currentName.equals(newText)) {
				return NLS.bind(MarkerMessages.filtersDialog_conflictingName, newText);
			}
			return null;
		};
	}

	private void createSelectAllButton(Composite parent) {
		allButton = new Button(parent, SWT.CHECK);
		allButton.setText(MarkerMessages.ALL_Title);
		allButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateShowAll(allButton.getSelection());
			}
		});

	}

	/**
	 * Get a collection of names of the filters currently in the list
	 *
	 * @return Collection
	 */
	private Collection<String> getCurrentConfigurationNames() {
		Collection<String> names = new ArrayList<>();
		Iterator<MarkerFieldFilterGroup> filterIterator = filterGroups.iterator();
		while (filterIterator.hasNext()) {
			names.add(filterIterator.next().getName());
		}
		return names;
	}

	private void addConfiguration() {
		String newName = getNewConfigurationName(getCurrentConfigurationNames(),
				MarkerMessages.MarkerFilter_newFilterName);
		MarkerFieldFilterGroup configuration = createConfiguration(newName);
		filterGroups.add(configuration);
		configsTable.refresh();
		configsTable.setSelection(new StructuredSelection(configuration));
		configsTable.setChecked(configuration, true);
		updateRadioButtonsFromTable();
	}

	private String getNewConfigurationName(final Collection<String> avoidNames, String initialName) {
		String configName = initialName;
		for (int i = 1; avoidNames.contains(configName); i++) {
			configName = initialName + ' ' + i;
		}
		return configName;
	}

	private MarkerFieldFilterGroup createConfiguration(String newName) {
		MarkerFieldFilterGroup config = new MarkerFieldFilterGroup(null, generator);
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
	Collection<MarkerFieldFilterGroup> getFilters() {
		return filterGroups;
	}

	private IStructuredSelection getInitialSelection() {
		IDialogSettings settings = getDialogSettings();
		String selectedGroupName = settings.get(SELECTED_FILTER_GROUP);

		MarkerFieldFilterGroup selectedGroup = null;
		if (selectedGroupName == null && filterGroups.size() > 0) {
			selectedGroup = filterGroups.iterator().next();
		} else {
			Iterator<MarkerFieldFilterGroup> groups = filterGroups.iterator();
			while (groups.hasNext()) {
				MarkerFieldFilterGroup group = groups.next();
				if (group.getName().equals(selectedGroupName)) {
					selectedGroup = group;
					break;
				}
			}
		}

		String[] selectedElementNames = settings.getArray(PREV_SELECTED_ELEMENTS);
		List<MarkerFieldFilterGroup> selectedElements = new ArrayList<>();

		if (selectedElementNames != null) {
			for (String selectedElementName : selectedElementNames) {
				Iterator<MarkerFieldFilterGroup> filterGroupIterator = filterGroups.iterator();
				while (filterGroupIterator.hasNext()) {
					MarkerFieldFilterGroup group = filterGroupIterator.next();
					if (Util.equals(group.getName(), selectedElementName)) {
						selectedElements.add(group);
						break;
					}
				}
			}
			previouslyChecked = selectedElements.toArray();
		}

		if (selectedGroup != null) {
			return new StructuredSelection(selectedGroup);
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * Make a working copy of the groups.
	 *
	 * @param groups
	 * @return Collection of MarkerFieldFilterGroup
	 */
	private Collection<MarkerFieldFilterGroup> makeWorkingCopy(Collection<MarkerFieldFilterGroup> groups) {
		Iterator<MarkerFieldFilterGroup> initialFiltersIterator = groups.iterator();
		Collection<MarkerFieldFilterGroup> returnFilters = new ArrayList<>(groups.size());
		while (initialFiltersIterator.hasNext()) {
			MarkerFieldFilterGroup group = initialFiltersIterator.next();
			MarkerFieldFilterGroup copy = group.makeWorkingCopy();
			if (copy != null) {
				returnFilters.add(copy);
			}
		}
		return returnFilters;
	}

	@Override
	protected void okPressed() {
		generator.setMarkerLimitsEnabled(limitButton.getSelection());
		generator.setMarkerLimits(Integer.parseInt(limitText.getText().trim()));

		Iterator<MarkerFieldFilterGroup> filterGroupIterator = filterGroups.iterator();
		while (filterGroupIterator.hasNext()) {
			MarkerFieldFilterGroup group = filterGroupIterator.next();
			group.setEnabled(configsTable.getChecked(group));
		}

		storeConfiguration();
		saveDialogSettings();
		super.okPressed();
	}

	@Override
	protected void performDefaults() {
		andFilters = false;

		filterGroups.clear();
		List<MarkerFieldFilterGroup> declaredFilters = new ArrayList<>(generator.getDeclaredFilters());
		filterGroups.addAll(declaredFilters);
		configsTable.refresh();

		for (MarkerFieldFilterGroup marker : declaredFilters) {
			if (marker.isEnabled()) {
				configsTable.setChecked(marker, true);
			}
		}

		IPreferenceStore preferenceStore = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		boolean useMarkerLimits = preferenceStore.getBoolean(IDEInternalPreferences.USE_MARKER_LIMITS);
		int markerLimits = useMarkerLimits ? preferenceStore.getInt(IDEInternalPreferences.MARKER_LIMITS_VALUE) : 1000;

		limitButton.setSelection(useMarkerLimits);
		updateLimitTextEnablement();
		limitText.setText(Integer.toString(markerLimits));
		updateRadioButtonsFromTable();
	}

	/**
	 * Remove the filters in selection.
	 *
	 * @param selection
	 */
	private void removeFilters(ISelection selection) {
		filterGroups.remove(((IStructuredSelection) selection).getFirstElement());
		configsTable.refresh();
		updateRadioButtonsFromTable();
	}

	/**
	 * Save the dialog settings for the receiver.
	 */
	private void saveDialogSettings() {
		IDialogSettings settings = getDialogSettings();
		if (selectedFilterGroup != null) {
			settings.put(SELECTED_FILTER_GROUP, selectedFilterGroup.getName());
		}

		String[] selectedNames = new String[previouslyChecked.length];
		for (int i = 0; i < selectedNames.length; i++) {
			selectedNames[i] = ((MarkerFieldFilterGroup)previouslyChecked[i]).getName();
		}
		settings.put(PREV_SELECTED_ELEMENTS, selectedNames);
	}

	private void updateButtonEnablement(MarkerFieldFilterGroup group) {
		boolean enabled = group != null && !group.isSystem();
		removeButton.setEnabled(enabled);
		renameButton.setEnabled(enabled);
	}

	private MarkerFieldFilterGroup getSelectionFromTable() {
		IStructuredSelection selection = (IStructuredSelection) configsTable.getSelection();
		return (MarkerFieldFilterGroup) selection.getFirstElement();
	}

	/**
	 * Set the control and all of it's visibility state to visible.
	 *
	 * @param enabled
	 * @param control
	 */
	private void setEnabled(boolean enabled, Control control) {
		control.setEnabled(enabled);
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				setEnabled(enabled, child);
			}
		}
	}

	/**
	 * Set the enablement state of the fields to enabled.
	 */
	private void setFieldsEnabled(boolean enabled) {
		setEnabled(enabled, form);
	}

}
