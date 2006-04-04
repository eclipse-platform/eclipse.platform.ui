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
 * A strategy pattern object that can refresh all of the event controls
 * for a given day.
 * 
 * @since 3.2
 */
public abstract class EventContentProvider {
	/**
	 * Refreshes the contents of the specified event controls for the specified
	 * day.
	 * 
	 * @param day The day
	 * @param controls The controls to refresh
	 */
	abstract public void refresh(Date day, ICalendarable[] controls);
}
