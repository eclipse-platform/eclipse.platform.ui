/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.views.actions;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.editor.text.AntEditorDocumentProvider;
import org.eclipse.ant.internal.ui.model.AntElementNode;
import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * Code mostly a copy of the OpenWithMenu which cannot be effectively subclassed
 */
public class AntOpenWithMenu extends ContributionItem {

	private IWorkbenchPage page;
	private IAdaptable file;
	private IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
	private static final String SYSTEM_EDITOR_ID= PlatformUI.PLUGIN_ID + ".SystemEditor"; //$NON-NLS-1$

	private static Map imageCache = new Hashtable(11);
	
	private int fLine= -1;
	private int fColumn= -1;

	/**
	 * The id of this action.
	 */
	public static final String ID = IAntUIConstants.PLUGIN_ID + ".AntOpenWithMenu"; //$NON-NLS-1$

	public AntOpenWithMenu(IWorkbenchPage page) {
		super(ID);
		this.page= page;
	}
	
	public void setFile(IAdaptable file) {
		this.file= file;
	}
	
	public void dispose() {
		super.dispose();
		Iterator iter= imageCache.values().iterator();
		while (iter.hasNext()) {
			Image image = (Image) iter.next();
			image.dispose();
		}		
		imageCache.clear();
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
			imageDesc = registry.getImageDescriptor(getFileResource().getName());
		} else {
			imageDesc = editorDesc.getImageDescriptor();
		}
		if (imageDesc == null) {
			if (editorDesc.getId().equals(SYSTEM_EDITOR_ID)) {
				imageDesc = getSystemEditorImageDescriptor(getFileResource().getFileExtension());
			}
		}
		return imageDesc;
	}

	/**
	 * Return the image descriptor of the system editor
	 * that is registered with the OS to edit files of
	 * this type. Null if none can be found.
	 */
	private ImageDescriptor getSystemEditorImageDescriptor(String extension) {
		Program externalProgram = null;
		if (extension != null) {
			externalProgram = Program.findProgram(extension);
		}
		if (externalProgram == null) {
			return null;
		} 
		return new EditorImageDescriptor(externalProgram);
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
					case SWT.Selection :
						if (menuItem.getSelection()) {
							openEditor(descriptor);
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
		IFile fileResource = getFileResource();
		if (fileResource == null) {
			return;
		}

		IEditorDescriptor defaultEditor = registry.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID); // should not be null
		IEditorDescriptor preferredEditor = IDE.getDefaultEditor(fileResource); // may be null
		
		Object[] editors= registry.getEditors(fileResource.getName());
		Arrays.sort(editors, new Comparator() {
			/**
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Object o1, Object o2) {
				String s1 = ((IEditorDescriptor) o1).getLabel();
				String s2 = ((IEditorDescriptor) o2).getLabel();
				//Return true if elementTwo is 'greater than' elementOne
				return s1.compareToIgnoreCase(s2);
			}
		});
		IEditorDescriptor antEditor= registry.findEditor("org.eclipse.ant.internal.ui.editor.AntEditor"); //$NON-NLS-1$
		
		boolean defaultFound = false;
		boolean antFound= false;
		List alreadyAddedEditors= new ArrayList(editors.length);
		for (int i = 0; i < editors.length; i++) {
			IEditorDescriptor editor = (IEditorDescriptor) editors[i];
			if (alreadyAddedEditors.contains(editor.getId())) {
				continue;
			}
			createMenuItem(menu, editor, preferredEditor);
			if (defaultEditor != null && editor.getId().equals(defaultEditor.getId())) {
				defaultFound = true;
			}
			if (antEditor != null && editor.getId().equals(antEditor.getId())) {
				antFound= true;
			}
			alreadyAddedEditors.add(editor.getId());
			
		}

		// Only add a separator if there is something to separate
		if (editors.length > 0) {
			new MenuItem(menu, SWT.SEPARATOR);
		}

		// Add ant editor.
		 if (!antFound && antEditor != null) {
			 createMenuItem(menu, antEditor, preferredEditor);
		 }
			 
		// Add default editor.
		if (!defaultFound && defaultEditor != null) {
			createMenuItem(menu, defaultEditor, preferredEditor);
		}

		// Add system editor.
		IEditorDescriptor descriptor = registry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		createMenuItem(menu, descriptor, preferredEditor);
		createDefaultMenuItem(menu, fileResource);
	}

	/**
	 * Converts the IAdaptable file to IFile or null.
	 */
	private IFile getFileResource() {
		if (this.file instanceof IFile) {
			return (IFile) this.file;
		}
		IResource resource = (IResource) this.file.getAdapter(IResource.class);
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
	 * @param editor the editor descriptor, or null for the system editor
	 */
	private void openEditor(IEditorDescriptor editorDescriptor) {
		IEditorPart editorPart= null;
		IFile fileResource = getFileResource();
		try {
			if (editorDescriptor == null) {
				editorPart= page.openEditor(new FileEditorInput(fileResource), IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
			} else {
				editorPart= page.openEditor(new FileEditorInput(fileResource), editorDescriptor.getId());
			}
		} catch (PartInitException e) {
			AntUIPlugin.log(MessageFormat.format(AntViewActionMessages.getString("AntViewOpenWithMenu.Editor_failed"), new String[]{fileResource.getLocation().toOSString()}), e); //$NON-NLS-1$
		}
		if (fLine == -1) {
			return;
		}
		if (editorPart instanceof ITextEditor) {
			ITextEditor editor= (ITextEditor)editorPart;
			int offset= getOffset(fLine, fColumn, editor);
			if (offset == -1) {
				return;
			}
			IDocumentProvider provider= editor.getDocumentProvider();
			if (provider instanceof AntEditorDocumentProvider) {
				AntModel model= ((AntEditorDocumentProvider)provider).getAntModel(editor.getEditorInput());
				AntElementNode node= model.getProjectNode().getNode(offset);
				editor.setHighlightRange(node.getOffset(), node.getLength(), true);
				editor.selectAndReveal(node.getOffset(), node.getSelectionLength());	
			}
		}
	}
	
	private int getOffset(int line, int column, ITextEditor editor) {
		IDocumentProvider provider= editor.getDocumentProvider();
		IEditorInput input= editor.getEditorInput();
		try {
			provider.connect(input);
		} catch (CoreException e) {
			return -1;
		}
		try {
			IDocument document= provider.getDocument(input);
			if (document != null) {
				if (column > -1) {
					 //column marks the length..adjust to 0 index and to be within the element's source range
					return document.getLineOffset(line - 1) + column - 1 - 2;
				} 
				return document.getLineOffset(line - 1);
			}
		} catch (BadLocationException e) {
		} finally {
			provider.disconnect(input);
		}
		return -1;
	}

	/**
	 * Creates the menu item for the default editor
	 *
	 * @param menu the menu to add the item to
	 * @param file the file being edited
	 * @param registry the editor registry
	 */
	private void createDefaultMenuItem(Menu menu, final IFile fileResource) {
		final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setSelection(IDE.getDefaultEditor(fileResource) == null);
		menuItem.setText(AntViewActionMessages.getString("AntViewOpenWithMenu.Default_Editor_4")); //$NON-NLS-1$

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
					case SWT.Selection :
						if (menuItem.getSelection()) {
							IDE.setDefaultEditor(fileResource, null);
							try {
								IDE.openEditor(page, fileResource, true);
							} catch (PartInitException e) {
								AntUIPlugin.log(MessageFormat.format(AntViewActionMessages.getString("AntViewOpenWithMenu.Editor_failed"), new String[]{fileResource.getLocation().toOSString()}), e); //$NON-NLS-1$
							}
						}
						break;
				}
			}
		};

		menuItem.addListener(SWT.Selection, listener);
	}

	
	public void setExternalInfo(int line, int column) {
		fLine= line;
		fColumn= column;
	}
}
