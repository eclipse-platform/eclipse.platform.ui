package org.eclipse.update.ui.internal.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.jface.resource.ImageDescriptor;

public class ModelObject extends PlatformObject implements IWorkbenchAdapter {
	UpdateModel model;
	
	void setModel(UpdateModel model) {
		this.model = model;
	}
	
	public UpdateModel getModel() {
		return model;
	}
	
	protected void notifyObjectChanged(String property) {
		if (model==null) return;
		model.fireObjectChanged(this, property);
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