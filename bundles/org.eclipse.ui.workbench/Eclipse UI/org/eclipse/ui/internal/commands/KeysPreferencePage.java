/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.keys.KeySequenceText;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

public class KeysPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private final static class CommandAssignment implements Comparable {

        private KeySequenceBindingNode.Assignment assignment;

        private String contextId;

        private KeySequence keySequence;

        public int compareTo(Object object) {
            CommandAssignment castedObject = (CommandAssignment) object;
            int compareTo = Util.compare(contextId, castedObject.contextId);

            if (compareTo == 0) {
                compareTo = Util.compare(keySequence, castedObject.keySequence);

                if (compareTo == 0)
                    compareTo = Util.compare(assignment,
                            castedObject.assignment);
            }

            return compareTo;
        }

        public boolean equals(Object object) {
            if (!(object instanceof CommandAssignment))
                return false;

            CommandAssignment castedObject = (CommandAssignment) object;
            boolean equals = true;
            equals &= Util.equals(assignment, castedObject.assignment);
            equals &= Util.equals(contextId, castedObject.contextId);
            equals &= Util.equals(keySequence, castedObject.keySequence);
            return equals;
        }
    }

    private final static class KeySequenceAssignment implements Comparable {

        private KeySequenceBindingNode.Assignment assignment;

        private String contextId;

        public int compareTo(Object object) {
            KeySequenceAssignment castedObject = (KeySequenceAssignment) object;
            int compareTo = Util.compare(contextId, castedObject.contextId);

            if (compareTo == 0)
                compareTo = Util.compare(assignment, castedObject.assignment);

            return compareTo;
        }

        public boolean equals(Object object) {
            if (!(object instanceof CommandAssignment))
                return false;

            KeySequenceAssignment castedObject = (KeySequenceAssignment) object;
            boolean equals = true;
            equals &= Util.equals(assignment, castedObject.assignment);
            equals &= Util.equals(contextId, castedObject.contextId);
            return equals;
        }
    }

    /**
     * A selection listener to be used on the columns in the table on the view
     * tab. This selection listener modifies the sort order so that the
     * appropriate column is in the first position.
     * 
     * @since 3.1
     */
    private class SortOrderSelectionListener extends SelectionAdapter {

        /**
         * The column to be put in the first position. This value should be one
         * of the constants defined by <code>SORT_COLUMN_</code>.
         */
        private final int columnSelected;

        /**
         * Constructs a new instance of <code>SortOrderSelectionListener</code>.
         * 
         * @param columnSelected
         *            The column to be given first priority in the sort order;
         *            this value should be one of the constants defined as
         *            <code>SORT_COLUMN_</code>.
         */
        private SortOrderSelectionListener(final int columnSelected) {
            this.columnSelected = columnSelected;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetSelected(SelectionEvent e) {
            // Change the column titles.
            final int oldSortIndex = sortOrder[0];
            final TableColumn oldSortColumn = tableKeyBindings
                    .getColumn(oldSortIndex);
            oldSortColumn.setText(UNSORTED_COLUMN_NAMES[oldSortIndex]);
            final TableColumn newSortColumn = tableKeyBindings
                    .getColumn(columnSelected);
            newSortColumn.setText(SORTED_COLUMN_NAMES[columnSelected]);

            // Change the sort order.
            boolean columnPlaced = false;
            boolean enoughRoom = false;
            int bumpedColumn = -1;
            for (int i = 0; i < sortOrder.length; i++) {
                if (sortOrder[i] == columnSelected) {
                    /*
                     * We've found the place where the column existing in the
                     * old sort order. No matter what at this point, we have
                     * completed the reshuffling.
                     */
                    enoughRoom = true;
                    if (bumpedColumn != -1) {
                        // We have already started bumping things around, so
                        // drop the last bumped column here.
                        sortOrder[i] = bumpedColumn;
                    } else {
                        // The order has not changed.
                        columnPlaced = true;
                    }
                    break;

                } else if (columnPlaced) {
                    // We are currently bumping, so just bump another.
                    int temp = sortOrder[i];
                    sortOrder[i] = bumpedColumn;
                    bumpedColumn = temp;

                } else {
                    /*
                     * We are not currently bumping, so drop the column and
                     * start bumping.
                     */
                    bumpedColumn = sortOrder[i];
                    sortOrder[i] = columnSelected;
                    columnPlaced = true;
                }
            }

            // Grow the sort order.
            if (!enoughRoom) {
                final int[] newSortOrder = new int[sortOrder.length + 1];
                System.arraycopy(sortOrder, 0, newSortOrder, 0,
                        sortOrder.length);
                newSortOrder[sortOrder.length] = bumpedColumn;
                sortOrder = newSortOrder;
            }

            // Update the view tab.
            updateViewTab();
        }
    }

    private final static int DIFFERENCE_ADD = 0;

    private final static int DIFFERENCE_CHANGE = 1;

    private final static int DIFFERENCE_MINUS = 2;

    private final static int DIFFERENCE_NONE = 3;

    private final static Image IMAGE_BLANK = ImageFactory.getImage("blank"); //$NON-NLS-1$

    private final static Image IMAGE_CHANGE = ImageFactory.getImage("change"); //$NON-NLS-1$

    private final static Image IMAGE_MINUS = ImageFactory.getImage("minus"); //$NON-NLS-1$

    private final static Image IMAGE_PLUS = ImageFactory.getImage("plus"); //$NON-NLS-1$

    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(KeysPreferencePage.class.getName());

    /**
     * The index of the modify tab.
     * @since 3.1
     */
    private static final int TAB_INDEX_MODIFY = 1;

    /**
     * The index of the column on the view tab containing the category name.
     */
    private final static int VIEW_CATEGORY_COLUMN_INDEX = 0;

    /**
     * The index of the column on the view tab containing the command name.
     */
    private final static int VIEW_COMMAND_COLUMN_INDEX = 1;

    /**
     * The index of the column on the view tab containing the context name.
     */
    private final static int VIEW_CONTEXT_COLUMN_INDEX = 3;

    /**
     * The index of the column on the view tab containing the key sequence.
     */
    private final static int VIEW_KEY_SEQUENCE_COLUMN_INDEX = 2;

    /**
     * The total number of columns on the view tab.
     */
    private final static int VIEW_TOTAL_COLUMNS = 4;

    /**
     * The translated names for the columns when they are not the primary sort
     * key (e.g., "Category").
     */
    private final static String[] UNSORTED_COLUMN_NAMES = new String[VIEW_TOTAL_COLUMNS];

    /**
     * The translated names for the columns when they are the primary sort key
     * (e.g., ">Category <").
     */
    private final static String[] SORTED_COLUMN_NAMES = new String[VIEW_TOTAL_COLUMNS];

    static {
        UNSORTED_COLUMN_NAMES[VIEW_CATEGORY_COLUMN_INDEX] = Util
                .translateString(RESOURCE_BUNDLE, "tableColumnCategory"); //$NON-NLS-1$
        UNSORTED_COLUMN_NAMES[VIEW_COMMAND_COLUMN_INDEX] = Util
                .translateString(RESOURCE_BUNDLE, "tableColumnCommand"); //$NON-NLS-1$
        UNSORTED_COLUMN_NAMES[VIEW_KEY_SEQUENCE_COLUMN_INDEX] = Util
                .translateString(RESOURCE_BUNDLE, "tableColumnKeySequence"); //$NON-NLS-1$
        UNSORTED_COLUMN_NAMES[VIEW_CONTEXT_COLUMN_INDEX] = Util
                .translateString(RESOURCE_BUNDLE, "tableColumnContext"); //$NON-NLS-1$

        SORTED_COLUMN_NAMES[VIEW_CATEGORY_COLUMN_INDEX] = Util.translateString(
                RESOURCE_BUNDLE, "tableColumnCategorySorted"); //$NON-NLS-1$
        SORTED_COLUMN_NAMES[VIEW_COMMAND_COLUMN_INDEX] = Util.translateString(
                RESOURCE_BUNDLE, "tableColumnCommandSorted"); //$NON-NLS-1$
        SORTED_COLUMN_NAMES[VIEW_KEY_SEQUENCE_COLUMN_INDEX] = Util
                .translateString(RESOURCE_BUNDLE,
                        "tableColumnKeySequenceSorted"); //$NON-NLS-1$
        SORTED_COLUMN_NAMES[VIEW_CONTEXT_COLUMN_INDEX] = Util.translateString(
                RESOURCE_BUNDLE, "tableColumnContextSorted"); //$NON-NLS-1$
    }

    private Map assignmentsByContextIdByKeySequence;

    private Button buttonAdd;

    private Button buttonAddKey;

    private Button buttonRemove;

    private Button buttonRestore;

    private Map categoryIdsByUniqueName;

    private Map categoryUniqueNamesById;

    private Combo comboCategory;

    private Combo comboCommand;

    private Combo comboContext;

    private Combo comboKeyConfiguration;

    private Set commandAssignments;

    private Map commandIdsByCategoryId;

    private Map commandIdsByUniqueName;

    private MutableCommandManager commandManager;

    private Map commandUniqueNamesById;

    private Map contextIdsByUniqueName;

    private IContextManager contextManager;

    private Map contextUniqueNamesById;

    private Group groupCommand;

    private Group groupKeySequence;

    private Map keyConfigurationIdsByUniqueName;

    private Map keyConfigurationUniqueNamesById;

    private Set keySequenceAssignments;

    private Label labelAssignmentsForCommand;

    private Label labelAssignmentsForKeySequence;

    private Label labelCategory;

    private Label labelCommand;

    private Label labelContext;

    private Label labelContextExtends;

    private Label labelKeyConfiguration;

    private Label labelKeyConfigurationExtends;

    private Label labelKeySequence;

    private Menu menuButtonAddKey;

    private Color minusColour;

    /**
     * The sort order to be used on the view tab to display all of the key
     * bindings. This sort order can be changed by the user. This array is never
     * <code>null</code>, but may be empty.
     */
    private int[] sortOrder = { VIEW_CATEGORY_COLUMN_INDEX,
            VIEW_COMMAND_COLUMN_INDEX, VIEW_KEY_SEQUENCE_COLUMN_INDEX,
            VIEW_CONTEXT_COLUMN_INDEX };

    private TabFolder tabFolder;

    private Table tableAssignmentsForCommand;

    private Table tableAssignmentsForKeySequence;

    /**
     * A table of the key bindings currently defined. This table appears on the
     * view tab; it is intended to be an easy way for users to learn the key
     * bindings in Eclipse. This value is only <code>null</code> until the
     * controls are first created.
     */
    private Table tableKeyBindings;

    private Text textKeySequence;

    private KeySequenceText textKeySequenceManager;

    private SortedMap tree;

    private void buildCommandAssignmentsTable() {
        tableAssignmentsForCommand.removeAll();
        boolean matchFoundInFirstKeyConfiguration = false;

        for (Iterator iterator = commandAssignments.iterator(); iterator
                .hasNext();) {
            boolean createTableItem = true;
            CommandAssignment commandAssignment = (CommandAssignment) iterator
                    .next();
            KeySequenceBindingNode.Assignment assignment = commandAssignment.assignment;
            KeySequence keySequence = commandAssignment.keySequence;
            String commandString = null;
            int difference = DIFFERENCE_NONE;

            if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration
                    || assignment.hasPreferenceCommandIdInInheritedKeyConfiguration) {
                String preferenceCommandId;

                if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration)
                    preferenceCommandId = assignment.preferenceCommandIdInFirstKeyConfiguration;
                else
                    preferenceCommandId = assignment.preferenceCommandIdInInheritedKeyConfiguration;

                if (assignment.hasPluginCommandIdInFirstKeyConfiguration
                        || assignment.hasPluginCommandIdInInheritedKeyConfiguration) {
                    String pluginCommandId;

                    if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
                        pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
                    else
                        pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;
                    if (preferenceCommandId != null) {
                        difference = DIFFERENCE_CHANGE;
                        commandString =
                        /* commandUniqueNamesById.get(preferenceCommandId) */
                        keySequence.format() + ""; //$NON-NLS-1$
                    } else {
                        difference = DIFFERENCE_MINUS;
                        commandString = /* "Unassigned" */
                        keySequence.format();
                    }

                    if (pluginCommandId != null)
                        commandString += " (was: " //$NON-NLS-1$
                                + commandUniqueNamesById.get(pluginCommandId)
                                + ")"; //$NON-NLS-1$
                    else
                        commandString += " (was: " + "Unassigned" + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } else {
                    if (preferenceCommandId != null) {
                        difference = DIFFERENCE_ADD;
                        commandString =
                        /* commandUniqueNamesById.get(preferenceCommandId) */
                        keySequence.format() + ""; //$NON-NLS-1$
                    } else {
                        difference = DIFFERENCE_MINUS;
                        commandString = /* "Unassigned" */
                        keySequence.format();
                    }
                }
            } else {
                String pluginCommandId = null;
                if (assignment.hasPluginCommandIdInFirstKeyConfiguration) {
                    pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
                    if (pluginCommandId != null) {
                        matchFoundInFirstKeyConfiguration = true;
                    }
                } else if (!matchFoundInFirstKeyConfiguration) {
                    pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;
                } else {
                    createTableItem = false;
                    iterator.remove();
                }

                if (pluginCommandId != null) {
                    difference = DIFFERENCE_NONE;
                    commandString =
                    /* commandUniqueNamesById.get(preferenceCommandId) */
                    keySequence.format() + ""; //$NON-NLS-1$
                } else {
                    difference = DIFFERENCE_MINUS;
                    commandString = /* "Unassigned" */
                    keySequence.format();
                }
            }

            if (createTableItem) {
                TableItem tableItem = new TableItem(tableAssignmentsForCommand,
                        SWT.NULL);

                switch (difference) {
                case DIFFERENCE_ADD:
                    tableItem.setImage(0, IMAGE_PLUS);
                    break;

                case DIFFERENCE_CHANGE:
                    tableItem.setImage(0, IMAGE_CHANGE);
                    break;

                case DIFFERENCE_MINUS:
                    tableItem.setImage(0, IMAGE_MINUS);
                    break;

                case DIFFERENCE_NONE:
                    tableItem.setImage(0, IMAGE_BLANK);
                    break;
                }

                String contextId = commandAssignment.contextId;

                if (contextId == null) {
                    // This should never happen.
                    tableItem.setText(1, Util.ZERO_LENGTH_STRING);
                } else
                    tableItem.setText(1, (String) contextUniqueNamesById
                            .get(contextId)); //$NON-NLS-1$

                tableItem.setText(2, commandString);

                if (difference == DIFFERENCE_MINUS) {
                    tableItem.setForeground(minusColour);
                }
            }
        }
    }

    private void buildKeySequenceAssignmentsTable() {
        tableAssignmentsForKeySequence.removeAll();
        boolean matchFoundInFirstKeyConfiguration = false;

        for (Iterator iterator = keySequenceAssignments.iterator(); iterator
                .hasNext();) {
            boolean createTableItem = true;
            KeySequenceAssignment keySequenceAssignment = (KeySequenceAssignment) iterator
                    .next();
            KeySequenceBindingNode.Assignment assignment = keySequenceAssignment.assignment;
            String commandString = null;
            int difference = DIFFERENCE_NONE;

            if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration
                    || assignment.hasPreferenceCommandIdInInheritedKeyConfiguration) {
                String preferenceCommandId;

                if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration)
                    preferenceCommandId = assignment.preferenceCommandIdInFirstKeyConfiguration;
                else
                    preferenceCommandId = assignment.preferenceCommandIdInInheritedKeyConfiguration;

                if (assignment.hasPluginCommandIdInFirstKeyConfiguration
                        || assignment.hasPluginCommandIdInInheritedKeyConfiguration) {
                    String pluginCommandId;

                    if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
                        pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
                    else
                        pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;

                    if (preferenceCommandId != null) {
                        difference = DIFFERENCE_CHANGE;
                        commandString = commandUniqueNamesById
                                .get(preferenceCommandId)
                                + ""; //$NON-NLS-1$
                    } else {
                        difference = DIFFERENCE_MINUS;
                        commandString = "Unassigned"; //$NON-NLS-1$
                    }

                    if (pluginCommandId != null)
                        commandString += " (was: " //$NON-NLS-1$
                                + commandUniqueNamesById.get(pluginCommandId)
                                + ")"; //$NON-NLS-1$
                    else
                        commandString += " (was: " + "Unassigned" + ")"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                } else {
                    if (preferenceCommandId != null) {
                        difference = DIFFERENCE_ADD;
                        commandString = commandUniqueNamesById
                                .get(preferenceCommandId)
                                + ""; //$NON-NLS-1$
                    } else {
                        difference = DIFFERENCE_MINUS;
                        commandString = "Unassigned"; //$NON-NLS-1$
                    }
                }
            } else {
                String pluginCommandId = null;

                if (assignment.hasPluginCommandIdInFirstKeyConfiguration) {
                    pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
                    if (pluginCommandId != null) {
                        matchFoundInFirstKeyConfiguration = true;
                    }
                } else if (!matchFoundInFirstKeyConfiguration) {
                    pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;
                } else {
                    createTableItem = false;
                    iterator.remove();
                }

                if (pluginCommandId != null) {
                    difference = DIFFERENCE_NONE;
                    commandString = commandUniqueNamesById.get(pluginCommandId)
                            + ""; //$NON-NLS-1$
                } else {
                    difference = DIFFERENCE_MINUS;
                    commandString = "Unassigned"; //$NON-NLS-1$
                }
            }

            if (createTableItem) {
                TableItem tableItem = new TableItem(
                        tableAssignmentsForKeySequence, SWT.NULL);

                switch (difference) {
                case DIFFERENCE_ADD:
                    tableItem.setImage(0, IMAGE_PLUS);
                    break;

                case DIFFERENCE_CHANGE:
                    tableItem.setImage(0, IMAGE_CHANGE);
                    break;

                case DIFFERENCE_MINUS:
                    tableItem.setImage(0, IMAGE_MINUS);
                    break;

                case DIFFERENCE_NONE:
                    tableItem.setImage(0, IMAGE_BLANK);
                    break;
                }

                String contextId = keySequenceAssignment.contextId;

                if (contextId == null) {
                    // This should never happen.
                    tableItem.setText(1, Util.ZERO_LENGTH_STRING);
                } else {
                    tableItem.setText(1, (String) contextUniqueNamesById
                            .get(contextId)); //$NON-NLS-1$
                }

                tableItem.setText(2, commandString);

                if (difference == DIFFERENCE_MINUS) {
                    tableItem.setForeground(minusColour);
                }
            }
        }
    }

    protected Control createContents(Composite parent) {
        // Initialize the minus colour.
        minusColour = getShell().getDisplay().getSystemColor(
                SWT.COLOR_WIDGET_NORMAL_SHADOW);

        tabFolder = new TabFolder(parent, SWT.NULL);

        // View tab
        final TabItem viewTab = new TabItem(tabFolder, SWT.NULL);
        viewTab.setText(Util.translateString(RESOURCE_BUNDLE, "viewTab.Text")); //$NON-NLS-1$
        viewTab.setControl(createViewTab(tabFolder));

        // Modify tab
        final TabItem modifyTab = new TabItem(tabFolder, SWT.NULL);
        modifyTab.setText(Util.translateString(RESOURCE_BUNDLE,
                "modifyTab.Text")); //$NON-NLS-1$
        modifyTab.setControl(createModifyTab(tabFolder));

        // Do some fancy stuff.
        applyDialogFont(tabFolder);
        final IPreferenceStore store = getPreferenceStore();
        final int selectedTab = store
                .getInt(IPreferenceConstants.KEYS_PREFERENCE_SELECTED_TAB);
        if ((tabFolder.getItemCount() > selectedTab) && (selectedTab > 0)) {
            tabFolder.setSelection(selectedTab);
        }

        return tabFolder;
    }

    private Composite createModifyTab(TabFolder parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gridData);
        Composite compositeKeyConfiguration = new Composite(composite, SWT.NULL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        compositeKeyConfiguration.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        compositeKeyConfiguration.setLayoutData(gridData);
        labelKeyConfiguration = new Label(compositeKeyConfiguration, SWT.LEFT);
        labelKeyConfiguration.setText(Util.translateString(RESOURCE_BUNDLE,
                "labelKeyConfiguration")); //$NON-NLS-1$
        comboKeyConfiguration = new Combo(compositeKeyConfiguration,
                SWT.READ_ONLY);
        gridData = new GridData();
        gridData.widthHint = 200;
        comboKeyConfiguration.setLayoutData(gridData);

        comboKeyConfiguration.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                selectedComboKeyConfiguration();
            }
        });

        labelKeyConfigurationExtends = new Label(compositeKeyConfiguration,
                SWT.LEFT);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        labelKeyConfigurationExtends.setLayoutData(gridData);
        Control spacer = new Composite(composite, SWT.NULL);
        gridData = new GridData();
        gridData.heightHint = 10;
        gridData.widthHint = 10;
        spacer.setLayoutData(gridData);
        groupCommand = new Group(composite, SWT.SHADOW_NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        groupCommand.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_BOTH);
        groupCommand.setLayoutData(gridData);
        groupCommand.setText(Util.translateString(RESOURCE_BUNDLE,
                "groupCommand")); //$NON-NLS-1$	
        labelCategory = new Label(groupCommand, SWT.LEFT);
        gridData = new GridData();
        labelCategory.setLayoutData(gridData);
        labelCategory.setText(Util.translateString(RESOURCE_BUNDLE,
                "labelCategory")); //$NON-NLS-1$
        comboCategory = new Combo(groupCommand, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.widthHint = 200;
        comboCategory.setLayoutData(gridData);

        comboCategory.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                selectedComboCategory();
            }
        });

        labelCommand = new Label(groupCommand, SWT.LEFT);
        gridData = new GridData();
        labelCommand.setLayoutData(gridData);
        labelCommand.setText(Util.translateString(RESOURCE_BUNDLE,
                "labelCommand")); //$NON-NLS-1$
        comboCommand = new Combo(groupCommand, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.widthHint = 300;
        comboCommand.setLayoutData(gridData);

        comboCommand.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                selectedComboCommand();
            }
        });

        labelAssignmentsForCommand = new Label(groupCommand, SWT.LEFT);
        gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gridData.verticalAlignment = GridData.FILL_VERTICAL;
        labelAssignmentsForCommand.setLayoutData(gridData);
        labelAssignmentsForCommand.setText(Util.translateString(
                RESOURCE_BUNDLE, "labelAssignmentsForCommand")); //$NON-NLS-1$
        tableAssignmentsForCommand = new Table(groupCommand, SWT.BORDER
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        tableAssignmentsForCommand.setHeaderVisible(true);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 60;
        gridData.horizontalSpan = 2;
        gridData.widthHint = "carbon".equals(SWT.getPlatform()) ? 620 : 520; //$NON-NLS-1$
        tableAssignmentsForCommand.setLayoutData(gridData);
        TableColumn tableColumnDelta = new TableColumn(
                tableAssignmentsForCommand, SWT.NULL, 0);
        tableColumnDelta.setResizable(false);
        tableColumnDelta.setText(Util.ZERO_LENGTH_STRING);
        tableColumnDelta.setWidth(20);
        TableColumn tableColumnContext = new TableColumn(
                tableAssignmentsForCommand, SWT.NULL, 1);
        tableColumnContext.setResizable(true);
        tableColumnContext.setText(Util.translateString(RESOURCE_BUNDLE,
                "tableColumnContext")); //$NON-NLS-1$
        tableColumnContext.pack();
        tableColumnContext.setWidth(200);
        TableColumn tableColumnKeySequence = new TableColumn(
                tableAssignmentsForCommand, SWT.NULL, 2);
        tableColumnKeySequence.setResizable(true);
        tableColumnKeySequence.setText(Util.translateString(RESOURCE_BUNDLE,
                "tableColumnKeySequence")); //$NON-NLS-1$
        tableColumnKeySequence.pack();
        tableColumnKeySequence.setWidth(300);

        tableAssignmentsForCommand.addMouseListener(new MouseAdapter() {

            public void mouseDoubleClick(MouseEvent mouseEvent) {
                doubleClickedAssignmentsForCommand();
            }
        });

        tableAssignmentsForCommand.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                selectedTableAssignmentsForCommand();
            }
        });

        groupKeySequence = new Group(composite, SWT.SHADOW_NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        groupKeySequence.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_BOTH);
        groupKeySequence.setLayoutData(gridData);
        groupKeySequence.setText(Util.translateString(RESOURCE_BUNDLE,
                "groupKeySequence")); //$NON-NLS-1$	
        labelKeySequence = new Label(groupKeySequence, SWT.LEFT);
        gridData = new GridData();
        labelKeySequence.setLayoutData(gridData);
        labelKeySequence.setText(Util.translateString(RESOURCE_BUNDLE,
                "labelKeySequence")); //$NON-NLS-1$

        // The text widget into which the key strokes will be entered.
        textKeySequence = new Text(groupKeySequence, SWT.BORDER);
        // On MacOS X, this font will be changed by KeySequenceText
        textKeySequence.setFont(groupKeySequence.getFont());
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.widthHint = 300;
        textKeySequence.setLayoutData(gridData);
        textKeySequence.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                modifiedTextKeySequence();
            }
        });
        textKeySequence.addFocusListener(new FocusListener() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
             */
            public void focusGained(FocusEvent e) {
                PlatformUI.getWorkbench().getContextSupport()
                        .setKeyFilterEnabled(false);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
             */
            public void focusLost(FocusEvent e) {
                PlatformUI.getWorkbench().getContextSupport()
                        .setKeyFilterEnabled(true);
            }
        });

        // The manager for the key sequence text widget.
        textKeySequenceManager = new KeySequenceText(textKeySequence);
        textKeySequenceManager.setKeyStrokeLimit(4);

        // Button for adding trapped key strokes
        buttonAddKey = new Button(groupKeySequence, SWT.LEFT | SWT.ARROW);
        buttonAddKey.setToolTipText(Util.translateString(RESOURCE_BUNDLE,
                "buttonAddKey.ToolTipText")); //$NON-NLS-1$
        gridData = new GridData();
        gridData.heightHint = comboCategory.getTextHeight();
        buttonAddKey.setLayoutData(gridData);
        buttonAddKey.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                Point buttonLocation = buttonAddKey.getLocation();
                buttonLocation = groupKeySequence.toDisplay(buttonLocation.x,
                        buttonLocation.y);
                Point buttonSize = buttonAddKey.getSize();
                menuButtonAddKey.setLocation(buttonLocation.x, buttonLocation.y
                        + buttonSize.y);
                menuButtonAddKey.setVisible(true);
            }
        });

        // Arrow buttons aren't normally added to the tab list. Let's fix that.
        Control[] tabStops = groupKeySequence.getTabList();
        ArrayList newTabStops = new ArrayList();
        for (int i = 0; i < tabStops.length; i++) {
            Control tabStop = tabStops[i];
            newTabStops.add(tabStop);
            if (textKeySequence.equals(tabStop)) {
                newTabStops.add(buttonAddKey);
            }
        }
        Control[] newTabStopArray = (Control[]) newTabStops
                .toArray(new Control[newTabStops.size()]);
        groupKeySequence.setTabList(newTabStopArray);

        // Construct the menu to attach to the above button.
        menuButtonAddKey = new Menu(buttonAddKey);
        Iterator trappedKeyItr = KeySequenceText.TRAPPED_KEYS.iterator();
        while (trappedKeyItr.hasNext()) {
            final KeyStroke trappedKey = (KeyStroke) trappedKeyItr.next();
            MenuItem menuItem = new MenuItem(menuButtonAddKey, SWT.PUSH);
            menuItem.setText(trappedKey.format());
            menuItem.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    textKeySequenceManager.insert(trappedKey);
                    textKeySequence.setFocus();
                    textKeySequence
                            .setSelection(textKeySequence.getTextLimit());
                }
            });
        }

        labelAssignmentsForKeySequence = new Label(groupKeySequence, SWT.LEFT);
        gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gridData.verticalAlignment = GridData.FILL_VERTICAL;
        labelAssignmentsForKeySequence.setLayoutData(gridData);
        labelAssignmentsForKeySequence.setText(Util.translateString(
                RESOURCE_BUNDLE, "labelAssignmentsForKeySequence")); //$NON-NLS-1$
        tableAssignmentsForKeySequence = new Table(groupKeySequence, SWT.BORDER
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        tableAssignmentsForKeySequence.setHeaderVisible(true);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 60;
        gridData.horizontalSpan = 3;
        gridData.widthHint = "carbon".equals(SWT.getPlatform()) ? 620 : 520; //$NON-NLS-1$
        tableAssignmentsForKeySequence.setLayoutData(gridData);
        tableColumnDelta = new TableColumn(tableAssignmentsForKeySequence,
                SWT.NULL, 0);
        tableColumnDelta.setResizable(false);
        tableColumnDelta.setText(Util.ZERO_LENGTH_STRING);
        tableColumnDelta.setWidth(20);
        tableColumnContext = new TableColumn(tableAssignmentsForKeySequence,
                SWT.NULL, 1);
        tableColumnContext.setResizable(true);
        tableColumnContext.setText(Util.translateString(RESOURCE_BUNDLE,
                "tableColumnContext")); //$NON-NLS-1$
        tableColumnContext.pack();
        tableColumnContext.setWidth(200);
        TableColumn tableColumnCommand = new TableColumn(
                tableAssignmentsForKeySequence, SWT.NULL, 2);
        tableColumnCommand.setResizable(true);
        tableColumnCommand.setText(Util.translateString(RESOURCE_BUNDLE,
                "tableColumnCommand")); //$NON-NLS-1$
        tableColumnCommand.pack();
        tableColumnCommand.setWidth(300);

        tableAssignmentsForKeySequence.addMouseListener(new MouseAdapter() {

            public void mouseDoubleClick(MouseEvent mouseEvent) {
                doubleClickedTableAssignmentsForKeySequence();
            }
        });

        tableAssignmentsForKeySequence
                .addSelectionListener(new SelectionAdapter() {

                    public void widgetSelected(SelectionEvent selectionEvent) {
                        selectedTableAssignmentsForKeySequence();
                    }
                });

        Composite compositeContext = new Composite(composite, SWT.NULL);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        compositeContext.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        compositeContext.setLayoutData(gridData);
        labelContext = new Label(compositeContext, SWT.LEFT);
        labelContext.setText(Util.translateString(RESOURCE_BUNDLE,
                "labelContext")); //$NON-NLS-1$
        comboContext = new Combo(compositeContext, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.widthHint = 250;
        comboContext.setLayoutData(gridData);

        comboContext.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                selectedComboContext();
            }
        });

        labelContextExtends = new Label(compositeContext, SWT.LEFT);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        labelContextExtends.setLayoutData(gridData);
        Composite compositeButton = new Composite(composite, SWT.NULL);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 20;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        compositeButton.setLayout(gridLayout);
        gridData = new GridData();
        compositeButton.setLayoutData(gridData);
        buttonAdd = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
        gridData = new GridData();
        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        buttonAdd.setText(Util.translateString(RESOURCE_BUNDLE, "buttonAdd")); //$NON-NLS-1$
        gridData.widthHint = Math.max(widthHint, buttonAdd.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
        buttonAdd.setLayoutData(gridData);

        buttonAdd.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                selectedButtonAdd();
            }
        });

        buttonRemove = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
        gridData = new GridData();
        widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        buttonRemove.setText(Util.translateString(RESOURCE_BUNDLE,
                "buttonRemove")); //$NON-NLS-1$
        gridData.widthHint = Math.max(widthHint, buttonRemove.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
        buttonRemove.setLayoutData(gridData);

        buttonRemove.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                selectedButtonRemove();
            }
        });

        buttonRestore = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
        gridData = new GridData();
        widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        buttonRestore.setText(Util.translateString(RESOURCE_BUNDLE,
                "buttonRestore")); //$NON-NLS-1$
        gridData.widthHint = Math.max(widthHint, buttonRestore.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
        buttonRestore.setLayoutData(gridData);

        buttonRestore.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                selectedButtonRestore();
            }
        });

        // TODO WorkbenchHelp.setHelp(parent,
        // IHelpContextIds.WORKBENCH_KEY_PREFERENCE_PAGE);
        return composite;
    }

    /**
     * Creates a tab on the main page for displaying an uneditable list of the
     * current key bindings. This is intended as a discovery tool for new users.
     * It shows all of the key bindings for the current key configuration,
     * platform and locale.
     * 
     * @param parent
     *            The tab folder in which the tab should be created; must not be
     *            <code>null</code>.
     * @return The newly created composite containing all of the controls; never
     *         <code>null</code>.
     * @since 3.1
     */
    private final Composite createViewTab(final TabFolder parent) {
        GridData gridData = null;
        int widthHint;

        // Create the composite for the tab.
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        // Place a table inside the tab.
        tableKeyBindings = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION
                | SWT.H_SCROLL | SWT.V_SCROLL);
        tableKeyBindings.setHeaderVisible(true);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 400;
        gridData.horizontalSpan = 2;
        tableKeyBindings.setLayoutData(gridData);
        final TableColumn tableColumnCategory = new TableColumn(
                tableKeyBindings, SWT.NONE, VIEW_CATEGORY_COLUMN_INDEX);
        tableColumnCategory
                .setText(SORTED_COLUMN_NAMES[VIEW_CATEGORY_COLUMN_INDEX]);
        tableColumnCategory
                .addSelectionListener(new SortOrderSelectionListener(
                        VIEW_CATEGORY_COLUMN_INDEX));
        final TableColumn tableColumnCommand = new TableColumn(
                tableKeyBindings, SWT.NONE, VIEW_COMMAND_COLUMN_INDEX);
        tableColumnCommand
                .setText(UNSORTED_COLUMN_NAMES[VIEW_COMMAND_COLUMN_INDEX]);
        tableColumnCommand.addSelectionListener(new SortOrderSelectionListener(
                VIEW_COMMAND_COLUMN_INDEX));
        final TableColumn tableColumnKeySequence = new TableColumn(
                tableKeyBindings, SWT.NONE, VIEW_KEY_SEQUENCE_COLUMN_INDEX);
        tableColumnKeySequence
                .setText(UNSORTED_COLUMN_NAMES[VIEW_KEY_SEQUENCE_COLUMN_INDEX]);
        tableColumnKeySequence
                .addSelectionListener(new SortOrderSelectionListener(
                        VIEW_KEY_SEQUENCE_COLUMN_INDEX));
        final TableColumn tableColumnContext = new TableColumn(
                tableKeyBindings, SWT.NONE, VIEW_CONTEXT_COLUMN_INDEX);
        tableColumnContext
                .setText(UNSORTED_COLUMN_NAMES[VIEW_CONTEXT_COLUMN_INDEX]);
        tableColumnContext.addSelectionListener(new SortOrderSelectionListener(
                VIEW_CONTEXT_COLUMN_INDEX));
        tableKeyBindings.addSelectionListener(new SelectionAdapter() {
            public final void widgetDefaultSelected(final SelectionEvent e) {
                selectedTableKeyBindings();
            }
        });

        // A composite for the buttons.
        final Composite buttonBar = new Composite(composite, SWT.NONE);
        buttonBar.setLayout(new GridLayout(2, false));
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.END;
        buttonBar.setLayoutData(gridData);

        // A button for editing the current selection.
        final Button editButton = new Button(buttonBar, SWT.PUSH);
        gridData = new GridData();
        widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        gridData.widthHint = Math.max(widthHint, editButton.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
        editButton.setLayoutData(gridData);
        editButton.setText(Util.translateString(RESOURCE_BUNDLE, "buttonEdit")); //$NON-NLS-1$
        editButton.addSelectionListener(new SelectionListener() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public final void widgetDefaultSelected(final SelectionEvent event) {
                selectedTableKeyBindings();
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });

        // A button for exporting the contents to a file.
        final Button buttonExport = new Button(buttonBar, SWT.PUSH);
        gridData = new GridData();
        widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        gridData.widthHint = Math.max(widthHint, buttonExport.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
        buttonExport.setLayoutData(gridData);
        buttonExport.setText(Util.translateString(RESOURCE_BUNDLE,
                "buttonExport")); //$NON-NLS-1$
        buttonExport.addSelectionListener(new SelectionListener() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public final void widgetDefaultSelected(final SelectionEvent event) {
                selectedButtonExport();
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });

        return composite;
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return PlatformUI.getWorkbench().getPreferenceStore();
    }

    private void doubleClickedAssignmentsForCommand() {
        update();
    }

    private void doubleClickedTableAssignmentsForKeySequence() {
        update();
    }
    
    /**
     * Allows the user to change the key bindings for a particular command.
     * Switches the tab to the modify tab, and then selects the category and
     * command that corresponds with the given command name. It then selects the
     * given key sequence and gives focus to the key sequence text widget.
     * 
     * @param commandName
     *            The name of the command for which the key bindings should be
     *            edited; if <code>null</code>, then just switch to the
     *            modify tab. If the <code>commandName</code> is undefined or
     *            does not correspond to anything in the keys preference page,
     *            then this also just switches to the modify tab.
     * @param keySequence
     *            The key sequence for the selected item. If <code>null</code>,
     *            then just switch to the modify tab.
     * @since 3.1
     */
    public final void editCommand(final String commandName,
            final String keySequence) {

        final String commandId = (String) commandIdsByUniqueName
                .get(commandName);
        String categoryId = null;
        if (commandId != null) {
            final ICommand command = commandManager.getCommand(commandId);
            try {
                categoryId = command.getCategoryId();
            } catch (final NotDefinedException e) {
                // Leave the category identifier as null.
            }
        }
        final String categoryName = (String) categoryUniqueNamesById
                .get(categoryId);

        editCommand(categoryName, commandName, keySequence);
    }

    /**
     * Allows the user to change the key bindings for a particular command.
     * Switches the tab to the modify tab, and then selects the category and
     * command that corresponds with the given category and command name. It
     * then selects the given key sequence and gives focus to the key sequence
     * text widget.
     * 
     * @param categoryName
     *            The name of the category for which the key bindings should be
     *            edited; if <code>null</code>, then just switch to the
     *            modify tab. If the <code>categoryName</code> is undefined or
     *            does not correspond to anything in the keys preference page,
     *            then this also just switches to the modify tab.
     * @param commandName
     *            The name of the command for which the key bindings should be
     *            edited; if <code>null</code>, then just switch to the
     *            modify tab. If the <code>commandName</code> is undefined or
     *            does not correspond to anything in the keys preference page,
     *            then this also just switches to the modify tab.
     * @param keySequence
     *            The key sequence for the selected item. If <code>null</code>,
     *            then just switch to the modify tab.
     */
    public final void editCommand(final String categoryName,
            final String commandName, final String keySequence) {
        // Switch to the modify tab.
        tabFolder.setSelection(TAB_INDEX_MODIFY);

        // If there is no command name, stop here.
        if ((commandName == null) || (categoryName == null)
                || (keySequence == null)) {
            return;
        }

        // Update the category combo box.
        final String[] categoryNames = comboCategory.getItems();
        int i = 0;
        for (; i < categoryNames.length; i++) {
            if (categoryName.equals(categoryNames[i]))
                break;
        }
        if (i >= comboCategory.getItemCount()) {
            // Couldn't find the category, so abort.
            return;
        }
        comboCategory.select(i);

        // Update the commands combo box.
        setCommandsForCategory();

        // Update the command combo box.
        final String[] commandNames = comboCommand.getItems();
        int j = 0;
        for (; j < commandNames.length; j++) {
            if (commandName.equals(commandNames[j]))
                break;
        }
        if (j >= comboCommand.getItemCount()) {
            // Couldn't find the command, so just select the first and then stop
            comboCommand.select(0);
            update();
            return;
        }
        comboCommand.select(j);
        
        /*
         * Update and validate the state of the modify tab in response to these
         * selection changes.
         */
        update();

        // Select the right key binding, if possible.
        final TableItem[] items = tableAssignmentsForCommand.getItems();
        int k = 0;
        for (; k < items.length; k++) {
            final String currentKeySequence = items[k].getText(2);
            if (keySequence.equals(currentKeySequence)) {
                break;
            }
        }
        if (k < tableAssignmentsForCommand.getItemCount()) {
            tableAssignmentsForCommand.select(k);
            tableAssignmentsForCommand.notifyListeners(SWT.Selection, null);
            textKeySequence.setFocus();
        }
    }

    private String getCategoryId() {
        return !commandIdsByCategoryId.containsKey(null)
                || comboCategory.getSelectionIndex() > 0 ? (String) categoryIdsByUniqueName
                .get(comboCategory.getText())
                : null;
    }

    private String getCommandId() {
        return (String) commandIdsByUniqueName.get(comboCommand.getText());
    }

    private String getContextId() {
        return comboContext.getSelectionIndex() >= 0 ? (String) contextIdsByUniqueName
                .get(comboContext.getText())
                : null;
    }

    private String getKeyConfigurationId() {
        return comboKeyConfiguration.getSelectionIndex() >= 0 ? (String) keyConfigurationIdsByUniqueName
                .get(comboKeyConfiguration.getText())
                : null;
    }

    private KeySequence getKeySequence() {
        return textKeySequenceManager.getKeySequence();
    }

    public void init(IWorkbench workbench) {
        IWorkbenchContextSupport workbenchContextSupport = workbench
                .getContextSupport();
        contextManager = workbenchContextSupport.getContextManager();
        // TODO remove blind cast
        commandManager = (MutableCommandManager) workbench.getCommandSupport()
                .getCommandManager();
        commandAssignments = new TreeSet();
        keySequenceAssignments = new TreeSet();
    }

    private void modifiedTextKeySequence() {
        update();
    }

    public final boolean performCancel() {
        // Save the selected tab for future reference.
        IPreferenceStore store = getPreferenceStore();
        store.setValue(IPreferenceConstants.KEYS_PREFERENCE_SELECTED_TAB,
                tabFolder.getSelectionIndex());
        return super.performCancel();
    }

    protected void performDefaults() {
        String activeKeyConfigurationId = getKeyConfigurationId();
        List preferenceKeySequenceBindingDefinitions = new ArrayList();
        KeySequenceBindingNode.getKeySequenceBindingDefinitions(tree,
                KeySequence.getInstance(), 0,
                preferenceKeySequenceBindingDefinitions);

        if (activeKeyConfigurationId != null
                || !preferenceKeySequenceBindingDefinitions.isEmpty()) {
            final String title = Util.translateString(RESOURCE_BUNDLE,
                    "restoreDefaultsMessageBoxText"); //$NON-NLS-1$
            final String message = Util.translateString(RESOURCE_BUNDLE,
                    "restoreDefaultsMessageBoxMessage"); //$NON-NLS-1$
            final boolean confirmed = MessageDialog.openConfirm(getShell(),
                    title, message);

            if (confirmed) {
                setKeyConfigurationId(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID);
                Iterator iterator = preferenceKeySequenceBindingDefinitions
                        .iterator();

                while (iterator.hasNext()) {
                    KeySequenceBindingDefinition keySequenceBindingDefinition = (KeySequenceBindingDefinition) iterator
                            .next();
                    KeySequenceBindingNode.remove(tree,
                            keySequenceBindingDefinition.getKeySequence(),
                            keySequenceBindingDefinition.getContextId(),
                            keySequenceBindingDefinition
                                    .getKeyConfigurationId(), 0,
                            keySequenceBindingDefinition.getPlatform(),
                            keySequenceBindingDefinition.getLocale(),
                            keySequenceBindingDefinition.getCommandId());
                }
            }
        }

        update();
    }

    public boolean performOk() {
        List preferenceActiveKeyConfigurationDefinitions = new ArrayList();
        preferenceActiveKeyConfigurationDefinitions
                .add(new ActiveKeyConfigurationDefinition(
                        getKeyConfigurationId(), null));
        PreferenceCommandRegistry preferenceCommandRegistry = (PreferenceCommandRegistry) commandManager
                .getMutableCommandRegistry();
        preferenceCommandRegistry
                .setActiveKeyConfigurationDefinitions(preferenceActiveKeyConfigurationDefinitions);
        List preferenceKeySequenceBindingDefinitions = new ArrayList();
        KeySequenceBindingNode.getKeySequenceBindingDefinitions(tree,
                KeySequence.getInstance(), 0,
                preferenceKeySequenceBindingDefinitions);
        preferenceCommandRegistry
                .setKeySequenceBindingDefinitions(preferenceKeySequenceBindingDefinitions);

        try {
            preferenceCommandRegistry.save();
        } catch (IOException eIO) {
            // Do nothing
        }
        
        // Save the selected tab for future reference.
        IPreferenceStore store = getPreferenceStore();
        store.setValue(IPreferenceConstants.KEYS_PREFERENCE_SELECTED_TAB,
                tabFolder.getSelectionIndex());
        return super.performOk();
    }

    private void selectAssignmentForCommand(String contextId) {
        if (tableAssignmentsForCommand.getSelectionCount() > 1)
            tableAssignmentsForCommand.deselectAll();

        int i = 0;
        int selection = -1;
        KeySequence keySequence = getKeySequence();

        for (Iterator iterator = commandAssignments.iterator(); iterator
                .hasNext(); i++) {
            CommandAssignment commandAssignment = (CommandAssignment) iterator
                    .next();

            if (Util.equals(contextId, commandAssignment.contextId)
                    && Util.equals(keySequence, commandAssignment.keySequence)) {
                selection = i;
                break;
            }
        }

        if (selection != tableAssignmentsForCommand.getSelectionIndex()) {
            if (selection == -1
                    || selection >= tableAssignmentsForCommand.getItemCount())
                tableAssignmentsForCommand.deselectAll();
            else
                tableAssignmentsForCommand.select(selection);
        }
    }

    private void selectAssignmentForKeySequence(String contextId) {
        if (tableAssignmentsForKeySequence.getSelectionCount() > 1)
            tableAssignmentsForKeySequence.deselectAll();

        int i = 0;
        int selection = -1;

        for (Iterator iterator = keySequenceAssignments.iterator(); iterator
                .hasNext(); i++) {
            KeySequenceAssignment keySequenceAssignment = (KeySequenceAssignment) iterator
                    .next();

            if (Util.equals(contextId, keySequenceAssignment.contextId)) {
                selection = i;
                break;
            }
        }

        if (selection != tableAssignmentsForKeySequence.getSelectionIndex()) {
            if (selection == -1
                    || selection >= tableAssignmentsForKeySequence
                            .getItemCount())
                tableAssignmentsForKeySequence.deselectAll();
            else
                tableAssignmentsForKeySequence.select(selection);
        }
    }

    private void selectedButtonAdd() {
        String commandId = getCommandId();
        String contextId = getContextId();
        String keyConfigurationId = getKeyConfigurationId();
        KeySequence keySequence = getKeySequence();
        KeySequenceBindingNode.remove(tree, keySequence, contextId,
                keyConfigurationId, 0, null, null);
        KeySequenceBindingNode.add(tree, keySequence, contextId,
                keyConfigurationId, 0, null, null, commandId);
        List preferenceKeySequenceBindingDefinitions = new ArrayList();
        KeySequenceBindingNode.getKeySequenceBindingDefinitions(tree,
                KeySequence.getInstance(), 0,
                preferenceKeySequenceBindingDefinitions);
        update();
    }

    /**
     * Provides a facility for exporting the viewable list of key bindings to a
     * file. Currently, this only supports exporting to a list of
     * comma-separated values. The user is prompted for which file should
     * receive our bounty.
     * 
     * @since 3.1
     */
    private final void selectedButtonExport() {
        FileDialog fileDialog = new FileDialog(getShell());
        fileDialog.setFilterExtensions(new String[] { "*.csv" }); //$NON-NLS-1$
        fileDialog.setFilterNames(new String[] { Util.translateString(
                RESOURCE_BUNDLE, "csvFilterName") }); //$NON-NLS-1$
        final String filePath = fileDialog.open();
        if (filePath == null) {
            return;
        }

        final SafeRunnable runnable = new SafeRunnable() {
            public final void run() throws IOException {
                Writer fileWriter = null;
                try {
                    fileWriter = new BufferedWriter(new FileWriter(filePath));
                    final TableItem[] items = tableKeyBindings.getItems();
                    final int numColumns = tableKeyBindings.getColumnCount();
                    for (int i = 0; i < items.length; i++) {
                        final TableItem item = items[i];
                        for (int j = 0; j < numColumns; j++) {
                            fileWriter.write(item.getText(j));
                            if (j < numColumns - 1) {
                                fileWriter.write(',');
                            }
                        }
                        fileWriter.write(System.getProperty("line.separator")); //$NON-NLS-1$
                    }

                } finally {
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (final IOException e) {
                            // At least I tried.
                        }
                    }

                }
            }
        };
        Platform.run(runnable);
    }

    private void selectedButtonRemove() {
        String contextId = getContextId();
        String keyConfigurationId = getKeyConfigurationId();
        KeySequence keySequence = getKeySequence();
        KeySequenceBindingNode.remove(tree, keySequence, contextId,
                keyConfigurationId, 0, null, null);
        KeySequenceBindingNode.add(tree, keySequence, contextId,
                keyConfigurationId, 0, null, null, null);
        List preferenceKeySequenceBindingDefinitions = new ArrayList();
        KeySequenceBindingNode.getKeySequenceBindingDefinitions(tree,
                KeySequence.getInstance(), 0,
                preferenceKeySequenceBindingDefinitions);
        update();
    }

    private void selectedButtonRestore() {
        String contextId = getContextId();
        String keyConfigurationId = getKeyConfigurationId();
        KeySequence keySequence = getKeySequence();
        KeySequenceBindingNode.remove(tree, keySequence, contextId,
                keyConfigurationId, 0, null, null);
        List preferenceKeySequenceBindingDefinitions = new ArrayList();
        KeySequenceBindingNode.getKeySequenceBindingDefinitions(tree,
                KeySequence.getInstance(), 0,
                preferenceKeySequenceBindingDefinitions);
        update();
    }

    private void selectedComboCategory() {
        update();
    }

    private void selectedComboCommand() {
        update();
    }

    private void selectedComboContext() {
        update();
    }

    private void selectedComboKeyConfiguration() {
        update();
    }

    private void selectedTableAssignmentsForCommand() {
        int selection = tableAssignmentsForCommand.getSelectionIndex();
        List commandAssignmentsAsList = new ArrayList(commandAssignments);

        if (selection >= 0 && selection < commandAssignmentsAsList.size()
                && tableAssignmentsForCommand.getSelectionCount() == 1) {
            CommandAssignment commandAssignment = (CommandAssignment) commandAssignmentsAsList
                    .get(selection);
            String contextId = commandAssignment.contextId;
            KeySequence keySequence = commandAssignment.keySequence;
            setContextId(contextId);
            setKeySequence(keySequence);
        }

        update();
    }

    private void selectedTableAssignmentsForKeySequence() {
        int selection = tableAssignmentsForKeySequence.getSelectionIndex();
        List keySequenceAssignmentsAsList = new ArrayList(
                keySequenceAssignments);

        if (selection >= 0 && selection < keySequenceAssignmentsAsList.size()
                && tableAssignmentsForKeySequence.getSelectionCount() == 1) {
            KeySequenceAssignment keySequenceAssignment = (KeySequenceAssignment) keySequenceAssignmentsAsList
                    .get(selection);
            String contextId = keySequenceAssignment.contextId;
            setContextId(contextId);
        }

        update();
    }

    /**
     * Responds to some kind of trigger on the View tab by taking the current
     * selection on the key bindings table and selecting the appropriate items
     * in the Modify tab.
     * @since 3.1
     */
    private final void selectedTableKeyBindings() {
        final int selectionIndex = tableKeyBindings.getSelectionIndex();
        if (selectionIndex != -1) {
            final TableItem item = tableKeyBindings.getItem(selectionIndex);
            final String categoryName = item
                    .getText(VIEW_CATEGORY_COLUMN_INDEX);
            final String commandName = item.getText(VIEW_COMMAND_COLUMN_INDEX);
            final String keySequence = item
                    .getText(VIEW_KEY_SEQUENCE_COLUMN_INDEX);
            editCommand(categoryName, commandName, keySequence);

        } else {
            editCommand(null, null, null);
        }
    }

    private void setAssignmentsForCommand() {
        commandAssignments.clear();
        String commandId = getCommandId();

        for (Iterator iterator = assignmentsByContextIdByKeySequence.entrySet()
                .iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            KeySequence keySequence = (KeySequence) entry.getKey();
            Map assignmentsByContextId = (Map) entry.getValue();

            if (assignmentsByContextId != null)
                for (Iterator iterator2 = assignmentsByContextId.entrySet()
                        .iterator(); iterator2.hasNext();) {
                    Map.Entry entry2 = (Map.Entry) iterator2.next();
                    CommandAssignment commandAssignment = new CommandAssignment();
                    commandAssignment.assignment = (KeySequenceBindingNode.Assignment) entry2
                            .getValue();
                    commandAssignment.contextId = (String) entry2.getKey();
                    commandAssignment.keySequence = keySequence;

                    if (commandAssignment.assignment.contains(commandId))
                        commandAssignments.add(commandAssignment);
                }
        }

        buildCommandAssignmentsTable();
    }

    private void setAssignmentsForKeySequence() {
        keySequenceAssignments.clear();
        KeySequence keySequence = getKeySequence();
        Map assignmentsByContextId = (Map) assignmentsByContextIdByKeySequence
                .get(keySequence);

        if (assignmentsByContextId != null)
            for (Iterator iterator = assignmentsByContextId.entrySet()
                    .iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                KeySequenceAssignment keySequenceAssignment = new KeySequenceAssignment();
                keySequenceAssignment.assignment = (KeySequenceBindingNode.Assignment) entry
                        .getValue();
                keySequenceAssignment.contextId = (String) entry.getKey();
                keySequenceAssignments.add(keySequenceAssignment);
            }

        buildKeySequenceAssignmentsTable();
    }

    private void setCommandId(String commandId) {
        comboCommand.clearSelection();
        comboCommand.deselectAll();
        String commandUniqueName = (String) commandUniqueNamesById
                .get(commandId);

        if (commandUniqueName != null) {
            String items[] = comboCommand.getItems();

            for (int i = 0; i < items.length; i++)
                if (commandUniqueName.equals(items[i])) {
                    comboCommand.select(i);
                    break;
                }
        }
    }

    private void setCommandsForCategory() {
        String categoryId = getCategoryId();
        String commandId = getCommandId();
        Set commandIds = (Set) commandIdsByCategoryId.get(categoryId);
        Map commandIdsByName = new HashMap(commandIdsByUniqueName);
        if (commandIds == null) {
            commandIdsByName = new HashMap();
        } else {
            commandIdsByName.values().retainAll(commandIds);
        }
        List commandNames = new ArrayList(commandIdsByName.keySet());
        Collections.sort(commandNames, Collator.getInstance());
        comboCommand.setItems((String[]) commandNames
                .toArray(new String[commandNames.size()]));
        setCommandId(commandId);

        if (comboCommand.getSelectionIndex() == -1 && !commandNames.isEmpty())
            comboCommand.select(0);
    }

    /**
     * Changes the selected context name in the context combo box. The context
     * selected is either the one matching the identifier provided (if
     * possible), or the default context identifier. If no matching name can be
     * found in the combo, then the first item is selected.
     * 
     * @param contextId
     *            The context identifier for the context to be selected in the
     *            combo box; may be <code>null</code>.
     */
    private void setContextId(String contextId) {
        // Clear the current selection.
        comboContext.clearSelection();
        comboContext.deselectAll();

        // Figure out which name to look for.
        String contextName = (String) contextUniqueNamesById.get(contextId);
        if (contextName == null) {
            contextName = (String) contextUniqueNamesById
                    .get(KeySequenceBinding.DEFAULT_CONTEXT_ID);
        }
        if (contextName == null) {
            contextName = Util.ZERO_LENGTH_STRING;
        }

        // Scan the list for the selection we're looking for.
        final String[] items = comboContext.getItems();
        boolean found = false;
        for (int i = 0; i < items.length; i++) {
            if (contextName.equals(items[i])) {
                comboContext.select(i);
                found = true;
                break;
            }
        }

        // If we didn't find an item, then set the first item as selected.
        if ((!found) && (items.length > 0)) {
            comboContext.select(0);
        }
    }

    private void setContextsForCommand() {
        String commandId = getCommandId();
        String contextId = getContextId();
        Map contextIdsByName = new HashMap(contextIdsByUniqueName);

        List contextNames = new ArrayList(contextIdsByName.keySet());
        Collections.sort(contextNames, Collator.getInstance());

        comboContext.setItems((String[]) contextNames
                .toArray(new String[contextNames.size()]));
        setContextId(contextId);

        if (comboContext.getSelectionIndex() == -1 && !contextNames.isEmpty())
            comboContext.select(0);
    }

    private void setKeyConfigurationId(String keyConfigurationId) {
        comboKeyConfiguration.clearSelection();
        comboKeyConfiguration.deselectAll();
        String keyConfigurationUniqueName = (String) keyConfigurationUniqueNamesById
                .get(keyConfigurationId);

        if (keyConfigurationUniqueName != null) {
            String items[] = comboKeyConfiguration.getItems();

            for (int i = 0; i < items.length; i++)
                if (keyConfigurationUniqueName.equals(items[i])) {
                    comboKeyConfiguration.select(i);
                    break;
                }
        }
    }

    private void setKeySequence(KeySequence keySequence) {
        textKeySequenceManager.setKeySequence(keySequence);
    }

    public void setVisible(boolean visible) {
        if (visible == true) {
            Map contextsByName = new HashMap();

            for (Iterator iterator = contextManager.getDefinedContextIds()
                    .iterator(); iterator.hasNext();) {
                IContext context = contextManager.getContext((String) iterator
                        .next());

                try {
                    String name = context.getName();
                    Collection contexts = (Collection) contextsByName.get(name);

                    if (contexts == null) {
                        contexts = new HashSet();
                        contextsByName.put(name, contexts);
                    }

                    contexts.add(context);
                } catch (org.eclipse.ui.contexts.NotDefinedException eNotDefined) {
                    // Do nothing
                }
            }

            Map categoriesByName = new HashMap();

            for (Iterator iterator = commandManager.getDefinedCategoryIds()
                    .iterator(); iterator.hasNext();) {
                ICategory category = commandManager
                        .getCategory((String) iterator.next());

                try {
                    String name = category.getName();
                    Collection categories = (Collection) categoriesByName
                            .get(name);

                    if (categories == null) {
                        categories = new HashSet();
                        categoriesByName.put(name, categories);
                    }

                    categories.add(category);
                } catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
                    // Do nothing
                }
            }

            Map commandsByName = new HashMap();

            for (Iterator iterator = commandManager.getDefinedCommandIds()
                    .iterator(); iterator.hasNext();) {
                ICommand command = commandManager.getCommand((String) iterator
                        .next());

                try {
                    String name = command.getName();
                    Collection commands = (Collection) commandsByName.get(name);

                    if (commands == null) {
                        commands = new HashSet();
                        commandsByName.put(name, commands);
                    }

                    commands.add(command);
                } catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
                    // Do nothing
                }
            }

            Map keyConfigurationsByName = new HashMap();

            for (Iterator iterator = commandManager
                    .getDefinedKeyConfigurationIds().iterator(); iterator
                    .hasNext();) {
                IKeyConfiguration keyConfiguration = commandManager
                        .getKeyConfiguration((String) iterator.next());

                try {
                    String name = keyConfiguration.getName();
                    Collection keyConfigurations = (Collection) keyConfigurationsByName
                            .get(name);

                    if (keyConfigurations == null) {
                        keyConfigurations = new HashSet();
                        keyConfigurationsByName.put(name, keyConfigurations);
                    }

                    keyConfigurations.add(keyConfiguration);
                } catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
                    // Do nothing
                }
            }

            contextIdsByUniqueName = new HashMap();
            contextUniqueNamesById = new HashMap();

            for (Iterator iterator = contextsByName.entrySet().iterator(); iterator
                    .hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                Set contexts = (Set) entry.getValue();
                Iterator iterator2 = contexts.iterator();

                if (contexts.size() == 1) {
                    IContext context = (IContext) iterator2.next();
                    contextIdsByUniqueName.put(name, context.getId());
                    contextUniqueNamesById.put(context.getId(), name);
                } else
                    while (iterator2.hasNext()) {
                        IContext context = (IContext) iterator2.next();
                        String uniqueName = MessageFormat.format(
                                Util.translateString(RESOURCE_BUNDLE,
                                        "uniqueName"), new Object[] { name, //$NON-NLS-1$
                                        context.getId() });
                        contextIdsByUniqueName.put(uniqueName, context.getId());
                        contextUniqueNamesById.put(context.getId(), uniqueName);
                    }
            }

            categoryIdsByUniqueName = new HashMap();
            categoryUniqueNamesById = new HashMap();

            for (Iterator iterator = categoriesByName.entrySet().iterator(); iterator
                    .hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                Set categories = (Set) entry.getValue();
                Iterator iterator2 = categories.iterator();

                if (categories.size() == 1) {
                    ICategory category = (ICategory) iterator2.next();
                    categoryIdsByUniqueName.put(name, category.getId());
                    categoryUniqueNamesById.put(category.getId(), name);
                } else
                    while (iterator2.hasNext()) {
                        ICategory category = (ICategory) iterator2.next();
                        String uniqueName = MessageFormat.format(
                                Util.translateString(RESOURCE_BUNDLE,
                                        "uniqueName"), new Object[] { name, //$NON-NLS-1$
                                        category.getId() });
                        categoryIdsByUniqueName.put(uniqueName, category
                                .getId());
                        categoryUniqueNamesById.put(category.getId(),
                                uniqueName);
                    }
            }

            commandIdsByUniqueName = new HashMap();
            commandUniqueNamesById = new HashMap();

            for (Iterator iterator = commandsByName.entrySet().iterator(); iterator
                    .hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                Set commands = (Set) entry.getValue();
                Iterator iterator2 = commands.iterator();

                if (commands.size() == 1) {
                    ICommand command = (ICommand) iterator2.next();
                    commandIdsByUniqueName.put(name, command.getId());
                    commandUniqueNamesById.put(command.getId(), name);
                } else
                    while (iterator2.hasNext()) {
                        ICommand command = (ICommand) iterator2.next();
                        String uniqueName = MessageFormat.format(
                                Util.translateString(RESOURCE_BUNDLE,
                                        "uniqueName"), new Object[] { name, //$NON-NLS-1$
                                        command.getId() });
                        commandIdsByUniqueName.put(uniqueName, command.getId());
                        commandUniqueNamesById.put(command.getId(), uniqueName);
                    }
            }

            keyConfigurationIdsByUniqueName = new HashMap();
            keyConfigurationUniqueNamesById = new HashMap();

            for (Iterator iterator = keyConfigurationsByName.entrySet()
                    .iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                Set keyConfigurations = (Set) entry.getValue();
                Iterator iterator2 = keyConfigurations.iterator();

                if (keyConfigurations.size() == 1) {
                    IKeyConfiguration keyConfiguration = (IKeyConfiguration) iterator2
                            .next();
                    keyConfigurationIdsByUniqueName.put(name, keyConfiguration
                            .getId());
                    keyConfigurationUniqueNamesById.put(keyConfiguration
                            .getId(), name);
                } else
                    while (iterator2.hasNext()) {
                        IKeyConfiguration keyConfiguration = (IKeyConfiguration) iterator2
                                .next();
                        String uniqueName = MessageFormat.format(
                                Util.translateString(RESOURCE_BUNDLE,
                                        "uniqueName"), new Object[] { name, //$NON-NLS-1$
                                        keyConfiguration.getId() });
                        keyConfigurationIdsByUniqueName.put(uniqueName,
                                keyConfiguration.getId());
                        keyConfigurationUniqueNamesById.put(keyConfiguration
                                .getId(), uniqueName);
                    }
            }

            String activeKeyConfigurationId = commandManager
                    .getActiveKeyConfigurationId();
            commandIdsByCategoryId = new HashMap();

            for (Iterator iterator = commandManager.getDefinedCommandIds()
                    .iterator(); iterator.hasNext();) {
                ICommand command = commandManager.getCommand((String) iterator
                        .next());

                try {
                    String categoryId = command.getCategoryId();
                    Collection commandIds = (Collection) commandIdsByCategoryId
                            .get(categoryId);

                    if (commandIds == null) {
                        commandIds = new HashSet();
                        commandIdsByCategoryId.put(categoryId, commandIds);
                    }

                    commandIds.add(command.getId());
                } catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
                    // Do nothing
                }
            }

            ICommandRegistry commandRegistry = commandManager
                    .getCommandRegistry();
            ICommandRegistry mutableCommandRegistry = commandManager
                    .getMutableCommandRegistry();

            List pluginKeySequenceBindingDefinitions = new ArrayList(
                    commandRegistry.getKeySequenceBindingDefinitions());

            for (Iterator iterator = pluginKeySequenceBindingDefinitions
                    .iterator(); iterator.hasNext();) {
                KeySequenceBindingDefinition keySequenceBindingDefinition = (KeySequenceBindingDefinition) iterator
                        .next();
                KeySequence keySequence = keySequenceBindingDefinition
                        .getKeySequence();
                String commandId = keySequenceBindingDefinition.getCommandId();
                String contextId = keySequenceBindingDefinition.getContextId();
                String keyConfigurationId = keySequenceBindingDefinition
                        .getKeyConfigurationId();
                boolean validKeySequence = keySequence != null
                        && MutableCommandManager
                                .validateKeySequence(keySequence);
                boolean validContextId = contextId == null
                        || contextManager.getDefinedContextIds().contains(
                                contextId);
                boolean validCommandId = commandId == null
                        || commandManager.getDefinedCommandIds().contains(
                                commandId);
                boolean validKeyConfigurationId = keyConfigurationId == null
                        || commandManager.getDefinedKeyConfigurationIds()
                                .contains(keyConfigurationId);

                if (!validKeySequence || !validCommandId || !validContextId
                        || !validKeyConfigurationId)
                    iterator.remove();
            }

            List preferenceKeySequenceBindingDefinitions = new ArrayList(
                    mutableCommandRegistry.getKeySequenceBindingDefinitions());

            for (Iterator iterator = preferenceKeySequenceBindingDefinitions
                    .iterator(); iterator.hasNext();) {
                KeySequenceBindingDefinition keySequenceBindingDefinition = (KeySequenceBindingDefinition) iterator
                        .next();
                KeySequence keySequence = keySequenceBindingDefinition
                        .getKeySequence();
                String commandId = keySequenceBindingDefinition.getCommandId();
                String contextId = keySequenceBindingDefinition.getContextId();
                String keyConfigurationId = keySequenceBindingDefinition
                        .getKeyConfigurationId();
                boolean validKeySequence = keySequence != null
                        && MutableCommandManager
                                .validateKeySequence(keySequence);
                boolean validContextId = contextId == null
                        || contextManager.getDefinedContextIds().contains(
                                contextId);
                boolean validCommandId = commandId == null
                        || commandManager.getDefinedCommandIds().contains(
                                commandId);
                boolean validKeyConfigurationId = keyConfigurationId == null
                        || commandManager.getDefinedKeyConfigurationIds()
                                .contains(keyConfigurationId);

                if (!validKeySequence || !validCommandId || !validContextId
                        || !validKeyConfigurationId)
                    iterator.remove();
            }

            tree = new TreeMap();

            for (Iterator iterator = pluginKeySequenceBindingDefinitions
                    .iterator(); iterator.hasNext();) {
                KeySequenceBindingDefinition keySequenceBindingDefinition = (KeySequenceBindingDefinition) iterator
                        .next();
                KeySequenceBindingNode.add(tree, keySequenceBindingDefinition
                        .getKeySequence(), keySequenceBindingDefinition
                        .getContextId(), keySequenceBindingDefinition
                        .getKeyConfigurationId(), 1,
                        keySequenceBindingDefinition.getPlatform(),
                        keySequenceBindingDefinition.getLocale(),
                        keySequenceBindingDefinition.getCommandId());
            }

            for (Iterator iterator = preferenceKeySequenceBindingDefinitions
                    .iterator(); iterator.hasNext();) {
                KeySequenceBindingDefinition keySequenceBindingDefinition = (KeySequenceBindingDefinition) iterator
                        .next();
                KeySequenceBindingNode.add(tree, keySequenceBindingDefinition
                        .getKeySequence(), keySequenceBindingDefinition
                        .getContextId(), keySequenceBindingDefinition
                        .getKeyConfigurationId(), 0,
                        keySequenceBindingDefinition.getPlatform(),
                        keySequenceBindingDefinition.getLocale(),
                        keySequenceBindingDefinition.getCommandId());
            }

            // TODO?
            //HashSet categoryIdsReferencedByCommandDefinitions = new
            // HashSet();
            //categoryDefinitionsById.keySet().retainAll(categoryIdsReferencedByCommandDefinitions);

            /*
             * TODO rich client platform. simplify UI if possible boolean
             * showCategory = !categoryIdsByUniqueName.isEmpty();
             * labelCategory.setVisible(showCategory);
             * comboCategory.setVisible(showCategory); boolean showContext =
             * !contextIdsByUniqueName.isEmpty();
             * labelContext.setVisible(showContext);
             * comboContext.setVisible(showContext);
             * labelContextExtends.setVisible(showContext); boolean
             * showKeyConfiguration =
             * !keyConfigurationIdsByUniqueName.isEmpty();
             * labelKeyConfiguration.setVisible(showKeyConfiguration);
             * comboKeyConfiguration.setVisible(showKeyConfiguration);
             * labelKeyConfigurationExtends.setVisible(showKeyConfiguration);
             */

            List categoryNames = new ArrayList(categoryIdsByUniqueName.keySet());
            Collections.sort(categoryNames, Collator.getInstance());

            if (commandIdsByCategoryId.containsKey(null))
                categoryNames.add(0, Util.translateString(RESOURCE_BUNDLE,
                        "other")); //$NON-NLS-1$

            comboCategory.setItems((String[]) categoryNames
                    .toArray(new String[categoryNames.size()]));
            comboCategory.clearSelection();
            comboCategory.deselectAll();

            if (commandIdsByCategoryId.containsKey(null)
                    || !categoryNames.isEmpty())
                comboCategory.select(0);

            List keyConfigurationNames = new ArrayList(
                    keyConfigurationIdsByUniqueName.keySet());
            Collections.sort(keyConfigurationNames, Collator.getInstance());
            comboKeyConfiguration.setItems((String[]) keyConfigurationNames
                    .toArray(new String[keyConfigurationNames.size()]));
            setKeyConfigurationId(activeKeyConfigurationId);
            update();
        }

        super.setVisible(visible);
    }

    private void update() {
        updateViewTab();
        setCommandsForCategory();
        setContextsForCommand();
        String keyConfigurationId = getKeyConfigurationId();
        KeySequence keySequence = getKeySequence();
        String[] activeKeyConfigurationIds = MutableCommandManager
                .extend(commandManager
                        .getKeyConfigurationIds(keyConfigurationId));
        String[] activeLocales = MutableCommandManager
                .extend(MutableCommandManager.getPath(commandManager
                        .getActiveLocale(), MutableCommandManager.SEPARATOR));
        String[] activePlatforms = MutableCommandManager
                .extend(MutableCommandManager.getPath(commandManager
                        .getActivePlatform(), MutableCommandManager.SEPARATOR));
        KeySequenceBindingNode.solve(tree, activeKeyConfigurationIds,
                activePlatforms, activeLocales);
        assignmentsByContextIdByKeySequence = KeySequenceBindingNode
                .getAssignmentsByContextIdKeySequence(tree, KeySequence
                        .getInstance());
        setAssignmentsForKeySequence();
        setAssignmentsForCommand();
        String commandId = getCommandId();
        String contextId = getContextId();
        selectAssignmentForKeySequence(contextId);
        selectAssignmentForCommand(contextId);
        updateLabelKeyConfigurationExtends();
        updateLabelContextExtends();
        labelAssignmentsForKeySequence.setEnabled(keySequence != null
                && !keySequence.getKeyStrokes().isEmpty());
        tableAssignmentsForKeySequence.setEnabled(keySequence != null
                && !keySequence.getKeyStrokes().isEmpty());
        labelAssignmentsForCommand.setEnabled(commandId != null);
        tableAssignmentsForCommand.setEnabled(commandId != null);
        boolean buttonsEnabled = commandId != null && keySequence != null
                && !keySequence.getKeyStrokes().isEmpty();
        boolean buttonAddEnabled = buttonsEnabled;
        boolean buttonRemoveEnabled = buttonsEnabled;
        boolean buttonRestoreEnabled = buttonsEnabled;
        // TODO better button enablement
        buttonAdd.setEnabled(buttonAddEnabled);
        buttonRemove.setEnabled(buttonRemoveEnabled);
        buttonRestore.setEnabled(buttonRestoreEnabled);
    }

    private void updateLabelContextExtends() {
        String contextId = getContextId();

        if (contextId != null) {
            IContext context = contextManager.getContext(getContextId());

            if (context.isDefined()) {
                try {
                    String parentId = context.getParentId();
                    if (parentId != null) {
                        String name = (String) contextUniqueNamesById
                                .get(parentId);

                        if (name != null) {
                            labelContextExtends.setText(MessageFormat.format(
                                    Util.translateString(RESOURCE_BUNDLE,
                                            "extends"), //$NON-NLS-1$
                                    new Object[] { name }));

                            return;
                        }
                    }
                } catch (org.eclipse.ui.contexts.NotDefinedException eNotDefined) {
                    // Do nothing
                }
            }
        }

        labelContextExtends.setText(Util.ZERO_LENGTH_STRING);
    }

    private void updateLabelKeyConfigurationExtends() {
        String keyConfigurationId = getKeyConfigurationId();

        if (keyConfigurationId != null) {
            IKeyConfiguration keyConfiguration = commandManager
                    .getKeyConfiguration(keyConfigurationId);

            try {
                String name = (String) keyConfigurationUniqueNamesById
                        .get(keyConfiguration.getParentId());

                if (name != null) {
                    labelKeyConfigurationExtends.setText(MessageFormat.format(
                            Util.translateString(RESOURCE_BUNDLE, "extends"), //$NON-NLS-1$
                            new Object[] { name }));
                    return;
                }
            } catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
                // Do nothing
            }
        }

        labelKeyConfigurationExtends.setText(Util.ZERO_LENGTH_STRING);
    }

    /**
     * Updates the contents of the view tab. This queries the command manager
     * for a list of key sequence binding definitions, and these definitions are
     * then added to the table.
     * 
     * @since 3.1
     */
    private final void updateViewTab() {
        // Clear out the existing table contents.
        tableKeyBindings.removeAll();

        // Get a sorted list of key binding contents.
        final List bindings = new ArrayList(commandManager.getKeyBindings());
        Collections.sort(bindings, new Comparator() {
            /**
             * Compares two instances of <code>KeySequenceBindingMachine</code>
             * based on the current sort order.
             * 
             * @param object1
             *            The first object to compare; must be an instance of
             *            <code>KeySequenceBindingDefinition</code> (i.e., not
             *            <code>null</code>).
             * @param object2
             *            The second object to compare; must be an instance of
             *            <code>KeySequenceBindingDefinition</code> (i.e., not
             *            <code>null</code>).
             * @return The integer value representing the comparison. The
             *         comparison is based on the current sort order.
             * @since 3.1
             */
            public final int compare(final Object object1, final Object object2) {
                final KeySequenceBindingDefinition binding1 = (KeySequenceBindingDefinition) object1;
                final KeySequenceBindingDefinition binding2 = (KeySequenceBindingDefinition) object2;

                /*
                 * Get the category name, command name, formatted key sequence
                 * and context name for the first binding.
                 */
                final String commandId1 = binding1.getCommandId();
                String categoryName1 = Util.ZERO_LENGTH_STRING;
                String commandName1 = Util.ZERO_LENGTH_STRING;
                if (commandId1 != null) {
                    final ICommand command = commandManager
                            .getCommand(commandId1);
                    try {
                        commandName1 = command.getName();
                        final String categoryId = command.getCategoryId();
                        if (categoryId != null) {
                            categoryName1 = commandManager.getCategory(
                                    categoryId).getName();
                        }
                    } catch (final NotDefinedException e) {
                        // Just use the zero-length string.
                    }
                }
                final String keySequence1 = binding1.getKeySequence().format();
                final String contextId1 = binding1.getContextId();
                String contextName1 = Util.ZERO_LENGTH_STRING;
                if (contextId1 != null) {
                    final IContext context = contextManager
                            .getContext(contextId1);
                    try {
                        contextName1 = context.getName();
                    } catch (final org.eclipse.ui.contexts.NotDefinedException e) {
                        // Just use the zero-length string.
                    }
                }

                /*
                 * Get the category name, command name, formatted key sequence
                 * and context name for the first binding.
                 */
                final String commandId2 = binding2.getCommandId();
                String categoryName2 = Util.ZERO_LENGTH_STRING;
                String commandName2 = Util.ZERO_LENGTH_STRING;
                if (commandId2 != null) {
                    final ICommand command = commandManager
                            .getCommand(commandId2);
                    try {
                        commandName2 = command.getName();
                        final String categoryId = command.getCategoryId();
                        if (categoryId != null) {
                            categoryName2 = commandManager.getCategory(
                                    categoryId).getName();
                        }
                    } catch (final NotDefinedException e) {
                        // Just use the zero-length string.
                    }
                }
                final String keySequence2 = binding2.getKeySequence().format();
                final String contextId2 = binding2.getContextId();
                String contextName2 = Util.ZERO_LENGTH_STRING;
                if (contextId2 != null) {
                    final IContext context = contextManager
                            .getContext(contextId2);
                    try {
                        contextName2 = context.getName();
                    } catch (final org.eclipse.ui.contexts.NotDefinedException e) {
                        // Just use the zero-length string.
                    }
                }

                // Compare the items in the current sort order.
                int compare = 0;
                for (int i = 0; i < sortOrder.length; i++) {
                    switch (sortOrder[i]) {
                    case VIEW_CATEGORY_COLUMN_INDEX:
                        compare = Util.compare(categoryName1, categoryName2);
                        if (compare != 0) {
                            return compare;
                        }
                        break;
                    case VIEW_COMMAND_COLUMN_INDEX:
                        compare = Util.compare(commandName1, commandName2);
                        if (compare != 0) {
                            return compare;
                        }
                        break;
                    case VIEW_KEY_SEQUENCE_COLUMN_INDEX:
                        compare = Util.compare(keySequence1, keySequence2);
                        if (compare != 0) {
                            return compare;
                        }
                        break;
                    case VIEW_CONTEXT_COLUMN_INDEX:
                        compare = Util.compare(contextName1, contextName2);
                        if (compare != 0) {
                            return compare;
                        }
                        break;
                    default:
                        throw new Error(
                                "Programmer error: added another sort column without modifying the comparator."); //$NON-NLS-1$
                    }
                }

                return compare;
            }

            /**
             * @see Object#equals(java.lang.Object)
             */
            public final boolean equals(final Object object) {
                return super.equals(object);
            }
        });

        // Add a table item for each item in the list.
        final Iterator keyBindingItr = bindings.iterator();
        while (keyBindingItr.hasNext()) {
            final KeySequenceBindingDefinition keyBinding = (KeySequenceBindingDefinition) keyBindingItr
                    .next();

            // Get the command and category name.
            final String commandId = keyBinding.getCommandId();
            String commandName = Util.ZERO_LENGTH_STRING;
            String categoryName = Util.ZERO_LENGTH_STRING;
            if (commandId != null) {
                final ICommand command = commandManager.getCommand(commandId);
                try {
                    commandName = command.getName();
                    final String categoryId = command.getCategoryId();
                    if (categoryId != null) {
                        categoryName = commandManager.getCategory(categoryId)
                                .getName();
                    }
                } catch (final NotDefinedException e) {
                    // Just use the zero-length string.
                }
            }

            // Ignore items with a meaningless command name.
            if ((commandName == null) || (commandName.length() == 0)) {
                continue;
            }

            // Get the context name.
            final String contextId = keyBinding.getContextId();
            String contextName = Util.ZERO_LENGTH_STRING;
            if (contextId != null) {
                final IContext context = contextManager.getContext(contextId);
                try {
                    contextName = context.getName();
                } catch (final org.eclipse.ui.contexts.NotDefinedException e) {
                    // Just use the zero-length string.
                }
            }

            // Create the table item.
            final TableItem item = new TableItem(tableKeyBindings, SWT.NONE);
            item.setText(VIEW_CATEGORY_COLUMN_INDEX, categoryName);
            item.setText(VIEW_COMMAND_COLUMN_INDEX, commandName);
            item.setText(VIEW_KEY_SEQUENCE_COLUMN_INDEX, keyBinding
                    .getKeySequence().format());
            item.setText(VIEW_CONTEXT_COLUMN_INDEX, contextName);
        }

        // Pack the columns.
        for (int i = 0; i < tableKeyBindings.getColumnCount(); i++) {
            tableKeyBindings.getColumn(i).pack();
        }
    }

    /*
     * private void selectedButtonChange() { KeySequence keySequence =
     * getKeySequence(); boolean validKeySequence = keySequence != null &&
     * validateSequence(keySequence); String scopeId = getScopeId(); boolean
     * validScopeId = scopeId != null && contextsDefinitionsById.get(scopeId) !=
     * null; String keyConfigurationId = getKeyConfigurationId(); boolean
     * validKeyConfigurationId = keyConfigurationId != null &&
     * keyConfigurationsById.get(keyConfigurationId) != null; if
     * (validKeySequence && validScopeId && validKeyConfigurationId) { String
     * commandId = null; ISelection selection =
     * treeViewerCommands.getSelection(); if (selection instanceof
     * IStructuredSelection && !selection.isEmpty()) { Object object =
     * ((IStructuredSelection) selection).getFirstElement(); if (object
     * instanceof ICommandDefinition) commandId = ((ICommandDefinition)
     * object).getId(); } CommandRecord commandRecord =
     * getSelectedCommandRecord(); if (commandRecord == null) set(tree,
     * keySequence, scopeId, keyConfigurationId, commandId); else { if
     * (!commandRecord.customSet.isEmpty()) clear(tree, keySequence, scopeId,
     * keyConfigurationId); else set(tree, keySequence, scopeId,
     * keyConfigurationId, null); } commandRecords.clear();
     * buildCommandRecords(tree, commandId, commandRecords);
     * buildTableCommand(); selectTableCommand(scopeId, keyConfigurationId,
     * keySequence); keySequenceRecords.clear(); buildSequenceRecords(tree,
     * keySequence, keySequenceRecords); buildTableKeySequence();
     * selectTableKeySequence(scopeId, keyConfigurationId); update(); } }
     * private void buildTableCommand() { tableSequencesForCommand.removeAll();
     * for (int i = 0; i < commandRecords.size(); i++) { CommandRecord
     * commandRecord = (CommandRecord) commandRecords.get(i); Set customSet =
     * commandRecord.customSet; Set defaultSet = commandRecord.defaultSet; int
     * difference = DIFFERENCE_NONE; //String commandId = null; // // boolean
     * commandConflict = false; String alternateCommandId = null; boolean
     * alternateCommandConflict = false; if (customSet.isEmpty()) { if
     * (defaultSet.contains(commandRecord.command)) { //commandId // // =
     * commandRecord.commandId; commandConflict = commandRecord.defaultConflict; } }
     * else { if (defaultSet.isEmpty()) { if
     * (customSet.contains(commandRecord.command)) { difference =
     * DIFFERENCE_ADD; //commandId = // // commandRecord.commandId; // //
     * commandConflict = commandRecord.customConflict; } } else { if
     * (customSet.contains(commandRecord.command)) { difference =
     * DIFFERENCE_CHANGE; //commandId = // // commandRecord.commandId;
     * commandConflict = commandRecord.customConflict; alternateCommandId =
     * commandRecord.defaultCommand; alternateCommandConflict =
     * commandRecord.defaultConflict; } else { if
     * (defaultSet.contains(commandRecord.command)) { difference =
     * DIFFERENCE_MINUS; //commandId = // // commandRecord.commandId; // //
     * commandConflict = commandRecord.defaultConflict; alternateCommandId =
     * commandRecord.customCommand; alternateCommandConflict =
     * commandRecord.customConflict; } } } } TableItem tableItem = new
     * TableItem(tableSequencesForCommand, SWT.NULL); switch (difference) { case
     * DIFFERENCE_ADD : tableItem.setImage(0, IMAGE_PLUS); break; case
     * DIFFERENCE_CHANGE : tableItem.setImage(0, IMAGE_CHANGE); break; case
     * DIFFERENCE_MINUS : tableItem.setImage(0, IMAGE_MINUS); break; case
     * DIFFERENCE_NONE : tableItem.setImage(0, IMAGE_BLANK); break; }
     * IContextDefinition scope = (IContextDefinition)
     * contextsById.get(commandRecord.scope); tableItem.setText(1, scope != null ?
     * scope.getName() : bracket(commandRecord.scope)); Configuration
     * keyConfiguration = (Configuration)
     * keyConfigurationsById.get(commandRecord.configuration);
     * tableItem.setText(2, keyConfiguration != null ?
     * keyConfiguration.getName() : bracket(commandRecord.configuration));
     * boolean conflict = commandConflict || alternateCommandConflict;
     * StringBuffer stringBuffer = new StringBuffer(); if
     * (commandRecord.sequence != null)
     * stringBuffer.append(KeySupport.formatSequence(commandRecord.sequence,
     * true)); if (commandConflict) stringBuffer.append(SPACE +
     * COMMAND_CONFLICT); String alternateCommandName = null; if
     * (alternateCommandId == null) alternateCommandName = COMMAND_UNDEFINED;
     * else if (alternateCommandId.length() == 0) alternateCommandName =
     * COMMAND_NOTHING; else { ICommandDefinition command = (ICommandDefinition)
     * commandsById.get(alternateCommandId); if (command != null)
     * alternateCommandName = command.getName(); else alternateCommandName =
     * bracket(alternateCommandId); } if (alternateCommandConflict)
     * alternateCommandName += SPACE + COMMAND_CONFLICT;
     * stringBuffer.append(SPACE); if (difference == DIFFERENCE_CHANGE)
     * stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle,
     * "was"), new Object[] { alternateCommandName })); //$NON-NLS-1$ else if
     * (difference == DIFFERENCE_MINUS)
     * stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle,
     * "now"), new Object[] { alternateCommandName })); //$NON-NLS-1$
     * tableItem.setText(3, stringBuffer.toString()); if (difference ==
     * DIFFERENCE_MINUS) { if (conflict) tableItem.setForeground(new
     * Color(getShell().getDisplay(), RGB_CONFLICT_MINUS)); else
     * tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS)); }
     * else if (conflict) tableItem.setForeground(new
     * Color(getShell().getDisplay(), RGB_CONFLICT)); } } private void
     * buildTableKeySequence() { tableCommandsForSequence.removeAll(); for (int
     * i = 0; i < keySequenceRecords.size(); i++) { KeySequenceRecord
     * keySequenceRecord = (KeySequenceRecord) keySequenceRecords.get(i); int
     * difference = DIFFERENCE_NONE; String commandId = null; boolean
     * commandConflict = false; String alternateCommandId = null; boolean
     * alternateCommandConflict = false; if
     * (keySequenceRecord.customSet.isEmpty()) { commandId =
     * keySequenceRecord.defaultCommand; commandConflict =
     * keySequenceRecord.defaultConflict; } else { commandId =
     * keySequenceRecord.customCommand; commandConflict =
     * keySequenceRecord.customConflict; if
     * (keySequenceRecord.defaultSet.isEmpty()) difference = DIFFERENCE_ADD;
     * else { difference = DIFFERENCE_CHANGE; alternateCommandId =
     * keySequenceRecord.defaultCommand; alternateCommandConflict =
     * keySequenceRecord.defaultConflict; } } TableItem tableItem = new
     * TableItem(tableCommandsForSequence, SWT.NULL); switch (difference) { case
     * DIFFERENCE_ADD : tableItem.setImage(0, IMAGE_PLUS); break; case
     * DIFFERENCE_CHANGE : tableItem.setImage(0, IMAGE_CHANGE); break; case
     * DIFFERENCE_MINUS : tableItem.setImage(0, IMAGE_MINUS); break; case
     * DIFFERENCE_NONE : tableItem.setImage(0, IMAGE_BLANK); break; }
     * IContextDefinition scope = (IContextDefinition)
     * contextsById.get(keySequenceRecord.scope); tableItem.setText(1, scope !=
     * null ? scope.getName() : bracket(keySequenceRecord.scope)); Configuration
     * keyConfiguration = (Configuration)
     * keyConfigurationsById.get(keySequenceRecord.configuration);
     * tableItem.setText(2, keyConfiguration != null ?
     * keyConfiguration.getName() : bracket(keySequenceRecord.configuration));
     * boolean conflict = commandConflict || alternateCommandConflict;
     * StringBuffer stringBuffer = new StringBuffer(); String commandName =
     * null; if (commandId == null) commandName = COMMAND_UNDEFINED; else if
     * (commandId.length() == 0) commandName = COMMAND_NOTHING; else {
     * ICommandDefinition command = (ICommandDefinition)
     * commandsById.get(commandId); if (command != null) commandName =
     * command.getName(); else commandName = bracket(commandId); }
     * stringBuffer.append(commandName); if (commandConflict)
     * stringBuffer.append(SPACE + COMMAND_CONFLICT); String
     * alternateCommandName = null; if (alternateCommandId == null)
     * alternateCommandName = COMMAND_UNDEFINED; else if
     * (alternateCommandId.length() == 0) alternateCommandName =
     * COMMAND_NOTHING; else { ICommandDefinition command = (ICommandDefinition)
     * commandsById.get(alternateCommandId); if (command != null)
     * alternateCommandName = command.getName(); else alternateCommandName =
     * bracket(alternateCommandId); } if (alternateCommandConflict)
     * alternateCommandName += SPACE + COMMAND_CONFLICT;
     * stringBuffer.append(SPACE); if (difference == DIFFERENCE_CHANGE)
     * stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle,
     * "was"), new Object[] { alternateCommandName })); //$NON-NLS-1$
     * tableItem.setText(3, stringBuffer.toString()); if (difference ==
     * DIFFERENCE_MINUS) { if (conflict) tableItem.setForeground(new
     * Color(getShell().getDisplay(), RGB_CONFLICT_MINUS)); else
     * tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS)); }
     * else if (conflict) tableItem.setForeground(new
     * Color(getShell().getDisplay(), RGB_CONFLICT)); }
     */
}