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

import org.eclipse.core.runtime.IAdapterManager;

/**
 * @noreference This class is not intended to be referenced by clients.
 * @since 1.0
 */
public class ModelService extends ModelHandlerBase {
	private IAdapterManager manager;

	public ModelService(IAdapterManager manager) {
		this.manager = manager;
	}

	private Object loadAdapterLocal(Object element) {
		ModelHandlerBase handler = (ModelHandlerBase) manager.getAdapter(element,
				ModelHandlerBase.class);
		if (handler == null) {
			handler = (ModelHandlerBase) manager.loadAdapter(element,
					ModelHandlerBase.class.getName());
		}

		return handler;
	}

	@Override
	public Object[] getChildren(Object element, String id) {
		ModelHandlerBase handler = (ModelHandlerBase) loadAdapterLocal(element);
		if (handler != null) {
			return handler.getChildren(element, id);
		}

		return new Object[0];
	}

	@Override
	public Object getProperty(Object element, String id) {
		ModelHandlerBase handler = (ModelHandlerBase) loadAdapterLocal(element);
		if (handler != null) {
			return handler.getProperty(element, id);
		}

		return null;
	}

	@Override
	public String[] getPropIds(Object element) {
		ModelHandlerBase handler = (ModelHandlerBase) loadAdapterLocal(element);
		if (handler != null) {
			return handler.getPropIds(element);
		}

		return new String[0];
	}

	@Override
	public void setProperty(Object element, String id, Object value) {
		ModelHandlerBase handler = (ModelHandlerBase) loadAdapterLocal(element);
		if (handler != null) {
			handler.setProperty(element, id, value);
		}
	}
}
