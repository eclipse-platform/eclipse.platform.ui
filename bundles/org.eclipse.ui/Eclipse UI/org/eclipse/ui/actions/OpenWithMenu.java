package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.text.Collator;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.Sorter;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

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
	private Listener listener = new Listener() {
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Selection:
					IEditorDescriptor editorDesc = (IEditorDescriptor) event.item.getData();
					openEditor(editorDesc);
					break;
			}
		}
	};

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
 * @param isPreferredEditor whether the editor descriptor is the preferred one for the selected file
 */
private void createMenuItem(Menu menu, IEditorDescriptor descriptor, boolean isPreferredEditor) {
	// XXX: Would be better to use bold here, but SWT does not support it.
	MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
	menuItem.setSelection(isPreferredEditor);
	menuItem.setText(descriptor.getLabel());
	Image image = getImage(descriptor);
	if (image != null) {
		menuItem.setImage(image);
	}
	menuItem.setData(descriptor);
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

	IEditorDescriptor defaultEditor = registry.getDefaultEditor();
	IEditorDescriptor preferredEditor = registry.getDefaultEditor(file);
	Object[] editors = sorter.sort(registry.getEditors(file));
	boolean defaultFound = false;

	for (int i = 0; i < editors.length; i++) {
		IEditorDescriptor editor = (IEditorDescriptor) editors[i];
		createMenuItem(menu, editor, editor.getId().equals(preferredEditor.getId()));
		if (editor.getId().equals(defaultEditor.getId()))
			defaultFound = true;
	}

	//Only add a separator if there is something to separate
	if(editors.length > 0)
		new MenuItem(menu, SWT.SEPARATOR);

	// Add default editor. Check it if it is saved as the preference.
	boolean isPreferred = false;
	if (!defaultFound) {
		if (preferredEditor != null)
			isPreferred = defaultEditor.getId().equals(preferredEditor.getId());
		createMenuItem(menu, defaultEditor, isPreferred);
	}

	// Add system editor.
	IEditorDescriptor descriptor = EditorDescriptor.getSystemEditorDescriptor();
	if (preferredEditor != null)
		isPreferred = descriptor.getId().equals(preferredEditor.getId());
	createMenuItem(menu, descriptor, isPreferred);
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
	MenuItem menuItem;
	if (registry.getDefaultEditor(file) == null) {
		menuItem = new MenuItem(menu, SWT.CHECK);
		menuItem.setSelection(true);
	}
	else {
		menuItem = new MenuItem(menu, SWT.PUSH);
	}
	menuItem.setText(WorkbenchMessages.getString("DefaultEditorDescription.name"));
	
	Listener listener = new Listener() {
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Selection:
					registry.setDefaultEditor(file,null);
					try{
						page.openEditor(file);
					}
					catch (PartInitException e) {
						DialogUtil.openError(
							page.getWorkbenchWindow().getShell(),
							WorkbenchMessages.getString("OpenWithMenu.dialogTitle"), //$NON-NLS-1$
							e.getMessage(),
							e);
					}	
					break;
			}
		}
	};
	
	menuItem.addListener(SWT.Selection, listener);
}
}
