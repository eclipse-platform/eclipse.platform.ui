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

import org.eclipse.jface.viewers.ISelection;

/**
 * Carries the context for the Show In action. The default implementation
 * carries an input and a selection. Subclasses may extend.
 *
 * @see IShowInSource
 * @see IShowInTarget
 *
 * @since 2.1
 */
public class ShowInContext {

	private Object input;

	private ISelection selection;

	/**
	 * Constructs a new <code>ShowInContext</code> with the given input and
	 * selection.
	 *
	 * @param input     the input or <code>null</code>
	 * @param selection the selection or <code>null</code>
	 */
	public ShowInContext(Object input, ISelection selection) {
		setInput(input);
		setSelection(selection);
	}

	/**
	 * Returns the input, or <code>null</code> to indicate no input
	 *
	 * @return the input or <code>null</code>.
	 */
	public Object getInput() {
		return input;
	}

	/**
	 * Returns the selection, or <code>null</code> to indicate no selection.
	 *
	 * @return the selection or <code>null</code>
	 */
	public ISelection getSelection() {
		return selection;
	}

	/**
	 * Sets the input, or <code>null</code> to indicate no input.
	 *
	 * @param input the input or <code>null</code>
	 */
	public void setInput(Object input) {
		this.input = input;
	}

	/**
	 * Sets the selection, or <code>null</code> to indicate no selection.
	 *
	 * @param selection the selection or <code>null</code>
	 */
	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

}
