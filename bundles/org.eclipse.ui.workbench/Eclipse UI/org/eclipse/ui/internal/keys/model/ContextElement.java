/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.keys.model;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;

/**
 * @since 3.4
 *
 */
public class ContextElement extends ModelElement {

	/**
	 * @param kc
	 */
	public ContextElement(KeyController kc) {
		super(kc);
	}

	public void init(Context context) {
		setId(context.getId());
		setModelObject(context);
		try {
			setName(context.getName());
			setDescription(context.getDescription());
		} catch (NotDefinedException e) {
		}
	}
}
