package org.eclipse.core.tests.internal.resources;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 */
public class TestNature implements IProjectNature {
/**
 * Constructor for TestNature.
 */
public TestNature() {
	super();
}
/**
 * @see IProjectNature#configure()
 */
public void configure() throws CoreException {
}
/**
 * @see IProjectNature#deconfigure()
 */
public void deconfigure() throws CoreException {
}
/**
 * @see IProjectNature#getProject()
 */
public IProject getProject() {
	return null;
}
/**
 * @see IProjectNature#setProject(IProject)
 */
public void setProject(IProject project) {
}
}
