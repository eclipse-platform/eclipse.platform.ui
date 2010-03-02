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
import org.eclipse.jface.bindings.Scheme;

/**
 * @since 3.4
 * 
 */
public class SchemeElement extends ModelElement {

	/**
	 * @param kc
	 */
	public SchemeElement(KeyController kc) {
		super(kc);
	}

	/**
	 * @param scheme
	 */
	public void init(Scheme scheme) {
		setId(scheme.getId());
		setModelObject(scheme);
		try {
			setName(scheme.getName());
			setDescription(scheme.getDescription());
		} catch (NotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
