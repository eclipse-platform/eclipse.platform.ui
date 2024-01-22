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

package org.eclipse.ui.part;

/**
 * This interface must be provided by Show In targets (parts listed in the Show
 * In prompter). The part can either directly implement this interface, or
 * provide it via <code>IAdaptable.getAdapter(IShowInTarget.class)</code>.
 *
 * @see org.eclipse.ui.IPageLayout#addShowInPart
 *
 * @since 2.1
 */
public interface IShowInTarget {

	/**
	 * Shows the given context in this target. The target should check the context's
	 * selection for elements to show. If there are no relevant elements in the
	 * selection, then it should check the context's input.
	 *
	 * @param context the context to show
	 * @return <code>true</code> if the context could be shown, <code>false</code>
	 *         otherwise
	 */
	boolean show(ShowInContext context);
}
