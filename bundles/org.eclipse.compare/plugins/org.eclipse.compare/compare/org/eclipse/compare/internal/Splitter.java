/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.SashForm;

/**
 * @deprecated Use org.eclipse.compare.Splitter instead
 */
public class Splitter extends SashForm {
	
	private static final String VISIBILITY= "org.eclipse.compare.internal.visibility"; //$NON-NLS-1$
	

	public Splitter(Composite parent, int style) {
		super(parent, style);
	}
		
	public void setVisible(Control child, boolean visible) {
		
		boolean wasEmpty= isEmpty();
				
		child.setVisible(visible);
		child.setData(VISIBILITY, new Boolean(visible));
		
		if (wasEmpty != isEmpty()) {
			Composite parent= getParent();
			if (parent instanceof Splitter) {
				Splitter sp= (Splitter) parent;
				sp.setVisible(this, visible);
				sp.layout();
			}	
			else if (parent instanceof org.eclipse.compare.Splitter) {
				org.eclipse.compare.Splitter sp= (org.eclipse.compare.Splitter) parent;
				sp.setVisible(this, visible);
				sp.layout();
			}
		} else {
			layout();
		}
	}
	
	private boolean isEmpty() {
		Control[] controls= getChildren();
		for (int i= 0; i < controls.length; i++)
			if (isVisible(controls[i]))
				return false;
		return true;
	}
	
	private boolean isVisible(Control child) {
		if (child instanceof Sash)
			return false;
		Object data= child.getData(VISIBILITY);
		if (data instanceof Boolean)
			return ((Boolean)data).booleanValue();
		return true;
	}
			
	public void setMaximizedControl(Control control) {
		if (control == null || control == getMaximizedControl())
			super.setMaximizedControl(null);
		else
			super.setMaximizedControl(control);		
			
		// walk up
		Composite parent= getParent();
		if (parent instanceof Splitter)
			((Splitter) parent).setMaximizedControl(this);
		else if (parent instanceof org.eclipse.compare.Splitter)
			((org.eclipse.compare.Splitter) parent).setMaximizedControl(this);
		else
			layout(true);
	}
}
