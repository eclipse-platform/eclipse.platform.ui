/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import junit.framework.Assert;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Helper builder for cycle related tests.
 */
public class CycleBuilder extends TestBuilder {
	public static final String BUILDER_NAME = "org.eclipse.core.tests.resources.cyclebuilder";

	protected IProject[] beforeProjects = new IProject[0];
	protected IProject[] afterProjects = new IProject[0];
	protected int rebuildsToRequest = 0;
	private static CycleBuilder singleton;
	protected int buildCount = 0;

	/**
	 * Returns the most recently created instance.
	 */
	public static CycleBuilder getInstance() {
		return singleton;
	}

	public CycleBuilder() {
		singleton = this;
	}

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (beforeProjects != null) {
			for (int i = 0; i < beforeProjects.length; i++) {
				Assert.assertTrue("Missing before project: " + beforeProjects[i], hasBeenBuilt(beforeProjects[i]));
			}
		}
		if (afterProjects != null) {
			for (int i = 0; i < afterProjects.length; i++) {
				Assert.assertTrue("Missing after project: " + afterProjects[i], !hasBeenBuilt(afterProjects[i]));
			}
		}
		if (rebuildsToRequest > buildCount) {
			changeAllFiles();
			needRebuild();
		}
		//ensure that subsequent builds are always incremental
		if (buildCount > 0)
			Assert.assertTrue("Should be incremental build", kind == IncrementalProjectBuilder.INCREMENTAL_BUILD);
		buildCount++;
		return null;
	}

	/**
	 * Method getRandomContents.
	 * @return InputStream
	 */
	protected InputStream getRandomContents() {
		return new ByteArrayInputStream("foo".getBytes());
	}

	/**
	 * Method changeSomething.
	 */
	private void changeAllFiles() throws CoreException {
		IResourceVisitor visitor = new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (resource.getType() == IResource.FILE) {
					if (resource.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME))
						return false;
					((IFile) resource).setContents(getRandomContents(), IResource.NONE, null);
				}
				return true;
			}
		};
		getProject().accept(visitor, IResource.DEPTH_INFINITE, IResource.NONE);
	}

	public int getBuildCount() {
		return buildCount;
	}

	public void resetBuildCount() {
		buildCount = 0;
	}

	public void setAfterProjects(IProject[] afterProjects) {
		this.afterProjects = afterProjects;
	}

	public void setBeforeProjects(IProject[] beforeProjects) {
		this.beforeProjects = beforeProjects;
	}

	public void setRebuildsToRequest(int rebuildsToRequest) {
		this.rebuildsToRequest = rebuildsToRequest;
	}
}
