package org.eclipse.team.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Properties;

import org.eclipse.core.resources.IProjectNature;

/**
 * An interface that provides a team centric nature for providers. Each
 * provider must have a class that implements this interface and provide
 * an nature's extension point with this class as the <code>run</code> 
 * parameter.
 * <p>
 * A runtime instance of this class will be created for each project that
 * is associated with a provider. The implementing class must take
 * advantage of the lifecycle of natures and conform to the operation
 * specification in <code>IProjectNature</code> interface.</p>
 * <p>
 * When a project nature is re-created at runtime (e.g. the workbench
 * was shutdown and re-started) the <code>setProject()</code>
 * method will be called and the team nature must re-configure itself
 * by reading any saved meta information.</p>
 * 
 * @see IProjectNature
 * @see ITeamManager
 * @see ITeamProvider
 */
public interface ITeamNature extends IProjectNature {

	/** 
	 * Returns a team provider for the given project.
	 * <p>
	 * The returned provider can be used immediately to perform team
	 * operations.
	 * </p>
	 * 
	 * @return the <code>ITeamProvider</code> to which this project
	 * nature applies.
	 * 
	 * @throws TeamException if the provider cannot be found.
	 */
	public ITeamProvider getProvider() throws TeamException;

	/** 
	 * Configures this project nature given some provider specific configuration 
	 * information specified as properties.
	 * 
	 * @param configuration the properties used to configure the project.
	 * 
	 * @throws TeamException if the provider configuraton fails. Also, if the provider
	 * does not support creating providers programmatically it should throw an exception.
	 * 
	 * @see ITeamManager#setProvider(IProject, String, Properties, IProgressMonitor)
	 */
	public void configureProvider(Properties configuration) throws TeamException;
}