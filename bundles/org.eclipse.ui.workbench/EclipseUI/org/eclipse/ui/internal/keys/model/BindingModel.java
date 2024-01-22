/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/

package org.eclipse.ui.internal.keys.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.4
 */
public class BindingModel extends CommonModel {
	public static final String PROP_BINDING_ADD = "bindingAdd"; //$NON-NLS-1$
	public static final String PROP_BINDING_ELEMENT_MAP = "bindingElementMap"; //$NON-NLS-1$
	public static final String PROP_BINDING_FILTER = "bindingFilter"; //$NON-NLS-1$
	public static final String PROP_BINDING_REMOVE = "bindingRemove"; //$NON-NLS-1$
	public static final String PROP_BINDINGS = "bindings"; //$NON-NLS-1$
	public static final String PROP_CONFLICT_ELEMENT_MAP = "bindingConfictMap"; //$NON-NLS-1$

	static final boolean deletes(final Binding del, final Binding binding) {
		boolean deletes = true;
		deletes &= Objects.equals(del.getContextId(), binding.getContextId());
		deletes &= Objects.equals(del.getTriggerSequence(), binding.getTriggerSequence());
		if (del.getLocale() != null) {
			deletes &= Objects.equals(del.getLocale(), binding.getLocale());
		}
		if (del.getPlatform() != null) {
			deletes &= Objects.equals(del.getPlatform(), binding.getPlatform());
		}
		deletes &= (binding.getType() == Binding.SYSTEM);
		deletes &= Objects.equals(del.getParameterizedCommand(), null);

		return deletes;
	}

	private Collection<ParameterizedCommand> allParameterizedCommands;
	private BindingManager bindingManager;

	/**
	 * Holds all the {@link BindingElement} objects.
	 */
	private HashSet<BindingElement> bindingElements;

	/**
	 * A map of {@link Binding} objects to {@link BindingElement} objects.
	 */
	private Map<Binding, BindingElement> bindingToElement;

	/**
	 * A map of {@link ParameterizedCommand} objects to {@link BindingElement}
	 * objects.
	 */
	private Map<ParameterizedCommand, BindingElement> commandToElement;

	public BindingModel(KeyController kc) {
		super(kc);
	}

	/**
	 * Makes a copy of the selected element.
	 */
	public void copy() {
		BindingElement element = (BindingElement) getSelectedElement();
		copy(element);
	}

	/**
	 * Makes a copy of the
	 */
	public void copy(BindingElement element) {
		if (element == null || !(element.getModelObject() instanceof Binding)) {
			return;
		}
		BindingElement be = new BindingElement(controller);
		ParameterizedCommand parameterizedCommand = ((Binding) element.getModelObject()).getParameterizedCommand();
		be.init(parameterizedCommand);
		be.setParent(this);
		bindingElements.add(be);
		commandToElement.put(parameterizedCommand, be);
		controller.firePropertyChange(this, PROP_BINDING_ADD, null, be);
		setSelectedElement(be);
	}

	/**
	 * @return Returns the bindings.
	 */
	public HashSet<BindingElement> getBindings() {
		return bindingElements;
	}

	/**
	 * @return Returns the bindingToElement.
	 */
	public Map<Binding, BindingElement> getBindingToElement() {
		return bindingToElement;
	}

	/**
	 * @return Returns the commandToElement.
	 */
	public Map<ParameterizedCommand, BindingElement> getCommandToElement() {
		return commandToElement;
	}

	/**
	 * The initialization only.
	 */
	public void init(IServiceLocator locator, BindingManager manager, ContextModel model) {
		Set<ParameterizedCommand> cmdsForBindings = new HashSet<>();
		bindingToElement = new HashMap<>();
		commandToElement = new HashMap<>();

		bindingElements = new HashSet<>();
		bindingManager = manager;

		Iterator<?> i = manager.getActiveBindingsDisregardingContextFlat().iterator();
		while (i.hasNext()) {
			Binding b = (Binding) i.next();
			BindingElement be = new BindingElement(controller);
			be.init(b, model);
			be.setParent(this);
			bindingElements.add(be);
			bindingToElement.put(b, be);
			cmdsForBindings.add(b.getParameterizedCommand());
		}

		ICommandService commandService = locator.getService(ICommandService.class);
		final Collection<?> commandIds = commandService.getDefinedCommandIds();
		allParameterizedCommands = new HashSet<>();
		final Iterator<?> commandIdItr = commandIds.iterator();
		while (commandIdItr.hasNext()) {
			final String currentCommandId = (String) commandIdItr.next();
			final Command currentCommand = commandService.getCommand(currentCommandId);
			try {
				allParameterizedCommands.addAll(ParameterizedCommand.generateCombinations(currentCommand));
			} catch (final NotDefinedException e) {
				// It is safe to just ignore undefined commands.
			}
		}

		Iterator<ParameterizedCommand> ii = allParameterizedCommands.iterator();
		while (ii.hasNext()) {
			ParameterizedCommand cmd = ii.next();
			if (!cmdsForBindings.contains(cmd)) {
				BindingElement be = new BindingElement(controller);
				be.init(cmd);
				be.setParent(this);
				bindingElements.add(be);
				commandToElement.put(cmd, be);
			}
		}
	}

