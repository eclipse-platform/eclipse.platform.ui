/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 372799
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;

/**
 * The model manager maintains a list of open saveable models.
 *
 * @see Saveable
 * @see ISaveablesSource
 *
 * @since 3.2
 */
public class SaveablesList implements ISaveablesLifecycleListener {

	private ListenerList listeners = new ListenerList();

	// event source (mostly ISaveablesSource) -> Set of Saveable
	private Map<Object, Set<Saveable>> modelMap = new HashMap<>();

	// reference counting map
	private Map<Saveable, Integer> modelRefCounts = new HashMap<>();

	private Set<ISaveablesSource> nonPartSources = new HashSet<>();

	/**
	 * Returns the list of open models managed by this model manager.
	 *
	 * @return a list of models
	 */
	public Saveable[] getOpenModels() {
		Set<Saveable> allDistinctModels = new HashSet<>();
		Iterator<Set<Saveable>> saveables = modelMap.values().iterator();
		while (saveables.hasNext()) {
			allDistinctModels.addAll(saveables.next());
		}

		return allDistinctModels.toArray(
				new Saveable[allDistinctModels.size()]);
	}

	// returns true if this model has not yet been in getModels()
	private boolean addModel(Object source, Saveable model) {
		if (model == null) {
			logWarning(
					"Ignored attempt to add invalid saveable", source, model); //$NON-NLS-1$
			return false;
		}
		boolean result = false;
		Set<Saveable> modelsForSource = modelMap.get(source);
		if (modelsForSource == null) {
			modelsForSource = new HashSet<>();
			modelMap.put(source, modelsForSource);
		}
		if (modelsForSource.add(model)) {
			result = incrementRefCount(modelRefCounts, model);
		} else {
			logWarning(
					"Ignored attempt to add saveable that was already registered", source, model); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * returns true if the given key was added for the first time
	 *
	 * @param referenceMap
	 * @param key
	 * @return true if the ref count of the given key is now 1
	 */
	private boolean incrementRefCount(Map<Saveable, Integer> referenceMap, Saveable key) {
		boolean result = false;
		Integer refCount = referenceMap.get(key);
		if (refCount == null) {
			result = true;
			refCount = new Integer(0);
		}
		referenceMap.put(key, new Integer(refCount.intValue() + 1));
		return result;
	}

	/**
	 * returns true if the given key has been removed
	 *
	 * @param referenceMap
	 * @param key
	 * @return true if the ref count of the given key was 1
	 */
	private boolean decrementRefCount(Map<Saveable, Integer> referenceMap, Saveable key) {
		boolean result = false;
		Integer refCount = referenceMap.get(key);
		if (refCount == null)
			Assert.isTrue(false, key + ": " + key.getName()); //$NON-NLS-1$
		if (refCount.intValue() == 1) {
			referenceMap.remove(key);
			result = true;
		} else {
			referenceMap.put(key, new Integer(refCount.intValue() - 1));
		}
		return result;
	}

	// returns true if this model was removed from getModels();
	private boolean removeModel(Object source, Saveable model) {
		boolean result = false;
		Set<Saveable> modelsForSource = modelMap.get(source);
		if (modelsForSource == null) {
			logWarning(
					"Ignored attempt to remove a saveable when no saveables were known", source, model); //$NON-NLS-1$
		} else {
			if (modelsForSource.remove(model)) {
				result = decrementRefCount(modelRefCounts, model);
				if (modelsForSource.isEmpty()) {
					modelMap.remove(source);
				}
			} else {
				logWarning(
						"Ignored attempt to remove a saveable that was not registered", source, model); //$NON-NLS-1$
			}
		}
		return result;
	}

	private void logWarning(String message, Object source, Saveable model) {
		// create a new exception
		AssertionFailedException assertionFailedException = new AssertionFailedException("unknown saveable: " + model //$NON-NLS-1$
				+ " from part: " + source); //$NON-NLS-1$
		// record the current stack trace to help with debugging
		assertionFailedException.fillInStackTrace();
		WorkbenchPlugin.log(StatusUtil.newStatus(IStatus.WARNING, message,
				assertionFailedException));
	}

	/**
	 * This implementation of handleModelLifecycleEvent must be called by
	 * implementers of ISaveablesSource whenever the list of models of the model
	 * source changes, or when the dirty state of models changes. The
	 * ISaveablesSource instance must be passed as the source of the event
	 * object.
	 * <p>
	 * This method may also be called by objects that hold on to models but are
	 * not workbench parts. In this case, the event source must be set to an
	 * object that is not an instanceof IWorkbenchPart.
	 * </p>
	 * <p>
	 * Corresponding open and close events must originate from the same
	 * (identical) event source.
	 * </p>
	 * <p>
	 * This method must be called on the UI thread.
	 * </p>
	 */
	@Override
	public void handleLifecycleEvent(SaveablesLifecycleEvent event) {
		if (!(event.getSource() instanceof IWorkbenchPart)) {
			// just update the set of non-part sources. No prompting necessary.
			// See bug 139004.
			updateNonPartSource((ISaveablesSource) event.getSource());
			return;
		}
		Saveable[] modelArray = event.getSaveables();
		switch (event.getEventType()) {
		case SaveablesLifecycleEvent.POST_OPEN:
			addModels(event.getSource(), modelArray);
			break;
		case SaveablesLifecycleEvent.PRE_CLOSE:
			Saveable[] models = event.getSaveables();
			Map<Saveable, Integer> modelsDecrementing = new HashMap<>();
			Set<Saveable> modelsClosing = new HashSet<>();
			for (int i = 0; i < models.length; i++) {
				incrementRefCount(modelsDecrementing, models[i]);
			}

			fillModelsClosing(modelsClosing, modelsDecrementing);
			boolean canceled = promptForSavingIfNecessary(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow(), modelsClosing, modelsDecrementing,
					!event.isForce());
			if (canceled) {
				event.setVeto(true);
			}
			break;
		case SaveablesLifecycleEvent.POST_CLOSE:
			removeModels(event.getSource(), modelArray);
			break;
		case SaveablesLifecycleEvent.DIRTY_CHANGED:
			fireModelLifecycleEvent(new SaveablesLifecycleEvent(this, event
					.getEventType(), event.getSaveables(), false));
			break;
		}
	}

	/**
	 * Updates the set of non-part saveables sources.
	 * @param source
	 */
	private void updateNonPartSource(ISaveablesSource source) {
		Saveable[] saveables = source.getSaveables();
		if (saveables.length == 0) {
			nonPartSources.remove(source);
		} else {
			nonPartSources.add(source);
		}
	}

	/**
	 * @param source
	 * @param modelArray
	 */
	private void removeModels(Object source, Saveable[] modelArray) {
		List<Saveable> removed = new ArrayList<>();
		for (int i = 0; i < modelArray.length; i++) {
			Saveable model = modelArray[i];
			if (removeModel(source, model)) {
				removed.add(model);
			}
		}
		if (removed.size() > 0) {
			fireModelLifecycleEvent(new SaveablesLifecycleEvent(this,
					SaveablesLifecycleEvent.POST_OPEN, removed
							.toArray(new Saveable[removed.size()]), false));
		}
	}

	/**
	 * @param source
	 * @param modelArray
	 */
	private void addModels(Object source, Saveable[] modelArray) {
		List<Saveable> added = new ArrayList<>();
		for (int i = 0; i < modelArray.length; i++) {
			Saveable model = modelArray[i];
			if (addModel(source, model)) {
				added.add(model);
			}
		}
		if (added.size() > 0) {
			fireModelLifecycleEvent(new SaveablesLifecycleEvent(this,
					SaveablesLifecycleEvent.POST_OPEN, added
							.toArray(new Saveable[added.size()]), false));
		}
	}

	/**
	 * @param event
	 */
	private void fireModelLifecycleEvent(SaveablesLifecycleEvent event) {
		Object[] listenerArray = listeners.getListeners();
		for (int i = 0; i < listenerArray.length; i++) {
			((ISaveablesLifecycleListener) listenerArray[i])
					.handleLifecycleEvent(event);
		}
	}

	/**
	 * Adds the given listener to the list of listeners. Has no effect if the
	 * same (identical) listener has already been added. The listener will be
	 * notified about changes to the models managed by this model manager. Event
	 * types include: <br>
	 * POST_OPEN when models were added to the list of models <br>
	 * POST_CLOSE when models were removed from the list of models <br>
	 * DIRTY_CHANGED when the dirty state of models changed
	 * <p>
	 * Listeners should ignore all other event types, including PRE_CLOSE. There
	 * is no guarantee that listeners are notified before models are closed.
	 *
	 * @param listener
	 */
	public void addModelLifecycleListener(ISaveablesLifecycleListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the given listener from the list of listeners. Has no effect if
	 * the given listener is not contained in the list.
	 *
	 * @param listener
	 */
	public void removeModelLifecycleListener(ISaveablesLifecycleListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @param partsToClose
	 * @param save
	 * @param window
	 * @return the post close info to be passed to postClose
	 */
	public Object preCloseParts(List<IWorkbenchPart> partsToClose, boolean save,
			final IWorkbenchWindow window) {
		return preCloseParts(partsToClose, save, window, window);
	}

	public Object preCloseParts(List<IWorkbenchPart> partsToClose, boolean save, IShellProvider shellProvider,
			final IWorkbenchWindow window) {
		return preCloseParts(partsToClose, false, save, shellProvider, window);
	}

	public Object preCloseParts(List<IWorkbenchPart> partsToClose, boolean addNonPartSources, boolean save,
			IShellProvider shellProvider, final IWorkbenchWindow window) {
		// reference count (how many occurrences of a model will go away?)
		PostCloseInfo postCloseInfo = new PostCloseInfo();
		for (IWorkbenchPart part : partsToClose) {
			postCloseInfo.partsClosing.add(part);
			ISaveablePart saveable = SaveableHelper.getSaveable(part);
			if (saveable != null) {
				if (save && !saveable.isSaveOnCloseNeeded()) {
					// pretend for now that this part is not closing
					continue;
				}
			}
			if (save && saveable instanceof ISaveablePart2) {
				ISaveablePart2 saveablePart2 = (ISaveablePart2) saveable;
				// TODO show saveablePart2 before prompting, see
				// EditorManager.saveAll
				int response = SaveableHelper.savePart(saveablePart2, window,
						true);
				if (response == ISaveablePart2.CANCEL) {
					// user canceled
					return null;
				} else if (response != ISaveablePart2.DEFAULT) {
					// only include this part in the following logic if it returned
					// DEFAULT
					continue;
				}
			}
			Saveable[] modelsFromSource = getSaveables(part);
			for (int i = 0; i < modelsFromSource.length; i++) {
				incrementRefCount(postCloseInfo.modelsDecrementing,
						modelsFromSource[i]);
			}
		}
		fillModelsClosing(postCloseInfo.modelsClosing,
				postCloseInfo.modelsDecrementing);
		if (addNonPartSources) {
			for (ISaveablesSource nonPartSource : getNonPartSources()) {
				Saveable[] saveables = nonPartSource.getSaveables();
				for (Saveable saveable : saveables) {
					if (saveable.isDirty()) {
						postCloseInfo.modelsClosing.add(saveable);
					}
				}
			}
		}
		if (save) {
			boolean canceled = promptForSavingIfNecessary(shellProvider, window,
					postCloseInfo.modelsClosing, postCloseInfo.modelsDecrementing, true);
			if (canceled) {
				return null;
			}
		}
		return postCloseInfo;
	}

	/**
	 * @param window
	 * @param modelsClosing
	 * @param canCancel
	 * @return true if the user canceled
	 */
	private boolean promptForSavingIfNecessary(final IWorkbenchWindow window,
			Set<Saveable> modelsClosing, Map<Saveable, Integer> modelsDecrementing, boolean canCancel) {
		return promptForSavingIfNecessary(window, window, modelsClosing, modelsDecrementing,
				canCancel);
	}

	private boolean promptForSavingIfNecessary(IShellProvider shellProvider,
 IWorkbenchWindow window,
			Set<Saveable> modelsClosing, Map<Saveable, Integer> modelsDecrementing, boolean canCancel) {
		List<Saveable> modelsToOptionallySave = new ArrayList<>();
		for (Saveable modelDecrementing : modelsDecrementing.keySet()) {
			if (modelDecrementing.isDirty() && !modelsClosing.contains(modelDecrementing)) {
				modelsToOptionallySave.add(modelDecrementing);
			}
		}

		boolean shouldCancel = modelsToOptionallySave.isEmpty() ? false : promptForSaving(
				modelsToOptionallySave, shellProvider, window, canCancel, true);

		if (shouldCancel) {
			return true;
		}

		List<Saveable> modelsToSave = new ArrayList<>();
		for (Saveable modelClosing : modelsClosing) {
			if (modelClosing.isDirty()) {
				modelsToSave.add(modelClosing);
			}
		}
		return modelsToSave.isEmpty() ? false : promptForSaving(modelsToSave, shellProvider,
				window, canCancel, false);
	}

	/**
	 * @param modelsClosing
	 * @param modelsDecrementing
	 */
	private void fillModelsClosing(Set<Saveable> modelsClosing, Map<Saveable, Integer> modelsDecrementing) {
		for (Saveable model : modelsDecrementing.keySet()) {
			if (modelsDecrementing.get(model).equals(modelRefCounts.get(model))) {
				modelsClosing.add(model);
			}
		}
	}

	/**
	 * Prompt the user to save the given saveables.
	 * @param modelsToSave the saveables to be saved
	 * @param shellProvider the provider used to obtain a shell in prompting is
	 *            required. Clients can use a workbench window for this.
	 * @param runnableContext a runnable context that will be used to provide a
	 *            progress monitor while the save is taking place. Clients can
	 *            use a workbench window for this.
	 * @param canCancel whether the operation can be canceled
	 * @param stillOpenElsewhere whether the models are referenced by open parts
	 * @return true if the user canceled
	 */
	public boolean promptForSaving(List<Saveable> modelsToSave,
			final IShellProvider shellProvider, IRunnableContext runnableContext, final boolean canCancel, boolean stillOpenElsewhere) {
		// Save parts, exit the method if cancel is pressed.
		if (modelsToSave.size() > 0) {
			boolean canceled = SaveableHelper.waitForBackgroundSaveJobs(modelsToSave);
			if (canceled) {
				return true;
			}

			IPreferenceStore apiPreferenceStore = PrefUtil.getAPIPreferenceStore();
			boolean dontPrompt = stillOpenElsewhere && !apiPreferenceStore.getBoolean(IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN);

			if (dontPrompt) {
				modelsToSave.clear();
				return false;
			} else if (modelsToSave.size() == 1) {
				Saveable model = modelsToSave.get(0);
				// Show a dialog.
				String[] buttons;
				if(canCancel) {
					buttons = new String[] { IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL,
							IDialogConstants.CANCEL_LABEL };
				} else {
					buttons = new String[] { IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL};
				}

				// don't save if we don't prompt
				int choice = ISaveablePart2.NO;

				MessageDialog dialog;
				if (stillOpenElsewhere) {
					String message = NLS
							.bind(
									WorkbenchMessages.EditorManager_saveChangesOptionallyQuestion,
									model.getName());
					MessageDialogWithToggle dialogWithToggle = new MessageDialogWithToggle(shellProvider.getShell(),
							WorkbenchMessages.Save_Resource, null, message,
							MessageDialog.QUESTION, buttons, 0, WorkbenchMessages.EditorManager_closeWithoutPromptingOption, false) {
						@Override
						protected int getShellStyle() {
							return (canCancel ? SWT.CLOSE : SWT.NONE)
									| SWT.TITLE | SWT.BORDER
									| SWT.APPLICATION_MODAL | SWT.SHEET
									| getDefaultOrientation();
						}
					};
					dialog = dialogWithToggle;
				} else {
					String message = NLS
							.bind(
									WorkbenchMessages.EditorManager_saveChangesQuestion,
									model.getName());
					dialog = new MessageDialog(shellProvider.getShell(),
							WorkbenchMessages.Save_Resource, null, message,
							MessageDialog.QUESTION, buttons, 0) {
						@Override
						protected int getShellStyle() {
							return (canCancel ? SWT.CLOSE : SWT.NONE)
									| SWT.TITLE | SWT.BORDER
									| SWT.APPLICATION_MODAL | SWT.SHEET
									| getDefaultOrientation();
						}
					};
				}

				choice = SaveableHelper.testGetAutomatedResponse();
				if (SaveableHelper.testGetAutomatedResponse() == SaveableHelper.USER_RESPONSE) {
					choice = dialog.open();

					if(stillOpenElsewhere) {
						// map value of choice back to ISaveablePart2 values
						switch (choice) {
						case IDialogConstants.YES_ID:
							choice = ISaveablePart2.YES;
							break;
						case IDialogConstants.NO_ID:
							choice = ISaveablePart2.NO;
							break;
						case IDialogConstants.CANCEL_ID:
							choice = ISaveablePart2.CANCEL;
							break;
						default:
							break;
						}
						MessageDialogWithToggle dialogWithToggle = (MessageDialogWithToggle) dialog;
						if (choice != ISaveablePart2.CANCEL && dialogWithToggle.getToggleState()) {
							apiPreferenceStore.setValue(IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN, false);
						}
					}
				}

				// Branch on the user choice.
				// The choice id is based on the order of button labels
				// above.
				switch (choice) {
				case ISaveablePart2.YES: // yes
					break;
				case ISaveablePart2.NO: // no
					modelsToSave.clear();
					break;
				default:
				case ISaveablePart2.CANCEL: // cancel
					return true;
				}
			} else {
				MyListSelectionDialog dlg = new MyListSelectionDialog(
						shellProvider.getShell(),
						modelsToSave,
						new ArrayContentProvider(),
						new WorkbenchPartLabelProvider(),
						stillOpenElsewhere ? WorkbenchMessages.EditorManager_saveResourcesOptionallyMessage
								: WorkbenchMessages.EditorManager_saveResourcesMessage,
						canCancel, stillOpenElsewhere);
				dlg.setInitialSelections(modelsToSave.toArray());
				dlg.setTitle(WorkbenchMessages.EditorManager_saveResourcesTitle);

				// this "if" statement aids in testing.
				if (SaveableHelper.testGetAutomatedResponse() == SaveableHelper.USER_RESPONSE) {
					int result = dlg.open();
					// Just return null to prevent the operation continuing
					if (result == IDialogConstants.CANCEL_ID)
						return true;

					if (dlg.getDontPromptSelection()) {
						apiPreferenceStore.setValue(IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN, false);
					}

					modelsToSave = new ArrayList<>();
					Object[] objects = dlg.getResult();
					for (Object object : objects) {
						if (object instanceof Saveable) {
							modelsToSave.add((Saveable) object);
						}
					}
				}
			}
		}
		// Create save block.
		return saveModels(modelsToSave, shellProvider, runnableContext);
	}

	/**
	 * Save the given models.
	 * @param finalModels the list of models to be saved
	 * @param shellProvider the provider used to obtain a shell in prompting is
	 *            required. Clients can use a workbench window for this.
	 * @param runnableContext a runnable context that will be used to provide a
	 *            progress monitor while the save is taking place. Clients can
	 *            use a workbench window for this.
	 * @return <code>true</code> if the operation was canceled
	 */
	public boolean saveModels(final List<Saveable> finalModels, final IShellProvider shellProvider,
			IRunnableContext runnableContext) {
		return saveModels(finalModels, shellProvider, runnableContext, true);
	}

	/**
	 * Save the given models.
	 *
	 * @param finalModels
	 *            the list of models to be saved
	 * @param shellProvider
	 *            the provider used to obtain a shell in prompting is required.
	 *            Clients can use a workbench window for this.
	 * @param runnableContext
	 *            a runnable context that will be used to provide a progress
	 *            monitor while the save is taking place. Clients can use a
	 *            workbench window for this.
	 * @param blockUntilSaved
	 * @return <code>true</code> if the operation was canceled
	 */
	public boolean saveModels(final List<Saveable> finalModels, final IShellProvider shellProvider,
			IRunnableContext runnableContext, final boolean blockUntilSaved) {
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				IProgressMonitor monitorWrap = new EventLoopProgressMonitor(monitor);
				SubMonitor subMonitor = SubMonitor.convert(monitorWrap, WorkbenchMessages.Saving_Modifications,
						finalModels.size());
				for (Saveable model : finalModels) {
					// handle case where this model got saved as a result of
					// saving another
					if (!model.isDirty()) {
						subMonitor.worked(1);
						continue;
					}
					SaveableHelper.doSaveModel(model, subMonitor.split(1),
							shellProvider, blockUntilSaved);
					if (subMonitor.isCanceled())
						break;
				}
				monitorWrap.done();
			}
		};

		// Do the save.
		return !SaveableHelper.runProgressMonitorOperation(
				WorkbenchMessages.Save_All, progressOp, runnableContext,
				shellProvider);
	}

	private static class PostCloseInfo {
		private List<IWorkbenchPart> partsClosing = new ArrayList<>();

		private Map<Saveable, Integer> modelsDecrementing = new HashMap<>();

		private Set<Saveable> modelsClosing = new HashSet<>();
	}

	/**
	 * @param postCloseInfoObject
	 */
	public void postClose(Object postCloseInfoObject) {
		PostCloseInfo postCloseInfo = (PostCloseInfo) postCloseInfoObject;
		List<Saveable> removed = new ArrayList<>();
		for (IWorkbenchPart part : postCloseInfo.partsClosing) {
			Set<Saveable> saveables = modelMap.get(part);
			if (saveables != null) {
				// make a copy to avoid a ConcurrentModificationException - we
				// will remove from the original set as we iterate
				saveables = new HashSet<>(saveables);
				for (Saveable saveable : saveables) {
					if (removeModel(part, saveable)) {
						removed.add(saveable);
					}
				}
			}
		}
		if (removed.size() > 0) {
			fireModelLifecycleEvent(new SaveablesLifecycleEvent(this,
					SaveablesLifecycleEvent.POST_CLOSE, removed
							.toArray(new Saveable[removed.size()]), false));
		}
	}

	/**
	 * Returns the saveable models provided by the given part. If the part does
	 * not provide any models, a default model is returned representing the
	 * part.
	 *
	 * @param part
	 *            the workbench part
	 * @return the saveable models
	 */
	private Saveable[] getSaveables(IWorkbenchPart part) {
		if (part instanceof ISaveablesSource) {
			ISaveablesSource source = (ISaveablesSource) part;
			return source.getSaveables();
		} else if (SaveableHelper.isSaveable(part)) {
			return new Saveable[] { new DefaultSaveable(part) };
		} else {
			return new Saveable[0];
		}
	}

	/**
	 * @param part
	 */
	public void postOpen(IWorkbenchPart part) {
		addModels(part, getSaveables(part));
	}

	/**
	 * @param part
	 */
	public void dirtyChanged(IWorkbenchPart part) {
		Saveable[] saveables = getSaveables(part);
		if (saveables.length > 0) {
			fireModelLifecycleEvent(new SaveablesLifecycleEvent(this,
					SaveablesLifecycleEvent.DIRTY_CHANGED, saveables, false));
		}
	}

	/**
	 * For testing purposes. Not to be called by clients.
	 *
	 * @param model
	 * @return never null
	 */
	public Object[] testGetSourcesForModel(Saveable model) {
		List<Object> result = new ArrayList<>();
		for (Entry<Object, Set<Saveable>> entry : modelMap.entrySet()) {
			Set<Saveable> values = entry.getValue();
			if (values.contains(model)) {
				result.add(entry.getKey());
			}
		}
		return result.toArray();
	}

	private static final class MyListSelectionDialog extends
			ListSelectionDialog {
		private final boolean canCancel;
		private Button checkbox;
		private boolean dontPromptSelection;
		private boolean stillOpenElsewhere;

		private MyListSelectionDialog(Shell shell, Object input,
				IStructuredContentProvider contentprovider,
				ILabelProvider labelProvider, String message, boolean canCancel, boolean stillOpenElsewhere) {
			super(shell, input, contentprovider, labelProvider, message);
			this.canCancel = canCancel;
			this.stillOpenElsewhere = stillOpenElsewhere;
			int shellStyle = getShellStyle();
			if (!canCancel) {
				shellStyle &= ~SWT.CLOSE;
			}
			setShellStyle(shellStyle | SWT.SHEET);
		}

		public boolean getDontPromptSelection() {
			return dontPromptSelection;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID,
					IDialogConstants.OK_LABEL, true);
			if (canCancel) {
				createButton(parent, IDialogConstants.CANCEL_ID,
						IDialogConstants.CANCEL_LABEL, false);
			}
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			 Composite dialogAreaComposite = (Composite) super.createDialogArea(parent);

			 if (stillOpenElsewhere) {
				 Composite checkboxComposite = new Composite(dialogAreaComposite, SWT.NONE);
				 checkboxComposite.setLayout(new GridLayout(2, false));

				 checkbox = new Button(checkboxComposite, SWT.CHECK);
				 checkbox.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						dontPromptSelection = checkbox.getSelection();
					}
				 });
				 GridData gd = new GridData();
				 gd.horizontalAlignment = SWT.BEGINNING;
				 checkbox.setLayoutData(gd);

				 Label label = new Label(checkboxComposite, SWT.NONE);
				 label.setText(WorkbenchMessages.EditorManager_closeWithoutPromptingOption);
				 gd = new GridData();
				 gd.grabExcessHorizontalSpace = true;
				 gd.horizontalAlignment = SWT.BEGINNING;
			 }

			 return dialogAreaComposite;
		}
	}

	/**
	 * @return a list of ISaveablesSource objects registered with this saveables
	 *         list which are not workbench parts.
	 */
	public ISaveablesSource[] getNonPartSources() {
		return nonPartSources
				.toArray(new ISaveablesSource[nonPartSources.size()]);
	}

	public IWorkbenchPart[] getPartsForSaveable(Saveable model) {
		List<IWorkbenchPart> result = new ArrayList<>();
		for (Entry<Object, Set<Saveable>> entry : modelMap.entrySet()) {
			Set<Saveable> values = entry.getValue();
			if (values.contains(model) && entry.getKey() instanceof IWorkbenchPart) {
				result.add((IWorkbenchPart) entry.getKey());
			}
		}
		return result.toArray(new IWorkbenchPart[result.size()]);
	}

}
