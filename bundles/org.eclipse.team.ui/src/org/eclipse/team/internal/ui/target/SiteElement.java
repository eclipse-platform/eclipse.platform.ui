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
import org.eclipse.team.core.target.Site;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class SiteElement implements IWorkbenchAdapter, IAdaptable {
	private Site site;
	private int showMask = RemoteResourceElement.SHOW_FILES | RemoteResourceElement.SHOW_FOLDERS;
	
	public SiteElement(Site site) {
		this.site = site;
	}
	
	public SiteElement(Site site, int showMask) {
		this.site = site;
		this.showMask = showMask;
	}

	public Site getSite() {
		return site;
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}

	public int hashCode() {
		return site.hashCode();
	}

	public Object[] getChildren(Object o) {
		try {
			return new RemoteResourceElement(site.getRemoteResource(), showMask).getChildren(this);
		} catch (TeamException e) {
			TeamUIPlugin.handle(e);
			return new Object[0];
		}
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
		if(obj == null) return false;
		if(!(obj instanceof SiteElement)) return false;
		Site otherSite = ((SiteElement)obj).getSite();
		return getSite().equals(otherSite);
	}
}