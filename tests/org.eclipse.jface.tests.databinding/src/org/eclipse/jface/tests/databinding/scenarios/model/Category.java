package org.eclipse.jface.tests.databinding.scenarios.model;

public class Category extends ModelObject {

	private String name;

	private Adventure[] adventures = new Adventure[0];

	public void setName(String string) {
		Object oldValue = name;
		name = string;
		firePropertyChange("name", oldValue, name);
	}

	public void addAdventure(Adventure adventure) {
		adventures = (Adventure[]) append(adventures, adventure);
		firePropertyChange("adventures", null, null);
	}

	public Adventure[] getAdventures() {
		return adventures;
	}

	public String getName() {
		return name;
	}

}
