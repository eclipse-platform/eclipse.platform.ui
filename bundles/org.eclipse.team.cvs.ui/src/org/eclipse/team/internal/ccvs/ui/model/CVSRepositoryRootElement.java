package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryRoot;

/**
 * RemoteRootElement is the model element for a repository that
 * appears in the repositories view. Its children are:
 * a) HEAD
 * b) Branch tags category
 * c) Version tags category
 */
public class CVSRepositoryRootElement extends CVSModelElement {
	public ImageDescriptor getImageDescriptor(Object object) {
		if (object instanceof ICVSRepositoryLocation || object instanceof RepositoryRoot) {
			return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_REPOSITORY);
		}
		return null;
	}
	public String getLabel(Object o) {
		if (o instanceof ICVSRepositoryLocation) {
			ICVSRepositoryLocation root = (ICVSRepositoryLocation)o;
			return root.getLocation();
		}
		if (o instanceof RepositoryRoot) {
			RepositoryRoot root = (RepositoryRoot)o;
			String name = root.getName();
			if (name == null)
				return root.getRoot().getLocation();
			else
				return name;
		}
		return null;
	}
	public Object getParent(Object o) {
		return null;
	}
	public Object[] internalGetChildren(Object o, IProgressMonitor monitor) {
		ICVSRepositoryLocation location = null;
		if (o instanceof ICVSRepositoryLocation) {
			location = (ICVSRepositoryLocation)o;
		}
		if (o instanceof RepositoryRoot) {
			RepositoryRoot root = (RepositoryRoot)o;
			location = root.getRoot();
		}
		if (location == null) return null;
		return new Object[] {
			new CVSTagElement(CVSTag.DEFAULT, location),
			new BranchCategory(location),
			new VersionCategory(location)
		};
	}
}