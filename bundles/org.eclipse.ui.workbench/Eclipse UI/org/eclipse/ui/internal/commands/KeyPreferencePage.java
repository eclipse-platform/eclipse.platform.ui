/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class KeyPreferencePage extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {

	private final static ResourceBundle resourceBundle = ResourceBundle.getBundle(KeyPreferencePage.class.getName());

	private final static String COMMAND_CONFLICT = Util.getString(resourceBundle, "commandConflict"); //$NON-NLS-1$
	private final static String COMMAND_NOTHING = Util.getString(resourceBundle, "commandNothing"); //$NON-NLS-1$
	private final static String COMMAND_UNDEFINED = Util.getString(resourceBundle, "commandUndefined"); //$NON-NLS-1$
	private final static int DIFFERENCE_ADD = 0;	
	private final static int DIFFERENCE_CHANGE = 1;	
	private final static int DIFFERENCE_MINUS = 2;	
	private final static int DIFFERENCE_NONE = 3;	
	private final static Image IMAGE_BLANK = ImageFactory.getImage("blank"); //$NON-NLS-1$
	private final static Image IMAGE_CHANGE = ImageFactory.getImage("change"); //$NON-NLS-1$
	private final static Image IMAGE_MINUS = ImageFactory.getImage("minus"); //$NON-NLS-1$
	private final static Image IMAGE_PLUS = ImageFactory.getImage("plus"); //$NON-NLS-1$
	private final static RGB RGB_CONFLICT = new RGB(255, 0, 0);
	private final static RGB RGB_CONFLICT_MINUS = new RGB(255, 192, 192);
	private final static RGB RGB_MINUS =	new RGB(192, 192, 192);
	private final static char SPACE = ' ';

	private final class CommandSetPair {
		
		Set customSet;
		Set defaultSet;		
	}

	private final class CommandRecord {

		String command;
		Sequence sequence;
		String scope;
		String configuration;
		Set customSet;
		Set defaultSet;

		boolean customConflict = false;
		String customCommand = null;
		boolean defaultConflict = false;
		String defaultCommand = null;	

		void calculate() {
			if (customSet.size() > 1)
				customConflict = true;
			else if (!customSet.isEmpty())				
				customCommand = (String) customSet.iterator().next();
	
			if (defaultSet.size() > 1)
				defaultConflict = true;
			else if (!defaultSet.isEmpty())				
				defaultCommand = (String) defaultSet.iterator().next();
		}
	}

	private final class SequenceRecord {

		String scope;
		String configuration;
		Set customSet;
		Set defaultSet;

		boolean customConflict = false;
		String customCommand = null;
		boolean defaultConflict = false;
		String defaultCommand = null;	

		void calculate() {
			if (customSet.size() > 1)
				customConflict = true;
			else if (!customSet.isEmpty())				
				customCommand = (String) customSet.iterator().next();
	
			if (defaultSet.size() > 1)
				defaultConflict = true;
			else if (!defaultSet.isEmpty())				
				defaultCommand = (String) defaultSet.iterator().next();
		}
	}	

	private class TreeViewerCommandsContentProvider implements ITreeContentProvider {
		
		public void dispose() {
		}
		
		public Object[] getChildren(Object parentElement) {
			List children = new ArrayList();

			if (parentElement instanceof Category) {
				if (commands != null) {					
					Category category = (Category) parentElement;

					for (int i = 0; i < commands.size(); i++) {
						Command command = (Command) commands.get(i);
							
						if (category.getId().equals(command.getCategory()))
							children.add(command);											
					}
				}
			} else if (parentElement == null) {
				if (categories != null && commands != null) {
					List categories = new ArrayList(KeyPreferencePage.this.categories);
					Collections.sort(categories, Category.nameComparator());
					children.addAll(categories);
					List commands = new ArrayList();
	
					for (int i = 0; i < KeyPreferencePage.this.commands.size(); i++) {
						Command command = (Command) KeyPreferencePage.this.commands.get(i);
							
						if (command.getCategory() == null)
							commands.add(command);										
					}
	
					Collections.sort(commands, Command.nameComparator());
					children.addAll(commands);
				}									
			}
				
			return children.toArray();
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(null);
		}

		public Object getParent(Object element) {
			if (element instanceof Command && categoriesById != null) {
				String category = ((Command) element).getCategory();
				
				if (category != null)
					return categoriesById.get(category);
			}

			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length >= 1;
		}			

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
		
	private class TreeViewerCommandsLabelProvider extends LabelProvider {			

		public String getText(Object element) {
			if (element instanceof Category)
				return ((Category) element).getName();
			else if (element instanceof Command)
				return ((Command) element).getName();
			else 				
				return super.getText(element);
		}						
	};

	private Label labelActiveConfiguration; 
	private Combo comboActiveConfiguration;
	//private Button buttonNew; 
	//private Button buttonRename;
	//private Button buttonDelete;
	private Label labelCommands;
	private TreeViewer treeViewerCommands;
	//private Button buttonCategorize;
	private Label labelSequencesForCommand;
	private Table tableSequencesForCommand;
	//private TableViewer ;	
	private Label labelSequence;
	private Combo comboSequence;
	private Label labelScope; 
	private Combo comboScope;
	private Label labelConfiguration; 
	private Combo comboConfiguration;
	private Button buttonChange;
	private Label labelCommandsForSequence;
	private Table tableCommandsForSequence;
	//private TableViewer tableViewerCommandsForSequence;

	private IWorkbench workbench;

	private List categories;
	private SortedMap categoriesById;
	private SortedMap categoriesByName;
	private List commands;
	private SortedMap commandsById;
	private SortedMap commandsByName;
	private List scopes;
	private SortedMap scopesById;
	private SortedMap scopesByName;

	private List coreActiveKeyConfigurations;
	private List coreKeyBindings;
	private List coreKeyConfigurations;

	private List localActiveKeyConfigurations;
	private List localKeyBindings;
	private List localKeyConfigurations;

	private List preferenceActiveKeyConfigurations;
	private List preferenceKeyBindings;
	private List preferenceKeyConfigurations;

	private ActiveConfiguration activeKeyConfiguration;	
	private List activeKeyConfigurations;
	private List keyConfigurations;
	private SortedMap keyConfigurationsById;
	private SortedMap keyConfigurationsByName;	
	private SortedMap tree;
	private List commandRecords = new ArrayList();	
	private List keySequenceRecords = new ArrayList();
	private SortedMap keySequencesByName;

	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();

		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
		}
	
		preferenceActiveKeyConfigurations = new ArrayList(preferenceRegistry.getActiveKeyConfigurations());
		preferenceKeyBindings = new ArrayList(preferenceRegistry.getKeyBindings());
		preferenceKeyConfigurations = new ArrayList(preferenceRegistry.getKeyConfigurations());
	}

	public boolean performOk() {
		copyFromUI();
		PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();
		preferenceRegistry.setActiveKeyConfigurations(preferenceActiveKeyConfigurations);
		preferenceRegistry.setKeyBindings(preferenceKeyBindings);
		preferenceRegistry.setKeyConfigurations(preferenceKeyConfigurations);
		
		try {
			preferenceRegistry.save();
		} catch (IOException eIO) {
		}

		Manager.getInstance().reset();

		if (workbench instanceof Workbench)
			((Workbench) this.workbench).updateKeys();

		return super.performOk();
	}

	public void setVisible(boolean visible) {
		if (visible == true) {
			CoreRegistry coreRegistry = CoreRegistry.getInstance();
			LocalRegistry localRegistry = LocalRegistry.getInstance();
			PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();
			
			try {
				coreRegistry.load();
			} catch (IOException eIO) {
			}
	
			try {
				localRegistry.load();
			} catch (IOException eIO) {
			}
	
			try {
				preferenceRegistry.load();
			} catch (IOException eIO) {
			}		
	
			boolean categoriesChanged = false;
			List categories = new ArrayList();
			categories.addAll(coreRegistry.getCategories());
			categories.addAll(localRegistry.getCategories());
			categories.addAll(preferenceRegistry.getCategories());
	
			if (!Util.equals(categories, this.categories)) {
				this.categories = Collections.unmodifiableList(categories);
				categoriesById = Collections.unmodifiableSortedMap(Category.sortedMapById(this.categories));
				categoriesByName = Collections.unmodifiableSortedMap(Category.sortedMapByName(this.categories));
				categoriesChanged = true;
			}
	
			boolean commandsChanged = false;
			List commands = new ArrayList();
			commands.addAll(coreRegistry.getCommands());
			commands.addAll(localRegistry.getCommands());
			commands.addAll(preferenceRegistry.getCommands());
			
			if (!Util.equals(commands, this.commands)) {
				this.commands = Collections.unmodifiableList(commands);
				commandsById = Collections.unmodifiableSortedMap(Command.sortedMapById(this.commands));
				commandsByName = Collections.unmodifiableSortedMap(Command.sortedMapByName(this.commands));
				commandsChanged = true;
			}

			if (categoriesChanged|| commandsChanged)
				treeViewerCommands.setInput(new Object());
	
			List scopes = new ArrayList();
			scopes.addAll(coreRegistry.getScopes());
			scopes.addAll(localRegistry.getScopes());
			scopes.addAll(preferenceRegistry.getScopes());
	
			if (!Util.equals(scopes, this.scopes)) {
				this.scopes = Collections.unmodifiableList(scopes);
				scopesById = Collections.unmodifiableSortedMap(Scope.sortedMapById(this.scopes));
				scopesByName = Collections.unmodifiableSortedMap(Scope.sortedMapByName(this.scopes));
				Set scopeNameSet = scopesByName.keySet();
				comboScope.setItems((String[]) scopeNameSet.toArray(new String[scopeNameSet.size()]));
			}		

			coreActiveKeyConfigurations = new ArrayList(coreRegistry.getActiveKeyConfigurations());
			coreKeyBindings = new ArrayList(coreRegistry.getKeyBindings());
			coreKeyConfigurations = new ArrayList(coreRegistry.getKeyConfigurations());

			localActiveKeyConfigurations = new ArrayList(localRegistry.getActiveKeyConfigurations());
			localKeyBindings = new ArrayList(localRegistry.getKeyBindings());
			localKeyConfigurations = new ArrayList(localRegistry.getKeyConfigurations());

			copyToUI();
			update();
		} else
			copyFromUI();

		super.setVisible(visible);
	}

	protected Control createContents(Composite parent) {
		return createUI(parent);
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	protected void performDefaults() {
		// TODO only show message box if there are changes
		MessageBox restoreDefaultsMessageBox = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
		restoreDefaultsMessageBox.setText(Util.getString(resourceBundle, "restoreDefaultsMessageBoxText")); //$NON-NLS-1$
		restoreDefaultsMessageBox.setMessage(Util.getString(resourceBundle, "restoreDefaultsMessageBoxMessage")); //$NON-NLS-1$
		
		if (restoreDefaultsMessageBox.open() == SWT.YES) {
			preferenceActiveKeyConfigurations = new ArrayList();
			preferenceKeyBindings = new ArrayList();
			preferenceKeyConfigurations = new ArrayList();
			copyToUI();
		}
	}

	private void copyFromUI() {
		activeKeyConfiguration = null; 		
		preferenceActiveKeyConfigurations = new ArrayList();		
		int index = comboActiveConfiguration.getSelectionIndex();
				
		if (index >= 0) {
			Configuration keyConfiguration = (Configuration) keyConfigurationsByName.get(comboActiveConfiguration.getItem(index));
			activeKeyConfiguration = ActiveConfiguration.create(null, keyConfiguration.getId());
			preferenceActiveKeyConfigurations.add(activeKeyConfiguration);
		}

		preferenceKeyBindings = new ArrayList(solve(tree));
	}

	private void copyToUI() {	
		// TODO validate lists for referential integrity
		List activeKeyConfigurations = new ArrayList();
		activeKeyConfigurations.addAll(coreActiveKeyConfigurations);
		activeKeyConfigurations.addAll(localActiveKeyConfigurations);
		activeKeyConfigurations.addAll(preferenceActiveKeyConfigurations);

		if (!Util.equals(activeKeyConfigurations, this.activeKeyConfigurations)) {
			this.activeKeyConfigurations = Collections.unmodifiableList(activeKeyConfigurations);
			activeKeyConfiguration = (ActiveConfiguration) this.activeKeyConfigurations.get(this.activeKeyConfigurations.size() - 1);				
		}
		
		List keyConfigurations = new ArrayList();
		keyConfigurations.addAll(coreKeyConfigurations);
		keyConfigurations.addAll(localKeyConfigurations);
		keyConfigurations.addAll(preferenceKeyConfigurations);

		if (!Util.equals(keyConfigurations, this.keyConfigurations)) {
			this.keyConfigurations = Collections.unmodifiableList(keyConfigurations);
			keyConfigurationsById = Collections.unmodifiableSortedMap(Configuration.sortedMapById(this.keyConfigurations));
			keyConfigurationsByName = Collections.unmodifiableSortedMap(Configuration.sortedMapByName(this.keyConfigurations));
			Set keyConfigurationNameSet = keyConfigurationsByName.keySet();
			comboActiveConfiguration.setItems((String[]) keyConfigurationNameSet.toArray(new String[keyConfigurationNameSet.size()]));
			comboConfiguration.setItems((String[]) keyConfigurationNameSet.toArray(new String[keyConfigurationNameSet.size()]));
		}		

		int index = -1;
			
		if (activeKeyConfiguration != null) {
			Configuration keyConfiguration = (Configuration) keyConfigurationsById.get(activeKeyConfiguration.getValue());

			if (keyConfiguration != null)
				index = comboActiveConfiguration.indexOf(keyConfiguration.getName());
		}
			
		if (index >= 0)
			comboActiveConfiguration.select(index);
		else {
			comboActiveConfiguration.clearSelection();
			comboActiveConfiguration.deselectAll();
		}

		SortedSet keyBindingSet = new TreeSet();
		keyBindingSet.addAll(coreKeyBindings);
		keyBindingSet.addAll(localKeyBindings);
		keyBindingSet.addAll(preferenceKeyBindings);

		tree = build(keyBindingSet);	

		keySequencesByName = new TreeMap();
		Iterator iterator = tree.keySet().iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (object instanceof Sequence) {
				Sequence keySequence = (Sequence) object;
				String name = KeySupport.formatSequence(keySequence);
				keySequencesByName.put(name, keySequence);
			}
		}		

		Set keySequenceNameSet = keySequencesByName.keySet();
		comboSequence.setItems((String[]) keySequenceNameSet.toArray(new String[keySequenceNameSet.size()]));
		selectedTreeViewerCommands();
	}

	private Control createUI(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		Composite compositeActiveKeyConfiguration = new Composite(composite, SWT.NULL);
		compositeActiveKeyConfiguration.setFont(composite.getFont());
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 5;
		compositeActiveKeyConfiguration.setLayout(gridLayout);

		labelActiveConfiguration = new Label(compositeActiveKeyConfiguration, SWT.LEFT);
		labelActiveConfiguration.setFont(compositeActiveKeyConfiguration.getFont());
		labelActiveConfiguration.setText(Util.getString(resourceBundle, "labelActiveConfiguration")); //$NON-NLS-1$

		comboActiveConfiguration = new Combo(compositeActiveKeyConfiguration, SWT.READ_ONLY);
		comboActiveConfiguration.setFont(compositeActiveKeyConfiguration.getFont());
		GridData gridData = new GridData();
		gridData.widthHint = 150;
		comboActiveConfiguration.setLayoutData(gridData);

		//buttonNew = new Button(compositeActiveKeyConfiguration, SWT.CENTER | SWT.PUSH);
		//buttonNew.setFont(compositeActiveKeyConfiguration.getFont());
		//gridData = new GridData();
		//gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		//int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		//buttonNew.setText(Util.getString(resourceBundle, "buttonNew")); //$NON-NLS-1$
		//gridData.widthHint = Math.max(widthHint, buttonNew.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		//buttonNew.setLayoutData(gridData);
		//buttonNew.setVisible(false);	

		//buttonRename = new Button(compositeActiveKeyConfiguration, SWT.CENTER | SWT.PUSH);
		//buttonRename.setFont(compositeActiveKeyConfiguration.getFont());
		//gridData = new GridData();
		//gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		//widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		//buttonRename.setText(Util.getString(resourceBundle, "buttonRename")); //$NON-NLS-1$
		//gridData.widthHint = Math.max(widthHint, buttonRename.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		//buttonRename.setLayoutData(gridData);		
		//buttonRename.setVisible(false);	
		
		//buttonDelete = new Button(compositeActiveKeyConfiguration, SWT.CENTER | SWT.PUSH);
		//buttonDelete.setFont(compositeActiveKeyConfiguration.getFont());
		//gridData = new GridData();
		//gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		//widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		//buttonDelete.setText(Util.getString(resourceBundle, "buttonDelete")); //$NON-NLS-1$
		//gridData.widthHint = Math.max(widthHint, buttonDelete.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		//buttonDelete.setLayoutData(gridData);	
		//buttonDelete.setVisible(false);	

		Label labelSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		labelSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite compositeAssignment = new Composite(composite, SWT.NULL);
		compositeAssignment.setFont(composite.getFont());
		gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 7;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 2;
		compositeAssignment.setLayout(gridLayout);
		compositeAssignment.setLayoutData(new GridData(GridData.FILL_BOTH));

		labelCommands = new Label(compositeAssignment, SWT.LEFT);
		labelCommands.setFont(compositeAssignment.getFont());
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		labelCommands.setLayoutData(gridData);
		labelCommands.setText(Util.getString(resourceBundle, "labelCommands")); //$NON-NLS-1$

		Composite compositeAssignmentLeft = new Composite(compositeAssignment, SWT.NULL);
		compositeAssignmentLeft.setFont(compositeAssignment.getFont());
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		compositeAssignmentLeft.setLayout(gridLayout);
		compositeAssignmentLeft.setLayoutData(new GridData(GridData.FILL_VERTICAL));
 
		treeViewerCommands = new TreeViewer(compositeAssignmentLeft);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 0;
		gridData.widthHint = 200;
		treeViewerCommands.getControl().setLayoutData(gridData);
		treeViewerCommands.setContentProvider(new TreeViewerCommandsContentProvider());	
		treeViewerCommands.setLabelProvider(new TreeViewerCommandsLabelProvider());		

		//buttonCategorize = new Button(compositeAssignmentLeft, SWT.CHECK | SWT.LEFT);
		//buttonCategorize.setFont(compositeAssignmentLeft.getFont());
		//buttonCategorize.setSelection(true);
		//buttonCategorize.setText(Util.getString(resourceBundle, "buttonCategorize")); //$NON-NLS-1$
		//buttonCategorize.setVisible(false);	

		Composite compositeAssignmentRight = new Composite(compositeAssignment, SWT.NULL);
		compositeAssignmentRight.setFont(compositeAssignment.getFont());
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;		
		gridLayout.marginWidth = 0;
		compositeAssignmentRight.setLayout(gridLayout);
		compositeAssignmentRight.setLayoutData(new GridData(GridData.FILL_BOTH));

		labelSequencesForCommand = new Label(compositeAssignmentRight, SWT.LEFT);
		labelSequencesForCommand.setFont(compositeAssignmentRight.getFont());
		labelSequencesForCommand.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelSequencesForCommand.setText(Util.getString(resourceBundle, "labelSequencesForCommand.noSelection")); //$NON-NLS-1$

		tableSequencesForCommand = new Table(compositeAssignmentRight, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableSequencesForCommand.setFont(compositeAssignmentRight.getFont());
		tableSequencesForCommand.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 90;
		gridData.widthHint = 440;	
		tableSequencesForCommand.setLayoutData(gridData);

		TableColumn tableColumn = new TableColumn(tableSequencesForCommand, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(Util.ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);

		tableColumn = new TableColumn(tableSequencesForCommand, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnScope")); //$NON-NLS-1$
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(tableSequencesForCommand, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnConfiguration")); //$NON-NLS-1$
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(tableSequencesForCommand, SWT.NULL, 3);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnSequence")); //$NON-NLS-1$
		tableColumn.setWidth(220);	
		
		//tableViewerKeySequencesForCommand = new TableViewer(tableKeySequencesForCommand);
		//tableViewerKeySequencesForCommand.setContentProvider(new TableViewerKeySequencesForCommandContentProvider());
		//tableViewerKeySequencesForCommand.setLabelProvider(new TableViewerKeySequencesForCommandLabelProvider());

		Composite compositeAssignmentChange = new Composite(compositeAssignmentRight, SWT.NULL);
		compositeAssignmentChange.setFont(compositeAssignmentRight.getFont());
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 10;
		gridLayout.marginWidth = 10;		
		gridLayout.numColumns = 2;
		compositeAssignmentChange.setLayout(gridLayout);

		labelScope = new Label(compositeAssignmentChange, SWT.LEFT);
		labelScope.setFont(compositeAssignmentChange.getFont());
		labelScope.setText(Util.getString(resourceBundle, "labelScope")); //$NON-NLS-1$

		comboScope = new Combo(compositeAssignmentChange, SWT.READ_ONLY);
		comboScope.setFont(compositeAssignmentChange.getFont());
		gridData = new GridData();
		gridData.widthHint = 150;
		comboScope.setLayoutData(gridData);
		
		labelConfiguration = new Label(compositeAssignmentChange, SWT.LEFT);
		labelConfiguration.setFont(compositeAssignmentChange.getFont());
		labelConfiguration.setText(Util.getString(resourceBundle, "labelConfiguration")); //$NON-NLS-1$

		comboConfiguration = new Combo(compositeAssignmentChange, SWT.READ_ONLY);
		comboConfiguration.setFont(compositeAssignmentChange.getFont());
		gridData = new GridData();
		gridData.widthHint = 150;
		comboConfiguration.setLayoutData(gridData);

		labelSequence = new Label(compositeAssignmentChange, SWT.LEFT);
		labelSequence.setFont(compositeAssignmentChange.getFont());
		labelSequence.setText(Util.getString(resourceBundle, "labelSequence")); //$NON-NLS-1$

		comboSequence = new Combo(compositeAssignmentChange, SWT.NULL);
		comboSequence.setFont(compositeAssignmentChange.getFont());
		gridData = new GridData();
		gridData.widthHint = 220;
		comboSequence.setLayoutData(gridData);

		Control spacer = new Composite(compositeAssignmentChange, SWT.NULL);	
		gridData = new GridData();
		gridData.heightHint = 0;
		gridData.horizontalSpan = 2;
		gridData.widthHint = 0;
		spacer.setLayoutData(gridData);
		
		spacer = new Composite(compositeAssignmentChange, SWT.NULL);	
		gridData = new GridData();
		gridData.heightHint = 0;
		gridData.widthHint = 0;
		spacer.setLayoutData(gridData);
				
		buttonChange = new Button(compositeAssignmentChange, SWT.CENTER | SWT.PUSH);
		buttonChange.setFont(compositeAssignmentChange.getFont());
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonChange.setText(Util.getString(resourceBundle, "buttonChange")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonChange.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonChange.setLayoutData(gridData);		

		spacer = new Composite(compositeAssignmentRight, SWT.NULL);	
		gridData = new GridData();
		gridData.heightHint = 0;
		gridData.widthHint = 0;
		spacer.setLayoutData(gridData);

		spacer = new Composite(compositeAssignmentRight, SWT.NULL);	
		gridData = new GridData();
		gridData.heightHint = 0;
		gridData.widthHint = 0;
		spacer.setLayoutData(gridData);
		
		labelCommandsForSequence = new Label(compositeAssignmentRight, SWT.LEFT);
		labelCommandsForSequence.setFont(compositeAssignmentRight.getFont());
		labelCommandsForSequence.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelCommandsForSequence.setText(Util.getString(resourceBundle, "labelCommandsForSequence.noSelection")); //$NON-NLS-1$

		tableCommandsForSequence = new Table(compositeAssignmentRight, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableCommandsForSequence.setFont(compositeAssignmentRight.getFont());
		tableCommandsForSequence.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		gridData.widthHint = 440;	
		tableCommandsForSequence.setLayoutData(gridData);

		tableColumn = new TableColumn(tableCommandsForSequence, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(Util.ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);
		
		tableColumn = new TableColumn(tableCommandsForSequence, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnScope")); //$NON-NLS-1$
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(tableCommandsForSequence, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnConfiguration")); //$NON-NLS-1$
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(tableCommandsForSequence, SWT.NULL, 3);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnCommand")); //$NON-NLS-1$
		tableColumn.setWidth(220);	
		
		//tableViewerCommandsForKeySequence = new TableViewer(tableCommandsForKeySequence);
		//tableViewerCommandsForKeySequence.setContentProvider(new TableViewerCommandsForKeySequenceContentProvider());
		//tableViewerCommandsForKeySequence.setLabelProvider(new TableViewerCommandsForKeySequenceLabelProvider());

		comboActiveConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboActiveKeyConfiguration();
			}	
		});

		//buttonNew.addSelectionListener(new SelectionAdapter() {
		//	public void widgetSelected(SelectionEvent selectionEvent) {
		//	}	
		//});

		//buttonRename.addSelectionListener(new SelectionAdapter() {
		//	public void widgetSelected(SelectionEvent selectionEvent) {
		//	}	
		//});

		//buttonDelete.addSelectionListener(new SelectionAdapter() {
		//	public void widgetSelected(SelectionEvent selectionEvent) {
		//	}	
		//});

		treeViewerCommands.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickedTreeViewerCommands();
			}
		});

		treeViewerCommands.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selectedTreeViewerCommands();
			}
		});

		//buttonCategorize.addSelectionListener(new SelectionAdapter() {
		//	public void widgetSelected(SelectionEvent selectionEvent) {
		//	}	
		//});

		tableSequencesForCommand.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedTableKeySequencesForCommand();	
			}			
		});		

		tableSequencesForCommand.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {			
				selectedTableKeySequencesForCommand();
			}	
		});
	
		//tableViewerKeySequencesForCommand.addDoubleClickListener(new IDoubleClickListener() {
		//	public void doubleClick(DoubleClickEvent event) {
		//	}
		//});

		//tableViewerKeySequencesForCommand.addSelectionChangedListener(new ISelectionChangedListener() {
		//	public void selectionChanged(SelectionChangedEvent event) {
		//	}
		//});

		comboSequence.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent modifyEvent) {
				modifiedComboKeySequence();
			}	
		});

		comboSequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboKeySequence();
			}	
		});

		comboScope.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboScope();
			}	
		});
		
		comboConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboKeyConfiguration();
			}	
		});

		buttonChange.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonChange();
			}	
		});

		tableCommandsForSequence.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedTableCommandsForKeySequence();	
			}			
		});		

		tableCommandsForSequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {			
				selectedTableCommandsForKeySequence();
			}	
		});

		//tableViewerCommandsForKeySequence.addDoubleClickListener(new IDoubleClickListener() {
		//	public void doubleClick(DoubleClickEvent event) {
		//	}
		//});

		//tableViewerCommandsForKeySequence.addSelectionChangedListener(new ISelectionChangedListener() {
		//	public void selectionChanged(SelectionChangedEvent event) {
		//	}
		//});
				
		// TODO: WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_KEY_PREFERENCE_PAGE);
		return composite;	
	}

	private void selectedComboActiveKeyConfiguration() {		
	}

	private void doubleClickedTreeViewerCommands() {
	}

	private void selectedTreeViewerCommands() {
		commandRecords.clear();
		ISelection selection = treeViewerCommands.getSelection();
		
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object object = ((IStructuredSelection) selection).getFirstElement();
						
			if (object instanceof Command)
				buildCommandRecords(tree, ((Command) object).getId(), commandRecords);
		}

		buildTableCommand();
		Sequence keySequence = getKeySequence();
		String scopeId = getScopeId();
		String keyConfigurationId = getKeyConfigurationId();
		selectTableCommand(scopeId, keyConfigurationId, keySequence);				
		update();
	}

	private void doubleClickedTableKeySequencesForCommand() {
	}
		
	private void selectedTableKeySequencesForCommand() {
		CommandRecord commandRecord = (CommandRecord) getSelectedCommandRecord();
		
		if (commandRecord != null) {
			setScopeId(commandRecord.scope);
			setKeyConfigurationId(commandRecord.configuration);				
			setKeySequence(commandRecord.sequence);
		}
		
		update();
	}

	private void modifiedComboKeySequence() {
		selectedComboKeySequence();
	}

	private void selectedComboKeySequence() {
		Sequence keySequence = getKeySequence();
		String scopeId = getScopeId();
		String keyConfigurationId = getKeyConfigurationId();
		selectTableCommand(scopeId, keyConfigurationId, keySequence);						
		keySequenceRecords.clear();
		buildSequenceRecords(tree, keySequence, keySequenceRecords);
		buildTableKeySequence();	
		selectTableKeySequence(scopeId, keyConfigurationId);		
		update();
	}

	private void selectedComboScope() {
		Sequence keySequence = getKeySequence();
		String scopeId = getScopeId();
		String keyConfigurationId = getKeyConfigurationId();
		selectTableCommand(scopeId, keyConfigurationId, keySequence);
		selectTableKeySequence(scopeId, keyConfigurationId);
		update();
	}

	private void selectedComboKeyConfiguration() {
		Sequence keySequence = getKeySequence();
		String scopeId = getScopeId();
		String keyConfigurationId = getKeyConfigurationId();
		selectTableCommand(scopeId, keyConfigurationId, keySequence);
		selectTableKeySequence(scopeId, keyConfigurationId);
		update();
	}

	private void selectedButtonChange() {
		Sequence keySequence = getKeySequence();
		boolean validKeySequence = keySequence != null && !keySequence.getStrokes().isEmpty();
		String scopeId = getScopeId();
		boolean validScopeId = scopeId != null && scopesById.get(scopeId) != null;	
		String keyConfigurationId = getKeyConfigurationId();
		boolean validKeyConfigurationId = keyConfigurationId != null && keyConfigurationsById.get(keyConfigurationId) != null;
	
		if (validKeySequence && validScopeId && validKeyConfigurationId) {	
			String commandId = null;
			ISelection selection = treeViewerCommands.getSelection();
		
			if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
				Object object = ((IStructuredSelection) selection).getFirstElement();
						
				if (object instanceof Command)
					commandId = ((Command) object).getId();
			}

			CommandRecord commandRecord = getSelectedCommandRecord();
		
			if (commandRecord == null)
				set(tree, keySequence, scopeId, keyConfigurationId, commandId);			 
			else {
				if (!commandRecord.customSet.isEmpty())
					clear(tree, keySequence, scopeId, keyConfigurationId);
				else
					set(tree, keySequence, scopeId, keyConfigurationId, null);
			}

			commandRecords.clear();
			buildCommandRecords(tree, commandId, commandRecords);
			buildTableCommand();
			selectTableCommand(scopeId, keyConfigurationId, keySequence);							
			keySequenceRecords.clear();
			buildSequenceRecords(tree, keySequence, keySequenceRecords);
			buildTableKeySequence();	
			selectTableKeySequence(scopeId, keyConfigurationId);
			update();
		}
	}

	private void doubleClickedTableCommandsForKeySequence() {	
	}

	private void selectedTableCommandsForKeySequence() {
	}

	private void update() {
		Command command = null;
		ISelection selection = treeViewerCommands.getSelection();
		
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object object = ((IStructuredSelection) selection).getFirstElement();
						
			if (object instanceof Command)
				command = (Command) object;
		}

		boolean commandSelected = command != null;

		Sequence keySequence = getKeySequence();
		boolean validKeySequence = keySequence != null && !keySequence.getStrokes().isEmpty();
		String scopeId = getScopeId();
		boolean validScopeId = scopeId != null && scopesById.get(scopeId) != null;	
		String keyConfigurationId = getKeyConfigurationId();
		boolean validKeyConfigurationId = keyConfigurationId != null && keyConfigurationsById.get(keyConfigurationId) != null;

		labelSequencesForCommand.setEnabled(commandSelected);
		tableSequencesForCommand.setEnabled(commandSelected);
		labelSequence.setEnabled(commandSelected);		
		comboSequence.setEnabled(commandSelected);
		labelScope.setEnabled(commandSelected);		
		comboScope.setEnabled(commandSelected);
		labelConfiguration.setEnabled(commandSelected);	
		comboConfiguration.setEnabled(commandSelected);	
		buttonChange.setEnabled(commandSelected && validKeySequence && validScopeId && validKeyConfigurationId);		
		labelCommandsForSequence.setEnabled(validKeySequence);		
		tableCommandsForSequence.setEnabled(validKeySequence);		

		if (commandSelected) {
			String text = MessageFormat.format(Util.getString(resourceBundle, "labelSequencesForCommand.selection"), new Object[] { '\'' + command.getName() + '\'' }); //$NON-NLS-1$
			labelSequencesForCommand.setText(text);		
		} else 
			labelSequencesForCommand.setText(Util.getString(resourceBundle, "labelSequencesForCommand.noSelection")); //$NON-NLS-1$
		
		CommandRecord commandRecord = getSelectedCommandRecord();
		
		if (commandRecord == null)
			buttonChange.setText(Util.getString(resourceBundle, "buttonChange.add")); //$NON-NLS-1$			 
		else {
			if (!commandRecord.customSet.isEmpty() && !commandRecord.defaultSet.isEmpty()) {
				buttonChange.setText(Util.getString(resourceBundle, "buttonChange.restore")); //$NON-NLS-1$
			} else
				buttonChange.setText(Util.getString(resourceBundle, "buttonChange.remove")); //$NON-NLS-1$			 
		}

		if (validKeySequence) {
			String text = MessageFormat.format(Util.getString(resourceBundle, "labelCommandsForSequence.selection"), new Object[] { '\''+ KeySupport.formatSequence(keySequence) + '\''}); //$NON-NLS-1$
			labelCommandsForSequence.setText(text);
		} else 
			labelCommandsForSequence.setText(Util.getString(resourceBundle, "labelCommandsForSequence.noSelection")); //$NON-NLS-1$
	}

	private void buildCommandRecords(SortedMap tree, String command, List commandRecords) {
		if (commandRecords != null) {
			commandRecords.clear();
				
			if (tree != null) {
				Iterator iterator = tree.entrySet().iterator();
					
				while (iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					Sequence sequence = (Sequence) entry.getKey();					
					Map scopeMap = (Map) entry.getValue();						
		
					if (scopeMap != null) {
						Iterator iterator2 = scopeMap.entrySet().iterator();
						
						while (iterator2.hasNext()) {
							Map.Entry entry2 = (Map.Entry) iterator2.next();
							String scope = (String) entry2.getKey();										
							Map configurationMap = (Map) entry2.getValue();						
							Iterator iterator3 = configurationMap.entrySet().iterator();
										
							while (iterator3.hasNext()) {
								Map.Entry entry3 = (Map.Entry) iterator3.next();
								String configuration = (String) entry3.getKey();					
								CommandSetPair commandSetPair = (CommandSetPair) entry3.getValue();													
								Set customSet = new HashSet();
								Set defaultSet = new HashSet();						
	
								if (commandSetPair.customSet != null)
									customSet.addAll(commandSetPair.customSet);

								if (commandSetPair.defaultSet != null)
									defaultSet.addAll(commandSetPair.defaultSet);
									
								if (customSet.contains(command) || defaultSet.contains(command)) {
									CommandRecord commandRecord = new CommandRecord();
									commandRecord.command = command;
									commandRecord.sequence = sequence;
									commandRecord.scope = scope;
									commandRecord.configuration = configuration;
									commandRecord.customSet = customSet;
									commandRecord.defaultSet = defaultSet;
									commandRecord.calculate();	
									commandRecords.add(commandRecord);									
								}
							}
						}
					}
				}												
			}	
		}
	}
	
	private void buildSequenceRecords(SortedMap tree, Sequence sequence, List sequenceRecords) {
		if (sequenceRecords != null) {
			sequenceRecords.clear();
			
			if (tree != null && sequence != null) {
				Map scopeMap = (Map) tree.get(sequence);
			
				if (scopeMap != null) {
					Iterator iterator = scopeMap.entrySet().iterator();
			
					while (iterator.hasNext()) {
						Map.Entry entry = (Map.Entry) iterator.next();
						String scope = (String) entry.getKey();					
						Map configurationMap = (Map) entry.getValue();						
						Iterator iterator2 = configurationMap.entrySet().iterator();
							
						while (iterator2.hasNext()) {
							Map.Entry entry2 = (Map.Entry) iterator2.next();
							String configuration = (String) entry2.getKey();					
							CommandSetPair commandSetPair = (CommandSetPair) entry2.getValue();													
							Set customSet = new HashSet();
							Set defaultSet = new HashSet();						
	
							if (commandSetPair.customSet != null)
								customSet.addAll(commandSetPair.customSet);

							if (commandSetPair.defaultSet != null)
								defaultSet.addAll(commandSetPair.defaultSet);							

							SequenceRecord sequenceRecord = new SequenceRecord();
							sequenceRecord.scope = scope;
							sequenceRecord.configuration = configuration;							
							sequenceRecord.customSet = customSet;
							sequenceRecord.defaultSet = defaultSet;		
							sequenceRecord.calculate();
							sequenceRecords.add(sequenceRecord);
						}												
					}	
				}								
			}			
		}
	}

	private void buildTableCommand() {
		tableSequencesForCommand.removeAll();

		for (int i = 0; i < commandRecords.size(); i++) {
			CommandRecord commandRecord = (CommandRecord) commandRecords.get(i);
			Set customSet = commandRecord.customSet;
			Set defaultSet = commandRecord.defaultSet;
			int difference = DIFFERENCE_NONE;
			//String commandId = null;
			boolean commandConflict = false;
			String alternateCommandId = null;
			boolean alternateCommandConflict = false;
	
			if (customSet.isEmpty()) {
				if (defaultSet.contains(commandRecord.command)) {												
					//commandId = commandRecord.commandId;
					commandConflict = commandRecord.defaultConflict;					
				}
			} else {
				if (defaultSet.isEmpty()) {									
					if (customSet.contains(commandRecord.command)) {													
						difference = DIFFERENCE_ADD;
						//commandId = commandRecord.commandId;
						commandConflict = commandRecord.customConflict;
					}
				} else {
					if (customSet.contains(commandRecord.command)) {
						difference = DIFFERENCE_CHANGE;
						//commandId = commandRecord.commandId;
						commandConflict = commandRecord.customConflict;		
						alternateCommandId = commandRecord.defaultCommand;
						alternateCommandConflict = commandRecord.defaultConflict;
					} else {
						if (defaultSet.contains(commandRecord.command)) {	
							difference = DIFFERENCE_MINUS;
							//commandId = commandRecord.commandId;
							commandConflict = commandRecord.defaultConflict;		
							alternateCommandId = commandRecord.customCommand;
							alternateCommandConflict = commandRecord.customConflict;
						}
					}
				}								
			}

			TableItem tableItem = new TableItem(tableSequencesForCommand, SWT.NULL);					

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

			Scope scope = (Scope) scopesById.get(commandRecord.scope);
			tableItem.setText(1, scope != null ? scope.getName() : bracket(commandRecord.scope));
			Configuration keyConfiguration = (Configuration) keyConfigurationsById.get(commandRecord.configuration);			
			tableItem.setText(2, keyConfiguration != null ? keyConfiguration.getName() : bracket(commandRecord.configuration));
			boolean conflict = commandConflict || alternateCommandConflict;
			StringBuffer stringBuffer = new StringBuffer();

			if (commandRecord.sequence != null)
				stringBuffer.append(KeySupport.formatSequence(commandRecord.sequence));

			if (commandConflict)
				stringBuffer.append(SPACE + COMMAND_CONFLICT);

			String alternateCommandName = null;
				
			if (alternateCommandId == null) 
				alternateCommandName = COMMAND_UNDEFINED;
			else if (alternateCommandId.length() == 0)
				alternateCommandName = COMMAND_NOTHING;				
			else {
				Command command = (Command) commandsById.get(alternateCommandId);
					
				if (command != null)
					alternateCommandName = command.getName();
				else
					alternateCommandName = bracket(alternateCommandId);
			}

			if (alternateCommandConflict)
				alternateCommandName += SPACE + COMMAND_CONFLICT;

			stringBuffer.append(SPACE);

			if (difference == DIFFERENCE_CHANGE)
				stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle, "was"), new Object[] { alternateCommandName })); //$NON-NLS-1$
			else if (difference == DIFFERENCE_MINUS)
				stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle, "now"), new Object[] { alternateCommandName })); //$NON-NLS-1$

			tableItem.setText(3, stringBuffer.toString());				

			if (difference == DIFFERENCE_MINUS) {
				if (conflict)
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT_MINUS));	
				else 
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS));	
			} else if (conflict)
				tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT));	
		}			
	}
	
	private void buildTableKeySequence() {
		tableCommandsForSequence.removeAll();
	
		for (int i = 0; i < keySequenceRecords.size(); i++) {
			SequenceRecord keySequenceRecord = (SequenceRecord) keySequenceRecords.get(i);
			int difference = DIFFERENCE_NONE;
			String commandId = null;
			boolean commandConflict = false;
			String alternateCommandId = null;
			boolean alternateCommandConflict = false;

			if (keySequenceRecord.customSet.isEmpty()) {
				commandId = keySequenceRecord.defaultCommand;															
				commandConflict = keySequenceRecord.defaultConflict;
			} else {
				commandId = keySequenceRecord.customCommand;															
				commandConflict = keySequenceRecord.customConflict;						

				if (keySequenceRecord.defaultSet.isEmpty())
					difference = DIFFERENCE_ADD;
				else {
					difference = DIFFERENCE_CHANGE;									
					alternateCommandId = keySequenceRecord.defaultCommand;
					alternateCommandConflict = keySequenceRecord.defaultConflict;																		
				}
			}

			TableItem tableItem = new TableItem(tableCommandsForSequence, SWT.NULL);					

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

			Scope scope = (Scope) scopesById.get(keySequenceRecord.scope);
			tableItem.setText(1, scope != null ? scope.getName() : bracket(keySequenceRecord.scope));
			Configuration keyConfiguration = (Configuration) keyConfigurationsById.get(keySequenceRecord.configuration);			
			tableItem.setText(2, keyConfiguration != null ? keyConfiguration.getName() : bracket(keySequenceRecord.configuration));
			boolean conflict = commandConflict || alternateCommandConflict;
			StringBuffer stringBuffer = new StringBuffer();
			String commandName = null;
					
			if (commandId == null) 
				commandName = COMMAND_UNDEFINED;
			else if (commandId.length() == 0)
				commandName = COMMAND_NOTHING;				
			else {
				Command command = (Command) commandsById.get(commandId);
						
				if (command != null)
					commandName = command.getName();
				else
					commandName = bracket(commandId);
			}
			
			stringBuffer.append(commandName);

			if (commandConflict)
				stringBuffer.append(SPACE + COMMAND_CONFLICT);

			String alternateCommandName = null;
				
			if (alternateCommandId == null) 
				alternateCommandName = COMMAND_UNDEFINED;
			else if (alternateCommandId.length() == 0)
				alternateCommandName = COMMAND_NOTHING;				
			else {
				Command command = (Command) commandsById.get(alternateCommandId);
					
				if (command != null)
					alternateCommandName = command.getName();
				else
					alternateCommandName = bracket(alternateCommandId);
			}

			if (alternateCommandConflict)
				alternateCommandName += SPACE + COMMAND_CONFLICT;

			stringBuffer.append(SPACE);
			
			if (difference == DIFFERENCE_CHANGE)
				stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle, "was"), new Object[] { alternateCommandName })); //$NON-NLS-1$

			tableItem.setText(3, stringBuffer.toString());

			if (difference == DIFFERENCE_MINUS) {
				if (conflict)
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT_MINUS));	
				else 
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS));	
			} else if (conflict)
				tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT));	
		}
	}

	private void selectTableCommand(String scopeId, String keyConfigurationId, Sequence keySequence) {	
		int selection = -1;
		
		for (int i = 0; i < commandRecords.size(); i++) {
			CommandRecord commandRecord = (CommandRecord) commandRecords.get(i);			
			
			if (Util.equals(scopeId, commandRecord.scope) && Util.equals(keyConfigurationId, commandRecord.configuration) && Util.equals(keySequence, commandRecord.sequence)) {
				selection = i;
				break;			
			}			
		}

		if (tableSequencesForCommand.getSelectionCount() > 1)
			tableSequencesForCommand.deselectAll();

		if (selection != tableSequencesForCommand.getSelectionIndex()) {
			if (selection == -1 || selection >= tableSequencesForCommand.getItemCount())
				tableSequencesForCommand.deselectAll();
			else
				tableSequencesForCommand.select(selection);
		}
	}

	private void selectTableKeySequence(String scopeId, String keyConfigurationId) {		
		int selection = -1;
		
		for (int i = 0; i < keySequenceRecords.size(); i++) {
			SequenceRecord keySequenceRecord = (SequenceRecord) keySequenceRecords.get(i);			
			
			if (Util.equals(scopeId, keySequenceRecord.scope) && Util.equals(keyConfigurationId, keySequenceRecord.configuration)) {
				selection = i;
				break;			
			}			
		}

		if (tableCommandsForSequence.getSelectionCount() > 1)
			tableCommandsForSequence.deselectAll();

		if (selection != tableCommandsForSequence.getSelectionIndex()) {
			if (selection == -1 || selection >= tableCommandsForSequence.getItemCount())
				tableCommandsForSequence.deselectAll();
			else
				tableCommandsForSequence.select(selection);
		}
	}

	private CommandRecord getSelectedCommandRecord() {		
		int selection = tableSequencesForCommand.getSelectionIndex();
		
		if (selection >= 0 && selection < commandRecords.size() && tableSequencesForCommand.getSelectionCount() == 1)
			return (CommandRecord) commandRecords.get(selection);
		else
			return null;
	}

	private SequenceRecord getSelectedKeySequenceRecord() {		
		int selection = tableCommandsForSequence.getSelectionIndex();
		
		if (selection >= 0 && selection < keySequenceRecords.size() && tableCommandsForSequence.getSelectionCount() == 1)
			return (SequenceRecord) keySequenceRecords.get(selection);
		else
			return null;
	}

	private Sequence getKeySequence() {
		Sequence keySequence = null;
		String name = comboSequence.getText();		
		keySequence = (Sequence) keySequencesByName.get(name);
			
		if (keySequence == null)
			keySequence = KeySupport.parseSequence(name);

		return keySequence;
	}

	private void setKeySequence(Sequence keySequence) {
		comboSequence.setText(keySequence != null ? KeySupport.formatSequence(keySequence) : Util.ZERO_LENGTH_STRING);
	}

	private String getScopeId() {
		int selection = comboScope.getSelectionIndex();
		List scopes = new ArrayList(scopesByName.values());			
		
		if (selection >= 0 && selection < scopes.size()) {
			Scope scope = (Scope) scopes.get(selection);
			return scope.getId();				
		}
		
		return null;
	}

	private void setScopeId(String scopeId) {				
		comboScope.clearSelection();
		comboScope.deselectAll();
		
		if (scopeId != null) {
			List scopes = new ArrayList(scopesByName.values());			

			for (int i = 0; i < scopes.size(); i++) {
				Scope scope = (Scope) scopes.get(i);		
				
				if (scope.getId().equals(scopeId)) {
					comboScope.select(i);
					break;		
				}
			}
		}
	}

	private String getKeyConfigurationId() {
		int selection = comboConfiguration.getSelectionIndex();
		List keyConfigurations = new ArrayList(keyConfigurationsByName.values());
		
		if (selection >= 0 && selection < keyConfigurations.size()) {
			Configuration keyConfiguration = (Configuration) keyConfigurations.get(selection);
			return keyConfiguration.getId();				
		}
		
		return null;
	}

	private void setKeyConfigurationId(String keyConfigurationId) {				
		comboConfiguration.clearSelection();
		comboConfiguration.deselectAll();
		
		if (keyConfigurationId != null) {
			List keyConfigurations = new ArrayList(keyConfigurationsByName.values());
				
			for (int i = 0; i < keyConfigurations.size(); i++) {
				Configuration keyConfiguration = (Configuration) keyConfigurations.get(i);		
				
				if (keyConfiguration.getId().equals(keyConfigurationId)) {
					comboConfiguration.select(i);
					break;		
				}
			}
		}
	}

	private String bracket(String string) {
		return string != null ? '[' + string + ']' : "[]"; //$NON-NLS-1$	
	}

	private SortedMap build(SortedSet sequenceBindingSet) {
		SortedMap tree = new TreeMap();
		Iterator iterator = sequenceBindingSet.iterator();
		
		while (iterator.hasNext()) {
			SequenceBinding sequenceBinding = (SequenceBinding) iterator.next();
			Sequence sequence = sequenceBinding.getSequence();
			String configuration = sequenceBinding.getConfiguration();			
			String command = sequenceBinding.getCommand();			
			Path locale = SequenceMachine.getPathForLocale(sequenceBinding.getLocale());
			Path platform = SequenceMachine.getPathForPlatform(sequenceBinding.getPlatform());
			List paths = new ArrayList();
			paths.add(platform);
			paths.add(locale);
			State platformLocale = State.create(paths);
			Integer rank = new Integer(sequenceBinding.getRank());
			String scope = sequenceBinding.getScope();			
			SortedMap scopeMap = (SortedMap) tree.get(sequence);
			
			if (scopeMap == null) {
				scopeMap = new TreeMap();
				tree.put(sequence, scopeMap);
			}

			SortedMap configurationMap = (SortedMap) scopeMap.get(scope);
			
			if (configurationMap == null) {
				configurationMap = new TreeMap();
				scopeMap.put(scope, configurationMap);
			}

			SortedMap rankMap = (SortedMap) configurationMap.get(configuration);
		
			if (rankMap == null) {
				rankMap = new TreeMap();	
				configurationMap.put(configuration, rankMap);
			}

			SortedMap platformLocaleMap = (SortedMap) rankMap.get(rank);

			if (platformLocaleMap == null) {
				platformLocaleMap = new TreeMap();	
				rankMap.put(rank, platformLocaleMap);
			}

			Set commandSet = (Set) platformLocaleMap.get(platformLocale);

			if (commandSet == null) {
				commandSet = new HashSet();	
				platformLocaleMap.put(platformLocale, commandSet);
			}

			commandSet.add(command);										
		}

		List paths = new ArrayList();
		paths.add(SequenceMachine.getSystemPlatform());
		paths.add(SequenceMachine.getSystemLocale());
		State platformLocale = State.create(paths);
		iterator = tree.values().iterator();
		
		while (iterator.hasNext()) {
			SortedMap scopeMap = (SortedMap) iterator.next();			
			Iterator iterator2 = scopeMap.values().iterator();
			
			while (iterator2.hasNext()) {
				SortedMap configurationMap = (SortedMap) iterator2.next();			
				Iterator iterator3 = configurationMap.entrySet().iterator();
				
				while (iterator3.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator3.next();
					entry.setValue(solveRankMap((SortedMap) entry.getValue(), platformLocale));
				}
			}
		}
		
		return tree;
	}

	private CommandSetPair solveRankMap(SortedMap rankMap, State platformLocale) {
		CommandSetPair commandSetPair = new CommandSetPair();		
		Iterator iterator = rankMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Integer rank = (Integer) entry.getKey();
			SortedMap platformLocaleMap = (SortedMap) entry.getValue();			
			Set commandSet = solvePlatformLocaleMap(platformLocaleMap, platformLocale);

			if (rank.intValue() == 0)
				commandSetPair.customSet = commandSet;
			else if (commandSetPair.defaultSet == null)
				commandSetPair.defaultSet = commandSet;
		}

		return commandSetPair;
	}

	private Set solvePlatformLocaleMap(SortedMap platformLocaleMap, State platformLocale) {
		int bestDefinedMatch = -1;
		Set bestDefinedCommandSet = null;		
		int bestUndefinedMatch = -1;
		Set bestUndefinedCommandSet = null;		
		Iterator iterator = platformLocaleMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testPlatformLocale = (State) entry.getKey();
			Set testCommandSet = (Set) entry.getValue();
			int testMatch = testPlatformLocale.match(platformLocale);

			if (testMatch >= 0) {
				String testCommand = SequenceNode.solveCommandSet(testCommandSet);

				if (testCommand != null) {
					if (bestDefinedMatch == -1 || testMatch < bestDefinedMatch) {
						bestDefinedMatch = testMatch;
						bestDefinedCommandSet = testCommandSet;
					}	
				} else {
					if (bestUndefinedMatch == -1 || testMatch < bestUndefinedMatch) {
						bestUndefinedMatch = testMatch;
						bestUndefinedCommandSet = testCommandSet;
					}					
				}
			}	
		}

		return bestDefinedMatch >= 0 ? bestDefinedCommandSet : bestUndefinedCommandSet; 
	}

	private SortedSet solve(SortedMap tree) {
		SortedSet sequenceBindingSet = new TreeSet();		
		Iterator iterator = tree.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();	
			Sequence sequence = (Sequence) entry.getKey();
			SortedMap scopeMap = (SortedMap) entry.getValue();			
			Iterator iterator2 = scopeMap.entrySet().iterator();
			
			while (iterator2.hasNext()) {
				Map.Entry entry2 = (Map.Entry) iterator2.next();	
				String scope = (String) entry2.getKey();
				SortedMap configurationMap = (SortedMap) entry2.getValue();
				Iterator iterator3 = configurationMap.entrySet().iterator();
				
				while (iterator3.hasNext()) {
					Map.Entry entry3 = (Map.Entry) iterator3.next();					
					String configuration = (String) entry3.getKey();
					CommandSetPair commandSetPair = (CommandSetPair) entry3.getValue();
					Set customSet = commandSetPair.customSet;
					
					if (customSet != null) {
						Iterator iterator4 = customSet.iterator();
						
						while (iterator4.hasNext()) {
							String command = (String) iterator4.next();
							sequenceBindingSet.add(SequenceBinding.create(configuration, command, Util.ZERO_LENGTH_STRING, Util.ZERO_LENGTH_STRING, null, 0, scope, sequence));									
						}
					}
				}
			}
		}
		
		return sequenceBindingSet;		
	}

	private void set(SortedMap tree, Sequence sequence, String scope, String configuration, String command) {
		SortedMap scopeMap = (SortedMap) tree.get(sequence);
			
		if (scopeMap == null) {
			scopeMap = new TreeMap();
			tree.put(sequence, scopeMap);
		}

		SortedMap configurationMap = (SortedMap) scopeMap.get(scope);
			
		if (configurationMap == null) {
			configurationMap = new TreeMap();
			scopeMap.put(scope, configurationMap);
		}
	
		CommandSetPair commandSetPair = (CommandSetPair) configurationMap.get(configuration);
		
		if (commandSetPair == null) {
			commandSetPair = new CommandSetPair();
			configurationMap.put(configuration, commandSetPair);
		}
		
		Set customSet = new HashSet();
		customSet.add(command);		
		commandSetPair.customSet = customSet;		
	}

	private void clear(SortedMap tree, Sequence sequence, String scope, String configuration) {
		SortedMap scopeMap = (SortedMap) tree.get(sequence);

		if (scopeMap != null) {
			SortedMap configurationMap = (SortedMap) scopeMap.get(scope);
			
			if (configurationMap != null) {
				CommandSetPair commandSetPair = (CommandSetPair) configurationMap.get(configuration);
				
				if (commandSetPair != null) {				
					commandSetPair.customSet = null;

					if (commandSetPair.defaultSet == null) {					
						configurationMap.remove(configuration);
						
						if (configurationMap.isEmpty()) {
							scopeMap.remove(scope);
				
							if (scopeMap.isEmpty())
								tree.remove(sequence);
						}
					}
				}
			}
		}
	}
}
