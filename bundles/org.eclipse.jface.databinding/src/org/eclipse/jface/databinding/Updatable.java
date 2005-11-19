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
package org.eclipse.jface.databinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for updatable objects.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
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

	protected ChangeEvent fireChangeEvent(int changeType, Object oldValue,
			Object newValue) {
		return fireChangeEvent(changeType, oldValue, newValue, 0);
	}
	
	protected ChangeEvent fireChangeEvent(int changeType, Object oldValue, Object newValue,  int position) {
		return fireChangeEvent(changeType, oldValue, newValue, null, position);
	}

	protected ChangeEvent fireChangeEvent(int changeType, Object oldValue, Object newValue, Object parent, int position) {
		ChangeEvent changeEvent = new ChangeEvent(this, changeType, oldValue,
				newValue, parent, position);
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
