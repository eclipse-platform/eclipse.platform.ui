package org.eclipse.ui.tests.api;

import org.eclipse.ui.IActionFilter;

public class ListElementActionFilter implements IActionFilter {

	private static ListElementActionFilter singleton;
	private boolean called = false;

	public static ListElementActionFilter getSingleton() {
		if (singleton == null)
			singleton = new ListElementActionFilter();
		return singleton;
	}
		
	private ListElementActionFilter() {
		super();
	}

	/**
	 * @see IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		called = true;
		if (name.equals("name")) {
			ListElement le = (ListElement)target;
			return value.equals(le.getName());
		}	
		return false;
	}
	
	public void clearCalled() {
		called = false;
	}
	
	public boolean getCalled() {
		return called;
	}

}

