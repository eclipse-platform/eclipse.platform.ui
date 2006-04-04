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

import java.util.ArrayList;
import java.util.Map;
import junit.framework.Assert;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.resources.ResourceDeltaVerifier;

/**
 * This classes poses as a builder, and makes sure that the delta supplied to
 * the builder is as expected. Most of the work is forwarded to a
 * ResourceDeltaVerifier.
 */
public class DeltaVerifierBuilder extends TestBuilder {
	public static final String BUILDER_NAME = "org.eclipse.core.tests.resources.deltaverifierbuilder";
	/**
	 * The singleton builder instance
	 */
	protected static DeltaVerifierBuilder fgSingleton;
	/**
	 * The resource delta verifier that asserts the delta structure. Sharing it
	 * between builders means we can do things like shutdown the project,
	 * re-open, build, and assert the delta is appropriate.
	 */
	protected static final ResourceDeltaVerifier verifier = new ResourceDeltaVerifier();
	/** The projects to check deltas for (may be null) */
	protected IProject[] checkDeltas;
	/**
	 * Whether the last incremental build was empty
	 */
	protected boolean deltaWasEmpty = false;
	/** The empty deltas that were received */
	protected ArrayList emptyDeltas = new ArrayList();
	/** The deltas that were actually received */
	protected ArrayList receivedDeltas = new ArrayList();
	/** The projects to request deltas for (may be null) */
	protected IProject[] requestedDeltas;
	/**
	 * Whether the last build was full or batch
	 */
	protected int triggerForLastBuild = 0;

	/**
	 * Returns the singleton instance
	 */
	public static DeltaVerifierBuilder getInstance() {
		if (fgSingleton == null) {
			new DeltaVerifierBuilder();
		}
		return fgSingleton;
	}

	/**
	 * Captures the builder instantiated through reflection
	 */
	public DeltaVerifierBuilder() {
		if (fgSingleton != null) {
			//copy interesting data from old singleton
			this.triggerForLastBuild = fgSingleton.triggerForLastBuild;
			this.deltaWasEmpty = fgSingleton.deltaWasEmpty;
			this.requestedDeltas = fgSingleton.requestedDeltas;
			this.checkDeltas = fgSingleton.checkDeltas;
			this.receivedDeltas = fgSingleton.receivedDeltas;
			this.emptyDeltas = fgSingleton.emptyDeltas;
		}
		fgSingleton = this;
	}

	/**
	 * Signals to the comparer that the given resource is expected to change in
	 * the specified way. The change flags should be set to zero if no change is
	 * expected.
	 * 
	 * @param resource
	 *                  the resource that is expected to change
	 * @param topLevelParent
	 *                  Do not added expected changes above this parent
	 * @param status
	 *                  the type of change (ADDED, REMOVED, CHANGED)
	 * @param changeFlags
	 *                  the type of change (CONTENT, SYNC, etc)
	 * @param movedPath
	 *                  or null
	 * @see IResourceConstants
	 */
	public void addExpectedChange(IResource resource, IResource topLevelParent, int status, int changeFlags) {
		verifier.addExpectedChange(resource, topLevelParent, status, changeFlags, null, null);
	}

	/**
	 * Signals to the comparer that the given resource is expected to change in
	 * the specified way. The change flags should be set to zero if no change is
	 * expected.
	 * 
	 * @param resource
	 *                  the resource that is expected to change
	 * @param topLevelParent
	 *                  Do not added expected changes above this parent
	 * @param status
	 *                  the type of change (ADDED, REMOVED, CHANGED)
	 * @param changeFlags
	 *                  the type of change (CONTENT, SYNC, etc)
	 * @param movedPath
	 *                  or null
	 * @see IResourceConstants
	 */
	public void addExpectedChange(IResource resource, IResource topLevelParent, int status, int changeFlags, IPath movedFromPath, IPath movedToPath) {
		verifier.addExpectedChange(resource, topLevelParent, status, changeFlags, movedFromPath, movedToPath);
	}

