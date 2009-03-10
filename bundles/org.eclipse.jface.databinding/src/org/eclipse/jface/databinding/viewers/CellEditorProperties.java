/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 234496)
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.internal.databinding.viewers.CellEditorControlProperty;
import org.eclipse.jface.viewers.CellEditor;

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
	public static IValueProperty control() {
		return new CellEditorControlProperty();
	}
}
