/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
package org.eclipse.jface.examples.databinding.model;

import java.beans.PropertyChangeListener;

public class Cart {

	public void setAdventureDays(int i) {
		// TODO Auto-generated method stub

	}

	public int getLodgingDays() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		// not really necessary, but BeansObservables.observeValue(...) expects
		// it.
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		// not really necessary, but BeansObservables.observeValue(...) expects
		// it.
	}
}
