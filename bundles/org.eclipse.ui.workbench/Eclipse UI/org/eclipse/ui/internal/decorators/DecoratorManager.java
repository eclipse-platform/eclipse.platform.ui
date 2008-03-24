/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorDecorator;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.IDelayedLabelDecorator;
import org.eclipse.jface.viewers.IFontDecorator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The DecoratorManager is the class that handles all of the decorators defined
 * in the image.
 * 
 * @since 2.0
 */
public class DecoratorManager implements ILabelProviderListener,
		IDecoratorManager, IExtensionChangeHandler {

	private static String EXTENSIONPOINT_UNIQUE_ID = WorkbenchPlugin.PI_WORKBENCH
			+ "." + IWorkbenchRegistryConstants.PL_DECORATORS; //$NON-NLS-1$

	/**
	 * The family for the decorate job.
	 */
	public static final Object FAMILY_DECORATE = new Object();

	private DecorationScheduler scheduler;

	private LightweightDecoratorManager lightweightManager;

	// Hold onto the list of listeners to be told if a change has occured
	private ListenerList listeners = new ListenerList();

	// The full definitions read from the registry.
	// Initalize to an empty collection as this is rarely used now.
	private FullDecoratorDefinition[] fullDefinitions;

	private FullTextDecoratorRunnable fullTextRunnable = new FullTextDecoratorRunnable();

	private FullImageDecoratorRunnable fullImageRunnable = new FullImageDecoratorRunnable();

	private static final FullDecoratorDefinition[] EMPTY_FULL_DEF = new FullDecoratorDefinition[0];

	private final String PREFERENCE_SEPARATOR = ","; //$NON-NLS-1$

	private final String VALUE_SEPARATOR = ":"; //$NON-NLS-1$

	private final String P_TRUE = "true"; //$NON-NLS-1$

	private final String P_FALSE = "false"; //$NON-NLS-1$

	private LocalResourceManager resourceManager;

	
	/**
	 * ManagedWorkbenchLabelDecorator is the internal LabelDecorator
	 * passed as result of calls to {@link IDecoratorManager#getLabelDecorator()}
	 * @since 3.4
	 *
	 */
	private static class ManagedWorkbenchLabelDecorator extends LabelDecorator
			implements ILabelDecorator, IDelayedLabelDecorator,
			IColorDecorator, IFontDecorator {

		private final DecoratorManager decoratorManager;
		private LocalResourceManager resourceManager;

		/**
		 * Create a new instance of the receiver that supports decoratorManager
		 * @param decoratorManager
		 */
		public ManagedWorkbenchLabelDecorator(DecoratorManager decoratorManager) {
			this.decoratorManager = decoratorManager;
			this.resourceManager = null;
		}

		/**
		 * Return a resource manager local to the receiver. 
		 * @return {@link LocalResourceManager}
		 */
		private LocalResourceManager getResourceManager() {
			if (resourceManager == null) {
				resourceManager = new LocalResourceManager(decoratorManager
						.getResourceManager());
			}
			return resourceManager;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelDecorator#decorateImage(org.eclipse.swt.graphics.Image,
		 *      java.lang.Object, org.eclipse.jface.viewers.IDecorationContext)
		 */
		public Image decorateImage(Image image, Object element,
				IDecorationContext context) {
			return decoratorManager.decorateImage(image, element, context,
					getResourceManager());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelDecorator#decorateText(java.lang.String,
		 *      java.lang.Object, org.eclipse.jface.viewers.IDecorationContext)
		 */
		public String decorateText(String text, Object element,
				IDecorationContext context) {
			return decoratorManager.decorateText(text, element, context);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelDecorator#prepareDecoration(java.lang.Object,
		 *      java.lang.String, org.eclipse.jface.viewers.IDecorationContext)
		 */
		public boolean prepareDecoration(Object element, String originalText,
				IDecorationContext context) {
			return decoratorManager.prepareDecoration(element, originalText,
					context);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IDelayedLabelDecorator#prepareDecoration(java.lang.Object,
		 *      java.lang.String)
		 */
		public boolean prepareDecoration(Object element, String originalText) {
			return prepareDecoration(element, originalText,
					DecorationContext.DEFAULT_CONTEXT);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IFontDecorator#decorateFont(java.lang.Object)
		 */
		public Font decorateFont(Object element) {
			return decoratorManager.decorateFont(element);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IColorDecorator#decorateBackground(java.lang.Object)
		 */
		public Color decorateBackground(Object element) {
			return decoratorManager.decorateBackground(element);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IColorDecorator#decorateForeground(java.lang.Object)
		 */
		public Color decorateForeground(Object element) {
			return decoratorManager.decorateForeground(element);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image,
		 *      java.lang.Object)
		 */
		public Image decorateImage(Image image, Object element) {
			return decorateImage(image, element,
					DecorationContext.DEFAULT_CONTEXT);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String,
		 *      java.lang.Object)
		 */
		public String decorateText(String text, Object element) {
			return decorateText(text, element,
					DecorationContext.DEFAULT_CONTEXT);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
			decoratorManager.addListener(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			if (resourceManager != null) {
				resourceManager.dispose();
				resourceManager = null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
		 *      java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return decoratorManager.isLabelProperty(element, property);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
			decoratorManager.removeListener(listener);
		}

	}

	/**
	 * Create a new instance of the receiver and load the settings from the
	 * installed plug-ins.
	 */
	public DecoratorManager() {

		scheduler = new DecorationScheduler(this);
		IExtensionTracker tracker = PlatformUI.getWorkbench()
				.getExtensionTracker();
		tracker.registerHandler(this, ExtensionTracker
				.createExtensionPointFilter(getExtensionPointFilter()));

		resourceManager = null;
	}

	/**
	 * Initalize the decorator definitions.
	 */
	private void initializeDecoratorDefinitions() {
		DecoratorRegistryReader reader = new DecoratorRegistryReader();
		Collection values = reader
				.readRegistry(Platform.getExtensionRegistry());

		ArrayList full = new ArrayList();
		ArrayList lightweight = new ArrayList();
		Iterator allDefinitions = values.iterator();
		IExtensionTracker configurationElementTracker = PlatformUI
				.getWorkbench().getExtensionTracker();
		while (allDefinitions.hasNext()) {
			DecoratorDefinition nextDefinition = (DecoratorDefinition) allDefinitions
					.next();
			if (nextDefinition.isFull()) {
				full.add(nextDefinition);
			} else {
				lightweight.add(nextDefinition);
			}

			configurationElementTracker.registerObject(nextDefinition
					.getConfigurationElement().getDeclaringExtension(),
					nextDefinition, IExtensionTracker.REF_WEAK);
		}

		fullDefinitions = new FullDecoratorDefinition[full.size()];
		full.toArray(fullDefinitions);

		LightweightDecoratorDefinition[] lightweightDefinitions = new LightweightDecoratorDefinition[lightweight
				.size()];
		lightweight.toArray(lightweightDefinitions);

		lightweightManager = new LightweightDecoratorManager(
				lightweightDefinitions);

		applyDecoratorsPreference();
	}

	/**
	 * For dynamic UI
	 * 
	 * @param definition
	 *            the definition to add
	 * @since 3.0
	 */
	public void addDecorator(DecoratorDefinition definition) {
		if (definition.isFull()) {
			if (getFullDecoratorDefinition(definition.getId()) == null) {
				FullDecoratorDefinition[] oldDefs = getFullDefinitions();
				fullDefinitions = new FullDecoratorDefinition[fullDefinitions.length + 1];
				System
						.arraycopy(oldDefs, 0, fullDefinitions, 0,
								oldDefs.length);
				fullDefinitions[oldDefs.length] = (FullDecoratorDefinition) definition;
				clearCaches();
				updateForEnablementChange();
			}
		} else {
			if (getLightweightManager().addDecorator(
					(LightweightDecoratorDefinition) definition)) {
				clearCaches();
				updateForEnablementChange();
			}
		}
		((Workbench) PlatformUI.getWorkbench()).getExtensionTracker()
				.registerObject(
						definition.getConfigurationElement()
								.getDeclaringExtension(), definition,
						IExtensionTracker.REF_WEAK);
	}

	/**
	 * See if the supplied decorator cache has a value for the element. If not
	 * calculate it from the enabledDefinitions and update the cache.
	 * 
	 * @return Collection of DecoratorDefinition.
	 * @param element
	 *            The element being tested.
	 * @param enabledDefinitions
	 *            The definitions currently defined for this decorator.
	 */
	static Collection getDecoratorsFor(Object element,
			DecoratorDefinition[] enabledDefinitions) {

		ArrayList decorators = new ArrayList();

		for (int i = 0; i < enabledDefinitions.length; i++) {
			if (enabledDefinitions[i].isEnabledFor(element)) {
				decorators.add(enabledDefinitions[i]);
			}
		}

		return decorators;

	}

	/**
	 * Add the listener to the list of listeners.
	 */
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove the listener from the list.
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
		scheduler.listenerRemoved(listener);
	}

	/**
	 * Get the list of elements listening to the receiver.
	 * 
	 * @return ILabelProviderListener []
	 */
	ILabelProviderListener[] getListeners() {
		Object[] array = listeners.getListeners();
		ILabelProviderListener[] listenerArray = new ILabelProviderListener[array.length];
		System.arraycopy(array, 0, listenerArray, 0, listenerArray.length);
		return listenerArray;
	}

	/**
	 * Inform all of the listeners that require an update
	 * 
	 * @param listener
	 *            The listener we are updating.
	 * @param event
	 *            the event with the update details
	 */
	void fireListener(final LabelProviderChangedEvent event,
			final ILabelProviderListener listener) {
		SafeRunner.run(new SafeRunnable() {
			public void run() {
				listener.labelProviderChanged(event);
			}
		});

	}

	/**
	 * Inform all of the listeners that require an update
	 * 
	 * @param event
	 *            the event with the update details
	 */
	void fireListeners(final LabelProviderChangedEvent event) {
		Object[] array = listeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			final ILabelProviderListener l = (ILabelProviderListener) array[i];
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					l.labelProviderChanged(event);
				}
			});
		}
	}

	/**
	 * Fire any listeners from the UIThread. Used for cases where this may be
	 * invoked outside of the UI by the public API.
	 * 
	 * @param event
	 *            the event with the update details
	 */
	void fireListenersInUIThread(final LabelProviderChangedEvent event) {

		// No updates if there is no UI
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}

		// Only bother with the job if in the UI Thread
		if (Thread.currentThread() == PlatformUI.getWorkbench().getDisplay()
				.getThread()) {
			fireListeners(event);
			return;
		}

		WorkbenchJob updateJob = new WorkbenchJob(
				WorkbenchMessages.DecorationScheduler_UpdateJobName) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				fireListeners(event);
				return Status.OK_STATUS;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
			 */
			public boolean belongsTo(Object family) {
				return FAMILY_DECORATE == family;
			}
		};
		updateJob.setSystem(true);
		updateJob.schedule();

	}

	
	/**
	 * Decorate the text in the reciever using the context.
	 * @param text
	 * @param element
	 * @param context
	 * @return String
	 * @see LabelDecorator#decorateText(String, Object, IDecorationContext)
	 */
	public String decorateText(String text, Object element,
			IDecorationContext context) {
		// Get any adaptations to IResource
		Object adapted = getResourceAdapter(element);
		String result = scheduler.decorateWithText(text, element, adapted,
				context);
		FullDecoratorDefinition[] decorators = getDecoratorsFor(element);
		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].isEnabledFor(element)) {
				String newResult = safeDecorateText(element, result,
						decorators[i]);
				if (newResult != null) {
					result = newResult;
				}
			}
		}

		if (adapted != null) {
			decorators = getDecoratorsFor(adapted);
			for (int i = 0; i < decorators.length; i++) {
				if (decorators[i].isAdaptable()
						&& decorators[i].isEnabledFor(adapted)) {
					String newResult = safeDecorateText(adapted, result,
							decorators[i]);
					if (newResult != null) {
						result = newResult;
					}
				}
			}
		}

		return result;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	public String decorateText(String text, Object element) {
		return decorateText(text, element, DecorationContext.DEFAULT_CONTEXT);
	}

	/**
	 * Decorate the text in a SafeRunnable.
	 * 
	 * @param element
	 *            The element we are decorating
	 * @param start
	 *            The currently decorated String
	 * @param decorator
	 *            The decorator to run.
	 * @return String
	 */
	private String safeDecorateText(Object element, String start,
			FullDecoratorDefinition decorator) {
		fullTextRunnable.setValues(start, element, decorator);
		SafeRunner.run(fullTextRunnable);
		String newResult = fullTextRunnable.getResult();
		return newResult;
	}

	/**
	 * Decorate the image within the context. Allocate any new images in
	 * localResourceManager
	 * @param image
	 * @param element
	 * @param context
	 * @param localResourceManager
	 * @return Image
	 * @see LabelDecorator#decorateImage(Image, Object, IDecorationContext)
	 */
	public Image decorateImage(Image image, Object element,
			IDecorationContext context, ResourceManager localResourceManager) {
		Object adapted = getResourceAdapter(element);
		Image result = scheduler.decorateWithOverlays(image, element, adapted,
				context, localResourceManager);
		FullDecoratorDefinition[] decorators = getDecoratorsFor(element);

		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].isEnabledFor(element)) {
				Image newResult = safeDecorateImage(element, result,
						decorators[i]);
				if (newResult != null) {
					result = newResult;
				}
			}
		}

		// Get any adaptations to IResource

		if (adapted != null) {
			decorators = getDecoratorsFor(adapted);
			for (int i = 0; i < decorators.length; i++) {
				if (decorators[i].isAdaptable()
						&& decorators[i].isEnabledFor(adapted)) {
					Image newResult = safeDecorateImage(adapted, result,
							decorators[i]);
					if (newResult != null) {
						result = newResult;
					}
				}
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image,
	 *      java.lang.Object)
	 */
	public Image decorateImage(Image image, Object element) {
		return decorateImage(image, element, DecorationContext.DEFAULT_CONTEXT,
				getResourceManager());
	}

	/**
	 * Decorate the image in a SafeRunnable.
	 * 
	 * @param element
	 *            The element we are decorating
	 * @param start
	 *            The currently decorated Image
	 * @param decorator
	 *            The decorator to run.
	 * @return Image
	 */
	private Image safeDecorateImage(Object element, Image start,
			FullDecoratorDefinition decorator) {
		fullImageRunnable.setValues(start, element, decorator);
		SafeRunner.run(fullImageRunnable);
		Image newResult = fullImageRunnable.getResult();
		return newResult;
	}

	/**
	 * Get the resource adapted object for the supplied element. Return
	 * <code>null</code>. if there isn't one.
	 * 
	 * @param element
	 * @return Object or <code>null</code>.
	 */
	private Object getResourceAdapter(Object element) {
		Object adapted = LegacyResourceSupport
				.getAdaptedContributorResource(element);
		if (adapted != element) {
			return adapted; // Avoid applying decorator twice
		}
		return null;
	}

	/**
	 * Return whether or not the decorator registered for element has a label
	 * property called property name.
	 */
	public boolean isLabelProperty(Object element, String property) {
		return isLabelProperty(element, property, true);
	}

	/**
	 * Return whether or not the decorator registered for element has a label
	 * property called property name. Check for an adapted resource if
	 * checkAdapted is true.
	 * 
	 * @param element
	 * @param property
	 * @param checkAdapted
	 * @return boolean <code>true</code> if there is a label property for
	 *         element or its adapted value
	 */
	public boolean isLabelProperty(Object element, String property,
			boolean checkAdapted) {
		boolean fullCheck = isLabelProperty(element, property,
				getDecoratorsFor(element));

		if (fullCheck) {
			return fullCheck;
		}

		boolean lightweightCheck = isLabelProperty(element, property,
				getLightweightManager().getDecoratorsFor(element));

		if (lightweightCheck) {
			return true;
		}

		if (checkAdapted) {
			// Get any adaptions to IResource
			Object adapted = getResourceAdapter(element);
			if (adapted == null || adapted == element) {
				return false;
			}

			fullCheck = isLabelProperty(adapted, property,
					getDecoratorsFor(adapted));
			if (fullCheck) {
				return fullCheck;
			}

			return isLabelProperty(adapted, property, lightweightManager
					.getDecoratorsFor(adapted));
		}
		return false;
	}

	private boolean isLabelProperty(Object element, String property,
			DecoratorDefinition[] decorators) {
		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].isEnabledFor(element)
					&& decorators[i].isLabelProperty(element, property)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return the enabled full decorator definitions.
	 * 
	 * @return FullDecoratorDefinition[]
	 */
	private FullDecoratorDefinition[] enabledFullDefinitions() {

		FullDecoratorDefinition[] full = getFullDefinitions();
		// As this are a deprecated data type optimize for
		// the undefined case.
		if (full.length == 0) {
			return full;
		}
		ArrayList result = new ArrayList();
		for (int i = 0; i < full.length; i++) {
			if (full[i].isEnabled()) {
				result.add(full[i]);
			}
		}
		FullDecoratorDefinition[] returnArray = new FullDecoratorDefinition[result
				.size()];
		result.toArray(returnArray);
		return returnArray;
	}

	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		// do nothing
	}

	/**
	 * Clear the caches in the manager. This is required to avoid updates that
	 * may occur due to changes in enablement.
	 */
	public void clearCaches() {
		getLightweightManager().reset();
		fullTextRunnable.clearReferences();
		fullImageRunnable.clearReferences();
	}

	/**
	 * Enablement had changed. Fire the listeners and write the preference.
	 */
	public void updateForEnablementChange() {
		// Clear any results that may be around as all labels have changed
		scheduler.clearResults();
		fireListenersInUIThread(new LabelProviderChangedEvent(this));
		writeDecoratorsPreference();
	}

	/**
	 * Get the DecoratorDefinitions defined on the receiver.
	 * 
	 * @return DecoratorDefinition[]
	 */
	public DecoratorDefinition[] getAllDecoratorDefinitions() {
		LightweightDecoratorDefinition[] lightweightDefinitions = getLightweightManager()
				.getDefinitions();
		DecoratorDefinition[] returnValue = new DecoratorDefinition[fullDefinitions.length
				+ lightweightDefinitions.length];
		System.arraycopy(fullDefinitions, 0, returnValue, 0,
				fullDefinitions.length);
		System.arraycopy(lightweightDefinitions, 0, returnValue,
				fullDefinitions.length, lightweightDefinitions.length);
		return returnValue;
	}

	/*
	 * @see ILabelProviderListener#labelProviderChanged(LabelProviderChangedEvent)
	 */
	public void labelProviderChanged(LabelProviderChangedEvent event) {
		Object[] elements = event.getElements();
		scheduler.clearResults();
		// If the elements are not specified send out a general update
		if (elements == null) {
			fireListeners(event);
		} else {
			// Assume that someone is going to care about the
			// decoration result and just start it right away
			for (int i = 0; i < elements.length; i++) {
				Object adapted = getResourceAdapter(elements[i]);
				// Force an update in case full decorators are the only ones
				// enabled
				scheduler.queueForDecoration(elements[i], adapted, true, null,
						DecorationContext.DEFAULT_CONTEXT);
			}
		}
	}

	/**
	 * Store the currently enabled decorators in preference store.
	 */
	private void writeDecoratorsPreference() {
		StringBuffer enabledIds = new StringBuffer();
		writeDecoratorsPreference(enabledIds, getFullDefinitions());
		writeDecoratorsPreference(enabledIds, getLightweightManager()
				.getDefinitions());

		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(
				IPreferenceConstants.ENABLED_DECORATORS, enabledIds.toString());
		PrefUtil.savePrefs();
	}

	private void writeDecoratorsPreference(StringBuffer enabledIds,
			DecoratorDefinition[] definitions) {
		for (int i = 0; i < definitions.length; i++) {
			enabledIds.append(definitions[i].getId());
			enabledIds.append(VALUE_SEPARATOR);
			if (definitions[i].isEnabled()) {
				enabledIds.append(P_TRUE);
			} else {
				enabledIds.append(P_FALSE);
			}

			enabledIds.append(PREFERENCE_SEPARATOR);
		}
	}

	/**
	 * Get the currently enabled decorators in preference store and set the
	 * state of the current definitions accordingly.
	 */
	public void applyDecoratorsPreference() {

		String preferenceValue = WorkbenchPlugin.getDefault()
				.getPreferenceStore().getString(
						IPreferenceConstants.ENABLED_DECORATORS);

		StringTokenizer tokenizer = new StringTokenizer(preferenceValue,
				PREFERENCE_SEPARATOR);
		Set enabledIds = new HashSet();
		Set disabledIds = new HashSet();
		while (tokenizer.hasMoreTokens()) {
			String nextValuePair = tokenizer.nextToken();

			// Strip out the true or false to get the id
			String id = nextValuePair.substring(0, nextValuePair
					.indexOf(VALUE_SEPARATOR));
			if (nextValuePair.endsWith(P_TRUE)) {
				enabledIds.add(id);
			} else {
				disabledIds.add(id);
			}
		}

		FullDecoratorDefinition[] full = getFullDefinitions();
		for (int i = 0; i < full.length; i++) {
			String id = full[i].getId();
			if (enabledIds.contains(id)) {
				full[i].setEnabled(true);
			} else {
				if (disabledIds.contains(id)) {
					full[i].setEnabled(false);
				}
			}
		}

		LightweightDecoratorDefinition[] lightweightDefinitions = getLightweightManager()
				.getDefinitions();
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			String id = lightweightDefinitions[i].getId();
			if (enabledIds.contains(id)) {
				lightweightDefinitions[i].setEnabled(true);
			} else {
				if (disabledIds.contains(id)) {
					lightweightDefinitions[i].setEnabled(false);
				}
			}
		}

	}

	/**
	 * Shutdown the decorator manager by disabling all of the decorators so that
	 * dispose() will be called on them.
	 */
	public void shutdown() {
		// Disable all of the enabled decorators
		// so as to force a dispose of thier decorators
		FullDecoratorDefinition[] full = getFullDefinitions();
		for (int i = 0; i < full.length; i++) {
			if (full[i].isEnabled()) {
				full[i].setEnabled(false);
			}
		}
		if (lightweightManager != null) {
			getLightweightManager().shutdown();
		}
		scheduler.shutdown();
		dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IDecoratorManager#getEnabled(java.lang.String)
	 */
	public boolean getEnabled(String decoratorId) {
		DecoratorDefinition definition = getDecoratorDefinition(decoratorId);
		if (definition == null) {
			return false;
		}
		return definition.isEnabled();
	}

	/**
	 * @see IDecoratorManager#getLabelDecorator()
	 */
	public ILabelDecorator getLabelDecorator() {
		return new ManagedWorkbenchLabelDecorator(this);
	}

	/**
	 * Returns the resource manager used to created images for the light weight
	 * decorator.
	 * 
	 * @return the resource manager
	 */
	public ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(JFaceResources
					.getResources(PlatformUI.getWorkbench().getDisplay()));
		}
		return resourceManager;
	}

	/**
	 * @see IDecoratorManager#setEnabled(String, boolean)
	 */
	public void setEnabled(String decoratorId, boolean enabled) {
		DecoratorDefinition definition = getDecoratorDefinition(decoratorId);
		if (definition != null) {
			definition.setEnabled(enabled);
			clearCaches();
			updateForEnablementChange();
		}
	}

	/*
	 * @see IDecoratorManager#getBaseLabelProvider(String)
	 */
	public IBaseLabelProvider getBaseLabelProvider(String decoratorId) {
		IBaseLabelProvider fullProvider = getLabelDecorator(decoratorId);
		if (fullProvider == null) {
			return getLightweightLabelDecorator(decoratorId);
		}
		return fullProvider;
	}

	/*
	 * @see IDecoratorManager#getLabelDecorator(String)
	 */
	public ILabelDecorator getLabelDecorator(String decoratorId) {
		FullDecoratorDefinition definition = getFullDecoratorDefinition(decoratorId);

		// Do not return for a disabled decorator
		if (definition != null && definition.isEnabled()) {
			return definition.getDecorator();
		}
		return null;
	}

	/*
	 * @see IDecoratorManager#getLightweightLabelDecorator(String)
	 */
	public ILightweightLabelDecorator getLightweightLabelDecorator(
			String decoratorId) {
		LightweightDecoratorDefinition definition = getLightweightManager()
				.getDecoratorDefinition(decoratorId);
		// Do not return for a disabled decorator
		if (definition != null && definition.isEnabled()) {
			return definition.getDecorator();
		}
		return null;
	}

	/**
	 * Get the DecoratorDefinition with the supplied id
	 * 
	 * @return DecoratorDefinition or <code>null</code> if it is not found
	 * @param decoratorId
	 *            String
	 */
	private DecoratorDefinition getDecoratorDefinition(String decoratorId) {
		DecoratorDefinition returnValue = getFullDecoratorDefinition(decoratorId);
		if (returnValue == null) {
			return getLightweightManager().getDecoratorDefinition(decoratorId);
		}
		return returnValue;
	}

	/**
	 * Get the FullDecoratorDefinition with the supplied id
	 * 
	 * @return FullDecoratorDefinition or <code>null</code> if it is not found
	 * @param decoratorId
	 *            the id
	 */
	private FullDecoratorDefinition getFullDecoratorDefinition(
			String decoratorId) {
		int idx = getFullDecoratorDefinitionIdx(decoratorId);
		if (idx != -1) {
			return getFullDefinitions()[idx];
		}
		return null;
	}

	/**
	 * Return the index of the definition in the array.
	 * 
	 * @param decoratorId
	 *            the id
	 * @return the index of the definition in the array or <code>-1</code>
	 * @since 3.1
	 */
	private int getFullDecoratorDefinitionIdx(String decoratorId) {
		FullDecoratorDefinition[] full = getFullDefinitions();
		for (int i = 0; i < full.length; i++) {
			if (full[i].getId().equals(decoratorId)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the full decorator definitions registered for elements of this type.
	 * 
	 * @param element
	 *            The element to look up
	 * @return FullDecoratorDefinition[]
	 */
	private FullDecoratorDefinition[] getDecoratorsFor(Object element) {

		if (element == null) {
			return EMPTY_FULL_DEF;
		}

		Collection decorators = getDecoratorsFor(element,
				enabledFullDefinitions());
		FullDecoratorDefinition[] decoratorArray = EMPTY_FULL_DEF;
		if (decorators.size() > 0) {
			decoratorArray = new FullDecoratorDefinition[decorators.size()];
			decorators.toArray(decoratorArray);
		}

		return decoratorArray;
	}

	/**
	 * Returns the lightweightManager. This method is public for use by test
	 * cases. No other classes outside of this package should use this method.
	 * 
	 * @return LightweightDecoratorManager
	 */
	public LightweightDecoratorManager getLightweightManager() {
		if (lightweightManager == null) {
			initializeDecoratorDefinitions();
		}
		return lightweightManager;
	}

	/**
	 * @see org.eclipse.ui.IDecoratorManager#update(java.lang.String)
	 */
	public void update(String decoratorId) {

		IBaseLabelProvider provider = getBaseLabelProvider(decoratorId);
		if (provider != null) {
			scheduler.clearResults();
			fireListeners(new LabelProviderChangedEvent(provider));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelDecorator#prepareDecoration(java.lang.Object,
	 *      java.lang.String, org.eclipse.jface.viewers.IDecorationContext)
	 */
	public boolean prepareDecoration(Object element, String originalText,
			IDecorationContext context) {
		// Check if there is a decoration ready or if there is no lightweight
		// decorators to be applied
		if (scheduler.isDecorationReady(element, context)
				|| !getLightweightManager().hasEnabledDefinitions()) {
			return true;
		}

		// Force an update if there is a text already
		boolean force = true;
		// If not then do not force as the undecorated value is fine
		if (originalText == null || originalText.length() == 0) {
			force = false;
		}

		// Queue the decoration.
		scheduler.queueForDecoration(element, getResourceAdapter(element),
				force, originalText, context);

		// If we are going to force an update just let that happen later.
		return !force;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IDelayedLabelDecorator#prepareDecoration(java.lang.Object,
	 *      java.lang.String)
	 */
	public boolean prepareDecoration(Object element, String originalText) {
		return prepareDecoration(element, originalText,
				DecorationContext.DEFAULT_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IFontDecorator#decorateFont(java.lang.Object)
	 */
	public Font decorateFont(Object element) {
		return scheduler.getFont(element, getResourceAdapter(element));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IColorDecorator#decorateBackground(java.lang.Object)
	 */
	public Color decorateBackground(Object element) {
		return scheduler.getBackgroundColor(element,
				getResourceAdapter(element));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IColorDecorator#decorateForeground(java.lang.Object)
	 */
	public Color decorateForeground(Object element) {
		return scheduler.getForegroundColor(element,
				getResourceAdapter(element));
	}

	/**
	 * Get all of the defined fullDefinitions. Initalize if required
	 * 
	 * @return FullDecoratorDefinition[]
	 */
	private FullDecoratorDefinition[] getFullDefinitions() {
		if (fullDefinitions == null) {
			initializeDecoratorDefinitions();
		}
		return fullDefinitions;
	}

	private IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(
				EXTENSIONPOINT_UNIQUE_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker,
	 *      org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker,
			IExtension addedExtension) {
		IConfigurationElement addedElements[] = addedExtension
				.getConfigurationElements();
		for (int i = 0; i < addedElements.length; i++) {
			DecoratorRegistryReader reader = new DecoratorRegistryReader();
			reader.readElement(addedElements[i]);
			for (Iterator j = reader.getValues().iterator(); j.hasNext();) {
				addDecorator((DecoratorDefinition) j.next());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension,
	 *      java.lang.Object[])
	 */
	public void removeExtension(IExtension source, Object[] objects) {

		boolean shouldClear = false;
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof DecoratorDefinition) {
				DecoratorDefinition definition = (DecoratorDefinition) objects[i];
				if (definition.isFull()) {
					int idx = getFullDecoratorDefinitionIdx(definition.getId());
					if (idx != -1) {
						FullDecoratorDefinition[] oldDefs = getFullDefinitions();
						Util
								.arrayCopyWithRemoval(
										oldDefs,
										fullDefinitions = new FullDecoratorDefinition[fullDefinitions.length - 1],
										idx);
						shouldClear = true;
					}
				} else {
					shouldClear |= getLightweightManager().removeDecorator(
							(LightweightDecoratorDefinition) definition);
				}
			}
		}

		if (shouldClear) {
			clearCaches();
			updateForEnablementChange();
		}

	}

}
