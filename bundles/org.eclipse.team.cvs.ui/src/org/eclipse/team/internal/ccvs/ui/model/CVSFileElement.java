package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.ui.PlatformUI;

public class CVSFileElement extends CVSResourceElement {
	
	private ICVSFile file;
	
	public CVSFileElement(ICVSFile file) {
		this.file = file;
	}
	
	/**
	 * Initial implementation: return null;
	 */
	public Object[] getChildren(Object o) {
		return new Object[0];
	}
	/**
	 * Initial implementation: return null.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(file.getName());
	}
	/**
	 * Initial implementation: return the file's name and version
	 */
	public String getLabel(Object o) {
		try {
			ResourceSyncInfo info = file.getSyncInfo();
			if(info!=null) {
				return file.getName() + " " + info.getRevision(); //$NON-NLS-1$
			} else {
				return file.getName();
			}
		} catch (TeamException e) {
			handle(e);
			return null;
		}
	}
	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object o) {
		return null;
	}
	
	public ICVSFile getCVSFile() {
		return file;
	}
	/**
	 * @see CVSResourceElement#getCVSResource()
	 */
	public ICVSResource getCVSResource() {
		return file;
	}
}