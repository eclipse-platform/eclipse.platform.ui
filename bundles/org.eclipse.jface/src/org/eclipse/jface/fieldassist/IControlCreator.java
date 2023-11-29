/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.jface.fieldassist;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This interface is used to create a control with a specific parent and style
 * bits. It is used by {@link DecoratedField} to create the control to be
 * decorated. Clients are expected to implement this interface in order to
 * create a particular kind of control for decoration.
 *
 * @since 3.2
 * @deprecated As of 3.3, clients should use {@link ControlDecoration} instead
 *             of {@link DecoratedField}.
 */
@Deprecated
public interface IControlCreator {
	/**
	 * Create a control with the specified parent and style bits.
	 *
	 * @param parent
	 *            the parent of the control
	 * @param style
	 *            the style of the control
	 *
	 * @return the Control that was created.
	 */
	public Control createControl(Composite parent, int style);
}
