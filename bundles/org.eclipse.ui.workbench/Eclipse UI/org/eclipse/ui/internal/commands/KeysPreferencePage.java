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

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.bindings.keys.KeyStroke;
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
import org.eclipse.ui.IWorkbenchServices;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;

/**
 * The preference page for defining keyboard shortcuts. While some of its
 * underpinning have been made generic to "bindings" rather than "key bindings",
 * it will still take some work to remove the link entirely.
 * 
 * @since 3.0
 */
public class KeysPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

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
			final TableColumn oldSortColumn = tableBindings
					.getColumn(oldSortIndex);
			oldSortColumn.setText(UNSORTED_COLUMN_NAMES[oldSortIndex]);
			final TableColumn newSortColumn = tableBindings
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

	private static final String ITEM_DATA_KEY = "org.eclipse.jface.bindings"; //$NON-NLS-1$

	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(KeysPreferencePage.class.getName());

	/**
	 * The total number of columns on the view tab.
	 */
	private final static int VIEW_TOTAL_COLUMNS = 4;

	/**
	 * The translated names for the columns when they are the primary sort key
	 * (e.g., ">Category<").
	 */
	private final static String[] SORTED_COLUMN_NAMES = new String[VIEW_TOTAL_COLUMNS];

	/**
	 * The index of the modify tab.
	 * 
	 * @since 3.1
	 */
	private static final int TAB_INDEX_MODIFY = 1;

	/**
	 * The translated names for the columns when they are not the primary sort
	 * key (e.g., "Category").
	 */
	private final static String[] UNSORTED_COLUMN_NAMES = new String[VIEW_TOTAL_COLUMNS];

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

	private IBindingService bindingService;

	private Button buttonAdd;

	private Button buttonAddKey;

	private Button buttonRemove;

	private Button buttonRestore;

	private Map categoryIdsByUniqueName;

	private Map categoryUniqueNamesById;

	private Combo comboCategory;

	private Combo comboCommand;

	private Combo comboContext;

	private Combo comboScheme;

	private Map commandIdsByCategoryId;

	private Map commandIdsByUniqueName;

	private ICommandService commandService;

	private Map commandUniqueNamesById;

	private Map contextIdsByUniqueName;

	private IContextService contextService;

	private Map contextUniqueNamesById;

	private Group groupCommand;

	private Group groupKeySequence;

	private Label labelAssignmentsForCommand;

	private Label labelAssignmentsForKeySequence;

	private Label labelCategory;

	private Label labelCommand;

	private Label labelContext;

	private Label labelContextExtends;

	private Label labelKeyConfiguration;

	private Label labelKeyConfigurationExtends;

	private Label labelKeySequence;

	private BindingManager localChangeManager = new BindingManager(
			new ContextManager());

	private Menu menuButtonAddKey;

	private Color minusColour;

	private Map schemeIdsByUniqueName;

	private Map schemeUniqueNamesById;

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
	private Table tableBindings;

	private Text textTriggerSequence;

	private KeySequenceText textTriggerSequenceManager;

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
		comboScheme = new Combo(compositeKeyConfiguration, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 200;
		comboScheme.setLayoutData(gridData);

		comboScheme.addSelectionListener(new SelectionAdapter() {

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
		textTriggerSequence = new Text(groupKeySequence, SWT.BORDER);
		// On MacOS X, this font will be changed by KeySequenceText
		textTriggerSequence.setFont(groupKeySequence.getFont());
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 300;
		textTriggerSequence.setLayoutData(gridData);
		textTriggerSequence.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				modifiedTextKeySequence();
			}
		});
		textTriggerSequence.addFocusListener(new FocusListener() {

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
		textTriggerSequenceManager = new KeySequenceText(textTriggerSequence);
		textTriggerSequenceManager.setKeyStrokeLimit(4);

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
			if (textTriggerSequence.equals(tabStop)) {
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
					textTriggerSequenceManager.insert(trappedKey);
					textTriggerSequence.setFocus();
					textTriggerSequence.setSelection(textTriggerSequence
							.getTextLimit());
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
		tableBindings = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL);
		tableBindings.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 400;
		gridData.horizontalSpan = 2;
		tableBindings.setLayoutData(gridData);
		final TableColumn tableColumnCategory = new TableColumn(tableBindings,
				SWT.NONE, VIEW_CATEGORY_COLUMN_INDEX);
		tableColumnCategory
				.setText(SORTED_COLUMN_NAMES[VIEW_CATEGORY_COLUMN_INDEX]);
		tableColumnCategory
				.addSelectionListener(new SortOrderSelectionListener(
						VIEW_CATEGORY_COLUMN_INDEX));
		final TableColumn tableColumnCommand = new TableColumn(tableBindings,
				SWT.NONE, VIEW_COMMAND_COLUMN_INDEX);
		tableColumnCommand
				.setText(UNSORTED_COLUMN_NAMES[VIEW_COMMAND_COLUMN_INDEX]);
		tableColumnCommand.addSelectionListener(new SortOrderSelectionListener(
				VIEW_COMMAND_COLUMN_INDEX));
		final TableColumn tableColumnKeySequence = new TableColumn(
				tableBindings, SWT.NONE, VIEW_KEY_SEQUENCE_COLUMN_INDEX);
		tableColumnKeySequence
				.setText(UNSORTED_COLUMN_NAMES[VIEW_KEY_SEQUENCE_COLUMN_INDEX]);
		tableColumnKeySequence
				.addSelectionListener(new SortOrderSelectionListener(
						VIEW_KEY_SEQUENCE_COLUMN_INDEX));
		final TableColumn tableColumnContext = new TableColumn(tableBindings,
				SWT.NONE, VIEW_CONTEXT_COLUMN_INDEX);
		tableColumnContext
				.setText(UNSORTED_COLUMN_NAMES[VIEW_CONTEXT_COLUMN_INDEX]);
		tableColumnContext.addSelectionListener(new SortOrderSelectionListener(
				VIEW_CONTEXT_COLUMN_INDEX));
		tableBindings.addSelectionListener(new SelectionAdapter() {
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
			final Command command = commandService.getCommand(commandId);
			try {
				categoryId = command.getCategory().getId();
			} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
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
			textTriggerSequence.setFocus();
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

	private KeySequence getKeySequence() {
		return textTriggerSequenceManager.getKeySequence();
	}

	private String getSchemeId() {
		return comboScheme.getSelectionIndex() >= 0 ? (String) schemeIdsByUniqueName
				.get(comboScheme.getText())
				: null;
	}

	public void init(IWorkbench workbench) {
		bindingService = (IBindingService) workbench
				.getService(IWorkbenchServices.BINDING);
		commandService = (ICommandService) workbench
				.getService(IWorkbenchServices.COMMAND);
		contextService = (IContextService) workbench
				.getService(IWorkbenchServices.CONTEXT);
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
		String activeKeyConfigurationId = getSchemeId();
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
				setScheme(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID);
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
				.add(new ActiveKeyConfigurationDefinition(getSchemeId(), null));
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
		String schemeId = getSchemeId();
		KeySequence keySequence = getKeySequence();
		localChangeManager.removeBindings(keySequence, schemeId, contextId,
				null, null, null, Binding.USER);
		localChangeManager.addBinding(new KeyBinding(keySequence, commandId,
				schemeId, contextId, null, null, null, Binding.USER));
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
					final TableItem[] items = tableBindings.getItems();
					final int numColumns = tableBindings.getColumnCount();
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
		String schemeId = getSchemeId();
		KeySequence keySequence = getKeySequence();
		localChangeManager.removeBindings(keySequence, schemeId, contextId,
				null, null, null, Binding.USER);
		localChangeManager.addBinding(new KeyBinding(keySequence, null,
				schemeId, contextId, null, null, null, Binding.USER));
		update();
	}

	private void selectedButtonRestore() {
		String contextId = getContextId();
		String schemeId = getSchemeId();
		KeySequence keySequence = getKeySequence();
		localChangeManager.removeBindings(keySequence, schemeId, contextId,
				null, null, null, Binding.USER);
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
		final int selection = tableAssignmentsForCommand.getSelectionIndex();
		if ((selection >= 0)
				&& (selection < tableAssignmentsForCommand.getItemCount())) {
			final TableItem item = tableAssignmentsForCommand
					.getItem(selection);
			final KeyBinding binding = (KeyBinding) item.getData(ITEM_DATA_KEY);
			setContextId(binding.getContextId());
			setKeySequence(binding.getKeySequence());
		}

		update();
	}

	private void selectedTableAssignmentsForKeySequence() {
		final int selection = tableAssignmentsForKeySequence
				.getSelectionIndex();
		if ((selection >= 0)
				&& (selection < tableAssignmentsForKeySequence.getItemCount())) {
			final TableItem item = tableAssignmentsForKeySequence
					.getItem(selection);
			final Binding binding = (Binding) item.getData(ITEM_DATA_KEY);
			setContextId(binding.getContextId());
		}

		update();
	}

	/**
	 * Responds to some kind of trigger on the View tab by taking the current
	 * selection on the key bindings table and selecting the appropriate items
	 * in the Modify tab.
	 * 
	 * @since 3.1
	 */
	private final void selectedTableKeyBindings() {
		final int selectionIndex = tableBindings.getSelectionIndex();
		if (selectionIndex != -1) {
			final TableItem item = tableBindings.getItem(selectionIndex);
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

	private void setKeySequence(KeySequence keySequence) {
		textTriggerSequenceManager.setKeySequence(keySequence);
	}

	private void setScheme(Scheme scheme) {
		comboScheme.clearSelection();
		comboScheme.deselectAll();
		String schemeUniqueName = (String) schemeUniqueNamesById.get(scheme
				.getId());

		if (schemeUniqueName != null) {
			String items[] = comboScheme.getItems();

			for (int i = 0; i < items.length; i++)
				if (schemeUniqueName.equals(items[i])) {
					comboScheme.select(i);
					break;
				}
		}
	}

	public void setVisible(boolean visible) {
		if (visible == true) {
			Map contextsByName = new HashMap();

			for (Iterator iterator = contextService.getDefinedContextIds()
					.iterator(); iterator.hasNext();) {
				Context context = contextService.getContext((String) iterator
						.next());
				try {
					String name = context.getName();
					Collection contexts = (Collection) contextsByName.get(name);

					if (contexts == null) {
						contexts = new HashSet();
						contextsByName.put(name, contexts);
					}

					contexts.add(context);
				} catch (final NotDefinedException e) {
					// Do nothing.
				}
			}

			Map categoriesByName = new HashMap();

			for (Iterator iterator = commandService.getDefinedCategoryIds()
					.iterator(); iterator.hasNext();) {
				Category category = commandService
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
				} catch (NotDefinedException eNotDefined) {
					// Do nothing
				}
			}

			Map commandsByName = new HashMap();

			for (Iterator iterator = commandService.getDefinedCommandIds()
					.iterator(); iterator.hasNext();) {
				Command command = commandService.getCommand((String) iterator
						.next());

				try {
					String name = command.getName();
					Collection commands = (Collection) commandsByName.get(name);

					if (commands == null) {
						commands = new HashSet();
						commandsByName.put(name, commands);
					}

					commands.add(command);
				} catch (NotDefinedException eNotDefined) {
					// Do nothing
				}
			}

			Map schemesByName = new HashMap();

			for (Iterator iterator = bindingService.getDefinedSchemeIds()
					.iterator(); iterator.hasNext();) {
				Scheme scheme = bindingService.getScheme((String) iterator
						.next());
				try {
					String name = scheme.getName();
					Collection schemes = (Collection) schemesByName.get(name);

					if (schemes == null) {
						schemes = new HashSet();
						schemesByName.put(name, schemes);
					}

					schemes.add(scheme);
				} catch (final NotDefinedException e) {
					// Do nothing.
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
					Context context = (Context) iterator2.next();
					contextIdsByUniqueName.put(name, context.getId());
					contextUniqueNamesById.put(context.getId(), name);
				} else
					while (iterator2.hasNext()) {
						Context context = (Context) iterator2.next();
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
					Category category = (Category) iterator2.next();
					categoryIdsByUniqueName.put(name, category.getId());
					categoryUniqueNamesById.put(category.getId(), name);
				} else
					while (iterator2.hasNext()) {
						Category category = (Category) iterator2.next();
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
					Command command = (Command) iterator2.next();
					commandIdsByUniqueName.put(name, command.getId());
					commandUniqueNamesById.put(command.getId(), name);
				} else
					while (iterator2.hasNext()) {
						Command command = (Command) iterator2.next();
						String uniqueName = MessageFormat.format(
								Util.translateString(RESOURCE_BUNDLE,
										"uniqueName"), new Object[] { name, //$NON-NLS-1$
										command.getId() });
						commandIdsByUniqueName.put(uniqueName, command.getId());
						commandUniqueNamesById.put(command.getId(), uniqueName);
					}
			}

			schemeIdsByUniqueName = new HashMap();
			schemeUniqueNamesById = new HashMap();

			for (Iterator iterator = schemesByName.entrySet().iterator(); iterator
					.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set keyConfigurations = (Set) entry.getValue();
				Iterator iterator2 = keyConfigurations.iterator();

				if (keyConfigurations.size() == 1) {
					Scheme scheme = (Scheme) iterator2.next();
					schemeIdsByUniqueName.put(name, scheme.getId());
					schemeUniqueNamesById.put(scheme.getId(), name);
				} else
					while (iterator2.hasNext()) {
						Scheme scheme = (Scheme) iterator2.next();
						String uniqueName = MessageFormat.format(
								Util.translateString(RESOURCE_BUNDLE,
										"uniqueName"), new Object[] { name, //$NON-NLS-1$
										scheme.getId() });
						schemeIdsByUniqueName.put(uniqueName, scheme.getId());
						schemeUniqueNamesById.put(scheme.getId(), uniqueName);
					}
			}

			Scheme activeScheme = bindingService.getActiveScheme();
			commandIdsByCategoryId = new HashMap();

			for (Iterator iterator = commandService.getDefinedCommandIds()
					.iterator(); iterator.hasNext();) {
				Command command = commandService.getCommand((String) iterator
						.next());

				try {
					String categoryId = command.getCategory().getId();
					Collection commandIds = (Collection) commandIdsByCategoryId
							.get(categoryId);

					if (commandIds == null) {
						commandIds = new HashSet();
						commandIdsByCategoryId.put(categoryId, commandIds);
					}

					commandIds.add(command.getId());
				} catch (NotDefinedException eNotDefined) {
					// Do nothing
				}
			}

			// Make an internal copy of the binding manager, for local changes.
			try {
				final Collection definedSchemeIds = bindingService
						.getDefinedSchemeIds();
				final Iterator definedSchemeIdItr = definedSchemeIds.iterator();
				while (definedSchemeIdItr.hasNext()) {
					final String definedSchemeId = (String) definedSchemeIdItr
							.next();
					final Scheme scheme = bindingService
							.getScheme(definedSchemeId);
					final Scheme copy = localChangeManager
							.getScheme(definedSchemeId);
					copy.define(scheme.getName(), scheme.getDescription(),
							scheme.getParentId());
				}
				localChangeManager.setActiveScheme(bindingService
						.getActiveScheme().getId());
			} catch (final NotDefinedException e) {
				throw new Error(
						"There is a programmer error in the keys preference page"); //$NON-NLS-1$
			}
			localChangeManager.setBindings(bindingService.getBindings());
			localChangeManager.setLocale(bindingService.getLocale());
			localChangeManager.setPlatform(bindingService.getPlatform());

			// Populate the category combo box.
			List categoryNames = new ArrayList(categoryIdsByUniqueName.keySet());
			Collections.sort(categoryNames, Collator.getInstance());
			if (commandIdsByCategoryId.containsKey(null)) {
				categoryNames.add(0, Util.translateString(RESOURCE_BUNDLE,
						"other")); //$NON-NLS-1$
			}
			comboCategory.setItems((String[]) categoryNames
					.toArray(new String[categoryNames.size()]));
			comboCategory.clearSelection();
			comboCategory.deselectAll();
			if (commandIdsByCategoryId.containsKey(null)
					|| !categoryNames.isEmpty()) {
				comboCategory.select(0);
			}

			// Populate the scheme combo box.
			List schemeNames = new ArrayList(schemeIdsByUniqueName.keySet());
			Collections.sort(schemeNames, Collator.getInstance());
			comboScheme.setItems((String[]) schemeNames
					.toArray(new String[schemeNames.size()]));
			setScheme(activeScheme);

			// Update the entire page.
			update();
		}

		super.setVisible(visible);
	}

	private void update() {
		updateViewTab();
		setCommandsForCategory();
		setContextsForCommand();
		TriggerSequence triggerSequence = getKeySequence();
		buildKeySequenceAssignmentsTable();
		buildCommandAssignmentsTable();
		String commandId = getCommandId();
		String contextId = getContextId();
		selectAssignmentForKeySequence(contextId);
		selectAssignmentForCommand(contextId);
		updateLabelKeyConfigurationExtends();
		updateLabelContextExtends();
		labelAssignmentsForKeySequence.setEnabled(triggerSequence != null
				&& !triggerSequence.getTriggers().isEmpty());
		tableAssignmentsForKeySequence.setEnabled(triggerSequence != null
				&& !triggerSequence.getTriggers().isEmpty());
		labelAssignmentsForCommand.setEnabled(commandId != null);
		tableAssignmentsForCommand.setEnabled(commandId != null);
		boolean buttonsEnabled = commandId != null && triggerSequence != null
				&& !triggerSequence.getTriggers().isEmpty();
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
			Context context = contextService.getContext(getContextId());

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
				} catch (org.eclipse.core.commands.common.NotDefinedException e) {
					// Do nothing
				}
			}
		}

		labelContextExtends.setText(Util.ZERO_LENGTH_STRING);
	}

	private void updateLabelKeyConfigurationExtends() {
		String schemeId = getSchemeId();

		if (schemeId != null) {
			Scheme scheme = bindingService.getScheme(schemeId);

			try {
				String name = (String) schemeUniqueNamesById.get(scheme
						.getParentId());

				if (name != null) {
					labelKeyConfigurationExtends.setText(MessageFormat.format(
							Util.translateString(RESOURCE_BUNDLE, "extends"), //$NON-NLS-1$
							new Object[] { name }));
					return;
				}
			} catch (org.eclipse.core.commands.common.NotDefinedException e) {
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
		tableBindings.removeAll();

		// Get a sorted list of key binding contents.
		final List bindings = new ArrayList(bindingService
				.getActiveBindingsDisregardingContext());
		Collections.sort(bindings, new Comparator() {
			/**
			 * Compares two instances of <code>Binding</code> based on the
			 * current sort order.
			 * 
			 * @param object1
			 *            The first object to compare; must be an instance of
			 *            <code>Binding</code> (i.e., not <code>null</code>).
			 * @param object2
			 *            The second object to compare; must be an instance of
			 *            <code>Binding</code> (i.e., not <code>null</code>).
			 * @return The integer value representing the comparison. The
			 *         comparison is based on the current sort order.
			 * @since 3.1
			 */
			public final int compare(final Object object1, final Object object2) {
				final Binding binding1 = (Binding) object1;
				final Binding binding2 = (Binding) object2;

				/*
				 * Get the category name, command name, formatted key sequence
				 * and context name for the first binding.
				 */
				final String commandId1 = binding1.getCommandId();
				String categoryName1 = Util.ZERO_LENGTH_STRING;
				String commandName1 = Util.ZERO_LENGTH_STRING;
				if (commandId1 != null) {
					final Command command = commandService
							.getCommand(commandId1);
					try {
						commandName1 = command.getName();
						categoryName1 = command.getCategory().getName();
					} catch (final NotDefinedException e) {
						// Just use the zero-length string.
					}
				}
				final String triggerSequence1 = binding1.getTriggerSequence()
						.format();
				final String contextId1 = binding1.getContextId();
				String contextName1 = Util.ZERO_LENGTH_STRING;
				if (contextId1 != null) {
					final Context context = contextService
							.getContext(contextId1);
					try {
						contextName1 = context.getName();
					} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
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
					final Command command = commandService
							.getCommand(commandId2);
					try {
						commandName2 = command.getName();
						categoryName2 = command.getCategory().getName();
					} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
						// Just use the zero-length string.
					}
				}
				final String keySequence2 = binding2.getTriggerSequence()
						.format();
				final String contextId2 = binding2.getContextId();
				String contextName2 = Util.ZERO_LENGTH_STRING;
				if (contextId2 != null) {
					final Context context = contextService
							.getContext(contextId2);
					try {
						contextName2 = context.getName();
					} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
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
						compare = Util.compare(triggerSequence1, keySequence2);
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
				final Command command = commandService.getCommand(commandId);
				try {
					commandName = command.getName();
					categoryName = command.getCategory().getName();
				} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
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
				final Context context = contextService.getContext(contextId);
				try {
					contextName = context.getName();
				} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
					// Just use the zero-length string.
				}
			}

			// Create the table item.
			final TableItem item = new TableItem(tableBindings, SWT.NONE);
			item.setText(VIEW_CATEGORY_COLUMN_INDEX, categoryName);
			item.setText(VIEW_COMMAND_COLUMN_INDEX, commandName);
			item.setText(VIEW_KEY_SEQUENCE_COLUMN_INDEX, keyBinding
					.getKeySequence().format());
			item.setText(VIEW_CONTEXT_COLUMN_INDEX, contextName);
		}

		// Pack the columns.
		for (int i = 0; i < tableBindings.getColumnCount(); i++) {
			tableBindings.getColumn(i).pack();
		}
	}
}
