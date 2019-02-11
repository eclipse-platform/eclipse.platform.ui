/*******************************************************************************
 * Copyright (c) 2018, 2019 Remain Software and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.ide.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tips.core.ITipManager;
import org.eclipse.tips.core.internal.LogUtil;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * Internal class to source a new boolean variable in the IDE called "newtips".
 *
 */
@SuppressWarnings("restriction")
public class TipSourceProvider extends AbstractSourceProvider {
	private boolean fNewTips;
	private ITipManager fManager;

	public TipSourceProvider(ITipManager manager) {
		fManager = manager;
	}

	@Override
	public void dispose() {
	}

	@Override
	public Map<?, ?> getCurrentState() {
		Map<Object, Object> currentState = new HashMap<>();
		currentState.put(Constants.SOURCE_UNREAD_TIPS, Boolean.valueOf(fNewTips));
		return currentState;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { Constants.SOURCE_UNREAD_TIPS };
	}

	/**
	 * Propagate the new status of the <code>newtips</code> variable but always
	 * layouts all workbench windows to update the trim status.
	 *
	 * @param newTips true if there are new tips, false if there are no more new
	 *                tips.
	 */
	public synchronized void setStatus(boolean newTips) {
		boolean changed = fNewTips != newTips;
		if (changed) {
			fNewTips = newTips;
		}
		layoutWorkbench(changed);
	}

	private void layoutWorkbench(boolean changed) {
		UIJob job = new UIJob(PlatformUI.getWorkbench().getDisplay(), Messages.TipSourceProvider_0) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (changed) {
					fireSourceChanged(ISources.ACTIVE_WORKBENCH_WINDOW, getCurrentState());
				}
				for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
					fManager.log(LogUtil.info(Messages.TipSourceProvider_1 + window + " -> " + fNewTips)); //$NON-NLS-1$
					window.getShell().layout(true, true);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule(5000); // allow the workbench to settle in.
	}
}