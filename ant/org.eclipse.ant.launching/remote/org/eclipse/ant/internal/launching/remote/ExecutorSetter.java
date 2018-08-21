/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
package org.eclipse.ant.internal.launching.remote;

import org.apache.tools.ant.Executor;
import org.apache.tools.ant.Project;

/**
 * This class exists so that the Ant integration has backwards compatibility with Ant releases previous to 1.6.3. Executors are a new feature of Ant
 * 1.6.3.
 */
public class ExecutorSetter {

	protected void setExecutor(Project project) {
		Executor executor = new EclipseDefaultExecutor();
		project.setExecutor(executor);
	}
}
