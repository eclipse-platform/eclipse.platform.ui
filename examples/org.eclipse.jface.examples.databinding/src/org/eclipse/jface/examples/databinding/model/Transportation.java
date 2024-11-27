/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.model;

import org.eclipse.jface.examples.databinding.ModelObject;

public class Transportation extends ModelObject {

	private String arrivalTime;
	private double price;

	public void setArrivalTime(String string) {
		String oldValue = arrivalTime;
		arrivalTime = string;
		firePropertyChange("arrivaltime", oldValue, string);
	}

	public String getArrivalTime() {
		return arrivalTime;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double aPrice) {
		double oldPrice = price;
		price = aPrice;
		firePropertyChange("price", Double.valueOf(oldPrice), Double.valueOf(price));
	}

}
