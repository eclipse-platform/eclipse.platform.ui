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

package org.eclipse.ui.internal.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * <p>
 * This class is not intended to be sub-classed by clients.
 * </p><p>
 * This class is a work-in-progress and its API may change before the final release of Eclipse 3.1.
 * </p>
 * @since 3.1
 */
public final class WorkingCopyManager implements IWorkingCopyManager{

	// all working copies - maps absolute path to PreferencesWorkingCopy instance
	private Map workingCopies = new HashMap();

	/**
	 * Return a working copy instance based on the given preference node. If a working
	 * copy already exists then return it, otherwise create one and keep track of it for 
	 * other clients who are looking for it.
	 * 
	 * @param original the original node
	 * @return the working copy node
	 */
	public IEclipsePreferences getWorkingCopy(IEclipsePreferences original) {
		if (original instanceof WorkingCopyPreferences)
			throw new IllegalArgumentException("Trying to get a working copy of a working copy"); //$NON-NLS-1$
		String absolutePath = original.absolutePath();
		IEclipsePreferences preferences = (IEclipsePreferences) workingCopies.get(absolutePath);
		if (preferences == null) {
			preferences = new WorkingCopyPreferences(original, this);
			workingCopies.put(absolutePath, preferences);
		}
		return preferences;
	}

	/**
	 * Apply the changes for <em>all</em> working copies, to their original preference
	 * nodes. Alternatively, if a client wishes to apply the changes for a single working copy
	 * they can call <code>#flush</code> on that working copy node.
	 * 
	 * @throws BackingStoreException if there were problems accessing the backing store
	 */
	public void applyChanges() throws BackingStoreException {
		for (Iterator i = workingCopies.values().iterator(); i.hasNext();)
			((WorkingCopyPreferences) i.next()).flush();
	}

}
