/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.io.IOException;
import java.text.Collator;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.commands.registry.ActiveConfiguration;
import org.eclipse.ui.internal.commands.registry.Category;
import org.eclipse.ui.internal.commands.registry.Command;
import org.eclipse.ui.internal.commands.registry.Configuration;
import org.eclipse.ui.internal.commands.registry.Context;
import org.eclipse.ui.internal.commands.registry.CoreRegistry;
import org.eclipse.ui.internal.commands.registry.IMutableRegistry;
import org.eclipse.ui.internal.commands.registry.IRegistry;
import org.eclipse.ui.internal.commands.registry.LocalRegistry;
import org.eclipse.ui.internal.commands.registry.PreferenceRegistry;
import org.eclipse.ui.internal.commands.registry.SequenceBinding;
import org.eclipse.ui.internal.commands.util.GestureSupport;
import org.eclipse.ui.internal.commands.util.Sequence;
import org.eclipse.ui.internal.commands.util.Util;

public class GesturePreferencePage extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {

	private final static ResourceBundle resourceBundle = ResourceBundle.getBundle(GesturePreferencePage.class.getName());

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
	private final static RGB RGB_CONFLICT_MINUS = new RGB(255, 160, 160);
	private final static RGB RGB_MINUS =	new RGB(160, 160, 160);
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
			List commands = new ArrayList(GesturePreferencePage.this.commands);
			Collections.sort(commands, Command.nameComparator());

			if (parentElement instanceof Category) {
				Category category = (Category) parentElement;

				for (int i = 0; i < commands.size(); i++) {
					Command command = (Command) commands.get(i);
							
					if (category.getId().equals(command.getCategory()))
						children.add(command);											
				}
			} else if (parentElement == null) {
				List categories = new ArrayList(GesturePreferencePage.this.categories);
				Collections.sort(categories, Category.nameComparator());
				children.addAll(categories);
	
				for (int i = 0; i < commands.size(); i++) {
					Command command = (Command) commands.get(i);
							
					if (command.getCategory() == null)
						children.add(command);										
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
	private Label labelName;
	private Text textName;
	private Label labelDescription;
	private Text textDescription; 
	private Label labelSequencesForCommand;
	private Table tableSequencesForCommand;
	//private TableViewer ;	
	private Label labelSequence;
	private Combo comboSequence;
	private Label labelScope; 
	private Combo comboScope;
	private Label labelConfiguration; 
	private Combo comboConfiguration;
	private Button buttonAdd;
	private Button buttonRemove;
	private Button buttonRestore;
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

	private List coreActiveGestureConfigurations;
	private List coreGestureBindings;
	private List coreGestureConfigurations;

	private List localActiveGestureConfigurations;
	private List localGestureBindings;
	private List localGestureConfigurations;

	private List preferenceActiveGestureConfigurations;
	private List preferenceGestureBindings;
	private List preferenceGestureConfigurations;

	private ActiveConfiguration activeGestureConfiguration;	
	private List activeGestureConfigurations;
	private List gestureConfigurations;
	private SortedMap gestureConfigurationsById;
	private SortedMap gestureConfigurationsByName;	
	private SortedMap tree;
	private List commandRecords = new ArrayList();	
	private List gestureSequenceRecords = new ArrayList();
	private SortedMap gestureSequencesByName;

	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		IMutableRegistry preferenceRegistry = PreferenceRegistry.getInstance();

		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
		}
	
		preferenceActiveGestureConfigurations = new ArrayList(preferenceRegistry.getActiveGestureConfigurations());
		preferenceGestureBindings = new ArrayList(preferenceRegistry.getGestureBindings());
		Manager.validateSequenceBindings(preferenceGestureBindings);		
		preferenceGestureConfigurations = new ArrayList(preferenceRegistry.getGestureConfigurations());
	}

	public boolean performOk() {
		copyFromUI();
		IMutableRegistry preferenceRegistry = PreferenceRegistry.getInstance();
		preferenceRegistry.setActiveGestureConfigurations(preferenceActiveGestureConfigurations);
		preferenceRegistry.setGestureBindings(preferenceGestureBindings);
		preferenceRegistry.setGestureConfigurations(preferenceGestureConfigurations);
		
		try {
			preferenceRegistry.save();
		} catch (IOException eIO) {
		}

		Manager.getInstance().reset();

		if (workbench instanceof Workbench)
			((Workbench) workbench).updateActiveGestureBindingService();

		return super.performOk();
	}

