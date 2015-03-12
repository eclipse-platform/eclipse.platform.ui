/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.modeling;

/**
 * @noreference This class is not intended to be referenced by clients.
 * @since 1.0
 */
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
