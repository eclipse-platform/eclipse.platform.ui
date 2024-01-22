/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
	@Override
	public void removeWorkingSet(IWorkingSet workingSet) {
		internalRemoveWorkingSet(workingSet);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addRecentWorkingSet(IWorkingSet workingSet) {
		internalAddRecentWorkingSet(workingSet);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveState(IMemento memento) {
		saveWorkingSetState(memento);
		saveMruList(memento);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restoreState(IMemento memento) {
		Assert.isNotNull(memento);
		Assert.isTrue(getWorkingSets().length == 0);
		restoreWorkingSetState(memento);
		restoreMruList(memento);
	}
}
