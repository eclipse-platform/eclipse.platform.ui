/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 429575
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.swt.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingTable;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Process contexts in the model, feeding them into the command service.
 */
public class BindingProcessingAddon {
	private static final String[] DEFAULT_SCHEMES = { "org.eclipse.ui.defaultAcceleratorConfiguration" };

	@Inject
	private MApplication application;

	@Inject
	private IEventBroker broker;

	@Inject
	private ContextManager contextManager;

	@Inject
	private BindingTableManager bindingTables;

	@Inject
	@Optional
	private BindingManager bindingManager;

	@Inject
	private ECommandService commandService;

	@Inject
	private EBindingService bindingService;

	private EventHandler additionHandler;

	private EventHandler contextHandler;

	@PostConstruct
	public void init() {
		String[] schemes = DEFAULT_SCHEMES;
		if (bindingManager != null) {
			final Scheme activeScheme = bindingManager.getActiveScheme();
			if (activeScheme != null) {
				schemes = getSchemeIds(activeScheme.getId());
			}
		}
		bindingTables.setActiveSchemes(schemes);
		defineBindingTables();
		activateContexts(application);
		registerModelListeners();
	}

	private final String[] getSchemeIds(String schemeId) {
		final List<String> strings = new ArrayList<String>();
		while (schemeId != null) {
			strings.add(schemeId);
			try {
				schemeId = getScheme(schemeId).getParentId();
			} catch (final NotDefinedException e) {
				return new String[0];
			}
		}

		return strings.toArray(new String[strings.size()]);
	}

	private final Scheme getScheme(String schemeId) {
		return bindingManager.getScheme(schemeId);
	}

	private void activateContexts(Object me) {
		if (me instanceof MBindings) {
			MContext contextModel = (MContext) me;
			MBindings container = (MBindings) me;
			List<MBindingContext> bindingContexts = container
					.getBindingContexts();
			IEclipseContext context = contextModel.getContext();
			if (context != null && !bindingContexts.isEmpty()) {
				EContextService cs = context.get(EContextService.class);
				for (MBindingContext element : bindingContexts) {
					cs.activateContext(element.getElementId());
				}
			}
		}
		if (me instanceof MElementContainer) {
			List<MUIElement> children = ((MElementContainer) me).getChildren();
			Iterator<MUIElement> i = children.iterator();
			while (i.hasNext()) {
				MUIElement e = i.next();
				activateContexts(e);
			}
		}
	}

	private void defineBindingTables() {
		Activator.trace(Policy.DEBUG_CMDS,
				"Initialize binding tables from model", null); //$NON-NLS-1$
		for (MBindingTable bindingTable : application.getBindingTables()) {
			defineBindingTable(bindingTable);
		}
	}

	/**
	 * @param bindingTable
	 */
	private void defineBindingTable(MBindingTable bindingTable) {
		Assert.isNotNull(
				bindingTable.getBindingContext(),
				"Binding context referred to by the binding table \"" + bindingTable.getElementId() + "\""); //$NON-NLS-1$
		Assert.isNotNull(bindingTable.getBindingContext().getElementId(),
				"Element ID of binding table \"" + bindingTable.getElementId() + "\"."); //$NON-NLS-1$
		final Context bindingContext = contextManager.getContext(bindingTable
				.getBindingContext().getElementId());
		BindingTable table = bindingTables.getTable(bindingTable
				.getBindingContext().getElementId());
		if (table == null) {
			table = new BindingTable(bindingContext);
			bindingTables.addTable(table);
		}
		for (MKeyBinding binding : bindingTable.getBindings()) {
			defineBinding(table, bindingContext, binding);
		}
	}

	/**
	 * @param bindingTable
	 * @param binding
	 */
	private void defineBinding(BindingTable bindingTable,
			Context bindingContext, MKeyBinding binding) {
		Binding keyBinding = createBinding(bindingContext,
				binding.getCommand(), binding.getParameters(),
				binding.getKeySequence(), binding);
		if (keyBinding != null
				&& !binding.getTags().contains(
						EBindingService.DELETED_BINDING_TAG)) {
			bindingTable.addBinding(keyBinding);
		}
	}

