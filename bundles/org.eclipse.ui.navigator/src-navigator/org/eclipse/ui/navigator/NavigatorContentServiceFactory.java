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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.navigator.internal.NavigatorContentService;


/**
 * Provides a factory pattern for creating {@link INavigatorContentService}s 
 * for given viewer ids.  
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2  
 *
 */
public final class NavigatorContentServiceFactory {
	
	public static final NavigatorContentServiceFactory INSTANCE = new NavigatorContentServiceFactory(); 
	
	
	public INavigatorContentService createContentService(String aViewerId) { 
		return createContentService(aViewerId, null);
	}
	
	public INavigatorContentService createContentService(String aViewerId, StructuredViewer aViewer) {
		if(aViewer == null)
			return new NavigatorContentService(aViewerId);
		return new NavigatorContentService(aViewerId, aViewer);
	}

}
