/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;

/**
 * @since 3.4
 *
 */
public class SchemeModel extends CommonModel {

	public static final String PROP_SCHEMES = "schemes"; //$NON-NLS-1$
	private List schemes;

	/**
	 * @param kc
	 */
	public SchemeModel(KeyController kc) {
		super(kc);
	}

	/**
	 * @param bindingManager
	 */
	public void init(BindingManager bindingManager) {
		schemes = new ArrayList();
		for (Scheme definedScheme : bindingManager.getDefinedSchemes()) {
			SchemeElement se = new SchemeElement(controller);
			se.init(definedScheme);
			se.setParent(this);
			schemes.add(se);
			if (definedScheme == bindingManager.getActiveScheme()) {
				setSelectedElement(se);
			}
		}
	}

	/**
	 * @return Returns the schemes.
	 */
	public List getSchemes() {
		return schemes;
	}

	/**
	 * @param schemes
	 *            The schemes to set.
	 */
	public void setSchemes(List schemes) {
		List old = this.schemes;
		this.schemes = schemes;
		controller.firePropertyChange(this, PROP_SCHEMES, old, schemes);
	}

}
