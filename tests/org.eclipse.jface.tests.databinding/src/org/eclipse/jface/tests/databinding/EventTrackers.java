/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;

/**
 * Implementations of change listeners that keep track of the number of times an
 * event fires.
 * 
 * @since 1.1
 */
public class EventTrackers {
	public static class ChangeEventTracker implements IChangeListener {
		public int count;
		public ChangeEvent event;

		public void handleChange(ChangeEvent event) {
			count++;
			this.event = event;
		}
	}

	public static class ValueChangeEventTracker implements IValueChangeListener {
		public int count;
		public ValueChangeEvent event;

		public void handleValueChange(ValueChangeEvent event) {
			count++;
			this.event = event;
		}
	}
	
	public static class MapChangeEventTracker implements IMapChangeListener {
		public int count;
		public MapChangeEvent event;
		
		public void handleMapChange(MapChangeEvent event) {
			count++;
			this.event = event;
		}		
	}
}