	private Binding createBinding(Context bindingContext, MCommand cmdModel,
			List<MParameter> modelParms, String keySequence, MKeyBinding binding) {
		Binding keyBinding = null;

		if (binding.getTransientData()
				.get(EBindingService.MODEL_TO_BINDING_KEY) != null) {
			try {
				keyBinding = (Binding) binding.getTransientData().get(
						EBindingService.MODEL_TO_BINDING_KEY);
				return keyBinding;
			} catch (ClassCastException cce) {
				System.err
						.println("Invalid type stored in transient data with the key "
								+ EBindingService.MODEL_TO_BINDING_KEY);
				return null;
			}
		}

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
		ParameterizedCommand cmd = commandService.createCommand(
				cmdModel.getElementId(), parameters);
		TriggerSequence sequence = null;
		sequence = bindingService.createSequence(keySequence);

		if (cmd == null) {
			System.err
					.println("Failed to find command for binding: " + binding); //$NON-NLS-1$
		} else if (sequence == null) {
			System.err.println("Failed to map binding: " + binding); //$NON-NLS-1$
		} else {
			try {
				String schemeId = null;
				String locale = null;
				String platform = null;

				Map<String, String> attrs = new HashMap<String, String>();
				List<String> tags = binding.getTags();
				for (String tag : tags) {
					// remember to skip the ':' in each tag!
					if (tag.startsWith(EBindingService.SCHEME_ID_ATTR_TAG)) {
						schemeId = tag.substring(9);
						attrs.put(EBindingService.SCHEME_ID_ATTR_TAG, schemeId);
					} else if (tag.startsWith(EBindingService.LOCALE_ATTR_TAG)) {
						locale = tag.substring(7);
						attrs.put(EBindingService.LOCALE_ATTR_TAG, locale);
					} else if (tag
							.startsWith(EBindingService.PLATFORM_ATTR_TAG)) {
						platform = tag.substring(9);
						attrs.put(EBindingService.PLATFORM_ATTR_TAG, platform);
					} else if (tag.startsWith(EBindingService.TYPE_ATTR_TAG)) {
						// system bindings won't pass this attr
						attrs.put(EBindingService.TYPE_ATTR_TAG, "user");
					}
				}
				keyBinding = bindingService.createBinding(sequence, cmd,
						bindingContext.getId(), attrs);
				binding.getTransientData().put(
						EBindingService.MODEL_TO_BINDING_KEY, keyBinding);
			} catch (IllegalArgumentException e) {
				Activator.trace(Policy.DEBUG_MENUS,
						"failed to create: " + binding, e); //$NON-NLS-1$
				return null;
			}

		}
		return keyBinding;
	}

	private void updateBinding(MKeyBinding binding, boolean add, Object eObj) {
		Object parentObj = ((EObject) binding).eContainer();
		if (!(parentObj instanceof MBindingTable)) {
			// the link will already be broken for removes, so we'll try this
			if (eObj instanceof MBindingTable) {
				parentObj = eObj;
			}
		}

		if (parentObj == null) {
			return;
		}

		MBindingTable bt = (MBindingTable) parentObj;
		final Context bindingContext = contextManager.getContext(bt
				.getBindingContext().getElementId());
		BindingTable table = bindingTables.getTable(bindingContext.getId());
		if (table == null) {
			Activator.log(IStatus.ERROR, "Trying to create \'" + binding //$NON-NLS-1$
					+ "\' without binding table " + bindingContext.getId()); //$NON-NLS-1$
			return;
		}
		Binding keyBinding = createBinding(bindingContext,
				binding.getCommand(), binding.getParameters(),
				binding.getKeySequence(), binding);
		if (keyBinding != null) {
			if (add) {
				table.addBinding(keyBinding);
			} else {
				table.removeBinding(keyBinding);
			}
		}
	}

	@PreDestroy
	public void dispose() {
		unregsiterModelListeners();
	}

