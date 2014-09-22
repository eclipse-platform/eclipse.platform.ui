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
 *     Steven Spungin <steven@spungin.tv> - Ongoing Maintenance
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
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MInputPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.UIEvents.ApplicationElement;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("deprecation")
public class RegistryUtil {

	private static final String PLATFORM = "platform:"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String COMMAND_ID = "commandId"; //$NON-NLS-1$
	private static final String CATEGORY_ID = "categoryId"; //$NON-NLS-1$
	private static final String COMPATIBILITY_VIEW = "bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"; //$NON-NLS-1$
	private static final String VIEW_MENU = "ViewMenu"; //$NON-NLS-1$
	private static final String LAUNCHER = "launcher"; //$NON-NLS-1$
	private static final String EDITOR = "editor"; //$NON-NLS-1$
	private static final String VIEW = "View"; //$NON-NLS-1$
	private static final String CATEGORY_TAG = "categoryTag:"; //$NON-NLS-1$
	private static final String CATEGORY = "category"; //$NON-NLS-1$
	private static final String CLASS = "class"; //$NON-NLS-1$
	private static final String DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String ICON = "icon"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	public final static String HINT_VIEW = "view"; //$NON-NLS-1$
	public final static String HINT_EDITOR = EDITOR;
	public final static String HINT_COMPAT_VIEW = "compatibilityView"; //$NON-NLS-1$

	/**
	 *
	 * @param t
	 * @param application
	 * @param elements
	 * @return
	 */
	public static MApplicationElement[] getModelElements(Class<? extends MApplicationElement> t, String hint, MApplication application, IConfigurationElement... elements) {

		Assert.isNotNull(t);
		Assert.isNotNull(elements);
		Assert.isTrue(elements.length > 0);

		if (t.equals(MCommand.class)) {
			return getCommands(elements, application);
		} else if (t.equals(MCategory.class)) {
			return getCategories(elements);
		} else if (t.equals(MPerspective.class)) {
			return getPerspectives(elements);
		} else if (t.equals(MPart.class) && HINT_COMPAT_VIEW.equals(hint)) {
			return getViewsAsCompatibilityViews(elements);
		} else if (t.equals(MPart.class)) {
			return getViews(elements);
		} else if (t.equals(MInputPart.class)) {
			return getEditors(elements);
		} else if (t.equals(MHandler.class)) {
			return getHandlers(elements, application);
		} else if (t.equals(MPartDescriptor.class) && HINT_EDITOR.equals(hint)) {
			return getEditorPartDescriptors(elements);
		} else if (t.equals(MPartDescriptor.class) && HINT_VIEW.equals(hint)) {
			return getViewPartDescriptors(elements);
		} else if (t.equals(MPartDescriptor.class) && HINT_COMPAT_VIEW.equals(hint)) {
			return getPartDescriptorsAsCompatibilyViews(elements);
		}
		return new MApplicationElement[0];
	}

	private static MCommand[] getCommands(IConfigurationElement[] elements, MApplication application) {

		ArrayList<MCommand> result = new ArrayList<MCommand>();

		MCommandsFactory commandsFactory = MCommandsFactory.INSTANCE;

		for (IConfigurationElement element : elements) {

			MCommand command = commandsFactory.createCommand();
			command.setCommandName(element.getAttribute(NAME));
			command.setDescription(element.getAttribute(DESCRIPTION));
			command.setElementId(element.getAttribute(ID));
			String catId = element.getAttribute(CATEGORY_ID);

			if (catId != null && catId.trim().length() > 0) {
				List<MCategory> categories = application.getCategories();
				for (MCategory category : categories) {
					if (category.getElementId().equals(catId)) {
						command.setCategory(category);
						break;
					}
				}
			}

			result.add(command);
		}

		return result.toArray(new MCommand[0]);
	}

	private static MPerspective[] getPerspectives(IConfigurationElement[] elements) {

		ArrayList<MPerspective> result = new ArrayList<MPerspective>();

		MAdvancedFactory factory = MAdvancedFactory.INSTANCE;

		for (IConfigurationElement element : elements) {
			MPerspective perspective = factory.createPerspective();
			perspective.setLabel(element.getAttribute(NAME));
			perspective.setIconURI(getIconURI(element, ICON));
			perspective.setElementId(element.getAttribute(ID));
			perspective.setToBeRendered(true);
			perspective.setVisible(true);
			result.add(perspective);
		}

		return result.toArray(new MPerspective[0]);
	}

	private static MCategory[] getCategories(IConfigurationElement[] elements) {

		ArrayList<MCategory> result = new ArrayList<MCategory>();

		MCommandsFactory commandsFactory = MCommandsFactory.INSTANCE;

		for (IConfigurationElement element : elements) {

			MCategory category = commandsFactory.createCategory();
			category.setDescription(element.getAttribute(DESCRIPTION));
			category.setElementId(element.getAttribute(ID));
			category.setName(element.getAttribute(NAME));

			result.add(category);
		}

		return result.toArray(new MCategory[0]);
	}

