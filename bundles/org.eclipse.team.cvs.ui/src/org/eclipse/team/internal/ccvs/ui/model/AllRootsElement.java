package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class AllRootsElement extends CVSModelElement implements IAdaptable {
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}
	public Object[] getChildren(Object o) {
		return CVSUIPlugin.getPlugin().getRepositoryManager().getKnownRoots();
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

