/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.model;

/**
 * A simple model object that is mutable.
 * 
 * @since 3.3
 */
public class Person {

	private int id;
	private String surname;
	private String givenname;
	private boolean admin = false;


	Person(int id, String sn, String gn) {
		surname = sn;
		givenname = gn;
		this.id = id;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public int getId() {
		return id;
	}

	public String getGivenname() {
		return givenname;
	}

	public void setGivenname(String givenname) {
		this.givenname = givenname;
	}
	
	public boolean hasAdminRights() {
		return admin;
	}
	
	public void setAdminRights(boolean admin) {
		this.admin = admin;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer(surname);
		buf.append(", "); //$NON-NLS-1$
		buf.append(givenname);
		buf.append(" ("); //$NON-NLS-1$
		buf.append(id);
		if (admin) {
			buf.append("-adm"); //$NON-NLS-1$
		}
		buf.append(")"); //$NON-NLS-1$
		return buf.toString();
	}

	protected Person copy() {
		return new Person(id, surname, givenname);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Person) {
			Person p = (Person) o;
			return p.givenname == givenname && p.id == id
					&& p.surname == surname;
		}
		return false;
	}
}
