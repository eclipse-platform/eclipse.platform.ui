/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.actions.keybindings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class DialogCustomize extends Dialog {

	private static int BUTTON_SET_ID = IDialogConstants.CLIENT_ID + 0;
	private static int DOUBLE_SPACE = 16;
	private static int HALF_SPACE = 4;
	private static int SPACE = 8;	

	class Record {
		
		org.eclipse.ui.internal.actions.Action action;
		KeySequence keySequence;
		Scope scope;
		Configuration configuration;
		String platform;			
		String locale;
	}	

	class ViewContentProvider implements IStructuredContentProvider {
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {
			org.eclipse.ui.internal.actions.Registry actionRegistry = org.eclipse.ui.internal.actions.Registry.getInstance();
			org.eclipse.ui.internal.actions.keybindings.Registry keyBindingRegistry = org.eclipse.ui.internal.actions.keybindings.Registry.getInstance();
			List list = new ArrayList();
			KeyManager keyManager = KeyManager.getInstance();
			Map keySequenceMap = keyManager.getKeyMachine().getKeySequenceMap();
			Iterator iterator = keySequenceMap.entrySet().iterator();
			
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();			
				KeySequence keySequence = (KeySequence) entry.getKey();				
				/*
				Match match = (Match) entry.getValue();
				Record record = new Record();
				record.action = (org.eclipse.ui.internal.actions.Action) actionRegistry.getActionMap().get(match.getBinding().getAction());
				record.keySequence = keySequence;
				Binding binding = match.getBinding();
				String scopeId = binding.getScope();
				String configurationId = binding.getConfiguration();
				record.scope = (Scope) keyBindingRegistry.getScopeMap().get(scopeId);
				record.configuration = (Configuration) keyBindingRegistry.getConfigurationMap().get(configurationId);
				list.add(record);
				*/
			}

			return list.toArray();
		}
	}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public String getColumnText(Object obj, int index) {
			if (obj instanceof Record) {
				Record record = (Record) obj;			
				KeyManager keyManager = KeyManager.getInstance();
				
				switch (index) {
					case 0:	
						return "";
					case 1:
						return record.scope != null ? record.scope.toString() : "?";
					case 2:
						return record.configuration != null ? record.configuration.toString() : "?";
					case 3: 
						return record.action != null ? record.action.toString() : "?";
					case 4:
						return record.platform != null ? record.platform.toString() : "";
					case 5:
						return record.locale != null ? record.locale.toString() : "";
				}					
			}

			return getText(obj);
		}
		
		public Image getColumnImage(Object obj, int index) {
			if (obj instanceof Record) {
				Record record = (Record) obj;			
				KeyManager keyManager = KeyManager.getInstance();
				
				switch (index) {
					case 0:	
						if (record.action == null || record.action.toString().length() == 0 || record.action.toString().charAt(0) == '(')
							return ImageFactory.getImage("plus");
						else if (record.action.toString().charAt(0) == 'P')	
							return ImageFactory.getImage("minus");
						else if (record.action.toString().charAt(0) == 'F')	
							return ImageFactory.getImage("difference");
						else if (record.action.toString().charAt(0) == 'C')	
							return ImageFactory.getImage("attention");
				}					
			}

			return getImage(obj);
		}
	}
	
	class NameSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Record) e1).keySequence.compareTo(((Record) e2).keySequence);	
		}
	}

	private String columnHeaders[] = {
		"",
		"Scope",
		"Configuration",
		"Action"
	};

	private ColumnLayoutData columnLayouts[] = {
		new ColumnPixelData(20, false),
		new ColumnPixelData(100, false),
		new ColumnPixelData(100, false),
		new ColumnPixelData(250, false) 
	};		

	private KeyManager keyManager;
	private KeyMachine keyMachine;	
	private Image imageError;
	private Image imageInfo;
	private Image imageWarning;	
	private Label labelKeySequence;
	private Combo comboKeySequence;
	private Label labelPromptImage;
	private Label labelPromptText;
	private Button buttonDefault;
	private Text textDefault;
	private Button buttonCustom; 
	private Combo comboCustom;
	private Label labelScope; 
	private Combo comboScope;
	private Label labelConfiguration; 
	private Combo comboConfiguration;
	private Button buttonSet;
	private TableViewer viewer;
	private Button buttonOptions;

	public DialogCustomize(Shell parentShell) {
		super(parentShell);
		keyManager = KeyManager.getInstance();
		keyMachine = keyManager.getKeyMachine();		
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.getString("DialogCustomize.Title"));
	}

	protected Control createDialogArea(Composite parent) {
		imageError = getImage(DLG_IMG_MESSAGE_ERROR);
		imageInfo = getImage(DLG_IMG_MESSAGE_INFO);
		imageWarning = getImage(DLG_IMG_MESSAGE_WARNING);
		
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayoutComposite = new GridLayout();
		gridLayoutComposite.marginHeight = SPACE;
		composite.setLayout(gridLayoutComposite);

		Composite compositeKeySequence = new Composite(composite, SWT.NONE);		
		GridLayout gridLayoutCompositeKeySequence = new GridLayout();
		gridLayoutCompositeKeySequence.numColumns = 3;
		gridLayoutCompositeKeySequence.marginWidth = SPACE;	
		gridLayoutCompositeKeySequence.horizontalSpacing = SPACE;
		compositeKeySequence.setLayout(gridLayoutCompositeKeySequence);
		
		labelKeySequence = new Label(compositeKeySequence, SWT.LEFT);
		labelKeySequence.setFont(parent.getFont());
		labelKeySequence.setText(Messages.getString("&Key Sequence:"));

		comboKeySequence = new Combo(compositeKeySequence, SWT.NONE);
		GridData gridDataComboKeySequence = new GridData();
		gridDataComboKeySequence.widthHint = 200;
		comboKeySequence.setLayoutData(gridDataComboKeySequence);		
		comboKeySequence.setFont(parent.getFont());

		Composite compositePrompt = new Composite(compositeKeySequence, SWT.NONE);		
		GridLayout gridLayoutCompositePrompt = new GridLayout();
		gridLayoutCompositePrompt.numColumns = 2;
		gridLayoutCompositePrompt.marginWidth = 0;
		gridLayoutCompositePrompt.marginHeight = 0;
		gridLayoutCompositePrompt.horizontalSpacing = HALF_SPACE;
		gridLayoutCompositePrompt.verticalSpacing = HALF_SPACE;
		compositePrompt.setLayout(gridLayoutCompositePrompt);
		
		//compositePrompt.setBackground(new Color(parent.getDisplay(), new RGB(230, 226, 221)));

		labelPromptImage = new Label(compositePrompt, SWT.LEFT);	
		labelPromptImage.setFont(parent.getFont());
		//labelPromptImage.setImage(imageInfo);
		labelPromptImage.setImage(imageError);

		labelPromptText = new Label(compositePrompt, SWT.LEFT);		
		labelPromptText.setFont(parent.getFont());
		//labelPromptText.setText("Select or Type a Key Sequence");
		labelPromptText.setText("Invalid Key Sequence");

		Composite compositeStateAndAction = new Composite(composite, SWT.NONE);
		GridLayout gridLayoutCompositeStateAndAction = new GridLayout();
		gridLayoutCompositeStateAndAction.numColumns = 2;
		//gridLayoutCompositeStateAndAction.makeColumnsEqualWidth = true;
		gridLayoutCompositeStateAndAction.horizontalSpacing = SPACE;
		compositeStateAndAction.setLayout(gridLayoutCompositeStateAndAction);

		Group groupState = new Group(compositeStateAndAction, SWT.NONE);	
		groupState.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gridLayoutGroupState = new GridLayout();
		gridLayoutGroupState.numColumns = 2;
		gridLayoutGroupState.marginWidth = SPACE;
		gridLayoutGroupState.marginHeight = SPACE;
		gridLayoutGroupState.horizontalSpacing = SPACE;
		gridLayoutGroupState.verticalSpacing = SPACE;
		groupState.setLayout(gridLayoutGroupState);
		groupState.setFont(parent.getFont());
		groupState.setText(Messages.getString("When"));	

		labelScope = new Label(groupState, SWT.LEFT);
		labelScope.setFont(parent.getFont());
		labelScope.setText(Messages.getString("Sco&pe:"));

		comboScope = new Combo(groupState, SWT.READ_ONLY);
		GridData gridDataComboScope = new GridData();
		gridDataComboScope.widthHint = 100;
		comboScope.setLayoutData(gridDataComboScope);
		comboScope.setFont(parent.getFont());
		comboScope.setItems(getScopes());

		labelConfiguration = new Label(groupState, SWT.LEFT);
		labelConfiguration.setFont(parent.getFont());
		labelConfiguration.setText(Messages.getString("Co&nfiguration:"));

		comboConfiguration = new Combo(groupState, SWT.READ_ONLY);
		GridData gridDataComboConfiguration = new GridData(GridData.FILL_HORIZONTAL);
		gridDataComboConfiguration.widthHint = 100;
		comboConfiguration.setLayoutData(gridDataComboConfiguration);
		comboConfiguration.setFont(parent.getFont());
		comboConfiguration.setItems(getConfigurations());

		Group groupAction = new Group(compositeStateAndAction, SWT.NONE);	
		groupAction.setLayoutData(new GridData(GridData.FILL_BOTH));		
		GridLayout gridLayoutGroupAction = new GridLayout();
		gridLayoutGroupAction.numColumns = 2;
		gridLayoutGroupAction.marginWidth = SPACE;
		gridLayoutGroupAction.marginHeight = SPACE;
		gridLayoutGroupAction.horizontalSpacing = SPACE;
		gridLayoutGroupAction.verticalSpacing = SPACE;
		groupAction.setLayout(gridLayoutGroupAction);
		groupAction.setFont(parent.getFont());
		groupAction.setText(Messages.getString("Action"));			

		buttonDefault = new Button(groupAction, SWT.LEFT | SWT.RADIO);
		buttonDefault.setFont(parent.getFont());
		buttonDefault.setText(Messages.getString("&Default:"));

		textDefault = new Text(groupAction, SWT.BORDER | SWT.READ_ONLY);
		GridData gridDataTextDefault = new GridData(GridData.FILL_HORIZONTAL);
		gridDataTextDefault.widthHint = 150;
		textDefault.setLayoutData(gridDataTextDefault);
		textDefault.setFont(parent.getFont());

		buttonCustom = new Button(groupAction, SWT.LEFT | SWT.RADIO);
		buttonCustom.setFont(parent.getFont());
		buttonCustom.setText(Messages.getString("&Custom:"));

		comboCustom = new Combo(groupAction, SWT.READ_ONLY);
		GridData gridDataComboCustom = new GridData(GridData.FILL_HORIZONTAL);
		gridDataComboCustom.widthHint = 150;
		comboCustom.setLayoutData(gridDataComboCustom);
		comboCustom.setFont(parent.getFont());
		comboCustom.setItems(getActions());

		Composite compositeSet = new Composite(composite, SWT.NONE);		
		GridLayout gridLayoutCompositeSet = new GridLayout();
		gridLayoutCompositeSet.marginWidth = DOUBLE_SPACE;
		compositeSet.setLayout(gridLayoutCompositeSet);

		buttonSet = createButton(compositeSet, BUTTON_SET_ID, "&Set", false); 

		Composite compositeTable = new Composite(composite, SWT.NONE);		
		compositeTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gridLayoutCompositeTable = new GridLayout();
		compositeTable.setLayout(gridLayoutCompositeTable);

		Table table = new Table(compositeTable, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gridDataTable = new GridData(GridData.FILL_BOTH);
		gridDataTable.heightHint = 100;		
		table.setLayoutData(gridDataTable);

		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setHeaderVisible(true);
		
		for (int i = 0; i < columnHeaders.length; i++) {
			layout.addColumnData(columnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(columnHeaders[i]);
			//tc.addSelectionListener(headerListener);
		}

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(new Object());
		//refresh();		

		//TableItem[] tableItems = table.getItems();
		//Color background = tableItems[0].getBackground();
		//tableItems[0].setBackground(new Color(parent.getDisplay(), new RGB(background.getRed() - 15, background.getGreen(), background.getBlue() - 15)));

		return composite;		
	}	

	private String[] getActions() {
		SortedMap actionMap = org.eclipse.ui.internal.actions.Registry.getInstance().getActionMap();
		int size = actionMap.size();
		String[] actions = new String[size];
		Iterator iterator = actionMap.values().iterator();
		int i = 0;
		
		while (iterator.hasNext()) {
			org.eclipse.ui.internal.actions.Action action = (org.eclipse.ui.internal.actions.Action) iterator.next();
			actions[i++] = action.toString();							
		}
		
		return actions;	
	}

	private String[] getConfigurations() {
		SortedMap configurationMap = keyManager.getRegistryConfigurationMap();
		int size = configurationMap.size();
		String[] configurations = new String[size];
		Iterator iterator = configurationMap.values().iterator();
		int i = 0;
		
		while (iterator.hasNext()) {
			Configuration configuration = (Configuration) iterator.next();
			configurations[i++] = configuration.toString();							
		}
		
		return configurations;	
	}

	private String[] getScopes() {
		SortedMap scopeMap = keyManager.getRegistryScopeMap();
		int size = scopeMap.size();
		String[] scopes = new String[size];
		Iterator iterator = scopeMap.values().iterator();
		int i = 0;
		
		while (iterator.hasNext()) {
			Scope scope = (Scope) iterator.next();
			scopes[i++] = scope.toString();							
		}
		
		return scopes;	
	}
}
