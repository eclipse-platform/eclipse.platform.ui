package org.eclipse.jface.tests.binding.scenarios.model;

public class Category extends ModelObject {

	private String name;
	public void setName(String string) {
		Object oldValue = name;
		name = string;
		firePropertyChange("name", oldValue, name);
	}
	public void addAdventure(Adventure beach_holiday) {
		// TODO Auto-generated method stub
		
	}

}
