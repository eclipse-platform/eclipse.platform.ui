/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.themes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @since 3.0
 */
public class ThemePropertyListener implements IPropertyChangeListener {

	private ArrayList<PropertyChangeEvent> events = new ArrayList<>();

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		events.add(event);
	}

	public List<PropertyChangeEvent> getEvents() {
		return events;
	}
}
