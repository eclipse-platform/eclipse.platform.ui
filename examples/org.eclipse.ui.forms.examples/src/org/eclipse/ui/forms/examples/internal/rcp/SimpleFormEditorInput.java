/*
 * Created on Jan 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.examples.internal.rcp;
import org.eclipse.ui.forms.examples.internal.FormEditorInput;

public class SimpleFormEditorInput extends FormEditorInput {
	private SimpleModel model;
	
	public SimpleFormEditorInput(String name) {
		super(name);
		model = new SimpleModel();
	}
	
	public SimpleModel getModel() {
		return model;
	}
}