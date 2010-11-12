/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Broadcom Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Concrete implementation of a build configuration.
 *<p>
 * This class can both be used as a real build configuration in a project.
 * As well as the reference to a build configuration in another project.
 *</p>
 *<p>
 * When being used as a reference, core.resources <strong>must</strong> call
 * {@link #getBuildConfiguration()} to dereference the build configuration to the
 * the actual build configuration on the referenced project.
 *</p>
 */
public class BuildConfiguration implements IBuildConfiguration {

	/** Project on which this build configuration is set */
	private final IProject project;
	/** Configuration id unique in the project */
	private final String id;

	public BuildConfiguration(String id) {
		this(null, id, null);
	}

	public BuildConfiguration(IBuildConfiguration config, IProject project) {
		this(project, config.getId());
	}

	public BuildConfiguration(IProject project, String configurationId) {
		this(project, configurationId, null);
	}

	public BuildConfiguration(IProject project, String configurationId, String name) {
		this.project = project;
		this.id = configurationId;
	}

	/**
	 * @return the concrete build configuration referred to by this IBuildConfiguration
	 *         when it's being used as a reference
	 */
	public IBuildConfiguration getBuildConfiguration() throws CoreException {
		return project.getBuildConfiguration(id);
	}

	/*
	 * (non-Javadoc)
	 * @see IBuildConfiguration#getConfigurationId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see IBuildConfiguration#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * Helper method used to work out if the project's build configurations
	 * need to be persisted in the .project.
	 * If the user isn't using build configurations then no need to clutter the project XML.
	 * @return boolean indicating if this configuration is a default auto-generated one.
	 */
	public boolean isDefault() {
		if (!DEFAULT_CONFIG_ID.equals(id))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BuildConfiguration other = (BuildConfiguration) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		if (project != null)
			result.append(project.getName());
		else
			result.append("?"); //$NON-NLS-1$
		result.append(";"); //$NON-NLS-1$
		if (id != null)
			result.append(" [").append(id).append(']'); //$NON-NLS-1$
		else
			result.append(" [active]"); //$NON-NLS-1$
		return result.toString();
	}

}
