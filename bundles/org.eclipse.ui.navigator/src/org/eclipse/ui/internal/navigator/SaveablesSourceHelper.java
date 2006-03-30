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

package org.eclipse.ui.internal.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptorManager;
import org.eclipse.ui.internal.navigator.extensions.NavigatorSaveablesProviderFactoryManager;
import org.eclipse.ui.navigator.ISaveablesSourceHelper;
import org.eclipse.ui.navigator.SaveablesProvider;
import org.eclipse.ui.navigator.SaveablesProviderFactory;

/**
 * @since 3.2
 * 
 */
public class SaveablesSourceHelper implements ISaveablesSourceHelper {

	private class LifecycleListener implements ISaveablesLifecycleListener {
		public void handleLifecycleEvent(SaveablesLifecycleEvent event) {
			Saveable[] saveables = event.getSaveables();
			switch (event.getEventType()) {
			case SaveablesLifecycleEvent.POST_OPEN:
				recomputeSaveablesAndNotify();
				break;
			case SaveablesLifecycleEvent.POST_CLOSE:
				recomputeSaveablesAndNotify();
				break;
			case SaveablesLifecycleEvent.DIRTY_CHANGED:
				Saveable[] shownSaveables = getShownSaveables(saveables);
				if (shownSaveables.length > 0) {
					outsideListener
							.handleLifecycleEvent(new SaveablesLifecycleEvent(
									saveablesSource,
									SaveablesLifecycleEvent.DIRTY_CHANGED,
									shownSaveables, false));
				}
				break;
			}
		}
	}

	private Saveable[] currentSaveables;

	private ISaveablesLifecycleListener outsideListener;

	private ISaveablesLifecycleListener saveablesLifecycleListener = new LifecycleListener();

	private final ISaveablesSource saveablesSource;

	private final StructuredViewer viewer;

	/**
	 * @param saveablesSource
	 * @param viewer
	 * @param outsideListener
	 */
	public SaveablesSourceHelper(final ISaveablesSource saveablesSource,
			final StructuredViewer viewer,
			ISaveablesLifecycleListener outsideListener) {
		this.saveablesSource = saveablesSource;
		this.viewer = viewer;
		this.outsideListener = outsideListener;
		currentSaveables = computeSaveables();
	}

	private Saveable[] computeSaveables() {
		ITreeContentProvider contentProvider = (ITreeContentProvider) viewer
				.getContentProvider();
		Object viewerInput = viewer.getInput();
		List result = new ArrayList();
		Set roots = new HashSet(Arrays.asList(contentProvider
				.getElements(viewerInput)));
		SaveablesProvider[] saveablesProviders = getSaveablesProviders(saveablesLifecycleListener);
		for (int i = 0; i < saveablesProviders.length; i++) {
			SaveablesProvider saveablesProvider = saveablesProviders[i];
			Saveable[] saveables = saveablesProvider.getSaveables();
			for (int j = 0; j < saveables.length; j++) {
				Saveable saveable = saveables[j];
				Object[] elements = saveablesProvider.getElements(saveable);
				findParentChainLoop: for (int k = 0; k < elements.length; k++) {
					Object element = elements[k];
					while (element != null) {
						if (roots.contains(element)) {
							// found a parent chain leading to a root. The saveable
							// is part of the tree.
							result.add(saveable);
							break findParentChainLoop;
						}
						element = contentProvider.getParent(element);
					}
				}
			}
		}
		return (Saveable[]) result.toArray(new Saveable[result.size()]);
	}

