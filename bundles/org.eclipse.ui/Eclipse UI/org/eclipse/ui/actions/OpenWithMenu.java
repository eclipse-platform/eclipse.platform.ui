package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
	private Sorter sorter = new Sorter() {
		public boolean compare(Object o1, Object o2) {
			String s1 = ((IEditorDescriptor)o1).getLabel().toUpperCase();
			String s2 = ((IEditorDescriptor)o2).getLabel().toUpperCase();
			//Return true if elementTwo is 'greater than' elementOne
			return s2.compareTo(s1) > 0;
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
	super("OpenWithMenu");//$NON-NLS-1$
	this.page = page;
	this.file = file;
}
/**
 * Creates an image to show for the corresponding image descriptor.
 *
 * @param descriptor the editor descriptor, or null for the system editor
 * @return a newly created image
 */
private Image createImage(IEditorDescriptor descriptor) {
	ImageDescriptor imageDesc = null;
	if (descriptor == null) {
		imageDesc = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(getFileResource());
	}
	else {
		imageDesc = descriptor.getImageDescriptor();
	}
	if (imageDesc == null) {
		if (descriptor.getId().equals(IWorkbenchConstants.SYSTEM_EDITOR_ID))
			imageDesc = ((EditorRegistry)PlatformUI.getWorkbench().getEditorRegistry()).getSystemEditorImageDescriptor(getFileResource());
		if (imageDesc == null)
			return null;
	}
	return imageDesc.createImage();
}
/**
 * Creates the menu item for the editor descriptor.
 *
 * @param menu the menu to add the item to
 * @param descriptor the editor descriptor, or null for the system editor
 * @param isPreferredEditor whether the editor descriptor is the preferred one for the selected file
 */
private void createMenuItem(Menu menu, final IEditorDescriptor descriptor, boolean isPreferredEditor) {
	MenuItem menuItem;
	if (isPreferredEditor) {
		// XXX: Would be better to use bold here, but SWT does not support it.
		menuItem = new MenuItem(menu, SWT.CHECK);
		menuItem.setSelection(true);
	}
	else {
		menuItem = new MenuItem(menu, SWT.PUSH);
	}
	menuItem.setText(descriptor.getLabel());
	final Image image = createImage(descriptor);
	if (image != null) {
		menuItem.setImage(image);
	}
	Listener listener = new Listener() {
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Selection:
					openEditor(descriptor);
					break;
				case SWT.Dispose:
					if (image != null) {
						image.dispose();
					}
					break;
			}
		}
	};
	menuItem.addListener(SWT.Dispose, listener);
	menuItem.addListener(SWT.Selection, listener);
}
/**
 * Ensures that the contents of the given file resource are local.
 *
 * @param file the file resource
 * @return <code>true</code> if the file is local, and <code>false</code> if
 *   it could not be made local for some reason
 */
private boolean ensureFileLocal(final IFile file) {
	try {
		file.setLocal(true, IResource.DEPTH_ZERO, null);
	} catch (CoreException exception) {
		return false;
	}
	return true;
}
/* (non-Javadoc)
 * Fills the menu with perspective items.
 */
public void fill(Menu menu, int index) {
	IFile file = getFileResource();
	if (file == null) {
		return;
	}

	IEditorRegistry registry =
		page.getWorkbenchWindow().getWorkbench().getEditorRegistry();
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
	if (ensureFileLocal(file)) {
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
}
}
