package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.ccvs.core.IRemoteRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;

public class RemoteRootElement extends RemoteFolderElement {
	/**
	 * Initial implementation: return null.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof IRemoteRoot)) return null;
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_REPOSITORY);
	}
	/**
	 * Initial implementation. Doesn't handle ports.
	 */
	public String getLabel(Object o) {
		if (!(o instanceof IRemoteRoot)) return null;
		IRemoteRoot root = (IRemoteRoot)o;
		StringBuffer result = new StringBuffer();
		result.append(":");
		result.append(root.getConnectionMethod());
		result.append(":");
		result.append(root.getUser());
		result.append("@");
		result.append(root.getHost());
		result.append(":");
		result.append(root.getRepositoryPath());
		return result.toString();
	}
	/**
	 * Return null.
	 */
	public Object getParent(Object o) {
		return null;
	}
}