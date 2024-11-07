/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	private List<String> prefixes = new ArrayList<>();

	private List<String> suffixes = new ArrayList<>();

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
	 * @param context a decoration context
	 */
	public DecorationBuilder(IDecorationContext context) {
		this.context = context;
	}

	/**
	 * Set the value of the definition we are currently working on.
	 */
	void setCurrentDefinition(LightweightDecoratorDefinition definition) {
		this.currentDefinition = definition;
	}

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addOverlay(org.eclipse.jface.resource.ImageDescriptor)
	 */
	@Override
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
	@Override
	public void addOverlay(ImageDescriptor overlay, int quadrant) {
		if (quadrant >= 0 && quadrant < DECORATOR_ARRAY_SIZE) {
			if (descriptors[quadrant] == null) {
				descriptors[quadrant] = overlay;
			}
			valueSet = true;
		} else {
			WorkbenchPlugin.log(
					"Unable to apply decoration for " + currentDefinition.getId() + " invalid quadrant: " + quadrant); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addPrefix(java.lang.String)
	 */
	@Override
	public void addPrefix(String prefixString) {
		prefixes.add(prefixString);
		valueSet = true;
	}

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addSuffix(java.lang.String)
	 */
	@Override
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
		return new DecorationResult(new ArrayList<>(prefixes), new ArrayList<>(suffixes), descriptors,
				foregroundColor, backgroundColor, font);
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

	@Override
	public void setBackgroundColor(Color bgColor) {
		this.backgroundColor = bgColor;
		valueSet = true;
	}

	@Override
	public void setFont(Font newFont) {
		this.font = newFont;
		valueSet = true;
	}

	@Override
	public void setForegroundColor(Color fgColor) {
		this.foregroundColor = fgColor;
		valueSet = true;
	}

	@Override
	public IDecorationContext getDecorationContext() {
		return context;
	}
}
