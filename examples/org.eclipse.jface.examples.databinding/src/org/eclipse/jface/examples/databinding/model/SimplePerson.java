/*
 * Copyright (C) 2005, 2015 David Orme <djo@coconut-palm-software.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme     - Initial API and implementation
 *     Brad Reynolds (bug 139407)
 */
package org.eclipse.jface.examples.databinding.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.examples.databinding.ModelObject;

public class SimplePerson extends ModelObject {

	private String name = "";
	private String address = "";
	private String city = "";
	private String state = "";
	private SimpleCart cart = new SimpleCart();

	private List<SimpleOrder> orders = new LinkedList<>();

	public SimplePerson(String name, String address, String city, String state) {
		this.name = name;
		this.address = address;
		this.city = city;
		this.state = state;

		int numOrders = (int) (Math.random() * 5);
		for (int i=0; i < numOrders; ++i) {
			orders.add(new SimpleOrder(i, new Date()));
		}
	}

	public SimplePerson() {}

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
		String old = this.address;
		this.address = address;
		firePropertyChange("address", old, address);
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
		String old = this.city;
		firePropertyChange("city", old, this.city = city);
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
		firePropertyChange("name", this.name, this.name = name);
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
		firePropertyChange("state", this.state, this.state = state); //$NON-NLS-1$
	}

	/**
	 * @return Returns the orders.
	 */
	public List<SimpleOrder> getOrders() {
		return orders;
	}

	public SimpleCart getCart() {
		return cart;
	}

	public void setCart(SimpleCart cart) {
		firePropertyChange("cart", this.cart, this.cart = cart); //$NON-NLS-1$
	}
}
