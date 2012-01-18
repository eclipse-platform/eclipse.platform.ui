/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla -	Bug 29633 [EditorMgmt] "Open" menu should
 *     						have Open With-->Other
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.EditorSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.icu.text.Collator;

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
 * @noextend This class is not intended to be subclassed by clients.
 */
public class OpenWithMenu extends ContributionItem {
    private IWorkbenchPage page;

    private IAdaptable file;

    private IEditorRegistry registry = PlatformUI.getWorkbench()
            .getEditorRegistry();

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID + ".OpenWithMenu";//$NON-NLS-1$

    /**
     * Match both the input and id, so that different types of editor can be opened on the same input.
     */
    private static final int MATCH_BOTH = IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID;
    
    /*
     * Compares the labels from two IEditorDescriptor objects
     */
    private static final Comparator comparer = new Comparator() {
        private Collator collator = Collator.getInstance();

        public int compare(Object arg0, Object arg1) {
            String s1 = ((IEditorDescriptor) arg0).getLabel();
            String s2 = ((IEditorDescriptor) arg1).getLabel();
            return collator.compare(s1, s2);
        }
    };

    /**
     * Constructs a new instance of <code>OpenWithMenu</code>.
     *
     * @param page the page where the editor is opened if an item within
     *		the menu is selected
     * @deprecated As there is no way to set the file with this constructor use a
     * different constructor.
     */
    public OpenWithMenu(IWorkbenchPage page) {
        this(page, null);
    }

