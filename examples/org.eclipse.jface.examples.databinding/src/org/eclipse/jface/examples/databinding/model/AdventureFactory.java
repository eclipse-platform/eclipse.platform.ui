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

public class AdventureFactory {

	public Catalog createCatalog() {
		return new Catalog();
	}

	public Category createCategory() {
		return new Category();
	}

	public Adventure createAdventure() {
		return new Adventure();
	}

	public Lodging createLodging() {
		return new Lodging();
	}

	public Transportation createTransportation() {
		return new Transportation();
	}

	public Account createAccount() {
		return new Account();
	}

	public Cart createCart() {
		return new Cart();
	}

}
