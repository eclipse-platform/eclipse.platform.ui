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

package org.eclipse.ui.internal.incubator;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.keys.IBindingService;

/**
 * @since 3.3
 * 
 */
public class CommandProvider extends AbstractProvider {

	private AbstractElement[] cachedElements;
	private Map idToElement = new HashMap();

	public String getId() {
		return "org.eclipse.ui.commands"; //$NON-NLS-1$
	}

	public AbstractElement getElementForId(String id) {
		getElements();
		return (CommandElement) idToElement.get(id);
	}

	public AbstractElement[] getElements() {
		if (cachedElements == null) {
			BindingService bindingService = (BindingService) PlatformUI
					.getWorkbench().getService(IBindingService.class);
			Binding[] bindings = bindingService.getBindings();
			SortedSet commandSet = new TreeSet();
			for (int i = 0; i < bindings.length; i++) {
				Binding binding = bindings[i];
				ParameterizedCommand command = binding
						.getParameterizedCommand();
				if (command != null && command.getCommand().isHandled()) {
					commandSet.add(command);
				}
			}
			ParameterizedCommand[] commands = (ParameterizedCommand[]) commandSet
					.toArray(new ParameterizedCommand[commandSet.size()]);
			cachedElements = new AbstractElement[commands.length];
			for (int i = 0; i < commands.length; i++) {
				CommandElement commandElement = new CommandElement(commands[i]);
				cachedElements[i] = commandElement;
				idToElement.put(commandElement.getId(), commandElement);
			}
		}
		return cachedElements;
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	public String getName() {
		return IncubatorMessages.CtrlEAction_Commands;
	}
}
