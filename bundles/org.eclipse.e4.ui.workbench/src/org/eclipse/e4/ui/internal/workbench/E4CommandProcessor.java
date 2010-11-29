/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingTable;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MBindingTableContainer;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 *
 */
public class E4CommandProcessor {

	public static void processBindings(IEclipseContext context,
			MBindingTableContainer bindingContainer) {
		Activator.trace(Policy.DEBUG_CMDS, "Initialize binding tables from model", null); //$NON-NLS-1$
		final BindingTableManager bindingTables = (BindingTableManager) ContextInjectionFactory
				.make(BindingTableManager.class, context);
		context.set(BindingTableManager.class.getName(), bindingTables);
		final ECommandService cs = (ECommandService) context.get(ECommandService.class.getName());
		final EBindingService bs = (EBindingService) context.get(EBindingService.class.getName());
		if (cs == null) {
			Activator
					.log(IStatus.ERROR, "cannot run without ECommandService in defineBindingTable"); //$NON-NLS-1$
			return;
		}
		if (bs == null) {
			Activator
					.log(IStatus.ERROR, "cannot run without EBindingService in defineBindingTable"); //$NON-NLS-1$
			return;
		}
		final ContextManager manager = (ContextManager) context.get(ContextManager.class.getName());
		for (MBindingTable bt : bindingContainer.getBindingTables()) {
			final Context bindingContext = manager.getContext(bt.getBindingContextId());
			final BindingTable table = new BindingTable(bindingContext);
			bindingTables.addTable(table);
			List<MKeyBinding> bindings = bt.getBindings();
			for (MKeyBinding binding : bindings) {
				Binding keyBinding = createBinding(bindingContext, cs, bs, binding.getCommand(),
						binding.getParameters(), binding.getKeySequence(), binding);
				if (keyBinding != null) {
					table.addBinding(keyBinding);
				}
			}
		}

	}

	public static void watchForBindingChanges(IEclipseContext context) {
		final ECommandService cs = (ECommandService) context.get(ECommandService.class.getName());
		final EBindingService bs = (EBindingService) context.get(EBindingService.class.getName());
		final ContextManager manager = (ContextManager) context.get(ContextManager.class.getName());
		final BindingTableManager bindingTables = context.get(BindingTableManager.class);

		IEventBroker broker = context.get(IEventBroker.class);
		EventHandler handler = new EventHandler() {
			public void handleEvent(Event event) {
				Object elementObj = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (elementObj instanceof MApplication) {
					Object newObj = event.getProperty(UIEvents.EventTags.NEW_VALUE);
					if (UIEvents.EventTypes.ADD.equals(event.getProperty(UIEvents.EventTags.TYPE))
							&& newObj instanceof MBindingTable) {
						MBindingTable bt = (MBindingTable) newObj;
						final Context bindingContext = manager.getContext(bt.getBindingContextId());
						final BindingTable table = new BindingTable(bindingContext);
						bindingTables.addTable(table);
						List<MKeyBinding> bindings = bt.getBindings();
						for (MKeyBinding binding : bindings) {
							Binding keyBinding = createBinding(bindingContext, cs, bs,
									binding.getCommand(), binding.getParameters(),
									binding.getKeySequence(), binding);
							if (keyBinding != null) {
								table.addBinding(keyBinding);
							}
						}
					}
				} else if (elementObj instanceof MBindingTable) {
					Object newObj = event.getProperty(UIEvents.EventTags.NEW_VALUE);
					Object oldObj = event.getProperty(UIEvents.EventTags.OLD_VALUE);
					if (UIEvents.EventTypes.ADD.equals(event.getProperty(UIEvents.EventTags.TYPE))
							&& newObj instanceof MKeyBinding) {
						MKeyBinding binding = (MKeyBinding) newObj;
						updateBinding(cs, bs, manager, bindingTables, binding, true);
					} else if (UIEvents.EventTypes.REMOVE.equals(event
							.getProperty(UIEvents.EventTags.TYPE)) && oldObj instanceof MKeyBinding) {
						MKeyBinding binding = (MKeyBinding) oldObj;
						updateBinding(cs, bs, manager, bindingTables, binding, false);
					}
				} else if (elementObj instanceof MKeyBinding) {
					MKeyBinding binding = (MKeyBinding) elementObj;
					String attrName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
					if (UIEvents.EventTypes.SET.equals(event.getProperty(UIEvents.EventTags.TYPE))) {
						Object oldObj = event.getProperty(UIEvents.EventTags.OLD_VALUE);
						if (UIEvents.KeyBinding.COMMAND.equals(attrName)) {
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.setCommand((MCommand) oldObj);
							updateBinding(cs, bs, manager, bindingTables, oldBinding, false);
							updateBinding(cs, bs, manager, bindingTables, binding, true);
						} else if (UIEvents.KeySequence.KEYSEQUENCE.equals(attrName)) {
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.setKeySequence((String) oldObj);
							updateBinding(cs, bs, manager, bindingTables, oldBinding, false);
							updateBinding(cs, bs, manager, bindingTables, binding, true);
						}
					} else if (UIEvents.KeyBinding.PARAMETERS.equals(attrName)) {
						if (UIEvents.EventTypes.ADD.equals(event
								.getProperty(UIEvents.EventTags.TYPE))) {
							Object newObj = event.getProperty(UIEvents.EventTags.NEW_VALUE);
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.getParameters().remove(newObj);
							updateBinding(cs, bs, manager, bindingTables, oldBinding, false);
							updateBinding(cs, bs, manager, bindingTables, binding, true);
						} else if (UIEvents.EventTypes.REMOVE.equals(event
								.getProperty(UIEvents.EventTags.TYPE))) {
							Object oldObj = event.getProperty(UIEvents.EventTags.OLD_VALUE);
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.getParameters().add((MParameter) oldObj);
							updateBinding(cs, bs, manager, bindingTables, oldBinding, false);
							updateBinding(cs, bs, manager, bindingTables, binding, true);
						}
					}
				}
			}
		};
		broker.subscribe(UIEvents.buildTopic(UIEvents.BindingTableContainer.TOPIC,
				UIEvents.BindingTableContainer.BINDINGTABLES), handler);
		broker.subscribe(
				UIEvents.buildTopic(UIEvents.BindingTable.TOPIC, UIEvents.BindingTable.BINDINGS),
				handler);
		broker.subscribe(
				UIEvents.buildTopic(UIEvents.KeyBinding.TOPIC, UIEvents.KeyBinding.COMMAND),
				handler);
		broker.subscribe(
				UIEvents.buildTopic(UIEvents.KeyBinding.TOPIC, UIEvents.KeyBinding.PARAMETERS),
				handler);
		broker.subscribe(
				UIEvents.buildTopic(UIEvents.KeySequence.TOPIC, UIEvents.KeySequence.KEYSEQUENCE),
				handler);
	}

