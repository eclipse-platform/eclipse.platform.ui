/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.structuremergeviewer;

import java.text.MessageFormat;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.util.ListenerList;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.Utilities;

/**
 * Diff node are used as the compare result of the differencing engine.
 * Since it implements the <code>ITypedElement</code> and <code>ICompareInput</code>
 * interfaces it can be used directly to display the
 * compare result in a <code>DiffTreeViewer</code> and as the input to any other
 * compare/merge viewer.
 * <p>
 * <code>DiffNode</code>s are typically created as the result of performing
 * a compare with the <code>Differencer</code>.
 * <p>
 * Clients typically use this class as is, but may subclass if required.
 * 
 * @see DiffTreeViewer
 * @see Differencer
 */
public class DiffNode extends DiffContainer implements ITypedElement, ICompareInput {

	private ITypedElement fAncestor;
	private ITypedElement fLeft;
	private ITypedElement fRight;
	private boolean fDontExpand;
	private ListenerList fListener;

	
	/**
	 * Creates a new <code>DiffNode</code> and initializes with the given values.
	 *
	 * @param parent under which the new container is added as a child or <code>null</code>
	 * @param kind of difference (defined in <code>Differencer</code>)
	 * @param ancestor the common ancestor input to a compare
	 * @param left the left input to a compare
	 * @param right the right input to a compare
	 */
	public DiffNode(IDiffContainer parent, int kind, ITypedElement ancestor, ITypedElement left, ITypedElement right) {
		this(parent, kind);
		fAncestor= ancestor;
		fLeft= left;
		fRight= right;
	}

	/**
	 * Creates a new <code>DiffNode</code> with diff kind <code>Differencer.CHANGE</code>
	 * and initializes with the given values.
	 *
	 * @param left the left input to a compare
	 * @param right the right input to a compare
	 */
	public DiffNode(ITypedElement left, ITypedElement right) {
		this(null, Differencer.CHANGE, null, left, right);
	}

	/**
	 * Creates a new <code>DiffNode</code> and initializes with the given values.
	 *
	 * @param kind of difference (defined in <code>Differencer</code>)
	 * @param ancestor the common ancestor input to a compare
	 * @param left the left input to a compare
	 * @param right the right input to a compare
	 */
	public DiffNode(int kind, ITypedElement ancestor, ITypedElement left, ITypedElement right) {
		this(null, kind, ancestor, left, right);
	}

	/**
	 * Creates a new <code>DiffNode</code> with the given diff kind.
	 *
	 * @param kind of difference (defined in <code>Differencer</code>)
	 */
	public DiffNode(int kind) {
		super(null, kind);
	}

	/**
	 * Creates a new <code>DiffNode</code> and initializes with the given values.
	 *
	 * @param parent under which the new container is added as a child or <code>null</code>
	 * @param kind of difference (defined in <code>Differencer</code>)
	 */
	public DiffNode(IDiffContainer parent, int kind) {
		super(parent, kind);
	}

	/**
	 * Registers a listener for changes of this <code>ICompareInput</code>.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener to add
	 */
	public void addCompareInputChangeListener(ICompareInputChangeListener listener) {
		if (fListener == null)
			fListener= new ListenerList();
		fListener.add(listener);
	}
	
	/**
	 * Unregisters a <code>ICompareInput</code> listener.
	 * Has no effect if listener is not registered.
	 *
	 * @param listener the listener to remove
	 */
	public void removeCompareInputChangeListener(ICompareInputChangeListener listener) {
		if (fListener != null) {
			fListener.remove(listener);
			if (fListener.isEmpty())
				fListener= null;
		}
	}
	
	/**
	 * Sends out notification that a change has occured on the <code>ICompareInput</code>.
	 */
	protected void fireChange() {
		if (fListener != null) {
			Object[] listeners= fListener.getListeners();
			for (int i= 0; i < listeners.length; i++)
				((ICompareInputChangeListener) listeners[i]).compareInputChanged(this);
		}
	}

	//---- getters & setters

	/**
	 * Returns <code>true</code> if this node shouldn't automatically be expanded in
	 * a </code>DiffTreeViewer</code>.
	 *
	 * @return <code>true</code> if node shouldn't automatically be expanded
	 */
	public boolean dontExpand() {
		return fDontExpand;
	}

