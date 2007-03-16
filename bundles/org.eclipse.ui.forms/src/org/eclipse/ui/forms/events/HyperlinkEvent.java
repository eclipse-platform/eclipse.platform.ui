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
package org.eclipse.ui.forms.events;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Widget;
/**
 * Notifies listeners about a hyperlink change.
 *
 * @since 3.0
 */
public final class HyperlinkEvent extends TypedEvent {
	private static final long serialVersionUID = 6009335074727417445L;
	private String label;
	private int stateMask;
	/**
	 * Creates a new hyperlink
	 * 
	 * @param widget
	 *            event source
	 * @param href
	 *            the hyperlink reference that will be followed upon when the
	 *            hyperlink is activated.
	 * @param label
	 *            the name of the hyperlink (the text that is rendered as a
	 *            link in the source widget).
	 * @param stateMask
	 *            the given state mask
	 */
	public HyperlinkEvent(Widget widget, Object href, String label, int stateMask) {
		super(widget);
		this.widget = widget;
		this.data = href;
		this.label = label;
		this.stateMask = stateMask;
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
	/**
	 * Returns the value of the keyboard state mask present when
	 * the event occured, or SWT.NULL for no modifiers.
	 * @return the keyboard state mask or <code>SWT.NULL</code>. 
	 */
	public int getStateMask() {
		return stateMask;
	}
}
