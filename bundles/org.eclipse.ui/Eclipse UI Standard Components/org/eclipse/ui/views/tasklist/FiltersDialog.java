package org.eclipse.ui.views.tasklist;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
  Cagatay Kavukcuoglu <cagatayk@acm.org> - Filter for markers in same project
**********************************************************************/

import java.text.Collator;
import java.util.*;

import org.eclipse.core.resources.IMarker;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchPlugin;

/* package */ class FiltersDialog extends Dialog {
	/**
	 * ID for the Reset button
	 */
	static final int RESET_ID = IDialogConstants.CLIENT_ID;
	static final int SELECT_ID = IDialogConstants.CLIENT_ID + 1;

	private static class EnumValue {
		private int value;
		private String text;
		private Image image;
		
		EnumValue(int value, String text, Image image) {
			this.value = value;
			this.text = text;
			this.image = image;
		}
		int getValue() {
			return value;
		}
		String getText() {
			return text;
		}
		Image getImage() {
			return image;
		}
	}
	
	private static class EnumType {
		private EnumValue[] values;
		
		EnumType(EnumValue[] values) {
			this.values = values;
		}
		EnumValue[] getValues() {
			return values;
		};
	}

	private EnumType severityType;
	private EnumType priorityType;
	private EnumType completionType;
	
	private class CheckboxEnumGroup {
		private EnumType type;
		private Button enableButton;
		private Button[] valueButtons;

		CheckboxEnumGroup(Composite parent, String text, EnumType type) {
			this.type = type;
			// although not needed for layout, this composite is needed to get the tab order right
			Composite enableComposite = new Composite(parent, SWT.NONE);
			enableComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			enableComposite.setLayout(new FillLayout());
			enableButton = new Button(enableComposite, SWT.CHECK);
			enableButton.addSelectionListener(selectionListener);
			enableButton.setText(text);
			Composite valueComposite = new Composite(parent, SWT.NONE);
			valueComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			valueComposite.setLayout(new FillLayout());
			EnumValue[] values = type.getValues();
			valueButtons = new Button[values.length];
			for (int i = 0; i < values.length; ++i) {
				Button valueButton = new Button(valueComposite, SWT.CHECK);
				valueButton.setText(values[i].getText());
//				valueButton.setImage(values[i].getImage());
				valueButtons[i] = valueButton;
			}
		}

		boolean getEnabled() {
			return enableButton.getEnabled();
		}
	
		void setEnabled(boolean enabled) {
			enableButton.setEnabled(enabled);
			updateEnabledState();
		}

		boolean getSelection() {
			return enableButton.getSelection();
		}

		void setSelection(boolean selected) {
			enableButton.setSelection(selected);
			updateEnabledState();
		}
			
		void updateEnabledState() {
			boolean enabled = enableButton.isEnabled() && enableButton.getSelection();
			for (int i = 0; i < valueButtons.length; ++i) {
				valueButtons[i].setEnabled(enabled);
			}
		}

		int getValueMask() {
			int mask = 0;
			EnumValue[] values = type.getValues();
			for (int i = 0; i < valueButtons.length; ++i) {
				if (valueButtons[i].getSelection()) {
					mask |= (1 << values[i].getValue());
				}
			}
			return mask;
		}

		void setValueMask(int mask) {
			EnumValue[] values = type.getValues();
			for (int i = 0; i < values.length; ++i) {
				valueButtons[i].setSelection((mask & (1 << values[i].getValue())) != 0);
			}
		}
	}


	private class LabelComboTextGroup {
		Label label;
		Combo combo;
		Text text;

		LabelComboTextGroup(Composite parent, String labelText, String[] comboStrings, String initialText, int widthHint) {
			Composite group = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			//Set the margin width to 0 in order to line up with other items
			layout.marginWidth = 0;
			group.setLayout(layout);
			label = new Label(group, SWT.NONE);
			label.setText(labelText);
			combo = createCombo(group, comboStrings, 0);
			text = new Text(parent, SWT.SINGLE | SWT.BORDER);
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.widthHint = widthHint;
			text.setLayoutData(gridData);
			text.setText(initialText);
		}
	}

	/**
	 * Creates and manages a group of widgets for selecting a working 
	 * set task filter.
	 */
	private class WorkingSetGroup {
		private Button button;
		private Button selectButton;
		/**
		 * Creates the working set filter selection widgets.
		 * 
		 * @param parent the parent composite of the working set widgets
		 */
		WorkingSetGroup(Composite parent) {
			// radio button has to be part of main radio button group
			button = createRadioButton(parent, TaskListMessages.getString("TaskList.noWorkingSet")); //$NON-NLS-1$
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			button.setLayoutData(data);

			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			Button radio = new Button(parent, SWT.RADIO);
			layout.marginWidth = radio.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			layout.marginHeight = 0;
			radio.dispose();
			composite.setLayout(layout);
			selectButton = createButton(composite, SELECT_ID, TaskListMessages.getString("TaskList.workingSetSelect"), false); //$NON-NLS-1$
		}
		/**
		 * Returns wether or not a working set filter should be used
		 * 
		 * @return 
		 * 	true=a working set filter should be used
		 * 	false=a working set filter should not be used
		 */
		boolean getSelection() {
			return button.getSelection();
		}
		/**
		 * Returns the selected working set filter or null if none 
		 * is selected.
		 * 
		 * @return the selected working set filter or null if none 
		 * 	is selected.
		 */
		IWorkingSet getWorkingSet() {
			return (IWorkingSet) button.getData();
		}
		/**
		 * Sets the working set filter selection.
		 * 
		 * @param selected true=a working set filter should be used
		 * 	false=no working set filter should be used
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
			IWorkingSetSelectionDialog dialog = WorkbenchPlugin.getDefault().getWorkingSetManager().createWorkingSetSelectionDialog(getShell(), false);
			IWorkingSet workingSet = getWorkingSet();
			
			if (workingSet != null) {
				dialog.setSelection(new IWorkingSet[]{workingSet});
			}
			if (dialog.open() == Window.OK) {
				IWorkingSet[] result = dialog.getSelection();
				if (result != null && result.length > 0) {
					setWorkingSet(result[0]);
				}
				else {
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
		 * @param workingSet the working set 
		 */
		void setWorkingSet(IWorkingSet workingSet) {
			button.setData(workingSet);
			if (workingSet != null) {					
				button.setText(TaskListMessages.format(
					"TaskList.workingSet", 					//$NON-NLS-1$
					new Object[] {workingSet.getName()})); 
			}
			else {
				button.setText(TaskListMessages.getString("TaskList.noWorkingSet")); //$NON-NLS-1$
			}
		}
	}

	private TasksFilter filter;

	private MarkerTypesModel markerTypesModel = new MarkerTypesModel();
	
	private MarkerType[] markerTypes;
	
	private CheckboxTreeViewer typesViewer;
	private Button anyResourceButton;
	private Button anyResourceInSameProjectButton; // added by cagatayk@acm.org
	private Button selectedResourceButton;
	private Button selectedResourceAndChildrenButton;
	private WorkingSetGroup workingSetGroup;
	private LabelComboTextGroup descriptionGroup;
	private CheckboxEnumGroup severityGroup;
	private CheckboxEnumGroup priorityGroup;
	private CheckboxEnumGroup completionGroup;
	private Button filterOnMarkerLimit;
	private Text markerLimit;

	private SelectionListener selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			FiltersDialog.this.widgetSelected(e);
		}
	};

	private ICheckStateListener checkStateListener = new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			FiltersDialog.this.checkStateChanged(event);
		}
	};
