package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;

/**
 * RemoteRootElement is the model element for a repository that
 * appears in the repositories view. Its children are:
 * a) HEAD
 * b) Branch tags category
 * c) Version tags category
 */
public class CVSRepositoryRootElement extends CVSModelElement {
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof ICVSRepositoryLocation)) return null;
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_REPOSITORY);
	}
	public String getLabel(Object o) {
		if (!(o instanceof ICVSRepositoryLocation)) return null;
		ICVSRepositoryLocation root = (ICVSRepositoryLocation)o;
		return root.getLocation();
	}
	public Object getParent(Object o) {
		return null;
	}
	public Object[] getChildren(Object o) {
		if (!(o instanceof ICVSRepositoryLocation)) return null;
		return new Object[] {
			new BranchTag(CVSTag.DEFAULT, (ICVSRepositoryLocation)o),
			new BranchCategory((ICVSRepositoryLocation)o),
			new VersionCategory((ICVSRepositoryLocation)o)
		};
	}
}