	/**
	 * Refreshes the binding model to be in sync with the {@link BindingManager}.
	 */
	public void refresh(ContextModel contextModel) {
		Set<Object> cmdsForBindings = new HashSet<>();
		Collection<?> activeManagerBindings = bindingManager.getActiveBindingsDisregardingContextFlat();

		// add any bindings that we don't already have.
		Iterator<?> i = activeManagerBindings.iterator();
		while (i.hasNext()) {
			KeyBinding b = (KeyBinding) i.next();
			ParameterizedCommand parameterizedCommand = b.getParameterizedCommand();
			cmdsForBindings.add(parameterizedCommand);
			if (!bindingToElement.containsKey(b)) {
				BindingElement be = new BindingElement(controller);
				be.init(b, contextModel);
				be.setParent(this);
				bindingElements.add(be);
				bindingToElement.put(b, be);
				controller.firePropertyChange(this, PROP_BINDING_ADD, null, be);

				if (commandToElement.containsKey(parameterizedCommand)
						&& be.getUserDelta().intValue() == Binding.SYSTEM) {
					Object remove = commandToElement.remove(parameterizedCommand);
					bindingElements.remove(remove);
					controller.firePropertyChange(this, PROP_BINDING_REMOVE, null, remove);
				}
			}
		}

		// remove bindings that shouldn't be there
		i = bindingElements.iterator();
		while (i.hasNext()) {
			BindingElement be = (BindingElement) i.next();
			Object obj = be.getModelObject();
			if (obj instanceof Binding) {
				Binding b = (Binding) obj;
				if (!activeManagerBindings.contains(b)) {
					ParameterizedCommand cmd = b.getParameterizedCommand();
					if (cmd != null) {
						commandToElement.remove(cmd);
					}
					bindingToElement.remove(b);
					i.remove();
					controller.firePropertyChange(this, PROP_BINDING_REMOVE, null, be);
				}
			} else {
				cmdsForBindings.add(obj);
			}
		}

		// If we removed the last binding for a parameterized command,
		// put back the CMD
		i = allParameterizedCommands.iterator();
		while (i.hasNext()) {
			ParameterizedCommand cmd = (ParameterizedCommand) i.next();
			if (!cmdsForBindings.contains(cmd)) {
				BindingElement be = new BindingElement(controller);
				be.init(cmd);
				be.setParent(this);
				bindingElements.add(be);
				commandToElement.put(cmd, be);
				controller.firePropertyChange(this, PROP_BINDING_ADD, null, be);
			}
		}
	}

	/**
	 * Removes the selected element's binding
	 */
	public void remove() {
		BindingElement element = (BindingElement) getSelectedElement();
		remove(element);
	}

	/**
	 * Removes the <code>bindingElement</code> binding.
	 */
	public void remove(BindingElement bindingElement) {
		if (bindingElement == null || !(bindingElement.getModelObject() instanceof Binding)) {
			return;
		}
		KeyBinding keyBinding = (KeyBinding) bindingElement.getModelObject();
		if (keyBinding.getType() == Binding.USER) {
			bindingManager.removeBinding(keyBinding);
		} else {
			KeySequence keySequence = keyBinding.getKeySequence();

			// Add the delete binding
			bindingManager.addBinding(new KeyBinding(keySequence, null, keyBinding.getSchemeId(),
					keyBinding.getContextId(), null, null, null, Binding.USER));

			// Unbind any conflicts affected by the delete binding
			ConflictModel conflictModel = controller.getConflictModel();
			conflictModel.updateConflictsFor(bindingElement);
			Collection<?> conflictsList = conflictModel.getConflicts();
			if (conflictsList != null) {
				Object[] conflicts = conflictsList.toArray();
				for (Object conflict : conflicts) {
					BindingElement be = (BindingElement) conflict;
					if (be == bindingElement) {
						continue;
					}
					Object modelObject = be.getModelObject();
					if (modelObject instanceof Binding) {
						Binding binding = (Binding) modelObject;
						if (binding.getType() != Binding.SYSTEM) {
							continue;
						}
						ParameterizedCommand pCommand = binding.getParameterizedCommand();
						be.fill(pCommand);
						commandToElement.put(pCommand, be);
					}
				}
			}
		}
		ParameterizedCommand parameterizedCommand = keyBinding.getParameterizedCommand();
		bindingElement.fill(parameterizedCommand);
		commandToElement.put(parameterizedCommand, bindingElement);
		controller.firePropertyChange(this, PROP_CONFLICT_ELEMENT_MAP, null, bindingElement);
	}

