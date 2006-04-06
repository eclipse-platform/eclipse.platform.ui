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

package org.eclipse.jface.examples.databinding.compositetable.day.internal;

/**
 * Represents a model of how the events are laid out in a particular day
 * 
 * @since 3.2
 */
public class DayModel {
	
	public DayModel() {
		
	}
	
	/**
	 * Return the number of columns required to layout the day's events
	 * 
	 * @return number of columns required to layout the day's events
	 */
	public int getNumberOfColumns() {
		return -1;
	}
	
	
}
