package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class RemoteFolderElement extends RemoteResourceElement {
	/**
	 * Initial implementation: return members
	 */
	public Object[] getChildren(Object o) {
		if (!(o instanceof ICVSRemoteFolder)) return null;
		try {
			return ((ICVSRemoteFolder)o).getMembers(new NullProgressMonitor());
		} catch (TeamException e) {
			return null;
		}
	}
	/**
	 * Initial implementation: return null.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof ICVSRemoteFolder)) return null;
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}
}

