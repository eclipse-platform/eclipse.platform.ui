package org.eclipse.ui.examples.adapterservice.snippets.adapter;

import org.eclipse.core.runtime.IAdaptable;

class MultiFacetedObject implements IAdaptable, Greeter {

	String name;

	MultiFacetedObject(String name) {
		this.name = name;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Greeter.class)) {
			return this;
		}
		return null;
	}

	@Override
	public void greet() {
		System.out.println("Hello, my name is " + name);
	}
	
	public String identify() {
		return "I am the MultiFacetedObject named " + name;
	}

}