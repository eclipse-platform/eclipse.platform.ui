package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;

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
 * @see IPerspective#openEditor
 * @see org.eclipse.ui.part.EditorPart
 */
public interface IEditorPart extends IWorkbenchPart {

	/**
	 * The property id for <code>isDirty</code>.
	 */
	public static final int PROP_DIRTY = 0x101;

	/**
	 * The property id for <code>getEditorInput</code>.
	 */
	public static final int PROP_INPUT = 0x102;
/**
 * Saves the contents of this editor.
 * <p>
 * If the save is successful, the editor should fire a property changed event 
 * reflecting the new dirty state (<code>PROP_SAVE_NEEDED</code> property).
 * </p>
 * <p>
 * If the save is cancelled through user action, or for any other reason, the
 * editor should invoke <code>setCancelled</code> on the <code>monitor</code>
 * to inform the caller.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param monitor the progress monitor
 */
public void doSave(IProgressMonitor monitor);
/**
 * Saves the contents of this editor to another object.
 * <p>
 * Implementors are expected to open a "save as" dialog where the user will
 * be able to select a new name for the contents. After the selection is made,
 * the contents should be saved to that new name.  During this operation a
 * <code>ProgressMonitorDialog</code> should be used to indicate progress.
 * </p>
 * <p>
 * If the save is successful, the editor fires a property changed event 
 * reflecting the new dirty state (<code>PROP_SAVE_NEEDED</code> property).
 * </p>
 */
public void doSaveAs();
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
/**
 * Returns whether the contents of this editor have changed since the last save
 * operation.  If this value changes the part must fire a property listener 
 * event with <code>PROP_DIRTY</code>.
 * <p>
 *
 * @return <code>true</code> if the contents have been modified and need
 *   saving, and <code>false</code> if they have not changed since the last
 *   save
 */
public boolean isDirty();
/**
 * Returns whether the "save as" operation is supported by this editor.
 *
 * @return <code>true</code> if "save as" is supported, and <code>false</code>
 *  if "save as" is not supported
 */
public boolean isSaveAsAllowed();
/**
 * Returns whether the contents of this editor should be saved when the editor
 * is closed.
 *
 * @return <code>true</code> if the contents of the editor should be saved on
 *   close, and <code>false</code> if the contents are expendable
 */
public boolean isSaveOnCloseNeeded();
}
