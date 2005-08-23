/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.util.Util;

/**
 * Provides notifications when the active part changes.
 * 
 * @since 3.1
 */
public class ActivePartSourceProvider extends AbstractSourceProvider {

	/**
	 * The last active part id seen as active by this provider. This value may
	 * be <code>null</code> if there is no currently active part.
	 */
	private String lastActivePartId = null;

	/**
	 * The last active part site seen by this provider. This value may be
	 * <code>null</code> if there is no currently active site.
	 */
	private IWorkbenchPartSite lastActivePartSite = null;

	private final IPartListener partListener = new IPartListener() {

		public final void partActivated(final IWorkbenchPart part) {
			checkActivePart();
		}

		public final void partBroughtToTop(final IWorkbenchPart part) {
			checkActivePart();
		}

		public final void partClosed(final IWorkbenchPart part) {
			checkActivePart();
		}

		public final void partDeactivated(final IWorkbenchPart part) {
			checkActivePart();
		}

		public final void partOpened(final IWorkbenchPart part) {
			checkActivePart();
		}

	};

	private final IWindowListener windowListener = new IWindowListener() {

		public final void windowActivated(final IWorkbenchWindow window) {
			checkActivePart();
		}

		public final void windowClosed(final IWorkbenchWindow window) {
			if (window != null) {
				window.getPartService().removePartListener(partListener);
			}
			checkActivePart();
		}

		public final void windowDeactivated(final IWorkbenchWindow window) {
			checkActivePart();
		}

		public final void windowOpened(final IWorkbenchWindow window) {
			if (window != null) {
				window.getPartService().addPartListener(partListener);
			}
			checkActivePart();
		}

	};

	/**
	 * The workbench on which this source provider will act.
	 */
	private final IWorkbench workbench;

	/**
	 * Constructs a new instance of <code>ShellSourceProvider</code>.
	 * 
	 * @param workbench
	 *            The workbench on which to monitor shell activations; must not
	 *            be <code>null</code>.
	 */
	public ActivePartSourceProvider(final IWorkbench workbench) {
		this.workbench = workbench;
		workbench.addWindowListener(windowListener);
	}

	private final void checkActivePart() {
		final Map currentState = getCurrentState();
		int sources = 0;
		final Object newActivePartId = currentState
				.get(ISources.ACTIVE_PART_NAME);
		if (!Util.equals(newActivePartId, lastActivePartId)) {
			sources |= ISources.ACTIVE_PART;
			lastActivePartId = (String) newActivePartId;
		}
		final Object newActivePartSite = currentState
				.get(ISources.ACTIVE_SITE_NAME);
		if (!Util.equals(newActivePartSite, lastActivePartSite)) {
			sources |= ISources.ACTIVE_SITE;
			lastActivePartSite = (IWorkbenchPartSite) newActivePartSite;
		}

		if (sources != 0) {
			fireSourceChanged(sources, currentState);
		}
	}

	public final void dispose() {
		workbench.removeWindowListener(windowListener);
	}

	public final Map getCurrentState() {
		final Map currentState = new HashMap(4);
		currentState.put(ISources.ACTIVE_SITE_NAME, null);
		currentState.put(ISources.ACTIVE_PART_NAME, null);

		final IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			final IWorkbenchPage activeWorkbenchPage = activeWorkbenchWindow
					.getActivePage();
			if (activeWorkbenchPage != null) {
				final IWorkbenchPart activeWorkbenchPart = activeWorkbenchPage
						.getActivePart();
				if (activeWorkbenchPart != null) {
					final IWorkbenchPartSite activeWorkbenchPartSite = activeWorkbenchPart
							.getSite();
					currentState.put(ISources.ACTIVE_SITE_NAME,
							activeWorkbenchPartSite);
					if (activeWorkbenchPartSite != null) {
						final String newActivePartId = activeWorkbenchPartSite
								.getId();
						currentState.put(ISources.ACTIVE_PART_NAME,
								newActivePartId);
					}
				}
			}
		}

		return currentState;
	}

}
