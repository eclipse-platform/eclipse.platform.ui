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

import java.util.LinkedList;

public class Model {
	public Model() {
		// Add some sample data to our personList...
		personList.add(new Person("John", "1234", "Wheaton", "IL"));
		personList.add(new Person("Jane", "1234", "Glen Ellyn", "IL"));
		personList.add(new Person("Frank", "1234", "Lombard", "IL"));
		personList.add(new Person("Joe", "1234", "Elmhurst", "IL"));
		personList.add(new Person("Chet", "1234", "Oak Lawn", "IL"));
		personList.add(new Person("Wilbur", "1234", "Austin", "IL"));
		personList.add(new Person("Elmo", "1234", "Chicago", "IL"));
	}

	// Now a PersonList property...
	
	LinkedList personList = new LinkedList();
	
	public LinkedList getPersonList() {
		return personList;
	}

}