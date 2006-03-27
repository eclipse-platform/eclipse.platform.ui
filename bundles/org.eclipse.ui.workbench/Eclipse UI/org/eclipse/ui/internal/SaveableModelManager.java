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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IModelLifecycleListener;
import org.eclipse.ui.ISaveableModel;
import org.eclipse.ui.ISaveableModelSource;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ModelLifecycleEvent;
import org.eclipse.ui.internal.misc.Assert;

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

	public ISaveableModel[] getModels() {
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
		}
		return result;
	}

	public void handleModelLifecycleEvent(ModelLifecycleEvent event) {
		// System.out.print(event.getSource() + "." + event.getEventType() +
		// ":"); //$NON-NLS-1$//$NON-NLS-2$
		for (int i = 0; i < event.getModels().length; i++) {
			ISaveableModel model = event.getModels()[i];
			System.out.print(model.getName() + ","); //$NON-NLS-1$
		}
		System.out.println();
		ISaveableModel[] modelArray = event.getModels();
		switch (event.getEventType()) {
		case ModelLifecycleEvent.POST_OPEN:
			addModels(event.getSource(), modelArray);
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
	 * @param workbenchWindow
	 * @return the post close info to be passed to postClose
	 */
	public Object preEditorClose(List editorsToClose, boolean save,
			IWorkbenchWindow workbenchWindow) {
		// reference count (how many occurrences of a model will go away?)
		PostCloseInfo postCloseInfo = new PostCloseInfo();
		for (Iterator it = editorsToClose.iterator(); it.hasNext();) {
			IEditorPart part = (IEditorPart) it.next();
			postCloseInfo.partsClosing.add(part);
			if (!part.isSaveOnCloseNeeded()) {
				// pretend for now that this part is not closing
				continue;
			}
			if (part instanceof ISaveablePart2) {
				ISaveablePart2 saveablePart2 = (ISaveablePart2) part;
				int response = SaveableHelper.savePart(saveablePart2, workbenchWindow, true);
				// only include this part in the following logic if it returned DEFAULT
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
		for (Iterator it = postCloseInfo.modelsDecrementing.keySet().iterator(); it
				.hasNext();) {
			ISaveableModel model = (ISaveableModel) it.next();
			if (postCloseInfo.modelsDecrementing.get(model).equals(
					modelRefCounts.get(model))) {
				postCloseInfo.modelsClosing.add(model);
			}
		}
		// TODO prompt for saving of modelsClosing (changes will be lost)
		// TODO prompt for saving of modelsDecrementing but not closing (changes
		// won't be lost)
		return postCloseInfo;
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

}
