/*******************************************************************************
 * Copyright (c) 2019 Stefan Winkler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Winkler <stefan@winklerweb.net> - Initial contribution (bug 417255)
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.tests.navigator.util.TestWorkspace;

/**
 *
 * @since 3.3
 */
public class DecorationSchedulerRaceConditionTestDecorator implements ILightweightLabelDecorator {

	public static final QualifiedName DECO_PROP = new QualifiedName("org.eclipse.ui.tests.navigator", "decoProp");

	private static final Semaphore semaphoreDecoratorP1Ran = new Semaphore(0);
	private static final Semaphore semaphoreDecoratorP2Ran = new Semaphore(0);
	private static final Semaphore semaphoreBlockDecoration = new Semaphore(200);

	public static void resetWait() {
		semaphoreDecoratorP1Ran.drainPermits();
		semaphoreDecoratorP2Ran.drainPermits();
	}

	public static boolean hasP1Run(long timeout) throws InterruptedException {
		return semaphoreDecoratorP1Ran.tryAcquire(timeout, TimeUnit.MILLISECONDS);
	}

	public static boolean hasP2Run(long timeout) throws InterruptedException {
		return semaphoreDecoratorP2Ran.tryAcquire(timeout, TimeUnit.MILLISECONDS);
	}

	public static void blockDecoration() {
		semaphoreBlockDecoration.drainPermits();
	}

	public static void unblockDecorationOnce() {
		semaphoreBlockDecoration.release(1);
	}

	public static void unblockDecoration() {
		semaphoreBlockDecoration.release(200);
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		// decorate projects with the session property DECO_PROP's value
		if (!(element instanceof IProject)) {
			return;
		}

		IProject prj = (IProject) element;
		String prjName = prj.getName();
		Object suffix;
		try {
			suffix = prj.getSessionProperty(DECO_PROP);
		} catch (CoreException e) {
			e.printStackTrace();
			suffix = null;
		}

		if (suffix != null) {
			decoration.addSuffix((String) suffix);
		}

		// use semaphores to wait on specific events to force race condition
		if (prjName.equals(TestWorkspace.P1_PROJECT_NAME)) {
			try {
				semaphoreDecoratorP1Ran.release(); // signal that the decoration has run
				semaphoreBlockDecoration.acquire(); // if in blocking state, wait until unblocked
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else if (prjName.equals(TestWorkspace.P2_PROJECT_NAME)) {
			try {
				semaphoreDecoratorP2Ran.release(); // signal that the decoration has run
				semaphoreBlockDecoration.acquire(); // if in blocking state, wait until unblocked
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
