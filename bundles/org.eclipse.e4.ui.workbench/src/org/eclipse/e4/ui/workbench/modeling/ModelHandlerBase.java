/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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
package org.eclipse.e4.ui.workbench.modeling;

/**
 * @noreference This class is not intended to be referenced by clients.
 * @since 1.0
 */
@SuppressWarnings("unused") // parameters unused - this class should acts as interface only
public class ModelHandlerBase {
	protected ModelHandlerBase() {
	}

	public Object getProperty(Object element, String id) {
		return null;
	}

	public void setProperty(Object element, String id, Object value) {
	}

	public Object[] getChildren(Object element, String id) {
		return new Object[0];
	}

	public String[] getPropIds(Object element) {
		return new String[0];
	}
}
