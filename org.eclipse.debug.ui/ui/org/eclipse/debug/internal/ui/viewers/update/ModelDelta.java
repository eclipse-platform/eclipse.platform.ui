/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.viewers.IModelDelta;

/**
 * A model delta. Used to create model deltas.
 * <p>
 * Clients may instantiate this class; not intended to be subclassed.
 * </p>
 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta
 * @since 3.2
 */
public class ModelDelta implements IModelDelta {

	private IModelDelta fParent;
	private Object fElement;
	private int fFlags;
	private List fNodes = new ArrayList();
	private Object fReplacement;
	private int fIndex;

	/**
	 * Constructs a new delta for the given element.
	 * 
	 * @param element model element
	 * @param flags change flags
	 */
	public ModelDelta(Object element, int flags) {
		fElement = element;
		fFlags = flags;
	}

	/**
	 * Constructs a new delta for the given element to be replaced
	 * with the specified replacement element.
	 * 
	 * @param element model element
	 * @param replacement replacement element
	 * @param flags change flags
	 */
	public ModelDelta(Object element, Object replacement, int flags) {
        fElement = element;
        fReplacement = replacement;
        fFlags = flags;
    }

	/**
	 * Constructs a new delta for the given element to be inserted at
	 * the specified index.
	 * 
	 * @param element model element
	 * @param index insertion position
	 * @param flags change flags
	 */
    public ModelDelta(Object element, int index, int flags) {
        fElement = element;
        fIndex = index;
        fFlags = flags;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getElement()
     */
    public Object getElement() {
		return fElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getFlags()
	 */
	public int getFlags() {
		return fFlags;
	}

	/**
	 * Adds a child node to this delta with the given element and change flags,
	 * and returns the child delta.
	 * 
	 * @param element child element to add
	 * @param flags change flags for child
	 * @return newly created child delta
	 */
	public ModelDelta addNode(Object element, int flags) {
		ModelDelta node = new ModelDelta(element, flags);
		node.setParent(this);
		fNodes.add(node);
		return node;
	}

	/**
	 * Adds a child node to this delta to replace the given element with the
	 * specified replacement element and change flags, and returns the 
	 * newly created child delta.
	 * 
	 * @param element child element to add to this delta
	 * @param replacement replacement element for the child element
	 * @param flags change flags
	 * @return newly created child delta
	 */
    public ModelDelta addNode(Object element, Object replacement, int flags) {
        ModelDelta node = new ModelDelta(element, replacement, flags);
        node.setParent(this);
        fNodes.add(node);
        return node;
    }

    /**
     * Adds a child delta to this delta to insert the specified element at
     * the given index, and returns the newly created child delta.
     * 
     * @param element child element in insert
     * @param index index of insertion
     * @param flags change flags
     * @return newly created child delta
     */
    public ModelDelta addNode(Object element, int index, int flags) {
        ModelDelta node = new ModelDelta(element, index, flags);
        node.setParent(this);
        fNodes.add(node);
        return node;
    }
    
    /**
     * Sets the parent delta of this delta
     * 
     * @param node parent delta
     */
	void setParent(ModelDelta node) {
		fParent = node;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getParent()
	 */
	public IModelDelta getParent() {
		return fParent;
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getReplacementElement()
     */
    public Object getReplacementElement() {
        return fReplacement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getIndex()
     */
    public int getIndex() {
        return fIndex;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getNodes()
	 */
	public ModelDelta[] getNodes() {
		return (ModelDelta[]) fNodes.toArray(new ModelDelta[0]);
	}
}
