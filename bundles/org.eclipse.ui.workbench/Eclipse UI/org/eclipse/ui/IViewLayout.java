/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Represents the layout info for a view in an {@link IPageLayout}.
 * 
 * @since 3.0
 */
public interface IViewLayout {
    
    public boolean isCloseable();
    public void setCloseable(boolean closeable);
    
    public boolean isMoveable();
    public void setMoveable(boolean moveable);
    
    /**
     * Returns whether the view is a standalone view.
     * 
     * @see IPageLayout#addStandaloneView
     */
    public boolean isStandalone();

    /**
     * Returns whether the view shows its title.
     * This is only applicable to standalone views.
     * 
     * @see IPageLayout#addStandaloneView
     */
    public boolean getShowTitle();
}
