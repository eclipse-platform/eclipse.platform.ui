/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		firePropertyChange("arrivaltime",oldValue,string);
	}

	public String getArrivalTime(){
		return arrivalTime;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double aPrice) {
		double oldPrice = price;
		price = aPrice;
		firePropertyChange("price",new Double(oldPrice),new Double(price));
	}

}