	public void setVisible(boolean visible) {
		if (visible == true) {
			IRegistry coreRegistry = CoreRegistry.getInstance();
			IMutableRegistry localRegistry = LocalRegistry.getInstance();
			IMutableRegistry preferenceRegistry = PreferenceRegistry.getInstance();
			
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
			scopes.addAll(coreRegistry.getContexts());
			scopes.addAll(localRegistry.getContexts());
			scopes.addAll(preferenceRegistry.getContexts());
	
			if (!Util.equals(scopes, this.scopes)) {
				this.scopes = Collections.unmodifiableList(scopes);
				scopesById = Collections.unmodifiableSortedMap(Context.sortedMapById(this.scopes));
				scopesByName = Collections.unmodifiableSortedMap(Context.sortedMapByName(this.scopes));							
				List names = new ArrayList();
				Iterator iterator = this.scopes.iterator();
				
				while (iterator.hasNext()) {
					Context scope = (Context) iterator.next();
					
					if (scope != null) {
						String name = scope.getName();
						String parent = scope.getParent();
					
						if (parent != null) {
							scope = (Context) scopesById.get(parent);
						
							if (scope != null)
								name = MessageFormat.format(Util.getString(resourceBundle, "extends"), new Object[] { name, scope.getName() }); //$NON-NLS-1$
						}

						names.add(name);
					}
				}
				
				Collections.sort(names, Collator.getInstance());								
				comboScope.setItems((String[]) names.toArray(new String[names.size()]));
			}		

			coreActiveGestureConfigurations = new ArrayList(coreRegistry.getActiveGestureConfigurations());
			coreGestureBindings = new ArrayList(coreRegistry.getGestureBindings());
			Manager.validateSequenceBindings(coreGestureBindings);
			coreGestureConfigurations = new ArrayList(coreRegistry.getGestureConfigurations());

			localActiveGestureConfigurations = new ArrayList(localRegistry.getActiveGestureConfigurations());
			localGestureBindings = new ArrayList(localRegistry.getGestureBindings());
			Manager.validateSequenceBindings(localGestureBindings);
			localGestureConfigurations = new ArrayList(localRegistry.getGestureConfigurations());

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
			preferenceActiveGestureConfigurations = new ArrayList();
			preferenceGestureBindings = new ArrayList();
			preferenceGestureConfigurations = new ArrayList();
			copyToUI();
		}
	}

	private void copyFromUI() {
		activeGestureConfiguration = null; 		
		preferenceActiveGestureConfigurations = new ArrayList();
		String activeGestureConfigurationId = getActiveGestureConfigurationId();		

		if (activeGestureConfigurationId != null) {
			activeGestureConfiguration = ActiveConfiguration.create(null, activeGestureConfigurationId);
			preferenceActiveGestureConfigurations.add(activeGestureConfiguration);
		}

		preferenceGestureBindings = new ArrayList(solve(tree));
	}

