package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IActionFilter;

public class ListElement implements IAdaptable {

	private String name;
	
	/**
	 * Constructor for Element
	 */
	public ListElement(String name) {
		super();
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}
		
	public Object getAdapter(Class adapter) {
		if (adapter == IActionFilter.class) {
			return ListElementActionFilter.getSingleton();
		}
		return null;
	}

}

