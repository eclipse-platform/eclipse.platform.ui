/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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

package org.eclipse.ui.preferences;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.internal.preferences.WorkingCopyPreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * WorkingCopyManager is a concrete implementation of an IWorkingCopyManager.
 * <p>
 * This class is not intended to be sub-classed by clients.
 * </p>
 *
 * @since 3.2
 */
public class WorkingCopyManager implements IWorkingCopyManager {

	private static final String EMPTY_STRING = "";//$NON-NLS-1$
	// all working copies - maps absolute path to PreferencesWorkingCopy instance
	private Map<String, IEclipsePreferences> workingCopies = new HashMap<>();

	@Override
	public IEclipsePreferences getWorkingCopy(IEclipsePreferences original) {
		if (original instanceof WorkingCopyPreferences) {
			throw new IllegalArgumentException("Trying to get a working copy of a working copy"); //$NON-NLS-1$
		}
		String absolutePath = original.absolutePath();
		IEclipsePreferences preferences = workingCopies.get(absolutePath);
		if (preferences == null) {
			preferences = new WorkingCopyPreferences(original, this);
			workingCopies.put(absolutePath, preferences);
		}
		return preferences;
	}

	@Override
	public void applyChanges() throws BackingStoreException {
		Collection<IEclipsePreferences> values = workingCopies.values();
		WorkingCopyPreferences[] valuesArray = values.toArray(new WorkingCopyPreferences[values.size()]);
		for (WorkingCopyPreferences prefs : valuesArray) {
			if (prefs.nodeExists(EMPTY_STRING))
				prefs.flush();
		}
	}

}
