package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.ListSelectionDialog;

public class CVSDecoratorPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button imageShowDirty;
	private Button imageShowHasRemote;
	private Button imageShowAdded;
	
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
		b.setLayoutData(new GridData());
		final Text formatToInsert = format;
		b.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent (Event event) {
				addVariables(formatToInsert, supportedBindings);
			}			
		});
		
		createLabel(composite, "Example:", 1);
		Text example = new Text(composite, SWT.BORDER);
		example.setEditable(false);
		example.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createLabel(composite, "", 1); // spacer
		return new TextPair(format, example);
	}
	
	protected void updateExamples() {
		String example = "";
		Map bindings = new HashMap();
		try {
			ICVSRepositoryLocation location = CVSRepositoryLocation.fromString(":pserver:username@host.acme.org:/home/cvsroot");				
			bindings.put(CVSDecoratorConfiguration.RESOURCE_TAG, "v2_0");
			bindings.put(CVSDecoratorConfiguration.FILE_KEYWORD, CVSDecorator.getFileTypeString("file.txt",null));
			bindings.put(CVSDecoratorConfiguration.FILE_REVISION, "1.34");
			bindings.put(CVSDecoratorConfiguration.DIRTY_FLAG, dirtyFlag.getText());
			bindings.put(CVSDecoratorConfiguration.ADDED_FLAG, addedFlag.getText());
			bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_HOST, location.getHost());
			bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_METHOD, location.getMethod().getName());
			bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_USER, location.getUsername());
			bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_ROOT, location.getRootDirectory());
			bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_REPOSITORY, "org.eclipse.project1");
		} catch(CVSException e) {
			example = e.getMessage();
		}
		bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, "file.txt");
		setTextFormatExample(bindings);
		bindings.remove(CVSDecoratorConfiguration.RESOURCE_NAME);
		bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, "folder");
		setFolderFormatExample(bindings);
		bindings.remove(CVSDecoratorConfiguration.RESOURCE_NAME);
		bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, "Project");
		setProjectFormatExample(bindings);
	}
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData());

		// file text decoration options

		Group fileTextGroup = new Group(composite, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 3;
		fileTextGroup.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		fileTextGroup.setLayoutData(data);
		fileTextGroup.setText("Text Labels");
		createLabel(fileTextGroup, "Select the format for file, folders, and project text labels:", 3);

		TextPair format = createFormatEditorControl(fileTextGroup, "&File Format:", "Add &Variables...", getFileBindingDescriptions());
		fileTextFormat = format.t1;
		fileTextFormatExample = format.t2;
		format = createFormatEditorControl(fileTextGroup, "F&older Format:", "Add Varia&bles...", getFolderBindingDescriptions());
		folderTextFormat = format.t1;
		folderTextFormatExample = format.t2;
		format = createFormatEditorControl(fileTextGroup, "&Project Format:", "Add Variable&s...", getFolderBindingDescriptions());
		projectTextFormat = format.t1;
		projectTextFormatExample = format.t2;

		createLabel(fileTextGroup, "&Label decoration for outgoing:", 1);
		dirtyFlag = new Text(fileTextGroup, SWT.BORDER);
		dirtyFlag.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dirtyFlag.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {				
				updateExamples();
			}
		});
		createLabel(fileTextGroup, "", 1); // spacer
		
		createLabel(fileTextGroup, "Label decorat&ion for added:", 1);
		addedFlag = new Text(fileTextGroup, SWT.BORDER);
		addedFlag.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addedFlag.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {				
				updateExamples();
			}
		});

		createLabel(fileTextGroup, "", 1); // spacer
		
		// image decoration options
		
		Group imageGroup = new Group(composite, SWT.NULL);
		layout = new GridLayout();
		imageGroup.setLayout(layout);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		imageGroup.setLayoutData(data);
		imageGroup.setText("Icon Overlays");
		imageShowDirty = createCheckBox(imageGroup, "Sho&w outgoing");
		imageShowHasRemote = createCheckBox(imageGroup, "Show has &remote");
		imageShowAdded = createCheckBox(imageGroup, "S&how is added");
		
		showDirty = createCheckBox(composite, "&Compute deep outgoing state for folders (disabling this will improve decorator performance)");
				
		initializeValues();
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
		
		store.setValue(ICVSUIConstants.PREF_CALCULATE_DIRTY, showDirty.getSelection());
		
		CVSDecorator.refresh();
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
				return ((StringPair)element).s1 + " - " + ((StringPair)element).s2;
			}
		};
		
		IStructuredContentProvider contentsProvider = new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[]) variables.toArray(new StringPair[variables.size()]);
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
				"Select the &variables to add to the decoration format:");
		dialog.setTitle("Add Variables");
		if (dialog.open() != dialog.OK)
			return;
	
		Object[] result = dialog.getResult();
		
		for (int i = 0; i < result.length; i++) {
			target.insert("{"+((StringPair)result[i]).s1 +"}");
		}		
	}
	
	private Map getFolderBindingDescriptions() {
		Map bindings = new HashMap();
		bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, "name of the resource being decorated");
		bindings.put(CVSDecoratorConfiguration.RESOURCE_TAG, "the tag applied to the resource (version, branch, or date)");
		bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_HOST, "the repository location's hostname");
		bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_METHOD, "the connection method (e.g. pserver, ssh)");
		bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_USER, "user name for the connection");
		bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_ROOT, "repository home directory on server");
		bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_REPOSITORY, "root relative directory");
		bindings.put(CVSDecoratorConfiguration.DIRTY_FLAG, "flag indicating that the folder has a child resource with outgoing changes");
		return bindings;
	}
	
	private Map getFileBindingDescriptions() {
		Map bindings = new HashMap();
		bindings.put(CVSDecoratorConfiguration.RESOURCE_NAME, "name of the resource being decorated");
		bindings.put(CVSDecoratorConfiguration.RESOURCE_TAG, "the tag applied to the resource");
		bindings.put(CVSDecoratorConfiguration.FILE_KEYWORD, "keyword subsitution rule for the resource");
		bindings.put(CVSDecoratorConfiguration.FILE_REVISION, "last revision loaded into workspace");
		bindings.put(CVSDecoratorConfiguration.DIRTY_FLAG, "flag indicating that the file has outgoing changes");
		bindings.put(CVSDecoratorConfiguration.ADDED_FLAG, "flag indicating that the file has been added to the server");
		return bindings;
	}
}