	private static MPart[] getViews(IConfigurationElement[] elements) {

		ArrayList<MPart> result = new ArrayList<MPart>();
		for (IConfigurationElement element : elements) {
			MPart part = (MPart) EcoreUtil.create(BasicPackageImpl.Literals.PART);
			part.setElementId(element.getAttribute(ID));
			part.setLabel(element.getAttribute(NAME));
			part.setIconURI(getIconURI(element, ICON));
			part.setContributionURI(getContributionURI(element, CLASS));
			part.setToBeRendered(true);
			part.setVisible(true);
			part.setToolbar(createToolBar(part));
			part.getMenus().add(createViewMenu(part));
			part.setCloseable(true);
			part.getTags().add(VIEW);
			if (element.getAttribute(CATEGORY) != null) {
				part.getTags().add(CATEGORY_TAG + element.getAttribute(CATEGORY));
			}

			result.add(part);
		}
		return result.toArray(new MPart[0]);
	}

	private static MToolBar createToolBar(MPart part) {
		MToolBar toolBar = MMenuFactory.INSTANCE.createToolBar();
		toolBar.setElementId(part.getElementId());
		return toolBar;
	}

	private static MMenu createViewMenu(MPart part) {
		MMenu menu = MMenuFactory.INSTANCE.createMenu();
		menu.setElementId(part.getElementId());
		menu.getTags().add(VIEW_MENU);
		return menu;
	}

	private static MPart[] getViewsAsCompatibilityViews(IConfigurationElement[] elements) {
		ArrayList<MPart> result = new ArrayList<MPart>();
		MPart[] parts = getViews(elements);
		for (MPart part : parts) {
			part.setContributionURI(COMPATIBILITY_VIEW);
			result.add(part);
		}
		return result.toArray(new MPart[0]);
	}

	private static MPartDescriptor[] getPartDescriptorsAsCompatibilyViews(IConfigurationElement[] elements) {
		ArrayList<MPartDescriptor> result = new ArrayList<MPartDescriptor>();
		MPartDescriptor[] parts = getViewPartDescriptors(elements);
		for (MPartDescriptor part : parts) {
			part.setContributionURI(COMPATIBILITY_VIEW);
			result.add(part);
		}
		return result.toArray(new MPartDescriptor[0]);
	}

	private static MPart[] getEditors(IConfigurationElement[] elements) {

		ArrayList<MPart> result = new ArrayList<MPart>();
		for (IConfigurationElement element : elements) {
			if ("editor".equals(element.getName())) /* Sanity Check */{ //$NON-NLS-1$
				MPart part = (MPart) EcoreUtil.create(BasicPackageImpl.Literals.PART);
				part.setElementId(element.getAttribute("id")); //$NON-NLS-1$
				part.setLabel(element.getAttribute("name")); //$NON-NLS-1$
				part.setIconURI(getIconURI(element, "icon")); //$NON-NLS-1$
				if (element.getAttribute("class") != null) { //$NON-NLS-1$
					part.setContributionURI(getContributionURI(element, "class")); //$NON-NLS-1$
				} else {
					part.setContributionURI(getContributionURI(element, "launcher")); //$NON-NLS-1$
				}
				part.setToBeRendered(true);
				part.setVisible(true);
				part.setToolbar(createToolBar(part));
				part.getMenus().add(createViewMenu(part));
				part.setCloseable(true);
				result.add(part);
			}
		}

		return result.toArray(new MPart[0]);
	}

	private static MPartDescriptor[] getEditorPartDescriptors(IConfigurationElement[] elements) {

		ArrayList<MPartDescriptor> result = new ArrayList<MPartDescriptor>();
		for (IConfigurationElement element : elements) {
			MPartDescriptor part = (MPartDescriptor) EcoreUtil.create(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.Literals.PART_DESCRIPTOR);
			part.setElementId(element.getAttribute(ID));
			part.setLabel(element.getAttribute(NAME));
			part.setIconURI(getIconURI(element, ICON));
			if (element.getAttribute(CLASS) != null) {
				part.setContributionURI(getContributionURI(element, CLASS));
			} else {
				part.setContributionURI(getContributionURI(element, LAUNCHER));
			}
			part.setDirtyable(true);
			part.setAllowMultiple(true);

			MToolBar toolBar = MMenuFactory.INSTANCE.createToolBar();
			toolBar.setElementId(part.getElementId());
			part.setToolbar(toolBar);

			MMenu menu = MMenuFactory.INSTANCE.createMenu();
			menu.setElementId(part.getElementId());
			menu.getTags().add(VIEW_MENU);
			part.getMenus().add(menu);

			part.setCloseable(true);
			result.add(part);
		}

		return result.toArray(new MPartDescriptor[0]);
	}

