/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 288642)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

/**
 * @since 3.3
 *
 */
public abstract class SingleSelectionIndexProperty extends
		WidgetIntValueProperty {
	/**
	 * @param events
	 */
	public SingleSelectionIndexProperty(int[] events) {
		super(events);
	}

	@Override
	protected void doSetValue(Object source, Object value) {
		super.doSetValue(source, value == null ? new Integer(-1) : value);
	}
}