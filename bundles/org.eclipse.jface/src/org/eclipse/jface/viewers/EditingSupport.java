/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

/**
 * EditingSupport is the abstract superclass of the support for cell editing.
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as
 * part of a work in progress. This API may change at any given time. Please 
 * do not use this API without consulting with the Platform/UI team.
 * @since 3.3
 * 
 */
public abstract class EditingSupport {
	/**
	 * The editor to be shown
	 * 
	 * @param element
	 *            the model element
	 * @return the CellEditor
	 */
	protected abstract CellEditor getCellEditor(Object element);

	/**
	 * Is the cell editable
	 * 
	 * @param element
	 *            the model element
	 * @return true if editable
	 */
	protected abstract boolean canEdit(Object element);

	/**
	 * Get the value to set to the editor
	 * 
	 * @param element
	 *            the model element
	 * @return the value shown
	 */
	protected abstract Object getValue(Object element);

	/**
	 * Restore the value from the CellEditor
	 * 
	 * @param element
	 *            the model element
	 * @param value
	 *            the new value
	 */
	protected abstract void setValue(Object element, Object value);
}