	private void registerModelListeners() {
		additionHandler = new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				Object elementObj = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (elementObj instanceof MApplication) {
					if (UIEvents.isADD(event)) {
						for (Object newObj : UIEvents.asIterable(event,
								UIEvents.EventTags.NEW_VALUE)) {
							if (newObj instanceof MBindingTable) {
								MBindingTable bt = (MBindingTable) newObj;
								final Context bindingContext = contextManager
										.getContext(bt.getBindingContext()
												.getElementId());
								final BindingTable table = new BindingTable(
										bindingContext);
								bindingTables.addTable(table);
								List<MKeyBinding> bindings = bt.getBindings();
								for (MKeyBinding binding : bindings) {
									Binding keyBinding = createBinding(
											bindingContext,
											binding.getCommand(),
											binding.getParameters(),
											binding.getKeySequence(), binding);
									if (keyBinding != null) {
										table.addBinding(keyBinding);
									}
								}
							}
						}
					}
				} else if (elementObj instanceof MBindingTable) {
					// adding a binding
					if (UIEvents.isADD(event)) {
						for (Object newObj : UIEvents.asIterable(event,
								UIEvents.EventTags.NEW_VALUE)) {
							if (newObj instanceof MKeyBinding) {
								MKeyBinding binding = (MKeyBinding) newObj;
								updateBinding(binding, true, elementObj);
							}
						}
					}
					// removing a binding
					else if (UIEvents.isREMOVE(event)) {
						for (Object oldObj : UIEvents.asIterable(event,
								UIEvents.EventTags.OLD_VALUE)) {
							if (oldObj instanceof MKeyBinding) {
								MKeyBinding binding = (MKeyBinding) oldObj;
								updateBinding(binding, false, elementObj);
							}
						}
					}
				} else if (elementObj instanceof MKeyBinding) {
					MKeyBinding binding = (MKeyBinding) elementObj;

					String attrName = (String) event
							.getProperty(UIEvents.EventTags.ATTNAME);

					// System.out.println("MKeyBinding." + attrName + ": "
					// + event.getProperty(UIEvents.EventTags.TYPE));
					if (UIEvents.isSET(event)) {
						Object oldObj = event
								.getProperty(UIEvents.EventTags.OLD_VALUE);
						if (UIEvents.KeyBinding.COMMAND.equals(attrName)) {
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.setCommand((MCommand) oldObj);
							updateBinding(oldBinding, false,
									((EObject) binding).eContainer());
							updateBinding(binding, true, null);
						} else if (UIEvents.KeySequence.KEYSEQUENCE
								.equals(attrName)) {
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							oldBinding.setKeySequence((String) oldObj);
							updateBinding(oldBinding, false,
									((EObject) binding).eContainer());
							updateBinding(binding, true, null);
						}
					} else if (UIEvents.KeyBinding.PARAMETERS.equals(attrName)) {
						if (UIEvents.isADD(event)) {
							Object newObj = event
									.getProperty(UIEvents.EventTags.NEW_VALUE);
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							if (UIEvents.EventTypes.ADD_MANY.equals(event
									.getProperty(UIEvents.EventTags.TYPE))) {
								oldBinding.getParameters().removeAll(
										(Collection<?>) newObj);
							} else {
								oldBinding.getParameters().remove(newObj);
							}
							updateBinding(oldBinding, false,
									((EObject) binding).eContainer());
							updateBinding(binding, true, null);
						} else if (UIEvents.isREMOVE(event)) {
							Object oldObj = event
									.getProperty(UIEvents.EventTags.OLD_VALUE);
							MKeyBinding oldBinding = (MKeyBinding) EcoreUtil
									.copy((EObject) binding);
							if (UIEvents.EventTypes.REMOVE_MANY.equals(event
									.getProperty(UIEvents.EventTags.TYPE))) {
								@SuppressWarnings("unchecked")
								Collection<MParameter> parms = (Collection<MParameter>) oldObj;
								oldBinding.getParameters().addAll(parms);
							} else {
								oldBinding.getParameters().add(
										(MParameter) oldObj);
							}

							updateBinding(oldBinding, false,
									((EObject) binding).eContainer());
							updateBinding(binding, true, null);
						}
					}
					// if we've updated the tags for an MKeyBinding
					else if (UIEvents.ApplicationElement.TAGS.equals(attrName)) {
						List<String> tags = binding.getTags();
						// if we added a deleted tag to the MKeyBinding, then
						// remove it from the runtime binding tables
						if (tags.contains(EBindingService.DELETED_BINDING_TAG)) {
							updateBinding(binding, false, elementObj);
						}
						// else we're adding the binding to the runtime tables
						else {
							updateBinding(binding, true, elementObj);
						}
					}
				}
			}
		};
		broker.subscribe(UIEvents.BindingTableContainer.TOPIC_BINDINGTABLES,
				additionHandler);
		broker.subscribe(UIEvents.BindingTable.TOPIC_BINDINGS, additionHandler);
		broker.subscribe(UIEvents.KeyBinding.TOPIC_COMMAND, additionHandler);
		broker.subscribe(UIEvents.KeyBinding.TOPIC_PARAMETERS, additionHandler);
		broker.subscribe(UIEvents.KeySequence.TOPIC_KEYSEQUENCE,
				additionHandler);
		broker.subscribe(UIEvents.ApplicationElement.TOPIC_TAGS,
				additionHandler);

		contextHandler = new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				Object elementObj = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				Object newObj = event.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (UIEvents.EventTypes.SET.equals(event
						.getProperty(UIEvents.EventTags.TYPE))
						&& newObj instanceof IEclipseContext) {
					activateContexts(elementObj);
				}
			}
		};
		broker.subscribe(UIEvents.Context.TOPIC_CONTEXT, contextHandler);
	}

	private void unregsiterModelListeners() {
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(additionHandler);
		broker.unsubscribe(contextHandler);
	}
}
