package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
 
import org.eclipse.debug.core.model.IValue;
import org.eclipse.core.runtime.PlatformObject;


public class InspectItem extends PlatformObject {
	
	protected String fLabel;
	protected IValue fValue;
	
	
	public InspectItem(String label, IValue value) {
		fLabel = label;
		fValue = value;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	public IValue getValue() {
		return fValue;
	}
	
	/**
	 * @see IAdaptable
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == InspectItem.class) {
			return this;
		}			
		return super.getAdapter(adapter);
	}
}