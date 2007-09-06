/*******************************************************************************
 * Copyright (c) 2007 Ashley Cambrell and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ashley Cambrell - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;

class TestCounterValueChangeListener implements
		IValueChangeListener {
	public int counter = 0;

	public void handleValueChange(ValueChangeEvent event) {
		++counter;
	}
}
