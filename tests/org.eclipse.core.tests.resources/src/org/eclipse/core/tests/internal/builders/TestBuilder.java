/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import java.util.*;
import junit.framework.Assert;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.*;

/**
 * An abstract builder that is designed to be extended for testing purposes.
 */
public abstract class TestBuilder extends IncrementalProjectBuilder {
	/**
	 * Build command parameters.
	 */
	public static final String BUILD_ID = "BuildID";
	public static final String INTERESTING_PROJECT = "InterestingProject";
	/**
	 * Lifecycle event identifiers
	 */
	public static final String STARTUP_ON_INITIALIZE = "StartupOnInitialize";
	public static final String SET_INITIALIZATION_DATA = "SetInitializationData";
	public static final String DEFAULT_BUILD_ID = "Build0";
	/**
	 * The arguments for one run of the builder.
	 */
	protected Map arguments;
	private IConfigurationElement config = null;
	private String name = null;
	private Object data = null;
	/**
	 * These are static because we want one event set for all builder instances.
	 */
	private static final ArrayList expectedEvents = new ArrayList();
	private static final ArrayList actualEvents = new ArrayList();

	/**
	 * Logs the given plug-in lifecycle event for this builder's plugin.
	 */
	public void addExpectedLifecycleEvent(String event) {
		expectedEvents.add(event);
	}

	/**
	 * Verifies that the given lifecycle events occurred. Throws an assertion
	 * failure if expectations are not met. If successful, clears the list of
	 * expected and actual events in preparation for the next test.
	 */
	public void assertLifecycleEvents(String text) {
		Assert.assertEquals(text, expectedEvents, actualEvents);
		reset();
	}

	/**
	 * Implements the inherited abstract method in
	 * <code>InternalBuilder</code>.
	 * 
	 * @see InternalBuilder#build(IResourceDelta,int,IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		arguments = args == null ? new HashMap(1) : args;
		logPluginLifecycleEvent(getBuildId());
		return new IProject[0];
	}

	/**
	 * Returns an ID that identifies the current build.
	 */
	private String getBuildId() {
		String buildId = (String) arguments.get(BUILD_ID);
		if (buildId == null)
			buildId = DEFAULT_BUILD_ID;
		return buildId;
	}

	/**
	 * Return the configuration element that created this builder.
	 * 
	 * @see setInitializationData(IConfigurationElement, String, Object)
	 */
	public IConfigurationElement getConfigurationElement() {
		return config;
	}

	/**
	 * Return the data, always a <code>Hashtable</code> or <code>String</code>,
	 * that was set when this builder was initialized.
	 * 
	 * @see setInitializationData(IConfigurationElement, String, Object)
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Return the name of the child configuration element that named this
	 * builder in its class attribute.
	 * 
	 * @see setInitializationData(IConfigurationElement, String, Object)
	 */
	public String getName() {
		return name;
	}

	/**
	 * Logs the given plug-in lifecycle event for this builder's plugin.
	 */
	private void logPluginLifecycleEvent(String event) {
		actualEvents.add(event);
	}

	/**
	 * Resets expected and actual lifecycle events for this builder.
	 */
	public void reset() {
		expectedEvents.clear();
		actualEvents.clear();
	}

	/**
	 * Part of <code>IExecutableExtensionAdaptor</code> interface.
	 * 
	 * @see IExecutableExtensionAdaptor
	 * @see IConfigurationElement#createExecutableExtension(String)
	 */
	public void setInitializationData(IConfigurationElement config, String name, Object data) {
		logPluginLifecycleEvent(SET_INITIALIZATION_DATA);
		this.config = config;
		this.name = name;
		this.data = data;
	}

	/**
	 * Implemented inherited method from <code>BaseBuilder</code>.
	 * 
	 * @see BaseBuilder#startupOnInitialize
	 */
	protected void startupOnInitialize() {
		logPluginLifecycleEvent(STARTUP_ON_INITIALIZE);
	}
}
