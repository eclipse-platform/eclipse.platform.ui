/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.intro;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

/**
 * The intro part is a visual component within the workbench responsible for
 * introducing the product to new users. The intro part is typically shown the
 * first time a product is started up.
 * <p>
 * The intro part implementation is contributed to the workbench via the
 * <code>org.eclipse.ui.intro</code> extension point.  There can be several
 * intro part implementations, and associations between intro part
 * implementations and products. The workbench will only make use of the intro
 * part implementation for the current product (as given by
 * {@link org.eclipse.core.runtime.Platform#getProduct()}. There is at most one
 * intro part instance in the entire workbench, and it resides in exactly one
 * workbench window at a time.
 * </p>
 * <p>
 * This interface in not intended to be directly implemented. Rather, clients
 * providing a intro part implementation should subclass 
 * {@link org.eclipse.ui.part.IntroPart}. 
 * </p>
 * 
 * @see org.eclipse.ui.intro.IIntroManager#showIntro(org.eclipse.ui.IWorkbenchWindow, boolean)
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIntroPart extends IAdaptable {

    /**
	 * The property id for <code>getTitleImage</code> and
	 * <code>getTitle</code>.
	 * 
	 * @since 3.2 this property now covers changes to <code>getTitle</code> in
	 *        addition to <code>getTitleImage</code>
	 */
	public static final int PROP_TITLE = IWorkbenchPart.PROP_TITLE;

    /**
	 * Returns the site for this intro part.
	 * 
	 * @return the intro site
	 */
    IIntroSite getIntroSite();

    /**
     * Initializes this intro part with the given intro site. A memento is
     * passed to the part which contains a snapshot of the part state from a
     * previous session. Where possible, the part should try to recreate that
     * state.
     * <p>
     * This method is automatically called by the workbench shortly after
     * part construction.  It marks the start of the intro's lifecycle. Clients
     * must not call this method.
     * </p>
     *
     * @param site the intro site
     * @param memento the intro part state or <code>null</code> if there is no previous
     * saved state
     * @exception PartInitException if this part was not initialized
     * successfully
     */
    public void init(IIntroSite site, IMemento memento)
            throws PartInitException;

    /**
     * Sets the standby state of this intro part. An intro part should render
     * itself differently in the full and standby modes. In standby mode, the
     * part should be partially visible to the user but otherwise allow them
     * to work. In full mode, the part should be fully visible and be the center
     * of the user's attention. 
     * <p>
     * This method is automatically called by the workbench at appropriate
     * times. Clients must not call this method directly (call
     * {@link IIntroManager#setIntroStandby(IIntroPart, boolean)} instead.
     * </p>
     * 
     * @param standby <code>true</code> to put this part in its partially
     * visible standy mode, and <code>false</code> to make it fully visible
     */
    public void standbyStateChanged(boolean standby);

    /**
     * Saves the object state within a memento.
     * <p>
     * This method is automatically called by the workbench at appropriate
     * times. Clients must not call this method directly.
     * </p>
     *
     * @param memento a memento to receive the object state
     */
    public void saveState(IMemento memento);

    /**
     * Adds a listener for changes to properties of this intro part.
     * Has no effect if an identical listener is already registered.
     * <p>
     * The properties ids are as follows:
     * <ul>
     *   <li><code>IIntroPart.PROP_TITLE</code> </li>
     * </ul>
     * </p>
     *
     * @param listener a property listener
     */
    public void addPropertyListener(IPropertyListener listener);

    /**
     * Creates the SWT controls for this intro part.
     * <p>
     * Clients should not call this method (the workbench calls this method when
     * it needs to, which may be never).
     * </p>
     * <p>
     * For implementors this is a multi-step process:
     * <ol>
     *   <li>Create one or more controls within the parent.</li>
     *   <li>Set the parent layout as needed.</li>
     *   <li>Register any global actions with the <code>IActionService</code>.</li>
     *   <li>Register any popup menus with the <code>IActionService</code>.</li>
     *   <li>Register a selection provider with the <code>ISelectionService</code>
     *     (optional). </li>
     * </ol>
     * </p>
     *
     * @param parent the parent control
     */
    public void createPartControl(Composite parent);

    /**
     * Disposes of this intro part.
     * <p>
     * This is the last method called on the <code>IIntroPart</code>.  At this
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
     * Returns the title image of this intro part.  If this value changes 
     * the part must fire a property listener event with 
     * {@link IIntroPart#PROP_TITLE}.
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
     * Returns the title of this intro part. If this value changes 
     * the part must fire a property listener event with 
     * {@link IIntroPart#PROP_TITLE}.
     * <p>
     * The title is used to populate the title bar of this part's visual
     * container.  
     * </p>
     *
     * @return the intro part title (not <code>null</code>)
     * @since 3.2
     */
    public String getTitle();

    /**
     * Removes the given property listener from this intro part.
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
     * {@link IIntroManager#showIntro(IWorkbenchWindow, boolean)}.
     * </p>
     */
    public void setFocus();
}
