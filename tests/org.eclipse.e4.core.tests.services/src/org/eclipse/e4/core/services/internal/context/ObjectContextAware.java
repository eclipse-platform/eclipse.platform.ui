/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;

/**
 * Test class to check injection mechanism
 */
public class ObjectContextAware implements IDisposable {
	protected IEclipseContext equinoxContext;

	private boolean finalized;
	private boolean disposed;

	public ObjectContextAware() {
		finalized = false;
		disposed = false;
	}

	@Inject
	public void contextSet(IEclipseContext context) {
		equinoxContext = context;
		finalized = true;
	}

	public void dispose() {
		equinoxContext = null;
		disposed = true;
	}

	public IEclipseContext getEquinoxContext() {
		return equinoxContext;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public boolean isDisposed() {
		return disposed;
	}

}
