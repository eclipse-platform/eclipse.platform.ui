package org.eclipse.team.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;

/**
 * A node in a sync tree that represents a changed folder
 * (incoming/outgoing creation or deletion).
 */
public class ChangedTeamContainer extends UnchangedTeamContainer {
	private MergeResource mergeResource;
	
	/**
	 * ChangedTeamContainer constructor
	 */
	public ChangedTeamContainer(IDiffContainer parent, MergeResource resource, int description) {
		super(parent, resource.getResource(), description);
		this.mergeResource = resource;
	}
	
	/*
	 * Method declared on ITeamNode.
	 */
	public int getChangeDirection() {
		return getKind() & Differencer.DIRECTION_MASK;
	}

	public int getChangeType() {
		return getKind() & Differencer.CHANGE_TYPE_MASK;
	}

	public String getName() {
		return mergeResource.getName();
	}

	/**
	 * Returns the team resource managed by this object.
	 */
	public MergeResource getMergeResource() {
		return mergeResource;
	}

	/*
	 * Method declared on IDiffContainer
	 */
	public void removeToRoot(IDiffElement child) {
		// Don't want to remove empty changed containers
		remove(child);
	}

	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		return "ChangedTeamContainer(" + getResource().getName() + ")";
	}
}
