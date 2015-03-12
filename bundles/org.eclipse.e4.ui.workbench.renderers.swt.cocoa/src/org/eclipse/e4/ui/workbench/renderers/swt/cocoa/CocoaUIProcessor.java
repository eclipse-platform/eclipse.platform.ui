/*******************************************************************************
 * Copyright (c) 2010, 2012 Brian de Alwis, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt.cocoa;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * A hack to create an IStartup-like equivalent using ModelProcessor. But as the
 * context provided to a {@code ModelProcessor} is destroyed after this
 * processor has executed (thus disconnecting any event listeners, etc.), we
 * create a new context and defer processing to the actual handling is done by
 * {@link CocoaUIHandler}.
 */
public class CocoaUIProcessor {
	/**
	 * Useful constants for referencing classes defined within this
	 * host/fragment
	 */
	static final String FRAGMENT_ID = "org.eclipse.e4.ui.workbench.renderers.swt.cocoa"; //$NON-NLS-1$
	protected static final String CONTRIBUTOR_URI = "platform:/fragment/" + FRAGMENT_ID; //$NON-NLS-1$
	static final String HOST_ID = "org.eclipse.e4.ui.workbench.renderers.swt"; //$NON-NLS-1$
	protected static final String CONTRIBUTION_URI_PREFIX = "bundleclass://" + HOST_ID; //$NON-NLS-1$

	// constants for close dialog
	private static final String COMMAND_ID_CLOSE_DIALOG = "org.eclipse.ui.cocoa.closeDialog"; //$NON-NLS-1$
	private static final String CLOSE_DIALOG_KEYSEQUENCE = "M1+W"; //$NON-NLS-1$

	@Inject
	protected MApplication app;

	@Inject
	protected Provider<StatusReporter> statusReporter;

	/**
	 * Execute!
	 */
	@Execute
	public void execute() {
		// hook About, Preferences, etc.
		installAddon();

		// Window > Zoom, etc.
		installWindowHandlers();

		// install close dialog handlers
		installCloseDialogHandlers();
	}

	/**
	 * Install the addon.
	 */
	public void installAddon() {
		String addonId = CocoaUIHandler.class.getName();
		for (MAddon addon : app.getAddons()) {
			if (addonId.equals(addon.getElementId())) {
				return;
			}
		}

		MAddon addon = MApplicationFactory.INSTANCE.createAddon();
		addon.setContributionURI(getClassURI(CocoaUIHandler.class));
		addon.setElementId(addonId);
		app.getAddons().add(addon);
	}

	/**
	 * Install the Cocoa window handlers. Sadly this has to be done here rather
	 * than in a <tt>fragments.e4xmi</tt> as the project
	 * <tt>Application.e4xmi</tt> may (and likely will) use different IDs.
	 */
	public void installWindowHandlers() {
		installHandler(
				defineCommand(
						"org.eclipse.ui.category.window", "org.eclipse.ui.cocoa.arrangeWindowsInFront", //$NON-NLS-1$ //$NON-NLS-2$
						"%command.arrangeWindows.name", //$NON-NLS-1$
						"%command.arrangeWindows.desc", CONTRIBUTOR_URI), //$NON-NLS-1$
				ArrangeWindowHandler.class, CONTRIBUTOR_URI);
		installHandler(
				defineCommand(
						"org.eclipse.ui.category.window", "org.eclipse.ui.cocoa.minimizeWindow", //$NON-NLS-1$ //$NON-NLS-2$
						"%command.minimize.name", "%command.minimize.desc", CONTRIBUTOR_URI), //$NON-NLS-1$ //$NON-NLS-2$
				MinimizeWindowHandler.class, CONTRIBUTOR_URI);

		MCommand toggleFullscreenCommand = defineCommand(
				"org.eclipse.ui.category.window", "org.eclipse.ui.cocoa.fullscreenWindow", //$NON-NLS-1$ //$NON-NLS-2$
				"%command.fullscreen.name", "%command.fullscreen.desc", CONTRIBUTOR_URI); //$NON-NLS-1$//$NON-NLS-2$
		installHandler(toggleFullscreenCommand, FullscreenWindowHandler.class,
				CONTRIBUTOR_URI);
		// COMMAND+ALT+F is taken by Force Return
		installKeybinding(
				"org.eclipse.ui.contexts.window", "COMMAND+CTRL+F", toggleFullscreenCommand); //$NON-NLS-1$ //$NON-NLS-2$

		installHandler(
				defineCommand(
						"org.eclipse.ui.category.window", "org.eclipse.ui.cocoa.zoomWindow", //$NON-NLS-1$ //$NON-NLS-2$
						"%command.zoom.name", "%command.zoom.desc", CONTRIBUTOR_URI), //$NON-NLS-1$//$NON-NLS-2$
				ZoomWindowHandler.class, CONTRIBUTOR_URI);
	}

	/** Add the special Cmd-W dialog helper */
	private void installCloseDialogHandlers() {
		MCommand closeDialogCommand = defineCommand(
				"org.eclipse.ui.category.window", COMMAND_ID_CLOSE_DIALOG, //$NON-NLS-1$
				"%command.closeDialog.name", //$NON-NLS-1$
				"%command.closeDialog.desc", CONTRIBUTOR_URI);//$NON-NLS-1$
		installHandler(closeDialogCommand, CloseDialogHandler.class,
				CONTRIBUTOR_URI);
		installKeybinding(EBindingService.DIALOG_CONTEXT_ID,
				CLOSE_DIALOG_KEYSEQUENCE, closeDialogCommand);
	}

