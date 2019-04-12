/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.ui.dialogs;

import java.util.Arrays;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A class to select elements out of a list of elements.
 *
 * @since 2.0
 */
public class ElementListSelectionDialog extends AbstractElementListSelectionDialog {

	private Object[] fElements;

	/**
	 * Creates a list selection dialog.
	 * 
	 * @param parent   the parent widget.
	 * @param renderer the label renderer.
	 */
	public ElementListSelectionDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
	}

	/**
	 * Sets the elements of the list.
	 * 
	 * @param elements the elements of the list.
	 */
	public void setElements(Object[] elements) {
		fElements = elements;
	}

	@Override
	protected void computeResult() {
		setResult(Arrays.asList(getSelectedElements()));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite contents = (Composite) super.createDialogArea(parent);

		createMessageArea(contents);
		createFilterText(contents);
		createFilteredList(contents);

		setListElements(fElements);

		setSelection(getInitialElementSelections().toArray());

		return contents;
	}
}
