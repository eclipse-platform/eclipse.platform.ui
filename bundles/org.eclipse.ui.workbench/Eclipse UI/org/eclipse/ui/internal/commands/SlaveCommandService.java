/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.IUpdateService;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A command service which delegates almost all responsibility to the parent
 * service.
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 *
 * @since 3.2
 */
public class SlaveCommandService implements ICommandService, IUpdateService {

	private Collection fExecutionListeners = new ArrayList();

	/**
	 * The collection of ICallbackReferences added through this service.
	 *
	 * @since 3.3
	 */
	private Set fCallbackCache = new HashSet();

	private ICommandService fParentService;

	/**
	 * The scoping constant added to callback registrations submitted through
	 * this service.
	 *
	 * @since 3.3
	 */
	private String fScopingName;

	/**
	 * The object to scope. In theory, the service locator that would find this
	 * service.
	 *
	 * @since 3.3
	 */
	private IServiceLocator fScopingValue;

	private IEclipseContext fContext;

	/**
	 * Build the slave service.
	 *
	 * @param parent
	 *            the parent service. This must not be <code>null</code>.
	 */
	public SlaveCommandService(ICommandService parent, String scopeName, IServiceLocator scopeValue) {
		this(parent, scopeName, scopeValue, null);
	}

	public SlaveCommandService(ICommandService parent, String scopeName,
			IServiceLocator scopeValue,
			IEclipseContext context) {
		if (parent == null) {
			throw new NullPointerException(
					"The parent command service must not be null"); //$NON-NLS-1$
		}
		fParentService = parent;
		fScopingName = scopeName;
		fScopingValue = scopeValue;
		fContext = context;
	}

	@Override
	public void addExecutionListener(IExecutionListener listener) {
		if (!fExecutionListeners.contains(listener)) {
			fExecutionListeners.add(listener);
		}
		fParentService.addExecutionListener(listener);
	}

	@Override
	public void defineUncategorizedCategory(String name, String description) {
		fParentService.defineUncategorizedCategory(name, description);
	}

	@Override
	public ParameterizedCommand deserialize(
			String serializedParameterizedCommand) throws NotDefinedException,
			SerializationException {
		return fParentService.deserialize(serializedParameterizedCommand);
	}

	@Override
	public void dispose() {
		if (!fExecutionListeners.isEmpty()) {
			Object[] array = fExecutionListeners.toArray();
			for (Object element : array) {
				removeExecutionListener((IExecutionListener) element);
			}
			fExecutionListeners.clear();
		}
		if (!fCallbackCache.isEmpty()) {
			Object[] array = fCallbackCache.toArray();
			for (Object element : array) {
				unregisterElement((IElementReference) element);
			}
		}
	}

	@Override
	public Category getCategory(String categoryId) {
		return fParentService.getCategory(categoryId);
	}

	@Override
	public Command getCommand(String commandId) {
		return fParentService.getCommand(commandId);
	}

	@Override
	public Category[] getDefinedCategories() {
		return fParentService.getDefinedCategories();
	}

	@Override
	public Collection getDefinedCategoryIds() {
		return fParentService.getDefinedCategoryIds();
	}

	@Override
	public Collection getDefinedCommandIds() {
		return fParentService.getDefinedCommandIds();
	}

	@Override
	public Command[] getDefinedCommands() {
		return fParentService.getDefinedCommands();
	}

	@Override
	public Collection getDefinedParameterTypeIds() {
		return fParentService.getDefinedParameterTypeIds();
	}

	@Override
	public ParameterType[] getDefinedParameterTypes() {
		return fParentService.getDefinedParameterTypes();
	}

	@Override
	public final String getHelpContextId(final Command command)
			throws NotDefinedException {
		return fParentService.getHelpContextId(command);
	}

	@Override
	public final String getHelpContextId(final String commandId)
			throws NotDefinedException {
		return fParentService.getHelpContextId(commandId);
	}

	@Override
	public ParameterType getParameterType(String parameterTypeId) {
		return fParentService.getParameterType(parameterTypeId);
	}

	@Override
	public void readRegistry() {
		fParentService.readRegistry();
	}

	@Override
	public void removeExecutionListener(IExecutionListener listener) {
		fExecutionListeners.remove(listener);
		fParentService.removeExecutionListener(listener);
	}

	@Override
	public final void setHelpContextId(final IHandler handler,
			final String helpContextId) {
		fParentService.setHelpContextId(handler, helpContextId);
	}

	@Override
	public void refreshElements(String commandId, Map filter) {
		fParentService.refreshElements(commandId, filter);
	}

	@Override
	public IElementReference registerElementForCommand(
			ParameterizedCommand command, UIElement element)
			throws NotDefinedException {
		if (!command.getCommand().isDefined()) {
			throw new NotDefinedException(
					"Cannot define a callback for undefined command " //$NON-NLS-1$
							+ command.getCommand().getId());
		}
		if (element == null) {
			throw new NotDefinedException("No callback defined for command " //$NON-NLS-1$
					+ command.getCommand().getId());
		}

		ElementReference ref = new ElementReference(command.getId(), element,
				command.getParameterMap());
		registerElement(ref);
		return ref;
	}

	@Override
	public void registerElement(IElementReference elementReference) {
		fCallbackCache.add(elementReference);
		elementReference.getParameters().put(fScopingName, fScopingValue);
		fParentService.registerElement(elementReference);
	}

	@Override
	public void unregisterElement(IElementReference elementReference) {
		fCallbackCache.remove(elementReference);
		fParentService.unregisterElement(elementReference);
	}

	@Override
	public Runnable registerElementForUpdate(ParameterizedCommand parameterizedCommand,
			final MItem item) {
		UIElement element = new UIElement(fScopingValue) {

			@Override
			public void setText(String text) {
				item.setLabel(text);
			}

			@Override
			public void setTooltip(String text) {
				item.setTooltip(text);
			}

			@Override
			public void setIcon(ImageDescriptor desc) {
				item.setIconURI(MenuHelper.getIconURI(desc, fContext));
			}

			@Override
			public void setDisabledIcon(ImageDescriptor desc) {
				item.getTransientData().put(IPresentationEngine.DISABLED_ICON_IMAGE_KEY,
						MenuHelper.getIconURI(desc, fContext));
			}

			@Override
			public void setHoverIcon(ImageDescriptor desc) {
				// ignored
			}

			@Override
			public void setChecked(boolean checked) {
				item.setSelected(checked);
			}
		};

		try {
			final IElementReference reference = registerElementForCommand(parameterizedCommand,
					element);
			return () -> unregisterElement(reference);
		} catch (NotDefinedException e) {
			WorkbenchPlugin.log(e);
		}
		return null;
	}
}
