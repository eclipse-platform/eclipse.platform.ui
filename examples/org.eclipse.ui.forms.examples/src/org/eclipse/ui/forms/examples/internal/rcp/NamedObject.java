/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.examples.internal.rcp;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public	class NamedObject {
	private String name;
	protected SimpleModel model;
	
	public NamedObject(String name) {
		this.name = name;
	}
	public void setModel(SimpleModel model) {
		this.model = model;
	}
	public String getName() {
		return name;
	}
	public String toString() {
		return getName();
	}
	public void setName(String name) {
		this.name = name;
		model.fireModelChanged(new Object [] {this}, IModelListener.CHANGED, "");
	}
}