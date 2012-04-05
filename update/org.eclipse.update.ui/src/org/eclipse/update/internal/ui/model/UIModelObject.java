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

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.*;
import org.eclipse.ui.model.*;


public class UIModelObject extends PlatformObject implements IWorkbenchAdapter {
	UpdateModel model;
	
	public void setModel(UpdateModel model) {
		this.model = model;
	}
	
	public UpdateModel getModel() {
		return model;
	}
	
	protected void notifyObjectChanged(String property) {
		if (model==null) return;
		model.fireObjectChanged(this, property);
	}
	
	protected void notifyObjectsAdded(Object parent, Object [] objects) {
		if (model==null) return;
		model.fireObjectsAdded(parent, objects);
	}
	
	protected void notifyObjectsRemoved(Object parent, Object [] objects) {
		if (model==null) return;
		model.fireObjectsRemoved(parent, objects);
	}
	
	public Object [] getChildren(Object obj) {
		return new Object[0];
	}
	
	public Object getParent(Object obj) {
		return null;
	}
	public String getLabel(Object obj) {
		return toString();
	}
	public ImageDescriptor getImageDescriptor(Object obj) {
		return null;
	}
}
