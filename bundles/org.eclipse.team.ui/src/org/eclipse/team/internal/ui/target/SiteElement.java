/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.target;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.target.IRemoteTargetResource;
import org.eclipse.team.internal.core.target.Site;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.UIConstants;
import org.eclipse.team.ui.TeamImages;

/**
 * Used to show Site instances in the UI. Sites are really just remote
 * resources but are shown with a different icon and label.
 * 
 * @see RemoteResourceElement
 */
public class SiteElement extends RemoteResourceElement {
	private Site site;
	
	public SiteElement(Site site) {
		super(null);
		this.site = site;
	}
	
	public SiteElement(Site site, IRunnableContext runContext) {
		super(null, runContext);
		this.site = site;
	}
	
	public Site getSite() {
		return site;
	}

	public int hashCode() {
		return site.hashCode();
	}
	
	public ImageDescriptor getImageDescriptor(Object object) {
		return TeamImages.getImageDescriptor(UIConstants.IMG_SITE_ELEMENT);
	}
	
	public String getLabel(Object o) {
		return getSite().getDisplayName();
	}
	
	public Object getParent(Object o) {
		return null;
	}
	
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof SiteElement))
			return false;
		Site otherSite = ((SiteElement)obj).getSite();
		return getSite().equals(otherSite);
	}
	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object o) {
		try {
			setRemoteResource(site.getRemoteResource());
		} catch (TeamException e) {
			TeamUIPlugin.handle(e);
			return new Object[0];
		}
		return super.getChildren(this);
	}
	/**
	 * @see RemoteResourceElement#getRemoteResource()
	 */
	public IRemoteTargetResource getRemoteResource() {
		try {
			return site.getRemoteResource();
		} catch (TeamException e) {
			TeamUIPlugin.handle(e);
			return null;
		}
	}
}
