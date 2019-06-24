/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * Extension interface for {@link org.eclipse.jface.text.ITextViewer}. Adds the ability to retrieve
 * the last known selection from outside of the UI Thread.
 *
 * @since 3.16
 */
public interface ITextViewerExtension9 {


	/**
	 * Returns the last known selection from a cache, without polling widget.
	 * <p>
	 * This may <strong>not</strong> be the current selection. Indeed, operations that change the
	 * selection without sending related events may not refresh the returned value.
	 * </p>
	 * <p>
	 * As opposed to {@link ISelectionProvider#getSelection()} that usually requires UI Thread, this
	 * method can run from any thread.
	 * </p>
	 *
	 * @return the last known selection.
	 */
	public ITextSelection getLastKnownSelection();
}
