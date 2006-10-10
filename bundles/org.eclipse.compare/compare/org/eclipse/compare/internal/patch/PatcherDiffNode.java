/**
 * 
 */
package org.eclipse.compare.internal.patch;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;

public class PatcherDiffNode extends DiffNode {

	// Diff associated with this DiffNode
	private Diff diff = null;

	// Hunk associated with this DiffNode
	private Hunk hunk = null;

	// The name of this node
	private String name = null;

	public PatcherDiffNode(IDiffContainer parent, int kind,
			ITypedElement ancestor, ITypedElement left, ITypedElement right,
			Diff diff) {
		super(parent, kind, ancestor, left, right);
		this.diff = diff;
		name = diff.getLabel(diff);
	}

	public PatcherDiffNode(IDiffContainer parent, int kind,
			ITypedElement ancestor, ITypedElement left, ITypedElement right,
			Hunk hunk) {
		super(parent, kind, ancestor, left, right);
		this.hunk = hunk;
		name = hunk.getLabel(hunk);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	Diff getDiff() {
		return diff;
	}

	Hunk getHunk() {
		return hunk;
	}

}
