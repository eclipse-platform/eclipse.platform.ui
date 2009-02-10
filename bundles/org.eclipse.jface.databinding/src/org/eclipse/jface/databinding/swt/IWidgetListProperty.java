/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *******************************************************************************/

package org.eclipse.jface.databinding.swt;

import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.swt.widgets.Widget;

/**
 * {@link IListProperty} for observing an SWT Widget
 * 
 * @since 1.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWidgetListProperty extends IListProperty {
	/**
	 * Returns an {@link ISWTObservableList} observing this list property on the
	 * given widget
	 * 
	 * @param widget
	 *            the source widget
	 * @return an observable list observing this list property on the given
	 *         widget
	 */
	public ISWTObservableList observe(Widget widget);
}
