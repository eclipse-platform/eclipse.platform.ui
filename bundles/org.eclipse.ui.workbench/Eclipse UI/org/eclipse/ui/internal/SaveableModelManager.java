/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IModelLifecycleListener;
import org.eclipse.ui.ISaveableModel;
import org.eclipse.ui.ISaveableModelManager;
import org.eclipse.ui.ISaveableModelSource;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ModelLifecycleEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;

/**
 * @since 3.2
 * 
 */
public class SaveableModelManager implements ISaveableModelManager {

	private ListenerList listeners = new ListenerList();

	// event source (mostly ISaveableModelSource) -> Set of ISaveableModel
	private Map modelMap = new HashMap();

	// reference counting map, ISaveableModel -> Integer
	private Map modelRefCounts = new HashMap();

	public ISaveableModel[] getOpenModels() {
		return (ISaveableModel[]) modelRefCounts.keySet().toArray(
				new ISaveableModel[modelRefCounts.size()]);
	}

	// returns true if this model has not yet been in getModels()
	private boolean addModel(Object source, ISaveableModel model) {
		boolean result = false;
		Set modelsForSource = (Set) modelMap.get(source);
		if (modelsForSource == null) {
			modelsForSource = new HashSet();
			modelMap.put(source, modelsForSource);
		}
		if (modelsForSource.add(model)) {
			result = incrementRefCount(modelRefCounts, model);
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
	private boolean removeModel(Object source, ISaveableModel model) {
		boolean result = false;
		Set modelsForSource = (Set) modelMap.get(source);
		if (modelsForSource == null) {
			modelsForSource = new HashSet();
			modelMap.put(source, modelsForSource);
		}
		if (modelsForSource.remove(model)) {
			result = decrementRefCount(modelRefCounts, model);
			if (modelsForSource.isEmpty()) {
				modelMap.remove(source);
			}
		}
		return result;
	}

	public void handleModelLifecycleEvent(ModelLifecycleEvent event) {
		ISaveableModel[] modelArray = event.getModels();
		switch (event.getEventType()) {
		case ModelLifecycleEvent.POST_OPEN:
			addModels(event.getSource(), modelArray);
			break;
		case ModelLifecycleEvent.PRE_CLOSE:
			ISaveableModel[] models = event.getModels();
			Map modelsDecrementing = new HashMap();
			Set modelsClosing = new HashSet();
			for (int i = 0; i < models.length; i++) {
				incrementRefCount(modelsDecrementing, models[i]);
			}

			fillModelsClosing(modelsClosing, modelsDecrementing);
			boolean canceled = promptForSavingIfNecessary(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow(), modelsClosing,
					!event.isForce());
			if (canceled) {
				event.setVeto(true);
			}
			break;
		case ModelLifecycleEvent.POST_CLOSE:
			removeModels(event.getSource(), modelArray);
			break;
		case ModelLifecycleEvent.DIRTY_CHANGED:
			fireModelLifecycleEvent(new ModelLifecycleEvent(this, event
					.getEventType(), event.getModels(), false));
			break;
		}
	}

	/**
	 * @param source
	 * @param modelArray
	 */
	private void removeModels(Object source, ISaveableModel[] modelArray) {
		List removed = new ArrayList();
		for (int i = 0; i < modelArray.length; i++) {
			ISaveableModel model = modelArray[i];
			if (removeModel(source, model)) {
				removed.add(model);
			}
		}
		if (removed.size() > 0) {
			fireModelLifecycleEvent(new ModelLifecycleEvent(this,
					ModelLifecycleEvent.POST_OPEN, (ISaveableModel[]) removed
							.toArray(new ISaveableModel[removed.size()]), false));
		}
	}

	/**
	 * @param source
	 * @param modelArray
	 */
	private void addModels(Object source, ISaveableModel[] modelArray) {
		List added = new ArrayList();
		for (int i = 0; i < modelArray.length; i++) {
			ISaveableModel model = modelArray[i];
			if (addModel(source, model)) {
				added.add(model);
			}
		}
		if (added.size() > 0) {
			fireModelLifecycleEvent(new ModelLifecycleEvent(this,
					ModelLifecycleEvent.POST_OPEN, (ISaveableModel[]) added
							.toArray(new ISaveableModel[added.size()]), false));
		}
	}

	/**
	 * @param event
	 */
	private void fireModelLifecycleEvent(ModelLifecycleEvent event) {
		Object[] listenerArray = listeners.getListeners();
		for (int i = 0; i < listenerArray.length; i++) {
			((IModelLifecycleListener) listenerArray[i])
					.handleModelLifecycleEvent(event);
		}
	}

	public void addModelLifecycleListener(IModelLifecycleListener listener) {
		listeners.add(listener);
	}

	public void removeModelLifecycleListener(IModelLifecycleListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @param editorsToClose
	 * @param save
	 * @param window
	 * @return the post close info to be passed to postClose
	 */
	public Object preCloseParts(List editorsToClose, boolean save,
			final IWorkbenchWindow window) {
		// reference count (how many occurrences of a model will go away?)
		PostCloseInfo postCloseInfo = new PostCloseInfo();
		for (Iterator it = editorsToClose.iterator(); it.hasNext();) {
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
				// only include this part in the following logic if it returned
				// DEFAULT
				if (response != ISaveablePart2.DEFAULT) {
					continue;
				}
			}
			ISaveableModel[] modelsFromSource = getSaveableModels(part);
			for (int i = 0; i < modelsFromSource.length; i++) {
				incrementRefCount(postCloseInfo.modelsDecrementing,
						modelsFromSource[i]);
			}
		}
		fillModelsClosing(postCloseInfo.modelsClosing,
				postCloseInfo.modelsDecrementing);
		if (save) {
			boolean canceled = promptForSavingIfNecessary(window,
					postCloseInfo.modelsClosing, true);
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
			Set modelsClosing, boolean canCancel) {
		// TODO prompt for saving of dirty modelsDecrementing but not closing
		// (changes
		// won't be lost)

		List modelsToSave = new ArrayList();
		for (Iterator it = modelsClosing.iterator(); it.hasNext();) {
			ISaveableModel modelClosing = (ISaveableModel) it.next();
			if (modelClosing.isDirty()) {
				modelsToSave.add(modelClosing);
			}
		}
		return modelsToSave.isEmpty() ? false : promptForSaving(modelsToSave,
				window, canCancel);
	}

	/**
	 * @param modelsClosing
	 * @param modelsDecrementing
	 */
	private void fillModelsClosing(Set modelsClosing, Map modelsDecrementing) {
		for (Iterator it = modelsDecrementing.keySet().iterator(); it.hasNext();) {
			ISaveableModel model = (ISaveableModel) it.next();
			if (modelsDecrementing.get(model).equals(modelRefCounts.get(model))) {
				modelsClosing.add(model);
			}
		}
	}

	/**
	 * @param modelsToSave
	 * @param window
	 * @param canCancel
	 * @return true if the user canceled
	 */
	private boolean promptForSaving(List modelsToSave,
			final IWorkbenchWindow window, boolean canCancel) {
		// Save parts, exit the method if cancel is pressed.
		if (modelsToSave.size() > 0) {
			if (modelsToSave.size() == 1) {
				ISaveableModel model = (ISaveableModel) modelsToSave.get(0);
				String message = NLS.bind(
						WorkbenchMessages.EditorManager_saveChangesQuestion,
						model.getName());
				// Show a dialog.
				String[] buttons = new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL };
				MessageDialog d = new MessageDialog(window.getShell(),
						WorkbenchMessages.Save_Resource, null, message,
						MessageDialog.QUESTION, buttons, 0);

				int choice = SaveableHelper.testGetAutomatedResponse();
				if (SaveableHelper.testGetAutomatedResponse() == SaveableHelper.USER_RESPONSE) {
					choice = d.open();
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
				ListSelectionDialog dlg = new MyListSelectionDialog(window
						.getShell(), modelsToSave, new ArrayContentProvider(),
						new WorkbenchPartLabelProvider(),
						EditorManager.RESOURCES_TO_SAVE_MESSAGE, canCancel);
				dlg.setInitialSelections(modelsToSave.toArray());
				dlg.setTitle(EditorManager.SAVE_RESOURCES_TITLE);

				// this "if" statement aids in testing.
				if (SaveableHelper.testGetAutomatedResponse() == SaveableHelper.USER_RESPONSE) {
					int result = dlg.open();
					// Just return null to prevent the operation continuing
					if (result == IDialogConstants.CANCEL_ID)
						return true;

					modelsToSave = Arrays.asList(dlg.getResult());
				}
			}
		}
		// Create save block.
		final List finalModels = modelsToSave;
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IProgressMonitor monitorWrap = new EventLoopProgressMonitor(
						monitor);
				monitorWrap.beginTask("", finalModels.size()); //$NON-NLS-1$
				for (Iterator i = finalModels.iterator(); i.hasNext();) {
					ISaveableModel model = (ISaveableModel) i.next();
					// handle case where this model got saved as a result of
					// saving another
					if (!model.isDirty()) {
						monitor.worked(1);
						continue;
					}
					try {
						model.doSave(new SubProgressMonitor(monitorWrap, 1));
					} catch (CoreException e) {
						ErrorDialog.openError(window.getShell(),
								WorkbenchMessages.Error, e.getMessage(), e
										.getStatus());
					}
					if (monitorWrap.isCanceled())
						break;
				}
				monitorWrap.done();
			}
		};

		// Do the save.
		if (!SaveableHelper.runProgressMonitorOperation(
				WorkbenchMessages.Save_All, progressOp, window)) {
			// cancelled
			return true;
		}
		return false;
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
			ISaveableModel[] modelArray = getSaveableModels(part);
			for (int i = 0; i < modelArray.length; i++) {
				ISaveableModel model = modelArray[i];
				if (removeModel(part, model)) {
					removed.add(model);
				}
			}
		}
		if (removed.size() > 0) {
			fireModelLifecycleEvent(new ModelLifecycleEvent(this,
					ModelLifecycleEvent.POST_CLOSE, (ISaveableModel[]) removed
							.toArray(new ISaveableModel[removed.size()]), false));
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
	private ISaveableModel[] getSaveableModels(IWorkbenchPart part) {
		if (part instanceof ISaveableModelSource) {
			ISaveableModelSource source = (ISaveableModelSource) part;
			return source.getModels();
		} else if (part instanceof ISaveablePart) {
			return new ISaveableModel[] { new DefaultSaveableModel(part) };
		} else {
			return new ISaveableModel[0];
		}
	}

	/**
	 * @param actualPart
	 */
	public void postOpen(IWorkbenchPart part) {
		addModels(part, getSaveableModels(part));
	}

	/**
	 * @param actualPart
	 */
	public void dirtyChanged(IWorkbenchPart part) {
		ISaveableModel[] saveableModels = getSaveableModels(part);
		if (saveableModels.length > 0) {
			fireModelLifecycleEvent(new ModelLifecycleEvent(this,
					ModelLifecycleEvent.DIRTY_CHANGED, saveableModels, false));
		}
	}

	/**
	 * For testing purposes. Not to be called by clients.
	 * 
	 * @param model
	 * @return
	 */
	public Object[] testGetSourcesForModel(ISaveableModel model) {
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

		private MyListSelectionDialog(Shell shell, Object input,
				IStructuredContentProvider contentprovider,
				ILabelProvider labelProvider, String message, boolean canCancel) {
			super(shell, input, contentprovider, labelProvider, message);
			this.canCancel = canCancel;
			if (!canCancel) {
				int shellStyle = getShellStyle();
				shellStyle &= ~SWT.CLOSE;
				setShellStyle(shellStyle);
			}
		}

		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID,
					IDialogConstants.OK_LABEL, true);
			if (canCancel) {
				createButton(parent, IDialogConstants.CANCEL_ID,
						IDialogConstants.CANCEL_LABEL, false);
			}
		}
	}

}
