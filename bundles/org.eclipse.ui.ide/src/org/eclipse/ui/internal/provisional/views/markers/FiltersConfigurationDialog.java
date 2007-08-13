/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.IShellProvider;
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
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * FiltersConfigurationDialog is the dialog for configuring the filters for the
 * 
 * @since 3.3
 * 
 */
public class FiltersConfigurationDialog extends Dialog {

	private Collection filterAreas;

	private Collection filterGroups;

	private CheckboxTableViewer filtersList;

	private FilterConfigurationArea scopeArea = new ScopeArea();

	private MarkerFieldFilterGroup selectedFilterGroup;

	/**
	 * Create a new instance of the receiver on group.
	 * 
	 * @param parentShell
	 * @param groups
	 *            Collection of MarkerFieldFilterGroup
	 * @param fieldFilterAreas -
	 *            Collection of FilterConfigurationArea
	 */
	public FiltersConfigurationDialog(IShellProvider parentShell,
			Collection groups, Collection fieldFilterAreas) {
		super(parentShell);
		filterGroups = makeWorkingCopy(groups);
		filterAreas = fieldFilterAreas;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		Composite top = (Composite) super.createDialogArea(parent);

		initializeDialogUnits(top);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		top.setLayout(layout);

		createFilterSelectionArea(top);

		final FormToolkit toolkit = new FormToolkit(top.getDisplay());
		parent.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();

			}
		});
		final ScrolledForm form = toolkit.createScrolledForm(top);
		form.setBackground(parent.getBackground());

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		form.setLayoutData(data);
		form.getBody().setLayout(new GridLayout());

		// Expand all of the filter areas if the choices are small
		boolean expand = filterAreas.size() < 3;
		createFieldArea(toolkit, form, scopeArea, expand);
		Iterator areas = filterAreas.iterator();
		while (areas.hasNext()) {
			createFieldArea(toolkit, form, (FilterConfigurationArea) areas
					.next(), expand);

		}

		if (!filterGroups.isEmpty()) {
			filtersList.setSelection(new StructuredSelection(filterGroups
					.iterator().next()));
		}

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
		expandable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
		sectionClient
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
		title.setText(MarkerMessages.filtersDialog_title);
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
				InputDialog newDialog = new InputDialog(getShell(),
						MarkerMessages.MarkerFilterDialog_title,
						MarkerMessages.MarkerFilterDialog_message,
						MarkerMessages.MarkerFilter_newFilterName,
						new IInputValidator() {
							/*
							 * (non-Javadoc)
							 * 
							 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
							 */
							public String isValid(String newText) {
								if (newText.length() == 0)
									return MarkerMessages.MarkerFilterDialog_emptyMessage;
								Iterator filterIterator = filterGroups
										.iterator();
								while (filterIterator.hasNext()) {
									if (((MarkerFieldFilterGroup) filterIterator
											.next()).getName().equals(newText))
										return NLS
												.bind(
														MarkerMessages.filtersDialog_conflictingName,
														newText);
								}

								return null;
							}
						});
				newDialog.open();
				String newName = newDialog.getValue();
				if (newName != null) {
					createNewFilter(newName);
				}
			}
		});
		setButtonLayoutData(addNew);

		Button remove = new Button(buttons, SWT.PUSH);
		remove.setText(MarkerMessages.MarkerFilter_deleteSelectedName);
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeFilters(filtersList.getSelection());
			}
		});
		setButtonLayoutData(remove);
	}

	/**
	 * Create a new filter called newName
	 * 
	 * @param newName
	 */
	protected void createNewFilter(String newName) {
		// TODO Create one

	}

	/**
	 * Return the filter groups modified by the receiver.
	 * 
	 * @return Collection of {@link MarkerFieldFilterGroup}
	 */
	public Collection getFilters() {
		return filterGroups;
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
		if (selectedFilterGroup != null) {

			scopeArea.applyToGroup(selectedFilterGroup);
			Iterator areas = filterAreas.iterator();
			while (areas.hasNext()) {
				((FilterConfigurationArea) areas.next())
						.applyToGroup(selectedFilterGroup);
			}
		}
		super.okPressed();

	}

	/**
	 * Remove the filters in selection.
	 * 
	 * @param selection
	 */
	protected void removeFilters(ISelection selection) {
		// TODO Auto-generated method stub

	}

	/**
	 * Set the filter that is being worked on.
	 * 
	 * @param markerFieldFilterGroup
	 */
	void setSelectedFilter(MarkerFieldFilterGroup markerFieldFilterGroup) {
		MarkerFieldFilterGroup old = selectedFilterGroup;
		selectedFilterGroup = markerFieldFilterGroup;
		if (old != null)
			scopeArea.applyToGroup(old);
		scopeArea.initializeFromGroup(selectedFilterGroup);
		Iterator areas = filterAreas.iterator();
		while (areas.hasNext()) {
			FilterConfigurationArea area = (FilterConfigurationArea) areas
					.next();
			if (old != null)
				area.applyToGroup(old);
			area.initializeFromGroup(selectedFilterGroup);
		}
	}

}