	/**
	 * Controls whether this node is not automatically expanded when displayed in
	 * a </code>DiffTreeViewer</code>.
	 *
	 * @param dontExpand if <code>true</code> this node is not automatically expanded in </code>DiffTreeViewer</code>
	 */
	public void setDontExpand(boolean dontExpand) {
		fDontExpand= dontExpand;
	}

	/**
	 * Returns the first not-<code>null</code> input of this node.
	 * Method checks the three inputs in the order: ancestor, right, left.
	 *
	 * @return the first not-<code>null</code> input of this node
	 */
	public ITypedElement getId() {
		if (fAncestor != null)
			return fAncestor;
		if (fRight != null)
			return fRight;
		return fLeft;
	}

	/**
	 * Returns the (non-<code>null</code>) name of the left or right side if they are identical.
	 * Otherwise both names are concatenated (separated with a slash ('/')).
	 * <p>
	 * Subclasses may re-implement to provide a different name for this node.
	 */
	/* (non Javadoc)
	 * see ITypedElement.getName
	 */
	public String getName() {
		
		String s1= null;
		if (fRight != null)
			s1= fRight.getName();

		String s2= null;
		if (fLeft != null)
			s2= fLeft.getName();

		if (s1 == null && s2 == null) {
			if (fAncestor != null)
				return fAncestor.getName();
			return Utilities.getString("DiffNode.noName"); //$NON-NLS-1$
		}

		if (s1 == null)
			return s2;
		if (s2 == null)
			return s1;

		if (s1.equals(s2))
			return s1;
			
		String fmt= Utilities.getString("DiffNode.nameFormat"); //$NON-NLS-1$
		return MessageFormat.format(fmt, new String[] { s1, s2 });
	}

	/* (non Javadoc)
	 * see ITypedElement.getImage
	 */
	public Image getImage() {
		ITypedElement id= getId();
		if (id != null)
			return id.getImage();
		return null;
	}

	/* (non Javadoc)
	 * see ITypedElement.getType
	 */
	public String getType() {
		ITypedElement id= getId();
		if (id != null)
			return id.getType();
		return ITypedElement.UNKNOWN_TYPE;
	}

	/* (non Javadoc)
	 * see ICompareInput.getAncestor
	 */
	public ITypedElement getAncestor() {
		return fAncestor;
	}
	
	/**
	 * Sets the left input to the given value.
	 *
	 * @param left the new value for the left input
	 */
	public void setLeft(ITypedElement left) {
		fLeft= left;
	}
	
	/* (non Javadoc)
	 * see ICompareInput.getLeft
	 */
	public ITypedElement getLeft() {
		return fLeft;
	}

	/**
	 * Sets the right input to the given value.
	 *
	 * @param right the new value for the right input
	 */
	public void setRight(ITypedElement right) {
		fRight= right;
	}
	
	/* (non Javadoc)
	 * see ICompareInput.getRight
	 */
	public ITypedElement getRight() {
		return fRight;
	}

	/* (non Javadoc)
	 * see ICompareInput.copy
	 */
	public void copy(boolean leftToRight) {
		//System.out.println("DiffNode.copy: " + leftToRight);
		
		IDiffContainer pa= getParent();
		if (pa instanceof ICompareInput) {
			ICompareInput parent= (ICompareInput) pa;
			Object dstParent= leftToRight ? parent.getRight() : parent.getLeft();
			
			if (dstParent instanceof IEditableContent) {
				ITypedElement dst= leftToRight ? getRight() : getLeft();
				ITypedElement src= leftToRight ? getLeft() : getRight();
				dst= ((IEditableContent)dstParent).replace(dst, src);
				if (leftToRight)
					setRight(dst);
				else
					setLeft(dst);
				setKind(Differencer.NO_CHANGE);
				
				fireChange();
			}
		}
	}
	
	//---- object

	public int hashCode() {
		Object id= getId();
		if (id != null)
			return id.hashCode();
		return super.hashCode();
	}

	public boolean equals(Object other) {
		if (other != null && getClass() == other.getClass()) {
			DiffNode d= (DiffNode) other;
			Object id1= getId();
			Object id2= d.getId();
			if (id1 != null && id2 != null)
				return id1.equals(id2);
		}
		return super.equals(other);
	}
}
