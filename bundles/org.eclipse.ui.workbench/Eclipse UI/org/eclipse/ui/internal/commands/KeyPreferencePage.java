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
import java.util.HashMap;
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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WWinKeyBindingService;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;

public class KeyPreferencePage extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {

	private final static ResourceBundle resourceBundle = ResourceBundle.getBundle(KeyPreferencePage.class.getName());

	private final static String COMMAND_CONFLICT = Util.getString(resourceBundle, "commandConflict"); //$NON-NLS-1$
	private final static String COMMAND_UNDEFINED = Util.getString(resourceBundle, "commandUndefined"); //$NON-NLS-1$
	private final static int DIFFERENCE_ADD = 0;	
	private final static int DIFFERENCE_CHANGE = 1;	
	private final static int DIFFERENCE_MINUS = 2;	
	private final static int DIFFERENCE_NONE = 3;	
	private final static Image IMAGE_CHANGE = ImageFactory.getImage("change"); //$NON-NLS-1$
	private final static Image IMAGE_MINUS = ImageFactory.getImage("minus"); //$NON-NLS-1$
	private final static Image IMAGE_PLUS = ImageFactory.getImage("plus"); //$NON-NLS-1$
	private final static RGB RGB_CONFLICT = new RGB(255, 0, 0);
	private final static RGB RGB_CONFLICT_MINUS = new RGB(255, 192, 192);
	private final static RGB RGB_MINUS =	new RGB(192, 192, 192);
	private final static char SPACE = ' '; //$NON-NLS-1$
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	private final class CommandRecord {

		String commandId;
		KeySequence keySequence;
		String scopeId;
		String keyConfigurationId;
		Set customSet;
		Set defaultSet;

		boolean customConflict = false;
		String customCommandId = null;
		boolean defaultConflict = false;
		String defaultCommandId = null;	

		void calculate() {
			if (customSet.size() > 1)
				customConflict = true;
			else if (!customSet.isEmpty())				
				customCommandId = (String) customSet.iterator().next();
	
			if (defaultSet.size() > 1)
				defaultConflict = true;
			else if (!defaultSet.isEmpty())				
				defaultCommandId = (String) defaultSet.iterator().next();
		}
	}

	private final class KeySequenceRecord {

		String scopeId;
		String keyConfigurationId;
		Set customSet;
		Set defaultSet;

		boolean customConflict = false;
		String customCommandId = null;
		boolean defaultConflict = false;
		String defaultCommandId = null;	

		void calculate() {
			if (customSet.size() > 1)
				customConflict = true;
			else if (!customSet.isEmpty())				
				customCommandId = (String) customSet.iterator().next();
	
			if (defaultSet.size() > 1)
				defaultConflict = true;
			else if (!defaultSet.isEmpty())				
				defaultCommandId = (String) defaultSet.iterator().next();
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

	private Label labelActiveKeyConfiguration; 
	private Combo comboActiveKeyConfiguration;
	//private Button buttonNew; 
	//private Button buttonRename;
	//private Button buttonDelete;
	private Label labelCommands;
	private TreeViewer treeViewerCommands;
	//private Button buttonCategorize;
	private Label labelKeySequencesForCommand;
	private Table tableKeySequencesForCommand;
	//private TableViewer tableViewerKeySequencesForCommand;	
	private Label labelKeySequence;
	private Combo comboKeySequence;
	private Label labelScope; 
	private Combo comboScope;
	private Label labelKeyConfiguration; 
	private Combo comboKeyConfiguration;
	private Button buttonChange;
	private Label labelCommandsForKeySequence;
	private Table tableCommandsForKeySequence;
	//private TableViewer tableViewerCommandsForKeySequence;

	private IWorkbench workbench;
	private State[] states;

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
	private List coreRegionalKeyBindings;

	private List localActiveKeyConfigurations;
	private List localKeyBindings;
	private List localKeyConfigurations;
	private List localRegionalKeyBindings;

	private List preferenceActiveKeyConfigurations;
	private List preferenceKeyBindings;
	private List preferenceKeyConfigurations;
	private List preferenceRegionalKeyBindings;

	private ActiveKeyConfiguration activeKeyConfiguration;	
	private List activeKeyConfigurations;
	private List keyBindings;
	private List keyConfigurations;
	private SortedMap keyConfigurationsById;
	private SortedMap keyConfigurationsByName;		
	private List regionalKeyBindings;
	private SortedMap tree;
	private List commandRecords = new ArrayList();	
	private List keySequenceRecords = new ArrayList();
	private SortedMap keySequencesByName;

	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		List pathItems = new ArrayList();
		pathItems.add(KeyManager.systemPlatform());
		pathItems.add(KeyManager.systemLocale());
		states = new State[] { State.create(pathItems) };
		PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();

		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
		}
	
