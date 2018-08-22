/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.jface.text.source;

/**
 * Extension interface for {@link ILineDiffer}.
 * <p>
 * Allows to query the suspension state.
 * </p>
 *
 * @since 3.3
 */
public interface ILineDifferExtension2 {
	/**
	 * Returns <code>true</code> if the receiver is suspended, <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if the receiver is suspended, <code>false</code> otherwise
	 */
	boolean isSuspended();
}