	public Saveable[] getActiveSaveables() {
		ITreeContentProvider contentProvider = (ITreeContentProvider) viewer
				.getContentProvider();
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		Set result = new HashSet();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object element = it.next();
			// try to find a saveable that contains the selected element
			while (element != null) {
				Saveable saveable = getSaveable(element);
				if (saveable != null) {
					result.add(saveable);
					break;
				}
				element = contentProvider.getParent(element);
			}
		}
		return (Saveable[]) result.toArray(new Saveable[result.size()]);
	}

	/**
	 * @param element
	 * @return the saveable associated with the given element
	 */
	private Saveable getSaveable(Object element) {
		SaveablesProvider[] saveablesProviders = getSaveablesProviders(saveablesLifecycleListener);
		for (int i = 0; i < saveablesProviders.length; i++) {
			SaveablesProvider saveablesProvider = saveablesProviders[i];
			Saveable saveable = saveablesProvider.getSaveable(element);
			if (saveable != null) {
				return saveable;
			}
		}
		return null;
	}

	/**
	 * @return the saveables
	 */
	public Saveable[] getSaveables() {
		return currentSaveables;
	}

	/**
	 * @return all SaveablesProvider objects
	 */
	private SaveablesProvider[] getSaveablesProviders(
			ISaveablesLifecycleListener saveablesLifecycleListener) {
		// TODO optimize this
		NavigatorContentDescriptor[] descriptors = NavigatorContentDescriptorManager
				.getInstance().getAllContentDescriptors();
		List result = new ArrayList();
		for (int i = 0; i < descriptors.length; i++) {
			NavigatorContentDescriptor descriptor = descriptors[i];
			String id = descriptor.getId();
			SaveablesProviderFactory[] saveablesProviderFactories = NavigatorSaveablesProviderFactoryManager
					.getInstance().getSaveablesProviderFactories(id);
			for (int j = 0; j < saveablesProviderFactories.length; j++) {
				SaveablesProviderFactory factory = saveablesProviderFactories[j];
				SaveablesProvider saveablesProvider = factory
						.createSaveablesProvider(viewer.getContentProvider(),
								saveablesLifecycleListener);
				if (saveablesProvider != null) {
					result.add(saveablesProvider);
				}
			}
		}
		return (SaveablesProvider[]) result
				.toArray(new SaveablesProvider[result.size()]);
	}

	private Saveable[] getShownSaveables(Saveable[] saveables) {
		Set result = new HashSet(Arrays.asList(currentSaveables));
		result.retainAll(Arrays.asList(saveables));
		return (Saveable[]) result.toArray(new Saveable[result.size()]);
	}

	private void recomputeSaveablesAndNotify() {
		Set oldSaveables = new HashSet(Arrays.asList(currentSaveables));
		currentSaveables = computeSaveables();
		Set newSaveables = new HashSet(Arrays.asList(currentSaveables));
		Set removedSaveables = new HashSet(oldSaveables);
		removedSaveables.removeAll(newSaveables);
		Set addedSaveables = new HashSet(newSaveables);
		addedSaveables.removeAll(oldSaveables);
		if (addedSaveables.size() > 0) {
			outsideListener.handleLifecycleEvent(new SaveablesLifecycleEvent(
					saveablesSource, SaveablesLifecycleEvent.POST_OPEN,
					(Saveable[]) addedSaveables
							.toArray(new Saveable[addedSaveables.size()]),
					false));
		}
		// TODO this will make the closing of saveables non-cancelable.
		// Ideally, we should react to PRE_CLOSE events and fire
		// an appropriate PRE_CLOSE
		if (removedSaveables.size() > 0) {
			outsideListener.handleLifecycleEvent(new SaveablesLifecycleEvent(
					saveablesSource, SaveablesLifecycleEvent.PRE_CLOSE,
					(Saveable[]) removedSaveables
							.toArray(new Saveable[removedSaveables.size()]),
					true));
			outsideListener.handleLifecycleEvent(new SaveablesLifecycleEvent(
					saveablesSource, SaveablesLifecycleEvent.POST_CLOSE,
					(Saveable[]) removedSaveables
							.toArray(new Saveable[removedSaveables.size()]),
					false));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.ISaveablesSourceHelper#dispose()
	 */
	public void dispose() {

	}

}
