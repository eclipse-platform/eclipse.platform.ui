/*******************************************************************************
 * Copyright (c) 2012 Tensilica Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abeer Bagul (Tensilica Inc) - initial API and implementation (Bug 372181)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

/**
 * A marker interface to be implemented by objects which represent the "Add new expression" node in Expressions view.
 * An object which implements this interface will always be visible in the view,
 * even if user has selected working sets to filter expressions visible in the view.
 */
public interface IAddNewExpression {
	
}
