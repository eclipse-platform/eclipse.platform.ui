/*******************************************************************************
 * Copyright (c) 2005, 2022 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.keys;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.HandleObject;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.internal.util.Tracing;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.PreferencePersistence;
import org.eclipse.ui.keys.IBindingService;

/**
 * <p>
 * A static class for accessing the registry and the preference store.
 * </p>
 *
 * @since 3.1
 */
public class BindingPersistence extends PreferencePersistence {

	/**
	 * Whether this class should print out debugging information when it reads in
	 * data, or writes to the preference store.
	 */
	private static final boolean DEBUG = Policy.DEBUG_KEY_BINDINGS;

	/**
	 * The index of the active scheme configuration elements in the indexed array.
	 *
	 * @see BindingPersistence#read()
	 */
	private static final int INDEX_ACTIVE_SCHEME = 0;

	/**
	 * The index of the binding definition configuration elements in the indexed
	 * array.
	 *
	 * @see BindingPersistence#read()
	 */
	private static final int INDEX_BINDING_DEFINITIONS = 1;

	/**
	 * The index of the scheme definition configuration elements in the indexed
	 * array.
	 *
	 * @see BindingPersistence#read()
	 */
	private static final int INDEX_SCHEME_DEFINITIONS = 2;

	/**
	 * The name of the default scope in 2.1.x.
	 */
	private static final String LEGACY_DEFAULT_SCOPE = "org.eclipse.ui.globalScope"; //$NON-NLS-1$

	/**
	 * A look-up map for 2.1.x style <code>string</code> keys on a
	 * <code>keyBinding</code> element.
	 */
	private static final Map<String, Integer> r2_1KeysByName = new HashMap<>();

