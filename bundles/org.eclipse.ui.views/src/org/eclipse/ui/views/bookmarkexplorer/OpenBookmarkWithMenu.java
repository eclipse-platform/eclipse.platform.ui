package org.eclipse.ui.views.bookmarkexplorer;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.internal.misc.Sorter;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;

public class OpenBookmarkWithMenu extends OpenWithMenu {
	
	private IWorkbenchPage page;
	private IMarker marker;
	private EditorRegistry registry = (EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry();

	private static Hashtable imageCache = new Hashtable(11);

	private Sorter sorter = new Sorter() {
		private Collator collator = Collator.getInstance();
		
		public boolean compare(Object o1, Object o2) {
			String s1 = ((IEditorDescriptor)o1).getLabel();
			String s2 = ((IEditorDescriptor)o2).getLabel();
			//Return true if elementTwo is 'greater than' elementOne
			return collator.compare(s2, s1) > 0;
		}
	};

	public OpenBookmarkWithMenu(IWorkbenchPage page, IMarker marker) {
		super(page, marker.getResource());
		this.page = page;
		this.marker = marker;
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

	/**
	 * Converts the IAdaptable file to IFile or null.
	 */
	private IFile getFileResource() {
		if (marker == null)
			return null;
		
		IResource resource = marker.getResource();
		if (!(resource instanceof IFile))
			return null;
			
		return (IFile) resource;
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
				IEditorPart activeEditor = page.openEditor(file, editor.getId());
				if (activeEditor != null && !editor.getId().equals(IWorkbenchConstants.SYSTEM_EDITOR_ID))
					activeEditor.gotoMarker(marker);
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
