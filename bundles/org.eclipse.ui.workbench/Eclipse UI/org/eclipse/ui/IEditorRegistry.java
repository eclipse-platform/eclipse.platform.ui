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
package org.eclipse.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Registry of editors known to the workbench.
 * <p>
 * An editor can be created in one of two ways:
 * <ul>
 *   <li>An editor can be defined by an extension to the workbench.</li>
 *   <li>The user manually associates an editor with a given resource extension
 *      type. This will override any default workbench or platform association.
 *      </li>
 * </ul>
 * </p>
 * <p>
 * The registry does not keep track of editors that are "implicitly" determined.
 * For example a bitmap (<code>.bmp</code>) file will typically not have a 
 * registered editor. Instead, when no registered editor is found, the 
 * underlying OS is consulted.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see org.eclipse.ui.IWorkbench#getEditorRegistry()
 */
public interface IEditorRegistry {

	/**
	 * The property id for the contents of this registry.
	 */
	public static final int PROP_CONTENTS = 0x01;
/**
 * Adds a listener for changes to properties of this registry.
 * Has no effect if an identical listener is already registered.
 * <p>
 * The properties ids are as follows:
 * <ul>
 *   <li><code>PROP_CONTENTS</code>: Triggered when the file editor mappings in
 *       the editor registry change.</li>
 * </ul>
 * </p>
 * <p>
 * [Issue: Check handling of identical listeners.]
 * </p>
 *
 * @param listener a property listener
 */
public void addPropertyListener(IPropertyListener listener);
/**
 * Finds and returns the descriptor of the editor with the given editor id.
 *
 * @param editorId the editor id
 * @return the editor descriptor with the given id, or <code>null</code> if not
 *   found
 */
public IEditorDescriptor findEditor(String editorId);
/**
 * Returns the default editor used for all unmapped resource types.
 * There is always a default editor.
 *
 * @return the descriptor of the default editor
 */
public IEditorDescriptor getDefaultEditor();
/**
 * Returns the default editor for a given file name.  
 * <p>
 * The default editor is determined by taking the file extension for the
 * file and obtaining the default editor for that extension.
 * </p>
 *
 * @param filename the file name
 * @return the descriptor of the default editor, or <code>null</code> if not
 *   found
 */
public IEditorDescriptor getDefaultEditor(String fileName) ;
/**
 * Returns the default editor for a given file.
 * <p>
 * A default editor id may be registered for a specific file using
 * <code>setDefaultEditor</code>.  If the given file has a registered
 * default editor id the default editor will derived from it.  If not, 
 * the default editor is determined by taking the file extension for the 
 * file and obtaining the default editor for that extension.
 * </p>
 *
 * @param file the file
 * @return the descriptor of the default editor, or <code>null</code> if not
 *   found
 */
public IEditorDescriptor getDefaultEditor(IFile file) ;
/**
 * Returns the list of file editors registered to work against the file
 * with the given file name. 
 * <p>
 * Note: Use <code>getDefaultEditor</code> if you only the need the default
 * editor rather than all candidate editors.
 * </p>
 *
 * @param filename the file name
 * @return a list of editor descriptors
 */
public IEditorDescriptor[] getEditors(String filename) ;
/**
 * Returns the list of file editors registered to work against the given
 * file. 
 * <p>
 * Note: Use <code>getDefaultEditor</code> if you only the need the default
 * editor rather than all candidate editors.
 * </p><p>
 * This is a convenience method for use with <code>IFile</code>'s.
 * </p>
 *
 * @param file the file
 * @return a list of editor descriptors
 */
public IEditorDescriptor[] getEditors(IFile file) ;
/**
 * Returns a list of mappings from file type to editor.  The resulting list
 * is sorted in ascending order by file extension.
 * <p>
 * Each mapping defines an extension and the set of editors that are 
 * available for that type. The set of editors includes those registered 
 * via plug-ins and those explicitly associated with a type by the user 
 * in the workbench preference pages.
 * </p>
 *
 * @return a list of mappings sorted alphabetically by extension
 */
public IFileEditorMapping[] getFileEditorMappings();
/**
 * Returns the image descriptor associated with a given file.  This image
 * is usually displayed next to the given file.
 * <p>
 * The image is determined by taking the file extension of the file and 
 * obtaining the image for the default editor associated with that extension.
 * A default image is returned if no default editor is available.
 * </p>
 *
 * @param filename the file name
 * @return the descriptor of the image to display next to the file
 */
public ImageDescriptor getImageDescriptor(String filename) ;
/**
 * Returns the image descriptor associated with a given file.  This image
 * is usually displayed next to the given file.
 * <p>
 * The image is determined by taking the file extension of the file and 
 * obtaining the image for the default editor associated with that extension.
 * A default image is returned if no default editor is available.
 * </p><p>
 * This is a convenience method for use with <code>IFile</code>'s.
 * </p>
 *
 * @param file the file
 * @return the descriptor of the image to display next to the given file
 */
public ImageDescriptor getImageDescriptor(IFile file);
/**
 * Removes the given property listener from this registry.
 * Has no affect if an identical listener is not registered.
 * <p>
 * [Issue: Check handling of identical listeners.]
 * </p>
 *
 * @param listener a property listener
 */
public void removePropertyListener(IPropertyListener listener);
/**
 * Sets the default editor id for a given file.  This value will be used
 * to determine the default editor descriptor for the file in future calls to
 * <code>getDefaultEditor(IFile)</code>.
 *
 * @param file the file
 * @param editorId the editor id
 */
public void setDefaultEditor(IFile file, String editorId);
/**
 * Sets the default editor id for a the files that match that
 * specified file name or extension. The specified editor must be
 * defined as an editor for that file name or extension.
 *
 * @param fileNamePattern the file name or pattern (e.g. "*.xml");
 * @param editorId the editor id
 */
public void setDefaultEditor(String fileNameOrExtension, String editorId);
}
