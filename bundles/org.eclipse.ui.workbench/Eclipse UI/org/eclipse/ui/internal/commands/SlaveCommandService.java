/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.menus.UIElement;

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
public class SlaveCommandService implements ICommandService {

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
	private Object fScopingValue;

	/**
	 * Build the slave service.
	 * 
	 * @param parent
	 *            the parent service. This must not be <code>null</code>.
	 */
	public SlaveCommandService(ICommandService parent, String scopeName,
			Object scopeValue) {
		if (parent == null) {
			throw new NullPointerException(
					"The parent command service must not be null"); //$NON-NLS-1$
		}
		fParentService = parent;
		fScopingName = scopeName;
		fScopingValue = scopeValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#addExecutionListener(org.eclipse.core.commands.IExecutionListener)
	 */
	public void addExecutionListener(IExecutionListener listener) {
		if (!fExecutionListeners.contains(listener)) {
			fExecutionListeners.add(listener);
		}
		fParentService.addExecutionListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#defineUncategorizedCategory(java.lang.String,
	 *      java.lang.String)
	 */
	public void defineUncategorizedCategory(String name, String description) {
		fParentService.defineUncategorizedCategory(name, description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#deserialize(java.lang.String)
	 */
	public ParameterizedCommand deserialize(
			String serializedParameterizedCommand) throws NotDefinedException,
			SerializationException {
		return fParentService.deserialize(serializedParameterizedCommand);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		if (!fExecutionListeners.isEmpty()) {
			Object[] array = fExecutionListeners.toArray();
			for (int i = 0; i < array.length; i++) {
				removeExecutionListener((IExecutionListener) array[i]);
			}
			fExecutionListeners.clear();
		}
		if (!fCallbackCache.isEmpty()) {
			Object[] array = fCallbackCache.toArray();
			for (int i = 0; i < array.length; i++) {
				unregisterElement((IElementReference) array[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#getCategory(java.lang.String)
	 */
	public Category getCategory(String categoryId) {
		return fParentService.getCategory(categoryId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#getCommand(java.lang.String)
	 */
	public Command getCommand(String commandId) {
		return fParentService.getCommand(commandId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#getDefinedCategories()
	 */
	public Category[] getDefinedCategories() {
		return fParentService.getDefinedCategories();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#getDefinedCategoryIds()
	 */
	public Collection getDefinedCategoryIds() {
		return fParentService.getDefinedCategoryIds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#getDefinedCommandIds()
	 */
	public Collection getDefinedCommandIds() {
		return fParentService.getDefinedCommandIds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#getDefinedCommands()
	 */
	public Command[] getDefinedCommands() {
		return fParentService.getDefinedCommands();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#getDefinedParameterTypeIds()
	 */
	public Collection getDefinedParameterTypeIds() {
		return fParentService.getDefinedParameterTypeIds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#getDefinedParameterTypes()
	 */
	public ParameterType[] getDefinedParameterTypes() {
		return fParentService.getDefinedParameterTypes();
	}

	public final String getHelpContextId(final Command command)
			throws NotDefinedException {
		return fParentService.getHelpContextId(command);
	}

	public final String getHelpContextId(final String commandId)
			throws NotDefinedException {
		return fParentService.getHelpContextId(commandId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#getParameterType(java.lang.String)
	 */
	public ParameterType getParameterType(String parameterTypeId) {
		return fParentService.getParameterType(parameterTypeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#readRegistry()
	 */
	public void readRegistry() {
		fParentService.readRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#removeExecutionListener(org.eclipse.core.commands.IExecutionListener)
	 */
	public void removeExecutionListener(IExecutionListener listener) {
		fExecutionListeners.remove(listener);
		fParentService.removeExecutionListener(listener);
	}

	public final void setHelpContextId(final IHandler handler,
			final String helpContextId) {
		fParentService.setHelpContextId(handler, helpContextId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#refreshElements(java.lang.String,
	 *      java.util.Map)
	 */
	public void refreshElements(String commandId, Map filter) {
		fParentService.refreshElements(commandId, filter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#registerElementForCommand(org.eclipse.core.commands.ParameterizedCommand,
	 *      org.eclipse.ui.menus.UIElement)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#registerElement(org.eclipse.ui.commands.IElementReference)
	 */
	public void registerElement(IElementReference elementReference) {
		fCallbackCache.add(elementReference);
		elementReference.getParameters().put(fScopingName, fScopingValue);
		fParentService.registerElement(elementReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICommandService#unregisterElement(org.eclipse.ui.commands.IElementReference)
	 */
	public void unregisterElement(IElementReference elementReference) {
		fCallbackCache.remove(elementReference);
		fParentService.unregisterElement(elementReference);
	}
}
