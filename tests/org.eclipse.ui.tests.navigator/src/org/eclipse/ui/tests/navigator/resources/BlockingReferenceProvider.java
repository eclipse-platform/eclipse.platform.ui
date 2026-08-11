/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IDynamicReferenceProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Dynamic reference provider that a test can hold inside a reference lookup, so
 * that the workspace can be changed while the project reference graph is being
 * built. It contributes no references and builds nothing.
 */
public class BlockingReferenceProvider extends IncrementalProjectBuilder implements IDynamicReferenceProvider {

	private static final String BUILDER_ID = "org.eclipse.ui.tests.navigator.blockingReferenceProvider"; //$NON-NLS-1$

	private record Gate(CountDownLatch entered, CountDownLatch released) {
	}

	private static volatile Gate gate;

	/**
	 * Adds this provider to the project, so that a reference lookup on it can be
	 * held. Reference lookups are cached, so a lookup only reaches the provider
	 * again after {@link IProject#clearCachedDynamicReferences()}.
	 */
	public static void addTo(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand command = description.newCommand();
		command.setBuilderName(BUILDER_ID);
		description.setBuildSpec(new ICommand[] { command });
		project.setDescription(description, null);
	}

	/** Makes the next reference lookup block until {@link #release()}. */
	public static void arm() {
		gate = new Gate(new CountDownLatch(1), new CountDownLatch(1));
	}

	public static void awaitBlocked() throws InterruptedException {
		gate.entered().await();
	}

	/** Lets a blocked lookup continue. Does nothing if none is blocked. */
	public static void release() {
		Gate blocked = gate;
		gate = null;
		if (blocked != null) {
			blocked.released().countDown();
		}
	}

	@Override
	public List<IProject> getDependentProjects(IBuildConfiguration buildConfiguration) {
		Gate blocked = gate;
		if (blocked != null) {
			blocked.entered().countDown();
			try {
				blocked.released().await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return List.of();
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) {
		return null;
	}
}
