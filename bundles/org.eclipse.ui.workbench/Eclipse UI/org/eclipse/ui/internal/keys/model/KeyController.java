/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440810, 472654
 *******************************************************************************/

package org.eclipse.ui.internal.keys.model;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.keys.KeysPreferencePage;
import org.eclipse.ui.internal.keys.NewKeysPreferenceMessages;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @since 3.4
 */
public class KeyController {
	private static final String DELIMITER = ","; //$NON-NLS-1$
	private static final String ESCAPED_QUOTE = "\""; //$NON-NLS-1$
	private static final String REPLACEMENT = "\"\""; //$NON-NLS-1$
	/**
	 * The resource bundle from which translations can be retrieved.
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(KeysPreferencePage.class.getName());
	private ListenerList<IPropertyChangeListener> eventManager = null;
	private BindingManager fBindingManager;
	private ContextModel contextModel;
	private SchemeModel fSchemeModel;
	private BindingModel bindingModel;
	private boolean notifying = true;
	private ConflictModel conflictModel;
	private IServiceLocator serviceLocator;

	private ListenerList<IPropertyChangeListener> getEventManager() {
		if (eventManager == null) {
			eventManager = new ListenerList<>(ListenerList.IDENTITY);
		}
		return eventManager;
	}

	public void setNotifying(boolean b) {
		notifying = b;
	}

	public boolean isNotifying() {
		return notifying;
	}

	public void firePropertyChange(Object source, String propId, Object oldVal, Object newVal) {
		if (!isNotifying()) {
			return;
		}
		if (Objects.equals(oldVal, newVal)) {
			return;
		}

		PropertyChangeEvent event = new PropertyChangeEvent(source, propId, oldVal, newVal);
		for (IPropertyChangeListener listener : getEventManager()) {
			listener.propertyChange(event);
		}
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		getEventManager().add(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		getEventManager().remove(listener);
	}

	public void init(IServiceLocator locator) {
		getEventManager().clear();
		this.serviceLocator = locator;
		fBindingManager = loadModelBackend(serviceLocator);
		contextModel = new ContextModel(this);
		contextModel.init(serviceLocator);
		fSchemeModel = new SchemeModel(this);
		fSchemeModel.init(fBindingManager);
		bindingModel = new BindingModel(this);
		bindingModel.init(serviceLocator, fBindingManager, contextModel);
		conflictModel = new ConflictModel(this);
		conflictModel.init(fBindingManager, bindingModel);
		addSetContextListener();
		addSetBindingListener();
		addSetConflictListener();
		addSetKeySequenceListener();
		addSetSchemeListener();
		addSetModelObjectListener();
	}

	private static BindingManager loadModelBackend(IServiceLocator locator) {
		IBindingService bindingService = locator.getService(IBindingService.class);
		BindingManager bindingManager = new BindingManager(new ContextManager(), new CommandManager());
		final Scheme[] definedSchemes = bindingService.getDefinedSchemes();
		try {
			Scheme modelActiveScheme = null;
			for (final Scheme scheme : definedSchemes) {
				final Scheme copy = bindingManager.getScheme(scheme.getId());
				copy.define(scheme.getName(), scheme.getDescription(), scheme.getParentId());
				if (scheme.getId().equals(bindingService.getActiveScheme().getId())) {
					modelActiveScheme = copy;
				}
			}
			bindingManager.setActiveScheme(modelActiveScheme);
		} catch (final NotDefinedException e) {
			StatusManager.getManager().handle(new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH,
					"Keys page found an undefined scheme", e)); //$NON-NLS-1$
		}

		bindingManager.setLocale(bindingService.getLocale());
		bindingManager.setPlatform(bindingService.getPlatform());

		Set<Binding> bindings = new HashSet<>();
		EBindingService eBindingService = locator.getService(EBindingService.class);
		bindings.addAll(eBindingService.getActiveBindings());
		bindings.addAll(Arrays.asList(bindingService.getBindings()));

		bindingManager.setBindings(bindings.toArray(new Binding[0]));

		return bindingManager;
	}

	public ContextModel getContextModel() {
		return contextModel;
	}

	public SchemeModel getSchemeModel() {
		return fSchemeModel;
	}

	public BindingModel getBindingModel() {
		return bindingModel;
	}

	public ConflictModel getConflictModel() {
		return conflictModel;
	}

	private void addSetContextListener() {
		addPropertyChangeListener(event -> {
			if (event.getSource() == contextModel && CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
				updateBindingContext((ContextElement) event.getNewValue());
			}
		});
	}

	private void addSetBindingListener() {
		addPropertyChangeListener(event -> {
			if (event.getSource() == bindingModel && CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
				BindingElement binding = (BindingElement) event.getNewValue();
				if (binding == null) {
					conflictModel.setSelectedElement(null);
					return;
				}
				conflictModel.setSelectedElement(binding);
				ContextElement context = binding.getContext();
				if (context != null) {
					contextModel.setSelectedElement(context);
				}
			}
		});
	}

	private void addSetConflictListener() {
		addPropertyChangeListener(event -> {
			if (event.getSource() == conflictModel && CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
				if (event.getNewValue() != null) {
					bindingModel.setSelectedElement((ModelElement) event.getNewValue());
				}
			}
		});
	}

	private void addSetKeySequenceListener() {
		addPropertyChangeListener(event -> {
			if (BindingElement.PROP_TRIGGER.equals(event.getProperty())) {
				updateTrigger((BindingElement) event.getSource(), (KeySequence) event.getNewValue());
			}
		});
	}

	private void addSetModelObjectListener() {
		addPropertyChangeListener(event -> {
			if (event.getSource() instanceof BindingElement
					&& ModelElement.PROP_MODEL_OBJECT.equals(event.getProperty())) {
				if (event.getNewValue() != null) {
					BindingElement element = (BindingElement) event.getSource();
					Object oldValue = event.getOldValue();
					Object newValue = event.getNewValue();
					if (oldValue instanceof Binding && newValue instanceof Binding) {
						conflictModel.updateConflictsFor(element, ((Binding) oldValue).getTriggerSequence(),
								((Binding) newValue).getTriggerSequence(), false);
					} else {
						conflictModel.updateConflictsFor(element, false);
					}

					ContextElement context = element.getContext();
					if (context != null) {
						contextModel.setSelectedElement(context);
					}
				}
			}
		});
	}

	private void addSetSchemeListener() {
		addPropertyChangeListener(event -> {
			if (event.getSource() == fSchemeModel && CommonModel.PROP_SELECTED_ELEMENT.equals(event.getProperty())) {
				changeScheme((SchemeElement) event.getNewValue());
			}
		});
	}

	protected void changeScheme(SchemeElement newScheme) {
		if (newScheme == null || newScheme.getModelObject() == fBindingManager.getActiveScheme()) {
			return;
		}
		try {
			fBindingManager.setActiveScheme((Scheme) newScheme.getModelObject());
			bindingModel.refresh(contextModel);
			bindingModel.setSelectedElement(null);
		} catch (NotDefinedException e) {
			WorkbenchPlugin.log(e);
		}

	}

	private void updateBindingContext(ContextElement context) {
		if (context == null) {
			return;
		}
		BindingElement activeBinding = (BindingElement) bindingModel.getSelectedElement();
		if (activeBinding == null) {
			return;
		}
		String activeSchemeId = fSchemeModel.getSelectedElement().getId();
		Object obj = activeBinding.getModelObject();
		if (obj instanceof KeyBinding) {
			KeyBinding keyBinding = (KeyBinding) obj;
			if (!keyBinding.getContextId().equals(context.getId())) {
				final KeyBinding binding = new KeyBinding(keyBinding.getKeySequence(),
						keyBinding.getParameterizedCommand(), activeSchemeId, context.getId(), null, null, null,
						Binding.USER);
				if (keyBinding.getType() == Binding.USER) {
					fBindingManager.removeBinding(keyBinding);
				} else {
					fBindingManager.addBinding(new KeyBinding(keyBinding.getKeySequence(), null,
							keyBinding.getSchemeId(), keyBinding.getContextId(), null, null, null, Binding.USER));
				}
				bindingModel.getBindingToElement().remove(activeBinding.getModelObject());

				fBindingManager.addBinding(binding);
				activeBinding.fill(binding, contextModel);
				bindingModel.getBindingToElement().put(binding, activeBinding);
			}
		}
	}

	private void updateTrigger(BindingElement activeBinding, KeySequence keySequence) {
		if (activeBinding == null) {
			return;
		}
		Object obj = activeBinding.getModelObject();
		if (obj instanceof KeyBinding) {
			KeyBinding keyBinding = (KeyBinding) obj;
			if (!keyBinding.getKeySequence().equals(keySequence)) {
				if (keySequence != null && !keySequence.isEmpty()) {
					String activeSchemeId = fSchemeModel.getSelectedElement().getId();
					ModelElement selectedElement = contextModel.getSelectedElement();
					String activeContextId = selectedElement == null ? IContextService.CONTEXT_ID_WINDOW
							: selectedElement.getId();
					final KeyBinding binding = new KeyBinding(keySequence, keyBinding.getParameterizedCommand(),
							activeSchemeId, activeContextId, null, null, null, Binding.USER);
					Map<Binding, BindingElement> bindingToElement = bindingModel.getBindingToElement();
					bindingToElement.remove(keyBinding);
					if (keyBinding.getType() == Binding.USER) {
						fBindingManager.removeBinding(keyBinding);
					} else {
						fBindingManager.addBinding(new KeyBinding(keyBinding.getKeySequence(), null,
								keyBinding.getSchemeId(), keyBinding.getContextId(), null, null, null, Binding.USER));
					}

					fBindingManager.addBinding(binding);
					activeBinding.fill(binding, contextModel);
					bindingModel.getBindingToElement().put(binding, activeBinding);

					// Remove binding for any system conflicts

					bindingModel.setSelectedElement(activeBinding);
				} else {
					bindingModel.getBindingToElement().remove(keyBinding);
					if (keyBinding.getType() == Binding.USER) {
						fBindingManager.removeBinding(keyBinding);
					} else {
						fBindingManager.addBinding(new KeyBinding(keyBinding.getKeySequence(), null,
								keyBinding.getSchemeId(), keyBinding.getContextId(), null, null, null, Binding.USER));
					}
					activeBinding.fill(keyBinding.getParameterizedCommand());
				}
			}
		} else if (obj instanceof ParameterizedCommand) {
			ParameterizedCommand cmd = (ParameterizedCommand) obj;
			if (keySequence != null && !keySequence.isEmpty()) {
				String activeSchemeId = fSchemeModel.getSelectedElement().getId();
				ModelElement selectedElement = contextModel.getSelectedElement();
				String activeContextId = selectedElement == null ? IContextService.CONTEXT_ID_WINDOW
						: selectedElement.getId();
				final KeyBinding binding = new KeyBinding(keySequence, cmd, activeSchemeId, activeContextId, null, null,
						null, Binding.USER);
				fBindingManager.addBinding(binding);
				activeBinding.fill(binding, contextModel);
				bindingModel.getBindingToElement().put(binding, activeBinding);
			}
		}
	}

	/**
	 * Replaces all the current bindings with the bindings in the local copy of the
	 * binding manager.
	 *
	 * @param bindingService The binding service that saves the changes made to the
	 *                       local copy of the binding manager
	 */
	public void saveBindings(IBindingService bindingService) {
		try {
			bindingService.savePreferences(fBindingManager.getActiveScheme(), fBindingManager.getBindings());
		} catch (IOException e) {
			logPreferenceStoreException(e);
		}
	}

