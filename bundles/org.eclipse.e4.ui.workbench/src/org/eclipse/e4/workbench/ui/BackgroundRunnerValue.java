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

package org.eclipse.e4.workbench.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.IBackgroundRunner;
import org.eclipse.e4.core.services.IRunnableWithProgress;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IComputedValue;

public class BackgroundRunnerValue implements IComputedValue {

	public Object compute(IEclipseContext context, Object[] arguments) {
		return new IBackgroundRunner() {
			public void schedule(long delay, String name,
					final IRunnableWithProgress runnable) {
				new Job(name) {
					protected IStatus run(IProgressMonitor monitor) {
						return runnable.run(monitor);
					}
				}.schedule(delay);
			}
		};
	}

}
