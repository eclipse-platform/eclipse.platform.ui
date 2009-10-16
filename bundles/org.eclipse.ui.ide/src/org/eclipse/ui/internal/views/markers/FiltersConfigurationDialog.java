/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.preferences.ViewSettingsDialog;
import org.eclipse.ui.views.markers.FilterConfigurationArea;
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

	private CheckboxTableViewer filtersList;

	private GroupFilterConfigurationArea scopeArea = new ScopeArea();

	private MarkerFieldFilterGroup selectedFilterGroup;

	private ScrolledForm form;

	private MarkerContentGenerator generator;

	private Collection filterAreas;

	private boolean andFilters = false;

	private Button removeButton;

	private Button renameButton;
	
	private Button cloneButton;

	private Button andButton;

	private Button orButton;

	private Label andOrLabel;

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
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	public boolean close() {
		saveDialogSettings();
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		parent.getShell().setText(MarkerMessages.configureFiltersDialog_title);

		Composite top = (Composite) super.createDialogArea(parent);

		initializeDialogUnits(top);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		top.setLayout(layout);

		createFilterSelectionArea(top);
		
		Label seprator=new Label(top, SWT.SEPARATOR|SWT.VERTICAL);
		seprator.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));
		
		final FormToolkit toolkit = new FormToolkit(top.getDisplay());
		parent.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();

			}
		});
		form = toolkit.createScrolledForm(top);
		form.setBackground(parent.getBackground());

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		form.setLayoutData(data);
		form.getBody().setLayout(new GridLayout());

		filterAreas = generator.createFilterConfigurationFields();

		createFieldArea(toolkit, form, scopeArea, true);
		Iterator areas = filterAreas.iterator();

		while (areas.hasNext()) {
			createFieldArea(toolkit, form, (FilterConfigurationArea) areas
					.next(), true);
		}

		if (filterGroups.isEmpty())
			setFieldsEnabled(false);
		else
			loadDialogSettings();

		applyDialogFont(top);
		return top;
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
		expandable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, area.grabExcessVerticalSpace()));
		expandable.addExpansionListener(new IExpansionListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanged(ExpansionEvent e) {
				expandable.getParent().layout(true);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent)
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

	/**
	 * Create the area for selecting the filters and enabling/disabling them.
	 * 
	 * @param top
	 */
	private void createFilterSelectionArea(Composite top) {

		Composite filtersComposite = new Composite(top, SWT.NONE);
		filtersComposite.setLayout(new GridLayout(2, false));
		filtersComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));

		Label title = new Label(filtersComposite, SWT.NONE);
		title.setText(MarkerMessages.filtersDialog_entriesTitle);
		GridData titleData = new GridData();
		titleData.horizontalSpan = 2;
		title.setLayoutData(titleData);

		filtersList = CheckboxTableViewer.newCheckList(filtersComposite,
				SWT.BORDER);
		
		filtersList.setContentProvider(new IStructuredContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
				// Do nothing
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return filterGroups.toArray();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// Do nothing
			}
		});

		filtersList.setLabelProvider(new LabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((MarkerFieldFilterGroup) element).getName();
			}
		});

		if (selectedFilterGroup != null)
			filtersList.setSelection(new StructuredSelection(
					selectedFilterGroup));

		filtersList
				.addSelectionChangedListener(new ISelectionChangedListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
					 */
					public void selectionChanged(SelectionChangedEvent event) {
						setSelectedFilter((MarkerFieldFilterGroup) ((IStructuredSelection) event
								.getSelection()).getFirstElement());
					}
				});

		filtersList.setInput(this);

		Iterator filterIterator = filterGroups.iterator();
		while (filterIterator.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) filterIterator
					.next();
			filtersList.setChecked(group, group.isEnabled());
		}

		GridData listData = new GridData(SWT.FILL, SWT.FILL, true, true);
		listData.widthHint = convertHorizontalDLUsToPixels(100);
		filtersList.getControl().setLayoutData(listData);

		Composite buttons = new Composite(filtersComposite, SWT.NONE);
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
				addNewFilter(false);
			}			
		});
		setButtonLayoutData(addNew);
		
		cloneButton= new Button(buttons, SWT.PUSH);
		cloneButton.setText(MarkerMessages.MarkerFilter_cloneFilterName);
		cloneButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addNewFilter(true);
			}
		});
		setButtonLayoutData(cloneButton);

		removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText(MarkerMessages.MarkerFilter_deleteSelectedName);
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeFilters(filtersList.getSelection());
			}
		});
		setButtonLayoutData(removeButton);
		
		renameButton = new Button(buttons, SWT.PUSH);
		renameButton.setText(MarkerMessages.MarkerFilter_renameName);
		renameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MarkerFieldFilterGroup filterGroup = (MarkerFieldFilterGroup) ((IStructuredSelection) filtersList
						.getSelection()).getFirstElement();
				renameFilter(filterGroup);
			}
		});
		setButtonLayoutData(renameButton);
		
		andOrLabel = new Label(filtersComposite, SWT.NONE);
		GridData labelData = new GridData();
		labelData.horizontalSpan = 2;
		andOrLabel.setLayoutData(labelData);
		andOrLabel.setText(MarkerMessages.AND_OR_Label);
		
		andButton = new Button(filtersComposite, SWT.RADIO);
		GridData data = new GridData(GridData.FILL_HORIZONTAL, SWT.NONE, true,
				false);
		data.horizontalSpan = 2;
		data.horizontalIndent = IDialogConstants.INDENT;
		andButton.setLayoutData(data);
		andButton.setText(MarkerMessages.AND_Title);
		andButton.setSelection(andFilters);
		andButton.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				andFilters = true;
			}
		});

		orButton = new Button(filtersComposite, SWT.RADIO);
		data = new GridData(GridData.FILL_HORIZONTAL, SWT.NONE, true, false);
		data.horizontalSpan = 2;
		data.horizontalIndent = IDialogConstants.INDENT;
		orButton.setLayoutData(data);
		orButton.setText(MarkerMessages.OR_Title);
		orButton.setSelection(!andFilters);
		orButton.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				andFilters = false;
			}
		});
		filtersList.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateAndOrEnblement();				
			}
		});
	}

	/**
	 * Opens Input Dialog for name,creates a 
	 * new filterGroup, and adds it to the filterGroups 
	 * @param cloneSelected true clones the selected filterGroup
	 * 				
	 */
	private void addNewFilter(boolean cloneSelected) {
		String newName =getNewFilterName(getCurrentFilterNames(),null);
		if (newName != null) {
			createNewFilter(newName,cloneSelected);
		}
	}
	/**
	 * Opens Input Dialog for a new filter name
	 * @param avoidNames filter names to avoid
	 * @param initialName initial name of the filter
	 * @return new filter name or null if canceled
	 * 				
	 */
	private String getNewFilterName(final Collection avoidNames,String initialName){
		InputDialog newDialog = new InputDialog(getShell(),
				MarkerMessages.MarkerFilterDialog_title,
				MarkerMessages.MarkerFilterDialog_message,
				initialName != null ? initialName
						: MarkerMessages.MarkerFilter_newFilterName,
				getNameValidator(avoidNames));
		if (Window.OK == newDialog.open()) {
			return newDialog.getValue();
		}
		return null;
	}

	/**
	 * Get IInputValidator for checking if the new name is valid
	 * @param avoidNames
	 * @return IInputValidator
	 */
	private IInputValidator getNameValidator(final Collection avoidNames) {
		return new IInputValidator() {
			public String isValid(String value) {
				String newText=value.trim();
				if (newText.length() == 0)
					return MarkerMessages.MarkerFilterDialog_emptyMessage;
				if (avoidNames.contains(newText))
					return NLS.bind(
							MarkerMessages.filtersDialog_conflictingName,
							newText);
				return null;
			}
		};
	}
	
	/**
	 * Get a collection of names of the filters currently in the list
	 * @return Collection
	 */
	private Collection getCurrentFilterNames() {
		Collection names = new ArrayList();
		Iterator filterIterator = filterGroups.iterator();
		while (filterIterator.hasNext()) {
			names.add(((MarkerFieldFilterGroup) filterIterator.next()).getName());
		}
		return names;
	}
	/**
	 * Create a new filterGroup, and adds it to the filterGroups 
	 * @param cloneSelected true clones the selected filterGroup
	 * @param newName name of new filterGroup
	 */
	private void createNewFilter(String newName,boolean cloneSelected) {
		MarkerFieldFilterGroup group = new MarkerFieldFilterGroup(null, generator);
		if(cloneSelected&&selectedFilterGroup!=null){
			captureStateInto(group); //copy current values from UI
		}
		group.setName(newName);
		filterGroups.add(group);
		filtersList.refresh();
		filtersList.setSelection(new StructuredSelection(group));
		filtersList.setChecked(group, true);
		updateAndOrEnblement();
	}

	/**
	 * Renames the supplied MarkerFieldFilterGroup
	 * @param filterGroup
	 */
	private void renameFilter(MarkerFieldFilterGroup filterGroup) {
		if (filterGroup != null) {
			Collection names = getCurrentFilterNames();
			String initial = null;
			initial = filterGroup.getName();
			names.remove(initial);
			String newName=getNewFilterName(names, initial);
			if(newName!=null){
				filterGroup.setName(newName);
				filtersList.update(filterGroup, null);
			}
		}
	}
	/**
	 * Enable/disable 'and', 'or' buttons
	 */
	private void updateAndOrEnblement() {
		if(filtersList.getCheckedElements().length==0){
			andOrLabel.setEnabled(false);
			andButton.setEnabled(false);
			orButton.setEnabled(false);
		}else{
			andOrLabel.setEnabled(true);
			andButton.setEnabled(true);
			orButton.setEnabled(true);
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	protected boolean isResizable() {
		return true;
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
					filtersList.setSelection(new StructuredSelection(group));
					return;
				}
			}
		}

		// If there is no initial selection make one
		filtersList.setSelection(new StructuredSelection(filterGroups
				.iterator().next()));
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

		if (!shouldContinue())
			return;

		Iterator filterGroupIterator = filterGroups.iterator();
		while (filterGroupIterator.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) filterGroupIterator
					.next();
			group.setEnabled(filtersList.getChecked(group));
		}
		captureStateInto(selectedFilterGroup);

		super.okPressed();

	}

	/**
	 * 
	 * Updates the filterGroup with the values showing in the dialog's GUI.
	 * @param filterGroup 
	 * 
	 */
	private void captureStateInto(MarkerFieldFilterGroup filterGroup) {
		if (filterGroup != null) {

			scopeArea.applyToGroup(filterGroup);
			Iterator areas = filterAreas.iterator();
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
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.preferences.ViewSettingsDialog#performDefaults()
	 */
	protected void performDefaults() {
		filterGroups.clear();
		filterGroups.addAll(generator.getDeclaredFilters());
		filtersList.refresh();
		filtersList.setSelection(new StructuredSelection(
				filterGroups.size() > 1 ? filterGroups.iterator().next()
						: new Object[0]));
		andFilters=false;
		andButton.setSelection(andFilters);
		orButton.setSelection(!andFilters);
	}

	/**
	 * Return whether or not deselected elements should have been selected.
	 * 
	 * @return boolean
	 */
	private boolean shouldContinue() {
		if (filtersList.getCheckedElements().length == 0) {
			return MessageDialog.openQuestion(getShell(),
					MarkerMessages.filtersDialogDeselectedFiltersTitle,
					MarkerMessages.filtersDialogDeselectedFiltersMessage);
		}

		return true;
	}

	/**
	 * Remove the filters in selection.
	 * 
	 * @param selection
	 */
	private void removeFilters(ISelection selection) {
		filterGroups.remove(((IStructuredSelection) selection)
				.getFirstElement());
		filtersList.refresh();
		updateAndOrEnblement();
	}

	/**
	 * Save the dialog settings for the receiver.
	 */
	private void saveDialogSettings() {
		IDialogSettings settings = getDialogSettings();

		if (selectedFilterGroup != null)
			settings.put(SELECTED_FILTER_GROUP, selectedFilterGroup.getName());

	}

	/**
	 * Set the control and all of it's visibility state to visible.
	 * 
	 * @param visible
	 * @param control
	 */
	private void setEnabled(boolean visible, Control control) {
		control.setEnabled(visible);
		if (control instanceof Composite) {
			Control[] children = ((Composite) control).getChildren();
			for (int i = 0; i < children.length; i++) {
				setEnabled(visible, children[i]);
			}
		}
	}

	/**
	 * Set the enablement state of the fields to enabled.
	 */
	private void setFieldsEnabled(boolean visible) {
		setEnabled(visible, form);
	}

	/**
	 * Set the filter that is being worked on.
	 * 
	 * @param markerFieldFilterGroup
	 */
	private void setSelectedFilter(MarkerFieldFilterGroup markerFieldFilterGroup) {
		if(selectedFilterGroup==markerFieldFilterGroup){
			return;
		}
		removeButton
				.setEnabled(!(markerFieldFilterGroup == null || markerFieldFilterGroup
						.isSystem()));
		renameButton
				.setEnabled(!(markerFieldFilterGroup == null || markerFieldFilterGroup
						.isSystem()));
		cloneButton.setEnabled(markerFieldFilterGroup != null);
		
		MarkerFieldFilterGroup old = selectedFilterGroup;
		selectedFilterGroup = markerFieldFilterGroup;
		if (old != null)
			scopeArea.applyToGroup(old);

		if (selectedFilterGroup == null) {
			setFieldsEnabled(false);
			return;
		}

		setFieldsEnabled(true);
		scopeArea.initializeFromGroup(selectedFilterGroup);
		Iterator areas = filterAreas.iterator();
		while (areas.hasNext()) {
			FilterConfigurationArea area = (FilterConfigurationArea) areas
					.next();
			if (old != null) {
				if (area instanceof GroupFilterConfigurationArea)
					((GroupFilterConfigurationArea) area).applyToGroup(old);
				area.apply(old.getFilter(area.getField()));
			}
			if (area instanceof GroupFilterConfigurationArea)
				((GroupFilterConfigurationArea) area)
						.initializeFromGroup(selectedFilterGroup);
			area.initialize(selectedFilterGroup.getFilter(area.getField()));
		}
	}
}