		preferenceActiveKeyConfigurations = new ArrayList(preferenceRegistry.getActiveKeyConfigurations());
		preferenceKeyBindings = new ArrayList(preferenceRegistry.getKeyBindings());
		preferenceKeyConfigurations = new ArrayList(preferenceRegistry.getKeyConfigurations());
		preferenceRegionalKeyBindings = new ArrayList(preferenceRegistry.getRegionalKeyBindings());
	}

	public boolean performOk() {
		copyFromUI();
		PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();
		preferenceRegistry.setActiveKeyConfigurations(preferenceActiveKeyConfigurations);
		preferenceRegistry.setKeyBindings(preferenceKeyBindings);
		preferenceRegistry.setKeyConfigurations(preferenceKeyConfigurations);
		preferenceRegistry.setRegionalKeyBindings(preferenceRegionalKeyBindings);
		
		try {
			preferenceRegistry.save();
		} catch (IOException eIO) {
		}

		if (workbench instanceof Workbench) {
			Workbench workbench = (Workbench) this.workbench;
			KeyManager.getInstance().getKeyMachine().setKeyConfiguration(activeKeyConfiguration != null ? activeKeyConfiguration.getValue() : ZERO_LENGTH_STRING); //$NON-NLS-1$
			IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

			if (workbenchWindow != null && workbenchWindow instanceof WorkbenchWindow) {
				WWinKeyBindingService wWinKeyBindingService = ((WorkbenchWindow) workbenchWindow).getKeyBindingService();

				if (wWinKeyBindingService != null)
					wWinKeyBindingService.clear();

				MenuManager menuManager = ((WorkbenchWindow) workbenchWindow).getMenuManager();
				menuManager.update(IAction.TEXT);
			}
		}

		KeyManager.getInstance().update();
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
			coreRegionalKeyBindings = new ArrayList(coreRegistry.getRegionalKeyBindings());

			localActiveKeyConfigurations = new ArrayList(localRegistry.getActiveKeyConfigurations());
			localKeyBindings = new ArrayList(localRegistry.getKeyBindings());
			localKeyConfigurations = new ArrayList(localRegistry.getKeyConfigurations());
			localRegionalKeyBindings = new ArrayList(localRegistry.getRegionalKeyBindings());
	
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
			preferenceRegionalKeyBindings = new ArrayList();
			copyToUI();
		}
	}

	private void copyFromUI() {
		activeKeyConfiguration = null; 		
		preferenceActiveKeyConfigurations = new ArrayList();		
		int index = comboActiveKeyConfiguration.getSelectionIndex();
				
		if (index >= 0) {
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsByName.get(comboActiveKeyConfiguration.getItem(index));
			activeKeyConfiguration = ActiveKeyConfiguration.create(null, keyConfiguration.getId());
			preferenceActiveKeyConfigurations.add(activeKeyConfiguration);
		}

		preferenceKeyBindings = new ArrayList(solve(tree));
		preferenceRegionalKeyBindings = new ArrayList();
	}

	private void copyToUI() {	
		// TODO validate lists for referential integrity
		List activeKeyConfigurations = new ArrayList();
		activeKeyConfigurations.addAll(coreActiveKeyConfigurations);
		activeKeyConfigurations.addAll(localActiveKeyConfigurations);
		activeKeyConfigurations.addAll(preferenceActiveKeyConfigurations);

		if (!Util.equals(activeKeyConfigurations, this.activeKeyConfigurations)) {
			this.activeKeyConfigurations = Collections.unmodifiableList(activeKeyConfigurations);
			activeKeyConfiguration = (ActiveKeyConfiguration) this.activeKeyConfigurations.get(this.activeKeyConfigurations.size() - 1);				
		}
		
		List keyConfigurations = new ArrayList();
		keyConfigurations.addAll(coreKeyConfigurations);
		keyConfigurations.addAll(localKeyConfigurations);
		keyConfigurations.addAll(preferenceKeyConfigurations);

		if (!Util.equals(keyConfigurations, this.keyConfigurations)) {
			this.keyConfigurations = Collections.unmodifiableList(keyConfigurations);
			keyConfigurationsById = Collections.unmodifiableSortedMap(KeyConfiguration.sortedMapById(this.keyConfigurations));
			keyConfigurationsByName = Collections.unmodifiableSortedMap(KeyConfiguration.sortedMapByName(this.keyConfigurations));
			Set keyConfigurationNameSet = keyConfigurationsByName.keySet();
			comboActiveKeyConfiguration.setItems((String[]) keyConfigurationNameSet.toArray(new String[keyConfigurationNameSet.size()]));
			comboKeyConfiguration.setItems((String[]) keyConfigurationNameSet.toArray(new String[keyConfigurationNameSet.size()]));
		}		

		int index = -1;
			
		if (activeKeyConfiguration != null) {
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(activeKeyConfiguration.getValue());

			if (keyConfiguration != null)
				index = comboActiveKeyConfiguration.indexOf(keyConfiguration.getName());
		}
			
		if (index >= 0)
			comboActiveKeyConfiguration.select(index);
		else {
			comboActiveKeyConfiguration.clearSelection();
			comboActiveKeyConfiguration.deselectAll();
		}

		SortedSet coreKeyBindingSet = new TreeSet();
		coreKeyBindingSet.addAll(coreKeyBindings);			
		SortedSet coreRegionalKeyBindingSet = new TreeSet();
		coreRegionalKeyBindingSet.addAll(coreRegionalKeyBindings);
		coreKeyBindingSet.addAll(KeyManager.solveRegionalKeyBindingSet(coreRegionalKeyBindingSet, states));

		SortedSet localKeyBindingSet = new TreeSet();
		localKeyBindingSet.addAll(localKeyBindings);			
		SortedSet localRegionalKeyBindingSet = new TreeSet();
		localRegionalKeyBindingSet.addAll(localRegionalKeyBindings);
		localKeyBindingSet.addAll(KeyManager.solveRegionalKeyBindingSet(localRegionalKeyBindingSet, states));

		SortedSet preferenceKeyBindingSet = new TreeSet();
		preferenceKeyBindingSet.addAll(preferenceKeyBindings);			
		SortedSet preferenceRegionalKeyBindingSet = new TreeSet();
		preferenceRegionalKeyBindingSet.addAll(preferenceRegionalKeyBindings);
		preferenceKeyBindingSet.addAll(KeyManager.solveRegionalKeyBindingSet(preferenceRegionalKeyBindingSet, states));

		tree = new TreeMap();
		SortedSet keyBindingSet = new TreeSet();
		keyBindingSet.addAll(coreKeyBindingSet);
		keyBindingSet.addAll(localKeyBindingSet);
		keyBindingSet.addAll(preferenceKeyBindingSet);
		Iterator iterator = keyBindingSet.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();				
			set(tree, keyBinding, false);			
		}

		keySequencesByName = new TreeMap();
		iterator = tree.keySet().iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (object instanceof KeySequence) {
				KeySequence keySequence = (KeySequence) object;
				String name = keySequence.toString();
				keySequencesByName.put(name, keySequence);
			}
		}		

		Set keySequenceNameSet = keySequencesByName.keySet();
		comboKeySequence.setItems((String[]) keySequenceNameSet.toArray(new String[keySequenceNameSet.size()]));
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

		labelActiveKeyConfiguration = new Label(compositeActiveKeyConfiguration, SWT.LEFT);
		labelActiveKeyConfiguration.setFont(compositeActiveKeyConfiguration.getFont());
		labelActiveKeyConfiguration.setText(Util.getString(resourceBundle, "labelActiveKeyConfiguration")); //$NON-NLS-1$

		comboActiveKeyConfiguration = new Combo(compositeActiveKeyConfiguration, SWT.READ_ONLY);
		comboActiveKeyConfiguration.setFont(compositeActiveKeyConfiguration.getFont());
		GridData gridData = new GridData();
		gridData.widthHint = 150;
		comboActiveKeyConfiguration.setLayoutData(gridData);

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
		gridData.heightHint = 200;
		gridData.widthHint = 220;
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

		labelKeySequencesForCommand = new Label(compositeAssignmentRight, SWT.LEFT);
		labelKeySequencesForCommand.setFont(compositeAssignmentRight.getFont());
		labelKeySequencesForCommand.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelKeySequencesForCommand.setText(Util.getString(resourceBundle, "labelKeySequencesForCommand.noSelection")); //$NON-NLS-1$

		tableKeySequencesForCommand = new Table(compositeAssignmentRight, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableKeySequencesForCommand.setFont(compositeAssignmentRight.getFont());
		tableKeySequencesForCommand.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 90;
		gridData.widthHint = 400;	
		tableKeySequencesForCommand.setLayoutData(gridData);

		TableColumn tableColumn = new TableColumn(tableKeySequencesForCommand, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);

		tableColumn = new TableColumn(tableKeySequencesForCommand, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnScope")); //$NON-NLS-1$
		tableColumn.setWidth(80);

		tableColumn = new TableColumn(tableKeySequencesForCommand, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnKeyConfiguration")); //$NON-NLS-1$
		tableColumn.setWidth(80);

		tableColumn = new TableColumn(tableKeySequencesForCommand, SWT.NULL, 3);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnKeySequence")); //$NON-NLS-1$
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
		
		labelKeyConfiguration = new Label(compositeAssignmentChange, SWT.LEFT);
		labelKeyConfiguration.setFont(compositeAssignmentChange.getFont());
		labelKeyConfiguration.setText(Util.getString(resourceBundle, "labelKeyConfiguration")); //$NON-NLS-1$

		comboKeyConfiguration = new Combo(compositeAssignmentChange, SWT.READ_ONLY);
		comboKeyConfiguration.setFont(compositeAssignmentChange.getFont());
		gridData = new GridData();
		gridData.widthHint = 150;
		comboKeyConfiguration.setLayoutData(gridData);

		labelKeySequence = new Label(compositeAssignmentChange, SWT.LEFT);
		labelKeySequence.setFont(compositeAssignmentChange.getFont());
		labelKeySequence.setText(Util.getString(resourceBundle, "labelKeySequence")); //$NON-NLS-1$

		comboKeySequence = new Combo(compositeAssignmentChange, SWT.NULL);
		comboKeySequence.setFont(compositeAssignmentChange.getFont());
		gridData = new GridData();
		gridData.widthHint = 220;
		comboKeySequence.setLayoutData(gridData);

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
		
		labelCommandsForKeySequence = new Label(compositeAssignmentRight, SWT.LEFT);
		labelCommandsForKeySequence.setFont(compositeAssignmentRight.getFont());
		labelCommandsForKeySequence.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelCommandsForKeySequence.setText(Util.getString(resourceBundle, "labelCommandsForKeySequence.noSelection")); //$NON-NLS-1$

		tableCommandsForKeySequence = new Table(compositeAssignmentRight, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableCommandsForKeySequence.setFont(compositeAssignmentRight.getFont());
		tableCommandsForKeySequence.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		gridData.widthHint = 400;	
		tableCommandsForKeySequence.setLayoutData(gridData);

		tableColumn = new TableColumn(tableCommandsForKeySequence, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);
		
		tableColumn = new TableColumn(tableCommandsForKeySequence, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnScope")); //$NON-NLS-1$
		tableColumn.setWidth(80);

		tableColumn = new TableColumn(tableCommandsForKeySequence, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnKeyConfiguration")); //$NON-NLS-1$
		tableColumn.setWidth(80);

		tableColumn = new TableColumn(tableCommandsForKeySequence, SWT.NULL, 3);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnCommand")); //$NON-NLS-1$
		tableColumn.setWidth(220);	
		
		//tableViewerCommandsForKeySequence = new TableViewer(tableCommandsForKeySequence);
		//tableViewerCommandsForKeySequence.setContentProvider(new TableViewerCommandsForKeySequenceContentProvider());
		//tableViewerCommandsForKeySequence.setLabelProvider(new TableViewerCommandsForKeySequenceLabelProvider());

		comboActiveKeyConfiguration.addSelectionListener(new SelectionAdapter() {
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

		tableKeySequencesForCommand.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedTableKeySequencesForCommand();	
			}			
		});		

		tableKeySequencesForCommand.addSelectionListener(new SelectionAdapter() {
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

		comboKeySequence.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent modifyEvent) {
				modifiedComboKeySequence();
			}	
		});

		comboKeySequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboKeySequence();
			}	
		});

		comboScope.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboScope();
			}	
		});
		
		comboKeyConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboKeyConfiguration();
			}	
		});

		buttonChange.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonChange();
			}	
		});

		tableCommandsForKeySequence.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedTableCommandsForKeySequence();	
			}			
		});		

		tableCommandsForKeySequence.addSelectionListener(new SelectionAdapter() {
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
		KeySequence keySequence = getKeySequence();
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
			setScopeId(commandRecord.scopeId);
			setKeyConfigurationId(commandRecord.keyConfigurationId);				

			// careful here. this triggers a modify event.
			setKeySequence(commandRecord.keySequence);
		}
		
		update();
	}

	private void modifiedComboKeySequence() {
		selectedComboKeySequence();
	}

	private void selectedComboKeySequence() {
		KeySequence keySequence = getKeySequence();
		String scopeId = getScopeId();
		String keyConfigurationId = getKeyConfigurationId();
		selectTableCommand(scopeId, keyConfigurationId, keySequence);						
		keySequenceRecords.clear();
		buildKeySequenceRecords(tree, keySequence, keySequenceRecords);
		buildTableKeySequence();	
		selectTableKeySequence(scopeId, keyConfigurationId);		
		update();
	}

	private void selectedComboScope() {
		KeySequence keySequence = getKeySequence();
		String scopeId = getScopeId();
		String keyConfigurationId = getKeyConfigurationId();
		selectTableCommand(scopeId, keyConfigurationId, keySequence);
		selectTableKeySequence(scopeId, keyConfigurationId);
		update();
	}

	private void selectedComboKeyConfiguration() {
		KeySequence keySequence = getKeySequence();
		String scopeId = getScopeId();
		String keyConfigurationId = getKeyConfigurationId();
		selectTableCommand(scopeId, keyConfigurationId, keySequence);
		selectTableKeySequence(scopeId, keyConfigurationId);
		update();
	}

	private void selectedButtonChange() {
		KeySequence keySequence = getKeySequence();
		boolean validKeySequence = keySequence != null && !keySequence.getKeyStrokes().isEmpty();
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
				set(tree, KeyBinding.create(commandId, keyConfigurationId, keySequence, null, 0, scopeId), true);			 
			else {
				if (!commandRecord.customSet.isEmpty())
					clear(tree, keySequence, scopeId, keyConfigurationId);
				else
					set(tree, KeyBinding.create(null, keyConfigurationId, keySequence, null, 0, scopeId), true);			 
			}

			commandRecords.clear();
			buildCommandRecords(tree, commandId, commandRecords);
			buildTableCommand();
			selectTableCommand(scopeId, keyConfigurationId, keySequence);							
			keySequenceRecords.clear();
			buildKeySequenceRecords(tree, keySequence, keySequenceRecords);
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

		KeySequence keySequence = getKeySequence();
		boolean validKeySequence = keySequence != null && !keySequence.getKeyStrokes().isEmpty();
		String scopeId = getScopeId();
		boolean validScopeId = scopeId != null && scopesById.get(scopeId) != null;	
		String keyConfigurationId = getKeyConfigurationId();
		boolean validKeyConfigurationId = keyConfigurationId != null && keyConfigurationsById.get(keyConfigurationId) != null;

		labelKeySequencesForCommand.setEnabled(commandSelected);
		tableKeySequencesForCommand.setEnabled(commandSelected);
		labelKeySequence.setEnabled(commandSelected);		
		comboKeySequence.setEnabled(commandSelected);
		labelScope.setEnabled(commandSelected);		
		comboScope.setEnabled(commandSelected);
		labelKeyConfiguration.setEnabled(commandSelected);	
		comboKeyConfiguration.setEnabled(commandSelected);	
		buttonChange.setEnabled(commandSelected && validKeySequence && validScopeId && validKeyConfigurationId);		
		labelCommandsForKeySequence.setEnabled(validKeySequence);		
		tableCommandsForKeySequence.setEnabled(validKeySequence);		

		if (commandSelected) {
			String text = MessageFormat.format(Util.getString(resourceBundle, "labelKeySequencesForCommand.selection"), new Object[] { '\'' + command.getName() + '\'' }); //$NON-NLS-1$
			labelKeySequencesForCommand.setText(text);		
		} else 
			labelKeySequencesForCommand.setText(Util.getString(resourceBundle, "labelKeySequencesForCommand.noSelection")); //$NON-NLS-1$
		
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
			String text = MessageFormat.format(Util.getString(resourceBundle, "labelCommandsForKeySequence.selection"), new Object[] { '\''+ keySequence.toString() + '\''}); //$NON-NLS-1$
			labelCommandsForKeySequence.setText(text);
		} else 
			labelCommandsForKeySequence.setText(Util.getString(resourceBundle, "labelCommandsForKeySequence.noSelection")); //$NON-NLS-1$
	}

	private void buildCommandRecords(SortedMap tree, String commandId, List commandRecords) {
		if (commandRecords != null) {
			commandRecords.clear();
				
			if (tree != null) {
				Iterator iterator = tree.entrySet().iterator();
					
				while (iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					KeySequence keySequence = (KeySequence) entry.getKey();					
					Map scopeMap = (Map) entry.getValue();						
		
					if (scopeMap != null) {
						Iterator iterator2 = scopeMap.entrySet().iterator();
						
						while (iterator2.hasNext()) {
							Map.Entry entry2 = (Map.Entry) iterator2.next();
							String scopeId = (String) entry2.getKey();										
							Map keyConfigurationMap = (Map) entry2.getValue();						
							Iterator iterator3 = keyConfigurationMap.entrySet().iterator();
										
							while (iterator3.hasNext()) {
								Map.Entry entry3 = (Map.Entry) iterator3.next();
								String keyConfigurationId = (String) entry3.getKey();					
								Map pluginMap = (Map) entry3.getValue();													
								Set customSet = new HashSet();
								Set defaultSet = new HashSet();						
								buildPluginSets(pluginMap, customSet, defaultSet);

								if (customSet.contains(commandId) || defaultSet.contains(commandId)) {
									CommandRecord commandRecord = new CommandRecord();
									commandRecord.commandId = commandId;
									commandRecord.keySequence = keySequence;
									commandRecord.scopeId = scopeId;
									commandRecord.keyConfigurationId = keyConfigurationId;
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
	
	private void buildKeySequenceRecords(SortedMap tree, KeySequence keySequence, List keySequenceRecords) {
		if (keySequenceRecords != null) {
			keySequenceRecords.clear();
			
			if (tree != null && keySequence != null) {
				Map scopeMap = (Map) tree.get(keySequence);
			
				if (scopeMap != null) {
					Iterator iterator = scopeMap.entrySet().iterator();
			
					while (iterator.hasNext()) {
						Map.Entry entry = (Map.Entry) iterator.next();
						String scopeId2 = (String) entry.getKey();					
						Map keyConfigurationMap = (Map) entry.getValue();						
						Iterator iterator2 = keyConfigurationMap.entrySet().iterator();
							
						while (iterator2.hasNext()) {
							Map.Entry entry2 = (Map.Entry) iterator2.next();
							String keyConfigurationId2 = (String) entry2.getKey();					
							Map pluginMap = (Map) entry2.getValue();			
							KeySequenceRecord keySequenceRecord = new KeySequenceRecord();
							keySequenceRecord.scopeId = scopeId2;
							keySequenceRecord.keyConfigurationId = keyConfigurationId2;							
							keySequenceRecord.customSet = new HashSet();
							keySequenceRecord.defaultSet = new HashSet();						
							buildPluginSets(pluginMap, keySequenceRecord.customSet, keySequenceRecord.defaultSet);			
							keySequenceRecord.calculate();
							keySequenceRecords.add(keySequenceRecord);
						}												
					}	
				}								
			}			
		}
	}

	private void buildPluginSets(Map pluginMap, Set customSet, Set defaultSet) {
		Iterator iterator = pluginMap.entrySet().iterator(); 

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String pluginId = (String) entry.getKey();
			Map commandMap = (Map) entry.getValue();
			Iterator iterator2 = commandMap.keySet().iterator();
	
			while (iterator2.hasNext()) {
				String commandId = (String) iterator2.next();
		
				if (pluginId == null)
					customSet.add(commandId);
				else 
					defaultSet.add(commandId);									
			}
		}
	}

	private void buildTableCommand() {
		tableKeySequencesForCommand.removeAll();

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
				if (defaultSet.contains(commandRecord.commandId)) {												
					//commandId = commandRecord.commandId;
					commandConflict = commandRecord.defaultConflict;					
				}
			} else {
				if (defaultSet.isEmpty()) {									
					if (customSet.contains(commandRecord.commandId)) {													
						difference = DIFFERENCE_ADD;
						//commandId = commandRecord.commandId;
						commandConflict = commandRecord.customConflict;
					}
				} else {
					if (customSet.contains(commandRecord.commandId)) {
						difference = DIFFERENCE_CHANGE;
						//commandId = commandRecord.commandId;
						commandConflict = commandRecord.customConflict;		
						alternateCommandId = commandRecord.defaultCommandId;
						alternateCommandConflict = commandRecord.defaultConflict;
					} else {
						if (defaultSet.contains(commandRecord.commandId)) {	
							difference = DIFFERENCE_MINUS;
							//commandId = commandRecord.commandId;
							commandConflict = commandRecord.defaultConflict;		
							alternateCommandId = commandRecord.customCommandId;
							alternateCommandConflict = commandRecord.customConflict;
						}
					}
				}								
			}

			TableItem tableItem = new TableItem(tableKeySequencesForCommand, SWT.NULL);					

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
					break;				
			}

			Scope scope = (Scope) scopesById.get(commandRecord.scopeId);
			tableItem.setText(1, scope != null ? scope.getName() : "[" + commandRecord.scopeId + "]");
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(commandRecord.keyConfigurationId);			
			tableItem.setText(2, keyConfiguration != null ? keyConfiguration.getName() : "[" + commandRecord.keyConfigurationId + "]");
			boolean conflict = commandConflict || alternateCommandConflict;
			StringBuffer stringBuffer = new StringBuffer();

			if (commandRecord.keySequence != null)
				stringBuffer.append(commandRecord.keySequence.toString());

			if (commandConflict)
				stringBuffer.append(" " + COMMAND_CONFLICT);

			if (difference == DIFFERENCE_CHANGE) {
				stringBuffer.append(" (was: ");
				String alternateCommandName = null;
				
				if (alternateCommandId == null) 
					alternateCommandName = COMMAND_UNDEFINED;
				else {
					Command command = (Command) commandsById.get(alternateCommandId);
					
					if (command != null)
						alternateCommandName = command.getName();
					else
						alternateCommandName = "[" + alternateCommandId + "]";
				}
								
				stringBuffer.append(alternateCommandName);

				if (alternateCommandConflict)
					stringBuffer.append(" " + COMMAND_CONFLICT);

				stringBuffer.append(')');
			} else if (difference == DIFFERENCE_MINUS) {
				stringBuffer.append(" (now: ");
				
				String alternateCommandName = null;
				
				if (alternateCommandId == null) 
					alternateCommandName = COMMAND_UNDEFINED;
				else {
					Command command = (Command) commandsById.get(alternateCommandId);
					
					if (command != null)
						alternateCommandName = command.getName();
					else
						alternateCommandName = "[" + alternateCommandId + "]";
				}
								
				stringBuffer.append(alternateCommandName);
				
				if (alternateCommandConflict)
					stringBuffer.append(" " + COMMAND_CONFLICT);

				stringBuffer.append(')');
			}

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
		tableCommandsForKeySequence.removeAll();
	
		for (int i = 0; i < keySequenceRecords.size(); i++) {
			KeySequenceRecord keySequenceRecord = (KeySequenceRecord) keySequenceRecords.get(i);
			int difference = DIFFERENCE_NONE;
			String commandId = null;
			boolean commandConflict = false;
			String alternateCommandId = null;
			boolean alternateCommandConflict = false;

			if (keySequenceRecord.customSet.isEmpty()) {
				commandId = keySequenceRecord.defaultCommandId;															
				commandConflict = keySequenceRecord.defaultConflict;
			} else {
				commandId = keySequenceRecord.customCommandId;															
				commandConflict = keySequenceRecord.customConflict;						

				if (keySequenceRecord.defaultSet.isEmpty())
					difference = DIFFERENCE_ADD;
				else {
					difference = DIFFERENCE_CHANGE;									
					alternateCommandId = keySequenceRecord.defaultCommandId;
					alternateCommandConflict = keySequenceRecord.defaultConflict;																		
				}
			}

			TableItem tableItem = new TableItem(tableCommandsForKeySequence, SWT.NULL);					

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
					break;				
			}

			Scope scope = (Scope) scopesById.get(keySequenceRecord.scopeId);
			tableItem.setText(1, scope != null ? scope.getName() : "[" + keySequenceRecord.scopeId + "]");
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationsById.get(keySequenceRecord.keyConfigurationId);			
			tableItem.setText(2, keyConfiguration != null ? keyConfiguration.getName() : "[" + keySequenceRecord.keyConfigurationId + "]");
			boolean conflict = commandConflict || alternateCommandConflict;
			StringBuffer stringBuffer = new StringBuffer();
			String commandName = null;
					
			if (commandId == null) 
				commandName = COMMAND_UNDEFINED;
			else {
				Command command = (Command) commandsById.get(commandId);
						
				if (command != null)
					commandName = command.getName();
				else
					commandName = "[" + commandId + "]";
			}
			
			stringBuffer.append(commandName);

			if (commandConflict)
				stringBuffer.append(" " + COMMAND_CONFLICT);

			if (difference == DIFFERENCE_CHANGE) {
				stringBuffer.append(" (was: ");
				String alternateCommandName = null;
					
				if (alternateCommandId == null) 
					alternateCommandName = COMMAND_UNDEFINED;
				else {
					Command command = (Command) commandsById.get(alternateCommandId);
						
					if (command != null)
						alternateCommandName = command.getName();
					else
						alternateCommandName = "[" + alternateCommandId + "]";
				}
									
				stringBuffer.append(alternateCommandName);
	
				if (alternateCommandConflict)
					stringBuffer.append(" " + COMMAND_CONFLICT);
	
				stringBuffer.append(')');
			}
	
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

	private void selectTableCommand(String scopeId, String keyConfigurationId, KeySequence keySequence) {	
		int selection = -1;
		
		for (int i = 0; i < commandRecords.size(); i++) {
			CommandRecord commandRecord = (CommandRecord) commandRecords.get(i);			
			
			if (Util.equals(scopeId, commandRecord.scopeId) && Util.equals(keyConfigurationId, commandRecord.keyConfigurationId) && Util.equals(keySequence, commandRecord.keySequence)) {
				selection = i;
				break;			
			}			
		}

		if (tableKeySequencesForCommand.getSelectionCount() > 1)
			tableKeySequencesForCommand.deselectAll();

		if (selection != tableKeySequencesForCommand.getSelectionIndex()) {
			if (selection == -1 || selection >= tableKeySequencesForCommand.getItemCount())
				tableKeySequencesForCommand.deselectAll();
			else
				tableKeySequencesForCommand.select(selection);
		}
	}

	private void selectTableKeySequence(String scopeId, String keyConfigurationId) {		
		int selection = -1;
		
		for (int i = 0; i < keySequenceRecords.size(); i++) {
			KeySequenceRecord keySequenceRecord = (KeySequenceRecord) keySequenceRecords.get(i);			
			
			if (Util.equals(scopeId, keySequenceRecord.scopeId) && Util.equals(keyConfigurationId, keySequenceRecord.keyConfigurationId)) {
				selection = i;
				break;			
			}			
		}

		if (tableCommandsForKeySequence.getSelectionCount() > 1)
			tableCommandsForKeySequence.deselectAll();

		if (selection != tableCommandsForKeySequence.getSelectionIndex()) {
			if (selection == -1 || selection >= tableCommandsForKeySequence.getItemCount())
				tableCommandsForKeySequence.deselectAll();
			else
				tableCommandsForKeySequence.select(selection);
		}
	}

	private void clear(SortedMap tree, KeySequence keySequence, String scope, String keyConfiguration) {			
		Map scopeMap = (Map) tree.get(keySequence);
		
		if (scopeMap != null) {
			Map keyConfigurationMap = (Map) scopeMap.get(scope);
		
			if (keyConfigurationMap != null) {
				Map pluginMap = (Map) keyConfigurationMap.get(keyConfiguration);
	
				if (pluginMap != null) {
					pluginMap.remove(null);
					
					if (pluginMap.isEmpty()) {
						keyConfigurationMap.remove(keyConfiguration);
						
						if (keyConfigurationMap.isEmpty()) {
							scopeMap.remove(scope);	

							if (scopeMap.isEmpty()) {
								tree.remove(keySequence);	
							}							
						}	
					}	
				}	
			}
		}
	}

	private void set(SortedMap tree, KeyBinding binding, boolean consolidate) {			
		Map scopeMap = (Map) tree.get(binding.getKeySequence());
		
		if (scopeMap == null) {
			scopeMap = new TreeMap();	
			tree.put(binding.getKeySequence(), scopeMap);
		}

		Map keyConfigurationMap = (Map) scopeMap.get(binding.getScope());
		
		if (keyConfigurationMap == null) {
			keyConfigurationMap = new TreeMap();	
			scopeMap.put(binding.getScope(), keyConfigurationMap);
		}
		
		Map pluginMap = (Map) keyConfigurationMap.get(binding.getKeyConfiguration());
		
		if (pluginMap == null) {
			pluginMap = new HashMap();	
			keyConfigurationMap.put(binding.getKeyConfiguration(), pluginMap);
		}

		Map commandMap = consolidate ? null : (Map) pluginMap.get(binding.getPlugin());
		
		if (commandMap == null) {
			commandMap = new HashMap();	
			pluginMap.put(binding.getPlugin(), commandMap);
		}

		Set bindingSet = (Set) commandMap.get(binding.getCommand());
		
		if (bindingSet == null) {
			bindingSet = new TreeSet();
			commandMap.put(binding.getCommand(), bindingSet);	
		}

		if (consolidate)
			bindingSet.clear();
		
		bindingSet.add(binding);
	}

	private SortedSet solve(SortedMap tree) {
		SortedSet bindingSet = new TreeSet();
		Iterator iterator = tree.values().iterator();
		
		while (iterator.hasNext()) {
			Map scopeMap = (Map) iterator.next();
			Iterator iterator2 = scopeMap.values().iterator();
			
			while (iterator2.hasNext()) {
				Map keyConfigurationMap = (Map) iterator2.next();
				Iterator iterator3 = keyConfigurationMap.values().iterator();
				
				while (iterator3.hasNext()) {
					Map pluginMap = (Map) iterator3.next();
					Map commandMap = (Map) pluginMap.get(null);
					
					if (commandMap != null) {
						Iterator iterator4 = commandMap.values().iterator();
						
						while (iterator4.hasNext())
							bindingSet.addAll((Set) iterator4.next());
					}
				}
			}		
		}
		
		return bindingSet;
	}

	private CommandRecord getSelectedCommandRecord() {		
		int selection = tableKeySequencesForCommand.getSelectionIndex();
		
		if (selection >= 0 && selection < commandRecords.size() && tableKeySequencesForCommand.getSelectionCount() == 1)
			return (CommandRecord) commandRecords.get(selection);
		else
			return null;
	}

	private KeySequenceRecord getSelectedKeySequenceRecord() {		
		int selection = tableCommandsForKeySequence.getSelectionIndex();
		
		if (selection >= 0 && selection < keySequenceRecords.size() && tableCommandsForKeySequence.getSelectionCount() == 1)
			return (KeySequenceRecord) keySequenceRecords.get(selection);
		else
			return null;
	}

	private KeySequence getKeySequence() {
		KeySequence keySequence = null;
		String name = comboKeySequence.getText();		
		keySequence = (KeySequence) keySequencesByName.get(name);
			
		if (keySequence == null)
			keySequence = KeySequence.parse(name);

		return keySequence;
	}

	private void setKeySequence(KeySequence keySequence) {
		comboKeySequence.setText(keySequence != null ? keySequence.toString() : ""); //$NON-NLS-1$
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
		int selection = comboKeyConfiguration.getSelectionIndex();
		List keyConfigurations = new ArrayList(keyConfigurationsByName.values());
		
		if (selection >= 0 && selection < keyConfigurations.size()) {
			KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurations.get(selection);
			return keyConfiguration.getId();				
		}
		
		return null;
	}

	private void setKeyConfigurationId(String keyConfigurationId) {				
		comboKeyConfiguration.clearSelection();
		comboKeyConfiguration.deselectAll();
		
		if (keyConfigurationId != null) {
			List keyConfigurations = new ArrayList(keyConfigurationsByName.values());
				
			for (int i = 0; i < keyConfigurations.size(); i++) {
				KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurations.get(i);		
				
				if (keyConfiguration.getId().equals(keyConfigurationId)) {
					comboKeyConfiguration.select(i);
					break;		
				}
			}
		}
	}
	
	private String quote(String string) {
		return string != null ? '\'' + string + '\'' : "\'\'"; //$NON-NLS-1$	
	}
}
