/*******************************************************************************
 * Copyright (c) 2013 Remain BV, Industrial-TSI BV and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wim Jongmam <wim.jongman@remainsoftware.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.imp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.PDEExtensionRegistry;

public class RegistryUtil {

	/**
	 * 
	 * @param t
	 * @param editingDomain
	 * @param elements
	 * @return
	 */
	public static MApplicationElement[] getModelElements(Class<? extends MApplicationElement> t, EditingDomain editingDomain, IConfigurationElement... elements) {

		Assert.isNotNull(t);
		Assert.isNotNull(elements);
		Assert.isTrue(elements.length > 0);

		if (t.equals(MCommand.class)) {
			return getCommands(elements, editingDomain);
		} else if (t.equals(MCategory.class)) {
			return getCategories(elements);
		}
		return new MApplicationElement[0];
	}

	private static MCommand[] getCommands(IConfigurationElement[] elements, EditingDomain editingDomain) {

		ArrayList<MCommand> result = new ArrayList<MCommand>();

		MCommandsFactory commandsFactory = MCommandsFactory.INSTANCE;

		for (IConfigurationElement element : elements) {

			MCommand command = commandsFactory.createCommand();
			command.setCommandName(element.getAttribute("name"));
			command.setDescription(element.getAttribute("description"));
			command.setElementId(element.getAttribute("id"));
			String catId = element.getAttribute("categoryId");

			if (catId != null || catId.trim().length() == 0) {
				MApplication app = (MApplication) editingDomain.getResourceSet().getResources().get(0).getContents().get(0);
				List<MCategory> categories = app.getCategories();
				for (MCategory category : categories) {
					if (category.getElementId().equals(catId)) {
						command.setCategory(category);
						break;
					}
				}
			}

			result.add((MCommand) command);
		}

		return result.toArray(new MCommand[0]);
	}

	private static MCategory[] getCategories(IConfigurationElement[] elements) {

		ArrayList<MCategory> result = new ArrayList<MCategory>();

		MCommandsFactory commandsFactory = MCommandsFactory.INSTANCE;

		for (IConfigurationElement element : elements) {

			MCategory category = commandsFactory.createCategory();
			category.setDescription(element.getAttribute("description"));
			category.setElementId(element.getAttribute("id"));
			category.setName(element.getAttribute("name"));

			result.add(category);
		}

		return result.toArray(new MCategory[0]);
	}

	/**
	 * Returns a list of bundle id's that have extension to the passed extension
	 * point.
	 * 
	 * @param registry
	 * @param extensionPoint
	 * @return
	 */
	public static String[] getProvidingBundles(IExtensionRegistry registry, String extensionPoint) {

		IPluginModelBase[] models = PluginRegistry.getWorkspaceModels();
		PDEExtensionRegistry reg = new PDEExtensionRegistry(models);
		ArrayList<String> result = new ArrayList<String>();

		IExtension[] extensions = reg.findExtensions(extensionPoint, true);
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (!result.contains(element.getContributor().getName())) {
					result.add(element.getContributor().getName());
				}
			}
		}

		String[] resultArray = result.toArray(new String[0]);
		Arrays.sort(resultArray);
		return resultArray;
	}

	/**
	 * 
	 * @param registry
	 * @param MApplicationElement
	 * @return the array of {@link IConfigurationElement} objects that meets the
	 *         passed criteria.
	 */
	public static IConfigurationElement[] getExtensions(IExtensionRegistry registry, RegistryStruct struct) {

		if (struct == null) {
			return new IConfigurationElement[0];
		}

		IPluginModelBase[] models = PluginRegistry.getWorkspaceModels();
		PDEExtensionRegistry reg = new PDEExtensionRegistry(models);
		ArrayList<IConfigurationElement> result = new ArrayList<IConfigurationElement>();

		IExtension[] extensions = reg.findExtensions(struct.getExtensionPoint(), true);
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getContributor().getName().equals(struct.getBundle())) {
					if (element.getName().equals(struct.getExtensionPointName())) {
						result.add(element);
					}
				}
			}
		}

		return result.toArray(new IConfigurationElement[0]);
	}

	public static RegistryStruct getStruct(Class<? extends MApplicationElement> applicationElement) {

		if (applicationElement == MCommand.class)
			return new RegistryStruct("", "org.eclipse.ui.commands", "command", "name");

		else if (applicationElement == MCategory.class)
			return new RegistryStruct("", "org.eclipse.ui.commands", "category", "name");

		return null;
	}

}
