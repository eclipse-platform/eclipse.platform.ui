/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.commands;

import java.io.IOException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.IContextBinding;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.keys.KeySequenceText;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

public class KeysPreferencePage2 extends
        org.eclipse.jface.preference.PreferencePage implements
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
            if (!(object instanceof CommandAssignment)) return false;
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
                    compareTo = Util.compare(assignment,
                            castedObject.assignment);
            return compareTo;
        }

        public boolean equals(Object object) {
            if (!(object instanceof CommandAssignment)) return false;
            KeySequenceAssignment castedObject = (KeySequenceAssignment) object;
            boolean equals = true;
            equals &= Util.equals(assignment, castedObject.assignment);
            equals &= Util.equals(contextId, castedObject.contextId);
            return equals;
        }
    }

    private class TreeViewerCommandsContentProvider implements
            ITreeContentProvider {

        public void dispose() {
        }

        private boolean filterCommand(String commandId) {
            String filter = textCommandsFilter.getText();
            if (filter == null) return true;
            String uniqueName = (String) commandUniqueNamesById.get(commandId);
            if (uniqueName == null) return true;
            return uniqueName.toUpperCase()
                    .indexOf(filter.trim().toUpperCase()) >= 0;
        }

        public Object[] getChildren(Object parentElement) {
            List children = new ArrayList();
            if (parentElement == null) {
                Collection categoryIds = categoryUniqueNamesById.keySet();
                if (categoryIds != null)
                        for (Iterator iterator = categoryIds.iterator(); iterator
                                .hasNext();) {
                            String categoryId = (String) iterator.next();
                            if (hasChildren(NODE_PREFIX_CATEGORY + categoryId))
                                    children.add(NODE_PREFIX_CATEGORY
                                            + categoryId);
                        }
                Collection commandIds = (Collection) commandIdsByCategoryId
                        .get(null);
                if (commandIds != null)
                        for (Iterator iterator = commandIds.iterator(); iterator
                                .hasNext();) {
                            String commandId = (String) iterator.next();
                            if (filterCommand(commandId))
                                    children.add(NODE_PREFIX_COMMAND
                                            + commandId);
                        }
            } else if (parentElement instanceof String) {
                String string = (String) parentElement;
                if (string.startsWith(NODE_PREFIX_CATEGORY)) {
                    String categoryId = string.substring(NODE_PREFIX_CATEGORY
                            .length());
                    Collection commandIds = (Collection) commandIdsByCategoryId
                            .get(categoryId);
                    if (commandIds != null) {
                        for (Iterator iterator = commandIds.iterator(); iterator
                                .hasNext();) {
                            String commandId = (String) iterator.next();
                            if (filterCommand(commandId))
                                    children.add(NODE_PREFIX_COMMAND
                                            + commandId);
                        }
                        /*
                         * is HashSet.toArray faster than Iterator? Object[]
                         * array = commandIds.toArray();
                         * 
                         * for (int i = 0; i < array.length; i++) { String
                         * commandId = (String) array[i]; if
                         * (filterCommand(commandId))
                         * children.add(NODE_PREFIX_COMMAND + commandId); }
                         */
                    }
                }
            }
            return children.toArray();
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(null);
        }

        public Object getParent(Object element) {
            if (element instanceof String) {
                String string = (String) element;
                if (string.startsWith(NODE_PREFIX_COMMAND)) {
                    String commandId = string.substring(NODE_PREFIX_COMMAND
                            .length());
                    for (Iterator iterator = commandIdsByCategoryId.entrySet()
                            .iterator(); iterator.hasNext();) {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        if (commandId.equals(entry.getValue()))
                                return entry.getKey();
                    }
                }
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            //return getChildren(element).length >= 1;
            if (element == null) {
                Collection categoryIds = categoryUniqueNamesById.keySet();
                if (categoryIds != null)
                        for (Iterator iterator = categoryIds.iterator(); iterator
                                .hasNext();) {
                            String categoryId = (String) iterator.next();
                            if (hasChildren(NODE_PREFIX_CATEGORY + categoryId))
                                    return true;
                        }
                Collection commandIds = (Collection) commandIdsByCategoryId
                        .get(null);
                if (commandIds != null)
                        for (Iterator iterator = commandIds.iterator(); iterator
                                .hasNext();) {
                            String commandId = (String) iterator.next();
                            if (filterCommand(commandId)) return true;
                        }
            } else if (element instanceof String) {
                String string = (String) element;
                if (string.startsWith(NODE_PREFIX_CATEGORY)) {
                    String categoryId = string.substring(NODE_PREFIX_CATEGORY
                            .length());
                    Collection commandIds = (Collection) commandIdsByCategoryId
                            .get(categoryId);
                    if (commandIds != null)
                            for (Iterator iterator = commandIds.iterator(); iterator
                                    .hasNext();) {
                                String commandId = (String) iterator.next();
                                if (filterCommand(commandId)) return true;
                            }
                }
            }
            return false;
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        //        private boolean isCategorized() {
        //            return buttonCategorize.getSelection();
        //        }
    }

    private class TreeViewerCommandsLabelProvider extends LabelProvider {

        public String getText(Object element) {
            if (element instanceof String) {
                String string = (String) element;
                if (string.startsWith(NODE_PREFIX_CATEGORY)) {
                    String categoryId = string.substring(NODE_PREFIX_CATEGORY
                            .length());
                    return (String) categoryUniqueNamesById.get(categoryId);
                } else if (string.startsWith(NODE_PREFIX_COMMAND)) {
                    String commandId = string.substring(NODE_PREFIX_COMMAND
                            .length());
                    return (String) commandUniqueNamesById.get(commandId);
                }
            }
            return super.getText(element);
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

    private final static String NODE_PREFIX_CATEGORY = ">"; //$NON-NLS-1$

    private final static String NODE_PREFIX_COMMAND = "-"; //$NON-NLS-1$		

    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(KeysPreferencePage2.class.getName());

    private final static RGB RGB_MINUS = new RGB(160, 160, 160);

    private Map assignmentsByContextIdByKeySequence;

    private Button buttonAdd;

    private Button buttonCategorize;

    private Button buttonInsert;

    private Button buttonRestore;

    private Map categoryIdsByUniqueName;

    private Map categoryUniqueNamesById;

    private Button checkBoxMultiKeyAssist;

    private Combo comboContext;

    private Combo comboKeyConfiguration;

    private Set commandAssignments;

    private Map commandIdsByCategoryId;

    private Map commandIdsByUniqueName;

    private MutableCommandManager commandManager;

    private Map commandUniqueNamesById;

    private Map contextIdsByCommandId;

    private Map contextIdsByUniqueName;

    private IContextManager contextManager;

    private Map contextUniqueNamesById;

    private Group groupKeySequence;

    private Map keyConfigurationIdsByUniqueName;

    private Map keyConfigurationUniqueNamesById;

    private Set keySequenceAssignments;

    private KeySequenceText keySequenceText;

    private Label labelAssignmentsForCommand;

    private Label labelContext;

    private Label labelContextExtends;

    private Label labelDescription;

    private Label labelKeyConfiguration;

    private Label labelKeyConfigurationExtends;

    private Label labelKeySequence;

    private Label labelName;

    private Menu menuButtonInsert;

    private Table tableAssignmentsForCommand;

    private Text textCommandsFilter;

    private Text textDescription;

    private Text textKeySequence;

    private IntegerFieldEditor textMultiKeyAssistTime;

    private Text textName;

    private SortedMap tree;

    private TreeViewer treeViewerCommands;

    private void buildCommandAssignmentsTable() {
        tableAssignmentsForCommand.removeAll();
        for (Iterator iterator = commandAssignments.iterator(); iterator
                .hasNext();) {
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
                String pluginCommandId;
                if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
                    pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
                else
                    pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;
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
            tableItem.setText(1, commandString);
            if (difference == DIFFERENCE_MINUS)
                    tableItem.setForeground(new Color(getShell().getDisplay(),
                            RGB_MINUS));
            String contextId = commandAssignment.contextId;
            if (contextId == null) {
                // This should never happen.
                tableItem.setText(2, Util.ZERO_LENGTH_STRING);
            } else
                tableItem.setText(2, (String) contextUniqueNamesById
                        .get(contextId)); //$NON-NLS-1$
        }
    }

    private Composite createAssignmentsTab(TabFolder parent) {
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
        Composite compositeCommands = new Composite(composite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 10;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        gridLayout.verticalSpacing = 2;
        compositeCommands.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_BOTH);
        compositeCommands.setLayoutData(gridData);
        textCommandsFilter = new Text(compositeCommands, SWT.BORDER);
        gridData = new GridData();
        gridData.widthHint = 300;
        textCommandsFilter.setLayoutData(gridData);
        textCommandsFilter.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                modifiedTextCommandsFilter();
            }
        });
        spacer = new Composite(compositeCommands, SWT.NULL);
        gridData = new GridData();
        gridData.heightHint = 10;
        gridData.widthHint = 10;
        spacer.setLayoutData(gridData);
        Composite compositeCommandLeft = new Composite(compositeCommands,
                SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        compositeCommandLeft.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_VERTICAL
                | GridData.HORIZONTAL_ALIGN_FILL);
        compositeCommandLeft.setLayoutData(gridData);
        treeViewerCommands = new TreeViewer(compositeCommandLeft);
        treeViewerCommands.setAutoExpandLevel(2);
        treeViewerCommands.setUseHashlookup(true);
        gridData = new GridData(GridData.FILL_BOTH);
        treeViewerCommands.getControl().setLayoutData(gridData);
        treeViewerCommands
                .setContentProvider(new TreeViewerCommandsContentProvider());
        treeViewerCommands
                .setLabelProvider(new TreeViewerCommandsLabelProvider());
        treeViewerCommands.setSorter(new ViewerSorter());
        treeViewerCommands
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        selectedTreeViewerCommands();
                    }
                });
        //        buttonCategorize = new Button(compositeCommandLeft, SWT.CHECK);
        //        buttonCategorize.setText(Util.translateString(RESOURCE_BUNDLE,
        //        			"buttonCategorize")); //$NON-NLS-1$
        //        buttonCategorize.setSelection(true);
        //        buttonCategorize.addSelectionListener(new SelectionAdapter() {
        //
        //            public void widgetSelected(SelectionEvent selectionEvent) {
        //            		selectedButtonCategorize();
        //            }
        //        });
        Composite compositeCommandRight = new Composite(compositeCommands,
                SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        compositeCommandRight.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_BOTH);
        compositeCommandRight.setLayoutData(gridData);
        Composite compositeNameAndDescription = new Composite(
                compositeCommandRight, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        compositeNameAndDescription.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_BOTH);
        compositeNameAndDescription.setLayoutData(gridData);
        labelName = new Label(compositeNameAndDescription, SWT.LEFT);
        gridData = new GridData();
        labelName.setLayoutData(gridData);
        labelName.setText(Util.translateString(RESOURCE_BUNDLE, "labelName")); //$NON-NLS-1$
        textName = new Text(compositeNameAndDescription, SWT.LEFT
                | SWT.READ_ONLY);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        textName.setLayoutData(gridData);
        labelDescription = new Label(compositeNameAndDescription, SWT.LEFT);
        gridData = new GridData();
        labelDescription.setLayoutData(gridData);
        labelDescription.setText(Util.translateString(RESOURCE_BUNDLE,
                "labelDescription")); //$NON-NLS-1$
        textDescription = new Text(compositeNameAndDescription, SWT.LEFT
                | SWT.READ_ONLY);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        textDescription.setLayoutData(gridData);
        labelAssignmentsForCommand = new Label(compositeCommandRight, SWT.LEFT);
        gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        labelAssignmentsForCommand.setLayoutData(gridData);
        labelAssignmentsForCommand.setText(Util.translateString(
                RESOURCE_BUNDLE, "labelAssignmentsForCommand")); //$NON-NLS-1$
        tableAssignmentsForCommand = new Table(compositeCommandRight,
                SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        tableAssignmentsForCommand.setHeaderVisible(true);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        gridData.widthHint = 520; //$NON-NLS-1$
        tableAssignmentsForCommand.setLayoutData(gridData);
        TableColumn tableColumnDelta = new TableColumn(
                tableAssignmentsForCommand, SWT.NULL, 0);
        tableColumnDelta.setResizable(false);
        tableColumnDelta.setText(Util.ZERO_LENGTH_STRING);
        tableColumnDelta.setWidth(20);
        TableColumn tableColumnKeySequence = new TableColumn(
                tableAssignmentsForCommand, SWT.NULL, 1);
        tableColumnKeySequence.setResizable(true);
        tableColumnKeySequence.setText(Util.translateString(RESOURCE_BUNDLE,
                "tableColumnKeySequence")); //$NON-NLS-1$
        tableColumnKeySequence.pack();
        tableColumnKeySequence.setWidth(280);
        TableColumn tableColumnContext = new TableColumn(
                tableAssignmentsForCommand, SWT.NULL, 2);
        tableColumnContext.setResizable(true);
        tableColumnContext.setText(Util.translateString(RESOURCE_BUNDLE,
                "tableColumnContext")); //$NON-NLS-1$
        tableColumnContext.pack();
        tableColumnContext.setWidth(150);
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
        buttonRestore = new Button(compositeCommandRight, SWT.CENTER | SWT.PUSH);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
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
        groupKeySequence = new Group(compositeCommandRight, SWT.SHADOW_NONE);
        groupKeySequence.setText(Util.translateString(RESOURCE_BUNDLE,
                "groupKeySequence.noCommand")); //$NON-NLS-1$        
        gridLayout = new GridLayout();
        groupKeySequence.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        groupKeySequence.setLayoutData(gridData);
        Composite groupKeySequenceTop = new Composite(groupKeySequence,
                SWT.NULL);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        groupKeySequenceTop.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        groupKeySequenceTop.setLayoutData(gridData);
        labelKeySequence = new Label(groupKeySequenceTop, SWT.LEFT);
        gridData = new GridData();
        labelKeySequence.setLayoutData(gridData);
        labelKeySequence.setText(Util.translateString(RESOURCE_BUNDLE,
                "labelKeySequence")); //$NON-NLS-1$
        Composite compositeKeySequence = new Composite(groupKeySequenceTop,
                SWT.NULL);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        compositeKeySequence.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        compositeKeySequence.setLayoutData(gridData);
        textKeySequence = new Text(compositeKeySequence, SWT.BORDER);
        textKeySequence.setFont(compositeKeySequence.getFont());
        gridData = new GridData();
        gridData.widthHint = 300;
        textKeySequence.setLayoutData(gridData);
        textKeySequence.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                modifiedTextKeySequence();
            }
        });
        textKeySequence.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                PlatformUI.getWorkbench().getContextSupport()
                        .setKeyFilterEnabled(false);
            }

            public void focusLost(FocusEvent e) {
                PlatformUI.getWorkbench().getContextSupport()
                        .setKeyFilterEnabled(true);
            }
        });
        keySequenceText = new KeySequenceText(textKeySequence);
        keySequenceText.setKeyStrokeLimit(4);
        buttonInsert = new Button(compositeKeySequence, SWT.LEFT | SWT.ARROW);
        buttonInsert.setToolTipText(Util.translateString(RESOURCE_BUNDLE,
                "buttonInsert.ToolTipText")); //$NON-NLS-1$
        gridData = new GridData();
        gridData.heightHint = comboKeyConfiguration.getTextHeight();
        buttonInsert.setLayoutData(gridData);
        buttonInsert.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                Point buttonLocation = buttonInsert.getLocation();
                buttonLocation = groupKeySequence.toDisplay(buttonLocation.x,
                        buttonLocation.y);
                Point buttonSize = buttonInsert.getSize();
                menuButtonInsert.setLocation(buttonLocation.x, buttonLocation.y
                        + buttonSize.y);
                menuButtonInsert.setVisible(true);
            }
        });
        Control[] tabStops = compositeKeySequence.getTabList();
        ArrayList newTabStops = new ArrayList();
        for (int i = 0; i < tabStops.length; i++) {
            Control tabStop = tabStops[i];
            newTabStops.add(tabStop);
            if (textKeySequence.equals(tabStop)) {
                newTabStops.add(buttonInsert);
            }
        }
        Control[] newTabStopArray = (Control[]) newTabStops
                .toArray(new Control[newTabStops.size()]);
        compositeKeySequence.setTabList(newTabStopArray);
        menuButtonInsert = new Menu(buttonInsert);
        Iterator trappedKeyItr = KeySequenceText.TRAPPED_KEYS.iterator();
        while (trappedKeyItr.hasNext()) {
            final KeyStroke trappedKey = (KeyStroke) trappedKeyItr.next();
            MenuItem menuItem = new MenuItem(menuButtonInsert, SWT.PUSH);
            menuItem.setText(trappedKey.format());
            menuItem.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    keySequenceText.insert(trappedKey);
                    textKeySequence.setFocus();
                    textKeySequence
                            .setSelection(textKeySequence.getTextLimit());
                }
            });
        }
        labelContext = new Label(groupKeySequenceTop, SWT.LEFT);
        labelContext.setText(Util.translateString(RESOURCE_BUNDLE,
                "labelContext")); //$NON-NLS-1$
        Composite compositeContext = new Composite(groupKeySequenceTop,
                SWT.NULL);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        compositeContext.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        compositeContext.setLayoutData(gridData);
        comboContext = new Combo(compositeContext, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.widthHint = 200;
        comboContext.setLayoutData(gridData);
        comboContext.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                selectedComboContext();
            }
        });
        labelContextExtends = new Label(compositeContext, SWT.LEFT);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        labelContextExtends.setLayoutData(gridData);
        buttonAdd = new Button(groupKeySequence, SWT.CENTER | SWT.PUSH);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
        widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        buttonAdd.setText(Util.translateString(RESOURCE_BUNDLE, "buttonAdd")); //$NON-NLS-1$
        gridData.widthHint = Math.max(widthHint, buttonAdd.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
        buttonAdd.setLayoutData(gridData);
        buttonAdd.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionEvent) {
                selectedButtonAdd();
            }
        });
        return composite;
    }

    protected Control createContents(Composite parent) {
        final TabFolder tabFolder = new TabFolder(parent, SWT.NULL);
        final TabItem assignmentsTab = new TabItem(tabFolder, SWT.NULL);
        assignmentsTab.setText(Util.translateString(RESOURCE_BUNDLE,
                "assignmentsTab.Text")); //$NON-NLS-1$
        assignmentsTab.setControl(createAssignmentsTab(tabFolder));
        final TabItem optionsTab = new TabItem(tabFolder, SWT.NULL);
        optionsTab.setText(Util.translateString(RESOURCE_BUNDLE,
                "optionsTab.Text")); //$NON-NLS-1$
        optionsTab.setControl(createOptionsTab(tabFolder));
        applyDialogFont(tabFolder);
        return tabFolder;
    }

    private Composite createOptionsTab(TabFolder parent) {
        GridData gridData = null;
        // The composite for this tab.
        final Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        // The multi-key assist button.
        checkBoxMultiKeyAssist = new Button(composite, SWT.CHECK);
        checkBoxMultiKeyAssist.setText(Util.translateString(RESOURCE_BUNDLE,
                "checkBoxMultiKeyAssist.Text")); //$NON-NLS-1$
        checkBoxMultiKeyAssist.setToolTipText(Util.translateString(
                RESOURCE_BUNDLE, "checkBoxMultiKeyAssist.ToolTipText")); //$NON-NLS-1$
        checkBoxMultiKeyAssist.setSelection(getPreferenceStore().getBoolean(
                IPreferenceConstants.MULTI_KEY_ASSIST));
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        checkBoxMultiKeyAssist.setLayoutData(gridData);
        // The multi key assist time.
        final IPreferenceStore store = WorkbenchPlugin.getDefault()
                .getPreferenceStore();
        textMultiKeyAssistTime = new IntegerFieldEditor(
                IPreferenceConstants.MULTI_KEY_ASSIST_TIME, Util
                        .translateString(RESOURCE_BUNDLE,
                                "textMultiKeyAssistTime.Text"), composite); //$NON-NLS-1$
        textMultiKeyAssistTime.setPreferenceStore(store);
        textMultiKeyAssistTime.setPreferencePage(this);
        textMultiKeyAssistTime.setTextLimit(9);
        textMultiKeyAssistTime.setErrorMessage(Util.translateString(
                RESOURCE_BUNDLE, "textMultiKeyAssistTime.ErrorMessage")); //$NON-NLS-1$
        textMultiKeyAssistTime
                .setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        textMultiKeyAssistTime.setValidRange(1, Integer.MAX_VALUE);
        textMultiKeyAssistTime.setStringValue(Integer.toString(store
                .getInt(IPreferenceConstants.MULTI_KEY_ASSIST_TIME)));
        textMultiKeyAssistTime
                .setPropertyChangeListener(new IPropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getProperty().equals(FieldEditor.IS_VALID))
                                setValid(textMultiKeyAssistTime.isValid());
                    }
                });
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 5;
        gridLayout.marginWidth = 5;
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
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

    private String getCommandId() {
        String commandId = null;
        IStructuredSelection structuredSelection = (IStructuredSelection) treeViewerCommands
                .getSelection();
        if (structuredSelection != null) {
            Object firstElement = structuredSelection.getFirstElement();
            if (firstElement instanceof String) {
                String string = (String) firstElement;
                if (string.startsWith(NODE_PREFIX_COMMAND))
                        commandId = string.substring(NODE_PREFIX_COMMAND
                                .length());
            }
        }
        return commandId;
    }

    private String getContextId() {
        //        return comboContext.getSelectionIndex() > 0 ? (String)
        // contextIdsByUniqueName
        //                .get(comboContext.getText())
        //                : null;
        return (String) contextIdsByUniqueName.get(comboContext.getText());
    }

    private String getKeyConfigurationId() {
        return comboKeyConfiguration.getSelectionIndex() >= 0 ? (String) keyConfigurationIdsByUniqueName
                .get(comboKeyConfiguration.getText())
                : null;
    }

    private KeySequence getKeySequence() {
        return keySequenceText.getKeySequence();
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

    private void modifiedTextCommandsFilter() {
        treeViewerCommands.refresh();
        treeViewerCommands.expandAll();
        
//        if (treeViewerCommands.getSelection().isEmpty()) {
//            TreeViewerCommandsContentProvider treeViewerCommandsContentProvider = (TreeViewerCommandsContentProvider) treeViewerCommands.getContentProvider();            
//            Object[] children = treeViewerCommandsContentProvider.getChildren(null);
//            
//            if (children.length >= 1) {
//                ViewerSorter viewerSorter = treeViewerCommands.getSorter();            
//                viewerSorter.sort(treeViewerCommands, children);                
//                children = treeViewerCommandsContentProvider.getChildren(children[0]);
//                
//                if (children.length >= 1) {
//                    viewerSorter.sort(treeViewerCommands, children);                
//                    treeViewerCommands.setSelection(new StructuredSelection(children[0]));
//                }
//            }
//        }
        
        update();
    }

    private void modifiedTextKeySequence() {
        update();
    }

    protected void performDefaults() {
        String activeKeyConfigurationId = getKeyConfigurationId();
        List preferenceKeySequenceBindingDefinitions = new ArrayList();
        KeySequenceBindingNode.getKeySequenceBindingDefinitions(tree,
                KeySequence.getInstance(), 0,
                preferenceKeySequenceBindingDefinitions);
        if (activeKeyConfigurationId != null
                || !preferenceKeySequenceBindingDefinitions.isEmpty()) {
            MessageBox restoreDefaultsMessageBox = new MessageBox(getShell(),
                    SWT.YES | SWT.NO | SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
            restoreDefaultsMessageBox.setText(Util.translateString(
                    RESOURCE_BUNDLE, "restoreDefaultsMessageBoxText")); //$NON-NLS-1$
            restoreDefaultsMessageBox.setMessage(Util.translateString(
                    RESOURCE_BUNDLE, "restoreDefaultsMessageBoxMessage")); //$NON-NLS-1$
            if (restoreDefaultsMessageBox.open() == SWT.YES) {
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
        IPreferenceStore store = getPreferenceStore();
        checkBoxMultiKeyAssist.setSelection(store
                .getDefaultBoolean(IPreferenceConstants.MULTI_KEY_ASSIST));
        textMultiKeyAssistTime.setStringValue(Integer.toString(store
                .getDefaultInt(IPreferenceConstants.MULTI_KEY_ASSIST_TIME)));
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
        }
        IPreferenceStore store = getPreferenceStore();
        store.setValue(IPreferenceConstants.MULTI_KEY_ASSIST,
                checkBoxMultiKeyAssist.getSelection());
        store.setValue(IPreferenceConstants.MULTI_KEY_ASSIST_TIME,
                textMultiKeyAssistTime.getIntValue());
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

    //    private void selectedButtonCategorize() {
    //		treeViewerCommands.refresh();
    //      treeViewerCommands.expandAll();
    //    }
    private void selectedTreeViewerCommands() {
        update();
    }

    private void setAssignmentsForCommand() {
        commandAssignments.clear();
        String commandId = getCommandId();

        if (commandId != null)
                for (Iterator iterator = assignmentsByContextIdByKeySequence
                        .entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    KeySequence keySequence = (KeySequence) entry.getKey();
                    Map assignmentsByContextId = (Map) entry.getValue();
                    if (assignmentsByContextId != null)
                            for (Iterator iterator2 = assignmentsByContextId
                                    .entrySet().iterator(); iterator2.hasNext();) {
                                Map.Entry entry2 = (Map.Entry) iterator2.next();
                                CommandAssignment commandAssignment = new CommandAssignment();
                                commandAssignment.assignment = (KeySequenceBindingNode.Assignment) entry2
                                        .getValue();
                                commandAssignment.contextId = (String) entry2
                                        .getKey();
                                commandAssignment.keySequence = keySequence;
                                if (commandAssignment.assignment
                                        .contains(commandId))
                                        commandAssignments
                                                .add(commandAssignment);
                            }
                }

        buildCommandAssignmentsTable();
    }

    private void setCommandId(String commandId) {
        if (commandId == null)
            treeViewerCommands.setSelection(new StructuredSelection());
        else
            treeViewerCommands.setSelection(new StructuredSelection(
                    NODE_PREFIX_COMMAND + commandId));
    }

    private void setContextId(String contextId) {
        comboContext.clearSelection();
        comboContext.deselectAll();
        String contextUniqueName = (String) contextUniqueNamesById
                .get(contextId);
        //        if (contextUniqueName != null) {
        //            String items[] = comboContext.getItems();
        //            for (int i = 1; i < items.length; i++)
        //                if (contextUniqueName.equals(items[i])) {
        //                    comboContext.select(i);
        //                    break;
        //                }
        //        } else
        //            comboContext.select(0);
        if (contextUniqueName != null) {
            String items[] = comboContext.getItems();
            for (int i = 0; i < items.length; i++)
                if (contextUniqueName.equals(items[i])) {
                    comboContext.select(i);
                    break;
                }
        }
    }

    private void setContextsForCommand() {
        String commandId = getCommandId();
        String contextId = getContextId();
        Set contextIds = (Set) contextIdsByCommandId.get(commandId);
        Map contextIdsByName = new HashMap(contextIdsByUniqueName);
        // TODO for context bound commands, this code retains only those
        // contexts explictly bound. what about assigning key bindings to
        // implicit descendant contexts?
        if (contextIds != null)
                contextIdsByName.values().retainAll(contextIds);
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
        keySequenceText.setKeySequence(keySequence);
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
            contextIdsByCommandId = new HashMap();
            for (Iterator iterator = commandManager.getDefinedCommandIds()
                    .iterator(); iterator.hasNext();) {
                ICommand command = commandManager.getCommand((String) iterator
                        .next());
                List contextBindings = command.getContextBindings();
                if (!contextBindings.isEmpty()) {
                    Set contextIds = new HashSet();
                    for (Iterator iterator2 = contextBindings.iterator(); iterator2
                            .hasNext();) {
                        IContextBinding contextBinding = (IContextBinding) iterator2
                                .next();
                        String contextId = contextBinding.getContextId();
                        if (contextManager.getDefinedContextIds().contains(
                                contextId)) contextIds.add(contextId);
                    }
                    contextIdsByCommandId.put(command.getId(), contextIds);
                }
            }
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
                Set contextIds = (Set) contextIdsByCommandId.get(commandId);
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
                boolean validContextIdForCommandId = contextIds == null
                        || contextIds.contains(contextId);
                if (!validKeySequence || !validCommandId || !validContextId
                        || !validKeyConfigurationId
                        || !validContextIdForCommandId) iterator.remove();
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
                Set contextIds = (Set) contextIdsByCommandId.get(commandId);
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
                boolean validContextIdForCommandId = contextIds == null
                        || contextIds.contains(contextId);
                if (!validKeySequence || !validCommandId || !validContextId
                        || !validKeyConfigurationId
                        || !validContextIdForCommandId) iterator.remove();
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
            List keyConfigurationNames = new ArrayList(
                    keyConfigurationIdsByUniqueName.keySet());
            Collections.sort(keyConfigurationNames, Collator.getInstance());
            comboKeyConfiguration.setItems((String[]) keyConfigurationNames
                    .toArray(new String[keyConfigurationNames.size()]));
            setKeyConfigurationId(activeKeyConfigurationId);           
            treeViewerCommands.setInput(new Object());
            update();
        }
        super.setVisible(visible);
    }

    private void update() {
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
        setAssignmentsForCommand();
        String commandId = getCommandId();
        if (commandId == null) {
            textName.setText(Util.ZERO_LENGTH_STRING);
            textDescription.setText(Util.ZERO_LENGTH_STRING);
            groupKeySequence.setText(Util.translateString(RESOURCE_BUNDLE,
                    "groupKeySequence.noCommand")); //$NON-NLS-1$
        } else {
            String uniqueName = (String) commandUniqueNamesById.get(commandId);
            if (uniqueName == null)
                textName.setText(Util.ZERO_LENGTH_STRING);
            else
                textName.setText(uniqueName);
            ICommand command = commandManager.getCommand(commandId);
            try {
                textDescription.setText(command.getDescription());
            } catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
                textDescription.setText(Util.ZERO_LENGTH_STRING);
            }
            groupKeySequence.setText(MessageFormat.format(Util.translateString(
                    RESOURCE_BUNDLE, "groupKeySequence.command"), //$NON-NLS-1$
                    new Object[] { '\'' + uniqueName  + '\'' }));
        }
        String contextId = getContextId();
        selectAssignmentForCommand(contextId);
        updateLabelKeyConfigurationExtends();
        updateLabelContextExtends();
        boolean validCommand = commandId != null;
        boolean validContext = contextId != null;
        boolean validKeySequence = keySequence != null
                && keySequence.isComplete() && !keySequence.isEmpty();
        boolean validSelection = false;

        if (tableAssignmentsForCommand.getSelectionCount() == 1) {
            // not the prettiest way to do this...
            int selectionIndex = tableAssignmentsForCommand.getSelectionIndex();

            if (selectionIndex >= 0) {
                TableItem tableItem = tableAssignmentsForCommand
                        .getItem(selectionIndex);

                if (tableItem != null)
                        validSelection = tableItem.getImage(0) != IMAGE_BLANK;
            }
        }

        labelName.setEnabled(validCommand);
        textName.setEnabled(validCommand);
        labelDescription.setEnabled(validCommand);
        textDescription.setEnabled(validCommand);
        labelAssignmentsForCommand.setEnabled(validCommand);
        tableAssignmentsForCommand.setEnabled(validCommand);
        buttonRestore.setEnabled(validCommand && validSelection);
        groupKeySequence.setEnabled(validCommand);
        labelKeySequence.setEnabled(validCommand);
        textKeySequence.setEnabled(validCommand);
        buttonInsert.setEnabled(validCommand);
        labelContext.setEnabled(validCommand);
        comboContext.setEnabled(validCommand);
        buttonAdd.setEnabled(validCommand && validContext && validKeySequence);
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
}
//	private static class AssignDialog extends Dialog {
//		
//		private Menu menuButtonInsert;
//		private Text textCommand;
//		private KeySequenceText keySequenceText;
//		private Combo comboContext;
//			
//		AssignDialog(Shell shell) {
//			super(shell);
//		}
//
//		KeySequence getKeySequence() {
//			return keySequenceText.getKeySequence();
//		}
//		
//		String getCommand() {
//			return textCommand.getText();
//		}
//		
//		void setKeySequence(KeySequence keySequence) {
//			keySequenceText.setKeySequence(keySequence);
//		}
//		
//		void setCommand(String command) {
//			textCommand.setText(command);
//		}
//		
//		protected Control createDialogArea(Composite parent) {
//			getShell().setText(Util
//					.translateString(RESOURCE_BUNDLE, "titleAssign")); //$NON-NLS-1$
//			
//			final Composite composite = (Composite) super
//					.createDialogArea(parent);
//			GridLayout gridLayout = new GridLayout();
//			gridLayout.numColumns = 3;
//			composite.setLayout(gridLayout);
//			GridData gridData = new GridData(GridData.FILL_BOTH);
//			composite.setLayoutData(gridData);
//			Label labelContext = new Label(composite, SWT.LEFT);
//			labelContext.setText(Util.translateString(RESOURCE_BUNDLE,
//					"labelContext")); //$NON-NLS-1$
//			comboContext = new Combo(composite, SWT.READ_ONLY);
//			gridData = new GridData();
//			gridData.widthHint = 250;
//			comboContext.setLayoutData(gridData);
//			comboContext.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent selectionEvent) {
//					// TODO selectedComboContext();
//				}
//			});
//			Label labelContextExtends = new Label(composite, SWT.LEFT);
//			gridData = new GridData(GridData.FILL_HORIZONTAL);
//			labelContextExtends.setLayoutData(gridData);
//			Label labelKeySequence = new Label(composite, SWT.LEFT);
//			gridData = new GridData();
//			labelKeySequence.setLayoutData(gridData);
//			labelKeySequence.setText(Util.translateString(RESOURCE_BUNDLE,
//					"labelKeySequence")); //$NON-NLS-1$
//			final Text textKeySequence = new Text(composite, SWT.BORDER);
//			textKeySequence.setFont(composite.getFont());
//			gridData = new GridData(GridData.FILL_HORIZONTAL);
//			gridData.widthHint = 300;
//			textKeySequence.setLayoutData(gridData);
//			textKeySequence.addModifyListener(new ModifyListener() {
//				public void modifyText(ModifyEvent e) {
//					// TODO modifiedTextKeySequence();
//				}
//			});
//			textKeySequence.addFocusListener(new FocusListener() {
//				public void focusGained(FocusEvent e) {
//					PlatformUI.getWorkbench().getContextSupport()
//							.setKeyFilterEnabled(false);
//				}
//				public void focusLost(FocusEvent e) {
//					PlatformUI.getWorkbench().getContextSupport()
//							.setKeyFilterEnabled(true);
//				}
//			});
//			keySequenceText = new KeySequenceText(
//					textKeySequence);
//			keySequenceText.setKeyStrokeLimit(4);
//			final Button buttonInsert = new Button(composite, SWT.LEFT
//					| SWT.ARROW);
//			buttonInsert.setToolTipText(Util.translateString(RESOURCE_BUNDLE,
//					"buttonInsert.ToolTipText")); //$NON-NLS-1$
//			gridData = new GridData();
//			gridData.heightHint = comboContext.getTextHeight();
//			buttonInsert.setLayoutData(gridData);
//			buttonInsert.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent selectionEvent) {
//					Point buttonLocation = buttonInsert.getLocation();
//					buttonLocation = composite.toDisplay(buttonLocation.x,
//							buttonLocation.y);
//					Point buttonSize = buttonInsert.getSize();
//					menuButtonInsert.setLocation(buttonLocation.x,
//							buttonLocation.y + buttonSize.y);
//					menuButtonInsert.setVisible(true);
//				}
//			});
//			Control[] tabList = composite.getTabList();
//			ArrayList newTabList = new ArrayList();
//			for (int i = 0; i < tabList.length; i++) {
//				Control tab = tabList[i];
//				newTabList.add(tab);
//				if (textKeySequence.equals(tab))
//					newTabList.add(buttonInsert);
//			}
//			Control[] newTabListAsArray = (Control[]) newTabList
//					.toArray(new Control[newTabList.size()]);
//			composite.setTabList(newTabListAsArray);
//			menuButtonInsert = new Menu(buttonInsert);
//			Iterator iterator = KeySequenceText.TRAPPED_KEYS.iterator();
//			while (iterator.hasNext()) {
//				final KeyStroke trappedKey = (KeyStroke) iterator.next();
//				MenuItem menuItem = new MenuItem(menuButtonInsert, SWT.PUSH);
//				menuItem.setText(trappedKey.format());
//				menuItem.addSelectionListener(new SelectionAdapter() {
//					public void widgetSelected(SelectionEvent e) {
//						keySequenceText.insert(trappedKey);
//						textKeySequence.setFocus();
//						textKeySequence.setSelection(textKeySequence
//								.getTextLimit());
//					}
//				});
//			}
//			Label labelCommand = new Label(composite, SWT.LEFT);
//			gridData = new GridData();
//			labelCommand.setLayoutData(gridData);
//			labelCommand.setText(Util
//					.translateString(RESOURCE_BUNDLE, "labelCommand")); //$NON-NLS-1$
//			textCommand = new Text(composite, SWT.LEFT | SWT.READ_ONLY);
//			gridData = new GridData(GridData.FILL_HORIZONTAL);
//			gridData.horizontalSpan = 2;
//			textCommand.setLayoutData(gridData);
//			Label labelStatusIcon = new Label(composite, SWT.LEFT);
//			gridData = new GridData();
//			labelStatusIcon.setLayoutData(gridData);
//			Label labelStatusText = new Label(composite, SWT.LEFT);
//			gridData = new GridData(GridData.FILL_HORIZONTAL);
//			gridData.horizontalSpan = 2;
//			labelStatusText.setLayoutData(gridData);
//			textKeySequence.setFocus();
//			return composite;
//		}
//	}
/*
 * private void selectedButtonChange() { KeySequence keySequence =
 * getKeySequence(); boolean validKeySequence = keySequence != null &&
 * validateSequence(keySequence); String scopeId = getScopeId(); boolean
 * validScopeId = scopeId != null && contextsDefinitionsById.get(scopeId) !=
 * null; String keyConfigurationId = getKeyConfigurationId(); boolean
 * validKeyConfigurationId = keyConfigurationId != null &&
 * keyConfigurationsById.get(keyConfigurationId) != null; if (validKeySequence &&
 * validScopeId && validKeyConfigurationId) { String commandId = null;
 * ISelection selection = treeViewerCommands.getSelection(); if (selection
 * instanceof IStructuredSelection && !selection.isEmpty()) { Object object =
 * ((IStructuredSelection) selection).getFirstElement(); if (object instanceof
 * ICommandDefinition) commandId = ((ICommandDefinition) object).getId(); }
 * CommandRecord commandRecord = getSelectedCommandRecord(); if (commandRecord ==
 * null) set(tree, keySequence, scopeId, keyConfigurationId, commandId); else {
 * if (!commandRecord.customSet.isEmpty()) clear(tree, keySequence, scopeId,
 * keyConfigurationId); else set(tree, keySequence, scopeId, keyConfigurationId,
 * null); } commandRecords.clear(); buildCommandRecords(tree, commandId,
 * commandRecords); buildTableCommand(); selectTableCommand(scopeId,
 * keyConfigurationId, keySequence); keySequenceRecords.clear();
 * buildSequenceRecords(tree, keySequence, keySequenceRecords);
 * buildTableKeySequence(); selectTableKeySequence(scopeId, keyConfigurationId);
 * update(); } } private void buildTableCommand() {
 * tableSequencesForCommand.removeAll(); for (int i = 0; i <
 * commandRecords.size(); i++) { CommandRecord commandRecord = (CommandRecord)
 * commandRecords.get(i); Set customSet = commandRecord.customSet; Set
 * defaultSet = commandRecord.defaultSet; int difference = DIFFERENCE_NONE;
 * //String commandId = null; // // boolean commandConflict = false; String
 * alternateCommandId = null; boolean alternateCommandConflict = false; if
 * (customSet.isEmpty()) { if (defaultSet.contains(commandRecord.command)) {
 * //commandId // // = commandRecord.commandId; commandConflict =
 * commandRecord.defaultConflict; } } else { if (defaultSet.isEmpty()) { if
 * (customSet.contains(commandRecord.command)) { difference = DIFFERENCE_ADD;
 * //commandId = // // commandRecord.commandId; // // commandConflict =
 * commandRecord.customConflict; } } else { if
 * (customSet.contains(commandRecord.command)) { difference = DIFFERENCE_CHANGE;
 * //commandId = // // commandRecord.commandId; commandConflict =
 * commandRecord.customConflict; alternateCommandId =
 * commandRecord.defaultCommand; alternateCommandConflict =
 * commandRecord.defaultConflict; } else { if
 * (defaultSet.contains(commandRecord.command)) { difference = DIFFERENCE_MINUS;
 * //commandId = // // commandRecord.commandId; // // commandConflict =
 * commandRecord.defaultConflict; alternateCommandId =
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
 * keyConfigurationsById.get(commandRecord.configuration); tableItem.setText(2,
 * keyConfiguration != null ? keyConfiguration.getName() :
 * bracket(commandRecord.configuration)); boolean conflict = commandConflict ||
 * alternateCommandConflict; StringBuffer stringBuffer = new StringBuffer(); if
 * (commandRecord.sequence != null)
 * stringBuffer.append(KeySupport.formatSequence(commandRecord.sequence, true));
 * if (commandConflict) stringBuffer.append(SPACE + COMMAND_CONFLICT); String
 * alternateCommandName = null; if (alternateCommandId == null)
 * alternateCommandName = COMMAND_UNDEFINED; else if
 * (alternateCommandId.length() == 0) alternateCommandName = COMMAND_NOTHING;
 * else { ICommandDefinition command = (ICommandDefinition)
 * commandsById.get(alternateCommandId); if (command != null)
 * alternateCommandName = command.getName(); else alternateCommandName =
 * bracket(alternateCommandId); } if (alternateCommandConflict)
 * alternateCommandName += SPACE + COMMAND_CONFLICT; stringBuffer.append(SPACE);
 * if (difference == DIFFERENCE_CHANGE)
 * stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle,
 * "was"), new Object[] { alternateCommandName })); //$NON-NLS-1$ else if
 * (difference == DIFFERENCE_MINUS)
 * stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle,
 * "now"), new Object[] { alternateCommandName })); //$NON-NLS-1$
 * tableItem.setText(3, stringBuffer.toString()); if (difference ==
 * DIFFERENCE_MINUS) { if (conflict) tableItem.setForeground(new
 * Color(getShell().getDisplay(), RGB_CONFLICT_MINUS)); else
 * tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS)); }
 * else if (conflict) tableItem.setForeground(new Color(getShell().getDisplay(),
 * RGB_CONFLICT)); } } private void buildTableKeySequence() {
 * tableCommandsForSequence.removeAll(); for (int i = 0; i <
 * keySequenceRecords.size(); i++) { KeySequenceRecord keySequenceRecord =
 * (KeySequenceRecord) keySequenceRecords.get(i); int difference =
 * DIFFERENCE_NONE; String commandId = null; boolean commandConflict = false;
 * String alternateCommandId = null; boolean alternateCommandConflict = false;
 * if (keySequenceRecord.customSet.isEmpty()) { commandId =
 * keySequenceRecord.defaultCommand; commandConflict =
 * keySequenceRecord.defaultConflict; } else { commandId =
 * keySequenceRecord.customCommand; commandConflict =
 * keySequenceRecord.customConflict; if (keySequenceRecord.defaultSet.isEmpty())
 * difference = DIFFERENCE_ADD; else { difference = DIFFERENCE_CHANGE;
 * alternateCommandId = keySequenceRecord.defaultCommand;
 * alternateCommandConflict = keySequenceRecord.defaultConflict; } } TableItem
 * tableItem = new TableItem(tableCommandsForSequence, SWT.NULL); switch
 * (difference) { case DIFFERENCE_ADD : tableItem.setImage(0, IMAGE_PLUS);
 * break; case DIFFERENCE_CHANGE : tableItem.setImage(0, IMAGE_CHANGE); break;
 * case DIFFERENCE_MINUS : tableItem.setImage(0, IMAGE_MINUS); break; case
 * DIFFERENCE_NONE : tableItem.setImage(0, IMAGE_BLANK); break; }
 * IContextDefinition scope = (IContextDefinition)
 * contextsById.get(keySequenceRecord.scope); tableItem.setText(1, scope != null ?
 * scope.getName() : bracket(keySequenceRecord.scope)); Configuration
 * keyConfiguration = (Configuration)
 * keyConfigurationsById.get(keySequenceRecord.configuration);
 * tableItem.setText(2, keyConfiguration != null ? keyConfiguration.getName() :
 * bracket(keySequenceRecord.configuration)); boolean conflict = commandConflict ||
 * alternateCommandConflict; StringBuffer stringBuffer = new StringBuffer();
 * String commandName = null; if (commandId == null) commandName =
 * COMMAND_UNDEFINED; else if (commandId.length() == 0) commandName =
 * COMMAND_NOTHING; else { ICommandDefinition command = (ICommandDefinition)
 * commandsById.get(commandId); if (command != null) commandName =
 * command.getName(); else commandName = bracket(commandId); }
 * stringBuffer.append(commandName); if (commandConflict)
 * stringBuffer.append(SPACE + COMMAND_CONFLICT); String alternateCommandName =
 * null; if (alternateCommandId == null) alternateCommandName =
 * COMMAND_UNDEFINED; else if (alternateCommandId.length() == 0)
 * alternateCommandName = COMMAND_NOTHING; else { ICommandDefinition command =
 * (ICommandDefinition) commandsById.get(alternateCommandId); if (command !=
 * null) alternateCommandName = command.getName(); else alternateCommandName =
 * bracket(alternateCommandId); } if (alternateCommandConflict)
 * alternateCommandName += SPACE + COMMAND_CONFLICT; stringBuffer.append(SPACE);
 * if (difference == DIFFERENCE_CHANGE)
 * stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle,
 * "was"), new Object[] { alternateCommandName })); //$NON-NLS-1$
 * tableItem.setText(3, stringBuffer.toString()); if (difference ==
 * DIFFERENCE_MINUS) { if (conflict) tableItem.setForeground(new
 * Color(getShell().getDisplay(), RGB_CONFLICT_MINUS)); else
 * tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS)); }
 * else if (conflict) tableItem.setForeground(new Color(getShell().getDisplay(),
 * RGB_CONFLICT)); }
 */
