package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;

public abstract class RemoteResourceElement extends CVSModelElement {
	/**
	 * Initial implementation: return the resource's name
	 */
	public String getLabel(Object o) {
		if (!(o instanceof ICVSRemoteResource)) return null;
		return ((ICVSRemoteResource)o).getName();
	}
	/**
	 * Return null.
	 */
	public Object getParent(Object o) {
		if (!(o instanceof ICVSRemoteResource)) return null;
		return null;
	}
}