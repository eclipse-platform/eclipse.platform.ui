/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private List<Runnable> queue = new LinkedList<Runnable>();

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
