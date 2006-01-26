/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.core.mapping;

import java.lang.reflect.InvocationTargetException;

import junit.framework.Test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.core.mapping.provider.ScopeGenerator;
import org.eclipse.team.internal.core.mapping.ResourceMappingScope;
import org.eclipse.team.tests.core.TeamTest;
import org.eclipse.team.ui.operations.ResourceMappingOperation;

public class ScopeBuildingTests extends TeamTest {

	private static final RuntimeException PROMPT_EXCEPTION = new RuntimeException();
	protected static final String TEST_MODEL_PROVIDER_ID = "id1";

	private class TestResourceMappingOperation extends ResourceMappingOperation {
		
		private ResourceMapping[] additionalMappings;

		protected TestResourceMappingOperation(ResourceMapping[] selectedMappings, ResourceMapping[] additionalMappings) {
			super(null, selectedMappings);
			this.additionalMappings = additionalMappings;
		}

		protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			// Do nothing since we're just testing the scope build
		}
		
		protected void promptForInputChange(IProgressMonitor monitor) {
			// Throw an exception to indicate that a prompt was requested
			throw PROMPT_EXCEPTION;
		}
		
		protected ScopeGenerator getScopeGenerator() {
			return new ScopeGenerator(getResourceMappingContext(), false) {	
				public IResourceMappingScope prepareScope(
						ResourceMapping[] selectedMappings,
						IProgressMonitor monitor) throws CoreException {
					
					IResourceMappingScope resourceMappingScope = super.prepareScope(selectedMappings, monitor);
					// Add the additional test mappings to the scope
					for (int i = 0; i < additionalMappings.length; i++) {
						ResourceMapping mapping = additionalMappings[i];
						ResourceTraversal[] traversals = mapping.getTraversals(getContext(), monitor);
						((ResourceMappingScope)resourceMappingScope).addMapping(mapping, traversals);
						// TODO: The additional mappings and additional resources boolean will not be set
						// TODO: This may bring in mappings from the resources modle provider
					}
					return resourceMappingScope;
				}
			
			};
		}
		
	}
	public static Test suite() {
		return suite(ScopeBuildingTests.class);
	}
	
	public ScopeBuildingTests() {
		super();
	}

	public ScopeBuildingTests(String name) {
		super(name);
	}
	
	private void expectPrompt(TestResourceMappingOperation op) {
		try {
			op.run(new NullProgressMonitor());
		} catch (InvocationTargetException e) {
			fail("Unexpected exception: " + e.getTargetException().getMessage());
		} catch (InterruptedException e) {
			fail("Unexpected interupt");
		} catch (RuntimeException e) {
			if (e == PROMPT_EXCEPTION)
				return;
			throw e;
		}
		fail("Expected prompt did not occur");
	}
	
	private ResourceMapping getMapping(final IProject project, final IResource[] resources, final int depth) {
		return new ResourceMapping() {
		
			public ResourceTraversal[] getTraversals(ResourceMappingContext context,
					IProgressMonitor monitor) throws CoreException {
				return new ResourceTraversal[] { new ResourceTraversal(resources, depth, IResource.NONE)};
			}
		
			public IProject[] getProjects() {
				return new IProject[] { project };
			}
		
			public Object getModelObject() {
				return new Object();
			}
			
			public String getModelProviderId() {
				return TEST_MODEL_PROVIDER_ID;
			}
		    public boolean isAncestorOf(ResourceMapping mapping) {
		    	return false;
		    }
		
		};
	}
	
	public void testAdditionalResources() throws CoreException {
		IProject project = createProject(new String[]{"file.txt", "folder1/file2.txt", "folder1/folder2/file3.txt", "folder3/"});
		ResourceMapping[] mappings = new ResourceMapping[] { 
				getMapping(project, new IResource[] { project.getFolder("folder1") }, IResource.DEPTH_INFINITE)
		};
		ResourceMapping[] additionalMappings = new ResourceMapping[] { 
				getMapping(project, new IResource[] { project.getFile("file.txt")}, IResource.DEPTH_INFINITE)
		};
		TestResourceMappingOperation op = new TestResourceMappingOperation(mappings, additionalMappings);
		expectPrompt(op);
	}


}
