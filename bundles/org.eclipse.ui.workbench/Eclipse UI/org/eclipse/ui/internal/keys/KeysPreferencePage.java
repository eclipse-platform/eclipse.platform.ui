/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.statushandlers.StatusManager;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.MessageFormat;

/**
 * The preference page for defining keyboard shortcuts. While some of its
 * underpinning have been made generic to "bindings" rather than "key bindings",
 * it will still take some work to remove the link entirely.
 * 
 * @since 3.0
 */
public final class KeysPreferencePage extends PreferencePage implements
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

	/**
	 * The data key for the binding stored on an SWT widget. The key is a
	 * fully-qualified name, but in reverse order. This is so that the equals
	 * method will detect misses faster.
	 */
	private static final String BINDING_KEY = "Binding.bindings.jface.eclipse.org"; //$NON-NLS-1$

	/**
	 * The image associate with a binding that exists as part of the system
	 * definition.
	 */
	private static final Image IMAGE_BLANK = ImageFactory.getImage("blank"); //$NON-NLS-1$

	/**
	 * The image associated with a binding changed by the user.
	 */
	private static final Image IMAGE_CHANGE = ImageFactory.getImage("change"); //$NON-NLS-1$

	/**
	 * The data key at which the <code>Binding</code> instance for a table
	 * item is stored.
	 */
	private static final String ITEM_DATA_KEY = "org.eclipse.jface.bindings"; //$NON-NLS-1$

	/**
	 * The number of items to show in the combo boxes.
	 */
	private static final int ITEMS_TO_SHOW = 9;

	/**
	 * The resource bundle from which translations can be retrieved.
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(KeysPreferencePage.class.getName());

	/**
	 * The total number of columns on the view tab.
	 */
	private static final int VIEW_TOTAL_COLUMNS = 4;

	/**
	 * The translated names for the columns when they are the primary sort key
	 * (e.g., ">Category<").
	 */
	private static final String[] SORTED_COLUMN_NAMES = new String[VIEW_TOTAL_COLUMNS];

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
	private static final String[] UNSORTED_COLUMN_NAMES = new String[VIEW_TOTAL_COLUMNS];

	/**
	 * The index of the column on the view tab containing the category name.
	 */
	private static final int VIEW_CATEGORY_COLUMN_INDEX = 0;

	/**
	 * The index of the column on the view tab containing the command name.
	 */
	private static final int VIEW_COMMAND_COLUMN_INDEX = 1;

	/**
	 * The index of the column on the view tab containing the context name.
	 */
	private static final int VIEW_CONTEXT_COLUMN_INDEX = 3;

	/**
	 * The index of the column on the view tab containing the key sequence.
	 */
	private static final int VIEW_KEY_SEQUENCE_COLUMN_INDEX = 2;

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

	/**
	 * The workbench's activity manager. This activity manager is used to see if
	 * certain commands should be filtered from the user interface.
	 */
	private IActivityManager activityManager;

	/**
	 * The workbench's binding service. This binding service is used to access
	 * the current set of bindings, and to persist changes.
	 */
	private IBindingService bindingService;

	/**
	 * The add button located on the bottom left of the preference page. This
	 * button adds the current trigger sequence to the currently selected
	 * command.
	 */
	private Button buttonAdd;

	/**
	 * The remove button located on the bottom left of the preference page. This
	 * button removes the current trigger sequence from the current command.
	 */
	private Button buttonRemove;

	/**
	 * The restore button located on the bottom left of the preference page.
	 * This button attempts to restore the currently trigger sequence to its
	 * initial (i.e., Binding.SYSTEM) state -- undoing all user modifications.
	 */
	private Button buttonRestore;

	/**
	 * A map of all the category identifiers indexed by the names that appear in
	 * the user interface. This look-up table is built during initialization.
	 */
	private Map categoryIdsByUniqueName;

	/**
	 * A map of all the category names in the user interface indexed by their
	 * identifiers. This look-up table is built during initialization.
	 */
	private Map categoryUniqueNamesById;

	/**
	 * The combo box containing the list of all categories for commands.
	 */
	private Combo comboCategory;

	/**
	 * The combo box containing the list of commands relevent for the currently
	 * selected category.
	 */
	private Combo comboCommand;

	/**
	 * The combo box containing the list of contexts in the system.
	 */
	private Combo comboContext;

	/**
	 * The combo box containing the list of schemes in the system.
	 */
	private Combo comboScheme;

	/**
	 * A map of all the command identifiers indexed by the categories to which
	 * they belong. This look-up table is built during initialization.
	 */
	private Map commandIdsByCategoryId;

	/**
	 * The parameterized commands corresponding to the current contents of
	 * <code>comboCommand</code>. The commands in this array are in the same
	 * order as in the combo. This value can be <code>null</code> if nothing
	 * is selected in the combo.
	 */
	private ParameterizedCommand[] commands = null;

	/**
	 * The workbench's command service. This command service is used to access
	 * the list of commands.
	 */
	private ICommandService commandService;

	/**
	 * A map of all the context identifiers indexed by the names that appear in
	 * the user interface. This look-up table is built during initialization.
	 */
	private Map contextIdsByUniqueName;

	/**
	 * The workbench's context service. This context service is used to access
	 * the list of contexts.
	 */
	private IContextService contextService;

	/**
	 * A map of all the category names in the user interface indexed by their
	 * identifiers. This look-up table is built during initialization.
	 */
	private Map contextUniqueNamesById;

	/**
	 * The workbench's help system. This is used to register the page with the
	 * help system.
	 * 
	 * TODO Add a help context
	 */
	// private IWorkbenchHelpSystem helpSystem;
	/**
	 * This is the label next to the table showing the bindings matching a
	 * particular command. The label is disabled if there isn't a selected
	 * command identifier.
	 */
	private Label labelBindingsForCommand;

	/**
	 * This is the label next to the table showing the bindings matching a
	 * particular trigger sequence. The label is disabled if there isn't a
	 * current key sequence.
	 */
	private Label labelBindingsForTriggerSequence;

	/**
	 * The label next to the context combo box. This label indicates whether the
	 * context is a child of another context. If the current context is not a
	 * child, then this label is blank.
	 */
	private Label labelContextExtends;

	/**
	 * The label next to the scheme combo box. This label indicates whether the
	 * scheme is a child of another scheme. If the current scheme is not a
	 * child, then this label is blank.
	 */
	private Label labelSchemeExtends;

	/**
	 * A binding manager local to this preference page. When the page is
	 * initialized, the current bindings are read out from the binding service
	 * and placed in this manager. This manager is then updated as the user
	 * makes changes. When the user has finished, the contents of this manager
	 * are compared with the contents of the binding service. The changes are
	 * then persisted.
	 */
	private final BindingManager localChangeManager = new BindingManager(
			new ContextManager(), new CommandManager());

	/**
	 * A map of all the scheme identifiers indexed by the names that appear in
	 * the user interface. This look-up table is built during initialization.
	 */
	private Map schemeIdsByUniqueName;

	/**
	 * A map of all the scheme names in the user interface indexed by their
	 * identifiers. This look-up table is built during initialization.
	 */
	private Map schemeUniqueNamesById;

	/**
	 * The sort order to be used on the view tab to display all of the key
	 * bindings. This sort order can be changed by the user. This array is never
	 * <code>null</code>, but may be empty.
	 */
	private int[] sortOrder = { VIEW_CATEGORY_COLUMN_INDEX,
			VIEW_COMMAND_COLUMN_INDEX, VIEW_KEY_SEQUENCE_COLUMN_INDEX,
			VIEW_CONTEXT_COLUMN_INDEX };

	/**
	 * The top-most tab folder for the preference page -- containing a view and
	 * a modify tab.
	 */
	private TabFolder tabFolder;

	/**
	 * A table of the key bindings currently defined. This table appears on the
	 * view tab; it is intended to be an easy way for users to learn the key
	 * bindings in Eclipse. This value is only <code>null</code> until the
	 * controls are first created.
	 */
	private Table tableBindings;

	/**
	 * The table containing all of the bindings matching the selected command.
	 */
	private Table tableBindingsForCommand;

	/**
	 * The table containing all of the bindings matching the current trigger
	 * sequence.
	 */
	private Table tableBindingsForTriggerSequence;

	/**
	 * The text widget where keys are entered. This widget is managed by
	 * <code>textTriggerSequenceManager</code>, which provides its special
	 * behaviour.
	 */
	private Text textTriggerSequence;

	/**
	 * The manager for the text widget that traps incoming key events. This
	 * manager should be used to access the widget, rather than accessing the
	 * widget directly.
	 */
	private KeySequenceText textTriggerSequenceManager;

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#applyData(java.lang.Object)
	 */
	public void applyData(Object data) {
		if(data instanceof Binding) {
			editBinding((Binding) data);
		}
	}
	protected final Control createContents(final Composite parent) {
		
		PlatformUI.getWorkbench().getHelpSystem()
			.setHelp(parent, IWorkbenchHelpContextIds.KEYS_PREFERENCE_PAGE);
		
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

	/**
	 * Creates the tab that allows the user to change the keyboard shortcuts.
	 * 
	 * @param parent
	 *            The tab folder in which the tab should be created; must not be
	 *            <code>null</code>.
	 * @return The composite which represents the contents of the tab; never
	 *         <code>null</code>.
	 */
	private final Composite createModifyTab(final TabFolder parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		GridData gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);
		final Composite compositeKeyConfiguration = new Composite(composite,
				SWT.NULL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		compositeKeyConfiguration.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		compositeKeyConfiguration.setLayoutData(gridData);
		final Label labelKeyConfiguration = new Label(
				compositeKeyConfiguration, SWT.LEFT);
		labelKeyConfiguration.setText(Util.translateString(RESOURCE_BUNDLE,
				"labelScheme")); //$NON-NLS-1$
		comboScheme = new Combo(compositeKeyConfiguration, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 200;
		comboScheme.setLayoutData(gridData);
		comboScheme.setVisibleItemCount(ITEMS_TO_SHOW);

		comboScheme.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(final SelectionEvent e) {
				selectedComboScheme();
			}
		});

		labelSchemeExtends = new Label(compositeKeyConfiguration, SWT.LEFT);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		labelSchemeExtends.setLayoutData(gridData);
		final Control spacer = new Composite(composite, SWT.NULL);
		gridData = new GridData();
		gridData.heightHint = 10;
		gridData.widthHint = 10;
		spacer.setLayoutData(gridData);
		final Group groupCommand = new Group(composite, SWT.SHADOW_NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		groupCommand.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_BOTH);
		groupCommand.setLayoutData(gridData);
		groupCommand.setText(Util.translateString(RESOURCE_BUNDLE,
				"groupCommand")); //$NON-NLS-1$	
		final Label labelCategory = new Label(groupCommand, SWT.LEFT);
		gridData = new GridData();
		labelCategory.setLayoutData(gridData);
		labelCategory.setText(Util.translateString(RESOURCE_BUNDLE,
				"labelCategory")); //$NON-NLS-1$
		comboCategory = new Combo(groupCommand, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 200;
		comboCategory.setLayoutData(gridData);
		comboCategory.setVisibleItemCount(ITEMS_TO_SHOW);

		comboCategory.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(final SelectionEvent e) {
				update();
			}
		});

		final Label labelCommand = new Label(groupCommand, SWT.LEFT);
		gridData = new GridData();
		labelCommand.setLayoutData(gridData);
		labelCommand.setText(Util.translateString(RESOURCE_BUNDLE,
				"labelCommand")); //$NON-NLS-1$
		comboCommand = new Combo(groupCommand, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 300;
		comboCommand.setLayoutData(gridData);
		comboCommand.setVisibleItemCount(9);

		comboCommand.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(final SelectionEvent e) {
				update();
			}
		});

		labelBindingsForCommand = new Label(groupCommand, SWT.LEFT);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		gridData.verticalAlignment = GridData.FILL_VERTICAL;
		labelBindingsForCommand.setLayoutData(gridData);
		labelBindingsForCommand.setText(Util.translateString(RESOURCE_BUNDLE,
				"labelAssignmentsForCommand")); //$NON-NLS-1$
		tableBindingsForCommand = new Table(groupCommand, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableBindingsForCommand.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		gridData.horizontalSpan = 2;
		gridData.widthHint = "carbon".equals(SWT.getPlatform()) ? 620 : 520; //$NON-NLS-1$
		tableBindingsForCommand.setLayoutData(gridData);
		TableColumn tableColumnDelta = new TableColumn(tableBindingsForCommand,
				SWT.NULL, 0);
		tableColumnDelta.setResizable(false);
		tableColumnDelta.setText(Util.ZERO_LENGTH_STRING);
		tableColumnDelta.setWidth(20);
		TableColumn tableColumnContext = new TableColumn(
				tableBindingsForCommand, SWT.NULL, 1);
		tableColumnContext.setResizable(true);
		tableColumnContext.setText(Util.translateString(RESOURCE_BUNDLE,
				"tableColumnContext")); //$NON-NLS-1$
		tableColumnContext.pack();
		tableColumnContext.setWidth(200);
		final TableColumn tableColumnKeySequence = new TableColumn(
				tableBindingsForCommand, SWT.NULL, 2);
		tableColumnKeySequence.setResizable(true);
		tableColumnKeySequence.setText(Util.translateString(RESOURCE_BUNDLE,
				"tableColumnKeySequence")); //$NON-NLS-1$
		tableColumnKeySequence.pack();
		tableColumnKeySequence.setWidth(300);

		tableBindingsForCommand.addMouseListener(new MouseAdapter() {

			public void mouseDoubleClick(MouseEvent mouseEvent) {
				update();
			}
		});

		tableBindingsForCommand.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedTableBindingsForCommand();
			}
		});

		final Group groupKeySequence = new Group(composite, SWT.SHADOW_NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		groupKeySequence.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_BOTH);
		groupKeySequence.setLayoutData(gridData);
		groupKeySequence.setText(Util.translateString(RESOURCE_BUNDLE,
				"groupKeySequence")); //$NON-NLS-1$	
		final Label labelKeySequence = new Label(groupKeySequence, SWT.LEFT);
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
				update();
			}
		});
		textTriggerSequence.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				bindingService.setKeyFilterEnabled(false);
			}

			public void focusLost(FocusEvent e) {
				bindingService.setKeyFilterEnabled(true);
			}
		});
		textTriggerSequence.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (!bindingService.isKeyFilterEnabled()) {
					bindingService.setKeyFilterEnabled(true);
				}
			}
		});

		// The manager for the key sequence text widget.
		textTriggerSequenceManager = new KeySequenceText(textTriggerSequence);
		textTriggerSequenceManager.setKeyStrokeLimit(4);

		// Button for adding trapped key strokes
		final Button buttonAddKey = new Button(groupKeySequence, SWT.LEFT
				| SWT.ARROW);
		buttonAddKey.setToolTipText(Util.translateString(RESOURCE_BUNDLE,
				"buttonAddKey.ToolTipText")); //$NON-NLS-1$
		gridData = new GridData();
		gridData.heightHint = comboCategory.getTextHeight();
		buttonAddKey.setLayoutData(gridData);

		// Arrow buttons aren't normally added to the tab list. Let's fix that.
		final Control[] tabStops = groupKeySequence.getTabList();
		final ArrayList newTabStops = new ArrayList();
		for (int i = 0; i < tabStops.length; i++) {
			Control tabStop = tabStops[i];
			newTabStops.add(tabStop);
			if (textTriggerSequence.equals(tabStop)) {
				newTabStops.add(buttonAddKey);
			}
		}
		final Control[] newTabStopArray = (Control[]) newTabStops
				.toArray(new Control[newTabStops.size()]);
		groupKeySequence.setTabList(newTabStopArray);

		// Construct the menu to attach to the above button.
		final Menu menuButtonAddKey = new Menu(buttonAddKey);
		final Iterator trappedKeyItr = KeySequenceText.TRAPPED_KEYS.iterator();
		while (trappedKeyItr.hasNext()) {
			final KeyStroke trappedKey = (KeyStroke) trappedKeyItr.next();
			final MenuItem menuItem = new MenuItem(menuButtonAddKey, SWT.PUSH);
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

		labelBindingsForTriggerSequence = new Label(groupKeySequence, SWT.LEFT);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		gridData.verticalAlignment = GridData.FILL_VERTICAL;
		labelBindingsForTriggerSequence.setLayoutData(gridData);
		labelBindingsForTriggerSequence.setText(Util.translateString(
				RESOURCE_BUNDLE, "labelAssignmentsForKeySequence")); //$NON-NLS-1$
		tableBindingsForTriggerSequence = new Table(groupKeySequence,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableBindingsForTriggerSequence.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		gridData.horizontalSpan = 3;
		gridData.widthHint = "carbon".equals(SWT.getPlatform()) ? 620 : 520; //$NON-NLS-1$
		tableBindingsForTriggerSequence.setLayoutData(gridData);
		tableColumnDelta = new TableColumn(tableBindingsForTriggerSequence,
				SWT.NULL, 0);
		tableColumnDelta.setResizable(false);
		tableColumnDelta.setText(Util.ZERO_LENGTH_STRING);
		tableColumnDelta.setWidth(20);
		tableColumnContext = new TableColumn(tableBindingsForTriggerSequence,
				SWT.NULL, 1);
		tableColumnContext.setResizable(true);
		tableColumnContext.setText(Util.translateString(RESOURCE_BUNDLE,
				"tableColumnContext")); //$NON-NLS-1$
		tableColumnContext.pack();
		tableColumnContext.setWidth(200);
		final TableColumn tableColumnCommand = new TableColumn(
				tableBindingsForTriggerSequence, SWT.NULL, 2);
		tableColumnCommand.setResizable(true);
		tableColumnCommand.setText(Util.translateString(RESOURCE_BUNDLE,
				"tableColumnCommand")); //$NON-NLS-1$
		tableColumnCommand.pack();
		tableColumnCommand.setWidth(300);

		tableBindingsForTriggerSequence.addMouseListener(new MouseAdapter() {

			public void mouseDoubleClick(MouseEvent mouseEvent) {
				update();
			}
		});

		tableBindingsForTriggerSequence
				.addSelectionListener(new SelectionAdapter() {

					public void widgetSelected(SelectionEvent selectionEvent) {
						selectedTableBindingsForTriggerSequence();
					}
				});

		final Composite compositeContext = new Composite(composite, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		compositeContext.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		compositeContext.setLayoutData(gridData);
		final Label labelContext = new Label(compositeContext, SWT.LEFT);
		labelContext.setText(Util.translateString(RESOURCE_BUNDLE,
				"labelContext")); //$NON-NLS-1$
		comboContext = new Combo(compositeContext, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 250;
		comboContext.setLayoutData(gridData);
		comboContext.setVisibleItemCount(ITEMS_TO_SHOW);

		comboContext.addSelectionListener(new SelectionAdapter() {
			public final void widgetSelected(final SelectionEvent e) {
				update();
			}
		});

		labelContextExtends = new Label(compositeContext, SWT.LEFT);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		labelContextExtends.setLayoutData(gridData);
		final Composite compositeButton = new Composite(composite, SWT.NULL);
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
		editButton.setText(Util.translateString(RESOURCE_BUNDLE, "buttonEdit")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, editButton.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		editButton.setLayoutData(gridData);
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
		buttonExport.setText(Util.translateString(RESOURCE_BUNDLE,
				"buttonExport")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonExport.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonExport.setLayoutData(gridData);
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
		return PrefUtil.getInternalPreferenceStore();
	}

	/**
	 * Allows the user to change the key bindings for a particular command.
	 * Switches the tab to the modify tab, and then selects the category and
	 * command that corresponds with the given command name. It then selects the
	 * given key sequence and gives focus to the key sequence text widget.
	 * 
	 * @param binding
	 *            The binding to be edited; if <code>null</code>, then just
	 *            switch to the modify tab. If the <code>binding</code> does
	 *            not correspond to anything in the keys preference page, then
	 *            this also just switches to the modify tab.
	 * @since 3.1
	 */
	public final void editBinding(final Binding binding) {
		// Switch to the modify tab.
		tabFolder.setSelection(TAB_INDEX_MODIFY);

		// If there is no command name, stop here.
		if (binding == null) {
			return;
		}

		/*
		 * Get the corresponding category and command names. If either is
		 * undefined, then we can just stop now. We won't be able to find their
		 * name.
		 */
		final ParameterizedCommand command = binding.getParameterizedCommand();
		String categoryName = null;
		String commandName = null;
		try {
			categoryName = command.getCommand().getCategory().getName();
			commandName = command.getName();
		} catch (final NotDefinedException e) {
			return; // no name
		}

		// Update the category combo box.
		final String[] categoryNames = comboCategory.getItems();
		int i = 0;
		for (; i < categoryNames.length; i++) {
			if (categoryName.equals(categoryNames[i])) {
				break;
			}
		}
		if (i >= comboCategory.getItemCount()) {
			// Couldn't find the category, so abort.
			return;
		}
		comboCategory.select(i);

		// Update the commands combo box.
		updateComboCommand();

		// Update the command combo box.
		final String[] commandNames = comboCommand.getItems();
		int j = 0;
		for (; j < commandNames.length; j++) {
			if (commandName.equals(commandNames[j])) {
				if (comboCommand.getSelectionIndex() != j) {
					comboCommand.select(j);
				}
				break;
			}
		}
		if (j >= comboCommand.getItemCount()) {
			// Couldn't find the command, so just select the first and then stop
			if (comboCommand.getSelectionIndex() != 0) {
				comboCommand.select(0);
			}
			update();
			return;
		}

		/*
		 * Update and validate the state of the modify tab in response to these
		 * selection changes.
		 */
		update();

		// Select the right key binding, if possible.
		final TableItem[] items = tableBindingsForCommand.getItems();
		int k = 0;
		for (; k < items.length; k++) {
			final String currentKeySequence = items[k].getText(2);
			if (binding.getTriggerSequence().format()
					.equals(currentKeySequence)) {
				break;
			}
		}
		if (k < tableBindingsForCommand.getItemCount()) {
			tableBindingsForCommand.select(k);
			tableBindingsForCommand.notifyListeners(SWT.Selection, null);
			textTriggerSequence.setFocus();
		}
	}

	/**
	 * Returns the identifier for the currently selected category.
	 * 
	 * @return The selected category; <code>null</code> if none.
	 */
	private final String getCategoryId() {
		return !commandIdsByCategoryId.containsKey(null)
				|| comboCategory.getSelectionIndex() > 0 ? (String) categoryIdsByUniqueName
				.get(comboCategory.getText())
				: null;
	}

	/**
	 * Returns the identifier for the currently selected context.
	 * 
	 * @return The selected context; <code>null</code> if none.
	 */
	private final String getContextId() {
		return comboContext.getSelectionIndex() >= 0 ? (String) contextIdsByUniqueName
				.get(comboContext.getText())
				: null;
	}

	/**
	 * Returns the current trigger sequence.
	 * 
	 * @return The trigger sequence; may be empty, but never <code>null</code>.
	 */
	private final KeySequence getKeySequence() {
		return textTriggerSequenceManager.getKeySequence();
	}

	/**
	 * Returns the currently-selected fully-parameterized command.
	 * 
	 * @return The selected fully-parameterized command; <code>null</code> if
	 *         none.
	 */
	private final ParameterizedCommand getParameterizedCommand() {
		final int selectionIndex = comboCommand.getSelectionIndex();
		if ((selectionIndex >= 0) && (commands != null)
				&& (selectionIndex < commands.length)) {
			return commands[selectionIndex];
		}

		return null;
	}

	/**
	 * Returns the identifier for the currently selected scheme.
	 * 
	 * @return The selected scheme; <code>null</code> if none.
	 */
	private final String getSchemeId() {
		return comboScheme.getSelectionIndex() >= 0 ? (String) schemeIdsByUniqueName
				.get(comboScheme.getText())
				: null;
	}

	public final void init(final IWorkbench workbench) {
		activityManager = workbench.getActivitySupport().getActivityManager();
		bindingService = (IBindingService) workbench.getService(IBindingService.class);
		commandService = (ICommandService) workbench.getService(ICommandService.class);
		contextService = (IContextService) workbench.getService(IContextService.class);
	}

	/**
	 * Checks whether the activity manager knows anything about this command
	 * identifier. If the activity manager is currently filtering this command,
	 * then it does not appear in the user interface.
	 * 
	 * @param command
	 *            The command which should be checked against the activities;
	 *            must not be <code>null</code>.
	 * @return <code>true</code> if the command identifier is not filtered;
	 *         <code>false</code> if it is
	 */
	private final boolean isActive(final Command command) {
		return activityManager.getIdentifier(command.getId()).isEnabled();
	}

	/**
	 * Logs the given exception, and opens an error dialog saying that something
	 * went wrong. The exception is assumed to have something to do with the
	 * preference store.
	 * 
	 * @param exception
	 *            The exception to be logged; must not be <code>null</code>.
	 */
	private final void logPreferenceStoreException(final Throwable exception) {
		final String message = Util.translateString(RESOURCE_BUNDLE,
				"PreferenceStoreError.Message"); //$NON-NLS-1$
		String exceptionMessage = exception.getMessage();
		if (exceptionMessage == null) {
			exceptionMessage = message;
		}
		final IStatus status = new Status(IStatus.ERROR,
				WorkbenchPlugin.PI_WORKBENCH, 0, exceptionMessage, exception);
		WorkbenchPlugin.log(message, status);
		StatusUtil.handleStatus(message, exception, StatusManager.SHOW);
	}

	public final boolean performCancel() {
		// Save the selected tab for future reference.
		persistSelectedTab();

		return super.performCancel();
	}

	protected final void performDefaults() {
		// Ask the user to confirm
		final String title = Util.translateString(RESOURCE_BUNDLE,
				"restoreDefaultsMessageBoxText"); //$NON-NLS-1$
		final String message = Util.translateString(RESOURCE_BUNDLE,
				"restoreDefaultsMessageBoxMessage"); //$NON-NLS-1$
		final boolean confirmed = MessageDialog.openConfirm(getShell(), title,
				message);

		if (confirmed) {
			// Fix the scheme in the local changes.
			final String defaultSchemeId = bindingService.getDefaultSchemeId();
			final Scheme defaultScheme = localChangeManager
					.getScheme(defaultSchemeId);
			try {
				localChangeManager.setActiveScheme(defaultScheme);
			} catch (final NotDefinedException e) {
				// At least we tried....
			}

			// Fix the bindings in the local changes.
			final Binding[] currentBindings = localChangeManager.getBindings();
			final int currentBindingsLength = currentBindings.length;
			final Set trimmedBindings = new HashSet();
			for (int i = 0; i < currentBindingsLength; i++) {
				final Binding binding = currentBindings[i];
				if (binding.getType() != Binding.USER) {
					trimmedBindings.add(binding);
				}
			}
			final Binding[] trimmedBindingArray = (Binding[]) trimmedBindings
					.toArray(new Binding[trimmedBindings.size()]);
			localChangeManager.setBindings(trimmedBindingArray);

			// Apply the changes.
			try {
				bindingService.savePreferences(defaultScheme,
						trimmedBindingArray);
			} catch (final IOException e) {
				logPreferenceStoreException(e);
			}
		}

		setScheme(localChangeManager.getActiveScheme()); // update the scheme
		update(true);
		super.performDefaults();
	}

	public final boolean performOk() {
		// Save the preferences.
		try {
			bindingService.savePreferences(
					localChangeManager.getActiveScheme(), localChangeManager
							.getBindings());
		} catch (final IOException e) {
			logPreferenceStoreException(e);
		}

		// Save the selected tab for future reference.
		persistSelectedTab();

		return super.performOk();
	}

	/**
	 * Remembers the currently selected tab for when the preference page next
	 * opens.
	 */
	private final void persistSelectedTab() {
		final IPreferenceStore store = getPreferenceStore();
		store.setValue(IPreferenceConstants.KEYS_PREFERENCE_SELECTED_TAB,
				tabFolder.getSelectionIndex());
	}

	/**
	 * Handles the selection event on the add button. This removes all
	 * user-defined bindings matching the given key sequence, scheme and
	 * context. It then adds a new binding with the current selections.
	 */
	private final void selectedButtonAdd() {
		final ParameterizedCommand command = getParameterizedCommand();
		final String contextId = getContextId();
		final String schemeId = getSchemeId();
		final KeySequence keySequence = getKeySequence();
		localChangeManager.removeBindings(keySequence, schemeId, contextId,
				null, null, null, Binding.USER);
		localChangeManager.addBinding(new KeyBinding(keySequence, command,
				schemeId, contextId, null, null, null, Binding.USER));
		update(true);
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
		final FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
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
							String buf = Util.replaceAll(item.getText(j), "\"", //$NON-NLS-1$
									"\"\""); //$NON-NLS-1$
							fileWriter.write("\"" + buf + "\"");  //$NON-NLS-1$//$NON-NLS-2$
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
		SafeRunner.run(runnable);
	}
	
	/**
	 * Handles the selection event on the remove button. This removes all
	 * user-defined bindings matching the given key sequence, scheme and
	 * context. It then adds a new deletion binding for the selected trigger
	 * sequence.
	 */
	private final void selectedButtonRemove() {
		final String contextId = getContextId();
		final String schemeId = getSchemeId();
		final KeySequence keySequence = getKeySequence();
		localChangeManager.removeBindings(keySequence, schemeId, contextId,
				null, null, null, Binding.USER);
		localChangeManager.addBinding(new KeyBinding(keySequence, null,
				schemeId, contextId, null, null, null, Binding.USER));
		update(true);
	}

	/**
	 * Handles the selection event on the restore button. This removes all
	 * user-defined bindings matching the given key sequence, scheme and
	 * context.
	 */
	private final void selectedButtonRestore() {
		String contextId = getContextId();
		String schemeId = getSchemeId();
		KeySequence keySequence = getKeySequence();
		localChangeManager.removeBindings(keySequence, schemeId, contextId,
				null, null, null, Binding.USER);
		update(true);
	}

	/**
	 * Updates the local managers active scheme, and then updates the interface.
	 */
	private final void selectedComboScheme() {
		final String activeSchemeId = getSchemeId();
		final Scheme activeScheme = localChangeManager
				.getScheme(activeSchemeId);
		try {
			localChangeManager.setActiveScheme(activeScheme);
		} catch (final NotDefinedException e) {
			// Oh, well.
		}
		update(true);
	}

	/**
	 * Handles the selection event on the table containing the bindings for a
	 * particular command. This updates the context and trigger sequence based
	 * on the selected binding.
	 */
	private final void selectedTableBindingsForCommand() {
		final int selection = tableBindingsForCommand.getSelectionIndex();
		if ((selection >= 0)
				&& (selection < tableBindingsForCommand.getItemCount())) {
			final TableItem item = tableBindingsForCommand.getItem(selection);
			final KeyBinding binding = (KeyBinding) item.getData(ITEM_DATA_KEY);
			setContextId(binding.getContextId());
			setKeySequence(binding.getKeySequence());
		}

		update();
	}

	/**
	 * Handles the selection event on the table containing the bindings for a
	 * particular trigger sequence. This updates the context based on the
	 * selected binding.
	 */
	private final void selectedTableBindingsForTriggerSequence() {
		final int selection = tableBindingsForTriggerSequence
				.getSelectionIndex();
		if ((selection >= 0)
				&& (selection < tableBindingsForTriggerSequence.getItemCount())) {
			final TableItem item = tableBindingsForTriggerSequence
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
			final Binding binding = (Binding) item.getData(BINDING_KEY);
			editBinding(binding);

		} else {
			editBinding(null);
		}
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
	private final void setContextId(final String contextId) {
		// Clear the current selection.
		comboContext.clearSelection();
		comboContext.deselectAll();

		// Figure out which name to look for.
		String contextName = (String) contextUniqueNamesById.get(contextId);
		if (contextName == null) {
			contextName = (String) contextUniqueNamesById
					.get(IContextIds.CONTEXT_ID_WINDOW);
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

	/**
	 * Sets the current trigger sequence.
	 * 
	 * @param keySequence
	 *            The trigger sequence; may be <code>null</code>.
	 */
	private final void setKeySequence(final KeySequence keySequence) {
		textTriggerSequenceManager.setKeySequence(keySequence);
	}

	/**
	 * Changes the selection in the command combo box.
	 * 
	 * @param command
	 *            The fully-parameterized command to select; may be
	 *            <code>null</code>.
	 */
	private final void setParameterizedCommand(
			final ParameterizedCommand command) {
		int i = 0;
		if (commands != null) {
			final int commandCount = commands.length;
			for (; i < commandCount; i++) {
				if (commands[i].equals(command)) {
					if ((comboCommand.getSelectionIndex() != i)
							&& (i < comboCommand.getItemCount())) {
						comboCommand.select(i);
					}
					break;
				}
			}
			if ((i >= comboCommand.getItemCount())
					&& (comboCommand.getSelectionIndex() != 0)) {
				comboCommand.select(0);
			}
		}
	}

	/**
	 * Sets the currently selected scheme
	 * 
	 * @param scheme
	 *            The scheme to select; may be <code>null</code>.
	 */
	private final void setScheme(final Scheme scheme) {
		comboScheme.clearSelection();
		comboScheme.deselectAll();
		final String schemeUniqueName = (String) schemeUniqueNamesById
				.get(scheme.getId());

		if (schemeUniqueName != null) {
			final String items[] = comboScheme.getItems();

			for (int i = 0; i < items.length; i++) {
				if (schemeUniqueName.equals(items[i])) {
					comboScheme.select(i);
					break;
				}
			}
		}
	}

	/**
	 * Builds the internal look-up tables before allowing the page to become
	 * visible.
	 */
	public final void setVisible(final boolean visible) {
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
			
			Map commandsByName = new HashMap();

			for (Iterator iterator = commandService.getDefinedCommandIds()
					.iterator(); iterator.hasNext();) {
				Command command = commandService.getCommand((String) iterator
						.next());
				if (!isActive(command)) {
					continue;
				}

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
			
			// moved here to allow us to remove any empty categories
			commandIdsByCategoryId = new HashMap();

			for (Iterator iterator = commandService.getDefinedCommandIds()
					.iterator(); iterator.hasNext();) {
				final Command command = commandService
						.getCommand((String) iterator.next());
				if (!isActive(command)) {
					continue;
				}

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

			Map categoriesByName = new HashMap();

			for (Iterator iterator = commandService.getDefinedCategoryIds()
					.iterator(); iterator.hasNext();) {
				Category category = commandService
						.getCategory((String) iterator.next());

				try {
					if (commandIdsByCategoryId.containsKey(category.getId())) {
						String name = category.getName();
						Collection categories = (Collection) categoriesByName
								.get(name);

						if (categories == null) {
							categories = new HashSet();
							categoriesByName.put(name, categories);
						}

						categories.add(category);
					}
				} catch (NotDefinedException eNotDefined) {
					// Do nothing
				}
			}

			Map schemesByName = new HashMap();

			final Scheme[] definedSchemes = bindingService.getDefinedSchemes();
			for (int i = 0; i < definedSchemes.length; i++) {
				final Scheme scheme = definedSchemes[i];
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
				} else {
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
				} else {
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
				} else {
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
			}

			Scheme activeScheme = bindingService.getActiveScheme();

			// Make an internal copy of the binding manager, for local changes.
			try {
				for (int i = 0; i < definedSchemes.length; i++) {
					final Scheme scheme = definedSchemes[i];
					final Scheme copy = localChangeManager.getScheme(scheme
							.getId());
					copy.define(scheme.getName(), scheme.getDescription(),
							scheme.getParentId());
				}
				localChangeManager.setActiveScheme(bindingService
						.getActiveScheme());
			} catch (final NotDefinedException e) {
				throw new Error(
						"There is a programmer error in the keys preference page"); //$NON-NLS-1$
			}
			localChangeManager.setLocale(bindingService.getLocale());
			localChangeManager.setPlatform(bindingService.getPlatform());
			localChangeManager.setBindings(bindingService.getBindings());

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
			update(true);
		}

		super.setVisible(visible);
	}

	/**
	 * Updates the entire preference page -- except the view tab -- based on
	 * current selection sate. This preference page is written so that
	 * everything can be made consistent simply by inspecting the state of its
	 * widgets. A change is triggered by the user, and an event is fired. The
	 * event triggers an update. It is possible for extra work to be done by
	 * this page before calling update.
	 */
	private final void update() {
		update(false);
	}

	/**
	 * Updates the entire preference page based on current changes. This
	 * preference page is written so that everything can be made consistent
	 * simply by inspecting the state of its widgets. A change is triggered by
	 * the user, and an event is fired. The event triggers an update. It is
	 * possible for extra work to be done by this page before calling update.
	 * 
	 * @param updateViewTab
	 *            Whether the view tab should be updated as well.
	 */
	private final void update(final boolean updateViewTab) {
		if (updateViewTab) {
			updateViewTab();
		}
		updateComboCommand();
		updateComboContext();
		final TriggerSequence triggerSequence = getKeySequence();
		updateTableBindingsForTriggerSequence(triggerSequence);
		final ParameterizedCommand command = getParameterizedCommand();
		updateTableBindingsForCommand(command);
		final String contextId = getContextId();
		updateSelection(tableBindingsForTriggerSequence, contextId,
				triggerSequence);
		updateSelection(tableBindingsForCommand, contextId, triggerSequence);
		updateLabelSchemeExtends();
		updateLabelContextExtends();
		updateEnabled(triggerSequence, command);
	}

	/**
	 * Updates the contents of the commands combo box, based on the current
	 * selection in the category combo box.
	 */
	private final void updateComboCommand() {
		// Remember the current selection, so we can restore it later.
		final ParameterizedCommand command = getParameterizedCommand();

		// Figure out where command identifiers apply to the selected category.
		final String categoryId = getCategoryId();
		Set commandIds = (Set) commandIdsByCategoryId.get(categoryId);
		if (commandIds==null) {
			commandIds = Collections.EMPTY_SET;
		}

		/*
		 * Generate an array of parameterized commands based on these
		 * identifiers. The parameterized commands will be sorted based on their
		 * names.
		 */
		List commands = new ArrayList();
		final Iterator commandIdItr = commandIds.iterator();
		while (commandIdItr.hasNext()) {
			final String currentCommandId = (String) commandIdItr.next();
			final Command currentCommand = commandService
					.getCommand(currentCommandId);
			try {
				commands.addAll(ParameterizedCommand
						.generateCombinations(currentCommand));
			} catch (final NotDefinedException e) {
				// It is safe to just ignore undefined commands.
			}
		}
		
		// sort the commands with a collator, so they appear in the
		// combo correctly
		commands = sortParameterizedCommands(commands);
		
		final int commandCount = commands.size();
		this.commands = (ParameterizedCommand[]) commands
				.toArray(new ParameterizedCommand[commandCount]);

		/*
		 * Generate an array of command names based on this array of
		 * parameterized commands.
		 */
		final String[] commandNames = new String[commandCount];
		for (int i = 0; i < commandCount; i++) {
			try {
				commandNames[i] = this.commands[i].getName();
			} catch (final NotDefinedException e) {
				throw new Error(
						"Concurrent modification of the command's defined state"); //$NON-NLS-1$
			}
		}

		/*
		 * Copy the command names into the combo box, but only if they've
		 * changed. We do this to try to avoid unnecessary calls out to the
		 * operating system, as well as to defend against bugs in SWT's event
		 * mechanism.
		 */
		final String[] currentItems = comboCommand.getItems();
		if (!Arrays.equals(currentItems, commandNames)) {
			comboCommand.setItems(commandNames);
		}

		// Try to restore the selection.
		setParameterizedCommand(command);

		/*
		 * Just to be extra careful, make sure that we have a selection at this
		 * point. This line could probably be removed, but it makes the code a
		 * bit more robust.
		 */
		if ((comboCommand.getSelectionIndex() == -1) && (commandCount > 0)) {
			comboCommand.select(0);
		}
	}
	
	/**
	 * Sort the commands using the correct language.
	 * @param commands the List of ParameterizedCommands
	 * @return The sorted List
	 */
	private List sortParameterizedCommands(List commands) {
		final Collator collator = Collator.getInstance();
		
		// this comparator is based on the ParameterizedCommands#compareTo(*)
		// method, but uses the collator.
		Comparator comparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				String name1 = null;
				String name2 = null;
				try {
					name1 = ((ParameterizedCommand) o1).getName();
				} catch (NotDefinedException e) {
					return -1;
				}
				try {
					name2 = ((ParameterizedCommand) o2).getName();
				} catch (NotDefinedException e) {
					return 1;
				}
				int rc = collator.compare(name1, name2);
				if (rc != 0) {
					return rc;
				}

				String id1 = ((ParameterizedCommand) o1).getId();
				String id2 = ((ParameterizedCommand) o2).getId();
				return collator.compare(id1, id2);
			}
		};
		Collections.sort(commands, comparator);
		return commands;
	}

	/**
	 * Updates the contents of the context combo box, as well as its selection.
	 */
	private final void updateComboContext() {
		final String contextId = getContextId();
		final Map contextIdsByName = new HashMap(contextIdsByUniqueName);

		final List contextNames = new ArrayList(contextIdsByName.keySet());
		Collections.sort(contextNames, Collator.getInstance());

		comboContext.setItems((String[]) contextNames
				.toArray(new String[contextNames.size()]));
		setContextId(contextId);

		if (comboContext.getSelectionIndex() == -1 && !contextNames.isEmpty()) {
			comboContext.select(0);
		}
	}

	/**
	 * Updates the enabled state of the various widgets on this page. The
	 * decision is based on the current trigger sequence and the currently
	 * selected command.
	 * 
	 * @param triggerSequence
	 *            The current trigger sequence; may be empty, but never
	 *            <code>null</code>.
	 * @param command
	 *            The currently selected command, if any; <code>null</code>
	 *            otherwise.
	 */
	private final void updateEnabled(final TriggerSequence triggerSequence,
			final ParameterizedCommand command) {
		final boolean commandSelected = command != null;
		labelBindingsForCommand.setEnabled(commandSelected);
		tableBindingsForCommand.setEnabled(commandSelected);

		final boolean triggerSequenceSelected = !triggerSequence.isEmpty();
		labelBindingsForTriggerSequence.setEnabled(triggerSequenceSelected);
		tableBindingsForTriggerSequence.setEnabled(triggerSequenceSelected);

		/*
		 * TODO Do some better button enablement.
		 */
		final boolean buttonsEnabled = commandSelected
				&& triggerSequenceSelected;
		buttonAdd.setEnabled(buttonsEnabled);
		buttonRemove.setEnabled(buttonsEnabled);
		buttonRestore.setEnabled(buttonsEnabled);
	}

	/**
	 * Updates the label next to the context that says "extends" if the context
	 * is a child of another context. If the context is not a child of another
	 * context, then the label is simply blank.
	 */
	private final void updateLabelContextExtends() {
		final String contextId = getContextId();

		if (contextId != null) {
			final Context context = contextService.getContext(getContextId());
			if (context.isDefined()) {
				try {
					final String parentId = context.getParentId();
					if (parentId != null) {
						final String name = (String) contextUniqueNamesById
								.get(parentId);
						if (name != null) {
							labelContextExtends.setText(MessageFormat.format(
									Util.translateString(RESOURCE_BUNDLE,
											"extends"), //$NON-NLS-1$
									new Object[] { name }));
							return;
						}
					}
				} catch (final NotDefinedException e) {
					// Do nothing
				}
			}
		}

		labelContextExtends.setText(Util.ZERO_LENGTH_STRING);
	}

	/**
	 * Updates the label next to the scheme that says "extends" if the scheme is
	 * a child of another scheme. If the scheme is not a child of another
	 * scheme, then the label is simply blank.
	 */
	private final void updateLabelSchemeExtends() {
		final String schemeId = getSchemeId();

		if (schemeId != null) {
			final Scheme scheme = bindingService.getScheme(schemeId);
			try {
				final String name = (String) schemeUniqueNamesById.get(scheme
						.getParentId());
				if (name != null) {
					labelSchemeExtends.setText(MessageFormat.format(Util
							.translateString(RESOURCE_BUNDLE, "extends"), //$NON-NLS-1$
							new Object[] { name }));
					return;
				}
			} catch (final NotDefinedException e) {
				// Do nothing
			}
		}

		labelSchemeExtends.setText(Util.ZERO_LENGTH_STRING);
	}

	/**
	 * Tries to select the correct entry in table based on the currently
	 * selected context and trigger sequence. If the table hasn't really
	 * changed, then this method is essentially trying to restore the selection.
	 * If it has changed, then it is trying to select the most entry based on
	 * the context.
	 * 
	 * @param table
	 *            The table to be changed; must not be <code>null</code>.
	 * @param contextId
	 *            The currently selected context; should not be
	 *            <code>null</code>.
	 * @param triggerSequence
	 *            The current trigger sequence; should not be <code>null</code>.
	 */
	private final void updateSelection(final Table table,
			final String contextId, final TriggerSequence triggerSequence) {
		if (table.getSelectionCount() > 1) {
			table.deselectAll();
		}

		final TableItem[] items = table.getItems();
		int selection = -1;
		for (int i = 0; i < items.length; i++) {
			final Binding binding = (Binding) items[i].getData(ITEM_DATA_KEY);
			if ((Util.equals(contextId, binding.getContextId()))
					&& (Util.equals(triggerSequence, binding
							.getTriggerSequence()))) {
				selection = i;
				break;
			}
		}

		if (selection != -1) {
			table.select(selection);
		}
	}

	/**
	 * Updates the contents of the table showing the bindings for the currently
	 * selected command. The selection is destroyed by this process.
	 * 
	 * @param parameterizedCommand
	 *            The currently selected fully-parameterized command; may be
	 *            <code>null</code>.
	 */
	private final void updateTableBindingsForCommand(
			final ParameterizedCommand parameterizedCommand) {
		// Clear the table of existing items.
		tableBindingsForCommand.removeAll();

		// Add each of the bindings, if the command identifier matches.
		final Collection bindings = localChangeManager
				.getActiveBindingsDisregardingContextFlat();
		final Iterator bindingItr = bindings.iterator();
		while (bindingItr.hasNext()) {
			final Binding binding = (Binding) bindingItr.next();
			if (!Util.equals(parameterizedCommand, binding
					.getParameterizedCommand())) {
				continue; // binding does not match
			}

			final TableItem tableItem = new TableItem(tableBindingsForCommand,
					SWT.NULL);
			tableItem.setData(ITEM_DATA_KEY, binding);

			/*
			 * Set the associated image based on the type of binding. Either it
			 * is a user binding or a system binding.
			 * 
			 * TODO Identify more image types.
			 */
			if (binding.getType() == Binding.SYSTEM) {
				tableItem.setImage(0, IMAGE_BLANK);
			} else {
				tableItem.setImage(0, IMAGE_CHANGE);
			}

			String contextName = (String) contextUniqueNamesById.get(binding
					.getContextId());
			if (contextName == null) {
				contextName = Util.ZERO_LENGTH_STRING;
			}
			tableItem.setText(1, contextName);
			tableItem.setText(2, binding.getTriggerSequence().format());
		}
	}

	/**
	 * Updates the contents of the table showing the bindings for the current
	 * trigger sequence. The selection is destroyed by this process.
	 * 
	 * @param triggerSequence
	 *            The current trigger sequence; may be <code>null</code> or
	 *            empty.
	 */
	private final void updateTableBindingsForTriggerSequence(
			final TriggerSequence triggerSequence) {
		// Clear the table of its existing items.
		tableBindingsForTriggerSequence.removeAll();

		// Get the collection of bindings for the current command.
		final Map activeBindings = localChangeManager
				.getActiveBindingsDisregardingContext();
		final Collection bindings = (Collection) activeBindings
				.get(triggerSequence);
		if (bindings == null) {
			return;
		}

		// Add each of the bindings.
		final Iterator bindingItr = bindings.iterator();
		while (bindingItr.hasNext()) {
			final Binding binding = (Binding) bindingItr.next();
			final Context context = contextService.getContext(binding
					.getContextId());
			final ParameterizedCommand parameterizedCommand = binding
					.getParameterizedCommand();
			final Command command = parameterizedCommand.getCommand();
			if ((!context.isDefined()) && (!command.isDefined())) {
				continue;
			}

			final TableItem tableItem = new TableItem(
					tableBindingsForTriggerSequence, SWT.NULL);
			tableItem.setData(ITEM_DATA_KEY, binding);

			/*
			 * Set the associated image based on the type of binding. Either it
			 * is a user binding or a system binding.
			 * 
			 * TODO Identify more image types.
			 */
			if (binding.getType() == Binding.SYSTEM) {
				tableItem.setImage(0, IMAGE_BLANK);
			} else {
				tableItem.setImage(0, IMAGE_CHANGE);
			}

			try {
				tableItem.setText(1, context.getName());
				tableItem.setText(2, parameterizedCommand.getName());
			} catch (final NotDefinedException e) {
				throw new Error(
						"Context or command became undefined on a non-UI thread while the UI thread was processing."); //$NON-NLS-1$
			}
		}
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
		final List bindings = new ArrayList(localChangeManager
				.getActiveBindingsDisregardingContextFlat());
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
				final Command command1 = binding1.getParameterizedCommand()
						.getCommand();
				String categoryName1 = Util.ZERO_LENGTH_STRING;
				String commandName1 = Util.ZERO_LENGTH_STRING;
				try {
					commandName1 = command1.getName();
					categoryName1 = command1.getCategory().getName();
				} catch (final NotDefinedException e) {
					// Just use the zero-length string.
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
				final Command command2 = binding2.getParameterizedCommand()
						.getCommand();
				String categoryName2 = Util.ZERO_LENGTH_STRING;
				String commandName2 = Util.ZERO_LENGTH_STRING;
				try {
					commandName2 = command2.getName();
					categoryName2 = command2.getCategory().getName();
				} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
					// Just use the zero-length string.
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
			final Binding binding = (Binding) keyBindingItr.next();

			// Get the command and category name.
			final ParameterizedCommand command = binding
					.getParameterizedCommand();
			String commandName = Util.ZERO_LENGTH_STRING;
			String categoryName = Util.ZERO_LENGTH_STRING;
			try {
				commandName = command.getName();
				categoryName = command.getCommand().getCategory().getName();
			} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
				// Just use the zero-length string.
			}

			// Ignore items with a meaningless command name.
			if ((commandName == null) || (commandName.length() == 0)) {
				continue;
			}

			// Get the context name.
			final String contextId = binding.getContextId();
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
			item.setText(VIEW_KEY_SEQUENCE_COLUMN_INDEX, binding
					.getTriggerSequence().format());
			item.setText(VIEW_CONTEXT_COLUMN_INDEX, contextName);
			item.setData(BINDING_KEY, binding);
		}

		// Pack the columns.
		for (int i = 0; i < tableBindings.getColumnCount(); i++) {
			tableBindings.getColumn(i).pack();
		}
	}
	
	
}
