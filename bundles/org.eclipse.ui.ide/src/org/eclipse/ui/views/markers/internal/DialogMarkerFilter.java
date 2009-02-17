/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * Dialog which allows user to modify all settings of an
 * org.eclipse.ui.views.MarkerFilter object. Not intended to be subclassed or
 * instantiated by clients.
 */
public abstract class DialogMarkerFilter extends TrayDialog {

	static final int SELECT_ALL_FILTERS_ID = IDialogConstants.CLIENT_ID + 4;

	static final int DESELECT_ALL_FILTERS_ID = IDialogConstants.CLIENT_ID + 5;

	/**
	 * button IDs
	 */
	static final int RESET_ID = IDialogConstants.CLIENT_ID;

	static final int SELECT_WORKING_SET_ID = IDialogConstants.CLIENT_ID + 1;

	static final int SELECT_ALL_ID = IDialogConstants.CLIENT_ID + 2;

	static final int DESELECT_ALL_ID = IDialogConstants.CLIENT_ID + 3;

	private class TypesLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			return ((AbstractNode) element).getName();
		}
	}

	/**
	 * Creates and manages a group of widgets for selecting a working set marker
	 * filter.
	 */
	private class WorkingSetGroup {

		private Button button;

		private Button selectButton;

		/**
		 * Creates the working set filter selection widgets.
		 * 
		 * @param parent
		 *            the parent composite of the working set widgets
		 */
		WorkingSetGroup(Composite parent) {
			// radio button has to be part of main radio button group
			button = createRadioButton(parent,
					MarkerMessages.filtersDialog_noWorkingSet);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			button.setLayoutData(data);

			Composite composite = new Composite(parent, SWT.NONE);
			composite.setFont(parent.getFont());
			GridLayout layout = new GridLayout();
			Button radio = new Button(parent, SWT.RADIO);
			layout.marginWidth = radio.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			layout.marginHeight = 0;
			radio.dispose();
			composite.setLayout(layout);
			selectButton = createButton(composite, SELECT_WORKING_SET_ID,
					MarkerMessages.filtersDialog_workingSetSelect, false);
		}

		/**
		 * Returns wether or not a working set filter should be used
		 * 
		 * @return true=a working set filter should be used false=a working set
		 *         filter should not be used
		 */
		boolean getSelection() {
			return button.getSelection();
		}

		/**
		 * Returns the selected working set filter or null if none is selected.
		 * 
		 * @return the selected working set filter or null if none is selected.
		 */
		IWorkingSet getWorkingSet() {
			return (IWorkingSet) button.getData();
		}

		/**
		 * Sets the working set filter selection.
		 * 
		 * @param selected
		 *            true=a working set filter should be used false=no working
		 *            set filter should be used
		 */
		void setSelection(boolean selected) {
			button.setSelection(selected);
			if (selected) {
				anyResourceButton.setSelection(false);
				anyResourceInSameProjectButton.setSelection(false);
				selectedResourceButton.setSelection(false);
				selectedResourceAndChildrenButton.setSelection(false);
			}
		}

		/**
		 * Opens the working set selection dialog.
		 */
		void selectPressed() {
			IWorkingSetSelectionDialog dialog = PlatformUI.getWorkbench()
					.getWorkingSetManager().createWorkingSetSelectionDialog(
							getShell(), false);
			IWorkingSet workingSet = getWorkingSet();

			if (workingSet != null) {
				dialog.setSelection(new IWorkingSet[] { workingSet });
			}
			if (dialog.open() == Window.OK) {
				markDirty();
				IWorkingSet[] result = dialog.getSelection();
				if (result != null && result.length > 0) {
					setWorkingSet(result[0]);
				} else {
					setWorkingSet(null);
				}
				if (getSelection() == false) {
					setSelection(true);
				}
			}
		}

		/**
		 * Sets the specified working set.
		 * 
		 * @param workingSet
		 *            the working set
		 */
		void setWorkingSet(IWorkingSet workingSet) {
			button.setData(workingSet);
			if (workingSet != null) {
				button.setText(NLS.bind(
						MarkerMessages.filtersDialog_workingSet, workingSet
								.getLabel()));
			} else {
				button.setText(MarkerMessages.filtersDialog_noWorkingSet);
			}
		}

		void setEnabled(boolean enabled) {
			button.setEnabled(enabled);
			selectButton.setEnabled(enabled);
		}
	}

	/**
	 * AbstractNode is the abstract superclass of the node elements for
	 * MarkerTypes.
	 * 
	 */
	private abstract class AbstractNode {

		/**
		 * Get the parent element of the receiver.
		 * 
		 * @return Object
		 */
		public abstract Object getParent();

		/**
		 * Get the name of the receiver.
		 * 
		 * @return String
		 */
		public abstract String getName();

		/**
		 * Return whether or not the receiver has children.
		 * 
		 * @return boolean
		 */
		public abstract boolean hasChildren();

		/**
		 * Get the children of the receiver.
		 * 
		 * @return Object[]
		 */
		public abstract Object[] getChildren();

		/**
		 * Return whether or not this is a category node.
		 * 
		 * @return boolean
		 */
		public abstract boolean isCategory();

	}

	/**
	 * MarkerTypeNode is the wrapper for marker types.
	 * 
	 */
	private class MarkerTypeNode extends AbstractNode {

		MarkerType type;

		MarkerCategory category;

		/**
		 * Create an instance of the receiver wrapping markerType.
		 * 
		 * @param markerType
		 */
		public MarkerTypeNode(MarkerType markerType) {
			type = markerType;
			nodeToTypeMapping.put(markerType.getId(), this);
		}

		/**
		 * Set the category of the receiver.
		 * 
		 * @param category
		 */
		public void setCategory(MarkerCategory category) {
			this.category = category;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter.AbstractNode#getChildren()
		 */
		public Object[] getChildren() {
			return new Object[0];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter.AbstractNode#getParent()
		 */
		public Object getParent() {
			return category;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter.AbstractNode#hasChildren()
		 */
		public boolean hasChildren() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter.AbstractNode#getName()
		 */
		public String getName() {
			return type.getLabel();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter.AbstractNode#isCategory()
		 */
		public boolean isCategory() {
			return false;
		}

		/**
		 * Return the marker type this is wrapping
		 * 
		 * @return Object
		 */
		public Object getMarkerType() {
			return type;
		}
	}

	/**
	 * The MarkerCategory is a data type to represent the categories in the tree
	 * view.
	 * 
	 */
	private class MarkerCategory extends AbstractNode {

		String name;

		Collection types = new ArrayList();

		/**
		 * Create a new instance of the receiver with name categoryName.
		 * 
		 * @param categoryName
		 */
		public MarkerCategory(String categoryName) {
			name = categoryName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter.AbstractNode#getName()
		 */
		public String getName() {
			return name;
		}

		/**
		 * Add markerType to the list of types.
		 * 
		 * @param markerType
		 */
		public void add(MarkerTypeNode markerType) {
			types.add(markerType);
			markerType.setCategory(this);
		}

		/**
		 * Return the marker types contained in the receiver.
		 * 
		 * @return Object[]
		 */
		public Object[] getMarkerTypes() {
			return types.toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter.AbstractNode#getChildren()
		 */
		public Object[] getChildren() {
			return getMarkerTypes();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter.AbstractNode#getParent()
		 */
		public Object getParent() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter.AbstractNode#hasChildren()
		 */
		public boolean hasChildren() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.internal.DialogMarkerFilter.AbstractNode#isCategory()
		 */
		public boolean isCategory() {
			return true;
		}

	}

	private MarkerFilter[] filters;

	private CheckboxTreeViewer typesViewer;

	private Button anyResourceButton;

	private Button anyResourceInSameProjectButton;

	private Button selectedResourceButton;

	private Button selectedResourceAndChildrenButton;

	private Button selectAllButton;

	private Button deselectAllButton;

	private WorkingSetGroup workingSetGroup;

	private boolean dirty = false;

	private CheckboxTableViewer filtersList;

	private MarkerFilter[] selectedFilters;

	private HashMap nodeToTypeMapping = new HashMap();

	private ITreeContentProvider typesContentProvider;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parentShell
	 * @param filtersList
	 */
	DialogMarkerFilter(Shell parentShell, MarkerFilter[] filtersList) {
		super(parentShell);
		setFilters(filtersList);
	}

	/**
	 * Set the filters in the filtersList by copying them.
	 * 
	 * @param initialFilters
	 */
	private void setFilters(MarkerFilter[] initialFilters) {
		MarkerFilter[] newMarkers = new MarkerFilter[initialFilters.length];
		for (int i = 0; i < initialFilters.length; i++) {
			MarkerFilter newFilter;
			try {
				newFilter = initialFilters[i].makeClone();
			} catch (CloneNotSupportedException exception) {
				ErrorDialog.openError(getShell(),
						MarkerMessages.MarkerFilterDialog_errorTitle,
						MarkerMessages.MarkerFilterDialog_failedFilterMessage,
						Util.errorStatus(exception));
				return;
			}

			newMarkers[i] = newFilter;

		}
		filters = newMarkers;

	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {

		switch (buttonId) {
		case RESET_ID:
			resetPressed();
			markDirty();
			break;
		case SELECT_WORKING_SET_ID:
			workingSetGroup.selectPressed();
			break;
		case SELECT_ALL_ID:
			setAllTypesChecked(true);
			break;
		case DESELECT_ALL_ID:
			setAllTypesChecked(false);
			break;
		case SELECT_ALL_FILTERS_ID:
			filtersList.setAllChecked(true);
			break;
		case DESELECT_ALL_FILTERS_ID:
			filtersList.setAllChecked(false);
			break;
		default:
			break;
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Set the check state of all of the items to checked.
	 * @param checked
	 * @since 3.4
	 */
	private void setAllTypesChecked(boolean checked) {
		TreeItem[] items = typesViewer.getTree().getItems();
		for (int i = 0; i < items.length; i++) {
			Object element = items[i].getData();
			typesViewer.setSubtreeChecked(element, checked);
		}
		
	}

	/**
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(MarkerMessages.filtersDialog_title);
	}

	protected void createResetArea(Composite parent) {

		Button reset = new Button(parent, SWT.PUSH);
		reset.setText(MarkerMessages.restoreDefaults_text);
		reset.setData(new Integer(RESET_ID));

		reset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
		});

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point minSize = reset.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		data.horizontalSpan = 2;
		reset.setLayoutData(data);
	}

	/**
	 * Creates a check box button with the given parent and text.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param text
	 *            the text for the check box
	 * @param grabRow
	 *            <code>true</code>to grab the remaining horizontal space,
	 *            <code>false</code> otherwise
	 * @return the check box button
	 */
	protected Button createCheckbox(Composite parent, String text,
			boolean grabRow) {
		Button button = new Button(parent, SWT.CHECK);
		if (grabRow) {
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			button.setLayoutData(gridData);
		}
		button.setText(text);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateForSelection();
			}
		});
		button.setFont(parent.getFont());
		return button;
	}

	/**
	 * Creates a combo box with the given parent, items, and selection
	 * 
	 * @param parent
	 *            the parent composite
	 * @param items
	 *            the items for the combo box
	 * @param selectionIndex
	 *            the index of the item to select
	 * @return the combo box
	 */
	protected Combo createCombo(Composite parent, String[] items,
			int selectionIndex) {
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setFont(parent.getFont());
		combo.setItems(items);
		combo.select(selectionIndex);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateForSelection();
			}
		});
		return combo;
	}

	/**
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		dialogArea.setLayout(new GridLayout(2, false));

		createFiltersArea(dialogArea);

		Composite selectedComposite = createSelectedFilterArea(dialogArea);
		selectedComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		updateUIFromFilter();

		filtersList.setSelection(new StructuredSelection(filters[0]));

		createResetArea(dialogArea);
		createSeparatorLine(dialogArea);

		applyDialogFont(dialogArea);
		return dialogArea;
	}

	/**
	 * Create the list in the receiver.
	 * 
	 * @param dialogArea
	 */
	/**
	 * @param dialogArea
	 */
	void createFiltersArea(Composite dialogArea) {

		Composite listArea = new Composite(dialogArea, SWT.NONE);
		listArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		listArea.setLayout(new GridLayout());

		createUserFiltersArea(listArea);
		createFilterSelectButtons(listArea);
	}

	/**
	 * Create the area for the user to select thier filters.
	 * 
	 * @param listArea
	 */
	void createUserFiltersArea(Composite listArea) {

		Composite userComposite = new Composite(listArea, SWT.NONE);
		userComposite.setLayout(new GridLayout(2, false));
		userComposite
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label title = new Label(userComposite, SWT.NONE);
		title.setText(MarkerMessages.MarkerFilter_filtersTitle);
		GridData titleData = new GridData();
		titleData.horizontalSpan = 2;
		title.setLayoutData(titleData);

		filtersList = CheckboxTableViewer.newCheckList(userComposite,
				SWT.BORDER);
		filtersList.setContentProvider(new IStructuredContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return filters;
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

		filtersList.setLabelProvider(new LabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((MarkerFilter) element).getName();
			}
		});

		selectedFilters = new MarkerFilter[] { filters[0] };
		filtersList.setSelection(new StructuredSelection(selectedFilters));

		filtersList
				.addSelectionChangedListener(new ISelectionChangedListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
					 */
					public void selectionChanged(SelectionChangedEvent event) {
						updateFilterFromUI();
						setSelectedFilter(event);

					}
				});

		filtersList.setInput(this);
		for (int i = 0; i < filters.length; i++) {
			filtersList.setChecked(filters[i], filters[i].isEnabled());
		}

		GridData listData = new GridData(SWT.FILL, SWT.FILL, true, true);
		listData.widthHint = convertHorizontalDLUsToPixels(100);
		filtersList.getControl().setLayoutData(listData);

		Composite buttons = new Composite(userComposite, SWT.NONE);
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
								for (int i = 0; i < filters.length; i++) {
									if (filters[i].getName().equals(newText))
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
	 * Set the selected filter from event.
	 * 
	 * @param event
	 */
	protected void setSelectedFilter(SelectionChangedEvent event) {

		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			Collection list = ((IStructuredSelection) selection).toList();
			MarkerFilter[] selected = new MarkerFilter[list.size()];
			list.toArray(selected);
			selectedFilters = selected;

		} else {
			selectedFilters = new MarkerFilter[0];
		}
		updateUIFromFilter();

	}

	/**
	 * Remove the filters in selection.
	 * 
	 * @param selection
	 */
	protected void removeFilters(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			List toRemove = ((IStructuredSelection) selection).toList();
			MarkerFilter[] newFilters = new MarkerFilter[filters.length
					- toRemove.size()];
			int index = 0;
			for (int i = 0; i < filters.length; i++) {
				if (toRemove.contains(filters[i])) {
					continue;
				}
				newFilters[index] = filters[i];
				index++;
			}

			filters = newFilters;
			filtersList.refresh();
			updateUIFromFilter();
		}
	}

	/**
	 * Create a new filter called newName.
	 * 
	 * @param newName
	 */
	private void createNewFilter(String newName) {
		MarkerFilter[] newFilters = new MarkerFilter[filters.length + 1];
		System.arraycopy(filters, 0, newFilters, 0, filters.length);
		MarkerFilter filter = newFilter(newName);
		newFilters[filters.length] = filter;
		filters = newFilters;
		filtersList.refresh();
		filtersList.setSelection(new StructuredSelection(filter), true);
		filtersList.getControl().setFocus();
	}

	/**
	 * Crate a newFilter called newName
	 * 
	 * @param newName
	 * @return MarkerFilter
	 */
	protected abstract MarkerFilter newFilter(String newName);

	/**
	 * Create the area for the selected filter.
	 * 
	 * @param composite
	 */
	Composite createSelectedFilterArea(Composite composite) {

		Composite selectedComposite = new Composite(composite, SWT.NONE);
		selectedComposite.setLayout(new GridLayout(2, false));

		Composite leftComposite = new Composite(selectedComposite, SWT.NONE);
		leftComposite.setLayout(new GridLayout());
		leftComposite
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createResourceArea(leftComposite);
		createAttributesArea(leftComposite);

		Composite rightComposite = new Composite(selectedComposite, SWT.NONE);
		createTypesArea(rightComposite);
		rightComposite.setLayout(new GridLayout());
		rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));

		return selectedComposite;
	}

	/**
	 * Creates a separator line above the OK/Cancel buttons bar
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createSeparatorLine(Composite parent) {
		// Build the separator line
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);
	}

	/**
	 * Creates a radio button with the given parent and text.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param text
	 *            the text for the check box
	 * @return the radio box button
	 */
	protected Button createRadioButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.RADIO);
		button.setText(text);
		button.setFont(parent.getFont());
		button.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updateForSelection();
			}
		});
		return button;
	}

	/**
	 * Creates the area showing which resources should be considered.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createResourceArea(Composite parent) {
		Composite group = new Composite(parent, SWT.NONE);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout());
		group.setFont(parent.getFont());
		anyResourceButton = createRadioButton(group,
				MarkerMessages.filtersDialog_anyResource);
		anyResourceInSameProjectButton = createRadioButton(group,
				MarkerMessages.filtersDialog_anyResourceInSameProject); // added
		// by
		// cagatayk@acm.org
		selectedResourceButton = createRadioButton(group,
				MarkerMessages.filtersDialog_selectedResource);
		selectedResourceAndChildrenButton = createRadioButton(group,
				MarkerMessages.filtersDialog_selectedAndChildren);
		workingSetGroup = new WorkingSetGroup(group);
	}

	/**
	 * Creates the area showing which marker types should be included.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createTypesArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.NONE);
		label.setText(MarkerMessages.filtersDialog_showItemsOfType);

		Tree tree = new Tree(composite, SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(false);
		TableLayout tableLayout = new TableLayout();
		tree.setLayout(tableLayout);
		tableLayout.addColumnData(new ColumnWeightData(100, true));
		new TreeColumn(tree, SWT.NONE, 0);

		typesViewer = new CheckboxTreeViewer(tree);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = convertVerticalDLUsToPixels(100);
		gridData.heightHint = convertVerticalDLUsToPixels(125);

		typesContentProvider = getTypesContentProvider();
		typesViewer.getControl().setLayoutData(gridData);
		typesViewer.setContentProvider(typesContentProvider);
		typesViewer.setLabelProvider(getLabelProvider());
		typesViewer.setComparator(getComparator());
		typesViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				markDirty();
				Object element = event.getElement();
				boolean checked = event.getChecked();
				setChildrenChecked(element, checked);
				setParentCheckState(element, checked);
			}
		});
		typesViewer.setInput(getSelectedFilter().getRootTypes().toArray());

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);
		selectAllButton = createButton(buttonComposite, SELECT_ALL_ID,
				MarkerMessages.filtersDialog_selectAllTypes, false);
		deselectAllButton = createButton(buttonComposite, DESELECT_ALL_ID,
				MarkerMessages.filtersDialog_deselectAllTypes, false);
	}

	/**
	 * Get the currently selected marker filter if there is only one selection.
	 * 
	 * @return MarkerFilter or <code>null</code>.
	 */
	protected MarkerFilter getSelectedFilter() {

		if (selectedFilters.length == 1) {
			return selectedFilters[0];
		}
		return null;
	}

	/**
	 * Get the content provider for the receiver.
	 * 
	 * @return ITreeContentProvider
	 */
	private ITreeContentProvider getTypesContentProvider() {
		return new ITreeContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				MarkerFilter selected = getSelectedFilter();
				if (selected == null) {
					return new Object[0];
				}

				return getRootEntries(selected);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
			 */
			public Object[] getChildren(Object parentElement) {
				return ((AbstractNode) parentElement).getChildren();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
			 */
			public Object getParent(Object element) {
				return ((AbstractNode) element).getParent();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
			 */
			public boolean hasChildren(Object element) {
				return ((AbstractNode) element).hasChildren();
			}
		};
	}

	/**
	 * This method is intended to be overridden by subclasses of FiltersDialog.
	 * The attributes area will be created just above the Restore Defaults
	 * button.
	 * 
	 * @param parent
	 *            the parent Composite
	 */
	abstract void createAttributesArea(Composite parent);

	private ILabelProvider getLabelProvider() {
		return new TypesLabelProvider();
	}

	/**
	 * Returns the selected marker types.
	 * 
	 * @return List the selected marker types
	 */
	protected List getSelectedTypes() {
		Object[] checkElements = typesViewer.getCheckedElements();
		List selected = new ArrayList();
		for (int i = 0; i < checkElements.length; i++) {
			AbstractNode node = (AbstractNode) checkElements[i];
			if (!node.isCategory()) {
				selected.add(((MarkerTypeNode) node).getMarkerType());
			}

		}
		return selected;
	}

	/**
	 * Return the sorter for the receiver.
	 * 
	 * @return ViewerSorter
	 */
	protected ViewerComparator getComparator() {
		return new ViewerComparator() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getComparator().compare(((AbstractNode) e1).getName(),
						((AbstractNode) e2).getName());
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		/**
		 * Updates the filter from the UI state. Must be done here rather than
		 * by extending open() because after super.open() is called, the
		 * widgetry is disposed.
		 */
		updateFilterFromUI();

		for (int i = 0; i < filters.length; i++) {
			filters[i].setEnabled(filtersList.getChecked(filters[i]));

		}
		super.okPressed();
	}

	/**
	 * Handles a press of the Reset button. Updates the UI state to correspond
	 * to a reset filter, but doesn't actually reset our filter.
	 */
	protected void resetPressed() {
		setAllTypesChecked(true);
		int onResource = MarkerFilter.DEFAULT_ON_RESOURCE;
		anyResourceButton.setSelection(onResource == MarkerFilter.ON_ANY);
		anyResourceInSameProjectButton
				.setSelection(onResource == MarkerFilter.ON_ANY_IN_SAME_CONTAINER);
		selectedResourceButton
				.setSelection(onResource == MarkerFilter.ON_SELECTED_ONLY);
		selectedResourceAndChildrenButton
				.setSelection(onResource == MarkerFilter.ON_SELECTED_AND_CHILDREN);
		workingSetGroup.setSelection(onResource == MarkerFilter.ON_WORKING_SET);
		updateEnabledState(true);
	}

	/**
	 * Sets the selected marker types.
	 * 
	 * @param markerTypes
	 */
	void setSelectedTypes(List markerTypes) {
		typesViewer.setCheckedElements(new Object[0]);
		for (int i = 0; i < markerTypes.size(); i++) {
			Object obj = markerTypes.get(i);
			if (obj instanceof MarkerType) {

				Object mapping = nodeToTypeMapping.get(((MarkerType) obj)
						.getId());
				if (mapping != null) {
					typesViewer.setChecked(mapping, true);
					setParentCheckState(mapping, true);
				}
			}
		}
	}

	/**
	 * Updates the enabled state of the widgetry based on whether or not it is
	 * enabled.
	 */
	protected void updateEnabledState(boolean enabled) {

		typesViewer.getTree().setEnabled(enabled);
		selectAllButton.setEnabled(enabled
				&& typesViewer.getTree().getItemCount() > 0);
		deselectAllButton.setEnabled(enabled
				&& typesViewer.getTree().getItemCount() > 0);

		anyResourceButton.setEnabled(enabled);
		anyResourceInSameProjectButton.setEnabled(enabled);
		selectedResourceButton.setEnabled(enabled);
		selectedResourceAndChildrenButton.setEnabled(enabled);
		workingSetGroup.setEnabled(enabled);
	}

	/**
	 * Updates the given filter from the UI state.
	 */
	protected final void updateFilterFromUI() {

		MarkerFilter filter = getSelectedFilter();

		if (filter == null) {
			updateEnabledState(false);
			return;
		}

		updateFilterFromUI(filter);
	}

	/**
	 * Update the selected filter from the UI.
	 * 
	 * @param filter
	 */
	protected void updateFilterFromUI(MarkerFilter filter) {

		filter.setSelectedTypes(getSelectedTypes());

		if (selectedResourceButton.getSelection()) {
			filter.setOnResource(MarkerFilter.ON_SELECTED_ONLY);
		} else if (selectedResourceAndChildrenButton.getSelection()) {
			filter.setOnResource(MarkerFilter.ON_SELECTED_AND_CHILDREN);
		} else if (anyResourceInSameProjectButton.getSelection()) {
			filter.setOnResource(MarkerFilter.ON_ANY_IN_SAME_CONTAINER);
		} else if (workingSetGroup.getSelection()) {
			filter.setOnResource(MarkerFilter.ON_WORKING_SET);
		} else {
			filter.setOnResource(MarkerFilter.ON_ANY);
		}

		filter.setWorkingSet(workingSetGroup.getWorkingSet());
	}

	/**
	 * Updates the UI state from the given filter.
	 */
	protected final void updateUIFromFilter() {

		MarkerFilter filter = getSelectedFilter();

		if (filter == null) {
			updateEnabledState(false);
			return;
		}

		updateUIWithFilter(filter);
	}

	/**
	 * Update the UI with the contents of filter.
	 * 
	 * @param filter
	 */
	protected void updateUIWithFilter(MarkerFilter filter) {
		setSelectedTypes(filter.getSelectedTypes());

		int on = filter.getOnResource();
		anyResourceButton.setSelection(on == MarkerFilter.ON_ANY);
		anyResourceInSameProjectButton
				.setSelection(on == MarkerFilter.ON_ANY_IN_SAME_CONTAINER);
		selectedResourceButton
				.setSelection(on == MarkerFilter.ON_SELECTED_ONLY);
		selectedResourceAndChildrenButton
				.setSelection(on == MarkerFilter.ON_SELECTED_AND_CHILDREN);
		workingSetGroup.setSelection(on == MarkerFilter.ON_WORKING_SET);
		workingSetGroup.setWorkingSet(filter.getWorkingSet());

		updateEnabledState(true);
	}

	/**
	 * @return <code>true</code> if the dirty flag has been set otherwise
	 *         <code>false</code>.
	 */
	boolean isDirty() {
		return dirty;
	}

	/**
	 * Marks the dialog as dirty.
	 */
	void markDirty() {
		dirty = true;
	}

	/**
	 * Set the marker filter.
	 * 
	 * @param newFilter
	 */
	public void setFilter(MarkerFilter newFilter) {
		setFilters(new MarkerFilter[] { newFilter });
		updateUIFromFilter();
	}

	/**
	 * @return the MarkerFilters associated with the dialog.
	 */
	public MarkerFilter[] getFilters() {
		return filters;
	}

	/**
	 * A selection has occured on one of the checkboxes or combos. Update.
	 * 
	 */
	protected void updateForSelection() {
		updateEnabledState(true);
		markDirty();
	}

	/**
	 * Get all of the marker types avilable for the filter
	 * 
	 * @param selected
	 * @return Object[]
	 */
	Object[] getRootEntries(MarkerFilter selected) {

		List roots = selected.getRootTypes();
		List markerNodes = new ArrayList();
		HashMap categories = new HashMap();
		for (int i = 0; i < roots.size(); i++) {
			Object obj = roots.get(i);
			buildTypeTree(markerNodes, obj, categories);
		}
		return markerNodes.toArray();
	}

	/**
	 * Build the list of types and categories from the supplied object
	 * 
	 * @param elements
	 * @param obj
	 * @param categories
	 */
	private void buildTypeTree(List elements, Object obj, HashMap categories) {
		if (obj instanceof MarkerType) {

			MarkerType markerType = (MarkerType) obj;

			String categoryName = MarkerSupportRegistry.getInstance()
					.getCategory(markerType.getId());

			if (categoryName == null) {
				elements.add(new MarkerTypeNode(markerType));
			} else {
				MarkerCategory category;
				if (categories.containsKey(categoryName)) {
					category = (MarkerCategory) categories.get(categoryName);
				} else {
					category = new MarkerCategory(categoryName);
					categories.put(categoryName, category);
					elements.add(category);
				}
				MarkerTypeNode node = new MarkerTypeNode(markerType);
				category.add(node);
			}

			MarkerType[] subTypes = ((MarkerType) obj).getSubtypes();
			for (int j = 0; j < subTypes.length; j++) {
				buildTypeTree(elements, subTypes[j], categories);
			}
		}
	}

	/**
	 * Grey check the parent if required
	 * 
	 * @param element
	 * @param checked
	 */
	private void setParentCheckState(Object element, boolean checked) {
		Object parent = typesContentProvider.getParent(element);
		if (parent == null) {
			return;
		}
		Object[] children = typesContentProvider.getChildren(parent);
		if (children.length == 0) {
			return;
		}
		if (checked) {// at least one is checked
			for (int i = 0; i < children.length; i++) {
				Object object = children[i];
				if (!typesViewer.getChecked(object)) {
					typesViewer.setGrayChecked(parent, true);
					return;
				}
			}
			// All checked - check the parent
			typesViewer.setChecked(parent, true);
		} else {
			for (int i = 0; i < children.length; i++) {
				Object object = children[i];
				if (typesViewer.getChecked(object)) {
					typesViewer.setGrayChecked(parent, true);
					return;
				}
			}
			// All checked - check the parent
			typesViewer.setChecked(parent, false);
		}

	}

	/**
	 * Set the check state of the children of element to checked.
	 * 
	 * @param element
	 * @param checked
	 */
	private void setChildrenChecked(Object element, boolean checked) {
		Object[] children = typesContentProvider.getChildren(element);
		if (children.length > 0) {
			for (int i = 0; i < children.length; i++) {
				typesViewer.setChecked(children[i], checked);
			}
		}
	}

	/**
	 * Create the buttons for selecting the filters.
	 * 
	 * @param listArea
	 */
	protected void createFilterSelectButtons(Composite listArea) {
		Composite buttons = new Composite(listArea, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(2, false);
		buttonLayout.marginWidth = 0;
		buttons.setLayout(buttonLayout);

		createButton(buttons, SELECT_ALL_FILTERS_ID,
				MarkerMessages.filtersDialog_selectAll, false);
		createButton(buttons, DESELECT_ALL_FILTERS_ID,
				MarkerMessages.filtersDialog_deselectAll, false);
	}
	
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    protected boolean isResizable() {
    	return true;
    }
}
