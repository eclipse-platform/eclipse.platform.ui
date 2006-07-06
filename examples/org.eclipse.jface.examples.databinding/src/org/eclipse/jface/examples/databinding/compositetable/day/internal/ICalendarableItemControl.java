/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.day.internal;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;

/**
 * @since 3.2
 *
 */
public interface ICalendarableItemControl {

	/* (non-Javadoc)
	 * {@link org.eclipse.swt.widgets.Widget#dispose()}
	 */
	public void dispose();

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setMenu(org.eclipse.swt.widgets.Menu)
	 */
	public void setMenu(Menu menu);

	/**
	 * @param text
	 */
	public void setText(String text);

	/**
	 * @param image
	 */
	public void setImage(Image image);

	public void setToolTipText(String text);

	/**
	 * Sets the clipping style bits
	 * @param clipping  One of SWT.TOP or SWT.BOTTOM
	 */
	public void setClipping(int clipping);

	/**
	 * @return The clipping style bits
	 */
	public int getClipping();

	/**
	 * Sets the continued style bits
	 * @param continued  One of SWT.TOP or SWT.BOTTOM
	 */
	public void setContinued(int continued);

	/**
	 * @return the continued style bits
	 */
	public int getContinued();

	/**
	 * Set or clear the selection indicator in the UI.
	 * 
	 * @param selected true if this control should appear selected; false otherwise.
	 */
	public void setSelected(boolean selected);

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addMouseListener(org.eclipse.swt.events.MouseListener)
	 */
	public void addMouseListener(MouseListener listener);

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeMouseListener(org.eclipse.swt.events.MouseListener)
	 */
	public void removeMouseListener(MouseListener listener);

	/**
	 * Method setCalendarable. Sets the associated model.
	 * @param calendarable
	 */
	public void setCalendarableItem(CalendarableItem calendarable);

	/**
	 * @return Returns the calendarable.
	 */
	public CalendarableItem getCalendarableItem();

}