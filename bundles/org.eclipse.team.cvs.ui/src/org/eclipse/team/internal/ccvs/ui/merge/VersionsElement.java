package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class VersionsElement implements IWorkbenchAdapter, IAdaptable {
	ICVSRemoteFolder remote;
	public VersionsElement(ICVSRemoteFolder remote) {
		this.remote = remote;
	}
	public Object[] getChildren(Object o) {
		final Object[][] result = new Object[1][];
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			// This is inefficient; need API to get remote resource for a given tag
			public void run() {
				try {
					CVSTag[] tags = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownVersionTags(remote, new NullProgressMonitor());
					TagElement[] elements = new TagElement[tags.length];
					for (int i = 0; i < elements.length; i++) {
						elements[i] = new TagElement(tags[i]);
					}
					result[0] = elements;
				} catch (TeamException e) {
					// To do
				}
			}
		});
		return result[0];
	}
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_VERSIONS_CATEGORY);
	}
	public String getLabel(Object o) {
		return Policy.bind("VersionsElement.versions");
	}
	public Object getParent(Object o) {
		return null;
	}
}