	private static Binding createBinding(Context bindingContext, ECommandService cs,
			EBindingService bs, MCommand cmdModel, List<MParameter> modelParms, String keySequence,
			MKeyBinding binding) {
		if (cmdModel == null) {
			Activator.log(IStatus.ERROR, "binding with no command: " + binding); //$NON-NLS-1$
			return null;
		}
		Map<String, Object> parameters = null;
		if (modelParms != null && !modelParms.isEmpty()) {
			parameters = new HashMap<String, Object>();
			for (MParameter mParm : modelParms) {
				parameters.put(mParm.getName(), mParm.getValue());
			}
		}
		ParameterizedCommand cmd = cs.createCommand(cmdModel.getElementId(), parameters);
		TriggerSequence sequence = null;
		sequence = bs.createSequence(keySequence);
		Binding keyBinding = null;
		if (cmd == null || sequence == null) {
			System.err.println("Failed to handle binding: " + binding); //$NON-NLS-1$
		} else {
			try {
				keyBinding = bs.createBinding(sequence, cmd,
						"org.eclipse.ui.defaultAcceleratorConfiguration", bindingContext.getId()); //$NON-NLS-1$
			} catch (IllegalArgumentException e) {
				Activator.trace(Policy.DEBUG_MENUS, "failed to create: " + binding, e); //$NON-NLS-1$
				return null;
			}

		}
		return keyBinding;
	}

	static void updateBinding(final ECommandService cs, final EBindingService bs,
			final ContextManager manager, final BindingTableManager bindingTables,
			MKeyBinding binding, boolean add) {
		Object parentObj = ((EObject) binding).eContainer();
		if (!(parentObj instanceof MBindingTable)) {
			return;
		}
		MBindingTable bt = (MBindingTable) parentObj;
		final Context bindingContext = manager.getContext(bt.getBindingContextId());
		BindingTable table = bindingTables.getTable(bindingContext.getId());
		if (table == null) {
			Activator.log(IStatus.ERROR, "Trying to create \'" + binding //$NON-NLS-1$
					+ "\' without binding table " + bindingContext.getId()); //$NON-NLS-1$
			return;
		}
		Binding keyBinding = createBinding(bindingContext, cs, bs, binding.getCommand(),
				binding.getParameters(), binding.getKeySequence(), binding);
		if (keyBinding != null) {
			if (add) {
				table.addBinding(keyBinding);
			} else {
				table.removeBinding(keyBinding);
			}
		}
	}
}
