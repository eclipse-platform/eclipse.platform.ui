package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class TagElement implements IWorkbenchAdapter, IAdaptable {
	CVSTag tag;
	public TagElement(CVSTag tag) {
		this.tag = tag;
	}
	public Object[] getChildren(Object o) {
		return new Object[0];
	}
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	public ImageDescriptor getImageDescriptor(Object object) {
		if (tag.getType() == CVSTag.BRANCH || tag == CVSTag.DEFAULT) {
			return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_TAG);
		} else {
			return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_PROJECT_VERSION);
		}
	}
	public String getLabel(Object o) {
		return tag.getName();
	}
	public Object getParent(Object o) {
		return null;
	}
	public CVSTag getTag() {
		return tag;
	}
}
