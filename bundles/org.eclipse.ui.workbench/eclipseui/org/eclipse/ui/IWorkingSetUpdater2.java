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
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;

/**
 * <code>IWorkingSetUpdater2</code> can be used to restore the content of a
 * working set, if the working set content shouldn't be persisted by the framework.
 *
 * @since 3.120
 */
public interface IWorkingSetUpdater2 extends IWorkingSetUpdater {

	/**
	 * Answers if the given working set content presistence is managed by this
	 * updater
	 *
	 * @param set non null working set
	 * @return true if the working set content should not be persisted by the
	 *         framework
	 */
	boolean isManagingPersistenceOf(IWorkingSet set);

	/**
	 * Restores working set content if its persistence is managed by current updater
	 *
	 * @param set non null working set
	 * @return non null array with initial children of given working set
	 * @see #isManagingPersistenceOf(IWorkingSet)
	 */
	IAdaptable[] restore(IWorkingSet set);
}
