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


import java.util.*;
import java.util.List;
import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ui.OverlayIcon;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.ide.IDE.SharedImages;

public class CVSDecoratorPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button imageShowDirty;
	private Button imageShowHasRemote;
	private Button imageShowAdded;
	private Button imageShowNewResource;
	
	private Text fileTextFormat;
	private Text folderTextFormat;	
	private Text projectTextFormat;
	
	private Text dirtyFlag;
	private Text addedFlag;
	
	private Button showDirty;
	
	//	Cache for folder images that have been overlayed with sample CVS icons
	private Map fgImageCache;

	// Tree that provides a preview of the decorations
	private TreeViewer previewTree;
	
	/**
	 * Update the preview tree when a theme changes.
	 */
	private IPropertyChangeListener themeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			previewTree.refresh(true /* update labels */);
		}
	};
	
	/**
	 * Label provider creates a dummy CVSDecoration using the preferences set
	 * in this page. The decoration properties are assigned to the different named
	 * resources in the preview tree so that most of the decoration options
	 * are shown
	 */
	private class PreviewLabelProvider extends LabelProvider implements IFontProvider, IColorProvider {
		
		public Color getBackground(Object element) {
			CVSDecoration decoration = getDecoration(element);
			return decoration.getBackgroundColor();
		}
		
		public Color getForeground(Object element) {
			CVSDecoration decoration = getDecoration(element);
			return decoration.getForegroundColor();
		}
			
		public Font getFont(Object element) {
			CVSDecoration decoration = getDecoration(element);
			return decoration.getFont();
		}
		
		public String getText(Object element) {
			CVSDecoration decoration = getDecoration(element);
			StringBuffer buffer = new StringBuffer();
			String prefix = decoration.getPrefix();
			if(prefix != null)
				buffer.append(prefix);
			buffer.append((String)element);
			String suffix = decoration.getSuffix();
			if(suffix != null)
				buffer.append(suffix);
			return buffer.toString();
		}
		
		public CVSDecoration getDecoration(Object element) {
			CVSDecoration decoration = buildDecoration((String)element);
			if(element.equals("ignored.txt")) { //$NON-NLS-1$
				decoration.setResourceType(IResource.FILE);
				decoration.setIgnored(true);
			} else if(element.equals("Project")) { //$NON-NLS-1$
				decoration.setResourceType(IResource.PROJECT);
				decoration.setHasRemote(true);
			} else if(element.equals("Folder")) { //$NON-NLS-1$
				decoration.setHasRemote(true);
				decoration.setResourceType(IResource.FOLDER);
				decoration.setDirty(true);
				decoration.setHasRemote(true);
			} else if(element.equals("dirty.cpp")){ //$NON-NLS-1$
				decoration.setResourceType(IResource.FILE);
				decoration.setDirty(true);
				decoration.setHasRemote(true);
			} else if(element.equals("added.java")){ //$NON-NLS-1$
				decoration.setResourceType(IResource.FILE);
				decoration.setAdded(true);
				decoration.setHasRemote(false);
			} else if(element.equals("todo.txt")){ //$NON-NLS-1$
				decoration.setResourceType(IResource.FILE);
				decoration.setNewResource(true);
			} else if(element.equals("bugs.txt")){ //$NON-NLS-1$
				decoration.setResourceType(IResource.FILE);
				decoration.setDirty(false);
				decoration.setHasRemote(true);
			}
			decoration.compute();
			return decoration;
		}
		
		public Image getImage(Object element) {
			Image baseImage;
			if(element.equals("Project")) { //$NON-NLS-1$
				baseImage= PlatformUI.getWorkbench().
				getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
			} else if(element.equals("Folder")) { //$NON-NLS-1$
				baseImage= PlatformUI.getWorkbench().
				getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			} else {
				baseImage= PlatformUI.getWorkbench().
				getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			}
			CVSDecoration decoration = getDecoration(element);
			ImageDescriptor overlay = decoration.getOverlay();
			if(overlay == null) 
				return baseImage;
			
			// Otherwise
			ImageDescriptor resultImageDescriptor = new OverlayIcon(baseImage, new ImageDescriptor[] {overlay}, new int[] {OverlayIcon.BOTTOM_RIGHT}, new Point(baseImage.getBounds().width, baseImage.getBounds().height));
			if (fgImageCache == null) {
				fgImageCache = new HashMap(10);
			}
			Image image = (Image) fgImageCache.get(overlay);
			if (image == null) {
				image = resultImageDescriptor.createImage();
				fgImageCache.put(resultImageDescriptor, image);
			}
			return image;
		}
	};
	
	/**
	 * Provides a fixed mock resource tree for showing the preview.
	 */
	private IContentProvider previewContentProvider = new ITreeContentProvider() {
		public Object[] getChildren(Object parentElement) {
			if(parentElement == ResourcesPlugin.getWorkspace().getRoot()) {
				return new String[] {
						"Project"}; //$NON-NLS-1$
			} else if(parentElement.equals("Project")) { //$NON-NLS-1$
				return new String[] {
						"ignored.txt", //$NON-NLS-1$
						"dirty.cpp", //$NON-NLS-1$
						"added.java", //$NON-NLS-1$
						"todo.txt", //$NON-NLS-1$
						"bugs.txt", //$NON-NLS-1$
						"Folder" //$NON-NLS-1$
				};
			} else {
				return new Object[0];
			}
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if(element.equals("Project") || element == ResourcesPlugin.getWorkspace().getRoot())  //$NON-NLS-1$
				return true;
			else
				return false;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	};
	
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
				previewTree.refresh(true);
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
		return new TextPair(format, null);
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
		//tabItem = new TabItem(tabFolder, SWT.NONE);
		//tabItem.setText(Policy.bind("Icon_Overlays_24"));//$NON-NLS-1$		
		//tabItem.setControl(createIconDecoratorPage(tabFolder));
		
		// general decoration options
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Policy.bind("CVSDecoratorPreferencesPage.generalTabFolder"));//$NON-NLS-1$
		tabItem.setControl(createGeneralDecoratorPage(tabFolder));
		
		initializeValues();
		WorkbenchHelp.setHelp(tabFolder, IHelpContextIds.DECORATORS_PREFERENCE_PAGE);
		Dialog.applyDialogFont(parent);
		
		previewTree.setInput(ResourcesPlugin.getWorkspace().getRoot());
		previewTree.expandAll();
		
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(themeListener);
		
		return tabFolder;
	}
	
	public void dispose() {
		PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(themeListener);
	}
	
	private Control createTextDecoratorPage(Composite parent) {
		Composite top = new Composite(parent, SWT.NULL);
		GridLayout	layout = new GridLayout();
		top.setLayout(layout);
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
		top.setLayoutData(data);
		
		Group fileTextGroup = new Group(top, SWT.SHADOW_IN);
		fileTextGroup.setText(Policy.bind("CVSDecoratorPreferencePage.1")); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 3;
		fileTextGroup.setLayout(layout);
		data = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		fileTextGroup.setLayoutData(data);

		TextPair format = createFormatEditorControl(fileTextGroup, Policy.bind("&File_Format__14"), Policy.bind("Add_&Variables_15"), getFileBindingDescriptions()); //$NON-NLS-1$ //$NON-NLS-2$
		fileTextFormat = format.t1;
		format = createFormatEditorControl(fileTextGroup, Policy.bind("F&older_Format__16"), Policy.bind("Add_Varia&bles_17"), getFolderBindingDescriptions()); //$NON-NLS-1$ //$NON-NLS-2$
		folderTextFormat = format.t1;
		format = createFormatEditorControl(fileTextGroup, Policy.bind("&Project_Format__18"), Policy.bind("Add_Variable&s_19"), getFolderBindingDescriptions()); //$NON-NLS-1$ //$NON-NLS-2$
		projectTextFormat = format.t1;

		createLabel(fileTextGroup, Policy.bind("&Label_decoration_for_outgoing__20"), 1); //$NON-NLS-1$
		dirtyFlag = new Text(fileTextGroup, SWT.BORDER);
		dirtyFlag.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dirtyFlag.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				previewTree.refresh(true);
			}
		});
		createLabel(fileTextGroup, "", 1); // spacer //$NON-NLS-1$

		createLabel(fileTextGroup, Policy.bind("Label_decorat&ion_for_added__22"), 1); //$NON-NLS-1$
		addedFlag = new Text(fileTextGroup, SWT.BORDER);
		addedFlag.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addedFlag.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				previewTree.refresh(true);
			}
		});
		createLabel(fileTextGroup, "", 1); // spacer //$NON-NLS-1$

		// **********
		// ICONS
		// **********
		
		Group iconGroup = new Group(top, SWT.SHADOW_IN);
		iconGroup.setText(Policy.bind("Icon_Overlays_24")); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 3;
		iconGroup.setLayout(layout);
		data = data = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		iconGroup.setLayoutData(data);
		
		createIconDecoratorPage(iconGroup);
		
		// **********
		// PREVIEW
		// **********
		
		Group previewGroup = new Group(top, SWT.SHADOW_IN);
		previewGroup.setText(Policy.bind("Example__1")); //$NON-NLS-1$
		layout = new GridLayout();
		previewGroup.setLayout(layout);
		data = new GridData(GridData.FILL, GridData.FILL, true, true);
		previewGroup.setLayoutData(data);
		
		// Preview Pane
		previewTree = new TreeViewer(previewGroup);
		data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		data.horizontalSpan = 2;
		data.heightHint = 100;
		previewTree.getTree().setLayoutData(data);
		previewTree.setContentProvider(previewContentProvider);
		previewTree.setLabelProvider(new PreviewLabelProvider());
		return top;	
	}
	
	private Control createIconDecoratorPage(Composite parent) {
		Composite imageGroup = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		imageGroup.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		imageGroup.setLayoutData(data);
		
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
		data.verticalAlignment = GridData.BEGINNING;
		label.setLayoutData(data);
		return label;
	}
	
	private Button createCheckBox(Composite group, String label) {
		Button button = new Button(group, SWT.CHECK);
		button.setText(label);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				previewTree.refresh(true);
			}
		});
		return button;
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
	
	protected CVSDecoration buildDecoration(String name) {
		Preferences prefs = new Preferences();
		
		prefs.setDefault(ICVSUIConstants.PREF_SHOW_DIRTY_DECORATION, imageShowDirty.getSelection());
		prefs.setDefault(ICVSUIConstants.PREF_SHOW_ADDED_DECORATION, imageShowAdded.getSelection());
		prefs.setDefault(ICVSUIConstants.PREF_SHOW_HASREMOTE_DECORATION, imageShowHasRemote.getSelection());
		prefs.setDefault(ICVSUIConstants.PREF_SHOW_NEWRESOURCE_DECORATION, imageShowNewResource.getSelection());
		prefs.setDefault(ICVSUIConstants.PREF_CALCULATE_DIRTY, true);
		prefs.setDefault(ICVSUIConstants.PREF_DIRTY_FLAG, dirtyFlag.getText());
		prefs.setDefault(ICVSUIConstants.PREF_ADDED_FLAG, addedFlag.getText());
		
		CVSDecoration decoration = 
			new CVSDecoration(name, prefs, fileTextFormat.getText(), folderTextFormat.getText(), projectTextFormat.getText());
		
		decoration.setTag("v1_0"); //$NON-NLS-1$
		decoration.setKeywordSubstitution(Command.KSUBST_TEXT.getShortDisplayText()); //$NON-NLS-1$
		decoration.setRevision("1.45"); //$NON-NLS-1$
		try {
			decoration.setLocation(CVSRepositoryLocation.fromString(":pserver:alize@cvs.site.org:/home/cvsroot")); //$NON-NLS-1$
		} catch (CVSException e) {
			// continue without a location, since the location is hard coded an exception should never occur
		}
		return decoration;
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
