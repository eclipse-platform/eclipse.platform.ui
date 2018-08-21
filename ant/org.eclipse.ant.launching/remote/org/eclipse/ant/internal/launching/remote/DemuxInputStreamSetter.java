/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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

import org.apache.tools.ant.DemuxInputStream;
import org.apache.tools.ant.Project;

/**
 * This class exists so that the Ant integration has backwards compatibility with Ant releases previous to 1.6. DemuxInputStream is a new class to Ant
 * 1.6.
 */
class DemuxInputStreamSetter {

	protected void remapSystemIn(Project project) {
		System.setIn(new DemuxInputStream(project));
	}
}
