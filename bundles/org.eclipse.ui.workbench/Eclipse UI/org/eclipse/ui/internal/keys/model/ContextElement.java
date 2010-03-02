/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
