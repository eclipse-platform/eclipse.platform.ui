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
package org.eclipse.team.tests.ccvs.core;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.RLog;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.operations.RemoteLogOperation;

public class FindCommittersTest extends EclipseTest {

	/**
	 * Constructor for CVSProviderTest
	 */
	public FindCommittersTest() {
		super();
	}

	/**
	 * Constructor for CVSProviderTest
	 */
	public FindCommittersTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(FindCommittersTest.class);
	}
	
	public void testFetchLogs() throws CVSException, InvocationTargetException, InterruptedException {
		CVSTag tag1 = new CVSTag(new Date(101, 10, 07));
		CVSTag tag2 = new CVSTag(new Date(101, 11, 31));
		CVSRepositoryLocation location = CVSRepositoryLocation.fromString(":pserver:anonymous@dev.eclipse.org:/cvsroot/eclipse");
		ICVSRemoteResource[] members = location.members(tag2, false, DEFAULT_MONITOR);
		RemoteLogOperation.LogEntryCache logEntryCache = new RemoteLogOperation.LogEntryCache();
		RemoteLogOperation op = new RemoteLogOperation(null, members, tag1, tag2, logEntryCache) {
			protected LocalOption[] getLocalOptions(CVSTag tag1, CVSTag tag2) {
				return new Command.LocalOption[] {RLog.NO_TAGS, RLog.ONLY_INCLUDE_CHANGES, new LocalOption("-d" + tag1.asDate() + "<" + tag2.asDate().toString(), null) {
					
				}};
			}
		};
		op.run(DEFAULT_MONITOR);
		String[] paths = logEntryCache.getCachedFilePaths();
		Set authors = new HashSet();
		for (int i = 0; i < paths.length; i++) {
			String path = paths[i];
			ILogEntry[] entries = logEntryCache.getLogEntries(path);
			for (int j = 0; j < entries.length; j++) {
				ILogEntry entry = entries[j];
				authors.add(entry.getAuthor());
			}
		}
		for (Iterator iterator = authors.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			System.out.println(name);
		}
		
	}
	
}
