package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
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
	
	public ITeamNode[] getNonAddedNodes() throws CVSException {
		List result = new ArrayList();
		for (Iterator it = getSyncSet().iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(node.getResource());
			if (cvsResource.isFolder()) {
				if (!((ICVSFolder)cvsResource).isCVSFolder()) {
					result.add(node);
				}
			} else if (!cvsResource.isManaged()) {
				result.add(node);
			}
		}
		return (ITeamNode[])result.toArray(new ITeamNode[result.size()]);
	}
	
	public boolean hasNonAddedChanges() throws CVSException {
		for (Iterator it = getSyncSet().iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(node.getResource());
			if (cvsResource.exists()) {
				if (cvsResource.isFolder()) {
					if (!((ICVSFolder)cvsResource).isCVSFolder()) {
						return true;
					}
				} else if (!cvsResource.isManaged()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean removeNonAddedChanges() {
		for (Iterator it = getSyncSet().iterator(); it.hasNext();) {
			try {
				ITeamNode node = (ITeamNode)it.next();
				ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(node.getResource());
				if (cvsResource.exists()) {
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
			} catch (CVSException e) {
				// isManaged or isCVSFolder threw an exception
				// Log it and continue
				CVSUIPlugin.log(e.getStatus());
			}
		}
		return false;
	}
	
	public boolean removeAddedChanges() {
		for (Iterator it = getSyncSet().iterator(); it.hasNext();) {
			try {
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
			} catch (CVSException e) {
				// isManaged or isCVSFolder threw an exception
				// Log it and continue
				CVSUIPlugin.log(e.getStatus());
			}
		}
		return false;
	}
}
