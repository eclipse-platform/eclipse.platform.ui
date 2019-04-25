/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 234496)
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.internal.databinding.viewers.CellEditorControlProperty;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Control;

/**
 * A factory for creating properties of JFace {@link CellEditor cell editors}.
 *
 * @since 1.3
 */
public class CellEditorProperties {
	/**
	 * Returns a value property for observing the control of a
	 * {@link CellEditor}.
	 *
	 * @return a value property for observing the control of a
	 *         {@link CellEditor}.
	 */
	public static IValueProperty<CellEditor, Control> control() {
		return new CellEditorControlProperty();
	}
}