	private void copyToUI() {	
		List activeGestureConfigurations = new ArrayList();
		activeGestureConfigurations.addAll(coreActiveGestureConfigurations);
		activeGestureConfigurations.addAll(localActiveGestureConfigurations);
		activeGestureConfigurations.addAll(preferenceActiveGestureConfigurations);

		if (!Util.equals(activeGestureConfigurations, this.activeGestureConfigurations)) {
			this.activeGestureConfigurations = Collections.unmodifiableList(activeGestureConfigurations);
			
			if (this.activeGestureConfigurations.size() >= 1)
				activeGestureConfiguration = (ActiveConfiguration) this.activeGestureConfigurations.get(this.activeGestureConfigurations.size() - 1);
			else
				activeGestureConfiguration = null;				
		}
		
		List gestureConfigurations = new ArrayList();
		gestureConfigurations.addAll(coreGestureConfigurations);
		gestureConfigurations.addAll(localGestureConfigurations);
		gestureConfigurations.addAll(preferenceGestureConfigurations);

		if (!Util.equals(gestureConfigurations, this.gestureConfigurations)) {
			this.gestureConfigurations = Collections.unmodifiableList(gestureConfigurations);
			gestureConfigurationsById = Collections.unmodifiableSortedMap(Configuration.sortedMapById(this.gestureConfigurations));
			gestureConfigurationsByName = Collections.unmodifiableSortedMap(Configuration.sortedMapByName(this.gestureConfigurations));
			List names = new ArrayList();
			Iterator iterator = this.gestureConfigurations.iterator();
				
			while (iterator.hasNext()) {
				Configuration gestureConfiguration = (Configuration) iterator.next();
					
				if (gestureConfiguration != null) {
					String name = gestureConfiguration.getName();
					String parent = gestureConfiguration.getParent();
					
					if (parent != null) {
						gestureConfiguration = (Configuration) gestureConfigurationsById.get(parent);
						
						if (gestureConfiguration != null)
							name = MessageFormat.format(Util.getString(resourceBundle, "extends"), new Object[] { name, gestureConfiguration.getName() }); //$NON-NLS-1$
					}	

					names.add(name);
				}
			}

			Collections.sort(names, Collator.getInstance());								
			comboActiveConfiguration.setItems((String[]) names.toArray(new String[names.size()]));
			comboConfiguration.setItems((String[]) names.toArray(new String[names.size()]));
		}		

		setActiveGestureConfigurationId(activeGestureConfiguration != null ? activeGestureConfiguration.getValue() : null);

		SortedSet gestureBindingSet = new TreeSet();
		gestureBindingSet.addAll(coreGestureBindings);
		gestureBindingSet.addAll(localGestureBindings);
		gestureBindingSet.addAll(preferenceGestureBindings);

		tree = build(gestureBindingSet);	

		gestureSequencesByName = new TreeMap();
		Iterator iterator = tree.keySet().iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (object instanceof Sequence) {
				Sequence gestureSequence = (Sequence) object;
				String name = GestureSupport.formatSequence(gestureSequence, false);
				gestureSequencesByName.put(name, gestureSequence);
			}
		}		

