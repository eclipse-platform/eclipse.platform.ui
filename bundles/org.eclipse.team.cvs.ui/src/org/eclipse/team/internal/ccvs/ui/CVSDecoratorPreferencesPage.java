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
package org.eclipse.team.internal.ccvs.ui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

public class CVSDecoratorPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button imageShowDirty;
	private Button imageShowHasRemote;
	private Button imageShowAdded;
	private Button imageShowNewResource;
	
	private Text fileTextFormat;
	private Text fileTextFormatExample;
	
	private Text folderTextFormat;
	private Text folderTextFormatExample;
	
	private Text projectTextFormat;
	private Text projectTextFormatExample;
	
	private Text dirtyFlag;
	private Text addedFlag;
	
	private Button showDirty;
	
	class StringPair {
		String s1;
		String s2;
	}
	
	class TextPair {
		TextPair(Text t1, Text t2) {
			this.t1 = t1;
			this.t2 = t2;
		}
		Text t1;
		Text t2;
	}
	
	/**
	 * Constructor for CVSDecoratorPreferencesPage.
	 */
	public CVSDecoratorPreferencesPage() {
		setDescription(Policy.bind("CVSDecoratorPreferencesPage.description")); //$NON-NLS-1$;
	}

	protected TextPair createFormatEditorControl(Composite composite, String title, String buttonText, final Map supportedBindings) {
		createLabel(composite, title, 1);
		Text format = new Text(composite, SWT.BORDER);
		format.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		format.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {				
				updateExamples();
			}
		});
		Button b = new Button(composite, SWT.NONE);
		b.setText(buttonText);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, b.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		b.setLayoutData(data);
		final Text formatToInsert = format;
		b.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
				addVariables(formatToInsert, supportedBindings);
			}			
		});
		
		createLabel(composite, Policy.bind("Example__1"), 1); //$NON-NLS-1$
		Text example = new Text(composite, SWT.BORDER);
		example.setEditable(false);
		example.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createLabel(composite, "", 1); // spacer //$NON-NLS-1$
		return new TextPair(format, example);
	}
	
	protected void updateExamples() {
		Map bindings = new HashMap();
		try {
			ICVSRepositoryLocation location = CVSRepositoryLocation.fromString(":pserver:username@host.acme.org:/home/cvsroot");				 //$NON-NLS-1$
			bindings.put(CVSDecoratorConfiguration.RESOURCE_TAG, "v2_0"); //$NON-NLS-1$
			bindings.put(CVSDecoratorConfiguration.FILE_KEYWORD,
				Command.KSUBST_TEXT.getShortDisplayText()); //$NON-NLS-1$
			bindings.put(CVSDecoratorConfiguration.FILE_REVISION, "1.34"); //$NON-NLS-1$
			bindings.put(CVSDecoratorConfiguration.DIRTY_FLAG, dirtyFlag.getText());
			bindings.put(CVSDecoratorConfiguration.ADDED_FLAG, addedFlag.getText());
			bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_HOST, location.getHost());
			bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_METHOD, location.getMethod().getName());
			bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_USER, location.getUsername());
			bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_ROOT, location.getRootDirectory());
			bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_REPOSITORY, "org.eclipse.project1"); //$NON-NLS-1$
		} catch(CVSException e) {
			// Ignore
		}
		bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, "file.txt"); //$NON-NLS-1$
		setTextFormatExample(bindings);
		bindings.remove(CVSDecoratorConfiguration.RESOURCE_NAME);
		bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, "folder"); //$NON-NLS-1$
		setFolderFormatExample(bindings);
		bindings.remove(CVSDecoratorConfiguration.RESOURCE_NAME);
		bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, "Project"); //$NON-NLS-1$
		setProjectFormatExample(bindings);
	}
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
				
		// create a tab folder for the page
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));		
		
		// text decoration options
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Policy.bind("Text_Labels_12"));//$NON-NLS-1$		
		tabItem.setControl(createTextDecoratorPage(tabFolder));
		
		// image decoration options
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Policy.bind("Icon_Overlays_24"));//$NON-NLS-1$		
		tabItem.setControl(createIconDecoratorPage(tabFolder));
		
		// general decoration options
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Policy.bind("CVSDecoratorPreferencesPage.generalTabFolder"));//$NON-NLS-1$
		tabItem.setControl(createGeneralDecoratorPage(tabFolder));
		
		initializeValues();
		WorkbenchHelp.setHelp(tabFolder, IHelpContextIds.DECORATORS_PREFERENCE_PAGE);
		Dialog.applyDialogFont(parent);
		return tabFolder;
	}
	
	private Control createTextDecoratorPage(Composite parent) {
		Composite fileTextGroup = new Composite(parent, SWT.NULL);
		GridLayout	layout = new GridLayout();
		layout.numColumns = 3;
		fileTextGroup.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		fileTextGroup.setLayoutData(data);

		createLabel(fileTextGroup, Policy.bind("Select_the_format_for_file,_folders,_and_project_text_labels__13"), 3); //$NON-NLS-1$

		TextPair format = createFormatEditorControl(fileTextGroup, Policy.bind("&File_Format__14"), Policy.bind("Add_&Variables_15"), getFileBindingDescriptions()); //$NON-NLS-1$ //$NON-NLS-2$
		fileTextFormat = format.t1;
		fileTextFormatExample = format.t2;
		format = createFormatEditorControl(fileTextGroup, Policy.bind("F&older_Format__16"), Policy.bind("Add_Varia&bles_17"), getFolderBindingDescriptions()); //$NON-NLS-1$ //$NON-NLS-2$
		folderTextFormat = format.t1;
		folderTextFormatExample = format.t2;
		format = createFormatEditorControl(fileTextGroup, Policy.bind("&Project_Format__18"), Policy.bind("Add_Variable&s_19"), getFolderBindingDescriptions()); //$NON-NLS-1$ //$NON-NLS-2$
		projectTextFormat = format.t1;
		projectTextFormatExample = format.t2;

		createLabel(fileTextGroup, Policy.bind("&Label_decoration_for_outgoing__20"), 1); //$NON-NLS-1$
		dirtyFlag = new Text(fileTextGroup, SWT.BORDER);
		dirtyFlag.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dirtyFlag.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateExamples();
			}
		});
		createLabel(fileTextGroup, "", 1); // spacer //$NON-NLS-1$

		createLabel(fileTextGroup, Policy.bind("Label_decorat&ion_for_added__22"), 1); //$NON-NLS-1$
		addedFlag = new Text(fileTextGroup, SWT.BORDER);
		addedFlag.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addedFlag.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateExamples();
			}
		});

		return fileTextGroup;	
	}
	
	private Control createIconDecoratorPage(Composite parent) {
		Composite imageGroup = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		imageGroup.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		imageGroup.setLayoutData(data);

		createLabel(imageGroup, Policy.bind("CVSDecoratorPreferencesPage.iconDescription"), 1); //$NON-NLS-1$
		
		imageShowDirty = createCheckBox(imageGroup, Policy.bind("Sho&w_outgoing_25")); //$NON-NLS-1$
		imageShowHasRemote = createCheckBox(imageGroup, Policy.bind("Show_has_&remote_26")); //$NON-NLS-1$
		imageShowAdded = createCheckBox(imageGroup, Policy.bind("S&how_is_added_27")); //$NON-NLS-1$
		imageShowNewResource = createCheckBox(imageGroup, Policy.bind("CVSDecoratorPreferencesPage.newResources")); //$NON-NLS-1$		
		return imageGroup;
	}
	
	private Control createGeneralDecoratorPage(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		createLabel(composite, Policy.bind("CVSDecoratorPreferencesPage.generalDescription"), 1); //$NON-NLS-1$		
		showDirty = createCheckBox(composite, Policy.bind("&Compute_deep_outgoing_state_for_folders_(disabling_this_will_improve_decorator_performance)_28")); //$NON-NLS-1$		
		return composite;
	}
	
	private Label createLabel(Composite parent, String text, int span) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	
	private Button createCheckBox(Composite group, String label) {
		Button button = new Button(group, SWT.CHECK);
		button.setText(label);
		return button;
	}
	
	protected void setTextFormatExample(Map bindings) {
		String example = CVSDecoratorConfiguration.bind(fileTextFormat.getText(), bindings);				
		fileTextFormatExample.setText(example);
	}
	
	protected void setFolderFormatExample(Map bindings) {
		String example = CVSDecoratorConfiguration.bind(folderTextFormat.getText(), bindings);				
		folderTextFormatExample.setText(example);
	}
	
	protected void setProjectFormatExample(Map bindings) {
		String example = CVSDecoratorConfiguration.bind(projectTextFormat.getText(), bindings);					
		projectTextFormatExample.setText(example);
	}

	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		
		fileTextFormat.setText(store.getString(ICVSUIConstants.PREF_FILETEXT_DECORATION));
		folderTextFormat.setText(store.getString(ICVSUIConstants.PREF_FOLDERTEXT_DECORATION));
		projectTextFormat.setText(store.getString(ICVSUIConstants.PREF_PROJECTTEXT_DECORATION));
		
		addedFlag.setText(store.getString(ICVSUIConstants.PREF_ADDED_FLAG));
		dirtyFlag.setText(store.getString(ICVSUIConstants.PREF_DIRTY_FLAG));
		
		imageShowDirty.setSelection(store.getBoolean(ICVSUIConstants.PREF_SHOW_DIRTY_DECORATION));
		imageShowAdded.setSelection(store.getBoolean(ICVSUIConstants.PREF_SHOW_ADDED_DECORATION));
		imageShowHasRemote.setSelection(store.getBoolean(ICVSUIConstants.PREF_SHOW_HASREMOTE_DECORATION));
		imageShowNewResource.setSelection(store.getBoolean(ICVSUIConstants.PREF_SHOW_NEWRESOURCE_DECORATION));
		
		showDirty.setSelection(store.getBoolean(ICVSUIConstants.PREF_CALCULATE_DIRTY));
		setValid(true);
	}

	/**
	* @see IWorkbenchPreferencePage#init(IWorkbench)
	*/
	public void init(IWorkbench workbench) {
	}

	/**
	 * OK was clicked. Store the CVS preferences.
	 *
	 * @return whether it is okay to close the preference page
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(ICVSUIConstants.PREF_FILETEXT_DECORATION, fileTextFormat.getText());
		store.setValue(ICVSUIConstants.PREF_FOLDERTEXT_DECORATION, folderTextFormat.getText());
		store.setValue(ICVSUIConstants.PREF_PROJECTTEXT_DECORATION, projectTextFormat.getText());
		
		store.setValue(ICVSUIConstants.PREF_ADDED_FLAG, addedFlag.getText());
		store.setValue(ICVSUIConstants.PREF_DIRTY_FLAG, dirtyFlag.getText());
		
		store.setValue(ICVSUIConstants.PREF_SHOW_DIRTY_DECORATION, imageShowDirty.getSelection());
		store.setValue(ICVSUIConstants.PREF_SHOW_ADDED_DECORATION, imageShowAdded.getSelection());
		store.setValue(ICVSUIConstants.PREF_SHOW_HASREMOTE_DECORATION, imageShowHasRemote.getSelection());
		store.setValue(ICVSUIConstants.PREF_SHOW_NEWRESOURCE_DECORATION, imageShowNewResource.getSelection());
		
		store.setValue(ICVSUIConstants.PREF_CALCULATE_DIRTY, showDirty.getSelection());
		
		CVSUIPlugin.broadcastPropertyChange(new PropertyChangeEvent(this, CVSUIPlugin.P_DECORATORS_CHANGED, null, null));
		CVSUIPlugin.getPlugin().savePluginPreferences();
		return true;
	}

	/**
	 * Defaults was clicked. Restore the CVS preferences to
	 * their default values
	 */
	protected void performDefaults() {
		super.performDefaults();
		IPreferenceStore store = getPreferenceStore();
		
		fileTextFormat.setText(store.getDefaultString(ICVSUIConstants.PREF_FILETEXT_DECORATION));
		folderTextFormat.setText(store.getDefaultString(ICVSUIConstants.PREF_FOLDERTEXT_DECORATION));
		projectTextFormat.setText(store.getDefaultString(ICVSUIConstants.PREF_PROJECTTEXT_DECORATION));
		
		addedFlag.setText(store.getDefaultString(ICVSUIConstants.PREF_ADDED_FLAG));
		dirtyFlag.setText(store.getDefaultString(ICVSUIConstants.PREF_DIRTY_FLAG));
		
		imageShowDirty.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_SHOW_DIRTY_DECORATION));
		imageShowAdded.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_SHOW_ADDED_DECORATION));
		imageShowHasRemote.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_SHOW_HASREMOTE_DECORATION));
		imageShowNewResource.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_SHOW_NEWRESOURCE_DECORATION));
		
		showDirty.setSelection(store.getDefaultBoolean(ICVSUIConstants.PREF_CALCULATE_DIRTY));	
	}

	/**
	* Returns preference store that belongs to the our plugin.
	* This is important because we want to store
	* our preferences separately from the desktop.
	*
	* @return the preference store for this plugin
	*/
	protected IPreferenceStore doGetPreferenceStore() {
		return CVSUIPlugin.getPlugin().getPreferenceStore();
	}
	
	/**
	 * Add another project to the list at the end.
	 */
	private void addVariables(Text target, Map bindings) {
	
		final List variables = new ArrayList(bindings.size());
		
		ILabelProvider labelProvider = new LabelProvider() {
			public String getText(Object element) {
				return ((StringPair)element).s1 + " - " + ((StringPair)element).s2; //$NON-NLS-1$
			}
		};
		
		IStructuredContentProvider contentsProvider = new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return variables.toArray(new StringPair[variables.size()]);
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		};
		
		for (Iterator it = bindings.keySet().iterator(); it.hasNext();) {
			StringPair variable = new StringPair();
			variable.s1 = (String) it.next(); // variable
			variable.s2 = (String) bindings.get(variable.s1); // description
			variables.add(variable);				
		}
	
		ListSelectionDialog dialog =
			new ListSelectionDialog(
				this.getShell(),
				this,
				contentsProvider,
				labelProvider,
				Policy.bind("Select_the_&variables_to_add_to_the_decoration_format__30")); //$NON-NLS-1$
		dialog.setTitle(Policy.bind("Add_Variables_31")); //$NON-NLS-1$
		if (dialog.open() != ListSelectionDialog.OK)
			return;
	
		Object[] result = dialog.getResult();
		
		for (int i = 0; i < result.length; i++) {
			target.insert("{"+((StringPair)result[i]).s1 +"}"); //$NON-NLS-1$ //$NON-NLS-2$
		}		
	}
	
	private Map getFolderBindingDescriptions() {
		Map bindings = new HashMap();
		bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, Policy.bind("name_of_the_resource_being_decorated_34")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.RESOURCE_TAG, Policy.bind("the_tag_applied_to_the_resource_(version,_branch,_or_date)_35")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_HOST, Policy.bind("the_repository_location__s_hostname_36")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_METHOD, Policy.bind("the_connection_method_(e.g._pserver,_ssh)_37")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_USER, Policy.bind("user_name_for_the_connection_38")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_ROOT, Policy.bind("repository_home_directory_on_server_39")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_REPOSITORY, Policy.bind("root_relative_directory_40")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.DIRTY_FLAG, Policy.bind("flag_indicating_that_the_folder_has_a_child_resource_with_outgoing_changes_41")); //$NON-NLS-1$
		return bindings;
	}
	
	private Map getFileBindingDescriptions() {
		Map bindings = new HashMap();
		bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, Policy.bind("name_of_the_resource_being_decorated_42")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.RESOURCE_TAG, Policy.bind("the_tag_applied_to_the_resource_43")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.FILE_KEYWORD, Policy.bind("keyword_substitution_rule_for_the_resource_44")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.FILE_REVISION, Policy.bind("last_revision_loaded_into_workspace_45")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.DIRTY_FLAG, Policy.bind("flag_indicating_that_the_file_has_outgoing_changes_46")); //$NON-NLS-1$
		bindings.put(CVSDecoratorConfiguration.ADDED_FLAG, Policy.bind("flag_indicating_that_the_file_has_been_added_to_the_server_47")); //$NON-NLS-1$
		return bindings;
	}
}
