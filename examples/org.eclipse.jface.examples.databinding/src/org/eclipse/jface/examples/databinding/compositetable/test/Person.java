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
package org.eclipse.jface.examples.databinding.compositetable.test;

public class Person {
	
	public String name = "";
	public String address = "";
	public String city = "Wheaton";
	public String state = "IL";
	
	public Person(String name, String address, String city, String state) {
		this.name = name;
		this.address = address;
		this.city = city;
		this.state = state;
	}
	
	public Person() {}
}