	/**
	 * Logs the given exception, and opens an error dialog saying that something
	 * went wrong. The exception is assumed to have something to do with the
	 * preference store.
	 *
	 * @param exception The exception to be logged; must not be <code>null</code>.
	 */
	private final void logPreferenceStoreException(final Throwable exception) {
		final String message = NewKeysPreferenceMessages.PreferenceStoreError_Message;
		String exceptionMessage = exception.getMessage();
		if (exceptionMessage == null) {
			exceptionMessage = message;
		}
		final IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 0, exceptionMessage, exception);
		WorkbenchPlugin.log(message, status);
		StatusUtil.handleStatus(message, exception, StatusManager.SHOW);
	}

	/**
	 * Filters contexts for the When Combo.
	 *
	 * @param actionSets <code>true</code> to filter action set contexts
	 * @param internal   <code>false</code> to filter internal contexts
	 */
	public void filterContexts(boolean actionSets, boolean internal) {
		contextModel.filterContexts(actionSets, internal);
	}

	/**
	 * Sets the bindings to default.
	 */
	public void setDefaultBindings(IBindingService bindingService) {
		// Fix the scheme in the local changes.
		final String defaultSchemeId = bindingService.getDefaultSchemeId();
		final Scheme defaultScheme = fBindingManager.getScheme(defaultSchemeId);
		try {
			fBindingManager.setActiveScheme(defaultScheme);
		} catch (final NotDefinedException e) {
			// At least we tried....
		}

		// Restore any User defined bindings
		for (Binding binding : fBindingManager.getBindings()) {
			if (binding.getType() == Binding.USER) {
				fBindingManager.removeBinding(binding);
			}
		}

		bindingModel.refresh(contextModel);
		saveBindings(bindingService);
	}

	public void exportCSV(Shell shell) {
		final FileDialog fileDialog = new FileDialog(shell, SWT.SAVE | SWT.SHEET);
		fileDialog.setFilterExtensions(new String[] { "*.csv" }); //$NON-NLS-1$
		fileDialog.setFilterNames(new String[] { Util.translateString(RESOURCE_BUNDLE, "csvFilterName") }); //$NON-NLS-1$
		fileDialog.setOverwrite(true);
		final String filePath = fileDialog.open();
		if (filePath == null) {
			return;
		}

		final SafeRunnable runnable = new SafeRunnable() {
			@Override
			public final void run() throws IOException {
				try (Writer fileWriter = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {

					final Object[] bindingElements = bindingModel.getBindings().toArray();
					for (Object bindingElement : bindingElements) {
						final BindingElement be = (BindingElement) bindingElement;
						if (be.getTrigger() == null || be.getTrigger().isEmpty()) {
							continue;
						}
						StringBuilder buffer = new StringBuilder();
						buffer.append(ESCAPED_QUOTE + Util.replaceAll(be.getCategory(), ESCAPED_QUOTE, REPLACEMENT)
								+ ESCAPED_QUOTE + DELIMITER);
						buffer.append(ESCAPED_QUOTE + be.getName() + ESCAPED_QUOTE + DELIMITER);
						buffer.append(ESCAPED_QUOTE + be.getTrigger().format() + ESCAPED_QUOTE + DELIMITER);
						buffer.append(ESCAPED_QUOTE + be.getContext().getName() + ESCAPED_QUOTE + DELIMITER);
						buffer.append(ESCAPED_QUOTE + be.getId() + ESCAPED_QUOTE);
						buffer.append(System.lineSeparator());
						fileWriter.write(buffer.toString());
					}

				}
			}
		};
		SafeRunner.run(runnable);
	}
}
