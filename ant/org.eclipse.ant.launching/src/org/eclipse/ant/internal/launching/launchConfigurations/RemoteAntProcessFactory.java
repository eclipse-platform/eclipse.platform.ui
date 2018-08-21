/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

package org.eclipse.ant.internal.launching.launchConfigurations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.launching.IAntLaunchConstants;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;

public class RemoteAntProcessFactory implements IProcessFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IProcessFactory#newProcess(org.eclipse.debug.core.ILaunch, java.lang.Process, java.lang.String, java.util.Map)
	 */
	@Override
	public IProcess newProcess(ILaunch launch, Process process, String label, Map<String, String> attributes) {
		if (attributes == null) {
			attributes = new HashMap<>(1);
		}
		attributes.put(IProcess.ATTR_PROCESS_TYPE, IAntLaunchConstants.ID_ANT_PROCESS_TYPE);
		return new RemoteAntRuntimeProcess(launch, process, label, attributes);
	}
}
