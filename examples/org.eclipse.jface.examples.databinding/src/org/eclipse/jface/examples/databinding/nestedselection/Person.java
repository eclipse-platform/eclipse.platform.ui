/*
 * Copyright (C) 2005 David Orme <djo@coconut-palm-software.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme     - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.nestedselection;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Person {
	
	private String name = "";
	private String address = "";
	private String city = "";
	private String state = "";
	
	private List orders = new LinkedList();
	
	public Person(String name, String address, String city, String state) {
		this.name = name;
		this.address = address;
		this.city = city;
		this.state = state;
		
		int numOrders = (int) (Math.random() * 5);
		for (int i=0; i < numOrders; ++i) {
			orders.add(new Order(i, new Date()));
		}
	}
	
	public Person() {}

	/**
	 * @return Returns the address.
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address The address to set.
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return Returns the city.
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city The city to set.
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the state.
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state The state to set.
	 */
	public void setState(String state) {
		this.state = state;
	}
	
	/**
	 * @return Returns the orders.
	 */
	public List getOrders() {
		return orders;
	}
}
