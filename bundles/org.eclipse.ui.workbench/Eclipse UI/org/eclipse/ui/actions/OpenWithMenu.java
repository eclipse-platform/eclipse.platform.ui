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
package org.eclipse.ui.actions;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.internal.misc.Sorter;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;

/**
 * A menu for opening files in the workbench.
 * <p>
 * An <code>OpenWithMenu</code> is used to populate a menu with
 * "Open With" actions.  One action is added for each editor which is applicable
 * to the selected file. If the user selects one of these items, the corresponding
 * editor is opened on the file.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */ 
public class OpenWithMenu extends ContributionItem {
	private IWorkbenchPage page;
	private IAdaptable file;
	private EditorRegistry registry = (EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry();

	private static Hashtable imageCache = new Hashtable(11);
	 
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenWithMenu";//$NON-NLS-1$


	private Sorter sorter = new Sorter() {
		private Collator collator = Collator.getInstance();
		
		public boolean compare(Object o1, Object o2) {
			String s1 = ((IEditorDescriptor)o1).getLabel();
			String s2 = ((IEditorDescriptor)o2).getLabel();
			//Return true if elementTwo is 'greater than' elementOne
			return collator.compare(s2, s1) > 0;
		}
	};
/**
 * Constructs a new instance of <code>OpenWithMenu</code>. 
 * <p>
 * If this method is used be sure to set the selected file by invoking
 * <code>setFile</code>.  The file input is required when the user
 * selects an item in the menu.  At that point the menu will attempt to
 * open an editor with the file as its input.
 * </p>
 *
 * @param page the page where the editor is opened if an item within
 *		the menu is selected
 */
public OpenWithMenu(IWorkbenchPage page) {
	this(page, null);
}
/**
 * Constructs a new instance of <code>OpenWithMenu</code>.  
 *
 * @param window the window where a new page is created if an item within
 *		the menu is selected
 * @param file the selected file
 */
public OpenWithMenu(IWorkbenchPage page, IAdaptable file) {
	super(ID);
	this.page = page;
	this.file = file;
}
/**
 * Returns an image to show for the corresponding editor descriptor.
 *
 * @param editorDesc the editor descriptor, or null for the system editor
 * @return the image or null
 */
private Image getImage(IEditorDescriptor editorDesc) {
	ImageDescriptor imageDesc = getImageDescriptor(editorDesc);
	if (imageDesc == null) {
		return null;
	}
	Image image = (Image) imageCache.get(imageDesc);
	if (image == null) {
		image = imageDesc.createImage();
		imageCache.put(imageDesc, image);
	}
	return image;
}


/**
 * Returns the image descriptor for the given editor descriptor,
 * or null if it has no image.
 */
private ImageDescriptor getImageDescriptor(IEditorDescriptor editorDesc) {
	ImageDescriptor imageDesc = null;
	if (editorDesc == null) {
		imageDesc = registry.getImageDescriptor(getFileResource());
	}
	else {
		imageDesc = editorDesc.getImageDescriptor();
	}
	if (imageDesc == null) {
		if (editorDesc.getId().equals(IWorkbenchConstants.SYSTEM_EDITOR_ID))
			imageDesc = registry.getSystemEditorImageDescriptor(getFileResource());
	}
	return imageDesc;
}
/**
 * Creates the menu item for the editor descriptor.
 *
 * @param menu the menu to add the item to
 * @param descriptor the editor descriptor, or null for the system editor
 * @param preferredEditor the descriptor of the preferred editor, or <code>null</code>
 */
private void createMenuItem(Menu menu, final IEditorDescriptor descriptor, final IEditorDescriptor preferredEditor) {
	// XXX: Would be better to use bold here, but SWT does not support it.
	final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
	boolean isPreferred = preferredEditor != null && descriptor.getId().equals(preferredEditor.getId());
	menuItem.setSelection(isPreferred);
	menuItem.setText(descriptor.getLabel());
	Image image = getImage(descriptor);
	if (image != null) {
		menuItem.setImage(image);
	}
	Listener listener = new Listener() {
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Selection:
					if(menuItem.getSelection())
						openEditor(descriptor);
					break;
			}
		}
	};
	menuItem.addListener(SWT.Selection, listener);
}
/* (non-Javadoc)
 * Fills the menu with perspective items.
 */
