package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ui.IActionFilter;

public class ListElement implements IAdaptable {

	private String name;
	private boolean flag;
	
	public ListElement(String name) {
		this(name, false);
	}
	
	public ListElement(String name, boolean flag) {
		this.name = name;
		this.flag = flag;
	}
	
	public String toString() {
		return name + ':' + flag;
	}

	public String getName() {
		return name;
	}
	
	public boolean getFlag() {
		return flag;
	}
		
	public Object getAdapter(Class adapter) {
		if (adapter == IActionFilter.class) {
			return ListElementActionFilter.getSingleton();
		}
		return null;
	}

}

