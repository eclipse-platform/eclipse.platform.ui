package org.eclipse.jface.tests.databinding.scenarios.model;


public class Catalog extends ModelObject {

	private Category[] categories = new Category[0];

	private Lodging[] lodgings = new Lodging[0];

	private Transportation[] transportations = new Transportation[0];

	private Account[] accounts = new Account[0];

	public Category[] getCategories() {
		return categories;
	}

	public void addCategory(Category category) {
		categories = (Category[]) append(categories, category);
		firePropertyChange("categories", null, null);
	}

	public void addLodging(Lodging lodging) {
		lodgings = (Lodging[]) append(lodgings, lodging);
		firePropertyChange("lodgings", null, null);
	}

	public void addTransportation(Transportation transportation) {
		transportations = (Transportation[]) append(transportations, transportation);
		firePropertyChange("transportations", null, null);
	}

	public void addAccount(Account account) {
		accounts = (Account[]) append(accounts, account);
		firePropertyChange("accounts", null, null);
	}

	public Lodging[] getLodgings() {
		return lodgings;
	}

	public void removeLodging(Lodging lodging) {
		lodgings = (Lodging[]) remove(lodgings, lodging);
		firePropertyChange("lodgings", null, null);
	}
	
	public void removeAccount(Account anAccount) {
		accounts = (Account[]) remove(accounts, anAccount);
		firePropertyChange("accounts", null, null);
	}	

	public Account[] getAccounts() {
		return accounts;
	}

}
