package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * Extension to the generic workbench content provider mechanism
 * to lazily determine whether an element has children.  That is,
 * children for an element aren't fetched until the user clicks
 * on the tree expansion box.
 */
public class RemoteContentProvider extends WorkbenchContentProvider {
	/* (non-Javadoc)
	 * Method declared on WorkbenchContentProvider.
	 */
	public boolean hasChildren(Object element) {
		if (element == null) {
			return false;
		}
		// the + box will always appear, but then disappear
		// if not needed after you first click on it.
		if (element instanceof ICVSRemoteResource) {
			if (element instanceof ICVSRemoteFolder) {
				return ((ICVSRemoteFolder)element).isExpandable();
			}
			return ((ICVSRemoteResource)element).isContainer();
		} else if(element instanceof CVSResourceElement) {
			ICVSResource r = ((CVSResourceElement)element).getCVSResource();
			if(r instanceof RemoteResource) {
				return r.isFolder();
			}
		} else if(element instanceof VersionCategory) {
			return true;
		} else if(element instanceof BranchTag) {
			return true;
		} else if(element instanceof RemoteModule) {
			return true;
		}
		return super.hasChildren(element);
	}
}
