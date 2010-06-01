/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.m12.model;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
public class M1Core {
	public static M1Resource getModelObject(IResource res) {
		switch (res.getType()) {
		case IResource.PROJECT:
			return new M1Project((IProject) res);
		case IResource.FOLDER:
			return new M1Folder((IFolder) res);
		case IResource.FILE:
			return new M1File((IFile) res);
		default:
			return null;
		}
	}
}
