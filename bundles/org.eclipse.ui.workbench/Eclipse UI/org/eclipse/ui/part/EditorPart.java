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
package org.eclipse.ui.part;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.*;

/**
 * Abstract base implementation of all workbench editors.
 * <p>
 * This class should be subclassed by clients wishing to define new editors.
 * The name of the subclass should be given as the <code>"class"</code> 
 * attribute in a <code>editor</code> extension contributed to the workbench's
 * view extension point (named <code>"org.eclipse.ui.editors"</code>).
 * For example, the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point="org.eclipse.ui.editors"&GT;
 *      &LT;editor id="com.example.myplugin.ed"
 *         name="My Editor"
 *         icon="./images/cedit.gif"
 * 		   extensions="foo" 
 * 		   class="com.example.myplugin.MyFooEditor" 
 * 		   contributorClass="com.example.myplugin.MyFooEditorContributor" 
 *      /&GT;
 * &LT;/extension&GT;
 * </pre>
 * where <code>com.example.myplugin.MyEditor</code> is the name of the
 * <code>EditorPart</code> subclass.
 * </p>
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 *   <li><code>IEditorPart.init</code> - to initialize editor when assigned its site</li>
 *   <li><code>IWorkbenchPart.createPartControl</code> - to create the editor's controls </li>
 *   <li><code>IWorkbenchPart.setFocus</code> - to accept focus</li>
 *   <li><code>IEditorPart.isDirty</code> - to decide whether a significant change has
 *       occurred</li>
 *   <li><code>IEditorPart.doSave</code> - to save contents of editor</li>
 *   <li><code>IEditorPart.doSaveAs</code> - to save contents of editor</li>
 *   <li><code>IEditorPart.isSaveAsAllowed</code> - to control Save As</li>
 *   <li><code>IEditorPart.gotoMarker</code> - to make selections based on markers</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend or reimplement the following methods as required:
 * <ul>
 *   <li><code>IExecutableExtension.setInitializationData</code> - extend to provide additional 
 *       initialization when editor extension is instantiated</li>
 *   <li><code>IWorkbenchPart.dispose</code> - extend to provide additional cleanup</li>
 *   <li><code>IAdaptable.getAdapter</code> - reimplement to make the editor
 *       adaptable</li>
 * </ul>
 * </p>
 */
public abstract class EditorPart extends WorkbenchPart implements IEditorPart {

	/**
	 * Editor input, or <code>null</code> if none.
	 */
	private IEditorInput editorInput = null;
/**
 * Creates a new workbench editor.
 */
protected EditorPart() {
	super();
}
/* (non-Javadoc)
 * Saves the contents of this editor.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public abstract void doSave(IProgressMonitor monitor);
/* (non-Javadoc)
 * Saves the contents of this editor to another object.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public abstract void doSaveAs();
/* (non-Javadoc)
 * Method declared on IEditorPart.
 */
public IEditorInput getEditorInput() {
	return editorInput;
}
/* (non-Javadoc)
 * Method declared on IEditorPart.
 */
public IEditorSite getEditorSite() {
	return (IEditorSite)getSite();
}
/* (non-Javadoc)
 * Gets the title tool tip text of this part.
 *
 * @return the tool tip text
 */
public String getTitleToolTip() {
	if (editorInput == null)
		return super.getTitleToolTip();
	else
		return editorInput.getToolTipText();
}
/* (non-Javadoc)
 * Sets the cursor and selection state for this editor to the passage defined
 * by the given marker.
 * <p>
 * Subclasses may override.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public abstract void gotoMarker(IMarker marker);
/* (non-Javadoc)
 * Initializes the editor part with a site and input.
 * <p>
 * Subclasses of <code>EditorPart</code> must implement this method.  Within
 * the implementation subclasses should verify that the input type is acceptable
 * and then save the site and input.  Here is sample code:
 * </p>
 * <pre>
 *		if (!(input instanceof IFileEditorInput))
 *			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
 *		setSite(site);
 *		setInput(editorInput);
 * </pre>
 */
public abstract void init(IEditorSite site, IEditorInput input) throws PartInitException;
/* (non-Javadoc)
 * Returns whether the contents of this editor have changed since the last save
 * operation.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public abstract boolean isDirty();
/* (non-Javadoc)
 * Returns whether the "save as" operation is supported by this editor.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public abstract boolean isSaveAsAllowed();
/* (non-Javadoc)
 * Returns whether the contents of this editor should be saved when the editor
 * is closed.
 * <p>
 * This method returns <code>true</code> if and only if the editor is dirty 
 * (<code>isDirty</code>).
 * </p>
 */
public boolean isSaveOnCloseNeeded() {
	return isDirty();
}
/**
 * Sets the input to this editor.
 *
 * @param input the editor input
 */
protected void setInput(IEditorInput input) {
	editorInput = input;
}
}
