package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.ccvs.core.IRemoteFile;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.PlatformUI;

public class RemoteFileElement extends RemoteResourceElement {
	/**
	 * Initial implementation: return null;
	 */
	public Object[] getChildren(Object o) {
		return null;
	}
	/**
	 * Initial implementation: return null.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof IRemoteFile)) return null;
		return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(((IRemoteFile)object).getName());
	}
	/**
	 * Initial implementation: return the file's name and version
	 */
	public String getLabel(Object o) {
		if (!(o instanceof IRemoteFile)) return null;
		IRemoteFile file = (IRemoteFile)o;
		try {
			return file.getName() + " " + file.getRevision(new NullProgressMonitor());
		} catch (TeamException e) {
			CVSUIPlugin.log(e.getStatus());
			return null;
		}
	}
}