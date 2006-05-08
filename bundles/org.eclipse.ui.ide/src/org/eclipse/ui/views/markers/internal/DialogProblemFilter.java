/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * DialogProblemFilter is the dialog class for problem filters.
 * 
 * @since 3.2
 * 
 */
public class DialogProblemFilter extends DialogMarkerFilter {

	private DescriptionGroup descriptionGroup;

	private SeverityGroup severityGroup;

	private Composite userFilterComposite;

	private Label systemSettingsLabel;

	private CheckboxTableViewer definedList;

	private class DescriptionGroup {
		private Label descriptionLabel;

		private Combo combo;

		private Text description;

		private String contains = MarkerMessages.filtersDialog_contains;

		private String doesNotContain = MarkerMessages.filtersDialog_doesNotContain;

		/**
		 * Create a descriptor group.
		 * 
		 * @param parent
		 */
		public DescriptionGroup(Composite parent) {

			Composite descriptionComposite = new Composite(parent, SWT.NONE);
			descriptionComposite.setLayout(new GridLayout(2, false));
			descriptionComposite.setLayoutData(new GridData(
					GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

			descriptionLabel = new Label(descriptionComposite, SWT.NONE);
			descriptionLabel.setFont(parent.getFont());
			descriptionLabel
					.setText(MarkerMessages.filtersDialog_descriptionLabel);

			combo = new Combo(descriptionComposite, SWT.READ_ONLY);
			combo.setFont(parent.getFont());
			combo.add(contains);
			combo.add(doesNotContain);
			combo.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					updateForSelection();
				}
			});
			// Prevent Esc and Return from closing the dialog when the combo is
			// active.
			combo.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_ESCAPE
							|| e.detail == SWT.TRAVERSE_RETURN) {
						e.doit = false;
					}
				}
			});

			description = new Text(descriptionComposite, SWT.SINGLE
					| SWT.BORDER);
			description.setFont(parent.getFont());
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;

			description.setLayoutData(data);
			description.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					DialogProblemFilter.this.markDirty();
				}
			});
		}

		/**
		 * Get the contains value.
		 * 
		 * @return boolean
		 */
		public boolean getContains() {
			return combo.getSelectionIndex() == combo.indexOf(contains);
		}

		/**
		 * Return whether or not the contains value is of use.
		 * 
		 * @param value
		 */
		public void setContains(boolean value) {
			if (value) {
				combo.select(combo.indexOf(contains));
			} else {
				combo.select(combo.indexOf(doesNotContain));
			}
		}

		/**
		 * Set the description field.
		 * 
		 * @param text
		 */
		public void setDescription(String text) {
			if (text == null) {
				description.setText(""); //$NON-NLS-1$ 
			} else {
				description.setText(text);
			}
		}

		/**
		 * Return the text for the description.
		 * 
		 * @return String
		 */
		public String getDescription() {
			return description.getText();
		}

		/**
		 * Update the enablement state based on whether or not the receiver is
		 * enabled.
		 * 
		 * @param enabled
		 */
		public void updateEnablement(boolean enabled) {
			descriptionLabel.setEnabled(enabled);
			combo.setEnabled(enabled);
			description.setEnabled(enabled);
		}
	}

	private class SeverityGroup {
		private Button enablementButton;

		private Button errorButton;

		private Button warningButton;

		private Button infoButton;

		/**
		 * Create a group for severity.
		 * 
		 * @param parent
		 */
		public SeverityGroup(Composite parent) {

			Composite severityComposite = new Composite(parent, SWT.NONE);
			severityComposite.setLayout(new GridLayout(4, false));
			severityComposite.setLayoutData(new GridData(
					GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

			SelectionListener listener = new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					updateEnablement(true);
					DialogProblemFilter.this.markDirty();
				}
			};

			enablementButton = new Button(severityComposite, SWT.CHECK);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			enablementButton.setLayoutData(data);
			enablementButton.setFont(parent.getFont());
			enablementButton
					.setText(MarkerMessages.filtersDialog_severityLabel);
			enablementButton.addSelectionListener(listener);

			errorButton = new Button(severityComposite, SWT.CHECK);
			errorButton.setFont(parent.getFont());
			errorButton.setText(MarkerMessages.filtersDialog_severityError);
			errorButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			errorButton.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					updateForSelection();
				}
			});

			warningButton = new Button(severityComposite, SWT.CHECK);
			warningButton.setFont(parent.getFont());
			warningButton.setText(MarkerMessages.filtersDialog_severityWarning);
			warningButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			warningButton.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					updateForSelection();
				}
			});

			infoButton = new Button(severityComposite, SWT.CHECK);
			infoButton.setFont(parent.getFont());
			infoButton.setText(MarkerMessages.filtersDialog_severityInfo);
			infoButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			infoButton.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					updateForSelection();
				}
			});
		}

		/**
		 * Return whether or not sort by severity is selected.
		 * 
		 * @return boolean
		 */
		public boolean isSeveritySelected() {
			return enablementButton.getSelection();
		}

		/**
		 * Set whether or not the enabled button is selected.
		 * 
		 * @param enabled
		 */
		public void setEnabled(boolean enabled) {
			enablementButton.setSelection(enabled);
		}

		/**
		 * Return whether or not the error button is selected.
		 * 
		 * @return boolean
		 */
		public boolean isErrorSelected() {
			return errorButton.getSelection();
		}

		/**
		 * Set whether or not the error button is selected.
		 * 
		 * @param selected
		 */
		public void setErrorSelected(boolean selected) {
			errorButton.setSelection(selected);
		}

		/**
		 * Return whether or not the warning button is selected.
		 * 
		 * @return boolean
		 */
		public boolean isWarningSelected() {
			return warningButton.getSelection();
		}

		/**
		 * Set whether or not the warning button is selected.
		 * 
		 * @param selected
		 */
		public void setWarningSelected(boolean selected) {
			warningButton.setSelection(selected);
		}

		/**
		 * Return whether or not the info button is selected.
		 * 
		 * @return boolean
		 */
		public boolean isInfoSelected() {
			return infoButton.getSelection();
		}

		/**
		 * Set whether or not the erinforor button is selected.
		 * 
		 * @param selected
		 */
		public void setInfoSelected(boolean selected) {
			infoButton.setSelection(selected);
		}

		/**
		 * Update enablement based on the enabled flag.
		 * 
		 * @param enabled
		 */
		public void updateEnablement(boolean enabled) {

			boolean showingSeverity = isSeveritySelected();
			enablementButton.setEnabled(enabled);
			errorButton.setEnabled(showingSeverity && enabled);
			warningButton.setEnabled(showingSeverity && enabled);
			infoButton.setEnabled(showingSeverity && enabled);

		}
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parentShell
	 * @param filters
	 */
	public DialogProblemFilter(Shell parentShell, ProblemFilter[] filters) {
		super(parentShell, filters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#createAttributesArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createAttributesArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		descriptionGroup = new DescriptionGroup(composite);
		severityGroup = new SeverityGroup(composite);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateFilterFromUI(org.eclipse.ui.views.markers.internal.MarkerFilter)
	 */
	protected void updateFilterFromUI(MarkerFilter filter) {
		super.updateFilterFromUI(filter);

		ProblemFilter problemFilter = (ProblemFilter) filter;
		problemFilter.setContains(descriptionGroup.getContains());
		problemFilter.setDescription(descriptionGroup.getDescription().trim());

		problemFilter.setSelectBySeverity(severityGroup.isSeveritySelected());
		int severity = 0;
		if (severityGroup.isErrorSelected()) {
			severity = severity | ProblemFilter.SEVERITY_ERROR;
		}
		if (severityGroup.isWarningSelected()) {
			severity = severity | ProblemFilter.SEVERITY_WARNING;
		}
		if (severityGroup.isInfoSelected()) {
			severity = severity | ProblemFilter.SEVERITY_INFO;
		}
		problemFilter.setSeverity(severity);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateUIWithFilter(org.eclipse.ui.views.markers.internal.MarkerFilter)
	 */
	protected void updateUIWithFilter(MarkerFilter filter) {

		ProblemFilter problemFilter = (ProblemFilter) filter;
		descriptionGroup.setContains(problemFilter.getContains());
		descriptionGroup.setDescription(problemFilter.getDescription());

		severityGroup.setEnabled(problemFilter.getSelectBySeverity());
		int severity = problemFilter.getSeverity();

		severityGroup
				.setErrorSelected((severity & ProblemFilter.SEVERITY_ERROR) > 0);
		severityGroup
				.setWarningSelected((severity & ProblemFilter.SEVERITY_WARNING) > 0);
		severityGroup
				.setInfoSelected((severity & ProblemFilter.SEVERITY_INFO) > 0);

		super.updateUIWithFilter(filter);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#updateEnabledState(boolean)
	 */
	protected void updateEnabledState(boolean enabled) {
		super.updateEnabledState(enabled);
		descriptionGroup.updateEnablement(enabled);
		severityGroup.updateEnablement(enabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markerview.FiltersDialog#resetPressed()
	 */
	protected void resetPressed() {
		descriptionGroup.setContains(ProblemFilter.DEFAULT_CONTAINS);
		descriptionGroup.setDescription(ProblemFilter.DEFAULT_DESCRIPTION);

		severityGroup.setEnabled(ProblemFilter.DEFAULT_SELECT_BY_SEVERITY);
		severityGroup
				.setErrorSelected((ProblemFilter.DEFAULT_SEVERITY & ProblemFilter.SEVERITY_ERROR) > 0);
		severityGroup
				.setWarningSelected((ProblemFilter.DEFAULT_SEVERITY & ProblemFilter.SEVERITY_WARNING) > 0);
		severityGroup
				.setInfoSelected((ProblemFilter.DEFAULT_SEVERITY & ProblemFilter.SEVERITY_INFO) > 0);

		super.resetPressed();
	}

	protected MarkerFilter newFilter(String newName) {
		return new ProblemFilter(newName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#createFiltersArea(org.eclipse.swt.widgets.Composite)
	 */
	void createFiltersArea(Composite dialogArea) {

		if (MarkerSupportRegistry.getInstance().getRegisteredFilters().size() == 0) {
			super.createFiltersArea(dialogArea);
			return;
		}

		Composite mainComposite = new Composite(dialogArea, SWT.NONE);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				true));

		mainComposite.setLayout(new FormLayout());

		Composite topComposite = new Composite(mainComposite, SWT.NONE);
		FormData topData = new FormData();
		topData.top = new FormAttachment(0);
		topData.left = new FormAttachment(0);
		topData.right = new FormAttachment(100);
		topData.bottom = new FormAttachment(50);

		topComposite.setLayoutData(topData);
		topComposite.setLayout(new GridLayout());

		createUserFiltersArea(topComposite);

		Composite bottomComposite = new Composite(mainComposite, SWT.NONE);
		FormData bottomData = new FormData();
		bottomData.top = new FormAttachment(50);
		bottomData.left = new FormAttachment(0);
		bottomData.right = new FormAttachment(100);
		bottomData.bottom = new FormAttachment(100);

		bottomComposite.setLayoutData(bottomData);
		bottomComposite.setLayout(new GridLayout());

		createRegisteredFilters(bottomComposite);
		createFilterSelectButtons(bottomComposite);

	}

	/**
	 * Create a composite for the registered filters.
	 * 
	 * @param bottomComposite
	 */
	private void createRegisteredFilters(Composite bottomComposite) {

		Composite listArea = new Composite(bottomComposite, SWT.NONE);
		listArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		listArea.setLayout(new GridLayout());

		Label title = new Label(listArea, SWT.NONE);
		title.setText(MarkerMessages.ProblemFilterDialog_System_Filters_Title);
		definedList = CheckboxTableViewer.newCheckList(listArea, SWT.BORDER);
		definedList.setContentProvider(new IStructuredContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return MarkerSupportRegistry.getInstance()
						.getRegisteredFilters().toArray();
			}

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
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// Do nothing
			}
		});

		definedList.setLabelProvider(new LabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((MarkerFilter) element).getName();
			}
		});

		definedList
				.addSelectionChangedListener(new ISelectionChangedListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
					 */
					public void selectionChanged(SelectionChangedEvent event) {

						ISelection selection = event.getSelection();
						if (selection instanceof IStructuredSelection) {
							Object selected = ((IStructuredSelection) selection)
									.getFirstElement();
							if (selected == null) {
								systemSettingsLabel.setText(Util.EMPTY_STRING);
							} else {
								systemSettingsLabel
										.setText(getSystemFilterString((ProblemFilter) selected));
							}
						} else {
							systemSettingsLabel.setText(Util.EMPTY_STRING);
						}
						showSystemLabel(true);

					}
				});

		Iterator definedFilters = MarkerSupportRegistry.getInstance()
				.getRegisteredFilters().iterator();
		definedList.setInput(this);
		while (definedFilters.hasNext()) {
			MarkerFilter next = (MarkerFilter) definedFilters.next();
			definedList.setChecked(next, next.isEnabled());
		}

		definedList.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

	}

	/**
	 * Return the string with the details of filter.
	 * 
	 * @param filter
	 * @return String
	 */
	protected String getSystemFilterString(ProblemFilter filter) {
		StringBuffer filterBuffer = new StringBuffer();

		String scopeString = getScopeString(filter);
		if (scopeString != null) {
			filterBuffer.append(scopeString);
		}

		String descriptionString = getDescriptionString(filter);
		if (descriptionString != null) {
			filterBuffer.append(Util.TWO_LINE_FEED);
			filterBuffer.append(descriptionString);
		}

		String severityString = getSeverityString(filter);
		if (severityString != null) {
			filterBuffer.append(Util.TWO_LINE_FEED);
			filterBuffer.append(severityString);
		}

		String typesString = getProblemTypesString(filter);
		filterBuffer.append(Util.TWO_LINE_FEED);
		filterBuffer.append(typesString);

		return filterBuffer.toString();
	}

	/**
	 * Get the problem types String for filter.
	 * 
	 * @param filter
	 * @return String
	 */
	private String getProblemTypesString(ProblemFilter filter) {
		List types = filter.getSelectedTypes();
		if (types.size() == getRootEntries(filter).length) {
			return MarkerMessages.ProblemFilterDialog_All_Problems;
		}
		StringBuffer typesBuffer = new StringBuffer();
		Iterator typesIterator = types.iterator();
		typesBuffer.append(MarkerMessages.ProblemFilterDialog_Selected_Types);

		while (typesIterator.hasNext()) {
			typesBuffer.append(Util.LINE_FEED_AND_TAB);
			typesBuffer.append(((MarkerType) typesIterator.next()).getLabel());

		}
		return typesBuffer.toString();
	}

	/**
	 * Return the string for severity if there is one. Otherwise return
	 * <code>null</code>.
	 * 
	 * @param filter
	 * @return String
	 */
	private String getSeverityString(ProblemFilter filter) {
		if (filter.getSelectBySeverity()) {
			switch (filter.getSeverity()) {
			case ProblemFilter.SEVERITY_INFO:
				return MarkerMessages.ProblemFilterDialog_Info_Severity;
			case ProblemFilter.SEVERITY_WARNING:
				return MarkerMessages.ProblemFilterDialog_Warning_Severity;
			case ProblemFilter.SEVERITY_ERROR:
				return MarkerMessages.ProblemFilterDialog_Error_Severity;
			default:
				return null;
			}
		}
		return null;
	}

	/**
	 * Return the string for the description if there is one. If not return
	 * <code>null</code>.
	 * 
	 * @param filter
	 * @return String or <code>null</code>.
	 */
	private String getDescriptionString(ProblemFilter filter) {
		if (filter.getDescription().length() == 0) {
			return null;
		}
		if (filter.getContains()) {
			return NLS.bind(
					MarkerMessages.ProblemFilterDialog_Contains_Description,
					filter.getDescription());
		}
		return NLS
				.bind(
						MarkerMessages.ProblemFilterDialog_Does_Not_Contain_Description,
						filter.getDescription());

	}

	/**
	 * Return the string that describes the scope.
	 * 
	 * @param filter
	 * @return String or <code>null</code> if the severity does not match.
	 */
	private String getScopeString(ProblemFilter filter) {

		switch (filter.onResource) {
		case MarkerFilter.ON_ANY:
			return MarkerMessages.ProblemFilterDialog_any;
		case MarkerFilter.ON_ANY_IN_SAME_CONTAINER:
			return MarkerMessages.ProblemFilterDialog_sameContainer;
		case MarkerFilter.ON_SELECTED_AND_CHILDREN:
			return MarkerMessages.ProblemFilterDialog_selectedAndChildren;
		case MarkerFilter.ON_SELECTED_ONLY:
			return MarkerMessages.ProblemFilterDialog_selected;
		case MarkerFilter.ON_WORKING_SET:
			return NLS.bind(MarkerMessages.ProblemFilterDialog_workingSet,
					filter.getWorkingSet());

		default:
			return null;

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#setSelectedFilter(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	protected void setSelectedFilter(SelectionChangedEvent event) {
		showSystemLabel(false);
		super.setSelectedFilter(event);
	}

	/**
	 * Show or hide the system label.
	 * 
	 * @param systemLabelShowing
	 */
	protected void showSystemLabel(boolean systemLabelShowing) {

		systemSettingsLabel.setVisible(systemLabelShowing);
		userFilterComposite.setVisible(!systemLabelShowing);
		userFilterComposite.getParent().layout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#createSelectedFilterArea(org.eclipse.swt.widgets.Composite)
	 */
	Composite createSelectedFilterArea(Composite composite) {

		Composite wrapper = new Composite(composite, SWT.NONE);
		FormLayout wrapperLayout = new FormLayout();
		wrapperLayout.marginHeight = 0;
		wrapperLayout.marginWidth = 0;
		wrapper.setLayout(wrapperLayout);

		systemSettingsLabel = createSystemSettingsLabel(wrapper);
		systemSettingsLabel.setVisible(false);

		FormData systemData = new FormData();
		systemData.top = new FormAttachment(0, IDialogConstants.VERTICAL_MARGIN);
		systemData.left = new FormAttachment(0,
				IDialogConstants.HORIZONTAL_MARGIN);
		systemData.right = new FormAttachment(100, -1
				* IDialogConstants.HORIZONTAL_MARGIN);
		systemData.bottom = new FormAttachment(100, -1
				* IDialogConstants.VERTICAL_MARGIN);

		systemSettingsLabel.setLayoutData(systemData);

		userFilterComposite = super.createSelectedFilterArea(wrapper);

		FormData userData = new FormData();
		userData.top = new FormAttachment(0);
		userData.left = new FormAttachment(0);
		userData.right = new FormAttachment(100);
		userData.bottom = new FormAttachment(100);

		userFilterComposite.setLayoutData(userData);

		return wrapper;
	}

	/**
	 * Create the label for system filters.
	 * 
	 * @param wrapper
	 * @return Label
	 */
	private Label createSystemSettingsLabel(Composite wrapper) {

		return new Label(wrapper, SWT.NONE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (definedList != null) {
			if (buttonId == SELECT_ALL_FILTERS_ID) {
				definedList.setAllChecked(true);
			} else if (buttonId == DESELECT_ALL_FILTERS_ID) {
				definedList.setAllChecked(false);
			}
		}

		super.buttonPressed(buttonId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter#okPressed()
	 */
	protected void okPressed() {

		Iterator registered = MarkerSupportRegistry.getInstance()
				.getRegisteredFilters().iterator();
		while (registered.hasNext()) {
			ProblemFilter next = (ProblemFilter) registered.next();
			next.setEnabled(definedList.getChecked(next));

		}
		super.okPressed();
	}
}
