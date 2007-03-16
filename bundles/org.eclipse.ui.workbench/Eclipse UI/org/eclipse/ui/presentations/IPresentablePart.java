/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.presentations;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;

/**
 * This is a skin's interface to the contents of a view or editor. Note that this
 * is essentially the same as IWorkbenchPart, except it does not provide access
 * to lifecycle events and allows repositioning of the part.
 * 
 * Not intended to be implemented by clients.
 * 
 * @since 3.0
 */
public interface IPresentablePart {

    /**
     * The property id for <code>isDirty</code>.
     */
    public static final int PROP_DIRTY = IWorkbenchPartConstants.PROP_DIRTY;

    /**
     * The property id for <code>getEditorInput</code>.
     */
    public static final int PROP_INPUT = IWorkbenchPartConstants.PROP_INPUT;

    /**
     * The property id for <code>getTitle</code>, <code>getTitleImage</code>
     * and <code>getTitleToolTip</code>.
     */
    public static final int PROP_TITLE = IWorkbenchPartConstants.PROP_TITLE;

    /**
     * The property id for <code>IWorkbenchPart2.getContentDescription()</code>
     */
    public static final int PROP_CONTENT_DESCRIPTION = IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION;

    /**
     * The property id for <code>IWorkbenchPart2.getContentDescription()</code>
     */
    public static final int PROP_PART_NAME = IWorkbenchPartConstants.PROP_PART_NAME;

    /**
     * The property id for <code>isBusy</code>.
     */
    public static final int PROP_BUSY = 0x92;

    /**
     * The property id for toolbar changes
     */
    public static final int PROP_TOOLBAR = 0x93;

    /**
     * The property id for highlighting the 
     * part if it is not in front.
     */
    public static final int PROP_HIGHLIGHT_IF_BACK = 0x94;

    /**
     * The property id for pane menu changes
     */
    public static final int PROP_PANE_MENU = 0x302;

    /**
     * Sets the bounds of this part.
     *  
     * @param bounds bounding rectangle (not null)
     */
    public void setBounds(Rectangle bounds);

    /**
     * Notifies the part whether or not it is visible in the current
     * perspective. A part is visible iff any part of its widgetry can
     * be seen.
     * 
     * @param isVisible true if the part has just become visible, false
     * if the part has just become hidden
     */
    public void setVisible(boolean isVisible);

    /**
     * Forces this part to have focus.
     */
    public void setFocus();

    /**
     * Adds a listener for changes to properties of this workbench part.
     * Has no effect if an identical listener is already registered.
     * <p>
     * The properties ids are defined by the PROP_* constants, above.
     * </p>
     *
     * @param listener a property listener (not null)
     */
    public void addPropertyListener(IPropertyListener listener);

    /**
     * Remove a listener that was previously added using addPropertyListener.
     *
     * @param listener a property listener (not null)
     */
    public void removePropertyListener(IPropertyListener listener);

    /**
     * Returns the short name of the part. This is used as the text on
     * the tab when this part is stacked on top of other parts.
     * 
     * @return the short name of the part (not null)
     */
    public String getName();

    /**
     * Returns the title of this workbench part. If this value changes 
     * the part must fire a property listener event with 
     * <code>PROP_TITLE</code>.
     * <p>
     * The title is used to populate the title bar of this part's visual
     * container.  
     * </p>
     *
     * @return the workbench part title (not null)
     */
    public String getTitle();

    /**
     * Returns the status message from the part's title, or the empty string if none. 
     * This is a substring of the part's title. A typical title will consist of
     * the part name, a separator, and a status message describing the current contents.
     * <p>
     * Presentations can query getName() and getTitleStatus() if they want to display
     * the status message and name separately, or they can use getTitle() if they want
     * to display the entire title.
     * </p> 
     * 
     * @return the status message or the empty string if none (not null)
     */
    public String getTitleStatus();

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
     * Returns the title tool tip text of this workbench part. If this value 
     * changes the part must fire a property listener event with 
     * <code>PROP_TITLE</code>.
     * <p>
     * The tool tip text is used to populate the title bar of this part's 
     * visual container.  
     * </p>
     *
     * @return the workbench part title tool tip (not null)
     */
    public String getTitleToolTip();

    /**
     * Returns true iff the contents of this part have changed recently. For
     * editors, this indicates that the part has changed since the last save.
     * For views, this indicates that the view contains interesting changes
     * that it wants to draw the user's attention to.
     * 
     * @return true iff the part is dirty
     */
    public boolean isDirty();

    /**
     * Return true if the the receiver is currently in a busy state.
     * @return boolean true if busy
     */
    public boolean isBusy();
    
    /**
     * Returns true iff this part can be closed
     * 
     * @return true iff this part can be closed
     * @since 3.1
     */
    public boolean isCloseable();
    
    /**
     * Returns the local toolbar for this part, or null if this part does not
     * have a local toolbar. Callers must not dispose or downcast the return value.
     * 
     * @return the local toolbar for the part, or null if none
     */
    public Control getToolBar();

    /**
     * Returns the menu for this part or null if none
     * 
     * @return the menu for this part or null if none
     */
    public IPartMenu getMenu();

    /**
     * Returns an SWT control that can be used to indicate the tab order for
     * this part. This can be returned as part of the result to 
     * {@link StackPresentation#getTabList(IPresentablePart)}. Any other use of this control is
     * unsupported. This may return a placeholder control that is only
     * meaningful in the context of <code>getTabList</code>.
     * 
     * @return the part's control (not null)
     */
    public Control getControl();

    /**
	 * Get a property from the part's arbitrary property set.
	 * <p>
	 * <b>Note:</b> this is a different set of properties than the ones covered
	 * by the PROP_* constants.
	 * </p>
	 * 
	 * @param key
	 *            The property key to retrieve. Must not be <code>null</code>.
	 * @return the property, or <code>null</code> if that property is not set.
	 * @since 3.3
	 */
	public String getPartProperty(String key);
	
	/**
	 * Add a listener for changes in the arbitrary properties set.
	 * <p>
	 * <b>Note:</b> this is a different set of properties than the ones covered
	 * by the PROP_* constants.
	 * </p>
	 * 
	 * @param listener
	 *            Must not be <code>null</code>.
	 * @since 3.3
	 */
	public void addPartPropertyListener(IPropertyChangeListener listener);
	
	/**
	 * Remove a change listener from the arbitrary properties set.
	 * <p>
	 * <b>Note:</b> this is a different set of properties than the ones covered
	 * by the PROP_* constants.
	 * </p>
	 * 
	 * @param listener
	 *            Must not be <code>null</code>.
	 * @since 3.3
	 */
	public void removePartPropertyListener(IPropertyChangeListener listener);
}
