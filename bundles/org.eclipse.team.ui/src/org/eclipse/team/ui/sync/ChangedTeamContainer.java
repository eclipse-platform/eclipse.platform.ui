package org.eclipse.team.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * 
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
		return "ChangedTeamContainer(" + getResource().getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