/**
 * Creates a new filters dialog.
 */
public FiltersDialog(Shell parentShell) {
	super(parentShell);
	initTypes();
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected void buttonPressed(int buttonId) {
	if (RESET_ID == buttonId) {
		resetPressed();
	}
	else
	if (SELECT_ID == buttonId) {
		workingSetGroup.selectPressed();
	}
	else {
		super.buttonPressed(buttonId);
	}
}
public void checkStateChanged(CheckStateChangedEvent event) {
	MarkerType type = (MarkerType) event.getElement();
	typesViewer.setSubtreeChecked(type, event.getChecked());
	MarkerType[] allSupertypes = type.getAllSupertypes();
	for (int i = 0; i < allSupertypes.length; ++i) {
		typesViewer.setChecked(allSupertypes[i], false);
	}
	updateEnabledState();
}
/* (non-Javadoc)
 * Method declared on Window.
 */
protected void configureShell(Shell newShell) {
	super.configureShell(newShell);
	newShell.setText(TaskListMessages.getString("TaskList.filter")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(newShell, ITaskListHelpContextIds.FILTERS_DIALOG);
}
/**
 * Creates the area showing filtering criteria on attribute values.
 *
 * @param parent the parent composite
 */
void createAttributesArea(Composite parent) {
	Composite composite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	composite.setLayout(layout);

	String[] filters = {TaskListMessages.getString("TaskList.contains"), TaskListMessages.getString("TaskList.doesNotContain")}; //$NON-NLS-2$ //$NON-NLS-1$
	descriptionGroup = new LabelComboTextGroup(composite, TaskListMessages.getString("TaskList.whereDescription"), filters, "", 200);//$NON-NLS-2$ //$NON-NLS-1$
	severityGroup = new CheckboxEnumGroup(composite, TaskListMessages.getString("TaskList.severity.label"), severityType); //$NON-NLS-1$
	priorityGroup = new CheckboxEnumGroup(composite, TaskListMessages.getString("TaskList.priority.label"), priorityType); //$NON-NLS-1$
	completionGroup = new CheckboxEnumGroup(composite, TaskListMessages.getString("TaskList.status.label"), completionType); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected void createButtonsForButtonBar(Composite parent) {
	super.createButtonsForButtonBar(parent);
	createButton(parent, RESET_ID, TaskListMessages.getString("TaskList.resetText"), false); //$NON-NLS-1$
}
/**
 * Creates a check box button with the given parent and text.
 *
 * @param parent the parent composite
 * @param text the text for the check box
 * @param grabRow <code>true</code>to grab the remaining horizontal space, <code>false</code> otherwise
 * @return the check box button
 */
Button createCheckbox(Composite parent, String text, boolean grabRow) {
	Button button = new Button(parent, SWT.CHECK);
	if (grabRow) {
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(gridData);
	}
	button.setText(text);
	button.addSelectionListener(selectionListener);
	return button;
}
/**
 * Creates a combo box with the given parent, items, and selection
 *
 * @param parent the parent composite
 * @param items the items for the combo box
 * @param selectionIndex the index of the item to select
 * @return the combo box
 */
Combo createCombo(Composite parent, String[] items, int selectionIndex) {
	Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
	combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	combo.setItems(items);
	combo.select(selectionIndex);
	combo.addSelectionListener(selectionListener);
	return combo;
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	Composite composite = (Composite) super.createDialogArea(parent);
	createMarkerLimitArea(composite);
	createTypesArea(composite);
	createResourceArea(composite);
	createAttributesArea(composite);	
	
	updateUIFromFilter(getFilter());
	
	return composite;
}
/**
 * Creates a radio button with the given parent and text.
 *
 * @param parent the parent composite
 * @param text the text for the check box
 * @return the radio box button
 */
Button createRadioButton(Composite parent, String text) {
	Button button = new Button(parent, SWT.RADIO);
	button.setText(text);
	button.addSelectionListener(selectionListener);
	return button;
}
/**
 * Creates the area showing which resources should be considered.
 *
 * @param parent the parent composite
 */
void createResourceArea(Composite parent) {
	Composite group = new Composite(parent, SWT.NONE);
	group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	group.setLayout(new GridLayout());
	anyResourceButton = createRadioButton(group, TaskListMessages.getString("TaskList.anyResource")); //$NON-NLS-1$
	anyResourceInSameProjectButton = createRadioButton(group, TaskListMessages.getString("TaskList.anyResourceInSameProject")); //$NON-NLS-1$ // added by cagatayk@acm.org
	selectedResourceButton = createRadioButton(group, TaskListMessages.getString("TaskList.selectedResource")); //$NON-NLS-1$
	selectedResourceAndChildrenButton = createRadioButton(group, TaskListMessages.getString("TaskList.selectedAndChildren")); //$NON-NLS-1$
	workingSetGroup = new WorkingSetGroup(group);
}
/**
 * Creates the area showing which marker types should be included.
 *
 * @param parent the parent composite
 */
void createTypesArea(Composite parent) {
	Composite composite = new Composite(parent, SWT.NONE);
	composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	GridLayout layout = new GridLayout();
	composite.setLayout(layout);

	Label label = new Label(composite, SWT.NONE);
	label.setText(TaskListMessages.getString("TaskList.showItemsOfType")); //$NON-NLS-1$

	typesViewer = new CheckboxTreeViewer(composite);
	GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.heightHint = 100;
	typesViewer.getControl().setLayoutData(gridData);
	typesViewer.setContentProvider(getContentProvider());
	typesViewer.setLabelProvider(getLabelProvider());
	typesViewer.setSorter(getSorter());
	typesViewer.setAutoExpandLevel(CheckboxTreeViewer.ALL_LEVELS);
	typesViewer.addCheckStateListener(checkStateListener);
	typesViewer.setInput(getMarkerTypes());
}
ITreeContentProvider getContentProvider() {
	return new ITreeContentProvider() {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		public void dispose() {}
		public Object[] getElements(Object inputElement) {
			return new Object[] {
				markerTypesModel.getType(IMarker.PROBLEM),
				markerTypesModel.getType(IMarker.TASK)
			};
		}
		public Object[] getChildren(Object parentElement) {
			MarkerType type = (MarkerType) parentElement;
			return type.getSubtypes();
		}
		public Object getParent(Object element) {
			return null;
		}
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	};
}

void createMarkerLimitArea(Composite parent) {
	Composite composite = new Composite(parent, SWT.NONE);
	composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	composite.setLayout(layout);
	filterOnMarkerLimit = createCheckbox(composite, TaskListMessages.getString(
			"TaskList.limitVisibleTasksTo"), false); //$NON-NLS-1$
	filterOnMarkerLimit.setLayoutData(new GridData());
	markerLimit = new Text(composite, SWT.SINGLE | SWT.BORDER);
	markerLimit.setTextLimit(6);
	GridData gridData = new GridData();
	gridData.widthHint = convertWidthInCharsToPixels(10);
	markerLimit.setLayoutData(gridData);
}	

/**
 * Returns the filter which this dialog is configuring.
 *
 * @return the filter
 */
public TasksFilter getFilter() {
	if (filter == null)
		filter = new TasksFilter();
	return filter;
}
ILabelProvider getLabelProvider() {
	return new LabelProvider() {
		public String getText(Object element) {
			MarkerType type = (MarkerType) element;
			return type.getLabel();
		}
	};
}
/**
 * Returns the marker types to display.
 *
 * @return the marker types to display
 */
MarkerType[] getMarkerTypes() {
	if (markerTypes == null) {
		ArrayList typesList = new ArrayList();
		MarkerType[] types = markerTypesModel.getTypes();
		for (int i = 0; i < types.length; ++i) {
			MarkerType type = types[i];
			if (type.getLabel().length() > 0) {
				if (type.isSubtypeOf(markerTypesModel.getType(IMarker.PROBLEM)) 
				 || type.isSubtypeOf(markerTypesModel.getType(IMarker.TASK))) {
					typesList.add(type);
				}
			}
		}
		Collections.sort(typesList, new Comparator() {
			Collator collator = Collator.getInstance();
			public int compare(Object o1, Object o2) {
				return collator.compare(((MarkerType) o1).getLabel(), ((MarkerType) o2).getLabel());
			}
		});
		markerTypes = new MarkerType[typesList.size()];
		typesList.toArray(markerTypes);
	}
	return markerTypes;
}
/**
 * Returns the ids of the selected marker types.
 *
 * @return the ids of the selected marker types
 */
String[] getSelectedTypes() {
	Object[] checked = typesViewer.getCheckedElements();
	ArrayList list = new ArrayList();
	for (int i = 0; i < checked.length; ++i) {
		MarkerType type = (MarkerType) checked[i];
		// Skip it if any supertypes have already been included.
		// Relies on getCheckedElements() using a pre-order traversal 
		// so parents are earlier in the list.
		boolean found = false;
		for (int j = list.size(); --j >= 0;) {
			if (type.isSubtypeOf((MarkerType) list.get(j))) {
				found = true;
				break;
			}
		}
		if (!found) {
			list.add(type);
		}
	}
	String[] types = new String[list.size()];
	for (int i = 0; i < list.size(); ++i) {
		types[i] = ((MarkerType) list.get(i)).getId();
	}
	return types;
}
ViewerSorter getSorter() {
	return new ViewerSorter() {
		public int compare(Viewer viewer, Object e1, Object e2) {
			MarkerType t1 = (MarkerType) e1;
			MarkerType t2 = (MarkerType) e2;
			return collator.compare(t1.getLabel(), t2.getLabel());
		}
	};
}
/**
 * Returns the id of the marker type at the given index
 *
 * @param typeIndex the index of the marker type in the UI list
 * @return the id of the marker type at the given index
 */
String getTypeId(int typeIndex) {
	return getMarkerTypes()[typeIndex].getId();
}
/**
 * Returns the index of the given marker type
 *
 * @param markerType the marker type id
 * @return the index of the marker type
 */
int getTypeIndex(String markerType) {
	MarkerType[] types = getMarkerTypes();
	String[] labels = new String[types.length];
	for (int i = 0; i < types.length; ++i) {
		String id = types[i].getId();
		if (id == null ? markerType == null : id.equals(markerType))
			return i;
	}
	return -1;
}
void initTypes() {
	severityType = new EnumType(
		new EnumValue[] {
			new EnumValue(IMarker.SEVERITY_ERROR, TaskListMessages.getString("TaskList.severity.error"), MarkerUtil.getImage("error")),//$NON-NLS-2$ //$NON-NLS-1$
			new EnumValue(IMarker.SEVERITY_WARNING, TaskListMessages.getString("TaskList.severity.warning"), MarkerUtil.getImage("warn")),//$NON-NLS-2$ //$NON-NLS-1$
			new EnumValue(IMarker.SEVERITY_INFO, TaskListMessages.getString("TaskList.severity.info"), MarkerUtil.getImage("info"))//$NON-NLS-2$ //$NON-NLS-1$
		}
	);
	
	priorityType = new EnumType(
		new EnumValue[] {
			new EnumValue(IMarker.PRIORITY_HIGH, TaskListMessages.getString("TaskList.priority.high"), MarkerUtil.getImage("hprio")),//$NON-NLS-2$ //$NON-NLS-1$
			new EnumValue(IMarker.PRIORITY_NORMAL, TaskListMessages.getString("TaskList.priority.normal"), null), //$NON-NLS-1$
			new EnumValue(IMarker.PRIORITY_LOW, TaskListMessages.getString("TaskList.priority.low"), MarkerUtil.getImage("lprio"))//$NON-NLS-2$ //$NON-NLS-1$
		}
	);

	completionType = new EnumType(
		new EnumValue[] {
			new EnumValue(1, TaskListMessages.getString("TaskList.status.completed"), null), //$NON-NLS-1$
			new EnumValue(0, TaskListMessages.getString("TaskList.status.notCompleted"), null) //$NON-NLS-1$
		}
	);
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
/**
 * Updates the filter from the UI state.
 * Must be done here rather than by extending open()
 * because after super.open() is called, the widgetry is disposed.
 */
protected void okPressed() {
	try {
		int markerLimit = Integer.parseInt(this.markerLimit.getText());

		if (markerLimit < 1) {
			throw new NumberFormatException();
		}			
		
		updateFilterFromUI(getFilter());
		super.okPressed();
	}
	catch (NumberFormatException eNumberFormat) {
		MessageBox messageBox = new MessageBox(getShell(), 
				SWT.OK | SWT.APPLICATION_MODAL | SWT.ICON_ERROR);
		messageBox.setText(TaskListMessages.getString(
				"TaskList.titleMarkerLimitInvalid")); //$NON-NLS-1$
		messageBox.setMessage(TaskListMessages.getString(
				"TaskList.messageMarkerLimitInvalid")); //$NON-NLS-1$
		messageBox.open();

		if (markerLimit.forceFocus()) {
			markerLimit.setSelection(0, markerLimit.getCharCount());
			markerLimit.showSelection();
		}
	}
}
/**
 * Handles a press of the Reset button.
 * Updates the UI state to correspond to a reset filter,
 * but doesn't actually reset our filter.
 */
void resetPressed() {
	updateUIFromFilter(new TasksFilter());
}
/**
 * Returns whether any of the selected types are a subtype of the given type.
 */
boolean selectionIncludesSubtypeOf(String type) {
	MarkerType superType = markerTypesModel.getType(type);
	if (superType == null) {
		return false;
	}
	Object[] checked = typesViewer.getCheckedElements();
	for (int i = 0; i < checked.length; ++i) {
		if (((MarkerType) checked[i]).isSubtypeOf(superType)) {
			return true;
		}
	}
	return false;
}
/**
 * Sets the filter which this dialog is to configure.
 *
 * @param filter the filter
 */
public void setFilter(TasksFilter filter) {
	this.filter = filter;
}
/**
 * Sets the selected marker types.
 *
 * @param typeIds the ids of the marker types to select
 */
void setSelectedTypes(String[] typeIds) {
	typesViewer.setCheckedElements(new MarkerType[0]);
	for (int i = 0; i < typeIds.length; ++i) {
		MarkerType type = markerTypesModel.getType(typeIds[i]);
		if (type != null) {
			typesViewer.setSubtreeChecked(type, true);
		}
	}
}
/**
 * Updates the enabled state of the widgetry.
 */
void updateEnabledState() {
	markerLimit.setEnabled(filterOnMarkerLimit.getSelection());
	boolean isProblemSelected = selectionIncludesSubtypeOf(IMarker.PROBLEM);
	boolean isTaskSelected = selectionIncludesSubtypeOf(IMarker.TASK);
	severityGroup.setEnabled(isProblemSelected);
	priorityGroup.setEnabled(isTaskSelected);
	completionGroup.setEnabled(isTaskSelected);
}
/**
 * Updates the given filter from the UI state.
 *
 * @param filter the filter to update
 */
void updateFilterFromUI(TasksFilter filter) {

	filter.types = getSelectedTypes();
	
	if (selectedResourceButton.getSelection())
		filter.onResource = TasksFilter.ON_SELECTED_RESOURCE_ONLY;
	else if (selectedResourceAndChildrenButton.getSelection())
		filter.onResource = TasksFilter.ON_SELECTED_RESOURCE_AND_CHILDREN;
	else if (anyResourceInSameProjectButton.getSelection()) // added by cagatayk@acm.org
		filter.onResource = TasksFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT;
	else if (workingSetGroup.getSelection())
		filter.onResource = TasksFilter.ON_WORKING_SET;
	else
		filter.onResource = TasksFilter.ON_ANY_RESOURCE;

	filter.workingSet = workingSetGroup.getWorkingSet();
	filter.descriptionFilterKind = descriptionGroup.combo.getSelectionIndex();
	filter.descriptionFilter = descriptionGroup.text.getText();
	filter.filterOnDescription = !filter.descriptionFilter.equals("");//$NON-NLS-1$
	
	filter.filterOnSeverity = severityGroup.getSelection();
	filter.severityFilter = severityGroup.getValueMask();
	
	filter.filterOnPriority = priorityGroup.getSelection();
	filter.priorityFilter = priorityGroup.getValueMask();
	
	filter.filterOnCompletion = completionGroup.getSelection();
	filter.completionFilter = completionGroup.getValueMask();

	int markerLimit = TasksFilter.DEFAULT_MARKER_LIMIT;
	
	try {
		markerLimit = Integer.parseInt(this.markerLimit.getText());
	}
	catch (NumberFormatException eNumberFormat) {
	}

	filter.setMarkerLimit(markerLimit);	
	filter.setFilterOnMarkerLimit(filterOnMarkerLimit.getSelection());
}
/**
 * Updates the UI state from the given filter.
 *
 * @param filter the filter to use
 */
void updateUIFromFilter(TasksFilter filter) {
	
	setSelectedTypes(filter.types);

	int on = filter.onResource;
	anyResourceButton.setSelection(on == TasksFilter.ON_ANY_RESOURCE);
	anyResourceInSameProjectButton.setSelection(on == TasksFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT); // added by cagatayk@acm.org
	selectedResourceButton.setSelection(on == TasksFilter.ON_SELECTED_RESOURCE_ONLY);
	selectedResourceAndChildrenButton.setSelection(on == TasksFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
	workingSetGroup.setSelection(on == TasksFilter.ON_WORKING_SET);
	workingSetGroup.setWorkingSet(filter.workingSet);
			
	descriptionGroup.combo.select(filter.descriptionFilterKind);
	descriptionGroup.text.setText(filter.descriptionFilter);
	
	severityGroup.setSelection(filter.filterOnSeverity);
	severityGroup.setValueMask(filter.severityFilter);
	
	priorityGroup.setSelection(filter.filterOnPriority);
	priorityGroup.setValueMask(filter.priorityFilter);
	
	completionGroup.setSelection(filter.filterOnCompletion);
	completionGroup.setValueMask(filter.completionFilter);

	markerLimit.setText("" + filter.getMarkerLimit());
	filterOnMarkerLimit.setSelection(filter.getFilterOnMarkerLimit());

	updateEnabledState();
}
/**
 * Handles selection on a check box or combo box.
 */
void widgetSelected(SelectionEvent e) {
	updateEnabledState();
}
}
