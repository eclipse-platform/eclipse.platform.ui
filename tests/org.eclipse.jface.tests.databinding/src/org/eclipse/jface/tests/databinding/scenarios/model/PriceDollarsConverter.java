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
package org.eclipse.jface.tests.databinding.scenarios.model;

import org.eclipse.jface.databinding.converter.IConverter;

public class PriceDollarsConverter implements IConverter {

	private double cents;

	public Class getTargetType() {
		return Integer.class;
	}

	public Class getModelType() {
		return Double.TYPE;
	}

	public Object convertTargetToModel(Object object) {
		// Argument is an Integer representing the dollar portion. Add to cents
		// to make the new price
		double newPrice = cents + ((Integer) object).intValue();
		return new Double(newPrice);
	}

	public Object convertModelToTarget(Object object) {
		// Argument is a Double representing the price. Return dollars only and
		// remember the cents
		Double price = (Double) object;
		int dollars = price.intValue();
		cents = price.doubleValue() - dollars;
		return new Integer(dollars);
	}
}
