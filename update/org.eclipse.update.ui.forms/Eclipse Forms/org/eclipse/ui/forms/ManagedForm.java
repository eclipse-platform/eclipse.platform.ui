/*
 * Created on Dec 4, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms;

import java.util.Vector;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ManagedForm {
	private Form form;
	private FormToolkit toolkit;
	private Vector parts = new Vector();

	public ManagedForm(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
	}

	public ManagedForm(FormToolkit toolkit, Form form) {
		this.form = form;
		this.toolkit = toolkit;
	}
	
	public void addPart(IFormPart part) {
		parts.add(part);
	}

	public void removePart(IFormPart part) {
		parts.remove(part);
	}
	
	public FormToolkit getToolkit() {
		return toolkit;
	}

	public Form getForm() {
		return form;
	}
	
	public void initialize() {
		for (int i=0; i<parts.size(); i++) {
			IFormPart part = (IFormPart)parts.get(i);
			part.initialize(this);
		}
	}
	public void dispose() {
		for (int i=0; i<parts.size(); i++) {
			IFormPart part = (IFormPart)parts.get(i);
			part.dispose();
		}
	}
}