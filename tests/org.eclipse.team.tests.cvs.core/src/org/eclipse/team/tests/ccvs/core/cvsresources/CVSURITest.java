/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core.cvsresources;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.filesystem.CVSURI;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class CVSURITest extends EclipseTest {

	public CVSURITest() {
		super();
	}

	public CVSURITest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CVSURITest.class);
		return new CVSTestSetup(suite);
	}
	
	public void testURIParse() throws URISyntaxException, CVSException {
		URI uri = new URI("cvs://:pserver:user@host.here:!root!path/project/path");
		CVSURI cvsUri = CVSURI.fromUri(uri);
		assertEquals("/project/path", cvsUri.getPath().toString());
		CVSRepositoryLocation location = CVSRepositoryLocation.fromString(":pserver:user@host.here:/root/path");
		assertEquals(cvsUri.getRepository().getLocation(false), location.getLocation(false));
		assertEquals(cvsUri.getTag(), null);
		assertEquals(cvsUri.toURI(), uri);
	}
	
	public void testURIParse2() throws URISyntaxException, CVSException {
		URI uri = new URI("cvs://:pserver:user:password@host.here:port!root!path/project/path");
		CVSURI cvsUri = CVSURI.fromUri(uri);
		assertEquals("/project/path", cvsUri.getPath().toString());
		CVSRepositoryLocation location = CVSRepositoryLocation.fromString(":pserver:user:password@host.here:port/root/path");
		assertEquals(cvsUri.getRepository().getLocation(false), location.getLocation(false));
		assertEquals(cvsUri.getTag(), null);
		assertEquals(cvsUri.toURI(), uri);
	}
	
	public void testURIParse3() throws URISyntaxException, CVSException {
		URI uri = new URI("cvs://:pserver:user:password@host.here:port!root!path/project/path?version=v1");
		CVSURI cvsUri = CVSURI.fromUri(uri);
		assertEquals("/project/path", cvsUri.getPath().toString());
		CVSRepositoryLocation location = CVSRepositoryLocation.fromString(":pserver:user:password@host.here:port/root/path");
		assertEquals(cvsUri.getRepository().getLocation(false), location.getLocation(false));
		assertEquals(cvsUri.getTag(), new CVSTag("v1", CVSTag.VERSION));
		assertEquals(cvsUri.toURI(), uri);
	}
	
	public void testURIParse4() throws URISyntaxException, CVSException {
		URI uri = new URI("cvs://:pserver:user:password@host.here:port!root!path/project/path?branch=b1");
		CVSURI cvsUri = CVSURI.fromUri(uri);
		assertEquals("/project/path", cvsUri.getPath().toString());
		CVSRepositoryLocation location = CVSRepositoryLocation.fromString(":pserver:user:password@host.here:port/root/path");
		assertEquals(cvsUri.getRepository().getLocation(false), location.getLocation(false));
		assertEquals(cvsUri.getTag(), new CVSTag("b1", CVSTag.BRANCH));
		assertEquals(cvsUri.toURI(), uri);
	}
	
	public void testURIParse5() throws URISyntaxException, CVSException {
		URI uri = new URI("cvs://:pserver:user:password@host.here:port!root!path/project/path?revision=1.5");
		CVSURI cvsUri = CVSURI.fromUri(uri);
		assertEquals("/project/path", cvsUri.getPath().toString());
		CVSRepositoryLocation location = CVSRepositoryLocation.fromString(":pserver:user:password@host.here:port/root/path");
		assertEquals(cvsUri.getRepository().getLocation(false), location.getLocation(false));
		assertEquals(cvsUri.getRevision(), "1.5");
		assertEquals(cvsUri.toURI(), uri);
	}
	
}
