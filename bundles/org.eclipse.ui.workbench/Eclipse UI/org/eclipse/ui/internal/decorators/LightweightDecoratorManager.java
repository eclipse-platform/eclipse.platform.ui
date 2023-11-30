/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.ObjectContributorManager;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.Util;

/**
 * The LightweightDecoratorManager is a decorator manager that encapsulates the
 * behavior for the lightweight decorators.
 */
public class LightweightDecoratorManager extends ObjectContributorManager {

	/**
	 * The runnable is the object used to run the decorations so that an error in
	 * someones decorator will not kill the thread. It is implemented here to
	 * prevent aborting of decoration i.e. successful decorations will still be
	 * applied.
	 */

	private static class LightweightRunnable implements ISafeRunnable {

		static class RunnableData {

			final DecorationBuilder builder;

			final LightweightDecoratorDefinition decorator;

			final Object element;

			public RunnableData(Object object, DecorationBuilder builder, LightweightDecoratorDefinition definition) {
				this.element = object;
				this.builder = builder;
				this.decorator = definition;
			}

			boolean isConsistent() {
				return builder != null && decorator != null && element != null;
			}
		}

		private volatile RunnableData data = new RunnableData(null, null, null);

		void setValues(Object object, DecorationBuilder builder, LightweightDecoratorDefinition definition) {
			data = new RunnableData(object, builder, definition);
		}

		/*
		 * @see ISafeRunnable.handleException(Throwable).
		 */
		@Override
		public void handleException(Throwable exception) {
			IStatus status = StatusUtil.newStatus(IStatus.ERROR, exception.getMessage(), exception);
			LightweightDecoratorDefinition decorator = data.decorator;
			String message;
			if (decorator == null) {
				message = WorkbenchMessages.DecoratorError;
			} else {
				String name = decorator.getName();
				if (name == null) {
					// decorator definition is not accessible anymore
					name = decorator.getId();
				}
				message = NLS.bind(WorkbenchMessages.DecoratorWillBeDisabled, name);
			}
			WorkbenchPlugin.log(message, status);
			if (decorator != null) {
				decorator.crashDisable();
			}
			clearReferences();
		}

		/*
		 * @see ISafeRunnable.run
		 */
		@Override
		public void run() throws Exception {
			// Copy to local variables, see
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=300358
			RunnableData data = this.data;
			if (data.isConsistent()) {
				data.decorator.decorate(data.element, data.builder);
			}
			clearReferences();
		}

