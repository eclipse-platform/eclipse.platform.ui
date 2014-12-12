/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.menu.tests;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.menus.UIElement;

class CmdService implements ICommandService {

	private Category category;

	@Inject
	private CommandManager manager;

	@Override
	public void addExecutionListener(IExecutionListener listener) {
	}

	@Override
	public void defineUncategorizedCategory(String name, String description) {
	}

	@Override
	public ParameterizedCommand deserialize(
			String serializedParameterizedCommand) throws NotDefinedException,
			SerializationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public Category getCategory(String categoryId) {
		return manager.getCategory(categoryId);
	}

	@Override
	public Command getCommand(String commandId) {
		Command cmd = manager.getCommand(commandId);
		if (!cmd.isDefined()) {
			cmd.define(commandId, null, category);
		}
		return cmd;
	}

	@Override
	public Category[] getDefinedCategories() {
		return manager.getDefinedCategories();
	}

	@Override
	public Collection getDefinedCategoryIds() {
		return manager.getDefinedCategoryIds();
	}

	@Override
	public Collection getDefinedCommandIds() {
		return manager.getDefinedCommandIds();
	}

	@Override
	public Command[] getDefinedCommands() {
		return manager.getDefinedCommands();
	}

	@Override
	public Collection getDefinedParameterTypeIds() {
		return manager.getDefinedParameterTypeIds();
	}

	@Override
	public ParameterType[] getDefinedParameterTypes() {
		return manager.getDefinedParameterTypes();
	}

	@Override
	public String getHelpContextId(Command command) throws NotDefinedException {
		return null;
	}

	@Override
	public String getHelpContextId(String commandId) throws NotDefinedException {
		return null;
	}

	@Override
	public ParameterType getParameterType(String parameterTypeId) {
		return manager.getParameterType(parameterTypeId);
	}

	@PostConstruct
	public void init() {
		category = manager.getCategory("fake.category");
		category.define("Fake Cat", null);
	}

	@Override
	public void readRegistry() {
	}

	@Override
	public void refreshElements(String commandId, Map filter) {
	}

	@Override
	public void registerElement(IElementReference elementReference) {
	}

	@Override
	public IElementReference registerElementForCommand(
			ParameterizedCommand command, UIElement element)
			throws NotDefinedException {
		return null;
	}

	@Override
	public void removeExecutionListener(IExecutionListener listener) {
	}

	@Override
	public void setHelpContextId(IHandler handler, String helpContextId) {
	}

	@Override
	public void unregisterElement(IElementReference elementReference) {
	}

}