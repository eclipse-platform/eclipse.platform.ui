/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.model;

/**
 * A shape enumeration for the dynamic tests view. (Should use an enum when we
 * can use Java 5).
 *
 * @author Anthony Hunter
 */
public class DynamicTestsShape {

	public static final DynamicTestsShape CIRCLE = new DynamicTestsShape(
			"circle"); //$NON-NLS-1$

	public static final DynamicTestsShape SQUARE = new DynamicTestsShape(
			"square"); //$NON-NLS-1$

	public static final DynamicTestsShape STAR = new DynamicTestsShape("star"); //$NON-NLS-1$

	public static final DynamicTestsShape TRIANGLE = new DynamicTestsShape(
			"triangle"); //$NON-NLS-1$

	/**
	 * @return the shape
	 */
	public static DynamicTestsShape getShape(String value) {
		if (SQUARE.getShape().equals(value)) {
			return SQUARE;
		} else if (CIRCLE.getShape().equals(value)) {
			return CIRCLE;
		} else if (TRIANGLE.getShape().equals(value)) {
			return TRIANGLE;
		} else if (STAR.getShape().equals(value)) {
			return STAR;
		}
		return null;
	}

	private String shape;

	private DynamicTestsShape(String aShape) {
		setShape(aShape);
	}

	/**
	 * @return the shape
	 */
	public String getShape() {
		return shape;
	}

	/**
	 * @param shape
	 *            the shape to set
	 */
	public void setShape(String aShape) {
		this.shape = aShape;
	}

	@Override
	public String toString() {
		return getShape();
	}

}
