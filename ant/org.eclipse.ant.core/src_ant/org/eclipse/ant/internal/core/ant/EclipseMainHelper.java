/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

package org.eclipse.ant.internal.core.ant;

import org.apache.tools.ant.Project;

public class EclipseMainHelper {

	public void runProjectHelp(String buildFileLocation, Project eclipseAntProject) {
		EclipseAntMain.run(new String[] { "-f", buildFileLocation, "-p" }, eclipseAntProject); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void runUsage(String buildFileLocation, Project eclipseAntProject) {
		EclipseAntMain.run(new String[] { "-f", buildFileLocation, "-h" }, eclipseAntProject); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
