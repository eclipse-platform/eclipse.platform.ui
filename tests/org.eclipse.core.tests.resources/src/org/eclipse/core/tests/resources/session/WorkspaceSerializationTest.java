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
import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.core.tests.harness.FussyProgressMonitor;
import org.eclipse.core.tests.harness.*;
import org.eclipse.core.tests.internal.builders.SortBuilder;
import org.eclipse.core.tests.internal.resources.*;

public class WorkspaceSerializationTest extends WorkspaceSessionTest {
	protected static final String PROJECT = "CrashProject";
	protected static final String FOLDER = "CrashFolder";
	protected static final String FILE = "CrashFile";
	protected IWorkspace workspace;

/**
 * Creates a new WorkspaceSerializationTest.
 */
public WorkspaceSerializationTest() {
	super("");
}
/**
 * Creates a new WorkspaceSerializationTest.
 * @param name the name of the test method to run
 */
public WorkspaceSerializationTest(String name) {
	super(name);
}
protected void setUp() throws Exception {
	workspace = getWorkspace();
}
}

