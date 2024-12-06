/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.examples.databinding.ModelObject;

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

	public int getMaxNumberOfPeople() {
		return maxNumberOfPeople;
	}

	public void setMaxNumberOfPeople(int anInt) {
		int oldValue = maxNumberOfPeople;
		maxNumberOfPeople = anInt;
		firePropertyChange("maxNumberOfPeople", oldValue, maxNumberOfPeople);
	}

	public IValidator<Integer> getMaxNumberOfPeopleDomainValidator() {
		return value -> {
			if (value < 1 || value > 20) {
				return ValidationStatus.error("Max number of people must be between 1 and 20 inclusive");
			}
			return null;
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
		firePropertyChange("price", Double.valueOf(oldValue), Double.valueOf(price));
	}

	public double getPrice() {
		return price;
	}

	public void setPetsAllowed(boolean b) {
		boolean oldValue = petsAllowed;
		petsAllowed = b;
		firePropertyChange("petsAllowed", Boolean.valueOf(oldValue), Boolean.valueOf(petsAllowed));
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
