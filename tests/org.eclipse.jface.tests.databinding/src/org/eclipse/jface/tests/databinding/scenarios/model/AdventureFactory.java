package org.eclipse.jface.tests.databinding.scenarios.model;

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
