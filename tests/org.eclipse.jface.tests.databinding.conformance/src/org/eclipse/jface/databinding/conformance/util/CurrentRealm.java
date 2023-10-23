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
 *     Matthew Hall - bug 268688
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 *******************************************************************************/
package org.eclipse.jface.databinding.conformance.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;

/**
 * Allows for the toggling of the current status of the realm. The
 * asyncExec(...) implementations do nothing.
 *
 * @since 3.2
 */
public class CurrentRealm extends Realm {
	private boolean current;
	private final List<Runnable> queue = new LinkedList<>();

	public CurrentRealm() {
		this(false);
	}

	public CurrentRealm(boolean current) {
		this.current = current;
	}

	@Override
	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
		processTasks();
	}

	@Override
	protected void syncExec(Runnable runnable) {
		super.syncExec(runnable);
	}

	private void processTasks() {
		if (isCurrent()) {
			for (Iterator<Runnable> it = queue.iterator(); it.hasNext();) {
				Runnable task = it.next();
				it.remove();
				safeRun(task);
			}
		}
	}

	@Override
	public void asyncExec(Runnable runnable) {
		queue.add(runnable);
	}

	protected static Realm setDefault(Realm realm) {
		return Realm.setDefault(realm);
	}
}
