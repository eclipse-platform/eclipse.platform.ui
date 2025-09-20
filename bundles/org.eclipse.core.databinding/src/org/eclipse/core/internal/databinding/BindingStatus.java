/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.core.internal.databinding;

import java.util.Arrays;

import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * A <code>MultiStatus</code> implementation that copies that state of the
 * added status to this instance if it is &gt;= the current severity.
 *
 * @since 1.0
 */
public class BindingStatus extends MultiStatus {
	/**
	 * Constructs a new instance.
	 *
	 * @param pluginId  the unique identifier of the relevant plug-in
	 * @param code      the plug-in-specific status code
	 * @param message   a human-readable message, localized to the current locale
	 * @param exception a low-level exception, or <code>null</code> if not
	 *                  applicable
	 */
	public BindingStatus(String pluginId, int code, String message,
			Throwable exception) {
		super(pluginId, code, message, exception);
	}

	/**
	 * Adds the status to the multi status. The details of the status will be
	 * copied to the multi status if the severity is &gt;= the current severity.
	 *
	 * @see org.eclipse.core.runtime.MultiStatus#add(org.eclipse.core.runtime.IStatus)
	 */
	@Override
	public void add(IStatus status) {
		if (status.getSeverity() >= getSeverity()) {
			setMessage((status.getMessage() != null) ? status.getMessage() : ""); //$NON-NLS-1$
			setException(status.getException());
			setPlugin(status.getPlugin());
			setCode(status.getCode());
		}

		super.add(status);
	}

	/**
	 * Instance initialized with the following values:
	 * <ul>
	 * <li>plugin = Policy.JFACE_DATABINDING</li>
	 * <li>severity = 0</li>
	 * <li>code = 0</li>
	 * <li>message = ""</li>
	 * <li>exception = null</li>
	 * </ul>
	 *
	 * @return status
	 */
	public static BindingStatus ok() {
		return new BindingStatus(Policy.JFACE_DATABINDING, 0, "", null); //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		return prime * result + Arrays.hashCode(getChildren());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BindingStatus other = (BindingStatus) obj;
		return Arrays.equals(getChildren(), other.getChildren());
	}
}
