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
import org.eclipse.team.internal.ccvs.ui.Policy;
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

	/**
	 * Returns a message for the status line describing this sync set.
	 * 
	 * Override the method in SyncSet to add information about new resources
	 */
	public String getStatusLineMessage() {
		int incoming = 0;
		int outgoing = 0;
		int conflicts = 0;
		int newResources = 0;
		ITeamNode[] nodes = getChangedNodes();
		for (int i = 0; i < nodes.length; i++) {
			ITeamNode next = nodes[i];
			switch (next.getChangeDirection()) {
				case IRemoteSyncElement.INCOMING:
					incoming++;
					break;
				case IRemoteSyncElement.OUTGOING:
					outgoing++;
					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(next.getResource());
					try {
						if (cvsResource.exists()) {
							if (cvsResource.isFolder()) {
								if (!((ICVSFolder)cvsResource).isCVSFolder()) {
									newResources++;
								}
							} else if (!cvsResource.isManaged()) {
								newResources++;
							}
						}
					} catch (CVSException e) {
						CVSUIPlugin.log(e.getStatus());
					}
					break;
				case IRemoteSyncElement.CONFLICTING:
					conflicts++;
					break;
			}
		}
		StringBuffer result = new StringBuffer();
		
		if (conflicts == 0) {
			result.append(Policy.bind("CVSSyncSet.noConflicts")); //$NON-NLS-1$
		} else {
			result.append(Policy.bind("CVSSyncSet.conflicts", new Object[] {Integer.toString(conflicts)} )); //$NON-NLS-1$
		}
		if (incoming == 0) {
			result.append(Policy.bind("CVSSyncSet.noIncomings")); //$NON-NLS-1$
		} else {
			result.append(Policy.bind("CVSSyncSet.incomings", new Object[] {Integer.toString(incoming)} )); //$NON-NLS-1$
		}
		if (outgoing == 0) {
			result.append(Policy.bind("CVSSyncSet.noOutgoings")); //$NON-NLS-1$
		} else {
			result.append(Policy.bind("CVSSyncSet.outgoings", new Object[] {Integer.toString(outgoing)} )); //$NON-NLS-1$
		}
		if (newResources == 0) {
			result.append(Policy.bind("CVSSyncSet.noNew")); //$NON-NLS-1$
		} else {
			result.append(Policy.bind("CVSSyncSet.new", new Object[] {Integer.toString(newResources)} )); //$NON-NLS-1$
		}

		return result.toString();
	}
}
