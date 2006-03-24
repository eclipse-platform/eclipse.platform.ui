/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.mapping;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

public class GroupProgressMonitor extends ProgressMonitorWrapper implements
		IProgressMonitor {

	private final IProgressMonitor group;
	private final int ticks;

	public GroupProgressMonitor(IProgressMonitor monitor, IProgressMonitor group, int groupTicks) {
		super(monitor);
		this.group = group;
		this.ticks = groupTicks;
	}

	public IProgressMonitor getGroup() {
		return group;
	}

	public int getTicks() {
		return ticks;
	}

}