	/**
	 * Restores the specified BindingElement. A refresh should be performed
	 * afterwards. The refresh may be done after several elements have been
	 * restored.
	 */
	public void restoreBinding(BindingElement element) {
		if (element == null) {
			return;
		}

		Object modelObject = element.getModelObject();

		ParameterizedCommand cmd = null;
		if (modelObject instanceof ParameterizedCommand) {
			cmd = (ParameterizedCommand) modelObject;
			TriggerSequence trigger = bindingManager.getBestActiveBindingFor(cmd.getId());
			Binding binding = bindingManager.getPerfectMatch(trigger);
			if (binding != null && binding.getType() == Binding.SYSTEM) {
				return;
			}
		} else if (modelObject instanceof KeyBinding) {
			cmd = ((KeyBinding) modelObject).getParameterizedCommand();
		}

		// Remove any USER bindings
		Binding[] managerBindings = bindingManager.getBindings();
		ArrayList<Binding> systemBindings = new ArrayList<>();
		ArrayList<Binding> removalBindings = new ArrayList<>();
		for (Binding managerBinding : managerBindings) {
			if (managerBinding.getParameterizedCommand() == null) {
				removalBindings.add(managerBinding);
			} else if (managerBinding.getParameterizedCommand().equals(cmd)) {
				if (managerBinding.getType() == Binding.USER) {
					bindingManager.removeBinding(managerBinding);
				} else if (managerBinding.getType() == Binding.SYSTEM) {
					systemBindings.add(managerBinding);
				}
			}
		}

		// Clear the USER bindings for parameterized commands
		Iterator<Binding> i = systemBindings.iterator();
		while (i.hasNext()) {
			Binding sys = i.next();
			Iterator<Binding> j = removalBindings.iterator();
			while (j.hasNext()) {
				Binding del = j.next();
				if (deletes(del, sys) && del.getType() == Binding.USER) {
					bindingManager.removeBinding(del);
				}
			}
		}

		setSelectedElement(null);

		bindingElements.remove(element);
		bindingToElement.remove(modelObject);
		commandToElement.remove(modelObject);
		controller.firePropertyChange(this, PROP_BINDING_REMOVE, null, element);
	}

	/**
	 * Restores the currently selected binding.
	 */
	public void restoreBinding(ContextModel contextModel) {
		BindingElement element = (BindingElement) getSelectedElement();

		if (element == null) {
			return;
		}

		restoreBinding(element);
		refresh(contextModel);

		Object obj = element.getModelObject();
		ParameterizedCommand cmd = null;
		if (obj instanceof ParameterizedCommand) {
			cmd = (ParameterizedCommand) obj;
		} else if (obj instanceof KeyBinding) {
			cmd = ((KeyBinding) obj).getParameterizedCommand();
		}

		boolean done = false;
		Iterator<BindingElement> i = bindingElements.iterator();
		// Reselects the command
		while (i.hasNext() && !done) {
			BindingElement be = i.next();
			obj = be.getModelObject();
			ParameterizedCommand pcmd = null;
			if (obj instanceof ParameterizedCommand) {
				pcmd = (ParameterizedCommand) obj;
			} else if (obj instanceof KeyBinding) {
				pcmd = ((KeyBinding) obj).getParameterizedCommand();
			}
			if (cmd.equals(pcmd)) {
				done = true;
				setSelectedElement(be);
			}
		}
	}

	/**
	 * @param bindings The bindings to set.
	 */
	public void setBindings(HashSet<BindingElement> bindings) {
		HashSet<BindingElement> old = this.bindingElements;
		this.bindingElements = bindings;
		controller.firePropertyChange(this, PROP_BINDINGS, old, bindings);
	}

	/**
	 * @param bindingToElement The bindingToElement to set.
	 */
	public void setBindingToElement(Map<Binding, BindingElement> bindingToElement) {
		Map<Binding, BindingElement> old = this.bindingToElement;
		this.bindingToElement = bindingToElement;
		controller.firePropertyChange(this, PROP_BINDING_ELEMENT_MAP, old, bindingToElement);
	}
}
