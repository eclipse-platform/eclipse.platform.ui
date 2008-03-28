/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sascha Zelzer <zelzer@mathi.uni-heidelberg.de> -
 *     	Fix for Bug 152927 [Decorators] ArrayOutOfBoundsException in DecorationBuilder.java
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The Decoration builder is the object that builds a decoration.
 */
public class DecorationBuilder implements IDecoration {

	private static int DECORATOR_ARRAY_SIZE = 6;

	private List prefixes = new ArrayList();

	private List suffixes = new ArrayList();

	private ImageDescriptor[] descriptors = new ImageDescriptor[DECORATOR_ARRAY_SIZE];

	private Color foregroundColor;

	private Color backgroundColor;

	private Font font;

	LightweightDecoratorDefinition currentDefinition;

	// A flag set if a value has been added
	private boolean valueSet = false;

	private final IDecorationContext context;

	/**
	 * Default constructor.
	 */
	DecorationBuilder() {
		this(DecorationContext.DEFAULT_CONTEXT);
	}

	/**
	 * Create a decoration builder for the given context
	 * 
	 * @param context
	 *            a decoration context
	 */
	public DecorationBuilder(IDecorationContext context) {
		this.context = context;
	}

	/**
	 * Set the value of the definition we are currently working on.
	 * 
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
		if (descriptors[quadrant] == null) {
			descriptors[quadrant] = overlay;
		}
		valueSet = true;
	}

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addOverlay(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void addOverlay(ImageDescriptor overlay, int quadrant) {
		if (quadrant >= 0 && quadrant < DECORATOR_ARRAY_SIZE) {
			if (descriptors[quadrant] == null) {
				descriptors[quadrant] = overlay;
			}
			valueSet = true;
		} else {
			WorkbenchPlugin
					.log("Unable to apply decoration for " + currentDefinition.getId() + " invalid quadrant: " + quadrant); //$NON-NLS-1$ //$NON-NLS-2$
		}
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
	 * 
	 * @return DecorationResult
	 */
	DecorationResult createResult() {
		// check whether the context says that replacement should happen
		boolean clearReplacementImage = true;
		if (context != null) {
			Object propertyValue = context.getProperty(IDecoration.ENABLE_REPLACE);
			if (propertyValue instanceof Boolean) {
				if (((Boolean) propertyValue).booleanValue()) {
					clearReplacementImage = false;
				}
			}
		}
		if (clearReplacementImage) {
			descriptors[IDecoration.REPLACE] = null;
		}
		DecorationResult newResult = new DecorationResult(new ArrayList(
				prefixes), new ArrayList(suffixes), descriptors,
				foregroundColor, backgroundColor, font);

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
	 * 
	 * @return boolean
	 */
	boolean hasValue() {
		return valueSet;
	}

	/**
	 * Apply the previously calculates result to the receiver.
	 * 
	 * @param result
	 */
	void applyResult(DecorationResult result) {
		prefixes.addAll(result.getPrefixes());
		suffixes.addAll(result.getSuffixes());
		ImageDescriptor[] resultDescriptors = result.getDescriptors();
		if (resultDescriptors != null) {
			for (int i = 0; i < descriptors.length; i++) {
				if (resultDescriptors[i] != null) {
					descriptors[i] = resultDescriptors[i];
				}
			}
		}

		setForegroundColor(result.getForegroundColor());
		setBackgroundColor(result.getBackgroundColor());
		setFont(result.getFont());
		valueSet = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IDecoration#setBackgroundColor(org.eclipse.swt.graphics.Color)
	 */

	public void setBackgroundColor(Color bgColor) {
		this.backgroundColor = bgColor;
		valueSet = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IDecoration#setFont(org.eclipse.swt.graphics.Font)
	 */
	public void setFont(Font newFont) {
		this.font = newFont;
		valueSet = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IDecoration#setForegroundColor(org.eclipse.swt.graphics.Color)
	 */
	public void setForegroundColor(Color fgColor) {
		this.foregroundColor = fgColor;
		valueSet = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IDecoration#getDecorationContext()
	 */
	public IDecorationContext getDecorationContext() {
		return context;
	}
}
