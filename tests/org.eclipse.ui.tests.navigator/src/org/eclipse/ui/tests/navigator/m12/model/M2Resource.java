/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.m12.model;
import org.eclipse.core.resources.IResource;
public class M2Resource extends ResourceWrapper {
	public M2Resource(IResource resource) {
		super(resource);
	}
	@Override
	protected ResourceWrapper getModelObject(IResource resource) {
		return M2Core.getModelObject(resource);
	}
	@Override
	public String getModelId() {
		return "M2";
	}
}
