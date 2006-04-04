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
 * A strategy pattern object that returns the number of events to display for a 
 * specific time period.
 * 
 * @since 3.2
 */
public abstract class EventCountProvider {
	/**
	 * Returns the number of events to display on a specific day.
	 *  
	 * @param day The day to query.
	 * @return The number of events on the specified day.
	 */
	abstract public int getNumberOfEventsInDay(Date day);
}
