/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 *******************************************************************************/
package org.eclipse.jface.databinding.conformance.util;

import org.eclipse.core.databinding.observable.Realm;

/**
 * Realm that will delegate to another for all operations except calls to
 * {@link #isCurrent()}. The current status can be set by the consumer to enable
 * testing of realm checks.
 *
 * @since 3.2
 */
public class DelegatingRealm extends CurrentRealm {
	private final Realm realm;

	public DelegatingRealm(Realm realm) {
		this.realm = realm;
	}

	@Override
	protected void syncExec(Runnable runnable) {
		realm.exec(runnable);
	}

	@Override
	public void asyncExec(Runnable runnable) {
		realm.asyncExec(runnable);
	}
}
