/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.jface.databinding.swt;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.swt.widgets.Widget;

/**
 * {@link IObservable} observing an SWT widget.
 *
 * @since 1.1
 *
 */
public interface ISWTObservable extends IObservable {

	/**
	 * Returns the widget of this observable
	 *
	 * @return the widget
	 */
	public Widget getWidget();

}