	private static MPartDescriptor[] getViewPartDescriptors(IConfigurationElement[] elements) {

		ArrayList<MPartDescriptor> result = new ArrayList<MPartDescriptor>();
		for (IConfigurationElement element : elements) {
			MPartDescriptor part = (MPartDescriptor) EcoreUtil.create(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.Literals.PART_DESCRIPTOR);
			part.setElementId(element.getAttribute(ID));
			part.setLabel(element.getAttribute(NAME));
			part.setIconURI(getIconURI(element, ICON));

			MToolBar toolBar = MMenuFactory.INSTANCE.createToolBar();
			toolBar.setElementId(part.getElementId());
			part.setToolbar(toolBar);

			MMenu menu = MMenuFactory.INSTANCE.createMenu();
			menu.setElementId(part.getElementId());
			menu.getTags().add(VIEW_MENU);
			part.getMenus().add(menu);

			part.setCloseable(true);
			result.add(part);
		}

		return result.toArray(new MPartDescriptor[0]);
	}

	private static MHandler[] getHandlers(IConfigurationElement[] elements, MApplication application) {

		ArrayList<MHandler> result = new ArrayList<MHandler>();
		for (IConfigurationElement element : elements) {
			MHandler hand = MCommandsFactory.INSTANCE.createHandler();
			hand.setElementId(element.getAttribute(ID));
			hand.setContributionURI(getContributionURI(element, CLASS));

			String cmdId = element.getAttribute(COMMAND_ID);

			if (cmdId != null && cmdId.trim().length() > 0) {
				List<MCommand> categories = application.getCommands();
				for (MCommand command : categories) {
					if (command.getElementId().equals(cmdId)) {
						hand.setCommand(command);
						break;
					}
				}
			}
			result.add(hand);
		}
		return result.toArray(new MHandler[0]);
	}

	private static String getIconURI(IConfigurationElement element, String attribute) {
		if (element.getAttribute(attribute) == null) {
			return EMPTY_STRING;
		}
		// FIXME any other cases?
		if (element.getAttribute(attribute).startsWith(PLATFORM)) {
			return element.getAttribute(attribute);
		}
		return "platform:/plugin/" + element.getContributor().getName() + "/" + element.getAttribute(attribute); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String getContributionURI(IConfigurationElement element, String attribute) {
		return "bundleclass://" + element.getContributor().getName() + "/" + element.getAttribute(attribute); //$NON-NLS-1$ //$NON-NLS-2$
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
			return new String[] { "No " + IExtensionLookup.class.getName() + " service found." }; //$NON-NLS-1$ //$NON-NLS-2$
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
	public static RegistryStruct getStruct(Class<? extends MApplicationElement> applicationElement, String hint) {

		if (applicationElement == MCommand.class)
			return new RegistryStruct(EMPTY_STRING, "org.eclipse.ui.commands", "command", NAME); //$NON-NLS-1$ //$NON-NLS-2$

		else if (applicationElement == MCategory.class)
			return new RegistryStruct(EMPTY_STRING, "org.eclipse.ui.commands", CATEGORY, NAME); //$NON-NLS-1$

		else if (applicationElement == MPerspective.class)
			return new RegistryStruct(EMPTY_STRING, "org.eclipse.ui.perspectives", "perspective", NAME); //$NON-NLS-1$ //$NON-NLS-2$

		else if (applicationElement == MPart.class)
			return new RegistryStruct(EMPTY_STRING, "org.eclipse.ui.views", "view", NAME); //$NON-NLS-1$ //$NON-NLS-2$

		else if (applicationElement == MHandler.class)
			return new RegistryStruct(EMPTY_STRING, "org.eclipse.ui.handlers", "handler", COMMAND_ID); //$NON-NLS-1$ //$NON-NLS-2$

		else if (applicationElement == MPart.class) {
			return new RegistryStruct(EMPTY_STRING, "org.eclipse.ui.views", "view", NAME); //$NON-NLS-1$ //$NON-NLS-2$
		}

		else if (applicationElement == MInputPart.class) {
			return new RegistryStruct("", "org.eclipse.ui.editors", "editor", "name"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		else if (applicationElement == MPartDescriptor.class) {
			if (hint == HINT_EDITOR)
				return new RegistryStruct(EMPTY_STRING, "org.eclipse.ui.editors", EDITOR, NAME); //$NON-NLS-1$
			if (hint == HINT_VIEW || hint == HINT_COMPAT_VIEW)
				return new RegistryStruct(EMPTY_STRING, "org.eclipse.ui.views", "view", NAME); //$NON-NLS-1$ //$NON-NLS-2$
		}

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