	/**
	 * Like a wiley restaurant critic, this method masquerades as a builder, but
	 * is actually verifying that the provided delta is correct.
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		super.build(kind, args, monitor);
		triggerForLastBuild = kind;
		doCheckDeltas();
		IResourceDelta delta = getDelta(getProject());
		deltaWasEmpty = delta == null || delta.getKind() == 0;
		if (deltaWasEmpty) {
			//full build or empty delta
			verifier.reset();
		} else {
			verifier.verifyDelta(delta);
		}
		return getRequestedDeltas();
	}

	/**
	 * Indicates which projects to check receipt of deltas for.
	 */
	public void checkDeltas(IProject[] projects) {
		checkDeltas = projects;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		triggerForLastBuild = CLEAN_BUILD;
	}

	/**
	 * Asks the platform for the deltas for a set of projects, and notes which
	 * deltas were actually returned.
	 */
	protected void doCheckDeltas() {
		if (checkDeltas == null)
			return;
		receivedDeltas.clear();
		for (int i = 0; i < checkDeltas.length; i++) {
			IResourceDelta delta = getDelta(checkDeltas[i]);
			if (delta != null) {
				receivedDeltas.add(checkDeltas[i]);
				//check if the delta was empty
				if (delta.getKind() == IResourceDelta.NO_CHANGE && delta.getAffectedChildren().length == 0) {
					emptyDeltas.add(checkDeltas[i]);
				}
				//regression test -- ensure delta resource is non null
				Assert.assertTrue("Non-null delta", delta.getResource() != null);
				Assert.assertTrue("Delta rooted at project", delta.getResource().getType() == IResource.PROJECT);
			}
		}
	}

	/**
	 * Signals that an empty build has occurred, so the build method hasn't been
	 * called but the state should still be considered valid.
	 */
	public void emptyBuild() throws CoreException {
		build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null, null);
	}

	/**
	 * Returns the empty deltas received during the last build.
	 */
	public ArrayList getEmptyDeltas() {
		return emptyDeltas;
	}

	/**
	 * Returns a message that describes the result of the resource delta
	 * verification checks.
	 */
	public String getMessage() {
		String msg;
		if (deltaWasEmpty) {
			if (verifier.hasExpectedChanges()) {
				msg = "Had expected changes but delta was empty";
			} else {
				msg = "No Delta";
			}
		} else {
			msg = verifier.getMessage();
		}
		return msg;
	}

	/**
	 * Returns the projects for the deltas that were actually received during
	 * the last build.
	 */
	public ArrayList getReceivedDeltas() {
		return receivedDeltas;
	}

	/**
	 * Returns the projects to request deltas for next build.
	 */
	protected IProject[] getRequestedDeltas() {
		return requestedDeltas == null ? new IProject[0] : requestedDeltas;
	}

	/**
	 * Returns whether the resource delta passed all verification checks. An
	 * empty delta is valid if no changes were expected.
	 */
	public boolean isDeltaValid() {
		return (deltaWasEmpty && !verifier.hasExpectedChanges()) || verifier.isDeltaValid();
	}

	/**
	 * Indicates that the builder should request deltas for the given projects.
	 */
	public void requestDeltas(IProject[] projects) {
		requestedDeltas = projects;
		receivedDeltas.clear();
		emptyDeltas.clear();
	}

	/*
	 * @see TestBuilder#reset()
	 */
	public void reset() {
		super.reset();
		triggerForLastBuild = 0;
		if (verifier != null)
			verifier.reset();
	}

	public boolean wasAutoBuild() {
		return triggerForLastBuild == IncrementalProjectBuilder.AUTO_BUILD;
	}

	/**
	 * Returns true if the builder has been invoked since the last time it was
	 * reset, and false otherwise.
	 */
	public boolean wasBuilt() {
		return triggerForLastBuild != 0;
	}

	public boolean wasCleanBuild() {
		return triggerForLastBuild == IncrementalProjectBuilder.CLEAN_BUILD;
	}

	public boolean wasFullBuild() {
		return triggerForLastBuild == IncrementalProjectBuilder.FULL_BUILD;
	}

	public boolean wasIncrementalBuild() {
		return triggerForLastBuild == IncrementalProjectBuilder.INCREMENTAL_BUILD;
	}
}
