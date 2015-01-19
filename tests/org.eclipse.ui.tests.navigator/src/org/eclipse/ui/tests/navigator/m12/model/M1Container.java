/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.m12.model;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
public class M1Container extends M1Resource {
	public M1Container(IContainer container) {
		super(container);
	}
	@Override
	public M1Resource[] getChildren() throws CoreException {
		IContainer container = (IContainer) getResource();
		IResource[] children = (container.isAccessible()) ? container.members()
				: new IResource[0];
		M1Resource[] m1Children = new M1Resource[children.length];
		for (int i = 0; i < children.length; i++) {
			m1Children[i] = M1Core.getModelObject(children[i]);
		}
		return m1Children;
	}
}
