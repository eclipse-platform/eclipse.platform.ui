package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class RemoteFolderElement extends RemoteResourceElement {
	public Object[] getChildren(final Object o) {
		if (!(o instanceof ICVSRemoteFolder)) return null;
		final Object[][] result = new Object[1][];
		try {
			CVSUIPlugin.runWithProgress(null, true /*cancelable*/, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						result[0] = ((ICVSRemoteFolder)o).members(monitor);
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException e) {
			return new Object[0];
		} catch (InvocationTargetException e) {
			handle(e.getTargetException());
		}
		return result[0];
	}
	/**
	 * Overridden to append the version name to remote folders which
	 * have version tags and are top-level folders.
	 */
	public String getLabel(Object o) {
		if (!(o instanceof ICVSRemoteFolder)) return null;
		ICVSRemoteFolder folder = (ICVSRemoteFolder)o;
		CVSTag tag = folder.getTag();
		if (tag != null && tag.getType() == CVSTag.VERSION) {
			if (folder.getRemoteParent() == null) {
				return Policy.bind("RemoteFolderElement.nameAndTag", folder.getName(), tag.getName()); //$NON-NLS-1$
			}
		}
		return folder.getName();
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof ICVSRemoteFolder)) return null;
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}
}
