/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.core.internal.contexts;

import java.lang.ref.Reference;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;

abstract public class RunAndTrackExt extends RunAndTrack {

	final private boolean group;

	public RunAndTrackExt(boolean group) {
		super();
		this.group = group;
	}

	public boolean batchProcess() {
		return group;
	}

	public Reference<Object> getReference() {
		return null;
	}

	abstract public boolean update(IEclipseContext eventsContext, int eventType, Object[] extraArguments);
}
