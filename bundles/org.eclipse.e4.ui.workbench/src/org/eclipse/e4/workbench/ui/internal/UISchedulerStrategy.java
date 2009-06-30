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
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
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

	public boolean schedule(final IEclipseContext context, final IRunAndTrack runnable,
			final String name, final int eventType, final Object[] args) {
		final boolean[] result = new boolean[1];
		Realm realm = Realm.getDefault();
		if (realm == null)
			return false;
		realm.asyncExec(new Runnable() {
			public void run() {
				result[0] = runnable.notify(EclipseContextFactory.createContextEvent(context,
						eventType, args, name, null));
			}
		});
		// TODO this return value is bogus because the runnable has not run yet.
		return result[0];
	}

	public void schedule(Runnable runnable) {
		Realm realm = Realm.getDefault();
		if (realm == null)
			return;
		realm.asyncExec(runnable);
	}
}
