package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.ui.model.IWorkbenchAdapter;

public abstract class CVSResourceElement extends CVSModelElement implements IAdaptable {
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	/**
	 * Initial implementation: return the resource's name
	 */
	public String getLabel(Object o) {
		if (!(o instanceof ICVSResource)) return null;
		return ((ICVSResource)o).getName();
	}
	/**
	 * Return null.
	 */
	public Object getParent(Object o) {
		if (!(o instanceof ICVSResource)) return null;
		return null;
	}
	
	abstract public ICVSResource getCVSResource();
}