	static {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		r2_1KeysByName.put(IKeyLookup.BACKSPACE_NAME, lookup.formalKeyLookupInteger(IKeyLookup.BACKSPACE_NAME));
		r2_1KeysByName.put(IKeyLookup.TAB_NAME, lookup.formalKeyLookupInteger(IKeyLookup.TAB_NAME));
		r2_1KeysByName.put(IKeyLookup.RETURN_NAME, lookup.formalKeyLookupInteger(IKeyLookup.RETURN_NAME));
		r2_1KeysByName.put(IKeyLookup.ENTER_NAME, lookup.formalKeyLookupInteger(IKeyLookup.ENTER_NAME));
		r2_1KeysByName.put(IKeyLookup.ESCAPE_NAME, lookup.formalKeyLookupInteger(IKeyLookup.ESCAPE_NAME));
		r2_1KeysByName.put(IKeyLookup.ESC_NAME, lookup.formalKeyLookupInteger(IKeyLookup.ESC_NAME));
		r2_1KeysByName.put(IKeyLookup.DELETE_NAME, lookup.formalKeyLookupInteger(IKeyLookup.DELETE_NAME));
		r2_1KeysByName.put(IKeyLookup.SPACE_NAME, lookup.formalKeyLookupInteger(IKeyLookup.SPACE_NAME));
		r2_1KeysByName.put(IKeyLookup.ARROW_UP_NAME, lookup.formalKeyLookupInteger(IKeyLookup.ARROW_UP_NAME));
		r2_1KeysByName.put(IKeyLookup.ARROW_DOWN_NAME, lookup.formalKeyLookupInteger(IKeyLookup.ARROW_DOWN_NAME));
		r2_1KeysByName.put(IKeyLookup.ARROW_LEFT_NAME, lookup.formalKeyLookupInteger(IKeyLookup.ARROW_LEFT_NAME));
		r2_1KeysByName.put(IKeyLookup.ARROW_RIGHT_NAME, lookup.formalKeyLookupInteger(IKeyLookup.ARROW_RIGHT_NAME));
		r2_1KeysByName.put(IKeyLookup.PAGE_UP_NAME, lookup.formalKeyLookupInteger(IKeyLookup.PAGE_UP_NAME));
		r2_1KeysByName.put(IKeyLookup.PAGE_DOWN_NAME, lookup.formalKeyLookupInteger(IKeyLookup.PAGE_DOWN_NAME));
		r2_1KeysByName.put(IKeyLookup.HOME_NAME, lookup.formalKeyLookupInteger(IKeyLookup.HOME_NAME));
		r2_1KeysByName.put(IKeyLookup.END_NAME, lookup.formalKeyLookupInteger(IKeyLookup.END_NAME));
		r2_1KeysByName.put(IKeyLookup.INSERT_NAME, lookup.formalKeyLookupInteger(IKeyLookup.INSERT_NAME));
		r2_1KeysByName.put(IKeyLookup.F1_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F1_NAME));
		r2_1KeysByName.put(IKeyLookup.F2_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F2_NAME));
		r2_1KeysByName.put(IKeyLookup.F3_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F3_NAME));
		r2_1KeysByName.put(IKeyLookup.F4_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F4_NAME));
		r2_1KeysByName.put(IKeyLookup.F5_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F5_NAME));
		r2_1KeysByName.put(IKeyLookup.F6_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F6_NAME));
		r2_1KeysByName.put(IKeyLookup.F7_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F7_NAME));
		r2_1KeysByName.put(IKeyLookup.F8_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F8_NAME));
		r2_1KeysByName.put(IKeyLookup.F9_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F9_NAME));
		r2_1KeysByName.put(IKeyLookup.F10_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F10_NAME));
		r2_1KeysByName.put(IKeyLookup.F11_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F11_NAME));
		r2_1KeysByName.put(IKeyLookup.F12_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F12_NAME));
		r2_1KeysByName.put(IKeyLookup.F13_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F13_NAME));
		r2_1KeysByName.put(IKeyLookup.F14_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F14_NAME));
		r2_1KeysByName.put(IKeyLookup.F15_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F15_NAME));
		r2_1KeysByName.put(IKeyLookup.F16_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F16_NAME));
		r2_1KeysByName.put(IKeyLookup.F17_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F17_NAME));
		r2_1KeysByName.put(IKeyLookup.F18_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F18_NAME));
		r2_1KeysByName.put(IKeyLookup.F19_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F19_NAME));
		r2_1KeysByName.put(IKeyLookup.F20_NAME, lookup.formalKeyLookupInteger(IKeyLookup.F20_NAME));
	}

	/**
	 * Converts a 2.1.x style key sequence (as parsed from the <code>string</code>
	 * attribute of the <code>keyBinding</code>) to a 3.1 key sequence.
	 *
	 * @param r21KeySequence The sequence of 2.1.x key strokes that should be
	 *                       converted into a 3.1 key sequence; never
	 *                       <code>null</code>.
	 * @return A 3.1 key sequence; never <code>null</code>.
	 */
	private static final KeySequence convert2_1Sequence(int[] r21KeySequence) {
		final int r21KeySequenceLength = r21KeySequence.length;
		final KeyStroke[] keyStrokes = new KeyStroke[r21KeySequenceLength];
		for (int i = 0; i < r21KeySequenceLength; i++) {
			keyStrokes[i] = convert2_1Stroke(r21KeySequence[i]);
		}

		return KeySequence.getInstance(keyStrokes);
	}

	/**
	 * Converts a 2.1.x style key stroke (as parsed from the <code>string</code>
	 * attribute of the <code>keyBinding</code> to a 3.1 key stroke.
	 *
	 * @param r21Stroke The 2.1.x stroke to convert; must never be
	 *                  <code>null</code>.
	 * @return A 3.1 key stroke; never <code>null</code>.
	 */
	private static final KeyStroke convert2_1Stroke(final int r21Stroke) {
		return SWTKeySupport.convertAcceleratorToKeyStroke(r21Stroke);
	}

	/**
	 * Returns the default scheme identifier for the currently running application.
	 *
	 * @return The default scheme identifier (<code>String</code>); never
	 *         <code>null</code>, but may be empty or point to an undefined scheme.
	 */
	public static final String getDefaultSchemeId() {
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		return store.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
	}

	/**
	 * Parses a 2.1.x <code>string</code> attribute of the <code>keyBinding</code>
	 * element.
	 *
	 * @param string The string to parse; must not be <code>null</code>.
	 * @return An array of integer values -- each integer representing a single key
	 *         stroke. This array may be empty, but it is never <code>null</code>.
	 */
	private static final int[] parse2_1Sequence(final String string) {
		final StringTokenizer stringTokenizer = new StringTokenizer(string);
		final int length = stringTokenizer.countTokens();
		final int[] strokes = new int[length];

		for (int i = 0; i < length; i++) {
			strokes[i] = parse2_1Stroke(stringTokenizer.nextToken());
		}

		return strokes;
	}

	/**
	 * Parses a single 2.1.x key stroke string, as provided by
	 * <code>parse2_1Sequence</code>.
	 *
	 * @param string The string to parse; must not be <code>null</code>.
	 * @return An single integer value representing this key stroke.
	 */
	private static final int parse2_1Stroke(final String string) {
		final StringTokenizer stringTokenizer = new StringTokenizer(string, KeyStroke.KEY_DELIMITER, true);

		// Copy out the tokens so we have random access.
		final int size = stringTokenizer.countTokens();
		final String[] tokens = new String[size];
		for (int i = 0; stringTokenizer.hasMoreTokens(); i++) {
			tokens[i] = stringTokenizer.nextToken();
		}

		int value = 0;
		if (size % 2 == 1) {
			String token = tokens[size - 1];
			final Integer integer = r2_1KeysByName.get(token.toUpperCase(Locale.ENGLISH));

			if (integer != null) {
				value = integer.intValue();
			} else if (token.length() == 1) {
				value = token.toUpperCase().charAt(0);
			}

			if (value != 0) {
				for (int i = 0; i < size - 1; i++) {
					token = tokens[i];

					if (i % 2 == 0) {
						if (token.equalsIgnoreCase(IKeyLookup.CTRL_NAME)) {
							if ((value & SWT.CTRL) != 0) {
								return 0;
							}

							value |= SWT.CTRL;

						} else if (token.equalsIgnoreCase(IKeyLookup.ALT_NAME)) {
							if ((value & SWT.ALT) != 0) {
								return 0;
							}

							value |= SWT.ALT;

						} else if (token.equalsIgnoreCase(IKeyLookup.SHIFT_NAME)) {
							if ((value & SWT.SHIFT) != 0) {
								return 0;
							}

							value |= SWT.SHIFT;

						} else if (token.equalsIgnoreCase(IKeyLookup.COMMAND_NAME)) {
							if ((value & SWT.COMMAND) != 0) {
								return 0;
							}

							value |= SWT.COMMAND;

						} else {
							return 0;

						}

					} else if (!KeyStroke.KEY_DELIMITER.equals(token)) {
						return 0;
					}
				}
			}
		}

		return value;
	}

	/**
	 * <p>
	 * Reads the registry and the preference store, and determines the identifier
	 * for the scheme that should be active. There is a complicated order of
	 * priorities for this. The registry will only be read if there is no user
	 * preference, and the default active scheme id is different than the default
	 * default active scheme id.
	 * </p>
	 * <ol>
	 * <li>A non-default preference.</li>
	 * <li>The legacy preference XML memento.</li>
	 * <li>A default preference value that is different than the default default
	 * active scheme id.</li>
	 * <li>The registry.</li>
	 * <li>The default default active scheme id.</li>
	 * </ol>
	 *
	 * @param configurationElements     The configuration elements from the commands
	 *                                  extension point; must not be
	 *                                  <code>null</code>.
	 * @param configurationElementCount The number of configuration elements that
	 *                                  are really in the array.
	 * @param preferences               The memento wrapping the commands preference
	 *                                  key; may be <code>null</code>.
	 * @param bindingManager            The binding manager that should be updated
	 *                                  with the active scheme. This binding manager
	 *                                  must already have its schemes defined. This
	 *                                  value must not be <code>null</code>.
	 */
	private static final void readActiveScheme(final IConfigurationElement[] configurationElements,
			final int configurationElementCount, final IMemento preferences, final BindingManager bindingManager) {
		// A non-default preference.
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final String defaultActiveSchemeId = store.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		final String preferenceActiveSchemeId = store.getString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		if ((preferenceActiveSchemeId != null) && (!preferenceActiveSchemeId.equals(defaultActiveSchemeId))) {
			try {
				bindingManager.setActiveScheme(bindingManager.getScheme(preferenceActiveSchemeId));
				return;
			} catch (final NotDefinedException e) {
				// Let's keep looking....
			}
		}

		// A legacy preference XML memento.
		if (preferences != null) {
			final IMemento[] preferenceMementos = preferences.getChildren(TAG_ACTIVE_KEY_CONFIGURATION);
			int preferenceMementoCount = preferenceMementos.length;
			for (int i = preferenceMementoCount - 1; i >= 0; i--) {
				final IMemento memento = preferenceMementos[i];
				String id = memento.getString(ATT_KEY_CONFIGURATION_ID);
				if (id != null) {
					try {
						bindingManager.setActiveScheme(bindingManager.getScheme(id));
						return;
					} catch (final NotDefinedException e) {
						// Let's keep looking....
					}
				}
			}
		}

		// A default preference value that is different than the default.
		if ((defaultActiveSchemeId != null && defaultActiveSchemeId.length() > 0)
				&& (!defaultActiveSchemeId.equals(IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID))) {
			try {
				bindingManager.setActiveScheme(bindingManager.getScheme(defaultActiveSchemeId));
				return;
			} catch (final NotDefinedException e) {
				// Let's keep looking....
			}
		}

		// The registry.
		for (int i = configurationElementCount - 1; i >= 0; i--) {
			final IConfigurationElement configurationElement = configurationElements[i];

			String id = configurationElement.getAttribute(ATT_KEY_CONFIGURATION_ID);
			if (id != null) {
				try {
					bindingManager.setActiveScheme(bindingManager.getScheme(id));
					return;
				} catch (final NotDefinedException e) {
					// Let's keep looking....
				}
			}

			id = configurationElement.getAttribute(ATT_VALUE);
			if (id != null) {
				try {
					bindingManager.setActiveScheme(bindingManager.getScheme(id));
					return;
				} catch (final NotDefinedException e) {
					// Let's keep looking....
				}
			}
		}

		// The default default active scheme id.
		try {
			bindingManager.setActiveScheme(bindingManager.getScheme(IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID));
		} catch (final NotDefinedException e) {
			// this is bad - the default default scheme should always exist
			throw new Error("The default default active scheme id is not defined."); //$NON-NLS-1$
		}
	}

	/**
	 * Reads all of the binding definitions from the preferences.
	 *
	 * @param preferences    The memento for the commands preferences key.
	 * @param bindingManager The binding manager to which the bindings should be
	 *                       added; must not be <code>null</code>.
	 * @param commandService The command service for the workbench; must not be
	 *                       <code>null</code>.
	 */
	private static final void readBindingsFromPreferences(final IMemento preferences,
			final BindingManager bindingManager, final CommandManager commandService) {
		List<IStatus> warningsToLog = new ArrayList<>(1);

		if (preferences != null) {
			final IMemento[] preferenceMementos = preferences.getChildren(TAG_KEY_BINDING);
			int preferenceMementoCount = preferenceMementos.length;
			for (int i = preferenceMementoCount - 1; i >= 0; i--) {
				final IMemento memento = preferenceMementos[i];

				// Read out the command id.
				String commandId = readOptional(memento, ATT_COMMAND_ID);
				if (commandId == null) {
					commandId = readOptional(memento, ATT_COMMAND);
				}
				final Command command;
				if (commandId != null) {
					command = commandService.getCommand(commandId);
				} else {
					command = null;
				}

				// Read out the scheme id.
				String schemeId = readOptional(memento, ATT_KEY_CONFIGURATION_ID);
				if (schemeId == null) {
					schemeId = readRequired(memento, ATT_CONFIGURATION, warningsToLog,
							"Key bindings need a scheme or key configuration"); //$NON-NLS-1$
					if (schemeId == null) {
						continue;
					}
				}

				// Read out the context id.
				String contextId = readOptional(memento, ATT_CONTEXT_ID);
				if (contextId == null) {
					contextId = readOptional(memento, ATT_SCOPE);
				}
				if (LEGACY_DEFAULT_SCOPE.equals(contextId)) {
					contextId = null;
				}
				if (contextId == null) {
					contextId = IContextIds.CONTEXT_ID_WINDOW;
				}

				// Read out the key sequence.
				String keySequenceText = readOptional(memento, ATT_KEY_SEQUENCE);
				KeySequence keySequence = null;
				if (keySequenceText == null) {
					keySequenceText = readRequired(memento, ATT_STRING, warningsToLog,
							"Key bindings need a key sequence or string"); //$NON-NLS-1$
					if (keySequenceText == null) {
						continue;
					}

					// The key sequence is in the old-style format.
					keySequence = convert2_1Sequence(parse2_1Sequence(keySequenceText));

				} else {
					// The key sequence is in the new-style format.
					try {
						keySequence = KeySequence.getInstance(keySequenceText);
					} catch (final ParseException e) {
						addWarning(warningsToLog, "Could not parse", null, //$NON-NLS-1$
								commandId, "keySequence", keySequenceText); //$NON-NLS-1$
						continue;
					}
					if (keySequence.isEmpty() || !keySequence.isComplete()) {
						addWarning(warningsToLog, "Key bindings cannot use an empty or incomplete key sequence", //$NON-NLS-1$
								null, commandId, "keySequence", keySequence //$NON-NLS-1$
										.toString());
						continue;
					}

				}

				// Read out the locale and platform.
				final String locale = readOptional(memento, ATT_LOCALE);
				final String platform = readOptional(memento, ATT_PLATFORM);

				// Read out the parameters
				final ParameterizedCommand parameterizedCommand = command != null
						? readParameters(memento, warningsToLog, command)
						: null;

				final Binding binding = new KeyBinding(keySequence, parameterizedCommand, schemeId, contextId, locale,
						platform, null, Binding.USER);
				bindingManager.addBinding(binding);
			}
		}

		// If there were any warnings, then log them now.
		logWarnings(warningsToLog, "Warnings while parsing the key bindings from the preference store"); //$NON-NLS-1$
	}

	/**
	 * Reads all of the binding definitions from the commands extension point.
	 *
	 * @param configurationElements     The configuration elements in the commands
	 *                                  extension point; must not be
	 *                                  <code>null</code>, but may be empty.
	 * @param configurationElementCount The number of configuration elements that
	 *                                  are really in the array.
	 * @param bindingManager            The binding manager to which the bindings
	 *                                  should be added; must not be
	 *                                  <code>null</code>.
	 * @param commandService            The command service for the workbench; must
	 *                                  not be <code>null</code>.
	 */
	@SuppressWarnings("removal")
	private static final void readBindingsFromRegistry(final IConfigurationElement[] configurationElements,
			final int configurationElementCount, final BindingManager bindingManager,
			final CommandManager commandService) {
		final Collection<KeyBinding> bindings = new ArrayList<>(configurationElementCount);
		final List<IStatus> warningsToLog = new ArrayList<>(1);

		HashSet<KeyBinding> cocoaTempList = new HashSet<>();
		// IViewRegistry viewRegistry =
		// PlatformUI.getWorkbench().getViewRegistry();

		// the local cache for the sequence modifiers
		IConfigurationElement[] sequenceModifiers = new IConfigurationElement[0];
		if (configurationElementCount > 0)
			sequenceModifiers = getSequenceModifierElements(configurationElements[0]);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// different extension. update the cache ...
			if (i > 0 && !configurationElement.getDeclaringExtension()
					.equals(configurationElements[i - 1].getDeclaringExtension()))
				sequenceModifiers = getSequenceModifierElements(configurationElement);

			/*
			 * Read out the command id. Doing this before determining if the key binding is
			 * actually valid is a bit wasteful. However, it is helpful to have the command
			 * identifier when logging syntax errors.
			 */
			String commandId = readCommandId(configurationElement);

			String viewParameter = null;
			final Command command;
			if (commandId != null) {
				// TODO should we still try processing keybindings to viewIds?
				// if (viewRegistry.find(commandId) == null) {
				command = commandService.getCommand(commandId);
				if (!command.isDefined()) {
					// Reference to an undefined command. This is invalid.
					addWarning(warningsToLog, "Cannot bind to an undefined command", //$NON-NLS-1$
							configurationElement, commandId);
					continue;
				}
			} else {
				command = null;
			}

			// Read out the scheme id.
			String schemeId = readSchemeId(configurationElement, warningsToLog, commandId);
			if (isEmpty(schemeId))
				continue;

			// Read out the context id.
			String contextId = readContextId(configurationElement);

			String keySequenceText = readKeySequenceText(configurationElement);
			if (isEmpty(keySequenceText)) {
				// The key sequence should never be null. This is pointless
				addWarning(warningsToLog, "Defining a key binding with no key sequence has no effect", //$NON-NLS-1$
						configurationElement, commandId);
				continue;
			}

			// Read out the key sequence.
			KeySequence keySequence = readKeySequence(configurationElement, warningsToLog, commandId, keySequenceText);
			if (keySequence == null)
				continue;

			// Read out the locale and platform.

			String locale = readNonEmptyAttribute(configurationElement, ATT_LOCALE);
			String platform = readNonEmptyAttribute(configurationElement, ATT_PLATFORM);

			// Read out the parameters, if any.
			ParameterizedCommand parameterizedCommand = readParameterizedCommand(warningsToLog, configurationElement,
					viewParameter, command);

			List<KeyBinding> modifiedBindings = applyModifiers(keySequence, keySequenceText, platform,
					sequenceModifiers, parameterizedCommand, schemeId, contextId, locale, warningsToLog);

			KeyBinding binding = modifiedBindings.get(0);
			if (modifiedBindings.size() > 1) {
				for (int j = 1; j < modifiedBindings.size(); j++) {
					bindings.add(modifiedBindings.get(j));
				}
			}

			if (Util.WS_COCOA.equals(platform)) {
				cocoaTempList.add(binding);
			} else if (Util.WS_CARBON.equals(platform)) {
				bindings.add(binding);
				// temp work around ... simply honour the carbon
				// bindings for cocoa.
				cocoaTempList.add(new KeyBinding(keySequence, parameterizedCommand, schemeId, contextId, locale,
						Util.WS_COCOA, null, Binding.SYSTEM));
			} else {
				bindings.add(binding);
			}
		}
		if (cocoaTempList.size() > 0) {
			bindings.addAll(cocoaTempList);
		}

		final Binding[] bindingArray = bindings.toArray(new Binding[bindings.size()]);
		bindingManager.setBindings(bindingArray);

		logWarnings(warningsToLog,
				"Warnings while parsing the key bindings from the 'org.eclipse.ui.commands' and 'org.eclipse.ui.bindings' extension point"); //$NON-NLS-1$
	}

	private static List<KeyBinding> applyModifiers(KeySequence keySequence, String keySequenceText, String platform,
			IConfigurationElement[] sequenceModifiers, ParameterizedCommand parameterizedCommand, String schemeId,
			String contextId, String locale, List<IStatus> warningsToLog) {

		List<KeyBinding> bindings = new ArrayList<>();

		for (IConfigurationElement sequenceModifier : sequenceModifiers) {

			String findSequence = sequenceModifier.getAttribute(ATT_FIND);

			if (keySequenceText.startsWith(findSequence)) {
				String replaceSequence = sequenceModifier.getAttribute(ATT_REPLACE);
				String modifiedSequence = replaceSequence + keySequenceText.substring(findSequence.length());
				String platformsString = sequenceModifier.getAttribute(ATT_PLATFORMS);

				String[] platforms = parseCommaSeparatedString(platformsString);

				try {
					if (platform == null) {
						addGenericBindings(keySequence, parameterizedCommand, schemeId, contextId, locale, bindings,
								modifiedSequence, platforms);

					} else {
						getBindingForPlatform(keySequence, platform, parameterizedCommand, schemeId, contextId, locale,
								bindings, modifiedSequence, platforms);
					}
				} catch (ParseException e) {
					bindings.clear();
					addWarning(warningsToLog, "Cannot create modified sequence for key binding", //$NON-NLS-1$
							sequenceModifier, parameterizedCommand.getId(), ATT_REPLACE, replaceSequence);

				}
				break;
			}
		}

		if (bindings.isEmpty()) {
			// no modifier was applied/error occurred ...
			KeyBinding binding = new KeyBinding(keySequence, parameterizedCommand, schemeId, contextId, locale,
					platform, null, Binding.SYSTEM);
			bindings.add(binding);
		}

		return bindings;
	}

	private static void getBindingForPlatform(KeySequence keySequence, String platform,
			ParameterizedCommand parameterizedCommand, String schemeId, String contextId, String locale,
			List<KeyBinding> bindings, String modifiedSequence, String[] platforms) throws ParseException {

		int j = 0;
		for (; j < platforms.length; j++) {
			if (platforms[j].equals(platform)) {
				KeyBinding newBinding = new KeyBinding(KeySequence.getInstance(modifiedSequence), parameterizedCommand,
						schemeId, contextId, locale, platforms[j], null, Binding.SYSTEM);
				bindings.add(newBinding);
				break;
			}
		}
		if (j == platforms.length) {
			// platform doesn't match. use the unmodified sequence
			KeyBinding newBinding = new KeyBinding(keySequence, parameterizedCommand, schemeId, contextId, locale,
					platform, null, Binding.SYSTEM);
			bindings.add(newBinding);
		}
	}

	private static void addGenericBindings(KeySequence keySequence, ParameterizedCommand parameterizedCommand,
			String schemeId, String contextId, String locale, List<KeyBinding> bindings, String modifiedSequence,
			String[] platforms) throws ParseException {

		KeyBinding originalBinding = new KeyBinding(keySequence, parameterizedCommand, schemeId, contextId, locale,
				null, null, Binding.SYSTEM);
		bindings.add(originalBinding);

		String platform = SWT.getPlatform();
		boolean modifierExists = false;
		for (String currentPlatform : platforms) {
			if (currentPlatform.equals(platform)) {
				modifierExists = true;
				break;
			}
		}

		if (modifierExists) {
			KeyBinding newBinding = new KeyBinding(KeySequence.getInstance(modifiedSequence), parameterizedCommand,
					schemeId, contextId, locale, SWT.getPlatform(), null, Binding.SYSTEM);

			KeyBinding deleteBinding = new KeyBinding(keySequence, null, schemeId, contextId, locale, SWT.getPlatform(),
					null, Binding.SYSTEM);

			bindings.add(newBinding);
			bindings.add(deleteBinding);
		}

	}

	private static IConfigurationElement[] getSequenceModifierElements(IConfigurationElement configurationElement) {

		IExtension extension = configurationElement.getDeclaringExtension();
		List<IConfigurationElement> modifierElements = new ArrayList<>();
		for (final IConfigurationElement configElement : extension.getConfigurationElements()) {
			if (TAG_SEQUENCE_MODIFIER.equals(configElement.getName()))
				modifierElements.add(configElement);
		}
		return modifierElements.toArray(new IConfigurationElement[modifierElements.size()]);
	}

	public static String[] parseCommaSeparatedString(String commaSeparatedString) {
		StringTokenizer tokenizer = new StringTokenizer(commaSeparatedString, ", "); //$NON-NLS-1$
		int count = tokenizer.countTokens();
		String[] tokens = new String[count];
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = tokenizer.nextToken();
		}
		return tokens;
	}

	private static String readKeySequenceText(IConfigurationElement configurationElement) {

		String keySequenceText = configurationElement.getAttribute(ATT_SEQUENCE);
		if (isEmpty(keySequenceText)) {
			keySequenceText = configurationElement.getAttribute(ATT_KEY_SEQUENCE);
		}
		if (isEmpty(keySequenceText))
			keySequenceText = configurationElement.getAttribute(ATT_STRING);

		return keySequenceText;

	}

	private static KeySequence readKeySequence(IConfigurationElement configurationElement, List<IStatus> warningsToLog,
			String commandId, String keySequenceText) {

		KeySequence keySequence = null;
		if (keySequenceText.equals(configurationElement.getAttribute(ATT_STRING))) {
			// The key sequence is in the old-style format.
			try {
				keySequence = convert2_1Sequence(parse2_1Sequence(keySequenceText));
			} catch (final IllegalArgumentException e) {
				addWarning(warningsToLog, "Could not parse key sequence", //$NON-NLS-1$
						configurationElement, commandId, "keySequence", //$NON-NLS-1$
						keySequenceText);
				return null;
			}
		} else {
			// The key sequence is in the new-style format.
			try {
				keySequence = KeySequence.getInstance(keySequenceText);
			} catch (final ParseException e) {
				addWarning(warningsToLog, "Could not parse key sequence", //$NON-NLS-1$
						configurationElement, commandId, "keySequence", //$NON-NLS-1$
						keySequenceText);
				return null;
			}
			if (keySequence.isEmpty() || !keySequence.isComplete()) {
				addWarning(warningsToLog, "Key bindings should not have an empty or incomplete key sequence", //$NON-NLS-1$
						configurationElement, commandId, "keySequence", //$NON-NLS-1$
						keySequence.toString());
				return null;
			}

		}
		return keySequence;
	}

	private static ParameterizedCommand readParameterizedCommand(final List<IStatus> warningsToLog,
			final IConfigurationElement configurationElement, String viewParameter, final Command command) {
		final ParameterizedCommand parameterizedCommand;
		if (command == null) {
			parameterizedCommand = null;
		} else if (viewParameter != null) {
			HashMap<String, String> parms = new HashMap<>();
			parms.put(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID, viewParameter);
			parameterizedCommand = ParameterizedCommand.generateCommand(command, parms);
		} else {
			parameterizedCommand = readParameters(configurationElement, warningsToLog, command);
		}
		return parameterizedCommand;
	}

	/**
	 * Reads the specified attribute from the configuration element. If the value is
	 * an empty string, will return null.
	 *
	 * @return the attribute value
	 */
	private static String readNonEmptyAttribute(IConfigurationElement configurationElement, String attribute) {
		String attributeValue = configurationElement.getAttribute(attribute);
		if ((attributeValue != null) && (attributeValue.isEmpty())) {
			attributeValue = null;
		}
		return attributeValue;
	}

	private static String readContextId(final IConfigurationElement configurationElement) {
		String contextId = configurationElement.getAttribute(ATT_CONTEXT_ID);
		if (LEGACY_DEFAULT_SCOPE.equals(contextId)) {
			contextId = null;
		} else if ((contextId == null) || (contextId.isEmpty())) {
			contextId = configurationElement.getAttribute(ATT_SCOPE);
			if (LEGACY_DEFAULT_SCOPE.equals(contextId)) {
				contextId = null;
			}
		}
		if ((contextId == null) || (contextId.isEmpty())) {
			contextId = IContextIds.CONTEXT_ID_WINDOW;
		}
		return contextId;
	}

	private static String readSchemeId(IConfigurationElement configurationElement, List<IStatus> warningsToLog,
			String commandId) {

		String schemeId = configurationElement.getAttribute(ATT_SCHEME_ID);
		if ((schemeId == null) || (schemeId.isEmpty())) {
			schemeId = configurationElement.getAttribute(ATT_KEY_CONFIGURATION_ID);
			if ((schemeId == null) || (schemeId.isEmpty())) {
				schemeId = configurationElement.getAttribute(ATT_CONFIGURATION);
				if ((schemeId == null) || (schemeId.isEmpty())) {
					// The scheme id should never be null. This is invalid.
					addWarning(warningsToLog, "Key bindings need a scheme", //$NON-NLS-1$
							configurationElement, commandId);
				}
			}
		}
		return schemeId;
	}

	private static String readCommandId(final IConfigurationElement configurationElement) {
		String commandId = configurationElement.getAttribute(ATT_COMMAND_ID);
		if ((commandId == null) || (commandId.isEmpty())) {
			commandId = configurationElement.getAttribute(ATT_COMMAND);
		}
		if ((commandId != null) && (commandId.isEmpty())) {
			commandId = null;
		}
		return commandId;
	}

	private static boolean isEmpty(String string) {
		return string == null || string.isEmpty();
	}

	/**
	 * Reads all of the scheme definitions from the registry.
	 *
	 * @param configurationElements     The configuration elements in the commands
	 *                                  extension point; must not be
	 *                                  <code>null</code>, but may be empty.
	 * @param configurationElementCount The number of configuration elements that
	 *                                  are really in the array.
	 * @param bindingManager            The binding manager to which the schemes
	 *                                  should be added; must not be
	 *                                  <code>null</code>.
	 */
	private static final void readSchemesFromRegistry(final IConfigurationElement[] configurationElements,
			final int configurationElementCount, final BindingManager bindingManager) {
		// Undefine all the previous handle objects.
		final HandleObject[] handleObjects = bindingManager.getDefinedSchemes();
		if (handleObjects != null) {
			for (HandleObject handleObject : handleObjects) {
				handleObject.undefine();
			}
		}

		final List<IStatus> warningsToLog = new ArrayList<>(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the attributes.
			final String id = readRequired(configurationElement, ATT_ID, warningsToLog, "Schemes need an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}
			final String name = readRequired(configurationElement, ATT_NAME, warningsToLog, "A scheme needs a name", //$NON-NLS-1$
					id);
			if (name == null) {
				continue;
			}
			final String description = readOptional(configurationElement, ATT_DESCRIPTION);

			String parentId = configurationElement.getAttribute(ATT_PARENT_ID);
			if ((parentId != null) && (parentId.isEmpty())) {
				parentId = configurationElement.getAttribute(ATT_PARENT);
				if ((parentId != null) && (parentId.isEmpty())) {
					parentId = null;
				}
			}

			// Define the scheme.
			final Scheme scheme = bindingManager.getScheme(id);
			scheme.define(name, description, parentId);
		}

		logWarnings(warningsToLog,
				"Warnings while parsing the key bindings from the 'org.eclipse.ui.bindings', 'org.eclipse.ui.acceleratorConfigurations' and 'org.eclipse.ui.commands' extension point"); //$NON-NLS-1$
	}

	/**
	 * Writes the given active scheme and bindings to the preference store. Only
	 * bindings that are of the <code>Binding.USER</code> type will be written; the
	 * others will be ignored.
	 *
	 * @param activeScheme The scheme which should be persisted; may be
	 *                     <code>null</code>.
	 * @param bindings     The bindings which should be persisted; may be
	 *                     <code>null</code>
	 * @throws IOException If something happens while trying to write to the
	 *                     workbench preference store.
	 */
	static final void write(final Scheme activeScheme, final Binding[] bindings) throws IOException {
		// Print out debugging information, if requested.
		if (DEBUG) {
			Tracing.printTrace("BINDINGS", "Persisting active scheme '" //$NON-NLS-1$ //$NON-NLS-2$
					+ activeScheme.getId() + '\'');
			Tracing.printTrace("BINDINGS", "Persisting bindings"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Write the simple preference key to the UI preference store.
		writeActiveScheme(activeScheme);

		// Build the XML block for writing the bindings and active scheme.
		final XMLMemento xmlMemento = XMLMemento.createWriteRoot(EXTENSION_COMMANDS);
		if (activeScheme != null) {
			writeActiveSchemeToPreferences(xmlMemento, activeScheme);
		}
		if (bindings != null) {
			final int bindingsLength = bindings.length;
			for (int i = 0; i < bindingsLength; i++) {
				final Binding binding = bindings[i];
				if (binding.getType() == Binding.USER) {
					writeBindingToPreferences(xmlMemento, binding);
				}
			}
		}

		// Write the XML block to the workbench preference store.
		final IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		try (Writer writer = new StringWriter()) {
			xmlMemento.save(writer);
			preferenceStore.setValue(EXTENSION_COMMANDS, writer.toString());
		}
	}

	/**
	 * Writes the active scheme to its own preference key. This key is used by RCP
	 * applications as part of their plug-in customization.
	 *
	 * @param scheme The scheme to write to the preference store. If the scheme is
	 *               <code>null</code>, then it is removed.
	 */
	private static final void writeActiveScheme(final Scheme scheme) {
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final String schemeId = (scheme == null) ? null : scheme.getId();
		final String defaultSchemeId = store.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		if ((defaultSchemeId == null) ? (scheme != null) : (!defaultSchemeId.equals(schemeId))) {
			store.setValue(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID, scheme.getId());
		} else {
			store.setToDefault(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		}
	}

	/**
	 * Writes the active scheme to the memento. If the scheme is <code>null</code>,
	 * then all schemes in the memento are removed.
	 *
	 * @param memento The memento to which the scheme should be written; must not be
	 *                <code>null</code>.
	 * @param scheme  The scheme that should be written; must not be
	 *                <code>null</code>.
	 */
	private static final void writeActiveSchemeToPreferences(final IMemento memento, final Scheme scheme) {
		// Add this active scheme, if it is not the default.
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final String schemeId = scheme.getId();
		final String defaultSchemeId = store.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		if ((defaultSchemeId == null) ? (schemeId != null) : (!defaultSchemeId.equals(schemeId))) {
			final IMemento child = memento.createChild(TAG_ACTIVE_KEY_CONFIGURATION);
			child.putString(ATT_KEY_CONFIGURATION_ID, schemeId);
		}
	}

	/**
	 * Writes the binding to the memento. This creates a new child element on the
	 * memento, and places the properties of the binding as its attributes.
	 *
	 * @param parent  The parent memento for the binding element; must not be
	 *                <code>null</code>.
	 * @param binding The binding to write; must not be <code>null</code>.
	 */
	private static final void writeBindingToPreferences(final IMemento parent, final Binding binding) {
		final IMemento element = parent.createChild(TAG_KEY_BINDING);
		element.putString(ATT_CONTEXT_ID, binding.getContextId());
		final ParameterizedCommand parameterizedCommand = binding.getParameterizedCommand();
		final String commandId = (parameterizedCommand == null) ? null : parameterizedCommand.getId();
		element.putString(ATT_COMMAND_ID, commandId);
		element.putString(ATT_KEY_CONFIGURATION_ID, binding.getSchemeId());
		element.putString(ATT_KEY_SEQUENCE, binding.getTriggerSequence().toString());
		element.putString(ATT_LOCALE, binding.getLocale());
		element.putString(ATT_PLATFORM, binding.getPlatform());
		if (parameterizedCommand != null) {
			final Map<String, String> parameterizations = parameterizedCommand.getParameterMap();
			final Iterator<Map.Entry<String, String>> parameterizationItr = parameterizations.entrySet().iterator();
			while (parameterizationItr.hasNext()) {
				final Map.Entry<String, String> entry = parameterizationItr.next();
				final String id = entry.getKey();
				final String value = entry.getValue();
				final IMemento parameterElement = element.createChild(TAG_PARAMETER);
				parameterElement.putString(ATT_ID, id);
				parameterElement.putString(ATT_VALUE, value);
			}
		}
	}

	/**
	 * The binding manager which should be populated with the values from the
	 * registry and preference store; must not be <code>null</code>.
	 */
	private final BindingManager bindingManager;

	/**
	 * The command service for the workbench; must not be <code>null</code>.
	 */
	private final CommandManager commandManager;

	/**
	 * Constructs a new instance of <code>BindingPersistence</code>.
	 *
	 * @param bindingManager The binding manager which should be populated with the
	 *                       values from the registry and preference store; must not
	 *                       be <code>null</code>.
	 * @param commandManager The command manager for the workbench; must not be
	 *                       <code>null</code>.
	 */
	public BindingPersistence(final BindingManager bindingManager, final CommandManager commandManager) {
		this.bindingManager = bindingManager;
		this.commandManager = commandManager;
		// HACK. Calling super.read() installs a required preferences change listener.
		// See bug 266604.
		super.read();
	}

	@Override
	protected final boolean isChangeImportant(final IRegistryChangeEvent event) {
		return false;
	}

	public boolean bindingsNeedUpdating(final IRegistryChangeEvent event) {
		final IExtensionDelta[] acceleratorConfigurationDeltas = event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_ACCELERATOR_CONFIGURATIONS);
		if (acceleratorConfigurationDeltas.length == 0) {
			final IExtensionDelta[] bindingDeltas = event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
					IWorkbenchRegistryConstants.PL_BINDINGS);
			if (bindingDeltas.length == 0) {
				final IExtensionDelta[] commandDeltas = event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
						IWorkbenchRegistryConstants.PL_COMMANDS);
				if (commandDeltas.length == 0) {
					final IExtensionDelta[] acceleratorScopeDeltas = event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
							IWorkbenchRegistryConstants.PL_ACCELERATOR_SCOPES);
					if (acceleratorScopeDeltas.length == 0) {
						final IExtensionDelta[] contextDeltas = event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
								IWorkbenchRegistryConstants.PL_CONTEXTS);
						if (contextDeltas.length == 0) {
							final IExtensionDelta[] actionDefinitionDeltas = event.getExtensionDeltas(
									PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_ACTION_DEFINITIONS);
							if (actionDefinitionDeltas.length == 0) {
								return false;
							}
						}
					}
				}
			}
		}

		return true;
	}

	@Override
	protected final boolean isChangeImportant(final PropertyChangeEvent event) {
		return EXTENSION_COMMANDS.equals(event.getProperty());
	}

	/**
	 * Reads all of the binding information from the registry and from the
	 * preference store.
	 */
	@Override
	public final void read() {
		super.read();
		reRead();
	}

	public void reRead() {
		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int activeSchemeElementCount = 0;
		int bindingDefinitionCount = 0;
		int schemeDefinitionCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[3][];

		// Sort the bindings extension point based on element name.
		final IConfigurationElement[] bindingsExtensionPoint = registry.getConfigurationElementsFor(EXTENSION_BINDINGS);
		for (final IConfigurationElement configurationElement : bindingsExtensionPoint) {
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (TAG_KEY.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements, INDEX_BINDING_DEFINITIONS,
						bindingDefinitionCount++);
			} else
			// Check to see if it is a scheme definition.
			if (TAG_SCHEME.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements, INDEX_SCHEME_DEFINITIONS,
						schemeDefinitionCount++);
			}

		}

		// Sort the commands extension point based on element name.
		final IConfigurationElement[] commandsExtensionPoint = registry.getConfigurationElementsFor(EXTENSION_COMMANDS);
		for (final IConfigurationElement configurationElement : commandsExtensionPoint) {
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (TAG_KEY_BINDING.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements, INDEX_BINDING_DEFINITIONS,
						bindingDefinitionCount++);

				// Check if it is a scheme defintion.
			} else if (TAG_KEY_CONFIGURATION.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements, INDEX_SCHEME_DEFINITIONS,
						schemeDefinitionCount++);

				// Check if it is an active scheme identifier.
			} else if (TAG_ACTIVE_KEY_CONFIGURATION.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements, INDEX_ACTIVE_SCHEME,
						activeSchemeElementCount++);
			}
		}

		/*
		 * Sort the accelerator configuration extension point into the scheme
		 * definitions.
		 */
		final IConfigurationElement[] acceleratorConfigurationsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_ACCELERATOR_CONFIGURATIONS);
		for (final IConfigurationElement configurationElement : acceleratorConfigurationsExtensionPoint) {
			final String name = configurationElement.getName();

			// Check if the name matches the accelerator configuration element
			if (TAG_ACCELERATOR_CONFIGURATION.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements, INDEX_SCHEME_DEFINITIONS,
						schemeDefinitionCount++);
			}
		}

		// Create the preference memento.
		final IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		final String preferenceString = store.getString(EXTENSION_COMMANDS);
		IMemento preferenceMemento = null;
		if ((preferenceString != null) && (preferenceString.length() > 0)) {
			final Reader reader = new StringReader(preferenceString);
			try {
				preferenceMemento = XMLMemento.createReadRoot(reader);
			} catch (final WorkbenchException e) {
				// Could not initialize the preference memento.
			}
		}

		// Read the scheme definitions.
		readSchemesFromRegistry(indexedConfigurationElements[INDEX_SCHEME_DEFINITIONS], schemeDefinitionCount,
				bindingManager);
		readActiveScheme(indexedConfigurationElements[INDEX_ACTIVE_SCHEME], activeSchemeElementCount, preferenceMemento,
				bindingManager);
		readBindingsFromRegistry(indexedConfigurationElements[INDEX_BINDING_DEFINITIONS], bindingDefinitionCount,
				bindingManager, commandManager);
		readBindingsFromPreferences(preferenceMemento, bindingManager, commandManager);
	}
}
