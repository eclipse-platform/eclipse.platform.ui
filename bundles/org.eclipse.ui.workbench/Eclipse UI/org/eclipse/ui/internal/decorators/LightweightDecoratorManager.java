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
import java.util.List;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.internal.ObjectContributorManager;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.Util;

/**
 * The LightweightDecoratorManager is a decorator manager that encapsulates the
 * behavior for the lightweight decorators.
 */
public class LightweightDecoratorManager extends ObjectContributorManager {

	/**
	 * The runnable is the object used to run the decorations so that an error
	 * in someones decorator will not kill the thread. It is implemented here to
	 * prevent aborting of decoration i.e. successful decorations will still be
	 * applied.
	 */

	private class LightweightRunnable implements ISafeRunnable {
		private Object element;

		private DecorationBuilder decoration;

		private LightweightDecoratorDefinition decorator;

		void setValues(Object object, DecorationBuilder builder,
				LightweightDecoratorDefinition definition) {
			element = object;
			decoration = builder;
			decorator = definition;

		}

		/*
		 * @see ISafeRunnable.handleException(Throwable).
		 */
		public void handleException(Throwable exception) {
			IStatus status = StatusUtil.newStatus(IStatus.ERROR, exception
					.getMessage(), exception);
			WorkbenchPlugin.log("Exception in Decorator", status); //$NON-NLS-1$
			if (decorator != null) {
				decorator.crashDisable();
			}
			clearReferences();
		}

		/*
		 * @see ISafeRunnable.run
		 */
		public void run() throws Exception {
			decorator.decorate(element, decoration);
			clearReferences();
		}

		/**
		 * Clear all of the references in the receiver.
		 * 
		 * @since 3.1
		 */
		void clearReferences() {
			decorator = null;
			element = null;// Clear the element
			decoration = null;
		}
	}

	private LightweightRunnable runnable = new LightweightRunnable();

	// The lightweight definitions read from the registry
	private LightweightDecoratorDefinition[] lightweightDefinitions;

	private static final LightweightDecoratorDefinition[] EMPTY_LIGHTWEIGHT_DEF = new LightweightDecoratorDefinition[0];

	LightweightDecoratorManager(LightweightDecoratorDefinition[] definitions) {
		super();
		lightweightDefinitions = definitions;
		buildContributors();
	}

	/**
	 * Get the lightweight definitions for the receiver.
	 * 
	 * @return LightweightDecoratorDefinition[]
	 */
	LightweightDecoratorDefinition[] getDefinitions() {
		return lightweightDefinitions;
	}

