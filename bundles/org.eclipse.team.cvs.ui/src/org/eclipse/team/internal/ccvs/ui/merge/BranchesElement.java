package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class BranchesElement implements IWorkbenchAdapter, IAdaptable {
	ICVSRemoteFolder remote;
	public BranchesElement(ICVSRemoteFolder remote) {
		this.remote = remote;
	}
	public Object[] getChildren(Object o) {
		BranchTag[] tags = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownBranchTags(remote.getRepository());
		TagElement[] result = new TagElement[tags.length];
		for (int i = 0; i < tags.length; i++) {
			result[i] = new TagElement(tags[i].getTag());
		}
		return result;
	}
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_BRANCHES_CATEGORY);
	}
	public String getLabel(Object o) {
		return Policy.bind("MergeWizardEndPage.branches");
	}
	public Object getParent(Object o) {
		return null;
	}
}