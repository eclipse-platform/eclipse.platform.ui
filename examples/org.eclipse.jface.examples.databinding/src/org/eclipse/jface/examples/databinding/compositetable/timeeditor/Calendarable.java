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

package org.eclipse.jface.examples.databinding.compositetable.timeeditor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * @since 3.2
 *
 */
public class Calendarable implements ICalendarable {

	private Date startTime = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#getStartTime()
	 */
	public Date getStartTime() {
		return startTime;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#setStartTime(java.util.Date)
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	private Date endTime = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#getEndTime()
	 */
	public Date getEndTime() {
		return endTime;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#setEndTime(java.util.Date)
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	private Image image;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#getImage()
	 */
	public Image getImage() {
		return this.image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#setImage(org.eclipse.swt.graphics.Image)
	 */
	public void setImage(Image image) {
		this.image = image;
	}

	private String text = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#getText()
	 */
	public String getText() {
		return text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#setText(java.lang.String)
	 */
	public void setText(String string) {
		this.text = string;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#reset()
	 */
	public void reset() {
		text = null;
		startTime = null;
		endTime = null;
		image = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#dispose()
	 */
	public void dispose() {
		fireDisposeEvent();
	}
	
	private List disposeListeners = new ArrayList();

	private void fireDisposeEvent() {
		for (Iterator disposeListenerIter = disposeListeners.iterator(); disposeListenerIter.hasNext();) {
			DisposeListener listener = (DisposeListener) disposeListenerIter.next();
			listener.widgetDisposed(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#addDisposeListener(org.eclipse.swt.events.DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener) {
		disposeListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEvent#removeDisposeListener(org.eclipse.swt.events.DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener) {
		disposeListeners.remove(listener);
	}

}
