/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui.benchmark;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource;

/**
 * Benchmark test superclass
 */
public abstract class BenchmarkTest extends EclipseTest {

	protected BenchmarkTest() {
	}

	protected BenchmarkTest(String name) {
		super(name);
	}

	protected IProject createUniqueProject(File zipFile) throws TeamException, CoreException, ZipException, IOException, InterruptedException, InvocationTargetException {
		return createAndImportProject(getName(), zipFile);
	}
	
	protected IProject createAndImportProject(String prefix, File zipFile) throws TeamException, CoreException, ZipException, IOException, InterruptedException, InvocationTargetException {
		// create a project with no contents
		IProject project = getUniqueTestProject(prefix);
		Util.importZip(project, zipFile);
		return project;
	}
	
    /**
     * @param string
     */
    protected void startTask(String string) {
        // TODO Auto-generated method stub
        
    }
    
	/**
     * 
     */
	protected void endTask() {
        // TODO Auto-generated method stub
        
    }
	
    /**
     * @param string
     */
    protected void startGroup(String string) {
        // TODO Auto-generated method stub
        
    }
    
	/**
     * 
     */
	protected void endGroup() {
        // TODO Auto-generated method stub
        
    }
	
	protected void disableLog() {
	    // TODO:
	}
	
	protected void enableLog() {
	    // TODO:
	}
	
	protected void syncResources(Subscriber subscriber, IResource[] resources) throws TeamException {
	    startTask("Synchronize with Repository action");
	    getSyncInfoSource().refresh(subscriber, resources);
	    endTask();
	}

    /**
     * @param resources
     * @param string
     * @throws CoreException
     * @throws TeamException
     */
    protected void syncCommitResources(IResource[] resources, String comment) throws TeamException, CoreException {
       syncResources(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(), resources);
       startTask("Sync View Commit action");
       // TODO: Commit all outgoing changes that are children of the given resource
       // by extracting them from the subscriber sync set
       commitResources(resources, IResource.DEPTH_INFINITE);
       endTask();
    }
    
    /**
     * @param resources
     * @throws TeamException
     */
    protected void syncUpdateResources(IResource[] resources) throws TeamException {
        syncResources(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(), resources);
        // TODO: Update all incoming changes that are children of the given resource
        // by extracting them from the subscriber sync set
        updateResources(resources, IResource.DEPTH_INFINITE, false);
    }

    /**
     * @return
     */
    private SyncInfoSource getSyncInfoSource() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * @param resources
     * @param depth_infinite
     * @param b
     */
    private void updateResources(IResource[] resources, int depth_infinite, boolean ignoreLocalChanges) {
        // TODO Auto-generated method stub
        
    }

}
