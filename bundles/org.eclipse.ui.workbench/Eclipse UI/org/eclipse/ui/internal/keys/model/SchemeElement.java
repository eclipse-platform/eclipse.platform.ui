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
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.ui.internal.WorkbenchPlugin;

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
			WorkbenchPlugin.log(e);
		}
	}
}
