package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import org.eclipse.core.runtime.PlatformObject;
import java.util.List;
import java.util.Vector;

public class InspectorList extends PlatformObject {

	protected Vector fInspectorList;

	/**
	 * @see IAdaptable
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == InspectItem.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}
	
	public InspectorList(int initialCapacity) {
		fInspectorList= new Vector(initialCapacity);
	}
	
	public List getList() {
		return fInspectorList;
	}
	
	public boolean isEmpty() {
		return fInspectorList.isEmpty();
	}
}
