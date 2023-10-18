/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.IAdaptable;

/**
 * A factory to create <code>IValidationCheckResultQuery</code> objects.
 * The LTK user interface plug-in provides a special factory for dialog
 * based queries.
 * <p>
 * The interface may be implemented by clients.
 * </p>
 *
 * @since 3.1
 */
public interface IValidationCheckResultQueryFactory {

	/**
	 * Creates a new query.
	 *
	 * @param context the factory adapts the context to a
	 * <code>org.eclipse.swt.widgets.Shell</code> that is to be used to parent
	 * any dialogs with the user; use <code>null</code> if there is no UI context
	 *
	 * @return the new query
	 */
	IValidationCheckResultQuery create(IAdaptable context);
}
