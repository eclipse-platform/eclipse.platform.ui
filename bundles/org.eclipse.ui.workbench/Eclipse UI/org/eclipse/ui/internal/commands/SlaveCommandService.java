/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.commands.ICommandService;

/**
 * A command service which delegates almost all responsibility to the parent
 * service.
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class SlaveCommandService implements ICommandService {

	private ICommandService fParentService;

	private Collection fExecutionListeners = new ArrayList();

	/**
	 * Build the slave service.
	 * 
	 * @param parent
	 *            the parent service. This must not be <code>null</code>.
	 */
	public SlaveCommandService(ICommandService parent) {
		if (parent == null) {
			throw new NullPointerException(
					"The parent command service must not be null"); //$NON-NLS-1$
		}
		fParentService = parent;
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
	}
}
