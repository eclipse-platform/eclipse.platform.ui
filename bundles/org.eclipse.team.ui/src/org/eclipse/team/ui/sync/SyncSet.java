package org.eclipse.team.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.Policy;

/**
 * This class contains a set of resources that are slated to be
 * synchronized.  It performs various operations on the
 * set in preparation for catchup/release.
 */
public class SyncSet {
	private HashSet set;
	
	/**
	 * Creates a new sync set on the nodes in the given selection.
	 */
	public SyncSet(IStructuredSelection nodeSelection) {
		this.set = new HashSet(nodeSelection.size() + 1);
		collectNodes(nodeSelection);
	}

	/**
	 * Collects all nodes to which this action will apply.  This means
	 * all nodes in the selection, plus all their children.
	 */
	private void collectNodes(IStructuredSelection selection) {
		Object[] nodes = selection.toArray();
		for (int i = 0; i < nodes.length; i++) {
			recursivelyAdd((ITeamNode)nodes[i]);
		}
	}
	
	/**
	 * Adds all parent creations for the given node to the sync set.
	 */
	private void collectParentCreations(ITeamNode node) {
		IDiffElement parent = node.getParent();
		if (parent != null && parent instanceof ITeamNode) {
			if (parent.getKind() != IRemoteSyncElement.IN_SYNC) {
				set.add(parent);
				collectParentCreations((ITeamNode)parent);
			}
		}
	}

	/**
	 * Returns all nodes in the set that have changes
	 */
	public ITeamNode[] getChangedNodes() {
		ArrayList nodeList = new ArrayList(set.size());
		for (Iterator it = set.iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			int dir = node.getChangeDirection();
			// We assume changed nodes of the wrong type have been culled
			// during set creation.
			if (dir != Differencer.NO_CHANGE) {
				nodeList.add(node);
			}
		}
		ITeamNode[] nodes = new ITeamNode[nodeList.size()];
		nodeList.toArray(nodes);
		return nodes;
	}
	
	/**
	 * Returns the resources from all the nodes in this set.
	 */
	public IResource[] getResources() {
		ITeamNode[] changed = getChangedNodes();
		IResource[] resources = new IResource[changed.length];
		for (int i = 0; i < changed.length; i++) {
			resources[i] = changed[i].getResource();
		}
		return resources;
	}
	
	/**
	 * Returns a message for the status line describing this sync set.
	 */
	public String getStatusLineMessage() {
		int incoming = 0;
		int outgoing = 0;
		int conflicts = 0;
		for (Iterator it = set.iterator(); it.hasNext();) {
			ITeamNode next = (ITeamNode)it.next();
			switch (next.getChangeDirection()) {
				case IRemoteSyncElement.INCOMING:
					incoming++;
					break;
				case IRemoteSyncElement.OUTGOING:
					outgoing++;
					break;
				case IRemoteSyncElement.CONFLICTING:
					conflicts++;
					break;
			}
		}
		StringBuffer result = new StringBuffer();
		
		if (conflicts == 0) {
			result.append(Policy.bind("SyncSet.noConflicts")); //$NON-NLS-1$
		} else {
			result.append(Policy.bind("SyncSet.conflicts", new Object[] {Integer.toString(conflicts)} )); //$NON-NLS-1$
		}
		if (incoming == 0) {
			result.append(Policy.bind("SyncSet.noIncomings")); //$NON-NLS-1$
		} else {
			result.append(Policy.bind("SyncSet.incomings", new Object[] {Integer.toString(incoming)} )); //$NON-NLS-1$
		}
		if (outgoing == 0) {
			result.append(Policy.bind("SyncSet.noOutgoings")); //$NON-NLS-1$
		} else {
			result.append(Policy.bind("SyncSet.outgoings", new Object[] {Integer.toString(outgoing)} )); //$NON-NLS-1$
		}
		return result.toString();
	}
	
	/**
	 * Returns true if there are any conflicting nodes in the set, and
	 * false otherwise.
	 */
	public boolean hasConflicts() {
		for (Iterator it = set.iterator(); it.hasNext();) {
			if (((ITeamNode)it.next()).getChangeDirection() == IRemoteSyncElement.CONFLICTING) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if this sync set has incoming changes.
	 * Note that conflicts are not considered to be incoming changes.
	 */
	public boolean hasIncomingChanges() {
		for (Iterator it = set.iterator(); it.hasNext();) {
			if (((ITeamNode)it.next()).getChangeDirection() == IRemoteSyncElement.INCOMING) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this sync set has outgoing changes.
	 * Note that conflicts are not considered to be outgoing changes.
	 */
	public boolean hasOutgoingChanges() {
		for (Iterator it = set.iterator(); it.hasNext();) {
			if (((ITeamNode)it.next()).getChangeDirection() == IRemoteSyncElement.OUTGOING) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this sync set has auto-mergeable conflicts.
	 */
	public boolean hasAutoMergeableConflicts() {
		for (Iterator it = set.iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			if ((node.getKind() & IRemoteSyncElement.AUTOMERGE_CONFLICT) != 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds the given node, plus all its children, to the given set.
	 */
	private void recursivelyAdd(ITeamNode node) {
		// Add the node and recurse
		if (set.add(node)) {
			if (node instanceof IDiffContainer) {
				IDiffElement[] children = ((IDiffContainer)node).getChildren();
				for (int i = 0; i < children.length; i++) {
					if (children[i] instanceof ITeamNode) {
						recursivelyAdd((ITeamNode)children[i]);
					}
				}
			}
			// Add any created parents (can't release or load a 
			// resource creation without including new parents)
			collectParentCreations(node);
		}
	}
	
	/**
	 * Removes all conflicting nodes from this set.
	 */
	public void removeConflictingNodes() {
		for (Iterator it = set.iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			if (node.getChangeDirection() == IRemoteSyncElement.CONFLICTING) {
				it.remove();
			}
		}
	}
	/**
	 * Removes all outgoing nodes from this set.
	 */
	public void removeOutgoingNodes() {
		for (Iterator it = set.iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			if (node.getChangeDirection() == IRemoteSyncElement.OUTGOING) {
				it.remove();
			}
		}
	}
	/**
	 * Removes all incoming nodes from this set.
	 */
	public void removeIncomingNodes() {
		for (Iterator it = set.iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			if (node.getChangeDirection() == IRemoteSyncElement.INCOMING) {
				it.remove();
			}
		}
	}
	/**
	 * Removes all conflicting nodes from this set that are not auto-mergeable
	 */
	public void removeNonMergeableNodes() {
		for (Iterator it = set.iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			if ((node.getKind() & IRemoteSyncElement.MANUAL_CONFLICT) != 0) {
				it.remove();
			}
		}
	}
	
	/**
	 * Removes all nodes that aren't applicable for the direction.
	 */
	public void removeNonApplicableNodes(int direction) {
		for (Iterator it = set.iterator(); it.hasNext();) {
			ITeamNode node = (ITeamNode)it.next();
			int nodeDirection = node.getKind() & IRemoteSyncElement.DIRECTION_MASK;
			if (nodeDirection != IRemoteSyncElement.CONFLICTING) {
				if (nodeDirection != direction) {
					it.remove();
				}
			}
		}
	}
	
	/**
	 * Remove the given node from the sync set
	 */
	public void remove(ITeamNode node) {
		set.remove(node);
	}
}
