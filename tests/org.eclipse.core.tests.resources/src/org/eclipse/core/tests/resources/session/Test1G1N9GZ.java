/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.resources.session;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.internal.builders.SortBuilder;

/**
 * Regression test for 1G1N9GZ: ITPCORE:WIN2000 - ElementTree corruption when linking trees
 */
public class Test1G1N9GZ extends WorkspaceSerializationTest {
/**
 * Constructor for Test1G1N9GZ.
 */
public Test1G1N9GZ() {
	super();
}
/**
 * Constructor for Test1G1N9GZ.
 * @param name
 */
public Test1G1N9GZ(String name) {
	super(name);
}
/**
 * Initial setup and save
 */
public void test1() throws CoreException {
	/* create P1 and set a builder */
	IProject p1 = workspace.getRoot().getProject("p1");
	p1.create(null);
	p1.open(null);
	IProjectDescription desc = p1.getDescription();
	ICommand command = desc.newCommand();
	command.setBuilderName(SortBuilder.BUILDER_NAME);
	command.getArguments().put(SortBuilder.BUILD_ID, "P1Build1");
	desc.setBuildSpec(new ICommand[] {command});
	p1.setDescription(desc, getMonitor());

	/* create P2 and set a builder */
	IProject p2 = workspace.getRoot().getProject("p2");
	p2.create(null);
	p2.open(null);
	desc = p1.getDescription();
	command = desc.newCommand();
	command.setBuilderName(SortBuilder.BUILDER_NAME);
	command.getArguments().put(SortBuilder.BUILD_ID, "P2Build1");
	desc.setBuildSpec(new ICommand[] {command});
	p1.setDescription(desc, getMonitor());

	/* PR test case */
	workspace.save(true, getMonitor());
}
public void test2() throws CoreException {
	workspace.save(true, getMonitor());
}
public void test3() throws CoreException {	
	/* get new handles */
	IProject p1 = workspace.getRoot().getProject("p1");
	IProject p2 = workspace.getRoot().getProject("p2");

	/* try to create other files */
	try {
		ByteArrayInputStream source = new ByteArrayInputStream("file's content".getBytes());
		p1.getFile("file2").create(source, true, null);
	} catch (Exception e) {
		fail("1.0", e);
	}
	try {
		ByteArrayInputStream source = new ByteArrayInputStream("file's content".getBytes());
		p2.getFile("file2").create(source, true, null);
	} catch (Exception e) {
		fail("1.1", e);
	}
}
}