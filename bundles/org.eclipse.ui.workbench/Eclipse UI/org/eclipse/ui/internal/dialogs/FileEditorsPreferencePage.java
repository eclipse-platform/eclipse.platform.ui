package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.registry.*;


/**
 * The file editors page presents the collection of file names and extensions
 * for which the user has registered editors. It also lets the user add new
 * internal or external (program) editors for a given file name and extension.
 *
 * The user can add an editor for either a specific file name and extension
 * (e.g. report.doc), or for all file names of a given extension (e.g. *.doc)
 *
 * The set of registered editors is tracked by the EditorRegistery
 * available from the workbench plugin.
 */
public class FileEditorsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, Listener {
	protected Table resourceTypeTable;
	protected Button addResourceTypeButton;
	protected Button removeResourceTypeButton;
	protected Table editorTable;
	protected Button addEditorButton;
	protected Button removeEditorButton;
	protected Button defaultEditorButton;
	protected Label editorLabel;

	protected IWorkbench workbench;
	protected List imagesToDispose;
	protected Map editorsToImages;
/**
 * Add a new resource type to the collection shown in the top of the page.
 * This is typically called after the extension dialog is shown to the user.
 */
public void addResourceType(String newName, String newExtension) {
	// Either a file name or extension must be provided
	Assert.isTrue((newName != null && newName.length() != 0) || 
		(newExtension != null && newExtension.length() != 0));

	// Wild card only valid by itself (i.e. rep* is not valid)
	// And must have an extension
	int index = newName.indexOf('*');
	if (index > -1) {
		Assert.isTrue(index == 0 && newName.length() == 1); 
		Assert.isTrue(newExtension != null && newExtension.length() != 0);
	}
	
	// Find the index at which to insert the new entry.
	String newFilename = (newName + (newExtension == null || newExtension.length() == 0 ?
		 "" : "." + newExtension)).toUpperCase();//$NON-NLS-1$ //$NON-NLS-2$
	IFileEditorMapping resourceType;
	TableItem[] items = resourceTypeTable.getItems();
	boolean found = false;
	int i = 0;

	while (i < items.length && !found) {
		resourceType = (IFileEditorMapping) items[i].getData();
		int result = newFilename.compareToIgnoreCase(resourceType.getLabel());
		if (result == 0) {
			// Same resource type not allowed!
			MessageDialog.openInformation(
				getControl().getShell(),
				WorkbenchMessages.getString("FileEditorPreference.existsTitle"), //$NON-NLS-1$
				WorkbenchMessages.getString("FileEditorPreference.existsMessage")); //$NON-NLS-1$
			return;
		}

		if (result < 0)
			found = true;
		else
			i++;
	}

	// Create the new type and insert it
	resourceType = new FileEditorMapping(newName, newExtension);
	TableItem item = newResourceTableItem(resourceType, i, true);
	resourceTypeTable.setFocus();
	resourceTypeTable.showItem(item);
	fillEditorTable();
}
/**
 * Creates the page's UI content.
 */
protected Control createContents(Composite parent) {
	imagesToDispose = new ArrayList();
	editorsToImages = new HashMap(50);
	Font font = parent.getFont();

	// define container & its gridding
	Composite pageComponent = new Composite(parent, SWT.NULL);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	pageComponent.setLayout(layout);
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	pageComponent.setLayoutData(data);
	pageComponent.setFont(font);

	//layout the contents

	//layout the top table & its buttons
	Label label = new Label(pageComponent, SWT.LEFT);
	label.setText(WorkbenchMessages.getString("FileEditorPreference.fileTypes")); //$NON-NLS-1$
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.horizontalSpan = 2;
	label.setLayoutData(data);
	label.setFont(font);

	resourceTypeTable = new Table(pageComponent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
	resourceTypeTable.addListener(SWT.Selection, this);
	resourceTypeTable.addListener(SWT.DefaultSelection, this);
	data = new GridData(GridData.FILL_HORIZONTAL);
	
	int availableRows = DialogUtil.availableRows(pageComponent);
		
	data.heightHint = resourceTypeTable.getItemHeight()* (availableRows / 8);
	resourceTypeTable.setLayoutData(data);
	resourceTypeTable.setFont(font);

	Composite groupComponent= new Composite(pageComponent, SWT.NULL);
	GridLayout groupLayout = new GridLayout();
	groupLayout.marginWidth = 0;
	groupLayout.marginHeight = 0;
	groupComponent.setLayout(groupLayout);
	data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	groupComponent.setLayoutData(data);
	groupComponent.setFont(font);
	
	addResourceTypeButton = new Button(groupComponent, SWT.PUSH);
	addResourceTypeButton.setText(WorkbenchMessages.getString("FileEditorPreference.add")); //$NON-NLS-1$
	addResourceTypeButton.addListener(SWT.Selection, this);
	addResourceTypeButton.setLayoutData(data);
	addResourceTypeButton.setFont(font);
	setButtonLayoutData(addResourceTypeButton);
	
	
	removeResourceTypeButton = new Button(groupComponent, SWT.PUSH);
	removeResourceTypeButton.setText(WorkbenchMessages.getString("FileEditorPreference.remove")); //$NON-NLS-1$
	removeResourceTypeButton.addListener(SWT.Selection, this);
	removeResourceTypeButton.setFont(font);
	setButtonLayoutData(removeResourceTypeButton);
	
	//Spacer
	label = new Label(pageComponent, SWT.LEFT);
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.horizontalSpan = 2;
	label.setLayoutData(data);

	// layout the bottom table & its buttons
	editorLabel = new Label(pageComponent, SWT.LEFT);
	editorLabel.setText(WorkbenchMessages.getString("FileEditorPreference.associatedEditors")); //$NON-NLS-1$
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.horizontalSpan = 2;
	editorLabel.setLayoutData(data);
	editorLabel.setFont(font);

	editorTable = new Table(pageComponent, SWT.SINGLE | SWT.BORDER);
	editorTable.addListener(SWT.Selection, this);
	editorTable.addListener(SWT.DefaultSelection, this);
	data = new GridData(GridData.FILL_BOTH);
	data.heightHint = editorTable.getItemHeight()*7;
	editorTable.setLayoutData(data);
	editorTable.setFont(font);
	
	groupComponent = new Composite(pageComponent, SWT.NULL);
	groupLayout = new GridLayout();
	groupLayout.marginWidth = 0;
	groupLayout.marginHeight = 0;
	groupComponent.setLayout(groupLayout);
	data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	groupComponent.setLayoutData(data);
	groupComponent.setFont(font);
	
	addEditorButton = new Button(groupComponent, SWT.PUSH);
	addEditorButton.setText(WorkbenchMessages.getString("FileEditorPreference.addEditor")); //$NON-NLS-1$
	addEditorButton.addListener(SWT.Selection, this);
	addEditorButton.setLayoutData(data);
	addEditorButton.setFont(font);
	setButtonLayoutData(addEditorButton);
	
	removeEditorButton = new Button(groupComponent, SWT.PUSH);
	removeEditorButton.setText(WorkbenchMessages.getString("FileEditorPreference.removeEditor")); //$NON-NLS-1$
	removeEditorButton.addListener(SWT.Selection, this);
	removeEditorButton.setFont(font);
	setButtonLayoutData(removeEditorButton);
	
	defaultEditorButton= new Button(groupComponent, SWT.PUSH);
	defaultEditorButton.setText(WorkbenchMessages.getString("FileEditorPreference.default")); //$NON-NLS-1$
	defaultEditorButton.addListener(SWT.Selection, this);
	defaultEditorButton.setFont(font);
	setButtonLayoutData(defaultEditorButton);

	fillResourceTypeTable();
	if (resourceTypeTable.getItemCount() > 0) {
		resourceTypeTable.setSelection(0);
	}
	fillEditorTable();
	updateEnabledState();

	WorkbenchHelp.setHelp(parent, IHelpContextIds.FILE_EDITORS_PREFERENCE_PAGE);
	
	return pageComponent;
}
/**
 * The preference page is going to be disposed. So deallocate all allocated
 * SWT resources that aren't disposed automatically by disposing the page
 * (i.e fonts, cursors, etc). Subclasses should reimplement this method to 
 * release their own allocated SWT resources.
 */
public void dispose() {
	super.dispose();
	if(imagesToDispose != null) {
		for (Iterator e = imagesToDispose.iterator(); e.hasNext();) {
			((Image)e.next()).dispose();
		}
		imagesToDispose = null;
	}
	if (editorsToImages != null) {
		for (Iterator e = editorsToImages.values().iterator(); e.hasNext();) {
			((Image)e.next()).dispose();
		}
		editorsToImages = null;
	}
}
	
/**
 * Hook method to get a page specific preference store. Reimplement this
 * method if a page don't want to use its parent's preference store.
 */
protected IPreferenceStore doGetPreferenceStore() {
	return WorkbenchPlugin.getDefault().getPreferenceStore();
}
protected void fillEditorTable() {
	editorTable.removeAll();
	FileEditorMapping resourceType = getSelectedResourceType();
	if (resourceType != null) {
		IEditorDescriptor[] array = resourceType.getEditors();
		for (int i=0;i < array.length; i++) { 
			IEditorDescriptor editor = array[i];
			TableItem item = new TableItem(editorTable, SWT.NULL);
			item.setData(editor);
			// Check if it is the default editor
			String defaultString = null;
			FileEditorMapping ext = getSelectedResourceType();
			if (ext != null){
				IEditorDescriptor preferredEditor = ext.getDefaultEditor();
				if (preferredEditor == editor)
					defaultString = WorkbenchMessages.getString("FileEditorPreference.defaultLabel"); //$NON-NLS-1$
			}

			if (defaultString != null) {
				item.setText(editor.getLabel() + " " + defaultString); //$NON-NLS-1$
			}
			else {
				item.setText(editor.getLabel());
			}
			item.setImage(getImage(editor));
		}
	}
}
/**
 * Place the existing resource types in the table
 */
protected void fillResourceTypeTable() {
	// Populate the table with the items
	IFileEditorMapping[] array = WorkbenchPlugin.getDefault().getEditorRegistry().getFileEditorMappings();
	for (int i = 0; i < array.length; i++) {
		FileEditorMapping mapping = (FileEditorMapping) array[i];
		mapping = (FileEditorMapping) mapping.clone(); // want a copy
		newResourceTableItem(mapping, i, false);
	}
}
/**
 * Returns the image associated with the given editor.
 */
protected Image getImage(IEditorDescriptor editor) {
	Image image = (Image)editorsToImages.get(editor);
	if (image == null) {
		image = editor.getImageDescriptor().createImage();
		editorsToImages.put(editor, image);
	}
	return image;
}
protected FileEditorMapping getSelectedResourceType() {
	TableItem[] items = resourceTypeTable.getSelection();
	if (items.length > 0) {
		return (FileEditorMapping)items[0].getData();  //Table is single select
	} else {
		return null;
	}
}
protected IEditorDescriptor[] getAssociatedEditors(){
	if (getSelectedResourceType() == null)
		return null;
	if (editorTable.getItemCount() > 0) {
		ArrayList editorList = new ArrayList();
		for (int i = 0; i < editorTable.getItemCount(); i++)
			editorList.add(editorTable.getItem(i).getData());

		return (IEditorDescriptor[])editorList.toArray(new IEditorDescriptor[editorList.size()]);
	}
	else
		return null;
}
public void handleEvent(Event event) {
	if (event.widget == addResourceTypeButton) {
		promptForResourceType();
	} else if (event.widget == removeResourceTypeButton) {
		removeSelectedResourceType();
	} else if (event.widget == addEditorButton) {
		promptForEditor();
	} else if (event.widget == removeEditorButton) {
		removeSelectedEditor();
	} else if (event.widget == defaultEditorButton) {
		setSelectedEditorAsDefault();
	} else if (event.widget == resourceTypeTable) {
		fillEditorTable();
	}

	updateEnabledState();   
		
}
/**
 * @see IWorkbenchPreferencePage
 */
public void init(IWorkbench aWorkbench){
	this.workbench = aWorkbench;
	noDefaultAndApplyButton();
}
/*
 * Create a new <code>TableItem</code> to represent the resource
 * type editor description supplied.
 */
protected TableItem newResourceTableItem(IFileEditorMapping mapping, int index, boolean selected) {
	Image image = mapping.getImageDescriptor().createImage(false);
	if (image != null)
		imagesToDispose.add(image);
	
	TableItem item = new TableItem(resourceTypeTable, SWT.NULL, index);
	if (image != null) {
		item.setImage(image);
	}
	item.setText(mapping.getLabel());
	item.setData(mapping);
	if (selected) {
		resourceTypeTable.setSelection(index);
	}

	return item;
}
/**
 * This is a hook for sublcasses to do special things when the ok
 * button is pressed.
 * For example reimplement this method if you want to save the 
 * page's data into the preference bundle.
 */
public boolean performOk() {
	TableItem[] items = resourceTypeTable.getItems();
	FileEditorMapping[] resourceTypes = new FileEditorMapping[items.length];
	for (int i = 0; i < items.length; i++) {
		resourceTypes[i] = (FileEditorMapping)(items[i].getData());
	}
	EditorRegistry registry = (EditorRegistry)WorkbenchPlugin.getDefault().getEditorRegistry(); // cast to allow save to be called
	registry.setFileEditorMappings(resourceTypes);
	registry.saveAssociations();
	return true;
}
public void promptForEditor() {
	EditorSelectionDialog dialog = new EditorSelectionDialog(getControl().getShell());
	dialog.setEditorsToFilter(getAssociatedEditors());
	dialog.setMessage(WorkbenchMessages.format("Choose_the_editor_for_file", new Object[] {getSelectedResourceType().getLabel()})); //$NON-NLS-1$
	if (dialog.open() == Dialog.OK) {
		EditorDescriptor editor = (EditorDescriptor)dialog.getSelectedEditor();
		if (editor != null) {
			int i = editorTable.getItemCount();
			boolean isEmpty = i < 1;
			TableItem item = new TableItem(editorTable, SWT.NULL, i);
			item.setData(editor);
			if (isEmpty)
				item.setText(editor.getLabel() + " " + 	WorkbenchMessages.getString("FileEditorPreference.defaultLabel")); //$NON-NLS-2$ //$NON-NLS-1$
			else
				item.setText(editor.getLabel());
			item.setImage(getImage(editor));
			editorTable.setSelection(i);
			editorTable.setFocus();
			getSelectedResourceType().addEditor(editor);
			updateSelectedResourceType(); //in case of new default
		}
	}
}
public void promptForResourceType() {
	FileExtensionDialog dialog = new FileExtensionDialog(getControl().getShell());
	if (dialog.open() == Dialog.OK) {
		String name = dialog.getName();
		String extension = dialog.getExtension();
		addResourceType(name, extension);
	}
}
/**
 * Remove the editor from the table
 */
public void removeSelectedEditor() {
	TableItem[] items = editorTable.getSelection();
	boolean defaultEditor = editorTable.getSelectionIndex() == 0;		
	if (items.length > 0) {
		getSelectedResourceType().removeEditor((EditorDescriptor)items[0].getData());
		items[0].dispose();  //Table is single selection
	}
	if (defaultEditor && editorTable.getItemCount() > 0){
		TableItem item = editorTable.getItem(0);
		if (item != null)
			item.setText(((EditorDescriptor)(item.getData())).getLabel() + " " + 	WorkbenchMessages.getString("FileEditorPreference.defaultLabel"));  //$NON-NLS-2$ //$NON-NLS-1$
	}

}
/**
 * Remove the type from the table
 */
public void removeSelectedResourceType() {
	TableItem[] items = resourceTypeTable.getSelection();
	if (items.length > 0) {
		items[0].dispose();  //Table is single selection
	}
	//Clear out the editors too
	editorTable.removeAll();
}
public void setSelectedEditorAsDefault() {
	TableItem[] items = editorTable.getSelection();
	if (items.length > 0) {
		// First change the label of the old default
		TableItem oldDefaultItem = editorTable.getItem(0);
		oldDefaultItem.setText(((EditorDescriptor)oldDefaultItem.getData()).getLabel());
		// Now set the new default
		EditorDescriptor editor = (EditorDescriptor)items[0].getData();
		getSelectedResourceType().setDefaultEditor(editor);
		items[0].dispose();  //Table is single selection
		TableItem item = new TableItem(editorTable, SWT.NULL, 0);
		item.setData(editor);
		item.setText(editor.getLabel() + " " + 	WorkbenchMessages.getString("FileEditorPreference.defaultLabel")); //$NON-NLS-2$ //$NON-NLS-1$
		item.setImage(getImage(editor));
		editorTable.setSelection(new TableItem[] {item});
	}
}
public void updateEnabledState() {
	//Update enabled state
	boolean resourceTypeSelected = resourceTypeTable.getSelectionIndex() != -1;
	boolean editorSelected = editorTable.getSelectionIndex() != -1;

	removeResourceTypeButton.setEnabled(resourceTypeSelected);
	editorLabel.setEnabled(resourceTypeSelected);
	addEditorButton.setEnabled(resourceTypeSelected);
	removeEditorButton.setEnabled(editorSelected);
	defaultEditorButton.setEnabled(editorSelected);
}
public void updateSelectedResourceType() {
//  TableItem item = resourceTypeTable.getSelection()[0]; //Single select
//  Image image = ((IFileEditorMapping)item.getData()).getImageDescriptor().getImage();
//  imagesToDispose.addElement(image);
//  item.setImage(image);
}
}
