package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.ccvs.core.IRemoteResource;

public abstract class RemoteResourceElement extends CVSModelElement {
	/**
	 * Initial implementation: return the resource's name
	 */
	public String getLabel(Object o) {
		if (!(o instanceof IRemoteResource)) return null;
		return ((IRemoteResource)o).getName();
	}
	/**
	 * Return null.
	 */
	public Object getParent(Object o) {
		if (!(o instanceof IRemoteResource)) return null;
		return ((IRemoteResource)o).getParent();
	}
}