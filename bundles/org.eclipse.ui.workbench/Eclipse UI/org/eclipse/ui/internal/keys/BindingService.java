/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.keys;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.bindings.keys.formatting.KeyFormatterFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.keys.WorkbenchKeyboard.KeyDownFilter;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * <p>
 * Provides services related to the binding architecture (e.g., keyboard
 * shortcuts) within the workbench. This service can be used to access the
 * currently active bindings, as well as the current state of the binding
 * architecture.
 * </p>
 * 
 * @since 3.1
 */
public final class BindingService implements IBindingService {

	/**
	 * The binding manager that supports this service. This value is never
	 * <code>null</code>.
	 */
	private final BindingManager bindingManager;

	/**
	 * The persistence class responsible for bindings.
	 */
	private final BindingPersistence bindingPersistence;

	/**
	 * The key binding support for the contexts. In the workbench, key bindings
	 * are intimately tied to the context mechanism.
	 */
	private WorkbenchKeyboard keyboard;

	private IWorkbench workbench;

	private Listener backForwardListener;

	/**
	 * Constructs a new instance of <code>BindingService</code> using a JFace
	 * binding manager.
	 * 
	 * @param bindingManager
	 *            The bind ing manager to use; must not be <code>null</code>.
	 * @param commandService
	 *            The command service providing support for this service; must
	 *            not be <code>null</code>;
	 * @param workbench
	 *            The workbench on which this context service will act; must not
	 *            be <code>null</code>.
	 */
	public BindingService(final BindingManager bindingManager,
			final ICommandService commandService, final IWorkbench workbench) {
		if (bindingManager == null) {
			throw new NullPointerException(
					"Cannot create a binding service with a null manager"); //$NON-NLS-1$
		}
		if (commandService == null) {
			throw new NullPointerException(
					"Cannot create a binding service with a null command service"); //$NON-NLS-1$
		}
		this.bindingManager = bindingManager;
		this.bindingPersistence = new BindingPersistence(bindingManager,
				commandService);

		this.workbench = workbench;
		// Hook up the key binding support.
		keyboard = new WorkbenchKeyboard(workbench);
		final Display display = workbench.getDisplay();
		final KeyDownFilter listener = keyboard.getKeyDownFilter();
		listener.setEnabled(false);
		display.addFilter(SWT.KeyDown, listener);
		display.addFilter(SWT.Traverse, listener);

		// Initialize the key formatter.
		KeyFormatterFactory.setDefault(SWTKeySupport
				.getKeyFormatterForPlatform());

		// Hook up the back/forward mouse buttons / special keys.
		backForwardListener = new Listener() {
			public void handleEvent(Event event) {
				String commandId;
				switch (event.button) {
				case 4:
				case 8:
					commandId = IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY;
					break;
				case 5:
				case 9:
					commandId = IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY;
					break;
				default:
					return;
				}

				final IHandlerService handlerService = (IHandlerService) workbench
						.getService(IHandlerService.class);

				try {
					handlerService.executeCommand(commandId, event);
					event.doit = false;
				} catch (NotDefinedException e) {
					// regular condition; do nothing
				} catch (NotEnabledException e) {
					// regular condition; do nothing
				} catch (NotHandledException e) {
					// regular condition; do nothing
				} catch (ExecutionException ex) {
					StatusUtil.handleStatus(ex, StatusManager.SHOW | StatusManager.LOG);
				}
			}
		};
		display.addFilter(SWT.MouseDown, backForwardListener);
	}

	/**
	 * TODO Promote this method to API.
	 * <p>
	 * Adds a single new binding to the existing array of bindings. If the array
	 * is currently <code>null</code>, then a new array is created and this
	 * binding is added to it. This method does not detect duplicates.
	 * </p>
	 * <p>
	 * This method completes in amortized <code>O(1)</code>.
	 * </p>
	 * 
	 * @param binding
	 *            The binding to be added; must not be <code>null</code>.
	 */
	public final void addBinding(final Binding binding) {
		bindingManager.addBinding(binding);
	}
	
	public final void dispose() {
		final Listener listener = keyboard.getKeyDownFilter();
		final Display display = workbench.getDisplay();
		if (display != null) {
			display.removeFilter(SWT.KeyDown, listener);
			display.removeFilter(SWT.Traverse, listener);
			display.removeFilter(SWT.MouseDown, backForwardListener);
		}
		workbench = null;
		keyboard = null;
		bindingPersistence.dispose();
		backForwardListener = null;
	}