    /**
     * Constructs a new instance of <code>OpenWithMenu</code>.
     *
     * @param page the page where the editor is opened if an item within
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
        return IDEWorkbenchPlugin.getDefault().getResourceManager().createImage(imageDesc);
    }

    /**
     * Returns the image descriptor for the given editor descriptor,
     * or null if it has no image.
     */
    private ImageDescriptor getImageDescriptor(IEditorDescriptor editorDesc) {
        ImageDescriptor imageDesc = null;
        if (editorDesc == null) {
            imageDesc = registry
                    .getImageDescriptor(getFileResource().getName());
			//TODO: is this case valid, and if so, what are the implications for content-type editor bindings?
        } else {
            imageDesc = editorDesc.getImageDescriptor();
        }
        if (imageDesc == null) {
            if (editorDesc.getId().equals(
                    IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID)) {
				imageDesc = registry
                        .getSystemExternalEditorImageDescriptor(getFileResource()
                                .getName());
			}
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
    private void createMenuItem(Menu menu, final IEditorDescriptor descriptor,
            final IEditorDescriptor preferredEditor) {
        // XXX: Would be better to use bold here, but SWT does not support it.
        final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
        boolean isPreferred = preferredEditor != null
                && descriptor.getId().equals(preferredEditor.getId());
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
                    if (menuItem.getSelection()) {
						openEditor(descriptor, false);
					}
                    break;
                }
            }
        };
        menuItem.addListener(SWT.Selection, listener);
    }

    /**
     * Creates the Other... menu item
     *
     * @param menu the menu to add the item to
     */
    private void createOtherMenuItem(final Menu menu) {
    	final IFile fileResource = getFileResource();
		if (fileResource == null) {
    		return;
    	}
        new MenuItem(menu, SWT.SEPARATOR);
        final MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(IDEWorkbenchMessages.OpenWithMenu_Other);
        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Selection:
                   	EditorSelectionDialog dialog = new EditorSelectionDialog(
							menu.getShell());
					dialog
							.setMessage(NLS
									.bind(
											IDEWorkbenchMessages.OpenWithMenu_OtherDialogDescription,
											fileResource.getName()));
					if (dialog.open() == Window.OK) {
						IEditorDescriptor editor = dialog.getSelectedEditor();
						if (editor != null) {
							openEditor(editor, editor.isOpenExternal());
						}
					}
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
		final IFile file= getFileResource();
        if (file == null) {
            return;
        }

		IContentType contentType= IDE.getContentType(file);
		FileEditorInput editorInput= new FileEditorInput(file);

        IEditorDescriptor defaultEditor = registry
                .findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID); // may be null
		final IEditorDescriptor preferredEditor= IDE.getDefaultEditor(file); // may be null

		IEditorDescriptor[] editors= registry.getEditors(file.getName(), contentType);

		editors= IDE.overrideEditorAssociations(editorInput, contentType, editors);

        Collections.sort(Arrays.asList(editors), comparer);

        boolean defaultFound = false;

        //Check that we don't add it twice. This is possible
        //if the same editor goes to two mappings.
        ArrayList alreadyMapped = new ArrayList();

        for (int i = 0; i < editors.length; i++) {
			IEditorDescriptor editor= editors[i];
            if (!alreadyMapped.contains(editor)) {
                createMenuItem(menu, editor, preferredEditor);
                if (defaultEditor != null
                        && editor.getId().equals(defaultEditor.getId())) {
					defaultFound = true;
				}
                alreadyMapped.add(editor);
            }
        }

        // Only add a separator if there is something to separate
        if (editors.length > 0) {
			new MenuItem(menu, SWT.SEPARATOR);
		}

        // Add default editor. Check it if it is saved as the preference.
        if (!defaultFound && defaultEditor != null) {
            createMenuItem(menu, defaultEditor, preferredEditor);
        }

        // Add system editor (should never be null)
        IEditorDescriptor descriptor = registry
                .findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
        createMenuItem(menu, descriptor, preferredEditor);

        // Add system in-place editor (can be null)
        descriptor = registry
                .findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
        if (descriptor != null) {
            createMenuItem(menu, descriptor, preferredEditor);
        }
		createDefaultMenuItem(menu, file, preferredEditor == null);
        
        // add Other... menu item
        createOtherMenuItem(menu);
    }
	

    /**
     * Converts the IAdaptable file to IFile or null.
     */
    private IFile getFileResource() {
        if (this.file instanceof IFile) {
            return (IFile) this.file;
        }
        IResource resource = (IResource) this.file
                .getAdapter(IResource.class);
        if (resource instanceof IFile) {
            return (IFile) resource;
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
     * @param editorDescriptor the editor descriptor, or null for the system editor
     * @param openUsingDescriptor use the descriptor's editor ID for opening if false (normal case),
     * or use the descriptor itself if true (needed to fix bug 178235).
     *
     * @since 3.5
     */
	protected void openEditor(IEditorDescriptor editorDescriptor, boolean openUsingDescriptor) {
        IFile file = getFileResource();
        if (file == null) {
            return;
        }
        try {
        	if (openUsingDescriptor) {
        		((WorkbenchPage) page).openEditorFromDescriptor(new FileEditorInput(file), editorDescriptor, true, null);
        	} else {
	            String editorId = editorDescriptor == null ? IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID
	                    : editorDescriptor.getId();
	            
	            page.openEditor(new FileEditorInput(file), editorId, true, MATCH_BOTH);
	            // only remember the default editor if the open succeeds
	            IDE.setDefaultEditor(file, editorId);
        	}
        } catch (PartInitException e) {
            DialogUtil.openError(page.getWorkbenchWindow().getShell(),
                    IDEWorkbenchMessages.OpenWithMenu_dialogTitle,
                    e.getMessage(), e);
        }
    }

    /**
	 * Creates the menu item for clearing the current selection.
	 * 
	 * @param menu the menu to add the item to
	 * @param file the file being edited
	 * @param markAsSelected <code>true</code> if the item should marked as selected
	 */
	private void createDefaultMenuItem(Menu menu, final IFile file, boolean markAsSelected) {
        final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setSelection(markAsSelected);
        menuItem.setText(IDEWorkbenchMessages.DefaultEditorDescription_name);

        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Selection:
                    if (menuItem.getSelection()) {
                        IDE.setDefaultEditor(file, null);
                        try {
                            openEditor(IDE.getEditorDescriptor(file), false);
                        } catch (PartInitException e) {
                            DialogUtil.openError(page.getWorkbenchWindow()
                                    .getShell(), IDEWorkbenchMessages.OpenWithMenu_dialogTitle,
                                    e.getMessage(), e);
                        }
                    }
                    break;
                }
            }
        };

        menuItem.addListener(SWT.Selection, listener);
    }
}
