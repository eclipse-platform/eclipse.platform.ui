package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;

public class RemoteRootElement extends RemoteFolderElement {
	/**
	 * Initial implementation: return null.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof ICVSRepositoryLocation)) return null;
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_REPOSITORY);
	}
	/**
	 * Initial implementation. Doesn't handle ports.
	 */
	public String getLabel(Object o) {
		if (!(o instanceof ICVSRepositoryLocation)) return null;
		ICVSRepositoryLocation root = (ICVSRepositoryLocation)o;
		return root.getLocation();
	}
	/**
	 * Return null.
	 */
	public Object getParent(Object o) {
		return null;
	}
	/**
	 * Return all tags
	 */
	public Object[] getChildren(Object o) {
		if (!(o instanceof ICVSRepositoryLocation)) return null;
		return CVSUIPlugin.getPlugin().getRepositoryManager().getKnownTags((ICVSRepositoryLocation)o);
	}
}