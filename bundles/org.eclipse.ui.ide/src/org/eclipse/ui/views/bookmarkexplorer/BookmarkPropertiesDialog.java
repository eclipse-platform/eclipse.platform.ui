/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.bookmarkexplorer;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.views.bookmarkexplorer.BookmarkMessages;
import org.eclipse.ui.views.markers.internal.DialogMarkerProperties;

/**
 * Shows the properties of a new or existing bookmark
 * This class was made public in 3.3.
 * 
 * @since 3.3 
 */
public class BookmarkPropertiesDialog extends DialogMarkerProperties {



    /**
     * Creates the dialog.  By default this dialog creates a new bookmark.
     * To set the resource and initial attributes for the new bookmark, 
     * use <code>setResource</code> and <code>setInitialAttributes</code>.
     * To show or modify an existing bookmark, use <code>setMarker</code>.
     * 
     * @param parentShell the parent shell
     */
    public BookmarkPropertiesDialog(Shell parentShell) {
        this(parentShell, BookmarkMessages.PropertiesDialogTitle_text);
    }

    /**
     * Creates the dialog.  By default this dialog creates a new bookmark.
     * To set the resource and initial attributes for the new bookmark, 
     * use <code>setResource</code> and <code>setInitialAttributes</code>.
     * To show or modify an existing bookmark, use <code>setMarker</code>.
     * 
     * @param parentShell the parent shell
     * @param title the title for the dialog
     */
    public BookmarkPropertiesDialog(Shell parentShell, String title) {
        super(parentShell, title);
    	setType(IMarker.BOOKMARK);
    }
    
    /**
     * Sets the marker to show or modify.
     * 
     * @param marker the marker, or <code>null</code> to create a new marker
     */
    public void setMarker(IMarker marker) {
    	// Method is overridden because API is being inherited from an internal class.
        super.setMarker(marker);
    }

    /**
     * Returns the marker being created or modified.
     * For a new marker, this returns <code>null</code> until
     * the dialog returns, but is non-null after.
     * 
     * @return the marker
     */
    public IMarker getMarker() {
    	// Method is overridden because API is being inherited from an internal class.
        return super.getMarker();
    }

    /**
     * Sets the resource to use when creating a new bookmark.
     * If not set, the new bookmark is created on the workspace root.
     * 
     * @param resource the resource
     */
    public void setResource(IResource resource) {
    	// Method is overridden because API is being inherited from an internal class.
        super.setResource(resource);
    }

    /**
     * Returns the resource to use when creating a new bookmark,
     * or <code>null</code> if none has been set.
     * If not set, the new bookmark is created on the workspace root.
     * 
     * @return the resource
     */
    public IResource getResource() {
    	// Method is overridden because API is being inherited from an internal class.
        return super.getResource();
    }

    /**
     * Sets initial attributes to use when creating a new bookmark.
     * If not set, the new bookmark is created with default attributes.
     * 
     * @param initialAttributes the initial attributes
     */
    public void setInitialAttributes(Map initialAttributes) {
    	// Method is overridden because API is being inherited from an internal class.
        super.setInitialAttributes(initialAttributes);
    }

    /**
     * Returns the initial attributes to use when creating a new bookmark,
     * or <code>null</code> if not set.
     * If not set, the new bookmark is created with default attributes.
     * 
     * @return the initial attributes
     */
    public Map getInitialAttributes() {
    	// Method is overridden because API is being inherited from an internal class.
        return super.getInitialAttributes();
    }
    
	/* (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.DialogMarkerProperties.getModifyOperationTitle()
     * 
     * @since 3.3
     */
	protected String getModifyOperationTitle() {
		return BookmarkMessages.ModifyBookmark_undoText;
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.DialogMarkerProperties.getCreateOperationTitle()
     * 
     * @since 3.3
     */
	protected String getCreateOperationTitle() {
		return BookmarkMessages.CreateBookmark_undoText;
		
	}
}
