/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.conformance.util;

import org.eclipse.core.databinding.observable.Realm;

/**
 * Allows for the toggling of the current status of the realm. The
 * asyncExec(...) implementations do nothing.
 * 
 * @since 3.2
 */
public class CurrentRealm extends Realm {
	private boolean current;

	public CurrentRealm() {
		this(false);
	}

	public CurrentRealm(boolean current) {
		this.current = current;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	protected void syncExec(Runnable runnable) {
		super.syncExec(runnable);
	}

	public void asyncExec(Runnable runnable) {
		throw new UnsupportedOperationException(
				"CurrentRealm does not support asyncExec(Runnable)."); //$NON-NLS-1$
	}

	protected static Realm setDefault(Realm realm) {
		return Realm.setDefault(realm);
	}
}
