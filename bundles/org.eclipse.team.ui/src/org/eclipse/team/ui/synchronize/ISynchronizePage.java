/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PartInitException;

/**
 * Interface for {@link org.eclipse.ui.part.Page} subclasses that can appear in the
 * synchronize view {@link ISynchronizeView} and other views, 
 * editors or dialogs that
 * display synchronization information. It is not a requirement
 * that pages that appear in the synchronize view implement this interface.
 * However, by doing so, the page is initialized with a  
 * {@link ISynchronizePageSite) which provides a context for the page.
 * The page is given this context in addition to the 
 * {@link org.eclipse.ui.part.IPageSite}
 * provided when the page is used in a view. However, the page site
 * may not be provided when the page appears in an editor or dialog.
 */
public interface ISynchronizePage {
	
	/**
	 * Initialize this page with workbench part that contains the page.
	 * This method will be called after the <code>Page#init(IPageSite)</code>
	 * but before <code>Page#createControl(Composite)</code>
	 * 
	 * @param part the workbench part for the view containing the page
	 * @throws PartInitException
	 */
	public void init(ISynchronizePageSite site) throws PartInitException;
	
	/**
	 * Returns the viewer associated with this page or <code>null</code> if the page
	 * doesn't have a viewer.
	 */
	public Viewer getViewer();

	/**
	 * Callback that is invoked from the synchronize configuration
	 * whenever a property's value is about to be changed. The page
	 * can react to the change before change events are fired or
	 * veto the change.
	 * @param configuration the synchronize page configuration
	 * @param key the property key
	 * @param newValue
	 * @return
	 */
	public boolean aboutToChangeProperty(ISynchronizePageConfiguration configuration, String key, Object newValue);

}
