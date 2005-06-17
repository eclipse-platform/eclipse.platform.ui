/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.spelling;

import org.eclipse.core.runtime.IStatus;

/**
 * A monitor that can be notified about status changes.
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 *
 * @see ISpellingPreferenceBlock#initialize(IPreferenceStatusMonitor)
 * @since 3.1
 */
public interface IPreferenceStatusMonitor {

	/**
	 * Notifies this monitor that the preference page's status has changed
	 * the given status.
	 *
	 * @param status the new status
	 */
	void statusChanged(IStatus status);
}
