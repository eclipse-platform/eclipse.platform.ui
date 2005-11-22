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

public class PriceCentsConverter implements IConverter {

	private double dollars;

	public Class getTargetType() {
		return Integer.class;
	}

	public Class getModelType() {
		return Double.TYPE;
	}

	public Object convertTargetToModel(Object object) {
		// Argument is an Integer representing the cents portion.
		// Add to dollars to make the new price
		double newPrice = dollars + ((Integer) object).doubleValue() / 100;
		return new Double(newPrice);
	}

	public Object convertModelToTarget(Object object) {
		// Return the cents portion only and remember the dollars
		Double price = (Double) object;
		dollars = price.intValue();
		double cents = price.doubleValue() - price.intValue();
		cents = cents * 100;
		return new Integer((int) cents);
	}
}
