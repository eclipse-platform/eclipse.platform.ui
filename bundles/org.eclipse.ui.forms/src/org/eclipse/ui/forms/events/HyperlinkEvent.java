/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.events;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Widget;
/**
 * Notifies listeners about a hyperlink change.
 * 
 * TODO (dejan) - spell out subclass contract
 * @since 3.0
 */
public class HyperlinkEvent extends TypedEvent {
	private String label;
	/**
	 * Creates a new hyperlink
	 * 
	 * @param obj
	 *            event source
	 * @param href
	 *            the hyperlink reference that will be followed upon when the
	 *            hyperlink is activated.
	 * @param label
	 *            the name of the hyperlink (the text that is rendered as a
	 *            link in the source widget).
	 */
	public HyperlinkEvent(Widget widget, Object href, String label) {
		super(widget);
		this.widget = widget;
		this.data = href;
		this.label = label;
	}
	/**
	 * The hyperlink reference that will be followed when the hyperlink is
	 * activated.
	 * 
	 * @return the hyperlink reference object
	 */
	public Object getHref() {
		return this.data;
	}
	/**
	 * The text of the hyperlink rendered in the source widget.
	 * 
	 * @return the hyperlink label
	 */
	public String getLabel() {
		return label;
	}
}