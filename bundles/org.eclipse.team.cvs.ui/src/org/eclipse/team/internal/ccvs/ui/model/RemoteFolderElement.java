package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class RemoteFolderElement extends RemoteResourceElement {

	/**
	 * Overridden to append the version name to remote folders which
	 * have version tags and are top-level folders.
	 */
	public String getLabel(Object o) {
		if (!(o instanceof ICVSRemoteFolder)) return null;
		ICVSRemoteFolder folder = (ICVSRemoteFolder)o;
		if (CVSUIPlugin.getPlugin().getRepositoryManager().isDisplayingProjectVersions(folder.getRepository())) {
			CVSTag tag = folder.getTag();
			if (tag != null && tag.getType() == CVSTag.VERSION) {
				if (folder.getRemoteParent() == null) {
					return Policy.bind("RemoteFolderElement.nameAndTag", folder.getName(), tag.getName()); //$NON-NLS-1$
				}
			}
		}
		return folder.getName();
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof ICVSRemoteFolder)) return null;
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.model.CVSModelElement#internalGetChildren(java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Object[] internalGetChildren(Object o, IProgressMonitor monitor) throws TeamException {
		if (!(o instanceof ICVSRemoteFolder)) return new Object[0];
		return ((ICVSRemoteFolder)o).members(monitor);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.model.CVSModelElement#isNeedsProgress()
	 */
	public boolean isNeedsProgress() {
		return true;
	}

}
