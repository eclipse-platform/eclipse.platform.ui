/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui;

import org.eclipse.core.resources.IMarker;

/**
 * An editor is a visual component within a workbench page. It is
 * typically used to edit or browse a document or input object. The input 
 * is identified using an <code>IEditorInput</code>.  Modifications made 
 * in an editor part follow an open-save-close lifecycle model (in contrast 
 * to a view part, where modifications are saved to the workbench 
 * immediately).
 * <p>
 * An editor is document or input-centric.  Each editor has an input, and only
 * one editor can exist for each editor input within a page.  This policy has 
 * been designed to simplify part management.  
 * </p><p>
 * An editor should be used in place of a view whenever more than one instance
 * of a document type can exist.
 * </p><p>
 * This interface may be implemented directly.  For convenience, a base
 * implementation is defined in <code>EditorPart</code>.
 * </p>
 * <p>
 * An editor part is added to the workbench in two stages:
 * <ol>
 * 	<li>An editor extension is contributed to the workbench registry. This
 *    extension defines the extension id, extension class, and the file 
 *    extensions which are supported by the editor.</li>
 *  <li>An editor part based upon the extension is created and added to the
 *    workbench when the user opens a file with one of the supported file
 *    extensions (or some other suitable form of editor input).</li>
 * </ol>
 * </p>
 * <p>
 * All editor parts implement the <code>IAdaptable</code> interface; extensions
 * are managed by the platform's adapter manager.
 * </p>
 *
 * @see org.eclipse.ui.IWorkbenchPage#openEditor()
 * @see org.eclipse.ui.part.EditorPart
 */
public interface IEditorPart extends IWorkbenchPart, ISaveablePart {

	/**
	 * The property id for <code>isDirty</code>.
	 */
	public static final int PROP_DIRTY = ISaveablePart.PROP_DIRTY;

	/**
	 * The property id for <code>getEditorInput</code>.
	 */
	public static final int PROP_INPUT = 0x102;
	
	/**
	 * Returns the input for this editor.  If this value changes the part must 
	 * fire a property listener event with <code>PROP_INPUT</code>.
	 *
	 * @return the editor input
	 */
	public IEditorInput getEditorInput();
	
	/**
	 * Returns the site for this editor. The method is equivalent to 
	 * <code>(IEditorSite) getSite()</code>.
	 *
	 * @return the editor site
	 */
	public IEditorSite getEditorSite();
	
	/**
	 * Sets the cursor and selection state for this editor to the passage defined
	 * by the given marker.
	 *
	 * @param marker the marker
	 */
	public void gotoMarker(IMarker marker);
	
	/**
	 * Initializes this editor with the given editor site and input.
	 * <p>
	 * This method is automatically called shortly after part construction; it marks
	 * the start of the part's lifecycle. The 
	 * {@link IWorkbenchPart#dispose IWorkbenchPart.dispose} method will be called 
	 * automically at the end of the lifecycle. Clients must not call this method.
	 * </p><p>
	 * Implementors of this method must examine the editor input object type to
	 * determine if it is understood.  If not, the implementor must throw
	 * a <code>PartInitException</code>
	 * </p>
	 * @param site the editor site
	 * @param input the editor input
	 * @exception PartInitException if this editor was not initialized successfully
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException;
}
