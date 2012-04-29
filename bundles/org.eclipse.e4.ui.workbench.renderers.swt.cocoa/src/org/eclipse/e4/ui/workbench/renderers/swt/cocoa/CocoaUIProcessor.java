/*******************************************************************************
 * Copyright (c) 2010 Brian de Alwis, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt.cocoa;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
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

	@Inject
	protected MApplication app;

	/**
	 * Execute!
	 */
	@Execute
	public void execute() {
		installAddon();

		// these handlers are installed directly on the app and are thus
		// independent of the context.
		installHandlers();
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
	public void installHandlers() {
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
		installHandler(
				defineCommand(
						"org.eclipse.ui.category.window", "org.eclipse.ui.cocoa.fullscreenWindow", //$NON-NLS-1$ //$NON-NLS-2$
						"%command.fullscreen.name", "%command.fullscreen.desc", CONTRIBUTOR_URI), //$NON-NLS-1$//$NON-NLS-2$
				FullscreenWindowHandler.class, CONTRIBUTOR_URI);
		MCommand disengageFullscreen = defineCommand(
				"org.eclipse.ui.category.window", "org.eclipse.ui.cocoa.disengageFullscreenWindow", //$NON-NLS-1$ //$NON-NLS-2$
				"%command.fullscreen.name", "%command.fullscreen.desc", CONTRIBUTOR_URI); //$NON-NLS-1$//$NON-NLS-2$
		installHandler(disengageFullscreen,
				DisengageFullscreenWindowHandler.class, CONTRIBUTOR_URI);
		installHandler(
				defineCommand(
						"org.eclipse.ui.category.window", "org.eclipse.ui.cocoa.zoomWindow", //$NON-NLS-1$ //$NON-NLS-2$
						"%command.zoom.name", "%command.zoom.desc", CONTRIBUTOR_URI), //$NON-NLS-1$//$NON-NLS-2$
				ZoomWindowHandler.class, CONTRIBUTOR_URI);
		installKeybinding(
				"org.eclipse.ui.contexts.window", "Esc", disengageFullscreen); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Install a keybinding to the provided command.
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
			// perhaps we should create it
			System.err.println("Cannot find table for binding context: " //$NON-NLS-1$
					+ bindingContextId);
			return;
		}

		MKeyBinding binding = MCommandsFactory.INSTANCE.createKeyBinding();
		binding.setCommand(cmd);
		binding.setKeySequence(keysequence);
		binding.setElementId("kb." + cmd.getElementId()); //$NON-NLS-1$
		bindingTable.getBindings().add(binding);
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
