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


public class PriceModelObject extends ModelObject {
	
	private double price;
	
	public double getDouble(){
		return price;
	}
	public void setPrice(double aPrice){
		double oldValue = price;
		price = aPrice;
		firePropertyChange("price",new Double(oldValue), new Double(price));
	}
	
	public double getPrice(){
		return price;
	}
	
	public int getCents(){
		return (int) ((price - new Double(price).intValue()) * 100);
	}
	
	public void setCents(int cents){
		int oldCents = getCents();
		price = getDollars() + cents*.01;
		firePropertyChange("cents",oldCents,getCents());
	}
	
	public int getDollars(){
		return new Double(price).intValue();
	}
	
	public void setDollars(int dollars){
		int oldDollars = getDollars();
		price = dollars + getCents()*.01;
		firePropertyChange("dollars",oldDollars,getDollars());
	}

}