	public final TriggerSequence[] getActiveBindingsFor(
			final ParameterizedCommand parameterizedCommand) {
		return bindingManager.getActiveBindingsFor(parameterizedCommand);
	}

	public final TriggerSequence[] getActiveBindingsFor(final String commandId) {
		return bindingManager.getActiveBindingsFor(commandId);
	}

	public final Scheme getActiveScheme() {
		return bindingManager.getActiveScheme();
	}

	public final TriggerSequence getBestActiveBindingFor(final String commandId) {
		return bindingManager.getBestActiveBindingFor(commandId);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.keys.IBindingService#getBestActiveBindingFor(org.eclipse.core.commands.ParameterizedCommand)
	 */
	public TriggerSequence getBestActiveBindingFor(ParameterizedCommand command) {
		return bindingManager.getBestActiveBindingFor(command);
	}

	public final String getBestActiveBindingFormattedFor(final String commandId) {
		return bindingManager.getBestActiveBindingFormattedFor(commandId);
	}

	public final Binding[] getBindings() {
		return bindingManager.getBindings();
	}

	public final TriggerSequence getBuffer() {
		return keyboard.getBuffer();
	}

	public final String getDefaultSchemeId() {
		return BindingPersistence.getDefaultSchemeId();
	}

	public final Scheme[] getDefinedSchemes() {
		return bindingManager.getDefinedSchemes();
	}

	/**
	 * Returns the key binding architecture for the workbench. This method is
	 * internal, and is only intended for testing. This must not be used by
	 * clients.
	 * 
	 * @return The key binding support; never <code>null</code>.
	 */
	public final WorkbenchKeyboard getKeyboard() {
		return keyboard;
	}

	public final String getLocale() {
		return bindingManager.getLocale();
	}

	public final Map getPartialMatches(final TriggerSequence trigger) {
		return bindingManager.getPartialMatches(trigger);
	}

	public final Binding getPerfectMatch(final TriggerSequence trigger) {
		return bindingManager.getPerfectMatch(trigger);
	}

	public final String getPlatform() {
		return bindingManager.getPlatform();
	}

	public final Scheme getScheme(final String schemeId) {
		return bindingManager.getScheme(schemeId);
	}

	public final boolean isKeyFilterEnabled() {
		return keyboard.getKeyDownFilter().isEnabled();
	}

	public final boolean isPartialMatch(final TriggerSequence sequence) {
		return bindingManager.isPartialMatch(sequence);
	}

	public final boolean isPerfectMatch(final TriggerSequence sequence) {
		return bindingManager.isPerfectMatch(sequence);
	}

	public final void openKeyAssistDialog() {
		keyboard.openMultiKeyAssistShell();
	}

	public final void readRegistryAndPreferences(
			final ICommandService commandService) {
		bindingPersistence.read();
	}

	/**
	 * Remove the specific binding by identity. Does nothing if the binding is
	 * not in the manager.
	 * 
	 * @param binding
	 *            The binding to be removed; must not be <code>null</code>.
	 */
	public final void removeBinding(final Binding binding) {
		bindingManager.removeBinding(binding);
	}

	public final void savePreferences(final Scheme activeScheme,
			final Binding[] bindings) throws IOException {
		BindingPersistence.write(activeScheme, bindings);
		try {
			bindingManager.setActiveScheme(activeScheme);
		} catch (final NotDefinedException e) {
			WorkbenchPlugin.log("The active scheme is not currently defined.",  //$NON-NLS-1$
					WorkbenchPlugin.getStatus(e));
		}
		bindingManager.setBindings(bindings);
	}

	public final void setKeyFilterEnabled(final boolean enabled) {
		keyboard.getKeyDownFilter().setEnabled(enabled);
	}

	/**
	 * @return Returns the bindingPersistence.
	 */
	public BindingPersistence getBindingPersistence() {
		return bindingPersistence;
	}
	
	public BindingManager getBindingManager() {
		return bindingManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#addBindingManagerListener(org.eclipse
	 * .jface.bindings.IBindingManagerListener)
	 */
	public void addBindingManagerListener(IBindingManagerListener listener) {
		bindingManager.addBindingManagerListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.keys.IBindingService#removeBindingManagerListener(org.
	 * eclipse.jface.bindings.IBindingManagerListener)
	 */
	public void removeBindingManagerListener(IBindingManagerListener listener) {
		bindingManager.removeBindingManagerListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.keys.IBindingService#getConflictsFor(org.eclipse.jface.bindings.TriggerSequence)
	 */
	public Collection getConflictsFor(TriggerSequence sequence) {
		return bindingManager.getConflictsFor(sequence);
	}

}