	/**
	 * Install a keybinding to the provided command, providing the command is
	 * not already bound to another keybinding.
	 *
	 * @param bindingContextId
	 *            the keybinding context
	 * @param keysequence
	 *            the key sequence to be bound
	 * @param cmd
	 *            the command to be bound
	 */
	private void installKeybinding(String bindingContextId, String keysequence,
			MCommand cmd) {
		// there is a one-to-one mapping between binding contexts and
		// binding tables, though binding tables may not necessarily
		// guaranteed an element id.
		MBindingTable bindingTable = null;
		for (MBindingTable table : app.getBindingTables()) {
			for (MKeyBinding binding : table.getBindings()) {
				// only perform the binding if cmd not already bound
				if (binding.getCommand() == cmd) {
					return;
				}
			}
			if (table.getBindingContext() != null
					&& bindingContextId.equals(table.getBindingContext()
							.getElementId())) {
				bindingTable = table;
			}
		}

		if (bindingTable == null) {
			MBindingContext bindingContext = findBindingContext(
					app.getBindingContexts(), bindingContextId);
			if (bindingContext == null) {
				statusReporter
						.get()
						.report(new Status(
								IStatus.WARNING,
								CocoaUIProcessor.FRAGMENT_ID,
								"No binding context exists for " + bindingContextId), //$NON-NLS-1$
						StatusReporter.LOG);
				return;
			}
			bindingTable = MCommandsFactory.INSTANCE.createBindingTable();
			bindingTable.setBindingContext(bindingContext);
			bindingTable.setContributorURI(CONTRIBUTOR_URI);
			app.getBindingTables().add(bindingTable);
		}

		MKeyBinding binding = MCommandsFactory.INSTANCE.createKeyBinding();
		binding.setCommand(cmd);
		binding.setKeySequence(keysequence);
		bindingTable.getBindings().add(binding);
	}

	/**
	 * @param bindingContexts
	 * @param bindingContextId
	 * @return
	 */
	private MBindingContext findBindingContext(
			List<MBindingContext> bindingContexts, String bindingContextId) {
		for (MBindingContext bc : bindingContexts) {
			if (bindingContextId.equals(bc.getElementId())) {
				return bc;
			}
			MBindingContext result = findBindingContext(bc.getChildren(),
					bindingContextId);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Configure and install a command handler for the provided command and
	 * handler
	 *
	 * @param handlerClass
	 * @param command
	 */
	private void installHandler(MCommand command, Class<?> handlerClass,
			String contributorURI) {
		for (MHandler handler : app.getHandlers()) {
			if (handlerClass.getName().equals(handler.getElementId())
					&& handler.getCommand() == command) {
				return;
			}
		}

		MHandler handler = MCommandsFactory.INSTANCE.createHandler();
		handler.setContributionURI(getClassURI(handlerClass));
		handler.setContributorURI(contributorURI);
		handler.setElementId(handlerClass.getName());
		handler.setCommand(command);
		app.getHandlers().add(handler);
	}

	/**
	 * Find the corresponding command and define if not found.
	 *
	 * @param commandId
	 * @param name
	 * @param description
	 * @return the command
	 */
	private MCommand defineCommand(String categoryId, String commandId,
			String name, String description, String contributorURI) {
		for (MCommand command : app.getCommands()) {
			if (commandId.equals(command.getElementId())) {
				return command;
			}
		}
		MCommand command = MCommandsFactory.INSTANCE.createCommand();
		command.setCategory(defineCategory(categoryId));
		command.setElementId(commandId);
		command.setCommandName(name);
		command.setDescription(description);
		command.setContributorURI(contributorURI);
		app.getCommands().add(command);
		return command;
	}

	/**
	 * Find the corresponding category and define if not found.
	 *
	 * @param categoryId
	 * @return the category
	 */
	private MCategory defineCategory(String categoryId) {
		for (MCategory category : app.getCategories()) {
			if (categoryId.equals(category.getElementId())) {
				return category;
			}
		}
		MCategory category = MCommandsFactory.INSTANCE.createCategory();
		category.setElementId(categoryId);
		category.setName(categoryId);
		app.getCategories().add(category);
		return category;
	}

	/**
	 * Return a platform-style URI to reference the provided class
	 *
	 * @param clazz
	 *            a class
	 * @return a URI referencing the class
	 * @throws IllegalArgumentException
	 *             if the class was not defined from a bundle
	 */
	private String getClassURI(Class<?> clazz) {
		return getBundleURI(clazz) + "/" + clazz.getName(); //$NON-NLS-1$
	}

	/**
	 * Return a platform-style URI to reference the bundle providing
	 * {@code clazz}
	 *
	 * @param clazz
	 *            a class
	 * @return a URI referencing the bundle
	 * @throws IllegalArgumentException
	 *             if the class was not defined from a bundle
	 */
	private String getBundleURI(Class<?> clazz) {
		Bundle bundle = FrameworkUtil.getBundle(clazz);
		if (bundle == null) {
			throw new IllegalArgumentException(clazz.getName());
		}
		return "bundleclass://" + bundle.getSymbolicName(); //$NON-NLS-1$
	}

}
