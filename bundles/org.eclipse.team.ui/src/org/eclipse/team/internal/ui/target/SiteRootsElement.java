package org.eclipse.team.internal.ui.target;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class SiteRootsElement implements IWorkbenchAdapter, IAdaptable {
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}
	public Object[] getChildren(Object o) {
		Site[] sites = TargetManager.getSites();
		SiteElement[] siteElements = new SiteElement[sites.length];
		for (int i = 0; i < sites.length; i++) {
			siteElements[i] = new SiteElement(sites[i]);
		}
		return siteElements;
	}
	public String getLabel(Object o) {
		return null;
	}
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	public Object getParent(Object o) {
		return null;
	}
}
