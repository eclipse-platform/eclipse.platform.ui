/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;

/** 
 * The Decoration builder is the object that builds
 * a decoration.
 */
class DecorationBuilder implements IDecoration {

	private static int DECORATOR_ARRAY_SIZE = 5;

	private List prefixes = new ArrayList();
	private List suffixes = new ArrayList();
	private ImageDescriptor[] descriptors =
		new ImageDescriptor[DECORATOR_ARRAY_SIZE];
	LightweightDecoratorDefinition currentDefinition;

	//A flag set if a value has been added
	private boolean valueSet = false;

	DecorationBuilder() {
	}


	/**
	 * Set the value of the definition we are currently 
	 * working on.
	 * @param definition
	 */
	void setCurrentDefinition(LightweightDecoratorDefinition definition) {
		this.currentDefinition = definition;
	}

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addOverlay(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void addOverlay(ImageDescriptor overlay) {
		int quadrant = currentDefinition.getQuadrant();
		if (descriptors[quadrant] == null)
			descriptors[quadrant] = overlay;
		valueSet = true;
	}

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addPrefix(java.lang.String)
	 */
	public void addPrefix(String prefixString) {
		prefixes.add(prefixString);
		valueSet = true;
	}

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addSuffix(java.lang.String)
	 */
	public void addSuffix(String suffixString) {
		suffixes.add(suffixString);
		valueSet = true;
	}

	/**
	 * Clear the current values and return a DecorationResult.
	 */
	DecorationResult createResult() {
		DecorationResult newResult =
			new DecorationResult(
				new ArrayList(prefixes),
				new ArrayList(suffixes),
				descriptors);

		return newResult;
	}


	/**
	 * Clear the contents of the result so it can be reused.
	 */
	void clearContents() {
		this.prefixes.clear();
		this.suffixes.clear();
		this.descriptors = new ImageDescriptor[DECORATOR_ARRAY_SIZE];
		valueSet = false;
	}

	/**
	 * Return whether or not a value has been set.
	 * @return boolean
	 */
	boolean hasValue() {
		return valueSet;
	}

}
