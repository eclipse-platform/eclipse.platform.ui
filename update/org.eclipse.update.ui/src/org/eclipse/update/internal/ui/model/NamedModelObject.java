/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.model;

import java.io.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.*;
import org.eclipse.ui.model.*;

public class NamedModelObject extends UIModelObject 
							implements IWorkbenchAdapter, Serializable  {

    private static final long serialVersionUID = 1L;
    private String name;
	transient private NamedModelObject parent;
	
	public static final String P_NAME="p_name"; //$NON-NLS-1$
	
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
	
	public IPath getPath() {
		Object parent = getParent(null);
		if (parent!=null && parent instanceof NamedModelObject)
			return ((NamedModelObject)parent).getPath().append(getName());
		else
			return new Path(getName());
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
