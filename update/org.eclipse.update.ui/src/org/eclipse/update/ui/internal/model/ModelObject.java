package org.eclipse.update.ui.internal.model;

import org.eclipse.core.runtime.*;

public class ModelObject extends PlatformObject {
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
}