		/**
		 * Clear all of the references in the receiver.
		 *
		 * @since 3.1
		 */
		void clearReferences() {
			data = new RunnableData(null, null, null);
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
	 * Register the decorators as object contributions so that adaptable lookup can
	 * occur.
	 */
	private void buildContributors() {
		for (LightweightDecoratorDefinition decorator : lightweightDefinitions) {
			for (String type : getTargetTypes(decorator)) {
				registerContributor(decorator, type);
			}
		}
	}

	/**
	 * For dynamic UI
	 *
	 * @param decorator the definition to add
	 * @return whether the definition was added
	 * @since 3.0
	 */
	public boolean addDecorator(LightweightDecoratorDefinition decorator) {
		if (getLightweightDecoratorDefinition(decorator.getId()) == null) {
			LightweightDecoratorDefinition[] oldDefs = lightweightDefinitions;
			lightweightDefinitions = new LightweightDecoratorDefinition[lightweightDefinitions.length + 1];
			System.arraycopy(oldDefs, 0, lightweightDefinitions, 0, oldDefs.length);
			lightweightDefinitions[oldDefs.length] = decorator;
			// no reset - handled in the DecoratorManager
			String[] types = getTargetTypes(decorator);
			for (String type : types) {
				registerContributor(decorator, type);
			}
			return true;
		}
		return false;
	}

	/**
	 * Get the name of the types that a decorator is registered for.
	 *
	 * @return String[]
	 */
	private String[] getTargetTypes(LightweightDecoratorDefinition decorator) {
		return decorator.getObjectClasses();
	}

	/**
	 * For dynamic-ui
	 *
	 * @param decorator the definition to remove
	 * @return whether the definition was removed
	 * @since 3.1
	 */
	public boolean removeDecorator(LightweightDecoratorDefinition decorator) {
		int idx = getLightweightDecoratorDefinitionIdx(decorator.getId());
		if (idx != -1) {
			LightweightDecoratorDefinition[] oldDefs = lightweightDefinitions;
			Util.arrayCopyWithRemoval(oldDefs,
					lightweightDefinitions = new LightweightDecoratorDefinition[lightweightDefinitions.length - 1],
					idx);
			// no reset - handled in the DecoratorManager
			for (String type : getTargetTypes(decorator)) {
				unregisterContributor(decorator, type);

			}
			return true;
		}
		return false;
	}

	/**
	 * Get the LightweightDecoratorDefinition with the supplied id
	 *
	 * @return LightweightDecoratorDefinition or <code>null</code> if it is not
	 *         found
	 * @param decoratorId String
	 * @since 3.0
	 */
	private LightweightDecoratorDefinition getLightweightDecoratorDefinition(String decoratorId) {
		int idx = getLightweightDecoratorDefinitionIdx(decoratorId);
		if (idx != -1) {
			return lightweightDefinitions[idx];
		}
		return null;
	}

	/**
	 * Return the index of the definition in the array.
	 *
	 * @param decoratorId the id
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
		ArrayList<LightweightDecoratorDefinition> result = new ArrayList<>();
		for (LightweightDecoratorDefinition lightweightDefinition : lightweightDefinitions) {
			if (lightweightDefinition.isEnabled()) {
				result.add(lightweightDefinition);
			}
		}
		LightweightDecoratorDefinition[] returnArray = new LightweightDecoratorDefinition[result.size()];
		result.toArray(returnArray);
		return returnArray;
	}

	/**
	 * Return whether there are enabled lightwieght decorators
	 *
	 * @return boolean
	 */
	boolean hasEnabledDefinitions() {
		for (LightweightDecoratorDefinition lightweightDefinition : lightweightDefinitions) {
			if (lightweightDefinition.isEnabled()) {
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
		for (LightweightDecoratorDefinition lightweightDefinition : lightweightDefinitions) {
			if (lightweightDefinition.isEnabled()) {
				lightweightDefinition.setEnabled(false);
			}
		}
	}

	/**
	 * Get the LightweightDecoratorDefinition with the supplied id
	 *
	 * @return LightweightDecoratorDefinition or <code>null</code> if it is not
	 *         found
	 * @param decoratorId String
	 */
	LightweightDecoratorDefinition getDecoratorDefinition(String decoratorId) {
		for (LightweightDecoratorDefinition lightweightDefinition : lightweightDefinitions) {
			if (lightweightDefinition.getId().equals(decoratorId)) {
				return lightweightDefinition;
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
					(DecoratorDefinition[]) contributors.toArray(new DecoratorDefinition[contributors.size()]));
			if (decorators.size() > 0) {
				decoratorArray = new LightweightDecoratorDefinition[decorators.size()];
				decorators.toArray(decoratorArray);
			}
		}

		return decoratorArray;
	}

	/**
	 * Fill the decoration with all of the results of the decorators.
	 *
	 * @param element    The source element
	 * @param decoration The DecorationResult we are working on. where adaptable is
	 *                   true.
	 */
	public void getDecorations(Object element, DecorationBuilder decoration) {
		for (LightweightDecoratorDefinition decorator : getDecoratorsFor(element)) {
			decoration.setCurrentDefinition(decorator);
			decorate(element, decoration, decorator);
		}
	}

	/**
	 * Decorate the element receiver in a SafeRunnable.
	 *
	 * @param element    The Object to be decorated
	 * @param decoration The object building decorations.
	 * @param decorator  The decorator being applied.
	 */
	private void decorate(Object element, DecorationBuilder decoration, LightweightDecoratorDefinition decorator) {

		runnable.setValues(element, decoration, decorator);
		SafeRunner.run(runnable);
	}

	/**
	 * Method for use by test cases
	 *
	 * @param object the object to be decorated
	 * @return the decoration result
	 */
	public DecorationResult getDecorationResult(Object object) {
		DecorationBuilder builder = new DecorationBuilder();
		getDecorations(object, builder);
		return builder.createResult();

	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		// Do nothing as this is handled by the DecoratorManager
		// This is not called as canHandleExtensionTracking returns
		// false.
	}
}
