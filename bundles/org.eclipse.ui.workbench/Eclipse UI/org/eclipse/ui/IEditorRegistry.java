/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.content.IContentType;
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
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEditorRegistry {

    /**
     * The property identifier for the contents of this registry.
     */
    public static final int PROP_CONTENTS = 0x01;

    /**
     * The identifier for the system external editor descriptor. This descriptor 
     * is always present in the registry on all platforms.
     * This editor requires an input which implements
     * {@link org.eclipse.ui.IPathEditorInput IPathEditorInput}.
     * Use {@link #findEditor findEditor} to access the editor descriptor for
     * this identifier.
     * 
     * @since 3.0
     */
    public static final String SYSTEM_EXTERNAL_EDITOR_ID = "org.eclipse.ui.systemExternalEditor"; //$NON-NLS-1$

    /**
     * The identifier for the system in-place editor descriptor. This descriptor 
     * is only present in the registry on platforms that support in-place editing
     * (for example Win32). This editor requires an input which implements
     * {@link org.eclipse.ui.IInPlaceEditorInput IInPlaceEditorInput}. Use 
     * {@link #findEditor findEditor} to access the editor descriptor for this
     * identifier.
     * 
     * @since 3.0
     */
    public static final String SYSTEM_INPLACE_EDITOR_ID = "org.eclipse.ui.systemInPlaceEditor"; //$NON-NLS-1$

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
     * Returns the default editor. The default editor always exist.
     *
     * @return the descriptor of the default editor
     * @deprecated The system external editor is the default editor.
     * Use <code>findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID)</code>
     * instead.
     */
    public IEditorDescriptor getDefaultEditor();

    /**
	 * Returns the default editor for a given file name. This method assumes an
	 * unknown content type for the given file.
	 * <p>
	 * The default editor is determined by taking the file extension for the
	 * file and obtaining the default editor for that extension.
	 * </p>
	 * 
	 * @param fileName
	 *            the file name in the system
	 * @return the descriptor of the default editor, or <code>null</code> if
	 *         not found
	 */
    public IEditorDescriptor getDefaultEditor(String fileName);
    
    /**
     * Returns the default editor for a given file name and with the given content type.  
     * <p>
     * The default editor is determined by taking the file extension for the
     * file and obtaining the default editor for that extension.
     * </p>
     *
     * @param fileName the file name in the system
     * @param contentType the content type or <code>null</code> for the unknown content type
     * @return the descriptor of the default editor, or <code>null</code> if not
     *   found
     * @since 3.1
     */
    public IEditorDescriptor getDefaultEditor(String fileName, IContentType contentType);

    /**
	 * Returns the list of file editors registered to work against the file with
	 * the given file name. This method assumes an unknown content type for the
	 * given file.
	 * <p>
	 * Note: Use <code>getDefaultEditor(String)</code> if you only the need
	 * the default editor rather than all candidate editors.
	 * </p>
	 * 
	 * @param fileName
	 *            the file name in the system
	 * @return a list of editor descriptors
	 */
    public IEditorDescriptor[] getEditors(String fileName);
 
    /**
	 * Returns the list of file editors registered to work against the file with
	 * the given file name and with the given content type.
	 * <p>
	 * Note: Use <code>getDefaultEditor(String)</code> if you only the need
	 * the default editor rather than all candidate editors.
	 * </p>
	 * 
	 * @param fileName
	 *            the file name in the system
	 * @param contentType
	 *            the content type or <code>null</code> for the unknown
	 *            content type
	 * @return a list of editor descriptors
	 * @since 3.1
	 */
    public IEditorDescriptor[] getEditors(String fileName, IContentType contentType);

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
	 * Returns the image descriptor associated with a given file. This image is
	 * usually displayed next to the given file. This method assumes an unknown
	 * content type for the given file.
	 * <p>
	 * The image is determined by taking the file extension of the file and
	 * obtaining the image for the default editor associated with that
	 * extension. A default image is returned if no default editor is available.
	 * </p>
	 * 
	 * @param filename
	 *            the file name in the system
	 * @return the descriptor of the image to display next to the file
	 */
    public ImageDescriptor getImageDescriptor(String filename);
	
    /**
	 * Returns the image descriptor associated with a given file. This image is
	 * usually displayed next to the given file.
	 * <p>
	 * The image is determined by taking the file extension of the file and
	 * obtaining the image for the default editor associated with that
	 * extension. A default image is returned if no default editor is available.
	 * </p>
	 * 
	 * @param filename
	 *            the file name in the system
	 * @param contentType
	 *            the content type of the file or <code>null</code> for the
	 *            unknown content type
	 * @return the descriptor of the image to display next to the file
	 * @since 3.1
	 */
    public ImageDescriptor getImageDescriptor(String filename, IContentType contentType);

    /**
     * Removes the given property listener from this registry.
     * Has no affect if an identical listener is not registered.
     *
     * @param listener a property listener
     */
    public void removePropertyListener(IPropertyListener listener);

    /**
     * Sets the default editor id for the files that match that
     * specified file name or extension. The specified editor must be
     * defined as an editor for that file name or extension.
     *
     * @param fileNameOrExtension the file name or extension pattern (e.g. "*.xml");
     * @param editorId the editor id or <code>null</code> for no default
     */
    public void setDefaultEditor(String fileNameOrExtension, String editorId);

    /**
     * Returns whether there is an in-place editor that could handle a file
     * with the given name.
     * 
     * @param filename the file name
     * @return <code>true</code> if an in-place editor is available, and
     * <code>false</code> otherwise
     * @since 3.0
     */
    public boolean isSystemInPlaceEditorAvailable(String filename);

    /**
     * Returns whether the system has an editor that could handle a file
     * with the given name.
     * 
     * @param filename the file name
     * @return <code>true</code> if an external editor available, and
     * <code>false</code> otherwise
     * @since 3.0
     */
    public boolean isSystemExternalEditorAvailable(String filename);

    /**
     * Returns the image descriptor associated with the system editor that
     * would be used to edit this file externally.
     *
     * @param filename the file name
     * @return the descriptor of the external editor image, or <code>null</code>
     * if none
     * @since 3.0
     */
    public ImageDescriptor getSystemExternalEditorImageDescriptor(
            String filename);
}
