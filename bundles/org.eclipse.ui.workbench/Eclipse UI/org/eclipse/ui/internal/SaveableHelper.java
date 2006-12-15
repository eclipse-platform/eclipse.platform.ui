/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.progress.IJobRunnable;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * Helper class for prompting to save dirty views or editors.
 * 
 * @since 3.0.1
 */
public class SaveableHelper {
	
	/**
	 * The helper must prompt.
	 */
	public static final int USER_RESPONSE = -1;
	
	private static int AutomatedResponse = USER_RESPONSE; 
	
	/**
	 * FOR USE BY THE AUTOMATED TEST HARNESS ONLY.
	 * 
	 * Sets the response to use when <code>savePart</code> is called with <code>confirm=true</code>. 
	 * 
	 * @param response 0 for yes, 1 for no, 2 for cancel, -1 for default (prompt)
	 */
	public static void testSetAutomatedResponse(int response) {
		AutomatedResponse = response;
	}
	
	/**
	 * FOR USE BY THE AUTOMATED TEST HARNESS ONLY.
	 * 
	 * Sets the response to use when <code>savePart</code> is called with <code>confirm=true</code>. 
	 * 
	 * @return 0 for yes, 1 for no, 2 for cancel, -1 for default (prompt)
	 */
	public static int testGetAutomatedResponse() {
		return AutomatedResponse;
	}
	
