/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla -	Bug 29633 [EditorMgmt] "Open" menu should
 *     						have Open With-->Other
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.EditorSelectionDialog;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The file editors page presents the collection of file names and extensions
 * for which the user has registered editors. It also lets the user add new
 * internal or external (program) editors for a given file name and extension.
 *
 * The user can add an editor for either a specific file name and extension
 * (e.g. report.doc), or for all file names of a given extension (e.g. *.doc)
 *
 * The set of registered editors is tracked by the EditorRegistery available
 * from the workbench plugin.
 */
public class FileEditorsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, Listener {

	private static final String DATA_EDITOR = "editor"; //$NON-NLS-1$

	private static final String DATA_FROM_CONTENT_TYPE = "type"; //$NON-NLS-1$

	protected Table resourceTypeTable;

	protected Button addResourceTypeButton;

	protected Button removeResourceTypeButton;

	protected Table editorTable;

	protected Button addEditorButton;

	protected Button removeEditorButton;

	protected Button defaultEditorButton;

	protected Label editorLabel;

	protected IWorkbench workbench;

	protected List<Image> imagesToDispose;

	protected Map<IEditorDescriptor, Image> editorsToImages;

	/**
	 * Add a new resource type to the collection shown in the top of the page. This
	 * is typically called after the extension dialog is shown to the user.
	 *
	 * @param newName      the new name
	 * @param newExtension the new extension
	 */
	public void addResourceType(String newName, String newExtension) {
		// Either a file name or extension must be provided
		Assert.isTrue(
				(newName != null && newName.length() != 0) || (newExtension != null && newExtension.length() != 0));

		// Wild card only valid by itself (i.e. rep* is not valid)
		// And must have an extension
		int index = newName.indexOf('*');
		if (index > -1) {
			Assert.isTrue(index == 0 && newName.length() == 1);
			Assert.isTrue(newExtension != null && newExtension.length() != 0);
		}

		// Find the index at which to insert the new entry.
		String newFilename = (newName + (newExtension == null || newExtension.isEmpty() ? "" : "." + newExtension)) //$NON-NLS-1$ //$NON-NLS-2$
				.toUpperCase();
		IFileEditorMapping resourceType;
		TableItem[] items = resourceTypeTable.getItems();
		boolean found = false;
		int i = 0;

		while (i < items.length && !found) {
			resourceType = (IFileEditorMapping) items[i].getData();
			int result = newFilename.compareToIgnoreCase(resourceType.getLabel());
			if (result == 0) {
				// Same resource type not allowed!
				MessageBox msgBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
				msgBox.setMessage(WorkbenchMessages.FileEditorPreference_existsMessage);
				msgBox.setText(WorkbenchMessages.FileEditorPreference_existsTitle);
				msgBox.open();
				return;
			}

			if (result < 0) {
				found = true;
			} else {
				i++;
			}
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
	@Override
	protected Control createContents(Composite parent) {
		imagesToDispose = new ArrayList<>();
		editorsToImages = new HashMap<>(50);

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

		// layout the contents

		PreferenceLinkArea contentTypeArea = new PreferenceLinkArea(pageComponent, SWT.NONE,
				"org.eclipse.ui.preferencePages.ContentTypes", //$NON-NLS-1$
				WorkbenchMessages.FileEditorPreference_contentTypesRelatedLink, (IWorkbenchPreferenceContainer) getContainer(), null);

		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		contentTypeArea.getControl().setLayoutData(data);

		// layout the top table & its buttons
		Label label = new Label(pageComponent, SWT.LEFT);
		label.setText(WorkbenchMessages.FileEditorPreference_fileTypes);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		resourceTypeTable = new Table(pageComponent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		resourceTypeTable.addListener(SWT.Selection, this);
		resourceTypeTable.addListener(SWT.DefaultSelection, this);
		data = new GridData(GridData.FILL_HORIZONTAL);

		int availableRows = DialogUtil.availableRows(pageComponent);

		data.heightHint = resourceTypeTable.getItemHeight() * (availableRows / 8);
		resourceTypeTable.setLayoutData(data);

		Composite groupComponent = new Composite(pageComponent, SWT.NULL);
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		groupComponent.setLayout(groupLayout);
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		groupComponent.setLayoutData(data);

		addResourceTypeButton = new Button(groupComponent, SWT.PUSH);
		addResourceTypeButton.setText(WorkbenchMessages.FileEditorPreference_add);
		addResourceTypeButton.addListener(SWT.Selection, this);
		setButtonLayoutData(addResourceTypeButton);

		removeResourceTypeButton = new Button(groupComponent, SWT.PUSH);
		removeResourceTypeButton.setText(WorkbenchMessages.FileEditorPreference_remove);
		removeResourceTypeButton.addListener(SWT.Selection, this);
		setButtonLayoutData(removeResourceTypeButton);

		// Spacer
		label = new Label(pageComponent, SWT.LEFT);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		// layout the bottom table & its buttons
		editorLabel = new Label(pageComponent, SWT.LEFT);
		editorLabel.setText(WorkbenchMessages.FileEditorPreference_associatedEditors);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		editorLabel.setLayoutData(data);

		editorTable = new Table(pageComponent, SWT.MULTI | SWT.BORDER);
		editorTable.addListener(SWT.Selection, this);
		editorTable.addListener(SWT.DefaultSelection, this);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = editorTable.getItemHeight() * 7;
		editorTable.setLayoutData(data);

		groupComponent = new Composite(pageComponent, SWT.NULL);
		groupLayout = new GridLayout();
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		groupComponent.setLayout(groupLayout);
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		groupComponent.setLayoutData(data);

		addEditorButton = new Button(groupComponent, SWT.PUSH);
		addEditorButton.setText(WorkbenchMessages.FileEditorPreference_addEditor);
		addEditorButton.addListener(SWT.Selection, this);
		addEditorButton.setLayoutData(data);
		setButtonLayoutData(addEditorButton);

		removeEditorButton = new Button(groupComponent, SWT.PUSH);
		removeEditorButton.setText(WorkbenchMessages.FileEditorPreference_removeEditor);
		removeEditorButton.addListener(SWT.Selection, this);
		setButtonLayoutData(removeEditorButton);

		defaultEditorButton = new Button(groupComponent, SWT.PUSH);
		defaultEditorButton.setText(WorkbenchMessages.FileEditorPreference_default);
		defaultEditorButton.addListener(SWT.Selection, this);
		setButtonLayoutData(defaultEditorButton);

		fillResourceTypeTable();
		if (resourceTypeTable.getItemCount() > 0) {
			resourceTypeTable.setSelection(0);
		}
		fillEditorTable();
		updateEnabledState();

		workbench.getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.FILE_EDITORS_PREFERENCE_PAGE);
		applyDialogFont(pageComponent);

		return pageComponent;
	}

	/**
	 * The preference page is going to be disposed. So deallocate all allocated SWT
	 * resources that aren't disposed automatically by disposing the page (i.e
	 * fonts, cursors, etc). Subclasses should reimplement this method to release
	 * their own allocated SWT resources.
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (imagesToDispose != null) {
			for (Image image : imagesToDispose) {
				image.dispose();
			}
			imagesToDispose = null;
		}
		if (editorsToImages != null) {
			for (Image image : editorsToImages.values()) {
				image.dispose();
			}
			editorsToImages = null;
		}
	}

	/**
	 * Hook method to get a page specific preference store. Reimplement this method
	 * if a page don't want to use its parent's preference store.
	 */
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	protected void fillEditorTable() {
		editorTable.removeAll();
		FileEditorMapping resourceType = getSelectedResourceType();
		if (resourceType != null) {
			for (IEditorDescriptor editor : resourceType.getEditors()) {
				TableItem item = new TableItem(editorTable, SWT.NULL);
				item.setData(DATA_EDITOR, editor);
				// Check if it is the default editor
				String defaultString = null;
				if (resourceType.getDefaultEditor() == editor && resourceType.isDeclaredDefaultEditor(editor)) {
					defaultString = WorkbenchMessages.FileEditorPreference_defaultLabel;
				}

				if (defaultString != null) {
					item.setText(editor.getLabel() + " " + defaultString); //$NON-NLS-1$
				} else {
					item.setText(editor.getLabel());
				}
				item.setImage(getImage(editor));
			}

			// now add any content type editors
			EditorRegistry registry = (EditorRegistry) WorkbenchPlugin.getDefault().getEditorRegistry();
			IContentType[] contentTypes = Platform.getContentTypeManager().findContentTypesFor(resourceType.getLabel());
			for (IContentType contentType : contentTypes) {
				for (IEditorDescriptor editor : registry.getEditorsForContentType(contentType)) {
					// don't add duplicates
					TableItem[] items = editorTable.getItems();
					TableItem foundItem = null;
					for (TableItem item : items) {
						if (item.getData(DATA_EDITOR).equals(editor)) {
							foundItem = item;
							break;
						}
					}
					if (foundItem == null) {
						TableItem item = new TableItem(editorTable, SWT.NULL);
						item.setData(DATA_EDITOR, editor);
						item.setData(DATA_FROM_CONTENT_TYPE, contentType);
						setLockedItemText(item, editor.getLabel());
						item.setImage(getImage(editor));
					} else { // update the item to reflect its origin
						foundItem.setData(DATA_FROM_CONTENT_TYPE, contentType);
						setLockedItemText(foundItem, foundItem.getText());
					}
				}
			}

		}
	}

	/**
	 * Set the locked message on the item. Assumes the item has an instance of
	 * IContentType in the data map.
	 *
	 * @param item      the item to set
	 * @param baseLabel the base label
	 */
	private void setLockedItemText(TableItem item, String baseLabel) {
		item.setText(NLS.bind(WorkbenchMessages.FileEditorPreference_isLocked, baseLabel,
				((IContentType) item.getData(DATA_FROM_CONTENT_TYPE)).getName()));
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
		Image image = editorsToImages.get(editor);
		if (image == null) {
			image = editor.getImageDescriptor().createImage();
			editorsToImages.put(editor, image);
		}
		return image;
	}

	protected FileEditorMapping getSelectedResourceType() {
		TableItem[] items = resourceTypeTable.getSelection();
		if (items.length == 1) {
			return (FileEditorMapping) items[0].getData();
		}
		return null;
	}

	protected IEditorDescriptor[] getAssociatedEditors() {
		if (getSelectedResourceType() == null) {
			return null;
		}
		if (editorTable.getItemCount() > 0) {
			ArrayList<IEditorDescriptor> editorList = new ArrayList<>();
			for (int i = 0; i < editorTable.getItemCount(); i++) {
				editorList.add((IEditorDescriptor) editorTable.getItem(i).getData(DATA_EDITOR));
			}

			return editorList.toArray(new IEditorDescriptor[editorList.size()]);
		}
		return null;
	}

	@Override
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
	@Override
	public void init(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
		noDefaultAndApplyButton();
	}

	/*
	 * Create a new <code>TableItem</code> to represent the resource type editor
	 * description supplied.
	 */
	protected TableItem newResourceTableItem(IFileEditorMapping mapping, int index, boolean selected) {
		Image image = mapping.getImageDescriptor().createImage(false);
		if (image != null) {
			imagesToDispose.add(image);
		}

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
	 * This is a hook for sublcasses to do special things when the ok button is
	 * pressed. For example reimplement this method if you want to save the page's
	 * data into the preference bundle.
	 */
	@Override
	public boolean performOk() {
		TableItem[] items = resourceTypeTable.getItems();
		FileEditorMapping[] resourceTypes = new FileEditorMapping[items.length];
		for (int i = 0; i < items.length; i++) {
			resourceTypes[i] = (FileEditorMapping) (items[i].getData());
		}
		EditorRegistry registry = (EditorRegistry) WorkbenchPlugin.getDefault().getEditorRegistry(); // cast to allow
																										// save to be
																										// called
		registry.setFileEditorMappings(resourceTypes);
		registry.saveAssociations();

		PrefUtil.savePrefs();
		return true;
	}

	/**
	 * Prompt for editor.
	 */
	public void promptForEditor() {
		EditorSelectionDialog dialog = new EditorSelectionDialog(getControl().getShell());
		dialog.setEditorsToFilter(getAssociatedEditors());
		dialog.setMessage(NLS.bind(WorkbenchMessages.Choose_the_editor_for_file, getSelectedResourceType().getLabel()));
		if (dialog.open() == Window.OK) {
			EditorDescriptor editor = (EditorDescriptor) dialog.getSelectedEditor();
			if (editor != null) {
				int i = editorTable.getItemCount();
				boolean isEmpty = i < 1;
				TableItem item = new TableItem(editorTable, SWT.NULL, i);
				item.setData(DATA_EDITOR, editor);
				if (isEmpty) {
					item.setText(editor.getLabel() + " " + WorkbenchMessages.FileEditorPreference_defaultLabel); //$NON-NLS-1$
				} else {
					item.setText(editor.getLabel());
				}
				item.setImage(getImage(editor));
				editorTable.setSelection(i);
				editorTable.setFocus();
				getSelectedResourceType().addEditor(editor);
				if (isEmpty) {
					getSelectedResourceType().setDefaultEditor(editor);
				}
				updateSelectedResourceType(); // in case of new default
			}
		}
	}

	/**
	 * Prompt for resource type.
	 */
	public void promptForResourceType() {
		FileExtensionDialog dialog = new FileExtensionDialog(getControl().getShell(),
				WorkbenchMessages.FileExtension_shellTitle, IWorkbenchHelpContextIds.FILE_EXTENSION_DIALOG,
				WorkbenchMessages.FileExtension_dialogTitle, WorkbenchMessages.FileExtension_fileTypeMessage,
				WorkbenchMessages.FileExtension_fileTypeLabel);
		if (dialog.open() == Window.OK) {
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
			for (TableItem item : items) {
				getSelectedResourceType().removeEditor((EditorDescriptor) item.getData(DATA_EDITOR));
				item.dispose();
			}
		}
		if (defaultEditor && editorTable.getItemCount() > 0) {
			TableItem item = editorTable.getItem(0);
			// explicitly set the first editor as the default
			getSelectedResourceType().setDefaultEditor((EditorDescriptor) item.getData(DATA_EDITOR));
			item.setText(((EditorDescriptor) (item.getData(DATA_EDITOR))).getLabel() + " " //$NON-NLS-1$
					+ WorkbenchMessages.FileEditorPreference_defaultLabel);
			if (!isEditorRemovable(item)) {
				setLockedItemText(item, item.getText());
			}
		}

	}

	/**
	 * Remove the type from the table
	 */
	public void removeSelectedResourceType() {
		for (TableItem item : resourceTypeTable.getSelection()) {
			item.dispose();
		}
		// Clear out the editors too
		editorTable.removeAll();
	}

	/**
	 * Add the selected editor to the default list.
	 */
	public void setSelectedEditorAsDefault() {
		TableItem[] items = editorTable.getSelection();
		if (items.length > 0) {
			// First change the label of the old default
			TableItem oldDefaultItem = editorTable.getItem(0);
			oldDefaultItem.setText(((EditorDescriptor) oldDefaultItem.getData(DATA_EDITOR)).getLabel());
			// update the label to reflect the locked state
			if (!isEditorRemovable(oldDefaultItem)) {
				setLockedItemText(oldDefaultItem, oldDefaultItem.getText());
			}
			// Now set the new default
			EditorDescriptor editor = (EditorDescriptor) items[0].getData(DATA_EDITOR);
			getSelectedResourceType().setDefaultEditor(editor);
			IContentType fromContentType = (IContentType) items[0].getData(DATA_FROM_CONTENT_TYPE);
			items[0].dispose(); // Table is single selection
			TableItem item = new TableItem(editorTable, SWT.NULL, 0);
			item.setData(DATA_EDITOR, editor);
			if (fromContentType != null) {
				item.setData(DATA_FROM_CONTENT_TYPE, fromContentType);
			}
			item.setText(editor.getLabel() + " " + WorkbenchMessages.FileEditorPreference_defaultLabel); //$NON-NLS-1$
			item.setImage(getImage(editor));
			if (!isEditorRemovable(item)) {
				setLockedItemText(item, item.getText());
			}
			editorTable.setSelection(new TableItem[] { item });
		}
	}

	/**
	 * Update the enabled state.
	 */
	public void updateEnabledState() {
		// Update enabled state
		int selectedResources = resourceTypeTable.getSelectionCount();
		int selectedEditors = editorTable.getSelectionCount();

		removeResourceTypeButton.setEnabled(selectedResources != 0);
		editorLabel.setEnabled(selectedResources == 1);
		addEditorButton.setEnabled(selectedResources == 1);
		removeEditorButton.setEnabled(areEditorsRemovable());
		defaultEditorButton.setEnabled(selectedEditors == 1);
	}

	/**
	 * Return whether the selected editors are removable. An editor is removable if
	 * it was not submitted via a content-type binding.
	 *
	 * @return whether all the selected editors are removable or not
	 * @since 3.1
	 */
	private boolean areEditorsRemovable() {
		TableItem[] items = editorTable.getSelection();
		if (items.length == 0) {
			return false;
		}

		for (TableItem item : items) {
			if (!isEditorRemovable(item)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Return whether the given editor is removable. An editor is removable if it is
	 * not submitted via a content-type binding.
	 *
	 * @param item the item to test
	 * @return whether the selected editor is removable
	 * @since 3.1
	 */
	private boolean isEditorRemovable(TableItem item) {
		IContentType fromContentType = (IContentType) item.getData(DATA_FROM_CONTENT_TYPE);
		return fromContentType == null;
	}

	/**
	 * Update the selected type.
	 */
	public void updateSelectedResourceType() {
		// TableItem item = resourceTypeTable.getSelection()[0]; //Single select
		// Image image =
		// ((IFileEditorMapping)item.getData()).getImageDescriptor().getImage();
		// imagesToDispose.addElement(image);
		// item.setImage(image);
	}
}
