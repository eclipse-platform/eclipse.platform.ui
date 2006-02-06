/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2 
 *
 */
public final class NavigatorContentServiceDescriptionProvider implements
		IDescriptionProvider { 
	
	private final NavigatorContentService contentService;

	public NavigatorContentServiceDescriptionProvider(NavigatorContentService aContentService) { 
		Assert.isNotNull(aContentService); 
		contentService = aContentService;		
	}	
	
	public String getDescription(Object anElement) { 
		
		Object target;
		
		if(anElement instanceof IStructuredSelection) {

			IStructuredSelection structuredSelection = (IStructuredSelection) anElement;
			if (structuredSelection.size() > 1) {
				return getDefaultStatusBarMessage(structuredSelection.size()); 
			} 
			target = structuredSelection.getFirstElement();
		} else {
			target = anElement;
		}
		String message = null;
		ILabelProvider[] providers = contentService.findRelevantLabelProviders(target);
		if (providers.length == 0)
			return getDefaultStatusBarMessage(0);
		for(int i=0; i<providers.length; i++) {
			if(providers[i] instanceof ICommonLabelProvider) {
				message =  ((ICommonLabelProvider)providers[i]).getDescription(target);
				break;
			}  
		}
		message = (message != null) ? message : getDefaultStatusBarMessage(1);
		return message;
		 
	}
	
	/**
	 * @param aStructuredSelection The current selection from the {@link CommonViewer}
	 * @return A string of the form "# items selected"
	 */
	protected final String getDefaultStatusBarMessage(int aSize) {
		return NLS.bind(CommonNavigatorMessages.Navigator_statusLineMultiSelect, new Object[]{new Integer(aSize)});

	}

}