	/**
	 * Register the decorators as object contributions so that adaptable lookup
	 * can occur.
	 */
	private void buildContributors() {
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			LightweightDecoratorDefinition decorator = lightweightDefinitions[i];
			String[] types = getTargetTypes(decorator);
			for (int j = 0; j < types.length; j++) {
				registerContributor(decorator, types[j]);
			}
		}
	}

	/**
	 * For dynamic UI
	 * 
	 * @param decorator
	 *            the definition to add
	 * @return whether the definition was added
	 * @since 3.0
	 */
	public boolean addDecorator(LightweightDecoratorDefinition decorator) {
		if (getLightweightDecoratorDefinition(decorator.getId()) == null) {
			LightweightDecoratorDefinition[] oldDefs = lightweightDefinitions;
			lightweightDefinitions = new LightweightDecoratorDefinition[lightweightDefinitions.length + 1];
			System.arraycopy(oldDefs, 0, lightweightDefinitions, 0,
					oldDefs.length);
			lightweightDefinitions[oldDefs.length] = decorator;
			// no reset - handled in the DecoratorManager
			String[] types = getTargetTypes(decorator);
			for (int i = 0; i < types.length; i++) {
				registerContributor(decorator, types[i]);
			}
			return true;
		}
		return false;
	}

	/**
	 * Get the name of the types that a decorator is registered for.
	 * 
	 * @param decorator
	 * @return String[]
	 */
	private String[] getTargetTypes(LightweightDecoratorDefinition decorator) {
		return decorator.getObjectClasses();
	}

	/**
	 * For dynamic-ui
	 * 
	 * @param decorator
	 *            the definition to remove
	 * @return whether the definition was removed
	 * @since 3.1
	 */
	public boolean removeDecorator(LightweightDecoratorDefinition decorator) {
		int idx = getLightweightDecoratorDefinitionIdx(decorator.getId());
		if (idx != -1) {
			LightweightDecoratorDefinition[] oldDefs = lightweightDefinitions;
			Util
					.arrayCopyWithRemoval(
							oldDefs,
							lightweightDefinitions = new LightweightDecoratorDefinition[lightweightDefinitions.length - 1],
							idx);
			// no reset - handled in the DecoratorManager
			String[] types = getTargetTypes(decorator);
			for (int i = 0; i < types.length; i++) {
				unregisterContributor(decorator, types[i]);

			}
			return true;
		}
		return false;
	}

	/**
	 * Get the LightweightDecoratorDefinition with the supplied id
	 * 
	 * @return LightweightDecoratorDefinition or <code>null</code> if it is
	 *         not found
	 * @param decoratorId
	 *            String
	 * @since 3.0
	 */
	private LightweightDecoratorDefinition getLightweightDecoratorDefinition(
			String decoratorId) {
		int idx = getLightweightDecoratorDefinitionIdx(decoratorId);
		if (idx != -1) {
			return lightweightDefinitions[idx];
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
	private int getLightweightDecoratorDefinitionIdx(String decoratorId) {
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].getId().equals(decoratorId)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Return the enabled lightweight decorator definitions.
	 * 
	 * @return LightweightDecoratorDefinition[]
	 */
	LightweightDecoratorDefinition[] enabledDefinitions() {
		ArrayList result = new ArrayList();
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].isEnabled()) {
				result.add(lightweightDefinitions[i]);
			}
		}
		LightweightDecoratorDefinition[] returnArray = new LightweightDecoratorDefinition[result
				.size()];
		result.toArray(returnArray);
		return returnArray;
	}

	/**
	 * Return whether there are enabled lightwieght decorators
	 * 
	 * @return boolean
	 */
	boolean hasEnabledDefinitions() {
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].isEnabled()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reset any cached values.
	 */
	void reset() {
		runnable.clearReferences();
	}

	/**
	 * Shutdown the decorator manager by disabling all of the decorators so that
	 * dispose() will be called on them.
	 */
	void shutdown() {
		// Disable all fo the enabled decorators
		// so as to force a dispose of thier decorators
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].isEnabled()) {
				lightweightDefinitions[i].setEnabled(false);
			}
		}
	}

	/**
	 * Get the LightweightDecoratorDefinition with the supplied id
	 * 
	 * @return LightweightDecoratorDefinition or <code>null</code> if it is
	 *         not found
	 * @param decoratorId
	 *            String
	 */
	LightweightDecoratorDefinition getDecoratorDefinition(String decoratorId) {
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].getId().equals(decoratorId)) {
				return lightweightDefinitions[i];
			}
		}
		return null;
	}

	/**
	 * Get the lightweight registered for elements of this type.
	 */
	LightweightDecoratorDefinition[] getDecoratorsFor(Object element) {

		if (element == null) {
			return EMPTY_LIGHTWEIGHT_DEF;
		}

		List elements = new ArrayList(1);
		elements.add(element);
		LightweightDecoratorDefinition[] decoratorArray = EMPTY_LIGHTWEIGHT_DEF;
		List contributors = getContributors(elements);
		if (!contributors.isEmpty()) {
			Collection decorators = DecoratorManager.getDecoratorsFor(element,
					(DecoratorDefinition[]) contributors
							.toArray(new DecoratorDefinition[contributors
									.size()]));
			if (decorators.size() > 0) {
				decoratorArray = new LightweightDecoratorDefinition[decorators
						.size()];
				decorators.toArray(decoratorArray);
			}
		}

		return decoratorArray;
	}

	/**
	 * Fill the decoration with all of the results of the decorators.
	 * 
	 * @param element
	 *            The source element
	 * @param decoration
	 *            The DecorationResult we are working on. where adaptable is
	 *            true.
	 */
	public void getDecorations(Object element, DecorationBuilder decoration) {

		LightweightDecoratorDefinition[] decorators = getDecoratorsFor(element);

		for (int i = 0; i < decorators.length; i++) {
			// If we are doing the adaptable one make sure we are
			// only applying the adaptable decorations
			LightweightDecoratorDefinition dd = decorators[i];
			decoration.setCurrentDefinition(dd);
			decorate(element, decoration, dd);
		}
	}

	/**
	 * Decorate the element receiver in a SafeRunnable.
	 * 
	 * @param element
	 *            The Object to be decorated
	 * @param decoration
	 *            The object building decorations.
	 * @param decorator
	 *            The decorator being applied.
	 */
	private void decorate(Object element, DecorationBuilder decoration,
			LightweightDecoratorDefinition decorator) {

		runnable.setValues(element, decoration, decorator);
		SafeRunner.run(runnable);
	}

	
	/**
	 * Method for use by test cases
	 * 
	 * @param object
	 *            the object to be decorated
	 * @return the decoration result
	 */
	public DecorationResult getDecorationResult(Object object) {
		DecorationBuilder builder = new DecorationBuilder();
		getDecorations(object, builder);
		return builder.createResult();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker,
	 *      org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		// Do nothing as this is handled by the DecoratorManager
		// This is not called as canHandleExtensionTracking returns
		// false.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ObjectContributorManager#canHandleExtensionTracking()
	 */
	protected boolean canHandleExtensionTracking() {
		return false;
	}

}
