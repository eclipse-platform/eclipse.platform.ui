/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.navigator.internal.NavigatorContentService;


/**
 * Provides a factory pattern for creating {@link INavigatorContentService}s 
 * for given viewer ids.  
 * 
 * <p>
 * Clients may supply the viewer in {@link #createContentService(String, StructuredViewer) }
 * or wait until the content provider is created by the service 
 * and set on the viewer. When the content provider is set, the 
 * viewer will call inputChanged(), and the content service
 * will update its managed viewer accordingly. Therefore, each
 * content service should be attached to at most one viewer. 
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2  
 *
 */
public final class NavigatorContentServiceFactory {
	
	/**
	 * The singleton instance for creating NavigatorContentServices. 
	 */
	public static final NavigatorContentServiceFactory INSTANCE = new NavigatorContentServiceFactory(); 
	
	
	/**
	 * Returns an instance of INavigatorContentService configured 
	 * for the given id. Instances are not shared for the same 
	 * viewerId. 
	 * 
	 * @param aViewerId The viewer id of interest
	 * @return An instance of INavigatorContentService configured for the given id. 
	 */
	public INavigatorContentService createContentService(String aViewerId) { 
		return createContentService(aViewerId, null);
	}
	
	/**
	 * Returns an instance of INavigatorContentService configured 
	 * for the given id. Instances are not shared for the same 
	 * viewerId. 
	 * 
	 * @param aViewerId The viewer id of interest
	 * @param aViewer The content service can use the given viewer to initialize content providers 
	 * @return An instance of INavigatorContentService configured for the given id. 
	 * @see IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, Object, Object)
	 */
	public INavigatorContentService createContentService(String aViewerId, StructuredViewer aViewer) {
		if(aViewer == null)
			return new NavigatorContentService(aViewerId);
		return new NavigatorContentService(aViewerId, aViewer);
	}

}
