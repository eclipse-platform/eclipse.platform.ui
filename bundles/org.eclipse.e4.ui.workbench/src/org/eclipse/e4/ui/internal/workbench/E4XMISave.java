/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.impl.XMISaveImpl;

/**
 * Custom save handler for e4 workbench model. Does filter elements that shall
 * not be persisted without changing the underlying model.
 */
public class E4XMISave extends XMISaveImpl {

	public E4XMISave(XMLHelper helper) {
		super(helper);
	}

	/*
	 * Filter elements that are not be persisted.
	 */
	@Override
	protected void saveElement(InternalEObject o, EStructuralFeature f) {
		if (o instanceof MApplicationElement) {
			MApplicationElement appElement = (MApplicationElement) o;
			String persists = appElement.getPersistedState().get(IWorkbench.PERSIST_STATE);
			if (persists != null && !Boolean.parseBoolean(persists)) {
				return;
			}
		}

		super.saveElement(o, f);
	}
}
