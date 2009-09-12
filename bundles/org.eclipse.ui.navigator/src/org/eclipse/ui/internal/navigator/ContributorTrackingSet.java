/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator;

import java.util.LinkedHashSet;

import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;

/**
 * @since 3.2
 *
 */
public class ContributorTrackingSet extends LinkedHashSet {

	
	private static final long serialVersionUID = 2516241537206281972L;
	
	private NavigatorContentDescriptor contributor;
	private NavigatorContentService contentService;
	
	/**
	 * Construct a tracking set.
	 * 
	 * @param aContentService 
	 */
	public ContributorTrackingSet(NavigatorContentService aContentService) {
		contentService = aContentService;
	}
	
	/**
	 * Construct a tracking set.
	 * 
	 * @param aContentService
	 * @param elements
	 */
	public ContributorTrackingSet(NavigatorContentService aContentService, Object[] elements) {
		
		for (int i = 0; i < elements.length; i++) 
			super.add(elements[i]); 
		
		contentService = aContentService;
	}
	
	/* (non-Javadoc)
	 * @see java.util.HashSet#add(java.lang.Object)
	 */
	public boolean add(Object o) { 
		if(contributor != null)
			contentService.rememberContribution(contributor, o);
		return super.add(o);
	}
	
	/* (non-Javadoc)
	 * @see java.util.HashSet#remove(java.lang.Object)
	 */
	public boolean remove(Object o) { 
		contentService.forgetContribution(o);
		return super.remove(o);
	}

	/**
	 * 
	 * @return The current contributor.
	 */
	public NavigatorContentDescriptor getContributor() {
		return contributor;
	}

	/**
	 * 
	 * @param newContributor The contributor to record for the next series of adds.
	 */
	public void setContributor(NavigatorContentDescriptor newContributor) {
		contributor = newContributor;
	}

	/**
	 * @param contents
	 */
	public void setContents(Object[] contents) {
		super.clear();
		if(contents != null) 
			for (int i = 0; i < contents.length; i++) 
				super.add(contents[i]); 
		
	}
}
