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
	private boolean showFiles = true;
	
	public SiteElement(Site site) {
		this.site = site;
	}
	
	public SiteElement(Site site, boolean showFiles) {
		this.site = site;
		this.showFiles = showFiles;
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
			return new RemoteResourceElement(site.getRemoteResource(), showFiles).getChildren(this);
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