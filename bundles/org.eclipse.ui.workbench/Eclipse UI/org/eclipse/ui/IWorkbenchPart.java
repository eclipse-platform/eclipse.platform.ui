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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * A workbench part is a visual component within a workbench page.  There
 * are two subtypes: view and editor, as defined by <code>IViewPart</code> and
 * <code>IEditorPart</code>.  
 * <p>
 * A view is typically used to navigate a hierarchy of information (like the 
 * workspace), open an editor, or display properties for the active editor.  
 * Modifications made in a view are saved immediately.  
 * </p><p>
 * An editor is typically used to edit or browse a document or input object. 
 * The input is identified using an <code>IEditorInput</code>.  Modifications made 
 * in an editor part follow an open-save-close lifecycle model.
 * </p><p>
 * This interface may be implemented directly.  For convenience, a base
 * implementation is defined in <code>WorkbenchPart</code>.
 * </p><p>
 * The lifecycle of a workbench part is as follows:
 * <ul>
 * 	<li>When a part extension is created:
 *    <ul>
 *		<li>instantiate the part</li>
 *		<li>create a part site</li>
 *		<li>call <code>part.init(site)</code></li>
 * 	  </ul>
 *  <li>When a part becomes visible in the workbench:
 * 	  <ul> 
 *		<li>add part to presentation by calling 
 *        <code>part.createControl(parent)</code> to create actual widgets</li>
 *		<li>fire <code>partOpened</code> event to all listeners</li>
 *	  </ul>
 *   </li>
 *  <li>When a part is activated or gets focus:
 *    <ul>
 *		<li>call <code>part.setFocus()</code></li>
 *		<li>fire <code>partActivated</code> event to all listeners</li>
 *	  </ul>
 *   </li>
 *  <li>When a part is closed:
 *    <ul>
 *		<li>if save is needed, do save; if it fails or is canceled return</li>
 *		<li>if part is active, deactivate part</li>
 *		<li>fire <code>partClosed</code> event to all listeners</li>
 *		<li>remove part from presentation; part controls are disposed as part
 *         of the SWT widget tree
 *		<li>call <code>part.dispose()</code></li>
 *	  </ul>
 *   </li>
 * </ul>
 * </p>
 * <p>
 * After <code>createPartControl</code> has been called, the implementor may 
 * safely reference the controls created.  When the part is closed 
 * these controls will be disposed as part of an SWT composite.  This
 * occurs before the <code>IWorkbenchPart.dispose</code> method is called.
 * If there is a need to free SWT resources the part should define a dispose 
 * listener for its own control and free those resources from the dispose
 * listener.  If the part invokes any method on the disposed SWT controls 
 * after this point an <code>SWTError</code> will be thrown.  
 * </p>
 * <p>
 * The last method called on <code>IWorkbenchPart</code> is <code>dispose</code>.  
 * This signals the end of the part lifecycle.
 * </p>
 * <p>
 * An important point to note about this lifecycle is that following 
 * a call to init, createControl may never be called. Thus in the dispose
 * method, implementors must not assume controls were created.
 * </p>
 * <p>
 * Workbench parts implement the <code>IAdaptable</code> interface; extensions
 * are managed by the platform's adapter manager.
 * </p>
 *
 * @see IViewPart
 * @see IEditorPart
 */
public interface IWorkbenchPart extends IAdaptable {

    /**
     * The property id for <code>getTitle</code>, <code>getTitleImage</code>
     * and <code>getTitleToolTip</code>.
     */
    public static final int PROP_TITLE = IWorkbenchPartConstants.PROP_TITLE;

    /**
     * Adds a listener for changes to properties of this workbench part.
     * Has no effect if an identical listener is already registered.
     * <p>
     * The property ids are defined in {@link IWorkbenchPartConstants}.
     * </p>
     *
     * @param listener a property listener
     */
    public void addPropertyListener(IPropertyListener listener);

    /**
     * Creates the SWT controls for this workbench part.
     * <p>
     * Clients should not call this method (the workbench calls this method when
     * it needs to, which may be never).
     * </p>
     * <p>
     * For implementors this is a multi-step process:
     * <ol>
     *   <li>Create one or more controls within the parent.</li>
     *   <li>Set the parent layout as needed.</li>
     *   <li>Register any global actions with the site's <code>IActionBars</code>.</li>
     *   <li>Register any context menus with the site.</li>
     *   <li>Register a selection provider with the site, to make it available to 
     *     the workbench's <code>ISelectionService</code> (optional). </li>
     * </ol>
     * </p>
     *
     * @param parent the parent control
     */
    public void createPartControl(Composite parent);

    /**
     * Disposes of this workbench part.
     * <p>
     * This is the last method called on the <code>IWorkbenchPart</code>.  At this
     * point the part controls (if they were ever created) have been disposed as part 
     * of an SWT composite.  There is no guarantee that createPartControl() has been 
     * called, so the part controls may never have been created.
     * </p>
     * <p>
     * Within this method a part may release any resources, fonts, images, etc.&nbsp; 
     * held by this part.  It is also very important to deregister all listeners
     * from the workbench.
     * </p>
     * <p>
     * Clients should not call this method (the workbench calls this method at
     * appropriate times).
     * </p>
     */
    public void dispose();

    /**
     * Returns the site for this workbench part. The site can be
     * <code>null</code> while the workbench part is being initialized. After
     * the initialization is complete, this value must be non-<code>null</code>
     * for the remainder of the part's life cycle.
     * 
     * @return The part site; this value may be <code>null</code> if the part
     *         has not yet been initialized
     */
    public IWorkbenchPartSite getSite();

    /**
     * Returns the title of this workbench part. If this value changes 
     * the part must fire a property listener event with 
     * <code>PROP_TITLE</code>.
     * <p>
     * The title is used to populate the title bar of this part's visual
     * container.  
     * </p>
     *
     * @return the workbench part title (not <code>null</code>)
     */
    public String getTitle();

    /**
     * Returns the title image of this workbench part.  If this value changes 
     * the part must fire a property listener event with 
     * <code>PROP_TITLE</code>.
     * <p>
     * The title image is usually used to populate the title bar of this part's
     * visual container. Since this image is managed by the part itself, callers
     * must <b>not</b> dispose the returned image.
     * </p>
     *
     * @return the title image
     */
    public Image getTitleImage();

    /**
     * Returns the title tool tip text of this workbench part. 
     * An empty string result indicates no tool tip.
     * If this value changes the part must fire a property listener event with 
     * <code>PROP_TITLE</code>.
     * <p>
     * The tool tip text is used to populate the title bar of this part's 
     * visual container.  
     * </p>
     *
     * @return the workbench part title tool tip (not <code>null</code>)
     */
    public String getTitleToolTip();

    /**
     * Removes the given property listener from this workbench part.
     * Has no affect if an identical listener is not registered.
     *
     * @param listener a property listener
     */
    public void removePropertyListener(IPropertyListener listener);

    /**
     * Asks this part to take focus within the workbench.
     * <p>
     * Clients should not call this method (the workbench calls this method at
     * appropriate times).  To have the workbench activate a part, use
     * <code>IWorkbenchPage.activate(IWorkbenchPart) instead</code>.
     * </p>
     */
    public void setFocus();
}
