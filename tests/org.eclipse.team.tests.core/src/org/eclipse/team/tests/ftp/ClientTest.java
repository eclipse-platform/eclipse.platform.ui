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
package org.eclipse.team.tests.ftp;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.internal.ftp.FTPException;
import org.eclipse.team.internal.ftp.client.FTPClient;
import org.eclipse.team.tests.core.TeamTest;

public class ClientTest extends TeamTest {

	private static final IProgressMonitor DEFAULT_PROGRESS_MONITOR = new NullProgressMonitor();
	/**
	 * Constructor for ClientTest.
	 * @param name
	 */
	public ClientTest(String name) {
		super(name);
	}
	public ClientTest() {
		super();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ClientTest.class);
		return new FTPTestSetup(suite);
		//return new FTPTestSetup(new ClientTest("testName"));
	}
	
	public URL getURL() {
		return FTPTestSetup.ftpURL;
	}
	
	public FTPClient openFTPConnection() throws FTPException {
		return FTPTestSetup.openFTPConnection(getURL());
	}
	
	public void testCreateDirectory() throws FTPException {
		FTPClient client = openFTPConnection();
		try {
			client.createDirectory("testCreateDirectory", DEFAULT_PROGRESS_MONITOR);
			client.deleteDirectory("testCreateDirectory", DEFAULT_PROGRESS_MONITOR);
		} finally {
			client.close(DEFAULT_PROGRESS_MONITOR);
		}
	}
	
	public void testSimpleTransfer() throws FTPException {
		FTPClient client = openFTPConnection();
		try {
			client.createDirectory("testCreateDirectory", DEFAULT_PROGRESS_MONITOR);
			client.deleteDirectory("testCreateDirectory", DEFAULT_PROGRESS_MONITOR);
		} finally {
			client.close(DEFAULT_PROGRESS_MONITOR);
		}
	}
}
