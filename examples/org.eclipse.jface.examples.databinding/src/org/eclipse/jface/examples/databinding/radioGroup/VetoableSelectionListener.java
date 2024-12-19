/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
package org.eclipse.jface.examples.databinding.radioGroup;

import org.eclipse.swt.events.SelectionEvent;

/**
 * Interface VetoableSelectionListener. An interface for SelectionListeners that
 * permit the new selection to be vetoed before widgetSelected or
 * widgetDefaultSelected is called.
 */
@FunctionalInterface
public interface VetoableSelectionListener {
	/**
	 * Method widgetCanChangeSelection. Indicates that the selection is about to be
	 * changed. Setting e.doit to false will prevent the selection from changing.
	 *
	 * @param e The SelectionEvent that is being processed.
	 */
	public void canWidgetChangeSelection(SelectionEvent e);
}
