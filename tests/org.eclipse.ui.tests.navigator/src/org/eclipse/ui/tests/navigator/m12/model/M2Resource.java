/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.m12.model;
import org.eclipse.core.resources.IResource;
public class M2Resource extends ResourceWrapper {
	public M2Resource(IResource resource) {
		super(resource);
	}
	protected ResourceWrapper getModelObject(IResource resource) {
		return M2Core.getModelObject(resource);
	}
	public String getModelId() {
		return "M2";
	}
}
