/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.menus;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IContributorResourceAdapter;

public class ObjectContributionClasses implements IAdapterFactory {
	
	public static final String PROJECT_NAME = "testContributorResourceAdapter";
	
	public static interface ICommon {
	}
	
	public static class Common implements ICommon {		
	}
	
	public static interface IA {
	}
	
	public static class A implements IA {	
	}
	
	public static class A1 extends A {
	}
	
	public static class A11 extends A1 {
	}
	
	public static interface IB {
	}
	
	public static class B implements IB {
	}
	
	public static class B2 implements IB {
	}
	
	public static class D extends Common implements IA {
	}
	
	public static class C implements ICommon {
	}
	
	public static class CResource implements IAdaptable {
		public Object getAdapter(Class adapter) {
			if(adapter == IContributorResourceAdapter.class) {
				return new ResourceAdapter();
			}			
			return null;
		}		
	}
	
	public static class CFile implements IAdaptable {
		public Object getAdapter(Class adapter) {
			if(adapter == IContributorResourceAdapter.class) {
				return new ResourceAdapter();
			}			
			return null;
		}		
	}
	
	// Default contributor adapter
	
	public static class ResourceAdapter implements IContributorResourceAdapter {
		public IResource getAdaptedResource(IAdaptable adaptable) {
			if(adaptable instanceof CResource) {
				return ResourcesPlugin.getWorkspace().getRoot();
			}
			if(adaptable instanceof CFile) {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME).getFile("dummy");
			}
			return null;
		}	
	}
	
	// Adapter methods
	
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adapterType == IContributorResourceAdapter.class) {
			return new ResourceAdapter();
		}
		if(adaptableObject instanceof IA && adapterType == IA.class) {
			return new A();
		}
		if(adapterType == IResource.class) {
			return ResourcesPlugin.getWorkspace().getRoot();
		}
		if(adapterType == ICommon.class) {
			return new Common();
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { ICommon.class, IResource.class, IFile.class, IContributorResourceAdapter.class};
	}
}
