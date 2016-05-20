/*******************************************************************************
 * Copyright (c) 2016 Lars Vogel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *     Axel Richard <axel.richard@obeo.fr> - Bug 486644, Bug 492438
 *     Mikael Barbero <mikael@eclipse.org> - Bug 486644
 *******************************************************************************/
package org.eclipse.ui.internal.ide.addons;

import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Model add-on for automatic save of dirty editors.
 *
 * @since 3.12
 */
public class SaveAllDirtyPartsAddon {

	private final class DirtyEventHandler implements EventHandler {
		@Override
		public void handleEvent(Event event) {
			if (isAutoSaveActive) {
				Object isDirty = event.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (isDirty instanceof Boolean && (Boolean) isDirty) {
					autoSaveJob.schedule(autoSaveInterval);
					addIdleListenerToWorkbenchDisplay();
				} else if (noDirtyEditor(PlatformUI.getWorkbench())) {
					removeIdleListenerFromWorkbenchDisplay();
					autoSaveJob.cancel();
				}
			}
		}

		private boolean noDirtyEditor(IWorkbench workbench) {
			IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
			for (IWorkbenchWindow window : windows) {
				IWorkbenchPage p = window.getActivePage();
				if (p != null) {
					for (IEditorReference editorRef : p.getEditorReferences()) {
						if (editorRef.isDirty()) {
							return false;
						}
					}
				}
			}
			return true;
		}
	}

	private final class IdleListener implements Listener {
		@Override
		public void handleEvent(org.eclipse.swt.widgets.Event event) {
			// a menu has been hidden, re-schedule the job.
			if (event.type == SWT.Hide && event.widget instanceof Menu) {
				autoSaveJob.cancel();
				autoSaveJob.schedule(autoSaveInterval);
			} else if (autoSaveJob.getState() == Job.SLEEPING) {
				// a menu has been shown, cancel the job. The job will be
				// re-schedule when the menu will be hidden.
				if (event.type == SWT.Show && event.widget instanceof Menu) {
					autoSaveJob.cancel();
				} else {
					// the user has pressed a key or has clicked somewhere
					// (see #addIdleListenerToWorkbenchDisplay for exact list of
					// listened events), re-schedule the job if it the previous
					// delay has not expired yet.
					autoSaveJob.cancel();
					autoSaveJob.schedule(autoSaveInterval);
				}
			}
		}
	}

	private final class AutoSaveJob extends WorkbenchJob {

		private AutoSaveJob(String name) {
			super(name);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (isAutoSaveActive) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				if (workbench != null) {
					IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
					for (IWorkbenchWindow window : windows) {
						IWorkbenchPage p = window.getActivePage();
						// We do not want to save dirty editors when a sub
						// shell is visible or active (e.g. content assist,
						// javadoc hover...)
						if (p != null && !hasVisibleSubShell(getWorkbenchDisplay())) {
							p.saveAllEditors(false);
						} else {
							// reschedule the job. No need to wait for the full
							// interval time as we already have waited for this
							// amount of time.
							this.schedule(autoSaveInterval / 2);
						}
					}
				}
			}
			return Status.OK_STATUS;
		}
	}

	@Inject
	IEventBroker eventBroker;

	private final WorkbenchJob autoSaveJob;

	private final EventHandler dirtyHandler;

	private final Listener idleListener;

	private boolean isAutoSaveActive;

	private long autoSaveInterval;

	/**
	 * @param autoSave
	 */
	@Inject
	@Optional
	public void setAutoSave(
			@SuppressWarnings("restriction") @Preference(value = IPreferenceConstants.SAVE_AUTOMATICALLY, nodePath = "org.eclipse.ui.workbench") boolean autoSave) {
		isAutoSaveActive = autoSave;
		if (isAutoSaveActive) {
			eventBroker.subscribe(UIEvents.Dirtyable.TOPIC_DIRTY, dirtyHandler);
			autoSaveJob.schedule();
		} else {
			eventBroker.unsubscribe(dirtyHandler);
		}
	}

	/**
	 * @param newInterval
	 */
	@Inject
	@Optional
	public void autoSaveIntervalChanged(
			@SuppressWarnings("restriction") @Preference(value = IPreferenceConstants.SAVE_AUTOMATICALLY_INTERVAL, nodePath = "org.eclipse.ui.workbench") int newInterval) {
		autoSaveInterval = TimeUnit.SECONDS.toMillis(newInterval);
	}

	/**
	 * Default constructor
	 */
	public SaveAllDirtyPartsAddon() {
		autoSaveJob = new AutoSaveJob("Auto save all editors"); //$NON-NLS-1$
		// auto-save job should not be displayed in the progress view.
		autoSaveJob.setSystem(true);
		idleListener = new IdleListener();
		dirtyHandler = new DirtyEventHandler();
	}

	private void addIdleListenerToWorkbenchDisplay() {
		Display display = getWorkbenchDisplay();
		if (display != null && !display.isDisposed()) {
			display.addFilter(SWT.KeyUp, idleListener);
			display.addFilter(SWT.MouseUp, idleListener);
			display.addFilter(SWT.Show, idleListener);
			display.addFilter(SWT.Hide, idleListener);
		}
	}

	private void removeIdleListenerFromWorkbenchDisplay() {
		Display display = getWorkbenchDisplay();
		if (display != null && !display.isDisposed()) {
			display.removeFilter(SWT.MouseUp, idleListener);
			display.removeFilter(SWT.KeyUp, idleListener);
			display.removeFilter(SWT.Show, idleListener);
			display.removeFilter(SWT.Hide, idleListener);
		}
	}

	@PreDestroy
	private void shutdown() {
		eventBroker.unsubscribe(dirtyHandler);
		autoSaveJob.cancel();

		final Display display = getWorkbenchDisplay();
		if (display != null && !display.isDisposed()) {
			try {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						removeIdleListenerFromWorkbenchDisplay();
						// save jov could have been rescheduled by idleListener
						// before it has been removed
						autoSaveJob.cancel();
					}
				});
			} catch (SWTException ex) {
				// ignore
			}
		}
	}

	/**
	 * Checks whether the current active shell has at least one visible sub
	 * shell. This is especially the case when the content assist popup is
	 * visible or the javadoc on mouse hover is enabled.
	 *
	 * @param display
	 *            the display from which the active shell should be retrieved
	 * @return true if the active shell has at least one sub shell visible,
	 *         false otherwise.
	 */
	private static boolean hasVisibleSubShell(final Display display) {
		if (display != null && !display.isDisposed()) {
			Shell shell = display.getActiveShell();
			if (shell != null) {
				for (Shell subShell : shell.getShells()) {
					if (subShell.isVisible()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns the current workbench display, null otherwise.
	 *
	 * @return the current workbench display, null otherwise.
	 */
	private static Display getWorkbenchDisplay() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			return workbench.getDisplay();
		}
		return null;
	}
}
