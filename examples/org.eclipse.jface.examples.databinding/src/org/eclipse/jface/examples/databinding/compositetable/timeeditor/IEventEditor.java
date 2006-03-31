/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.timeeditor;

import java.util.Date;

/**
 * Interface IEventEditor.  An interface for editors of time-based data that
 * can be visualized on various calendar-like controls.
 * 
 * @since 3.2
 */
public interface IEventEditor {
	/**
	 * Set the start date for this event editor.  How this is interpreted depends
	 * on how time is being visualized.
	 * <p>
	 * For example, a month editor would only pay attention to the month portion
	 * of the date.  A multi-day editor would make the date passed be the first
	 * date edited in the set of days being visualized.
	 *  
	 * @param startDate The date representing what slice of time to visualize in the editor.
	 */
	void setStartDate(Date startDate);
	
	/**
	 * Set the strategy pattern object that can return how many events to display
	 * for specific periods of time.
	 * 
	 * @param eventCountProvider The eventCountProvider to set.
	 */
	void setDayEventCountProvider(EventCountProvider eventCountProvider);
	
	/**
	 * Sets the strategy pattern object that can set the properties of the 
	 * event objects in order to display the data associated with the 
	 * specified event.
	 * 
	 * @param eventContentProvider The eventContentProvider to set.
	 */
	void setEventContentProvider(EventContentProvider eventContentProvider);
	
	/* Not needed for now? */
//	void addInsertHandler(IInsertHandler);
	
//	void addDeleteHandler(IDeleteHandler);
}
