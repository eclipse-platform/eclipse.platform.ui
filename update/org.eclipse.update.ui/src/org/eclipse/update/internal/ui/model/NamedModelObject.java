package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.model.*;
import java.util.*;
import org.eclipse.update.internal.ui.*;

public class NamedModelObject extends ModelObject 
							implements IWorkbenchAdapter {
	private String name;
	private NamedModelObject parent;
	
	public static final String P_NAME="p_name";
	
	public NamedModelObject() {
	}
	
	public NamedModelObject(String name) {
		this.name = name;
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return getName();
	}
	
	public void setName(String name) {
		this.name = name;
		notifyObjectChanged(P_NAME);
	}
	
	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		return null;
	}


	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object obj) {
		return null;
	}


	/**
	 * @see IWorkbenchAdapter#getLabel(Object)
	 */
	public String getLabel(Object obj) {
		return getName();
	}


	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object arg0) {
		return parent;
	}
	public void setParent(NamedModelObject parent) {
		this.parent = parent;
	}
}