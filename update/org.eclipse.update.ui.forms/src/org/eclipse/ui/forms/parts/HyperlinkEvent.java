/*
 * Created on Nov 26, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.parts;

import org.eclipse.swt.events.TypedEvent;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HyperlinkEvent extends TypedEvent {
	private String label;
	
	public HyperlinkEvent(Object obj, Object href, String label) {
		super(obj);
		this.data = href;
		this.label = label;
	}
	public Object getHref() {
		return this.data;
	}
	public String getLabel() {
		return label;
	}
}
