/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui.target;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.model.IWorkbenchAdapter;

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
	
	public Site getSite() {
		return site;
	}

	public int hashCode() {
		return site.hashCode();
	}
	
	public ImageDescriptor getImageDescriptor(Object object) {
		return TeamImages.getImageDescriptor(ISharedImages.IMG_SITE_ELEMENT);
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