public void fill(Menu menu, int index) {
	IFile file = getFileResource();
	if (file == null) {
		return;
	}

	IEditorDescriptor defaultEditor = registry.getDefaultEditor(); // should not be null
	IEditorDescriptor preferredEditor = registry.getDefaultEditor(file); // may be null
	
	Object[] editors = sorter.sort(registry.getEditors(file));
	boolean defaultFound = false;
	
	//Check that we don't add it twice. This is possible
	//if the same editor goes to two mappings.
	ArrayList alreadyMapped= new ArrayList();

	for (int i = 0; i < editors.length; i++) {
		IEditorDescriptor editor = (IEditorDescriptor) editors[i];
		if(!alreadyMapped.contains(editor)){
			createMenuItem(menu, editor, preferredEditor);
			if (defaultEditor != null && editor.getId().equals(defaultEditor.getId()))
				defaultFound = true;
			alreadyMapped.add(editor);
		}		
	}

	// Only add a separator if there is something to separate
	if (editors.length > 0)
		new MenuItem(menu, SWT.SEPARATOR);

	// Add default editor. Check it if it is saved as the preference.
	if (!defaultFound && defaultEditor != null) {
		createMenuItem(menu, defaultEditor, preferredEditor);
	}

	// Add system editor.
	IEditorDescriptor descriptor = EditorDescriptor.getSystemEditorDescriptor();
	createMenuItem(menu, descriptor, preferredEditor);
	createDefaultMenuItem(menu, file);
}
/**
 * Converts the IAdaptable file to IFile or null.
 */
private IFile getFileResource() {
	if (this.file instanceof IFile) {
		return (IFile) this.file;
	}
	else {
		IResource resource = (IResource) this.file.getAdapter(IResource.class);
		if (resource instanceof IFile) {
			return (IFile) resource;
		}
	}
	return null;
}
/* (non-Javadoc)
 * Returns whether this menu is dynamic.
 */
public boolean isDynamic() {
	return true;
}
/**
 * Opens the given editor on the selected file.
 *
 * @param editor the editor descriptor, or null for the system editor
 */
private void openEditor(IEditorDescriptor editor) {
	IFile file = getFileResource();
	try {
		if (editor == null) {
			page.openSystemEditor(file);
		} else {
			page.openEditor(file, editor.getId());
		}
	} catch (PartInitException e) {
		DialogUtil.openError(
			page.getWorkbenchWindow().getShell(),
			WorkbenchMessages.getString("OpenWithMenu.dialogTitle"), //$NON-NLS-1$
			e.getMessage(),
			e);
	}
}

/**
 * Creates the menu item for clearing the current selection.
 *
 * @param menu the menu to add the item to
 * @param file the file bing edited
 * @param registry the editor registry
 */
private void createDefaultMenuItem(Menu menu, final IFile file) {
	final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
	menuItem.setSelection(registry.getDefaultEditor(file) == null);
	menuItem.setText(WorkbenchMessages.getString("DefaultEditorDescription.name")); //$NON-NLS-1$
	
	Listener listener = new Listener() {
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Selection:
					if(menuItem.getSelection()) {
						registry.setDefaultEditor(file,null);
						try{
							page.openEditor(file);
						} catch (PartInitException e) {
							DialogUtil.openError(
								page.getWorkbenchWindow().getShell(),
								WorkbenchMessages.getString("OpenWithMenu.dialogTitle"), //$NON-NLS-1$
								e.getMessage(),
								e);
						}
					}
					break;
			}
		}
	};
	
	menuItem.addListener(SWT.Selection, listener);
}
}
