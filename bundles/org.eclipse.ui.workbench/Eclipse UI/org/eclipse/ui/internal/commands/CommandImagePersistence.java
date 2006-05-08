/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.RegistryPersistence;
import org.eclipse.ui.internal.util.BundleUtility;

/**
 * <p>
 * Handles persistence for the command images.
 * </p>
 * <p>
 * This class is only intended for internal use within the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
final class CommandImagePersistence extends RegistryPersistence {

	/**
	 * The index of the image elements in the indexed array.
	 * 
	 * @see CommandImagePersistence#read()
	 */
	private static final int INDEX_IMAGES = 0;

	/**
	 * Reads all of the images from the command images extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the command images extension
	 *            point; must not be <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param commandImageManager
	 *            The command image manager to which the images should be added;
	 *            must not be <code>null</code>.
	 * @param commandService
	 *            The command service for the workbench; must not be
	 *            <code>null</code>.
	 */
	private static final void readImagesFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final CommandImageManager commandImageManager,
			final ICommandService commandService) {
		// Undefine all the previous images.
		commandImageManager.clear();

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = readRequired(configurationElement,
					ATT_COMMAND_ID, warningsToLog, "Image needs an id"); //$NON-NLS-1$
			if (commandId == null) {
				continue;
			}

			if (!commandService.getCommand(commandId).isDefined()) {
				// Reference to an undefined command. This is invalid.
				addWarning(warningsToLog,
						"Cannot bind to an undefined command", //$NON-NLS-1$
						configurationElement, commandId);
				continue;
			}

			// Read out the style.
			final String style = readOptional(configurationElement, ATT_STYLE);

			// Read out the default icon.
			final String icon = readRequired(configurationElement, ATT_ICON,
					warningsToLog, commandId);
			if (icon == null) {
				continue;
			}

			final String disabledIcon = readOptional(configurationElement,
					ATT_DISABLEDICON);
			final String hoverIcon = readOptional(configurationElement,
					ATT_HOVERICON);

			final URL iconURL = BundleUtility.find(configurationElement
					.getNamespace(), icon);
			commandImageManager.bind(commandId,
					CommandImageManager.TYPE_DEFAULT, style, iconURL);
			if (disabledIcon != null) {
				final URL disabledIconURL = BundleUtility.find(
						configurationElement.getNamespace(), disabledIcon);
				commandImageManager.bind(commandId,
						CommandImageManager.TYPE_DISABLED, style,
						disabledIconURL);
			}
			if (hoverIcon != null) {
				final URL hoverIconURL = BundleUtility.find(
						configurationElement.getNamespace(), hoverIcon);
				commandImageManager.bind(commandId,
						CommandImageManager.TYPE_HOVER, style, hoverIconURL);
			}
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the images from the 'org.eclipse.ui.commandImages' extension point."); //$NON-NLS-1$
	}

	/**
	 * The command image manager which should be populated with the values from
	 * the registry; must not be <code>null</code>.
	 */
	private final CommandImageManager commandImageManager;

	/**
	 * The command service for the workbench; must not be <code>null</code>.
	 */
	private final ICommandService commandService;

	/**
	 * Constructs a new instance of <code>CommandImagePersistence</code>.
	 * 
	 * @param commandImageManager
	 *            The command image manager which should be populated with the
	 *            values from the registry; must not be <code>null</code>.
	 * @param commandService
	 *            The command service for the workbench; must not be
	 *            <code>null</code>.
	 */
	CommandImagePersistence(final CommandImageManager commandImageManager,
			final ICommandService commandService) {
		this.commandImageManager = commandImageManager;
		this.commandService = commandService;
	}

	protected final boolean isChangeImportant(final IRegistryChangeEvent event) {
		final IExtensionDelta[] imageDeltas = event.getExtensionDeltas(
				PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_COMMAND_IMAGES);
		return (imageDeltas.length != 0);
	}

	/**
	 * Reads all of the command images from the registry.
	 */
	protected final void read() {
		super.read();

		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int imageCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[1][];

		// Sort the commands extension point based on element name.
		final IConfigurationElement[] commandImagesExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_COMMAND_IMAGES);
		for (int i = 0; i < commandImagesExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = commandImagesExtensionPoint[i];
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (TAG_IMAGE.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements, INDEX_IMAGES,
						imageCount++);
			}
		}

		readImagesFromRegistry(indexedConfigurationElements[INDEX_IMAGES],
				imageCount, commandImageManager, commandService);
	}
}
