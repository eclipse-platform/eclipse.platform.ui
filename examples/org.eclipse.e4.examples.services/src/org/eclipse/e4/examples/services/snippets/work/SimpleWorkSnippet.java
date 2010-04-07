/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.examples.services.snippets.work;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.core.services.work.WorkContext;
import org.eclipse.e4.core.services.work.WorkRunnable;
import org.eclipse.e4.core.services.work.WorkScheduler;

public class SimpleWorkSnippet {
	@Inject
	Provider<StatusReporter> statusReporter;
	@Inject
	WorkScheduler scheduler;

	/**
	 * pretend result type
	 */
	static class Result {
	};

	/**
	 * pretend exception type
	 */
	static class FooException extends Exception {
	};

	public void example() {
		WorkRunnable<Result> runnable = new WorkRunnable<Result>() {
			@Override
			public Result run(WorkContext workContext) {
				workContext.setWorkRemaining(100, "working on it");
				try {
					doFirstHalfFoo(workContext.newChild(50));
					doSecondHalfFoo(workContext.newChild(50));
				} catch (FooException e) {
					statusReporter.get().show(IStatus.ERROR, "foo failure message", e);
				}
				// no need to call done, the caller knows that we're done
				return new Result();
			}
		};
		scheduler.schedule(runnable, 0, 0);
	}

	protected void doFirstHalfFoo(WorkContext wc) {
	}

	protected void doSecondHalfFoo(WorkContext wc) throws FooException {
		throw new FooException();
	}

}
