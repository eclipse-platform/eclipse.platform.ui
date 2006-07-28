/*******************************************************************************
 * Copyright (c) 2006 Coconut Palm Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Coconut Palm Software - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.binding;

import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.swt.widgets.Control;

/**
 * Defines an interface for objects that can bind a CompositeTable's row object
 * to an element of the collection to which the CompositeTable itself is bound.
 * @since 3.3
 */
public interface IRowBinder {
	/**
	 * Bind the controls inside the specified row object to the specified
	 * object using the supplied data binding context.
	 * <p>
	 * The sender will automatically dispose the bindings at the right time
	 * by disposing the supplied data binding context.
	 * 
	 * @param context The data binding context
	 * @param row The row Control to bind
	 * @param object The business model object to bind
	 */
	void bindRow(DataBindingContext context, Control row, Object object);
}
