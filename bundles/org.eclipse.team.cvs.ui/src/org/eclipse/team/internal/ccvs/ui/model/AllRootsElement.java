package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * AllRootsElement is the model element for the repositories view.
 * Its children are the array of all known repository roots.
 */
public class AllRootsElement extends CVSModelElement implements IAdaptable {
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}
	public Object[] internalGetChildren(Object o, IProgressMonitor monitor) {
		return CVSUIPlugin.getPlugin().getRepositoryManager().getWorkingRepositoryRoots();
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

