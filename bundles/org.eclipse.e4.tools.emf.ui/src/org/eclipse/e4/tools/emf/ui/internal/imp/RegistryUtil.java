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
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.tools.emf.ui.common.IExtensionLookup;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.workbench.UIEvents.ApplicationElement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class RegistryUtil {

	/**
	 * 
	 * @param t
	 * @param application
	 * @param elements
	 * @return
	 */
	public static MApplicationElement[] getModelElements(Class<? extends MApplicationElement> t, MApplication application, IConfigurationElement... elements) {

		Assert.isNotNull(t);
		Assert.isNotNull(elements);
		Assert.isTrue(elements.length > 0);

		if (t.equals(MCommand.class)) {
			return getCommands(elements, application);
		} else if (t.equals(MCategory.class)) {
			return getCategories(elements);
		}
		return new MApplicationElement[0];
	}

	private static MCommand[] getCommands(IConfigurationElement[] elements, MApplication application) {

		ArrayList<MCommand> result = new ArrayList<MCommand>();

		MCommandsFactory commandsFactory = MCommandsFactory.INSTANCE;

		for (IConfigurationElement element : elements) {

			MCommand command = commandsFactory.createCommand();
			command.setCommandName(element.getAttribute("name"));
			command.setDescription(element.getAttribute("description"));
			command.setElementId(element.getAttribute("id"));
			String catId = element.getAttribute("categoryId");

			if (catId != null && catId.trim().length() > 0) {
				List<MCategory> categories = application.getCategories();
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
	public static String[] getProvidingBundles(IExtensionRegistry registry, String extensionPoint, boolean isLive) {

		IExtensionLookup service = getService(IExtensionLookup.class, null);

		if (service == null) {
			return new String[] { "No " + IExtensionLookup.class.getName() + " service found." };
		}

		ArrayList<String> result = new ArrayList<String>();

		IExtension[] extensions = service.findExtensions(extensionPoint, isLive);
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
	 * @param struct
	 * @return the array of {@link IConfigurationElement} objects that meets the
	 *         passed criteria.
	 */
	public static IConfigurationElement[] getExtensions(IExtensionRegistry registry, RegistryStruct struct, boolean isLive) {

		IExtensionLookup service = getService(IExtensionLookup.class, null);
		if (struct == null || service == null) {
			return new IConfigurationElement[0];
		}

		ArrayList<IConfigurationElement> result = new ArrayList<IConfigurationElement>();

		IExtension[] extensions = service.findExtensions(struct.getExtensionPoint(), isLive);
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

	/**
	 * This will return a structure that contains the registry information we
	 * are looking for.
	 * 
	 * @param applicationElement
	 * @return the structure that matches the extension registry to the passed
	 *         {@link ApplicationElement}
	 */
	public static RegistryStruct getStruct(Class<? extends MApplicationElement> applicationElement) {

		if (applicationElement == MCommand.class)
			return new RegistryStruct("", "org.eclipse.ui.commands", "command", "name");

		else if (applicationElement == MCategory.class)
			return new RegistryStruct("", "org.eclipse.ui.commands", "category", "name");

		return null;
	}

	private static <T> T getService(Class<T> clazz, String filter) {

		try {
			BundleContext context = FrameworkUtil.getBundle(RegistryUtil.class).getBundleContext();
			Collection<ServiceReference<T>> references;
			references = context.getServiceReferences(clazz, filter);
			for (ServiceReference<T> reference : references) {
				return context.getService(reference);
			}
		} catch (InvalidSyntaxException e) {
			// FIXME log
		}
		return null;
	}
}
