/*******************************************************************************
 * Copyright (c) 2000, 20078 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Image;

/**
 * Implements a reference to a IWorkbenchPart.
 * The IWorkbenchPart will not be instantiated until the part 
 * becomes visible or the API getPart is sent with true;
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchPartReference {
    /**
     * Returns the IWorkbenchPart referenced by this object.
     * Returns <code>null</code> if the editors was not instantiated or
     * it failed to be restored. Tries to restore the editor
     * if <code>restore</code> is true.
     */
    public IWorkbenchPart getPart(boolean restore);

    /**
     * @see IWorkbenchPart#getTitle
     */
    public String getTitle();

    /**
     * @see IWorkbenchPart#getTitleImage
     */
    public Image getTitleImage();

    /**
     * @see IWorkbenchPart#getTitleToolTip
     */
    public String getTitleToolTip();

    /**
     * @see IWorkbenchPartSite#getId
     */
    public String getId();

    /**
     * @see IWorkbenchPart#addPropertyListener
     */
    public void addPropertyListener(IPropertyListener listener);

    /**
     * @see IWorkbenchPart#removePropertyListener
     */
    public void removePropertyListener(IPropertyListener listener);

    /**
     * Returns the workbench page that contains this part
     */
    public IWorkbenchPage getPage();

    /**
     * Returns the name of the part, as it should be shown in tabs.
     * 
     * @return the part name
     * 
     * @since 3.0
     */
    public String getPartName();

    /**
     * Returns the content description for the part (or the empty string if none)
     * 
     * @return the content description for the part
     * 
     * @since 3.0  
     */
    public String getContentDescription();
    

    /**
     * Returns whether the part is dirty (i.e. has unsaved changes).
     * 
     * @return <code>true</code> if the part is dirty, <code>false</code> otherwise
     * 
     * @since 3.2 (previously existed on IEditorReference)
     */
    public boolean isDirty();
    
    /**
	 * Return an arbitrary property from the reference. If the part has been
	 * instantiated, it just delegates to the part. If not, then it looks in its
	 * own cache of properties. If the property is not available or the part has
	 * never been instantiated, it can return <code>null</code>.
	 * 
	 * @param key
	 *            The property to return. Must not be <code>null</code>.
	 * @return The String property, or <code>null</code>.
	 * @since 3.3
	 */
    public String getPartProperty(String key);
    
    /**
	 * Add a listener for changes in the arbitrary properties set.
	 * 
	 * @param listener
	 *            Must not be <code>null</code>.
	 * @since 3.3
	 */
    public void addPartPropertyListener(IPropertyChangeListener listener);
    
    /**
	 * Remove a listener for changes in the arbitrary properties set.
	 * 
	 * @param listener
	 *            Must not be <code>null</code>.
	 * @since 3.3
	 */
    public void removePartPropertyListener(IPropertyChangeListener listener);
}