		Set gestureSequenceNameSet = gestureSequencesByName.keySet();
		comboSequence.setItems((String[]) gestureSequenceNameSet.toArray(new String[gestureSequenceNameSet.size()]));
		selectedTreeViewerCommands();
	}

	private Control createUI(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		Composite compositeActiveGestureConfiguration = new Composite(composite, SWT.NULL);
		compositeActiveGestureConfiguration.setFont(composite.getFont());
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 5;
		compositeActiveGestureConfiguration.setLayout(gridLayout);

		labelActiveConfiguration = new Label(compositeActiveGestureConfiguration, SWT.LEFT);
		labelActiveConfiguration.setFont(compositeActiveGestureConfiguration.getFont());
		labelActiveConfiguration.setText(Util.getString(resourceBundle, "labelActiveConfiguration")); //$NON-NLS-1$

		comboActiveConfiguration = new Combo(compositeActiveGestureConfiguration, SWT.READ_ONLY);
		comboActiveConfiguration.setFont(compositeActiveGestureConfiguration.getFont());
		GridData gridData = new GridData();
		gridData.widthHint = 200;
		comboActiveConfiguration.setLayoutData(gridData);

		//buttonNew = new Button(compositeActiveGestureConfiguration, SWT.CENTER | SWT.PUSH);
		//buttonNew.setFont(compositeActiveGestureConfiguration.getFont());
		//gridData = new GridData();
		//gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		//int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		//buttonNew.setText(Util.getString(resourceBundle, "buttonNew")); //$NON-NLS-1$
		//gridData.widthHint = Math.max(widthHint, buttonNew.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		//buttonNew.setLayoutData(gridData);
		//buttonNew.setVisible(false);	

		//buttonRename = new Button(compositeActiveGestureConfiguration, SWT.CENTER | SWT.PUSH);
		//buttonRename.setFont(compositeActiveGestureConfiguration.getFont());
		//gridData = new GridData();
		//gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		//widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		//buttonRename.setText(Util.getString(resourceBundle, "buttonRename")); //$NON-NLS-1$
		//gridData.widthHint = Math.max(widthHint, buttonRename.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		//buttonRename.setLayoutData(gridData);		
		//buttonRename.setVisible(false);	
		
		//buttonDelete = new Button(compositeActiveGestureConfiguration, SWT.CENTER | SWT.PUSH);
		//buttonDelete.setFont(compositeActiveGestureConfiguration.getFont());
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

		Composite compositeAssignmentTitle = new Composite(compositeAssignmentRight, SWT.NULL);
		compositeAssignmentTitle.setFont(compositeAssignmentRight.getFont());
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;		
		gridLayout.numColumns = 2;
		compositeAssignmentTitle.setLayout(gridLayout);
		compositeAssignmentTitle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		labelName = new Label(compositeAssignmentTitle, SWT.LEFT);
		labelName.setFont(compositeAssignmentTitle.getFont());
		labelName.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		labelName.setText(Util.getString(resourceBundle, "labelName")); //$NON-NLS-1$

		textName = new Text(compositeAssignmentTitle, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		textName.setFont(compositeAssignmentTitle.getFont());
		textName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		labelDescription = new Label(compositeAssignmentTitle, SWT.LEFT);
		labelDescription.setFont(compositeAssignmentTitle.getFont());
		labelDescription.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		labelDescription.setText(Util.getString(resourceBundle, "labelDescription")); //$NON-NLS-1$

		textDescription = new Text(compositeAssignmentTitle, SWT.BORDER | SWT.LEFT | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		textDescription.setFont(compositeAssignmentTitle.getFont());
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 30;
		textDescription.setLayoutData(gridData);

		labelSequencesForCommand = new Label(compositeAssignmentRight, SWT.LEFT);
		labelSequencesForCommand.setFont(compositeAssignmentRight.getFont());
		labelSequencesForCommand.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelSequencesForCommand.setText(Util.getString(resourceBundle, "labelSequencesForCommand")); //$NON-NLS-1$

		tableSequencesForCommand = new Table(compositeAssignmentRight, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableSequencesForCommand.setFont(compositeAssignmentRight.getFont());
		tableSequencesForCommand.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 90;
		tableSequencesForCommand.setLayoutData(gridData);

		int width = 0;
		TableColumn tableColumn = new TableColumn(tableSequencesForCommand, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(Util.ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);
		width += tableColumn.getWidth();

		tableColumn = new TableColumn(tableSequencesForCommand, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnScope")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth(tableColumn.getWidth() + 40);
		width += tableColumn.getWidth();

		tableColumn = new TableColumn(tableSequencesForCommand, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnConfiguration")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth(tableColumn.getWidth() + 40);
		width += tableColumn.getWidth();

		tableColumn = new TableColumn(tableSequencesForCommand, SWT.NULL, 3);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnSequence")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth(Math.max(220, Math.max(440 - width, tableColumn.getWidth() + 20)));	
		
		//tableViewerGestureSequencesForCommand = new TableViewer(tableGestureSequencesForCommand);
		//tableViewerGestureSequencesForCommand.setContentProvider(new TableViewerGestureSequencesForCommandContentProvider());
		//tableViewerGestureSequencesForCommand.setLabelProvider(new TableViewerGestureSequencesForCommandLabelProvider());

		Composite compositeAssignmentChange = new Composite(compositeAssignmentRight, SWT.NULL);
		compositeAssignmentChange.setFont(compositeAssignmentRight.getFont());
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 10;
		gridLayout.marginWidth = 10;		
		gridLayout.numColumns = 2;
		compositeAssignmentChange.setLayout(gridLayout);

		labelScope = new Label(compositeAssignmentChange, SWT.LEFT);
		labelScope.setFont(compositeAssignmentChange.getFont());
		labelScope.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		labelScope.setText(Util.getString(resourceBundle, "labelScope")); //$NON-NLS-1$

		comboScope = new Combo(compositeAssignmentChange, SWT.READ_ONLY);
		comboScope.setFont(compositeAssignmentChange.getFont());
		gridData = new GridData();
		gridData.widthHint = 200;
		comboScope.setLayoutData(gridData);
		
		labelConfiguration = new Label(compositeAssignmentChange, SWT.LEFT);
		labelConfiguration.setFont(compositeAssignmentChange.getFont());
		labelConfiguration.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		labelConfiguration.setText(Util.getString(resourceBundle, "labelConfiguration")); //$NON-NLS-1$

		comboConfiguration = new Combo(compositeAssignmentChange, SWT.READ_ONLY);
		comboConfiguration.setFont(compositeAssignmentChange.getFont());
		gridData = new GridData();
		gridData.widthHint = 200;
		comboConfiguration.setLayoutData(gridData);

		labelSequence = new Label(compositeAssignmentChange, SWT.LEFT);
		labelSequence.setFont(compositeAssignmentChange.getFont());
		labelSequence.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		labelSequence.setText(Util.getString(resourceBundle, "labelSequence")); //$NON-NLS-1$

		comboSequence = new Combo(compositeAssignmentChange, SWT.NULL);
		comboSequence.setFont(compositeAssignmentChange.getFont());
		gridData = new GridData();
		gridData.widthHint = 200;
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

		Composite compositeButton = new Composite(compositeAssignmentChange, SWT.NULL);
		compositeButton.setFont(compositeAssignmentChange.getFont());
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;		
		gridLayout.numColumns = 3;
		compositeButton.setLayout(gridLayout);
				
		buttonAdd = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		buttonAdd.setFont(compositeButton.getFont());
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonAdd.setText(Util.getString(resourceBundle, "buttonAdd")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonAdd.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonAdd.setLayoutData(gridData);		

		buttonRemove = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		buttonRemove.setFont(compositeButton.getFont());
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonRemove.setText(Util.getString(resourceBundle, "buttonRemove")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonRemove.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonRemove.setLayoutData(gridData);		

		buttonRestore = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		buttonRestore.setFont(compositeButton.getFont());
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonRestore.setText(Util.getString(resourceBundle, "buttonRestore")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonRestore.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonRestore.setLayoutData(gridData);		
		
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
		tableCommandsForSequence.setLayoutData(gridData);

		width = 0;
		tableColumn = new TableColumn(tableCommandsForSequence, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(Util.ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);
		width += tableColumn.getWidth();
		
		tableColumn = new TableColumn(tableCommandsForSequence, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnScope")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth(tableColumn.getWidth() + 40);
		width += tableColumn.getWidth();

		tableColumn = new TableColumn(tableCommandsForSequence, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnConfiguration")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth(tableColumn.getWidth() + 40);		
		width += tableColumn.getWidth();

		tableColumn = new TableColumn(tableCommandsForSequence, SWT.NULL, 3);
		tableColumn.setResizable(true);
		tableColumn.setText(Util.getString(resourceBundle, "tableColumnCommand")); //$NON-NLS-1$
		tableColumn.pack();
		tableColumn.setWidth(Math.max(220, Math.max(440 - width, tableColumn.getWidth() + 20)));		
		
		//tableViewerCommandsForGestureSequence = new TableViewer(tableCommandsForGestureSequence);
		//tableViewerCommandsForGestureSequence.setContentProvider(new TableViewerCommandsForGestureSequenceContentProvider());
		//tableViewerCommandsForGestureSequence.setLabelProvider(new TableViewerCommandsForGestureSequenceLabelProvider());

		comboActiveConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboActiveGestureConfiguration();
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
				doubleClickedTableGestureSequencesForCommand();	
			}			
		});		

		tableSequencesForCommand.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {			
				selectedTableGestureSequencesForCommand();
			}	
		});
	
		//tableViewerGestureSequencesForCommand.addDoubleClickListener(new IDoubleClickListener() {
		//	public void doubleClick(DoubleClickEvent event) {
		//	}
		//});

		//tableViewerGestureSequencesForCommand.addSelectionChangedListener(new ISelectionChangedListener() {
		//	public void selectionChanged(SelectionChangedEvent event) {
		//	}
		//});

		comboSequence.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent modifyEvent) {
				modifiedComboGestureSequence();
			}	
		});

		comboSequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboGestureSequence();
			}	
		});

		comboScope.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboScope();
			}	
		});
		
		comboConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboGestureConfiguration();
			}	
		});

		buttonAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonChange();
			}	
		});

		buttonRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonChange();
			}	
		});
		
		buttonRestore.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonChange();
			}	
		});

		tableCommandsForSequence.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedTableCommandsForGestureSequence();	
			}			
		});		

		tableCommandsForSequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {			
				selectedTableCommandsForGestureSequence();
			}	
		});

		//tableViewerCommandsForGestureSequence.addDoubleClickListener(new IDoubleClickListener() {
		//	public void doubleClick(DoubleClickEvent event) {
		//	}
		//});

		//tableViewerCommandsForGestureSequence.addSelectionChangedListener(new ISelectionChangedListener() {
		//	public void selectionChanged(SelectionChangedEvent event) {
		//	}
		//});
				
		// TODO: WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_GESTURE_PREFERENCE_PAGE);
		return composite;	
	}

	private void selectedComboActiveGestureConfiguration() {		
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
		setGestureSequence(null);
		// TODO: add 'globalScope' element to commands extension point to remove this.
		setScopeId("org.eclipse.ui.globalScope"); //$NON-NLS-1$
		setGestureConfigurationId(getActiveGestureConfigurationId());				
		Sequence gestureSequence = getGestureSequence();
		String scopeId = getScopeId();
		String gestureConfigurationId = getGestureConfigurationId();
		selectTableCommand(scopeId, gestureConfigurationId, gestureSequence);				
		update();
	}

	private void doubleClickedTableGestureSequencesForCommand() {
	}
		
	private void selectedTableGestureSequencesForCommand() {
		CommandRecord commandRecord = (CommandRecord) getSelectedCommandRecord();
		
		if (commandRecord != null) {
			setScopeId(commandRecord.scope);
			setGestureConfigurationId(commandRecord.configuration);				
			setGestureSequence(commandRecord.sequence);
		}
		
		update();
	}

	private void modifiedComboGestureSequence() {
		selectedComboGestureSequence();
	}

	private void selectedComboGestureSequence() {
		Sequence gestureSequence = getGestureSequence();
		String scopeId = getScopeId();
		String gestureConfigurationId = getGestureConfigurationId();
		selectTableCommand(scopeId, gestureConfigurationId, gestureSequence);						
		gestureSequenceRecords.clear();
		buildSequenceRecords(tree, gestureSequence, gestureSequenceRecords);
		buildTableGestureSequence();	
		selectTableGestureSequence(scopeId, gestureConfigurationId);		
		update();
	}

	private void selectedComboScope() {
		Sequence gestureSequence = getGestureSequence();
		String scopeId = getScopeId();
		String gestureConfigurationId = getGestureConfigurationId();
		selectTableCommand(scopeId, gestureConfigurationId, gestureSequence);
		selectTableGestureSequence(scopeId, gestureConfigurationId);
		update();
	}

	private void selectedComboGestureConfiguration() {
		Sequence gestureSequence = getGestureSequence();
		String scopeId = getScopeId();
		String gestureConfigurationId = getGestureConfigurationId();
		selectTableCommand(scopeId, gestureConfigurationId, gestureSequence);
		selectTableGestureSequence(scopeId, gestureConfigurationId);
		update();
	}

	private void selectedButtonChange() {
		Sequence gestureSequence = getGestureSequence();
		boolean validGestureSequence = gestureSequence != null && Util.validateSequence(gestureSequence);
		String scopeId = getScopeId();
		boolean validScopeId = scopeId != null && scopesById.get(scopeId) != null;	
		String gestureConfigurationId = getGestureConfigurationId();
		boolean validGestureConfigurationId = gestureConfigurationId != null && gestureConfigurationsById.get(gestureConfigurationId) != null;
	
		if (validGestureSequence && validScopeId && validGestureConfigurationId) {	
			String commandId = null;
			ISelection selection = treeViewerCommands.getSelection();
		
			if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
				Object object = ((IStructuredSelection) selection).getFirstElement();
						
				if (object instanceof Command)
					commandId = ((Command) object).getId();
			}

			CommandRecord commandRecord = getSelectedCommandRecord();
		
			if (commandRecord == null)
				set(tree, gestureSequence, scopeId, gestureConfigurationId, commandId);			 
			else {
				if (!commandRecord.customSet.isEmpty())
					clear(tree, gestureSequence, scopeId, gestureConfigurationId);
				else
					set(tree, gestureSequence, scopeId, gestureConfigurationId, null);
			}

			commandRecords.clear();
			buildCommandRecords(tree, commandId, commandRecords);
			buildTableCommand();
			selectTableCommand(scopeId, gestureConfigurationId, gestureSequence);							
			gestureSequenceRecords.clear();
			buildSequenceRecords(tree, gestureSequence, gestureSequenceRecords);
			buildTableGestureSequence();	
			selectTableGestureSequence(scopeId, gestureConfigurationId);
			update();
		}
	}

	private void doubleClickedTableCommandsForGestureSequence() {	
	}

	private void selectedTableCommandsForGestureSequence() {
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

		Sequence gestureSequence = getGestureSequence();
		boolean validGestureSequence = gestureSequence != null && Util.validateSequence(gestureSequence);
		String scopeId = getScopeId();
		boolean validScopeId = scopeId != null && scopesById.get(scopeId) != null;	
		String gestureConfigurationId = getGestureConfigurationId();
		boolean validGestureConfigurationId = gestureConfigurationId != null && gestureConfigurationsById.get(gestureConfigurationId) != null;

		labelName.setEnabled(commandSelected);
		textName.setEnabled(commandSelected);
		labelDescription.setEnabled(commandSelected);
		textDescription.setEnabled(commandSelected);
		labelSequencesForCommand.setEnabled(commandSelected);
		tableSequencesForCommand.setEnabled(commandSelected);
		labelSequence.setEnabled(commandSelected);		
		comboSequence.setEnabled(commandSelected);
		labelScope.setEnabled(commandSelected);		
		comboScope.setEnabled(commandSelected);
		labelConfiguration.setEnabled(commandSelected);	
		comboConfiguration.setEnabled(commandSelected);	
		buttonAdd.setEnabled(false);
		buttonRemove.setEnabled(false);
		buttonRestore.setEnabled(false);
		labelCommandsForSequence.setEnabled(validGestureSequence);		
		tableCommandsForSequence.setEnabled(validGestureSequence);		

		textName.setText(commandSelected ? command.getName() : Util.ZERO_LENGTH_STRING);
		textDescription.setText(commandSelected ? command.getDescription() : Util.ZERO_LENGTH_STRING);
		
		CommandRecord commandRecord = getSelectedCommandRecord();
		
		if (commandRecord == null)
			buttonAdd.setEnabled(commandSelected && validGestureSequence && validScopeId && validGestureConfigurationId);
		else {
			if (!commandRecord.customSet.isEmpty() && !commandRecord.defaultSet.isEmpty()) {
				buttonRestore.setEnabled(commandSelected && validGestureSequence && validScopeId && validGestureConfigurationId);
			} else
				buttonRemove.setEnabled(commandSelected && validGestureSequence && validScopeId && validGestureConfigurationId);
		}

		if (validGestureSequence) {
			String text = MessageFormat.format(Util.getString(resourceBundle, "labelCommandsForSequence.selection"), new Object[] { '\''+ GestureSupport.formatSequence(gestureSequence, false) + '\''}); //$NON-NLS-1$
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

			Context scope = (Context) scopesById.get(commandRecord.scope);
			tableItem.setText(1, scope != null ? scope.getName() : bracket(commandRecord.scope));
			Configuration gestureConfiguration = (Configuration) gestureConfigurationsById.get(commandRecord.configuration);			
			tableItem.setText(2, gestureConfiguration != null ? gestureConfiguration.getName() : bracket(commandRecord.configuration));
			boolean conflict = commandConflict || alternateCommandConflict;
			StringBuffer stringBuffer = new StringBuffer();

			if (commandRecord.sequence != null)
				stringBuffer.append(GestureSupport.formatSequence(commandRecord.sequence, false));

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
	
	private void buildTableGestureSequence() {
		tableCommandsForSequence.removeAll();
	
		for (int i = 0; i < gestureSequenceRecords.size(); i++) {
			SequenceRecord gestureSequenceRecord = (SequenceRecord) gestureSequenceRecords.get(i);
			int difference = DIFFERENCE_NONE;
			String commandId = null;
			boolean commandConflict = false;
			String alternateCommandId = null;
			boolean alternateCommandConflict = false;

			if (gestureSequenceRecord.customSet.isEmpty()) {
				commandId = gestureSequenceRecord.defaultCommand;															
				commandConflict = gestureSequenceRecord.defaultConflict;
			} else {
				commandId = gestureSequenceRecord.customCommand;															
				commandConflict = gestureSequenceRecord.customConflict;						

				if (gestureSequenceRecord.defaultSet.isEmpty())
					difference = DIFFERENCE_ADD;
				else {
					difference = DIFFERENCE_CHANGE;									
					alternateCommandId = gestureSequenceRecord.defaultCommand;
					alternateCommandConflict = gestureSequenceRecord.defaultConflict;																		
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

			Context scope = (Context) scopesById.get(gestureSequenceRecord.scope);
			tableItem.setText(1, scope != null ? scope.getName() : bracket(gestureSequenceRecord.scope));
			Configuration gestureConfiguration = (Configuration) gestureConfigurationsById.get(gestureSequenceRecord.configuration);			
			tableItem.setText(2, gestureConfiguration != null ? gestureConfiguration.getName() : bracket(gestureSequenceRecord.configuration));
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

	private void selectTableCommand(String scopeId, String gestureConfigurationId, Sequence gestureSequence) {	
		int selection = -1;
		
		for (int i = 0; i < commandRecords.size(); i++) {
			CommandRecord commandRecord = (CommandRecord) commandRecords.get(i);			
			
			if (Util.equals(scopeId, commandRecord.scope) && Util.equals(gestureConfigurationId, commandRecord.configuration) && Util.equals(gestureSequence, commandRecord.sequence)) {
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

	private void selectTableGestureSequence(String scopeId, String gestureConfigurationId) {		
		int selection = -1;
		
		for (int i = 0; i < gestureSequenceRecords.size(); i++) {
			SequenceRecord gestureSequenceRecord = (SequenceRecord) gestureSequenceRecords.get(i);			
			
			if (Util.equals(scopeId, gestureSequenceRecord.scope) && Util.equals(gestureConfigurationId, gestureSequenceRecord.configuration)) {
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

	private SequenceRecord getSelectedGestureSequenceRecord() {		
		int selection = tableCommandsForSequence.getSelectionIndex();
		
		if (selection >= 0 && selection < gestureSequenceRecords.size() && tableCommandsForSequence.getSelectionCount() == 1)
			return (SequenceRecord) gestureSequenceRecords.get(selection);
		else
			return null;
	}

	private Sequence getGestureSequence() {
		Sequence gestureSequence = null;
		String name = comboSequence.getText();		
		gestureSequence = (Sequence) gestureSequencesByName.get(name);
			
		if (gestureSequence == null)
			gestureSequence = GestureSupport.parseSequence(name);

		return gestureSequence;
	}

	private void setGestureSequence(Sequence gestureSequence) {
		comboSequence.setText(gestureSequence != null ? GestureSupport.formatSequence(gestureSequence, false) : Util.ZERO_LENGTH_STRING);
	}

	private String getScopeId() {
		int selection = comboScope.getSelectionIndex();
		List scopes = new ArrayList(scopesByName.values());			
		
		if (selection >= 0 && selection < scopes.size()) {
			Context scope = (Context) scopes.get(selection);
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
				Context scope = (Context) scopes.get(i);		
				
				if (scope.getId().equals(scopeId)) {
					comboScope.select(i);
					break;		
				}
			}
		}
	}

	private String getActiveGestureConfigurationId() {
		int selection = comboActiveConfiguration.getSelectionIndex();
		List gestureConfigurations = new ArrayList(gestureConfigurationsByName.values());
		
		if (selection >= 0 && selection < gestureConfigurations.size()) {
			Configuration gestureConfiguration = (Configuration) gestureConfigurations.get(selection);
			return gestureConfiguration.getId();				
		}
		
		return null;
	}

	private void setActiveGestureConfigurationId(String gestureConfigurationId) {				
		comboActiveConfiguration.clearSelection();
		comboActiveConfiguration.deselectAll();
		
		if (gestureConfigurationId != null) {
			List gestureConfigurations = new ArrayList(gestureConfigurationsByName.values());
				
			for (int i = 0; i < gestureConfigurations.size(); i++) {
				Configuration gestureConfiguration = (Configuration) gestureConfigurations.get(i);		
				
				if (gestureConfiguration.getId().equals(gestureConfigurationId)) {
					comboActiveConfiguration.select(i);
					break;		
				}
			}
		}
	}

	private String getGestureConfigurationId() {
		int selection = comboConfiguration.getSelectionIndex();
		List gestureConfigurations = new ArrayList(gestureConfigurationsByName.values());
		
		if (selection >= 0 && selection < gestureConfigurations.size()) {
			Configuration gestureConfiguration = (Configuration) gestureConfigurations.get(selection);
			return gestureConfiguration.getId();				
		}
		
		return null;
	}

	private void setGestureConfigurationId(String gestureConfigurationId) {				
		comboConfiguration.clearSelection();
		comboConfiguration.deselectAll();
		
		if (gestureConfigurationId != null) {
			List gestureConfigurations = new ArrayList(gestureConfigurationsByName.values());
				
			for (int i = 0; i < gestureConfigurations.size(); i++) {
				Configuration gestureConfiguration = (Configuration) gestureConfigurations.get(i);		
				
				if (gestureConfiguration.getId().equals(gestureConfigurationId)) {
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
			String scope = sequenceBinding.getContext();			
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
							sequenceBindingSet.add(SequenceBinding.create(command, configuration, scope, Util.ZERO_LENGTH_STRING, Util.ZERO_LENGTH_STRING, null, 0, sequence));									
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
