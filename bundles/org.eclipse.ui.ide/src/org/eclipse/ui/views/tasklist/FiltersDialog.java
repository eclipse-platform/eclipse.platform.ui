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

package org.eclipse.ui.views.tasklist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.ibm.icu.text.Collator;

import org.eclipse.osgi.util.NLS;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.internal.views.tasklist.TaskListMessages;

class FiltersDialog extends TrayDialog {
    /**
     * ID for the Reset button
     */
    static final int RESET_ID = IDialogConstants.CLIENT_ID;

    static final int SELECT_ID = IDialogConstants.CLIENT_ID + 1;

    private static class EnumValue {
        private int value;

        private String text;

        EnumValue(int value, String text) {
            this.value = value;
            this.text = text;
        }

        int getValue() {
            return value;
        }

        String getText() {
            return text;
        }
    }

    private static class EnumType {
        private EnumValue[] values;

        EnumType(EnumValue[] values) {
            this.values = values;
        }

        EnumValue[] getValues() {
            return values;
        }
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
            enableComposite
                    .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            enableComposite.setLayout(new FillLayout());
            enableButton = new Button(enableComposite, SWT.CHECK);
            enableButton.addSelectionListener(selectionListener);
            enableButton.setText(text);
            Composite valueComposite = new Composite(parent, SWT.NONE);
            valueComposite
                    .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            valueComposite.setLayout(new FillLayout());
            EnumValue[] values = type.getValues();
            valueButtons = new Button[values.length];
            for (int i = 0; i < values.length; ++i) {
                Button valueButton = new Button(valueComposite, SWT.CHECK);
                valueButton.setText(values[i].getText());
                valueButtons[i] = valueButton;
            }
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
            boolean enabled = enableButton.isEnabled()
                    && enableButton.getSelection();
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
                valueButtons[i]
                        .setSelection((mask & (1 << values[i].getValue())) != 0);
            }
        }
    }

    private class LabelComboTextGroup {
        Label label;

        Combo combo;

        Text text;

        LabelComboTextGroup(Composite parent, String labelText,
                String[] comboStrings, String initialText, int widthHint) {
            Font font = parent.getFont();
            Composite group = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.numColumns = 3;
            //Set the margin width to 0 in order to line up with other items
            layout.marginWidth = 0;
            group.setLayout(layout);
            group.setFont(font);
            label = new Label(group, SWT.NONE);
            label.setText(labelText);
            label.setFont(font);
            combo = createCombo(group, comboStrings, 0);
            text = new Text(parent, SWT.SINGLE | SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.widthHint = widthHint;
            text.setLayoutData(gridData);
            text.setFont(font);
            text.setText(initialText);
        }
    }

    /**
     * Creates and manages a group of widgets for selecting a working
     * set task filter.
     */
    private class WorkingSetGroup {
        private Button button;

        /**
         * Creates the working set filter selection widgets.
         * 
         * @param parent the parent composite of the working set widgets
         */
        WorkingSetGroup(Composite parent) {
            // radio button has to be part of main radio button group
            button = createRadioButton(parent, TaskListMessages.TaskList_noWorkingSet);
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
            createButton(composite, SELECT_ID, TaskListMessages.TaskList_workingSetSelect, false);
        }

		/**
		 * Returns whether or not a working set filter should be used
		 * 
		 * @return true=a working set filter should be used false=a working set filter should not be
		 *         used
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
            IWorkingSetSelectionDialog dialog = PlatformUI.getWorkbench()
                    .getWorkingSetManager().createWorkingSetSelectionDialog(
                            getShell(), false);
            IWorkingSet workingSet = getWorkingSet();

            if (workingSet != null) {
                dialog.setSelection(new IWorkingSet[] { workingSet });
            }
            if (dialog.open() == Window.OK) {
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
         * @param workingSet the working set
         */
        void setWorkingSet(IWorkingSet workingSet) {
            button.setData(workingSet);
            if (workingSet != null) {
                button.setText(NLS.bind(TaskListMessages.TaskList_workingSet,  workingSet.getLabel()));
            } else {
                button.setText(TaskListMessages.TaskList_noWorkingSet);
            }
        }
    }

    private TasksFilter filter;

    MarkerTypesModel markerTypesModel = new MarkerTypesModel();

    private MarkerType[] markerTypes;

    private CheckboxTreeViewer typesViewer;

    Button anyResourceButton;

    Button anyResourceInSameProjectButton; // added by cagatayk@acm.org

    Button selectedResourceButton;

    Button selectedResourceAndChildrenButton;

    private WorkingSetGroup workingSetGroup;

    private LabelComboTextGroup descriptionGroup;

    private CheckboxEnumGroup severityGroup;

    private CheckboxEnumGroup priorityGroup;

    private CheckboxEnumGroup completionGroup;

    private Button filterOnMarkerLimit;

    private Text markerLimit;

    SelectionListener selectionListener = new SelectionAdapter() {
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
     * 
     * @param parentShell the parent shell
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
        } else if (SELECT_ID == buttonId) {
            workingSetGroup.selectPressed();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    /**
     * Check state change.
     * 
     * @param event the event
     */
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
        newShell.setText(TaskListMessages.TaskList_filter);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
				ITaskListHelpContextIds.FILTERS_DIALOG);
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
        composite.setFont(parent.getFont());

        String[] filters = {
                TaskListMessages.TaskList_contains, TaskListMessages.TaskList_doesNotContain };
        descriptionGroup = new LabelComboTextGroup(composite, TaskListMessages.TaskList_whereDescription, filters, "", 200);//$NON-NLS-1$
        severityGroup = new CheckboxEnumGroup(composite, TaskListMessages.TaskList_severity_label, severityType);
        priorityGroup = new CheckboxEnumGroup(composite, TaskListMessages.TaskList_priority_label, priorityType);
        completionGroup = new CheckboxEnumGroup(composite, TaskListMessages.TaskList_status_label, completionType);
    }

    void createResetArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        Button reset = new Button(composite, SWT.PUSH);
        reset.setText(TaskListMessages.TaskList_resetText);
        reset.setData(new Integer(RESET_ID));

        reset.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                buttonPressed(((Integer) event.widget.getData()).intValue());
            }
        });

        reset.setFont(composite.getFont());
        setButtonLayoutData(reset);
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
        button.setFont(parent.getFont());
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
        combo.setFont(parent.getFont());
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
        createResetArea(composite);
        createSeparatorLine(composite);

        updateUIFromFilter(getFilter());

        return composite;
    }

    /**
     * Creates a separator line above the OK/Cancel buttons bar
     * 
     * @param parent the parent composite
     */
    void createSeparatorLine(Composite parent) {
        // Build the separator line
        Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        separator.setLayoutData(gd);
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
        button.setFont(parent.getFont());
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
        group.setFont(parent.getFont());
        anyResourceButton = createRadioButton(group, TaskListMessages.TaskList_anyResource);
        anyResourceInSameProjectButton = createRadioButton(group,
                TaskListMessages.TaskList_anyResourceInSameProject);// added by cagatayk@acm.org
        selectedResourceButton = createRadioButton(group, TaskListMessages.TaskList_selectedResource);
        selectedResourceAndChildrenButton = createRadioButton(group,
                TaskListMessages.TaskList_selectedAndChildren);
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
        label.setText(TaskListMessages.TaskList_showItemsOfType);

        typesViewer = new CheckboxTreeViewer(composite);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.heightHint = 100;
        typesViewer.getControl().setLayoutData(gridData);
        typesViewer.setContentProvider(getContentProvider());
        typesViewer.setLabelProvider(getLabelProvider());
        typesViewer.setComparator(getViewerComparator());
        typesViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        typesViewer.addCheckStateListener(checkStateListener);
        typesViewer.setInput(getMarkerTypes());
    }

    ITreeContentProvider getContentProvider() {
        return new ITreeContentProvider() {
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }

            public void dispose() {
            }

            public Object[] getElements(Object inputElement) {
                return new Object[] {
                        markerTypesModel.getType(IMarker.PROBLEM),
                        markerTypesModel.getType(IMarker.TASK) };
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
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setFont(font);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        filterOnMarkerLimit = createCheckbox(composite, TaskListMessages.TaskList_limitVisibleTasksTo, false);
        filterOnMarkerLimit.setLayoutData(new GridData());
        markerLimit = new Text(composite, SWT.SINGLE | SWT.BORDER);
        markerLimit.setTextLimit(6);
        GridData gridData = new GridData();
        gridData.widthHint = convertWidthInCharsToPixels(10);
        markerLimit.setLayoutData(gridData);
        markerLimit.setFont(font);
    }

    /**
     * Returns the filter which this dialog is configuring.
     *
     * @return the filter
     */
    public TasksFilter getFilter() {
        if (filter == null) {
			filter = new TasksFilter();
		}
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
                    if (type.isSubtypeOf(markerTypesModel
                            .getType(IMarker.PROBLEM))
                            || type.isSubtypeOf(markerTypesModel
                                    .getType(IMarker.TASK))) {
                        typesList.add(type);
                    }
                }
            }
            Collections.sort(typesList, new Comparator() {
                Collator collator = Collator.getInstance();

                public int compare(Object o1, Object o2) {
                    return collator.compare(((MarkerType) o1).getLabel(),
                            ((MarkerType) o2).getLabel());
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

    private ViewerComparator getViewerComparator() {
        return new ViewerComparator() {
            public int compare(Viewer viewer, Object e1, Object e2) {
                MarkerType t1 = (MarkerType) e1;
                MarkerType t2 = (MarkerType) e2;
                return getComparator().compare(t1.getLabel(), t2.getLabel());
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
        for (int i = 0; i < types.length; ++i) {
            String id = types[i].getId();
            if (id == null ? markerType == null : id.equals(markerType)) {
				return i;
			}
        }
        return -1;
    }

    void initTypes() {
        severityType = new EnumType(
                new EnumValue[] {
                        new EnumValue(
                                IMarker.SEVERITY_ERROR,
                                TaskListMessages.TaskList_severity_error),
                        new EnumValue(
                                IMarker.SEVERITY_WARNING,
                                TaskListMessages.TaskList_severity_warning),
                        new EnumValue(
                                IMarker.SEVERITY_INFO,
                                TaskListMessages.TaskList_severity_info)
                });

        priorityType = new EnumType(
                new EnumValue[] {
                        new EnumValue(
                                IMarker.PRIORITY_HIGH,
                                TaskListMessages.TaskList_priority_high),
                        new EnumValue(IMarker.PRIORITY_NORMAL, TaskListMessages.TaskList_priority_normal),
                        new EnumValue(
                                IMarker.PRIORITY_LOW,
                                TaskListMessages.TaskList_priority_low)
                });

        completionType = new EnumType(new EnumValue[] {
                new EnumValue(1, TaskListMessages.TaskList_status_completed),
                new EnumValue(0, TaskListMessages.TaskList_status_notCompleted)
                });
    }

    /**
     * Updates the filter from the UI state.
     * Must be done here rather than by extending open()
     * because after super.open() is called, the widgetry is disposed.
     */
    protected void okPressed() {
        try {
            int parseResult = Integer.parseInt(this.markerLimit.getText());

            if (parseResult < 1) {
                throw new NumberFormatException();
            }

            updateFilterFromUI(getFilter());
            super.okPressed();
        } catch (NumberFormatException eNumberFormat) {
            MessageBox messageBox = new MessageBox(getShell(), SWT.OK
                    | SWT.APPLICATION_MODAL | SWT.ICON_ERROR);
            messageBox.setText(TaskListMessages.TaskList_titleMarkerLimitInvalid);
            messageBox.setMessage(TaskListMessages.TaskList_messageMarkerLimitInvalid);
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
     * @param tasksFilter the filter to update
     */
    void updateFilterFromUI(TasksFilter tasksFilter) {

        tasksFilter.types = getSelectedTypes();

        if (selectedResourceButton.getSelection()) {
			tasksFilter.onResource = TasksFilter.ON_SELECTED_RESOURCE_ONLY;
		} else if (selectedResourceAndChildrenButton.getSelection()) {
			tasksFilter.onResource = TasksFilter.ON_SELECTED_RESOURCE_AND_CHILDREN;
		} else if (anyResourceInSameProjectButton.getSelection()) {
			tasksFilter.onResource = TasksFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT;
		} else if (workingSetGroup.getSelection()) {
			tasksFilter.onResource = TasksFilter.ON_WORKING_SET;
		} else {
			tasksFilter.onResource = TasksFilter.ON_ANY_RESOURCE;
		}

        tasksFilter.workingSet = workingSetGroup.getWorkingSet();
        tasksFilter.descriptionFilterKind = descriptionGroup.combo
                .getSelectionIndex();
        tasksFilter.descriptionFilter = descriptionGroup.text.getText();
        tasksFilter.filterOnDescription = !tasksFilter.descriptionFilter
                .equals("");//$NON-NLS-1$

        tasksFilter.filterOnSeverity = severityGroup.getSelection();
        tasksFilter.severityFilter = severityGroup.getValueMask();

        tasksFilter.filterOnPriority = priorityGroup.getSelection();
        tasksFilter.priorityFilter = priorityGroup.getValueMask();

        tasksFilter.filterOnCompletion = completionGroup.getSelection();
        tasksFilter.completionFilter = completionGroup.getValueMask();

        int limit = TasksFilter.DEFAULT_MARKER_LIMIT;

        try {
            limit = Integer.parseInt(this.markerLimit.getText());
        } catch (NumberFormatException eNumberFormat) {
        }

        tasksFilter.setMarkerLimit(limit);
        tasksFilter.setFilterOnMarkerLimit(filterOnMarkerLimit.getSelection());
    }

    /**
     * Updates the UI state from the given filter.
     *
     * @param tasksFilter the filter to use
     */
    void updateUIFromFilter(TasksFilter tasksFilter) {

        setSelectedTypes(tasksFilter.types);

        int on = tasksFilter.onResource;
        anyResourceButton.setSelection(on == TasksFilter.ON_ANY_RESOURCE);
        anyResourceInSameProjectButton
                .setSelection(on == TasksFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT); // added by cagatayk@acm.org
        selectedResourceButton
                .setSelection(on == TasksFilter.ON_SELECTED_RESOURCE_ONLY);
        selectedResourceAndChildrenButton
                .setSelection(on == TasksFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
        workingSetGroup.setSelection(on == TasksFilter.ON_WORKING_SET);
        workingSetGroup.setWorkingSet(tasksFilter.workingSet);

        descriptionGroup.combo.select(tasksFilter.descriptionFilterKind);
        descriptionGroup.text.setText(tasksFilter.descriptionFilter);

        severityGroup.setSelection(tasksFilter.filterOnSeverity);
        severityGroup.setValueMask(tasksFilter.severityFilter);

        priorityGroup.setSelection(tasksFilter.filterOnPriority);
        priorityGroup.setValueMask(tasksFilter.priorityFilter);

        completionGroup.setSelection(tasksFilter.filterOnCompletion);
        completionGroup.setValueMask(tasksFilter.completionFilter);

        markerLimit.setText("" + tasksFilter.getMarkerLimit()); //$NON-NLS-1$
        filterOnMarkerLimit.setSelection(tasksFilter.getFilterOnMarkerLimit());

        updateEnabledState();
    }

	/**
	 * Handles selection on a check box or combo box.
	 * 
	 * @param e the selection event
	 */
    void widgetSelected(SelectionEvent e) {
        updateEnabledState();
    }
}
