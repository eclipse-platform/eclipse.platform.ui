/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import java.lang.reflect.Field;

import org.eclipse.core.internal.jobs.InternalJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

class TestJob extends Job {

	public TestJob(String name) {
		super(name);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		throw new UnsupportedOperationException("Not implemented, because of just a unit test");
	}

	public void setInternalJobState(int state) {
		try {
			final Field field = InternalJob.class.getDeclaredField("flags");
			field.setAccessible(true); // hack for testing
			field.set(this, Integer.valueOf(state));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}