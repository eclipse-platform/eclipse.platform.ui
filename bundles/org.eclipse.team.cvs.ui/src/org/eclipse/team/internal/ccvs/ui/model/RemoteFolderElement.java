package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class RemoteFolderElement extends RemoteResourceElement {
	public Object[] getChildren(final Object o) {
		if (!(o instanceof ICVSRemoteFolder)) return null;
		final Object[][] result = new Object[1][];
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					result[0] = ((ICVSRemoteFolder)o).members(new NullProgressMonitor());
				} catch (TeamException e) {
					handle(e);
				}
			}
		});
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
				return folder.getName() + " " + tag.getName();
			}
		}
		return folder.getName();
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof ICVSRemoteFolder)) return null;
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}
}
