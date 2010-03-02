/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.ILocalWorkingSetManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.osgi.framework.BundleContext;


public class LocalWorkingSetManager extends AbstractWorkingSetManager implements ILocalWorkingSetManager {

	public LocalWorkingSetManager(BundleContext context) {
		super(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeWorkingSet(IWorkingSet workingSet) {
		internalRemoveWorkingSet(workingSet);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addRecentWorkingSet(IWorkingSet workingSet) {
		internalAddRecentWorkingSet(workingSet);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveState(IMemento memento) {
        saveWorkingSetState(memento);
        saveMruList(memento);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void restoreState(IMemento memento) {
		Assert.isNotNull(memento);
		Assert.isTrue(getWorkingSets().length == 0);
        restoreWorkingSetState(memento);
        restoreMruList(memento);
	}
}
