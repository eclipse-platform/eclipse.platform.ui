/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class TestFailExtension implements TestWatcher {

	private static final String BUNDLE_ID= "org.eclipse.core.filebuffers.tests";

	ILog log= ILog.of(Platform.getBundle(BUNDLE_ID));

	@Override
	public void testFailed(ExtensionContext context, Throwable e) {
		IStatus status= new Status(IStatus.ERROR, BUNDLE_ID, "FAIL in " + context.getDisplayName(), e);
		log.log(status);
	}

	@Override
	public void testSuccessful(ExtensionContext context) {
		IStatus status= new Status(IStatus.INFO, BUNDLE_ID, "PASS in " + context.getDisplayName());
		log.log(status);
	}
}
