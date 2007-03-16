/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SubProgressMonitor;
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
	private Map modelMap = new HashMap();

	// reference counting map, Saveable -> Integer
	private Map modelRefCounts = new HashMap();

	private Set nonPartSources = new HashSet();

	/**
	 * Returns the list of open models managed by this model manager.
	 * 
	 * @return a list of models
	 */
	public Saveable[] getOpenModels() {
		Set allDistinctModels = new HashSet();
		Iterator saveables = modelMap.values().iterator();
		while (saveables.hasNext())
			allDistinctModels.addAll((Set)saveables.next());
		
		return (Saveable[]) allDistinctModels.toArray(
				new Saveable[allDistinctModels.size()]);
	}

	// returns true if this model has not yet been in getModels()
	private boolean addModel(Object source, Saveable model) {
		boolean result = false;
		Set modelsForSource = (Set) modelMap.get(source);
		if (modelsForSource == null) {
			modelsForSource = new HashSet();
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
	private boolean incrementRefCount(Map referenceMap, Object key) {
		boolean result = false;
		Integer refCount = (Integer) referenceMap.get(key);
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
	private boolean decrementRefCount(Map referenceMap, Object key) {
		boolean result = false;
		Integer refCount = (Integer) referenceMap.get(key);
		Assert.isTrue(refCount != null);
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
		Set modelsForSource = (Set) modelMap.get(source);
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
			Map modelsDecrementing = new HashMap();
			Set modelsClosing = new HashSet();
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
		List removed = new ArrayList();
		for (int i = 0; i < modelArray.length; i++) {
			Saveable model = modelArray[i];
			if (removeModel(source, model)) {
				removed.add(model);
			}
		}
		if (removed.size() > 0) {
			fireModelLifecycleEvent(new SaveablesLifecycleEvent(this,
					SaveablesLifecycleEvent.POST_OPEN, (Saveable[]) removed
							.toArray(new Saveable[removed.size()]), false));
		}
	}

	/**
	 * @param source
	 * @param modelArray
	 */
	private void addModels(Object source, Saveable[] modelArray) {
		List added = new ArrayList();
		for (int i = 0; i < modelArray.length; i++) {
			Saveable model = modelArray[i];
			if (addModel(source, model)) {
				added.add(model);
			}
		}
		if (added.size() > 0) {
			fireModelLifecycleEvent(new SaveablesLifecycleEvent(this,
					SaveablesLifecycleEvent.POST_OPEN, (Saveable[]) added
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
	public Object preCloseParts(List partsToClose, boolean save,
			final IWorkbenchWindow window) {
		// reference count (how many occurrences of a model will go away?)
		PostCloseInfo postCloseInfo = new PostCloseInfo();
		for (Iterator it = partsToClose.iterator(); it.hasNext();) {
			IWorkbenchPart part = (IWorkbenchPart) it.next();
			postCloseInfo.partsClosing.add(part);
			if (part instanceof ISaveablePart) {
				ISaveablePart saveablePart = (ISaveablePart) part;
				if (save && !saveablePart.isSaveOnCloseNeeded()) {
					// pretend for now that this part is not closing
					continue;
				}
			}
			if (save && part instanceof ISaveablePart2) {
				ISaveablePart2 saveablePart2 = (ISaveablePart2) part;
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
		if (save) {
			boolean canceled = promptForSavingIfNecessary(window,
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
			Set modelsClosing, Map modelsDecrementing, boolean canCancel) {
		List modelsToOptionallySave = new ArrayList();
		for (Iterator it = modelsDecrementing.keySet().iterator(); it.hasNext();) {
			Saveable modelDecrementing = (Saveable) it.next();
			if (modelDecrementing.isDirty() && !modelsClosing.contains(modelDecrementing)) {
				modelsToOptionallySave.add(modelDecrementing);
			}
		}
		
		boolean shouldCancel = modelsToOptionallySave.isEmpty() ? false : promptForSaving(modelsToOptionallySave,
				window, window, canCancel, true);
		
		if (shouldCancel) {
			return true;
		}

		List modelsToSave = new ArrayList();
		for (Iterator it = modelsClosing.iterator(); it.hasNext();) {
			Saveable modelClosing = (Saveable) it.next();
			if (modelClosing.isDirty()) {
				modelsToSave.add(modelClosing);
			}
		}
		return modelsToSave.isEmpty() ? false : promptForSaving(modelsToSave,
				window, window, canCancel, false);
	}

	/**
	 * @param modelsClosing
	 * @param modelsDecrementing
	 */
	private void fillModelsClosing(Set modelsClosing, Map modelsDecrementing) {
		for (Iterator it = modelsDecrementing.keySet().iterator(); it.hasNext();) {
			Saveable model = (Saveable) it.next();
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
	public boolean promptForSaving(List modelsToSave,
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
				Saveable model = (Saveable) modelsToSave.get(0);
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
						protected int getShellStyle() {
							return (canCancel ? SWT.CLOSE : SWT.NONE)
									| SWT.TITLE | SWT.BORDER
									| SWT.APPLICATION_MODAL
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
						protected int getShellStyle() {
							return (canCancel ? SWT.CLOSE : SWT.NONE)
									| SWT.TITLE | SWT.BORDER
									| SWT.APPLICATION_MODAL
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
				dlg.setTitle(EditorManager.SAVE_RESOURCES_TITLE);

				// this "if" statement aids in testing.
				if (SaveableHelper.testGetAutomatedResponse() == SaveableHelper.USER_RESPONSE) {
					int result = dlg.open();
					// Just return null to prevent the operation continuing
					if (result == IDialogConstants.CANCEL_ID)
						return true;

					if (dlg.getDontPromptSelection()) {
						apiPreferenceStore.setValue(IWorkbenchPreferenceConstants.PROMPT_WHEN_SAVEABLE_STILL_OPEN, false);
					}
					
					modelsToSave = Arrays.asList(dlg.getResult());
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
	public boolean saveModels(final List finalModels, final IShellProvider shellProvider, IRunnableContext runnableContext) {
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IProgressMonitor monitorWrap = new EventLoopProgressMonitor(
						monitor);
				monitorWrap.beginTask("", finalModels.size()); //$NON-NLS-1$
				for (Iterator i = finalModels.iterator(); i.hasNext();) {
					Saveable model = (Saveable) i.next();
					// handle case where this model got saved as a result of
					// saving another
					if (!model.isDirty()) {
						monitor.worked(1);
						continue;
					}
					SaveableHelper.doSaveModel(model, new SubProgressMonitor(monitorWrap, 1), shellProvider, true);
					if (monitorWrap.isCanceled())
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
		private List partsClosing = new ArrayList();

		private Map modelsDecrementing = new HashMap();

		private Set modelsClosing = new HashSet();
	}

	/**
	 * @param postCloseInfoObject
	 */
	public void postClose(Object postCloseInfoObject) {
		PostCloseInfo postCloseInfo = (PostCloseInfo) postCloseInfoObject;
		List removed = new ArrayList();
		for (Iterator it = postCloseInfo.partsClosing.iterator(); it.hasNext();) {
			IWorkbenchPart part = (IWorkbenchPart) it.next();
			Set saveables = (Set) modelMap.get(part);
			if (saveables != null) {
				// make a copy to avoid a ConcurrentModificationException - we
				// will remove from the original set as we iterate
				saveables = new HashSet(saveables);
				for (Iterator it2 = saveables.iterator(); it2.hasNext();) {
					Saveable saveable = (Saveable) it2.next();
					if (removeModel(part, saveable)) {
						removed.add(saveable);
					}
				}
			}
		}
		if (removed.size() > 0) {
			fireModelLifecycleEvent(new SaveablesLifecycleEvent(this,
					SaveablesLifecycleEvent.POST_CLOSE, (Saveable[]) removed
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
		} else if (part instanceof ISaveablePart) {
			return new Saveable[] { new DefaultSaveable(part) };
		} else {
			return new Saveable[0];
		}
	}

	/**
	 * @param actualPart
	 */
	public void postOpen(IWorkbenchPart part) {
		addModels(part, getSaveables(part));
	}

	/**
	 * @param actualPart
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
	 * @return
	 */
	public Object[] testGetSourcesForModel(Saveable model) {
		List result = new ArrayList();
		for (Iterator it = modelMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Set values = (Set) entry.getValue();
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
			if (!canCancel) {
				int shellStyle = getShellStyle();
				shellStyle &= ~SWT.CLOSE;
				setShellStyle(shellStyle);
			}
		}

		/**
		 * @return
		 */
		public boolean getDontPromptSelection() {
			return dontPromptSelection;
		}

		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID,
					IDialogConstants.OK_LABEL, true);
			if (canCancel) {
				createButton(parent, IDialogConstants.CANCEL_ID,
						IDialogConstants.CANCEL_LABEL, false);
			}
		}
		
		protected Control createDialogArea(Composite parent) {
			 Composite dialogAreaComposite = (Composite) super.createDialogArea(parent);
			 
			 if (stillOpenElsewhere) {
				 Composite checkboxComposite = new Composite(dialogAreaComposite, SWT.NONE);
				 checkboxComposite.setLayout(new GridLayout(2, false));
				 
				 checkbox = new Button(checkboxComposite, SWT.CHECK);
				 checkbox.addSelectionListener(new SelectionAdapter() {
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
		return (ISaveablesSource[]) nonPartSources
				.toArray(new ISaveablesSource[nonPartSources.size()]);
	}

	/**
	 * @param model
	 */
	public IWorkbenchPart[] getPartsForSaveable(Saveable model) {
		List result = new ArrayList();
		for (Iterator it = modelMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Set values = (Set) entry.getValue();
			if (values.contains(model) && entry.getKey() instanceof IWorkbenchPart) {
				result.add(entry.getKey());
			}
		}
		return (IWorkbenchPart[]) result.toArray(new IWorkbenchPart[result.size()]);
	}

}
