/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
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
package org.eclipse.ant.internal.core;

import java.util.Map;

import org.eclipse.ant.core.AntCorePlugin;

public abstract class AbstractEclipseBuildLogger {

	/**
	 * Process identifier - used to link the Eclipse Ant build loggers to a process.
	 */
	public static final String ANT_PROCESS_ID = AntCorePlugin.PI_ANTCORE + ".ANT_PROCESS_ID"; //$NON-NLS-1$

	protected String fProcessId = null;

	public void configure(Map<String, String> userProperties) {
		fProcessId = userProperties.remove(ANT_PROCESS_ID);
	}
}