	/**
	 * Saves the workbench part.
	 * 
	 * @param saveable the part
	 * @param part the same part
	 * @param window the workbench window
	 * @param confirm request confirmation
	 * @return <code>true</code> for continue, <code>false</code> if the operation
	 * was cancelled.
	 */
	static boolean savePart(final ISaveablePart saveable, IWorkbenchPart part, 
			IWorkbenchWindow window, boolean confirm) {
		// Short circuit.
		if (!saveable.isDirty()) {
			return true;
		}

		// If confirmation is required ..
		if (confirm) {
			int choice = AutomatedResponse;
			if (choice == USER_RESPONSE) {				
				if (saveable instanceof ISaveablePart2) {
					choice = ((ISaveablePart2)saveable).promptToSaveOnClose();
				}
				if (choice == USER_RESPONSE || choice == ISaveablePart2.DEFAULT) {
					String message = NLS.bind(WorkbenchMessages.EditorManager_saveChangesQuestion, part.getTitle()); 
					// Show a dialog.
					String[] buttons = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
						MessageDialog d = new MessageDialog(
							window.getShell(), WorkbenchMessages.Save_Resource,
							null, message, MessageDialog.QUESTION, buttons, 0);
					choice = d.open();
				}
			}

			// Branch on the user choice.
			// The choice id is based on the order of button labels above.
			switch (choice) {
				case ISaveablePart2.YES : //yes
					break;
				case ISaveablePart2.NO : //no
					return true;
				default :
				case ISaveablePart2.CANCEL : //cancel
					return false;
			}
		}

		if (saveable instanceof ISaveablesSource) {
			return saveModels((ISaveablesSource) saveable, window, confirm);
		}

		// Create save block.
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IProgressMonitor monitorWrap = new EventLoopProgressMonitor(monitor);
				saveable.doSave(monitorWrap);
			}
		};

		// Do the save.
		return runProgressMonitorOperation(WorkbenchMessages.Save, progressOp, window); 
	}
	
	/**
	 * Saves the selected dirty models from the given model source.
	 * 
	 * @param modelSource the model source
	 * @param window the workbench window
	 * @param confirm 
	 * @return <code>true</code> for continue, <code>false</code> if the operation
	 *   was cancelled.
	 */
	private static boolean saveModels(ISaveablesSource modelSource, final IWorkbenchWindow window, final boolean confirm) {
		Saveable[] selectedModels = modelSource.getActiveSaveables();
		final ArrayList dirtyModels = new ArrayList();
		for (int i = 0; i < selectedModels.length; i++) {
			Saveable model = selectedModels[i];
			if (model.isDirty()) {
				dirtyModels.add(model);
			}
		}
		if (dirtyModels.isEmpty()) {
			return true;
		}
		
		// Create save block.
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IProgressMonitor monitorWrap = new EventLoopProgressMonitor(monitor);
				monitorWrap.beginTask("", dirtyModels.size()); //$NON-NLS-1$
				for (Iterator i = dirtyModels.iterator(); i.hasNext();) {
					Saveable model = (Saveable) i.next();
					// handle case where this model got saved as a result of saving another
					if (!model.isDirty()) {
						monitor.worked(1);
						continue;
					}
					doSaveModel(model, new SubProgressMonitor(monitorWrap, 1),
							(WorkbenchWindow) window, confirm);
					if (monitor.isCanceled()) {
						break;
					}
				}
				monitorWrap.done();
			}
		};

		// Do the save.
		return runProgressMonitorOperation(WorkbenchMessages.Save, progressOp, window); 
	}

	/**
	 * Saves the workbench part ... this is similar to 
	 * {@link SaveableHelper#savePart(ISaveablePart, IWorkbenchPart, IWorkbenchWindow, boolean) }
	 * except that the {@link ISaveablePart2#DEFAULT } case must cause the
	 * calling function to allow this part to participate in the default saving
	 * mechanism.
	 * 
	 * @param saveable the part
	 * @param window the workbench window
	 * @param confirm request confirmation
	 * @return the ISaveablePart2 constant
	 */
	static int savePart(final ISaveablePart2 saveable, 
			IWorkbenchWindow window, boolean confirm) {
		// Short circuit.
		if (!saveable.isDirty()) {
			return ISaveablePart2.YES;
		}

		// If confirmation is required ..
		if (confirm) {
			int choice = AutomatedResponse;
			if (choice == USER_RESPONSE) {
				choice = saveable.promptToSaveOnClose();
			}

			// Branch on the user choice.
			// The choice id is based on the order of button labels above.
			if (choice!=ISaveablePart2.YES) {
				return (choice==USER_RESPONSE?ISaveablePart2.DEFAULT:choice);
			}
		}

		// Create save block.
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IProgressMonitor monitorWrap = new EventLoopProgressMonitor(monitor);
				saveable.doSave(monitorWrap);
			}
		};

		// Do the save.
		if (!runProgressMonitorOperation(WorkbenchMessages.Save, progressOp,window)) {
			return ISaveablePart2.CANCEL;
		}
		return ISaveablePart2.YES;
	}
	
	/**
	 * Runs a progress monitor operation.
	 * Returns true if success, false if cancelled.
	 */
	static boolean runProgressMonitorOperation(String opName,
			IRunnableWithProgress progressOp, IWorkbenchWindow window) {
		return runProgressMonitorOperation(opName, progressOp, window,
				(WorkbenchWindow) window);
	}
	
	/**
	 * Runs a progress monitor operation.
	 * Returns true if success, false if cancelled.
	 */
	static boolean runProgressMonitorOperation(String opName,
			final IRunnableWithProgress progressOp,
			final IRunnableContext runnableContext, final IShellProvider shellProvider) {
		final boolean[] wasCanceled = new boolean[1];
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				progressOp.run(monitor);
				wasCanceled[0] = monitor.isCanceled();
			}
		};

		try {
			runnableContext.run(false, true, runnable);
		} catch (InvocationTargetException e) {
			String title = NLS.bind(WorkbenchMessages.EditorManager_operationFailed, opName ); 
			Throwable targetExc = e.getTargetException();
			WorkbenchPlugin.log(title, new Status(IStatus.WARNING, PlatformUI.PLUGIN_ID, 0, title, targetExc));
			MessageDialog.openError(shellProvider.getShell(), WorkbenchMessages.Error, title + ':' + targetExc.getMessage());
		} catch (InterruptedException e) {
			// Ignore.  The user pressed cancel.
			wasCanceled[0] = true;
		}
		return !wasCanceled[0];
	}

	/**
	 * Returns whether the model source needs saving. This is true if any of
	 * the active models are dirty. This logic must correspond with 
	 * {@link #saveModels} above.
	 * 
	 * @param modelSource
	 *            the model source
	 * @return <code>true</code> if save is required, <code>false</code>
	 *         otherwise
	 * @since 3.2
	 */
	public static boolean needsSave(ISaveablesSource modelSource) {
		Saveable[] selectedModels = modelSource.getActiveSaveables();
		for (int i = 0; i < selectedModels.length; i++) {
			Saveable model = selectedModels[i];
			if (model.isDirty() && !((InternalSaveable)model).isSavingInBackground()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param model
	 * @param progressMonitor
	 * @param shellProvider
	 * @param blockUntilSaved
	 */
	public static void doSaveModel(final Saveable model,
			IProgressMonitor progressMonitor,
			final IShellProvider shellProvider, boolean blockUntilSaved) {
		final ISchedulingRule schedulingRule = ((InternalSaveable) model)
				.getSchedulingRule();
		boolean ruleTransferred = false;
		try {
			if (model.supportsBackgroundSave()) {
				Job.getJobManager().beginRule(schedulingRule, null);
			} else {
				// To be safe for 3.3M4, don't lock if the saveable does not support background saves
				ruleTransferred = true;
			}
			final IJobRunnable[] backgroundSaveRunnable = new IJobRunnable[1];
			try {
				SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 3);
				backgroundSaveRunnable[0] = model.doSave(
						subMonitor.newChild(2), shellProvider);
				if (backgroundSaveRunnable[0] == null) {
					return;
				}
				if (blockUntilSaved || !model.supportsBackgroundSave()) {
					// for now, block on close by running the runnable in the UI
					// thread
					IStatus result = backgroundSaveRunnable[0].run(subMonitor
							.newChild(1));
					if (!result.isOK()) {
						ErrorDialog.openError(shellProvider.getShell(),
								WorkbenchMessages.Error, result.getMessage(),
								result);
						progressMonitor.setCanceled(true);
					}
					return;
				}
				// we will need the associated parts (for disabling their UI)
				((InternalSaveable) model).setSavingInBackground(true);
				SaveablesList saveablesList = (SaveablesList) PlatformUI
						.getWorkbench().getService(
								ISaveablesLifecycleListener.class);
				final IWorkbenchPart[] parts = saveablesList
						.getPartsForSaveable(model);
				{
					// about to start the job - notify the save actions,
					// this is done through the workbench windows, which
					// we can get from the parts...
					notifySaveAction(parts);
				}
				// for the job family, we use the model object because based on
				// the family we can display the busy state with an animated tab
				// (see the calls to showBusyForFamily() below).
				// As a "lock", we use a scheduling rule obtained from the model.
				// This lock is already held by the UI thread at this time. We
				// will transfer it to the job. This lock is necessary so that
				// saves or close-on-dirty that happen later, but still concurrent
				// with the background save, are blocked until the background save
				// is finished.
				Job saveJob = new Job(NLS.bind(
						WorkbenchMessages.EditorManager_backgroundSaveJobName,
						model.getName())) {
					public boolean belongsTo(Object family) {
						return family.equals(model);
					}

					protected IStatus run(IProgressMonitor monitor) {
						try {
							Job.getJobManager().beginRule(schedulingRule, null);
							return backgroundSaveRunnable[0].run(monitor);
						} finally {
							Job.getJobManager().endRule(schedulingRule);
							Job.getJobManager().endRule(schedulingRule);
						}
					}
				};
				// this will cause the parts tabs to show the ongoing background operation
				for (int i = 0; i < parts.length; i++) {
					IWorkbenchPart workbenchPart = parts[i];
					IWorkbenchSiteProgressService progressService = (IWorkbenchSiteProgressService) workbenchPart
							.getSite().getAdapter(
									IWorkbenchSiteProgressService.class);
					progressService.showBusyForFamily(model);
				}
				model.disableUI(parts, blockUntilSaved);
				// Add a listener for enabling the UI after the save job has
				// finished, and for displaying an error dialog if
				// necessary. We also use this to notify the UI thread that
				// the job has started. This complicated dance is necessary
				// to transfer the schedulingRule to the new job because we
				// can only do that once we know the thread to transfer the
				// rule to.
				final Boolean[] latch = { Boolean.FALSE };
				saveJob.addJobChangeListener(new JobChangeAdapter() {
					public void done(final IJobChangeEvent event) {
						latch[0] = Boolean.TRUE;
						shellProvider.getShell().getDisplay().asyncExec(
								new Runnable() {
									public void run() {
										((InternalSaveable) model)
												.setSavingInBackground(false);
										notifySaveAction(parts);
										model.enableUI(parts);
										IStatus result = event.getResult();
										if (!result.isOK()) {
											ErrorDialog
													.openError(
															shellProvider
																	.getShell(),
															WorkbenchMessages.Error,
															result.getMessage(),
															result);
										}
									}
								});
					}

					public void running(IJobChangeEvent event) {
						synchronized (latch) {
							latch[0] = Boolean.TRUE;
							latch.notifyAll();
						}
					}
				});
				// Finally, we are ready to schedule the job.
				// The job will block on the "beginRule" call since
				// we haven't transferred the rule to it yet. We can
				// only do that 
				saveJob.schedule();
				// wait until the job has a thread...
				synchronized (latch) {
					while (!latch[0].booleanValue()) {
						try {
							latch.wait();
						} catch (InterruptedException e) {
							// ignore
						}
					}
				}
				Job.getJobManager().transferRule(schedulingRule,
						saveJob.getThread());
				ruleTransferred = true;
				return;
			} catch (CoreException e) {
				ErrorDialog.openError(shellProvider.getShell(),
						WorkbenchMessages.Error, e.getMessage(), e.getStatus());
				progressMonitor.setCanceled(true);
			}
		} finally {
			if (!ruleTransferred) {
				Job.getJobManager().endRule(schedulingRule);
			}
			progressMonitor.done();
		}
	}

	private static void notifySaveAction(final IWorkbenchPart[] parts) {
		Set wwindows = new HashSet();
		for (int i = 0; i < parts.length; i++) {
			wwindows.add(parts[i].getSite().getWorkbenchWindow());
		}
		for (Iterator it = wwindows.iterator(); it.hasNext();) {
			WorkbenchWindow wwin = (WorkbenchWindow) it.next();
			wwin.fireBackgroundSaveStarted();
		}
	}

	public static void waitForBackgroundSaveJobs(List modelsToSave) {
		// block if any of the saveables is still saving in the background
		for (Iterator it = modelsToSave.iterator(); it.hasNext();) {
			Saveable model = (Saveable) it.next();
			// Temporary safety check for 3.3M4: Only block if the saveable
			// supports background save.
			if (model.supportsBackgroundSave()) {
				ISchedulingRule schedulingRule = ((InternalSaveable) model)
						.getSchedulingRule();
				try {
					Job.getJobManager().beginRule(schedulingRule, null);
					// if the saveable is no longer dirty, remove it from the list
					if (!model.isDirty()) {
						it.remove();
					}
				} finally {
					Job.getJobManager().endRule(schedulingRule);
				}
			}
		}
	}

}
