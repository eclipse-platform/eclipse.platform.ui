package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncSet;

/**
 * This class contains a set of CVS resources that are slated to be
 * synchronized. This adds CVS specific handling to the common sync set
 * class, specifically to deal with non-added outgoing changes.
 */
public class CVSSyncSet extends SyncSet {
	
	/**
	 * Creates a new sync set on the nodes in the given selection.
	 */
	public CVSSyncSet(IStructuredSelection nodeSelection) {
		super(nodeSelection);
	}
	
	public boolean hasNonAddedChanges() {
		for (Iterator it = getSyncSet().iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(node.getResource());
			if(cvsResource.isFolder()) {
				if(! ((ICVSFolder)cvsResource).isCVSFolder()) {
					return true;
				}
			} else if(! cvsResource.isManaged()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasCommitableChanges() {
		for (Iterator it = getSyncSet().iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			// outgoing file that is added is a commit candidate
			if (node.getChangeDirection() == IRemoteSyncElement.OUTGOING) {
				ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(node.getResource());
				if(!cvsResource.isFolder() && cvsResource.isManaged()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean removeNonAddedChanges() {
		for (Iterator it = getSyncSet().iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(node.getResource());
			if(cvsResource.isFolder()) {
				if(!((ICVSFolder)cvsResource).isCVSFolder()) {
					it.remove();
				}
			} else {
				if(!cvsResource.isManaged()) {
					it.remove();
				}
			}
		}
		return false;
	}
	
	public boolean removeAddedChanges() {
		for (Iterator it = getSyncSet().iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(node.getResource());
			if(cvsResource.isFolder()) {
				if(((ICVSFolder)cvsResource).isCVSFolder()) {
					it.remove();
				}
			} else {
				if(cvsResource.isManaged()) {
					it.remove();
				}
			}
		}
		return false;
	}
}
