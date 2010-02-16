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
package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.ISchedulerStrategy;

/**
 * A context scheduler strategy that uses the realm's async event queue for processing updates.
 */
public class UISchedulerStrategy implements ISchedulerStrategy {

	private static final ISchedulerStrategy instance = new UISchedulerStrategy();

	/**
	 * Returns the singleton UI scheduler instance
	 * 
	 * @return the UI scheduler instance
	 */
	public static ISchedulerStrategy getInstance() {
		return instance;
	}

	public boolean schedule(final IRunAndTrack runnable, final ContextChangeEvent event) {
		Realm realm = Realm.getDefault();
		if (realm == null)
			return runnable.notify(event);
		realm.asyncExec(new Runnable() {
			public void run() {
				runnable.notify(event);
			}
		});
		// since the event hasn't been broadcast yet we can't say the result
		return true;
	}

	public void schedule(Runnable runnable) {
		Realm realm = Realm.getDefault();
		if (realm == null)
			return;
		realm.asyncExec(runnable);
	}
}
