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

import org.eclipse.jface.databinding.validator.IValidator;
import org.eclipse.jface.databinding.validators.String2IntValidator;

public class Adventure extends ModelObject {

	private boolean petsAllowed;

	private double price;

	private Lodging defaultLodging;

	private String name;

	private String description;

	private String location;
	
	private int maxNumberOfPeople;

	public String getName() {
		return name;
	}

	public void setName(String string) {
		Object oldValue = name;
		name = string;
		firePropertyChange("name", oldValue, name);
	}
	
	public int getMaxNumberOfPeople(){
		return maxNumberOfPeople;
	}
	
	public void setMaxNumberOfPeople(int anInt) {
		int oldValue = maxNumberOfPeople;
		maxNumberOfPeople = anInt;
		firePropertyChange("maxNumberOfPeople", oldValue, maxNumberOfPeople);
	}
	
	public IValidator getMaxNumberOfPeopleValidator() {
		return new IValidator() {
			IValidator delegate = new String2IntValidator();
			public String isPartiallyValid(Object value) {
				return delegate.isPartiallyValid(value);
			}

			public String isValid(Object value) {
				String error = delegate.isValid(value);
				if (error != null) {
					return error;
				}
				int intValue = Integer.valueOf((String)value).intValue();
				if (intValue < 1 || intValue > 20) {
					return "Max number of people must be between 1 and 20 inclusive";
				}
				return null;
			}
			
		};
	}

	public Lodging getDefaultLodging() {
		return defaultLodging;
	}

	public void setDefaultLodging(Lodging lodging) {
		Object oldValue = defaultLodging;
		defaultLodging = lodging;
		firePropertyChange("defaultLodging", oldValue, defaultLodging);
	}

	public void setPrice(double d) {
		double oldValue = price;
		price = d;
		firePropertyChange("price", new Double(oldValue), new Double(price));
	}

	public double getPrice() {
		return price;
	}

	public void setPetsAllowed(boolean b) {
		boolean oldValue = petsAllowed;
		petsAllowed = b;
		firePropertyChange("petsAllowed", new Boolean(oldValue), new Boolean(
				petsAllowed));
	}

	public boolean isPetsAllowed() {
		return petsAllowed;
	}

	public void setDescription(String string) {
		Object oldValue = description;
		description = string;
		firePropertyChange("description", oldValue, description);
	}

	public void setLocation(String string) {
		Object oldValue = location;
		location = string;
		firePropertyChange("location", oldValue, location);
	}

	public String getDescription() {
		return description;
	}

}
