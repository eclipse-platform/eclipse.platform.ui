package org.eclipse.team.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.ITypedElement;
import org.eclipse.swt.graphics.Image;

/**
 * A content buffer for resources on a server.
 */
abstract class TypedBufferedContent extends BufferedContent implements ITypedElement, IEditableContent {
	private ITeamNode node;
	private boolean editable;
	
	/**
	 * Creates a new content buffer for the given team node.
	 */
	TypedBufferedContent(ITeamNode node, boolean editable) {
		this.node = node;
		this.editable = editable;
	}
	
	public Image getImage() {
		return node.getImage();
	}

	public String getName() {
		return node.getName();
	}

	public String getType() {
		return node.getType();
	}

	/**
	 * Returns true if this object can be modified.
	 * If it returns <code>false</code> the other methods must not be called.
	 * 
	 * @return <code>true</code> if this object can be modified.
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * This is not the definitive API!
	 * This method is called on a parent to
	 * - add a child,
	 * - remove a child,
	 * - copy the contents of a child
	 * 
	 * What to do is encoded in the two arguments as follows:
	 * add:	child == null		other != null
	 * remove:	child != null		other == null
	 * copy:	child != null		other != null
	 */
	public ITypedElement replace(ITypedElement child, ITypedElement other) {
		return null;
	}
}
