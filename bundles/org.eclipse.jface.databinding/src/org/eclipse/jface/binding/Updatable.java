/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.binding;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.2
 *
 */
abstract public class Updatable implements IUpdatable {

	private List changeListeners = new ArrayList();

	public void addChangeListener(IChangeListener changeListener) {
		if (!changeListeners.contains(changeListener)) {
			changeListeners.add(changeListener);
		}
	}

	public void removeChangeListener(IChangeListener changeListener) {
		changeListeners.remove(changeListener);
	}

	protected IChangeEvent fireChangeEvent(int changeType, Object oldValue,
			Object newValue) {
		return fireChangeEvent(changeType, oldValue, newValue, 0);
	}

	protected IChangeEvent fireChangeEvent(int changeType, Object oldValue,
			Object newValue, int position) {
		ChangeEvent changeEvent = new ChangeEvent(this, changeType, oldValue,
				newValue, position);
		IChangeListener[] listeners = (IChangeListener[]) changeListeners
				.toArray(new IChangeListener[changeListeners.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleChange(changeEvent);
		}
		return changeEvent;
	}

	public void dispose() {
		changeListeners = null;
	}

}
