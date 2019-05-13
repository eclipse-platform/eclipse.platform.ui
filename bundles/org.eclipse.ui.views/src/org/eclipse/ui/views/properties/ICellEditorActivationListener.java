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
package org.eclipse.ui.views.properties;

import org.eclipse.jface.viewers.CellEditor;

/**
 * A listener which is notified when a cell editor is
 * activated/deactivated
 */
/*package*/interface ICellEditorActivationListener {
	/**
	 * Notifies that the cell editor has been activated
	 *
	 * @param cellEditor the cell editor which has been activated
	 */
	public void cellEditorActivated(CellEditor cellEditor);

	/**
	 * Notifies that the cell editor has been deactivated
	 *
	 * @param cellEditor the cell editor which has been deactivated
	 */
	public void cellEditorDeactivated(CellEditor cellEditor);
}
