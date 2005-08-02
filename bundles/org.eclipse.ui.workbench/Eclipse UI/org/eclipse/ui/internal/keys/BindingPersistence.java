/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.keys.IBindingService;

/**
 * <p>
 * A static class for accessing the registry and the preference store.
 * </p>
 * 
 * @since 3.1
 */
public final class BindingPersistence {

	/**
	 * The name of the attribute storing the command id for a binding.
	 */
	private static final String ATTRIBUTE_COMMAND = "command"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the command id for a binding.
	 */
	private static final String ATTRIBUTE_COMMAND_ID = "commandId"; //$NON-NLS-1$

	/**
	 * The name of the configuration attribute storing the scheme id for a
	 * binding.
	 */
	private static final String ATTRIBUTE_CONFIGURATION = "configuration"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the context id for a binding.
	 */
	private static final String ATTRIBUTE_CONTEXT_ID = "contextId"; //$NON-NLS-1$

	/**
	 * The name of the description attribute, which appears on a scheme
	 * definition.
	 */
	private static final String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$

	/**
	 * The name of the id attribute, which is used on scheme definitions.
	 */
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the identifier for the active key
	 * configuration identifier. This provides legacy support for the
	 * <code>activeKeyConfiguration</code> element in the commands extension
	 * point.
	 */
	private static final String ATTRIBUTE_KEY_CONFIGURATION_ID = "keyConfigurationId"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the trigger sequence for a binding.
	 * This is called a 'keySequence' for legacy reasons.
	 */
	private static final String ATTRIBUTE_KEY_SEQUENCE = "keySequence"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the locale for a binding.
	 */
	private static final String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$

	/**
	 * The name of the name attribute, which appears on scheme definitions.
	 */
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

	/**
	 * The name of the deprecated parent attribute, which appears on scheme
	 * definitions.
	 */
	private static final String ATTRIBUTE_PARENT = "parent"; //$NON-NLS-1$

	/**
	 * The name of the parent id attribute, which appears on scheme definitions.
	 */
	private static final String ATTRIBUTE_PARENT_ID = "parentId"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the platform for a binding.
	 */
	private static final String ATTRIBUTE_PLATFORM = "platform"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the identifier for the active scheme.
	 * This is called a 'keyConfigurationId' for legacy reasons.
	 */
	private static final String ATTRIBUTE_SCHEME_ID = "schemeId"; //$NON-NLS-1$

	/**
	 * The name of the scope attribute for a binding.
	 */
	private static final String ATTRIBUTE_SCOPE = "scope"; //$NON-NLS-1$

	/**
	 * The name of the sequence attribute for a key binding.
	 */
	private static final String ATTRIBUTE_SEQUENCE = "sequence"; //$NON-NLS-1$

	/**
	 * The name of the string attribute (key sequence) for a binding in the
	 * commands extension point.
	 */
	private static final String ATTRIBUTE_STRING = "string"; //$NON-NLS-1$

	/**
	 * The name of the deprecated attribute of the deprecated
	 * <code>activeKeyConfiguration</code> element in the commands extension
	 * point.
	 */
	private static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

	/**
	 * Whether this class should print out debugging information when it reads
	 * in data, or writes to the preference store.
	 */
	private static final boolean DEBUG = Policy.DEBUG_KEY_BINDINGS;

	/**
	 * The name of the deprecated accelerator configuration element. This
	 * element was used in 2.1.x and earlier to define groups of what are now
	 * called schemes.
	 */
	private static final String ELEMENT_ACCELERATOR_CONFIGURATION = "acceleratorConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the element storing the active key configuration from the
	 * commands extension point.
	 */
	private static final String ELEMENT_ACTIVE_KEY_CONFIGURATION = "activeKeyConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the element storing the active scheme. This is called a
	 * 'keyConfiguration' for legacy reasons.
	 */
	private static final String ELEMENT_ACTIVE_SCHEME = ELEMENT_ACTIVE_KEY_CONFIGURATION;

	/**
	 * The name of the element storing the binding. This is called a
	 * 'keyBinding' for legacy reasons.
	 */
	private static final String ELEMENT_BINDING = "keyBinding"; //$NON-NLS-1$

	/**
	 * The name of the element storing a key binding.
	 */
	private static final String ELEMENT_KEY = "key"; //$NON-NLS-1$

	/**
	 * The name of the key binding element in the commands extension point.
	 */
	private static final String ELEMENT_KEY_BINDING = "keyBinding"; //$NON-NLS-1$

	/**
	 * The name of the deprecated key configuration element in the commands
	 * extension point. This element has been replaced with the scheme element
	 * in the bindings extension point.
	 */
	private static final String ELEMENT_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the element storing a parameter.
	 */
	private static final String ELEMENT_PARAMETER = "parameter"; //$NON-NLS-1$

	/**
	 * The name of the scheme element in the bindings extension point.
	 */
	private static final String ELEMENT_SCHEME = "scheme"; //$NON-NLS-1$

	/**
	 * The name of the deprecated accelerator configurations extension point.
	 */
	private static final String EXTENSION_ACCELERATOR_CONFIGURATIONS = "org.eclipse.ui.acceleratorConfigurations"; //$NON-NLS-1$

	/**
	 * The name of the bindings extension point.
	 */
	private static final String EXTENSION_BINDINGS = "org.eclipse.ui.bindings"; //$NON-NLS-1$

	/**
	 * The name of the commands extension point, and the name of the key for the
	 * commands preferences.
	 */
	private static final String EXTENSION_COMMANDS = "org.eclipse.ui.commands"; //$NON-NLS-1$

	/**
	 * The index of the active scheme configuration elements in the indexed
	 * array.
	 * 
	 * @see BindingPersistence#read(BindingManager, ICommandService)
	 */
	private static final int INDEX_ACTIVE_SCHEME = 0;

	/**
	 * The index of the binding definition configuration elements in the indexed
	 * array.
	 * 
	 * @see BindingPersistence#read(BindingManager, ICommandService)
	 */
	private static final int INDEX_BINDING_DEFINITIONS = 1;

	/**
	 * The index of the scheme definition configuration elements in the indexed
	 * array.
	 * 
	 * @see BindingPersistence#read(BindingManager, ICommandService)
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
	private static final Map r2_1KeysByName = new HashMap();
    
    /**
     * Whether the property change listener has been attached yet.
     */
    private static boolean propertyChangeListenerAttached = false;

	static {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		r2_1KeysByName.put(IKeyLookup.BACKSPACE_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.BACKSPACE_NAME));
		r2_1KeysByName.put(IKeyLookup.TAB_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.TAB_NAME));
		r2_1KeysByName.put(IKeyLookup.RETURN_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.RETURN_NAME));
		r2_1KeysByName.put(IKeyLookup.ENTER_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.ENTER_NAME));
		r2_1KeysByName.put(IKeyLookup.ESCAPE_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.ESCAPE_NAME));
		r2_1KeysByName.put(IKeyLookup.ESC_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.ESC_NAME));
		r2_1KeysByName.put(IKeyLookup.DELETE_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.DELETE_NAME));
		r2_1KeysByName.put(IKeyLookup.SPACE_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.SPACE_NAME));
		r2_1KeysByName.put(IKeyLookup.ARROW_UP_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.ARROW_UP_NAME));
		r2_1KeysByName.put(IKeyLookup.ARROW_DOWN_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.ARROW_DOWN_NAME));
		r2_1KeysByName.put(IKeyLookup.ARROW_LEFT_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.ARROW_LEFT_NAME));
		r2_1KeysByName.put(IKeyLookup.ARROW_RIGHT_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.ARROW_RIGHT_NAME));
		r2_1KeysByName.put(IKeyLookup.PAGE_UP_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.PAGE_UP_NAME));
		r2_1KeysByName.put(IKeyLookup.PAGE_DOWN_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.PAGE_DOWN_NAME));
		r2_1KeysByName.put(IKeyLookup.HOME_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.HOME_NAME));
		r2_1KeysByName.put(IKeyLookup.END_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.END_NAME));
		r2_1KeysByName.put(IKeyLookup.INSERT_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.INSERT_NAME));
		r2_1KeysByName.put(IKeyLookup.F1_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F1_NAME));
		r2_1KeysByName.put(IKeyLookup.F2_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F2_NAME));
		r2_1KeysByName.put(IKeyLookup.F3_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F3_NAME));
		r2_1KeysByName.put(IKeyLookup.F4_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F4_NAME));
		r2_1KeysByName.put(IKeyLookup.F5_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F5_NAME));
		r2_1KeysByName.put(IKeyLookup.F6_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F6_NAME));
		r2_1KeysByName.put(IKeyLookup.F7_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F7_NAME));
		r2_1KeysByName.put(IKeyLookup.F8_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F8_NAME));
		r2_1KeysByName.put(IKeyLookup.F9_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F9_NAME));
		r2_1KeysByName.put(IKeyLookup.F10_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F10_NAME));
		r2_1KeysByName.put(IKeyLookup.F11_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F11_NAME));
		r2_1KeysByName.put(IKeyLookup.F12_NAME, lookup
				.formalKeyLookupInteger(IKeyLookup.F12_NAME));
	}

	/**
	 * Inserts the given element into the indexed two-dimensional array in the
	 * array at the index. The array is grown as necessary.
	 * 
	 * @param elementToAdd
	 *            The element to add to the indexed array; may be
	 *            <code>null</code>
	 * @param indexedArray
	 *            The two-dimensional array that is indexed by element type;
	 *            must not be <code>null</code>.
	 * @param index
	 *            The index at which the element should be added; must be a
	 *            valid index.
	 * @param currentCount
	 *            The current number of items in the array at the index.
	 */
	private static final void addElementToIndexedArray(
			final IConfigurationElement elementToAdd,
			final IConfigurationElement[][] indexedArray, final int index,
			final int currentCount) {
		final IConfigurationElement[] elements;
		if (currentCount == 0) {
			elements = new IConfigurationElement[1];
			indexedArray[index] = elements;
		} else {
			if (currentCount >= indexedArray[index].length) {
				final IConfigurationElement[] copy = new IConfigurationElement[indexedArray[index].length * 2];
				System.arraycopy(indexedArray[index], 0, copy, 0, currentCount);
				elements = copy;
				indexedArray[index] = elements;
			} else {
				elements = indexedArray[index];
			}
		}
		elements[currentCount] = elementToAdd;
	}

	/**
	 * Converts a 2.1.x style key sequence (as parsed from the
	 * <code>string</code> attribute of the <code>keyBinding</code>) to a
	 * 3.1 key sequence.
	 * 
	 * @param r21KeySequence
	 *            The sequence of 2.1.x key strokes that should be converted
	 *            into a 3.1 key sequence; never <code>null</code>.
	 * @return A 3.1 key sequence; never <code>null</code>.
	 */
	private static final KeySequence convert2_1Sequence(int[] r21KeySequence) {
		final int r21KeySequenceLength = r21KeySequence.length;
		final KeyStroke[] keyStrokes = new KeyStroke[r21KeySequenceLength];
		for (int i = 0; i < r21KeySequenceLength; i++)
			keyStrokes[i] = convert2_1Stroke(r21KeySequence[i]);

		return KeySequence.getInstance(keyStrokes);
	}

	/**
	 * Converts a 2.1.x style key stroke (as parsed from the <code>string</code>
	 * attribute of the <code>keyBinding</code> to a 3.1 key stroke.
	 * 
	 * @param r21Stroke
	 *            The 2.1.x stroke to convert; must never be <code>null</code>.
	 * @return A 3.1 key stroke; never <code>null</code>.
	 */
	private static final KeyStroke convert2_1Stroke(final int r21Stroke) {
		return SWTKeySupport.convertAcceleratorToKeyStroke(r21Stroke);
	}

	/**
	 * Returns the default scheme identifier for the currently running
	 * application.
	 * 
	 * @return The default scheme identifier (<code>String</code>); never
	 *         <code>null</code>, but may be empty or point to an undefined
	 *         scheme.
	 */
	static final String getDefaultSchemeId() {
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		return store
				.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
	}

	/**
	 * Parses a 2.1.x <code>string</code> attribute of the
	 * <code>keyBinding</code> element.
	 * 
	 * @param string
	 *            The string to parse; must not be <code>null</code>.
	 * @return An array of integer values -- each integer representing a single
	 *         key stroke. This array may be empty, but it is never
	 *         <code>null</code>.
	 */
	private static final int[] parse2_1Sequence(final String string) {
		final StringTokenizer stringTokenizer = new StringTokenizer(string);
		final int length = stringTokenizer.countTokens();
		final int[] strokes = new int[length];

		for (int i = 0; i < length; i++)
			strokes[i] = parse2_1Stroke(stringTokenizer.nextToken());

		return strokes;
	}

	/**
	 * Parses a single 2.1.x key stroke string, as provided by
	 * <code>parse2_1Sequence</code>.
	 * 
	 * @param string
	 *            The string to parse; must not be <code>null</code>.
	 * @return An single integer value representing this key stroke.
	 */
	private static final int parse2_1Stroke(final String string) {
		final StringTokenizer stringTokenizer = new StringTokenizer(string,
				KeyStroke.KEY_DELIMITER, true);

		// Copy out the tokens so we have random access.
		final int size = stringTokenizer.countTokens();
		final String[] tokens = new String[size];
		for (int i = 0; stringTokenizer.hasMoreTokens(); i++) {
			tokens[i] = stringTokenizer.nextToken();
		}

		int value = 0;
		if (size % 2 == 1) {
			String token = tokens[size - 1];
			final Integer integer = (Integer) r2_1KeysByName.get(token
					.toUpperCase());

			if (integer != null)
				value = integer.intValue();
			else if (token.length() == 1)
				value = token.toUpperCase().charAt(0);

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

						} else if (token
								.equalsIgnoreCase(IKeyLookup.SHIFT_NAME)) {
							if ((value & SWT.SHIFT) != 0) {
								return 0;
							}

							value |= SWT.SHIFT;

						} else if (token
								.equalsIgnoreCase(IKeyLookup.COMMAND_NAME)) {
							if ((value & SWT.COMMAND) != 0) {
								return 0;
							}

							value |= SWT.COMMAND;

						} else {
							return 0;

						}

					} else if (!KeyStroke.KEY_DELIMITER.equals(token))
						return 0;
				}
			}
		}

		return value;
	}

	/**
     * Reads all of the binding information from the registry and from the
     * preference store.
     * 
     * @param bindingManager
     *            The binding manager which should be populated with the values
     *            from the registry and preference store; must not be
     *            <code>null</code>.
     * @param commandService
     *            The command service for the workbench; must not be
     *            <code>null</code>.
     */
    static final void read(final BindingManager bindingManager,
            final ICommandService commandService) {
        // Create the extension registry mementos.
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        int activeSchemeElementCount = 0;
        int bindingDefinitionCount = 0;
        int schemeDefinitionCount = 0;
        final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[3][];

        // Sort the bindings extension point based on element name.
        final IConfigurationElement[] bindingsExtensionPoint = registry
                .getConfigurationElementsFor(EXTENSION_BINDINGS);
        for (int i = 0; i < bindingsExtensionPoint.length; i++) {
            final IConfigurationElement configurationElement = bindingsExtensionPoint[i];
            final String name = configurationElement.getName();

            // Check if it is a binding definition.
            if (ELEMENT_KEY.equals(name)) {
                addElementToIndexedArray(configurationElement,
                        indexedConfigurationElements,
                        INDEX_BINDING_DEFINITIONS, bindingDefinitionCount++);
            } else
            // Check to see if it is a scheme definition.
            if (ELEMENT_SCHEME.equals(name)) {
                addElementToIndexedArray(configurationElement,
                        indexedConfigurationElements, INDEX_SCHEME_DEFINITIONS,
                        schemeDefinitionCount++);
            }

        }

        // Sort the commands extension point based on element name.
        final IConfigurationElement[] commandsExtensionPoint = registry
                .getConfigurationElementsFor(EXTENSION_COMMANDS);
        for (int i = 0; i < commandsExtensionPoint.length; i++) {
            final IConfigurationElement configurationElement = commandsExtensionPoint[i];
            final String name = configurationElement.getName();

            // Check if it is a binding definition.
            if (ELEMENT_KEY_BINDING.equals(name)) {
                addElementToIndexedArray(configurationElement,
                        indexedConfigurationElements,
                        INDEX_BINDING_DEFINITIONS, bindingDefinitionCount++);

                // Check if it is a scheme defintion.
            } else if (ELEMENT_KEY_CONFIGURATION.equals(name)) {
                addElementToIndexedArray(configurationElement,
                        indexedConfigurationElements, INDEX_SCHEME_DEFINITIONS,
                        schemeDefinitionCount++);

                // Check if it is an active scheme identifier.
            } else if (ELEMENT_ACTIVE_KEY_CONFIGURATION.equals(name)) {
                addElementToIndexedArray(configurationElement,
                        indexedConfigurationElements, INDEX_ACTIVE_SCHEME,
                        activeSchemeElementCount++);
            }
        }

        /*
         * Sort the accelerator configuration extension point into the scheme
         * definitions.
         */
        final IConfigurationElement[] acceleratorConfigurationsExtensionPoint = registry
                .getConfigurationElementsFor(EXTENSION_ACCELERATOR_CONFIGURATIONS);
        for (int i = 0; i < acceleratorConfigurationsExtensionPoint.length; i++) {
            final IConfigurationElement configurationElement = acceleratorConfigurationsExtensionPoint[i];
            final String name = configurationElement.getName();

            // Check if the name matches the accelerator configuration element
            if (ELEMENT_ACCELERATOR_CONFIGURATION.equals(name)) {
                addElementToIndexedArray(configurationElement,
                        indexedConfigurationElements, INDEX_SCHEME_DEFINITIONS,
                        schemeDefinitionCount++);
            }
        }

        // Create the preference memento.
        final IPreferenceStore store = WorkbenchPlugin.getDefault()
                .getPreferenceStore();
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
        readSchemes(indexedConfigurationElements[INDEX_SCHEME_DEFINITIONS],
                schemeDefinitionCount, bindingManager);
        readActiveScheme(indexedConfigurationElements[INDEX_ACTIVE_SCHEME],
                activeSchemeElementCount, preferenceMemento, bindingManager);
        readBindingsFromRegistry(
                indexedConfigurationElements[INDEX_BINDING_DEFINITIONS],
                bindingDefinitionCount, bindingManager, commandService);
        readBindingsFromPreferences(preferenceMemento, bindingManager,
                commandService);

        /*
         * Add a listener so that future preference changes trigger an update of
         * the binding manager automatically.
         */
        if (!propertyChangeListenerAttached) {
            store.addPropertyChangeListener(new IPropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    if (EXTENSION_COMMANDS.equals(event.getProperty())) {
                        read(bindingManager, commandService);
                    }
                }
            });
            propertyChangeListenerAttached = true;
        }
    }


	/**
	 * <p>
	 * Reads the registry and the preference store, and determines the
	 * identifier for the scheme that should be active. There is a complicated
	 * order of priorities for this. The registry will only be read if there is
	 * no user preference, and the default active scheme id is different than
	 * the default default active scheme id.
	 * </p>
	 * <ol>
	 * <li>A non-default preference.</li>
	 * <li>The legacy preference XML memento.</li>
	 * <li>A default preference value that is different than the default
	 * default active scheme id.</li>
	 * <li>The registry.</li>
	 * <li>The default default active scheme id.</li>
	 * </ol>
	 * 
	 * @param configurationElements
	 *            The configuration elements from the commands extension point;
	 *            must not be <code>null</code>.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param preferences
	 *            The memento wrapping the commands preference key; may be
	 *            <code>null</code>.
	 * @param bindingManager
	 *            The binding manager that should be updated with the active
	 *            scheme. This binding manager must already have its schemes
	 *            defined. This value must not be <code>null</code>.
	 */
	private static final void readActiveScheme(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount, final IMemento preferences,
			final BindingManager bindingManager) {
		// A non-default preference.
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final String defaultActiveSchemeId = store
				.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		final String preferenceActiveSchemeId = store
				.getString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		if ((preferenceActiveSchemeId != null)
				&& (!preferenceActiveSchemeId.equals(defaultActiveSchemeId))) {
			try {
				bindingManager.setActiveScheme(bindingManager
						.getScheme(preferenceActiveSchemeId));
				return;
			} catch (final NotDefinedException e) {
				// Let's keep looking....
			}
		}

		// A legacy preference XML memento.
		if (preferences != null) {
			final IMemento[] preferenceMementos = preferences
					.getChildren(ELEMENT_ACTIVE_KEY_CONFIGURATION);
			int preferenceMementoCount = preferenceMementos.length;
			for (int i = preferenceMementoCount - 1; i >= 0; i--) {
				final IMemento memento = preferenceMementos[i];
				String id = memento.getString(ATTRIBUTE_KEY_CONFIGURATION_ID);
				if (id != null) {
					try {
						bindingManager.setActiveScheme(bindingManager
								.getScheme(id));
						return;
					} catch (final NotDefinedException e) {
						// Let's keep looking....
					}
				}
			}
		}

		// A default preference value that is different than the default.
		if ((defaultActiveSchemeId != null)
				&& (!defaultActiveSchemeId
						.equals(IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID))) {
			try {
				bindingManager.setActiveScheme(bindingManager
						.getScheme(defaultActiveSchemeId));
				return;
			} catch (final NotDefinedException e) {
				// Let's keep looking....
			}
		}

		// The registry.
		for (int i = configurationElementCount - 1; i >= 0; i--) {
			final IConfigurationElement configurationElement = configurationElements[i];

			String id = configurationElement
					.getAttribute(ATTRIBUTE_KEY_CONFIGURATION_ID);
			if (id != null) {
				try {
					bindingManager
							.setActiveScheme(bindingManager.getScheme(id));
					return;
				} catch (final NotDefinedException e) {
					// Let's keep looking....
				}
			}

			id = configurationElement.getAttribute(ATTRIBUTE_VALUE);
			if (id != null) {
				try {
					bindingManager
							.setActiveScheme(bindingManager.getScheme(id));
					return;
				} catch (final NotDefinedException e) {
					// Let's keep looking....
				}
			}
		}

		// The default default active scheme id.
		try {
			bindingManager
					.setActiveScheme(bindingManager
							.getScheme(IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID));
		} catch (final NotDefinedException e) {
			// Damn, we're fucked.
			throw new Error("You cannot make something from nothing"); //$NON-NLS-1$
		}
	}

	/**
	 * Reads all of the binding definitions from the preferences.
	 * 
	 * @param preferences
	 *            The memento for the commands preferences key.
	 * @param bindingManager
	 *            The binding manager to which the bindings should be added;
	 *            must not be <code>null</code>.
	 * @param commandService
	 *            The command service for the workbench; must not be
	 *            <code>null</code>.
	 */
	private static final void readBindingsFromPreferences(
			final IMemento preferences, final BindingManager bindingManager,
			final ICommandService commandService) {
		/*
		 * If necessary, this list of status items will be constructed. It will
		 * only contains instances of <code>IStatus</code>.
		 */
		List warningsToLog = new ArrayList(1);

		if (preferences != null) {
			final IMemento[] preferenceMementos = preferences
					.getChildren(ELEMENT_KEY_BINDING);
			int preferenceMementoCount = preferenceMementos.length;
			for (int i = preferenceMementoCount - 1; i >= 0; i--) {
				final IMemento memento = preferenceMementos[i];

				// Read out the command id.
				String commandId = memento.getString(ATTRIBUTE_COMMAND_ID);
				if ((commandId == null) || (commandId.length() == 0)) {
					commandId = memento.getString(ATTRIBUTE_COMMAND);
				}
				if ((commandId != null) && (commandId.length() == 0)) {
					commandId = null;
				}
				final Command command;
				if (commandId != null) {
					command = commandService.getCommand(commandId);
				} else {
					command = null;
				}

				// Read out the scheme id.
				String schemeId = memento
						.getString(ATTRIBUTE_KEY_CONFIGURATION_ID);
				if ((schemeId == null) || (schemeId.length() == 0)) {
					schemeId = memento.getString(ATTRIBUTE_CONFIGURATION);
					if ((schemeId == null) || (schemeId.length() == 0)) {
						// The scheme id should never be null. This is invalid.
						final String message = "Key bindings need a scheme or key configuration: commandId='" //$NON-NLS-1$
								+ commandId + "'."; //$NON-NLS-1$
						final IStatus status = new Status(IStatus.WARNING,
								WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
						warningsToLog.add(status);
						continue;
					}
				}

				// Read out the context id.
				String contextId = memento.getString(ATTRIBUTE_CONTEXT_ID);
				if (LEGACY_DEFAULT_SCOPE.equals(contextId)) {
					contextId = null;
				} else if ((contextId == null) || (contextId.length() == 0)) {
					contextId = memento.getString(ATTRIBUTE_SCOPE);
					if (LEGACY_DEFAULT_SCOPE.equals(contextId)) {
						contextId = null;
					}
				}
				if ((contextId == null) || (contextId.length() == 0)) {
					contextId = IContextIds.CONTEXT_ID_WINDOW;
				}

				// Read out the key sequence.
				String keySequenceText = memento
						.getString(ATTRIBUTE_KEY_SEQUENCE);
				KeySequence keySequence = null;
				if ((keySequenceText == null)
						|| (keySequenceText.length() == 0)) {
					keySequenceText = memento.getString(ATTRIBUTE_STRING);
					if ((keySequenceText == null)
							|| (keySequenceText.length() == 0)) {
						/*
						 * The key sequence should never be null. This is
						 * pointless
						 */
						final String message = "Key bindings need a key sequence or string: commandId='" //$NON-NLS-1$
								+ commandId + "'."; //$NON-NLS-1$
						final IStatus status = new Status(IStatus.WARNING,
								WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
						warningsToLog.add(status);
						continue;
					}

					// The key sequence is in the old-style format.
					keySequence = convert2_1Sequence(parse2_1Sequence(keySequenceText));

				} else {
					// The key sequence is in the new-style format.
					try {
						keySequence = KeySequence.getInstance(keySequenceText);
					} catch (final ParseException e) {
						final String message = "Could not parse: sequence='" //$NON-NLS-1$
								+ keySequenceText + "': commandId='" //$NON-NLS-1$
								+ commandId + "'."; //$NON-NLS-1$
						final IStatus status = new Status(IStatus.WARNING,
								WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
						warningsToLog.add(status);
						continue;
					}
					if (keySequence.isEmpty() || !keySequence.isComplete()) {
						final String message = "Key bindings cannot use an empty or incomplete key sequence: sequence='" //$NON-NLS-1$
								+ keySequence + "': commandId='" //$NON-NLS-1$
								+ commandId + "'."; //$NON-NLS-1$
						final IStatus status = new Status(IStatus.WARNING,
								WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
						warningsToLog.add(status);
						continue;
					}

				}

				// Read out the locale and platform.
				String locale = memento.getString(ATTRIBUTE_LOCALE);
				if ((locale != null) && (locale.length() == 0)) {
					locale = null;
				}
				String platform = memento.getString(ATTRIBUTE_PLATFORM);
				if ((platform != null) && (platform.length() == 0)) {
					platform = null;
				}

				// Read out the parameters
				final ParameterizedCommand parameterizedCommand;
				if (command == null) {
					parameterizedCommand = null;
				} else {
					parameterizedCommand = readParameters(memento,
							warningsToLog, command);
				}

				final Binding binding = new KeyBinding(keySequence,
						parameterizedCommand, schemeId, contextId, locale,
						platform, null, Binding.USER);
				bindingManager.addBinding(binding);
			}
		}

		// If there were any warnings, then log them now.
		if (!warningsToLog.isEmpty()) {
			final String message = "Warnings while parsing the key bindings from the preference store"; //$NON-NLS-1$
			final IStatus status = new MultiStatus(
					WorkbenchPlugin.PI_WORKBENCH, 0, (IStatus[]) warningsToLog
							.toArray(new IStatus[warningsToLog.size()]),
					message, null);
			WorkbenchPlugin.log(status);
		}
	}

	/**
	 * Reads all of the binding definitions from the commands extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the commands extension point;
	 *            must not be <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param bindingManager
	 *            The binding manager to which the bindings should be added;
	 *            must not be <code>null</code>.
	 * @param commandService
	 *            The command service for the workbench; must not be
	 *            <code>null</code>.
	 */
	private static final void readBindingsFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final BindingManager bindingManager,
			final ICommandService commandService) {
		final Collection bindings = new ArrayList(configurationElementCount);

		/*
		 * If necessary, this list of status items will be constructed. It will
		 * only contains instances of <code>IStatus</code>.
		 */
		List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			/*
			 * Read out the command id. Doing this before determining if the key
			 * binding is actually valid is a bit wasteful. However, it is
			 * helpful to have the command identifier when logging syntax
			 * errors.
			 */
			String commandId = configurationElement
					.getAttribute(ATTRIBUTE_COMMAND_ID);
			if ((commandId == null) || (commandId.length() == 0)) {
				commandId = configurationElement
						.getAttribute(ATTRIBUTE_COMMAND);
			}
			if ((commandId != null) && (commandId.length() == 0)) {
				commandId = null;
			}
			final Command command;
			if (commandId != null) {
				command = commandService.getCommand(commandId);
				if (!command.isDefined()) {
					// Reference to an undefined command. This is invalid.
					final String message = "Cannot bind to an undefined command: plug-in='" //$NON-NLS-1$
							+ configurationElement.getNamespace()
							+ "', commandId='" //$NON-NLS-1$
							+ commandId + "'."; //$NON-NLS-1$
					final IStatus status = new Status(IStatus.WARNING,
							WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
					warningsToLog.add(status);
					continue;
				}
			} else {
				command = null;
			}

			// Read out the scheme id.
			String schemeId = configurationElement
					.getAttribute(ATTRIBUTE_SCHEME_ID);
			if ((schemeId == null) || (schemeId.length() == 0)) {
				schemeId = configurationElement
						.getAttribute(ATTRIBUTE_KEY_CONFIGURATION_ID);
				if ((schemeId == null) || (schemeId.length() == 0)) {
					schemeId = configurationElement
							.getAttribute(ATTRIBUTE_CONFIGURATION);
					if ((schemeId == null) || (schemeId.length() == 0)) {
						// The scheme id should never be null. This is invalid.
						final String message = "Key bindings need a scheme: plug-in='" //$NON-NLS-1$
								+ configurationElement.getNamespace()
								+ "', commandId='" //$NON-NLS-1$
								+ commandId + "'."; //$NON-NLS-1$
						final IStatus status = new Status(IStatus.WARNING,
								WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
						warningsToLog.add(status);
						continue;
					}
				}
			}

			// Read out the context id.
			String contextId = configurationElement
					.getAttribute(ATTRIBUTE_CONTEXT_ID);
			if (LEGACY_DEFAULT_SCOPE.equals(contextId)) {
				contextId = null;
			} else if ((contextId == null) || (contextId.length() == 0)) {
				contextId = configurationElement.getAttribute(ATTRIBUTE_SCOPE);
				if (LEGACY_DEFAULT_SCOPE.equals(contextId)) {
					contextId = null;
				}
			}
			if ((contextId == null) || (contextId.length() == 0)) {
				contextId = IContextIds.CONTEXT_ID_WINDOW;
			}

			// Read out the key sequence.
			KeySequence keySequence = null;
			String keySequenceText = configurationElement
					.getAttribute(ATTRIBUTE_SEQUENCE);
			if ((keySequenceText == null) || (keySequenceText.length() == 0)) {
				keySequenceText = configurationElement
						.getAttribute(ATTRIBUTE_KEY_SEQUENCE);
			}
			if ((keySequenceText == null) || (keySequenceText.length() == 0)) {
				keySequenceText = configurationElement
						.getAttribute(ATTRIBUTE_STRING);
				if ((keySequenceText == null)
						|| (keySequenceText.length() == 0)) {
					// The key sequence should never be null. This is
					// pointless
					final String message = "Defining a key binding with no key sequence has no effect: plug-in='" //$NON-NLS-1$
							+ configurationElement.getNamespace()
							+ "', commandId='" //$NON-NLS-1$
							+ commandId + "'."; //$NON-NLS-1$
					final IStatus status = new Status(IStatus.WARNING,
							WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
					warningsToLog.add(status);
					continue;
				}

				// The key sequence is in the old-style format.
				try {
					keySequence = convert2_1Sequence(parse2_1Sequence(keySequenceText));
				} catch (final IllegalArgumentException e) {
					final String message = "Could not parse key sequence '" + keySequenceText //$NON-NLS-1$
							+ "': plug-in='" //$NON-NLS-1$
							+ configurationElement.getNamespace()
							+ "', commandId='" //$NON-NLS-1$
							+ commandId + "'."; //$NON-NLS-1$
					final IStatus status = new Status(IStatus.WARNING,
							WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
					warningsToLog.add(status);
					continue;
				}

			} else {
				// The key sequence is in the new-style format.
				try {
					keySequence = KeySequence.getInstance(keySequenceText);
				} catch (final ParseException e) {
					final String message = "Could not parse key sequence '" + keySequenceText //$NON-NLS-1$
							+ "': plug-in='" //$NON-NLS-1$
							+ configurationElement.getNamespace()
							+ "', commandId='" //$NON-NLS-1$
							+ commandId + "'."; //$NON-NLS-1$
					final IStatus status = new Status(IStatus.WARNING,
							WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
					warningsToLog.add(status);
					continue;
				}
				if (keySequence.isEmpty() || !keySequence.isComplete()) {
					final String message = "Key bindings should not have an empty or incomplete key sequence: sequence='" //$NON-NLS-1$
							+ keySequence
							+ "': plug-in='" //$NON-NLS-1$
							+ configurationElement.getNamespace()
							+ "', commandId='" //$NON-NLS-1$
							+ commandId + "'."; //$NON-NLS-1$
					final IStatus status = new Status(IStatus.WARNING,
							WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
					warningsToLog.add(status);
					continue;
				}

			}

			// Read out the locale and platform.
			String locale = configurationElement.getAttribute(ATTRIBUTE_LOCALE);
			if ((locale != null) && (locale.length() == 0)) {
				locale = null;
			}
			String platform = configurationElement
					.getAttribute(ATTRIBUTE_PLATFORM);
			if ((platform != null) && (platform.length() == 0)) {
				platform = null;
			}

			// Read out the parameters, if any.
			final ParameterizedCommand parameterizedCommand;
			if (command == null) {
				parameterizedCommand = null;
			} else {
				parameterizedCommand = readParameters(configurationElement,
						warningsToLog, command);
			}

			final Binding binding = new KeyBinding(keySequence,
					parameterizedCommand, schemeId, contextId, locale,
					platform, null, Binding.SYSTEM);
			bindings.add(binding);
		}

		final Binding[] bindingArray = (Binding[]) bindings
				.toArray(new Binding[bindings.size()]);
		bindingManager.setBindings(bindingArray);

		// If there were any warnings, then log them now.
		if (!warningsToLog.isEmpty()) {
			final String message = "Warnings while parsing the key bindings from the 'org.eclipse.ui.commands' extension point"; //$NON-NLS-1$
			final IStatus status = new MultiStatus(
					WorkbenchPlugin.PI_WORKBENCH, 0, (IStatus[]) warningsToLog
							.toArray(new IStatus[warningsToLog.size()]),
					message, null);
			WorkbenchPlugin.log(status);
		}
	}

	/**
	 * Reads the parameters from a parent configuration element. This is used to
	 * read the parameter sub-elements from a key element. Each parameter is
	 * guaranteed to be valid. If invalid parameters are found, then a warning
	 * status will be appended to the <code>warningsToLog</code> list.
	 * 
	 * @param configurationElement
	 *            The configuration element from which the parameters should be
	 *            read; must not be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings found during parsing. Warnings found will
	 *            parsing the parameters will be appended to this list. This
	 *            value must not be <code>null</code>.
	 * @param command
	 *            The command around which the parameterization should be
	 *            created; must not be <code>null</code>.
	 * @return The array of parameters found for this configuration element;
	 *         <code>null</code> if none can be found.
	 */
	private static final ParameterizedCommand readParameters(
			final IConfigurationElement configurationElement,
			final List warningsToLog, final Command command) {
		final IConfigurationElement[] parameterElements = configurationElement
				.getChildren(ELEMENT_PARAMETER);
		if ((parameterElements == null) || (parameterElements.length == 0)) {
			return new ParameterizedCommand(command, null);
		}

		final Collection parameters = new ArrayList();
		for (int i = 0; i < parameterElements.length; i++) {
			final IConfigurationElement parameterElement = parameterElements[i];

			// Read out the id.
			final String id = parameterElement.getAttribute(ATTRIBUTE_ID);
			if ((id == null) || (id.length() == 0)) {
				// The name should never be null. This is invalid.
				final String message = "Parameters need a name: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Find the parameter on the command.
			IParameter parameter = null;
			try {
				final IParameter[] commandParameters = command.getParameters();
				if (parameters != null) {
					for (int j = 0; j < commandParameters.length; j++) {
						final IParameter currentParameter = commandParameters[j];
						if (Util.equals(currentParameter.getId(), id)) {
							parameter = currentParameter;
							break;
						}
					}

				}
			} catch (final NotDefinedException e) {
				// This should not happen.
			}
			if (parameter == null) {
				// The name should never be null. This is invalid.
				final String message = "Could not find a matching parameter: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace()
						+ "', parameterId='" + id //$NON-NLS-1$
						+ "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Read out the value.
			final String value = parameterElement.getAttribute(ATTRIBUTE_VALUE);
			if ((value == null) || (value.length() == 0)) {
				// The name should never be null. This is invalid.
				final String message = "Parameters need a value: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace()
						+ "', parameterId='" //$NON-NLS-1$
						+ id + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			parameters.add(new Parameterization(parameter, value));
		}

		if (parameters.isEmpty()) {
			return new ParameterizedCommand(command, null);
		}

		return new ParameterizedCommand(command,
				(Parameterization[]) parameters
						.toArray(new Parameterization[parameters.size()]));
	}

	/**
	 * Reads the parameters from a parent memento. This is used to read the
	 * parameter sub-elements from a key element. Each parameter is guaranteed
	 * to be valid. If invalid parameters are found, then a warning status will
	 * be appended to the <code>warningsToLog</code> list.
	 * 
	 * @param memento
	 *            The memento from which the parameters should be read; must not
	 *            be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings found during parsing. Warnings found will
	 *            parsing the parameters will be appended to this list. This
	 *            value must not be <code>null</code>.
	 * @param command
	 *            The command around which the parameterization should be
	 *            created; must not be <code>null</code>.
	 * @return The array of parameters found for this memento; <code>null</code>
	 *         if none can be found.
	 */
	private static final ParameterizedCommand readParameters(
			final IMemento memento, final List warningsToLog,
			final Command command) {
		final IMemento[] parameterElements = memento
				.getChildren(ELEMENT_PARAMETER);
		if ((parameterElements == null) || (parameterElements.length == 0)) {
			return new ParameterizedCommand(command, null);
		}

		final Collection parameters = new ArrayList();
		for (int i = 0; i < parameterElements.length; i++) {
			final IMemento parameterElement = parameterElements[i];

			// Read out the id.
			final String id = parameterElement.getString(ATTRIBUTE_ID);
			if ((id == null) || (id.length() == 0)) {
				// The name should never be null. This is invalid.
				final String message = "Parameters need a name: preferences."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Find the parameter on the command.
			IParameter parameter = null;
			try {
				final IParameter[] commandParameters = command.getParameters();
				if (parameters != null) {
					for (int j = 0; j < commandParameters.length; j++) {
						final IParameter currentParameter = commandParameters[j];
						if (Util.equals(currentParameter.getId(), id)) {
							parameter = currentParameter;
							break;
						}
					}

				}
			} catch (final NotDefinedException e) {
				// This should not happen.
			}
			if (parameter == null) {
				// The name should never be null. This is invalid.
				final String message = "Could not find a matching parameter: preferences, parameterId='" //$NON-NLS-1$
						+ id + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Read out the name.
			final String value = parameterElement.getString(ATTRIBUTE_VALUE);
			if ((value == null) || (value.length() == 0)) {
				// The name should never be null. This is invalid.
				final String message = "Parameters need a value: preferences, parameterId='" //$NON-NLS-1$
						+ id + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			parameters.add(new Parameterization(parameter, value));
		}

		if (parameters.isEmpty()) {
			return new ParameterizedCommand(command, null);
		}

		return new ParameterizedCommand(command,
				(Parameterization[]) parameters
						.toArray(new Parameterization[parameters.size()]));
	}

	/**
	 * Reads all of the scheme definitions from the registry.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the commands extension point;
	 *            must not be <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param bindingManager
	 *            The binding manager to which the schemes should be added; must
	 *            not be <code>null</code>.
	 */
	private static final void readSchemes(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final BindingManager bindingManager) {
		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the attributes.
			final String id = configurationElement.getAttribute(ATTRIBUTE_ID);
			if ((id == null) || (id.length() == 0)) {
				// The id cannot be null or empty.
				continue;
			}
			String name = configurationElement.getAttribute(ATTRIBUTE_NAME);
			if ((name != null) && (name.length() == 0)) {
				name = null;
			}
			String description = configurationElement
					.getAttribute(ATTRIBUTE_DESCRIPTION);
			if ((description != null) && (description.length() == 0)) {
				description = null;
			}
			String parentId = configurationElement
					.getAttribute(ATTRIBUTE_PARENT_ID);
			if ((parentId != null) && (parentId.length() == 0)) {
				parentId = configurationElement.getAttribute(ATTRIBUTE_PARENT);
				if ((parentId != null) && (parentId.length() == 0)) {
					parentId = null;
				}
			}

			// Define the scheme.
			final Scheme scheme = bindingManager.getScheme(id);
			scheme.define(name, description, parentId);
		}
	}

	/**
	 * Writes the given active scheme and bindings to the preference store. Only
	 * bindings that are of the <code>Binding.USER</code> type will be
	 * written; the others will be ignored.
	 * 
	 * @param activeScheme
	 *            The scheme which should be persisted; may be <code>null</code>.
	 * @param bindings
	 *            The bindings which should be persisted; may be
	 *            <code>null</code>
	 * @throws IOException
	 *             If something happens while trying to write to the workbench
	 *             preference store.
	 */
	static final void write(final Scheme activeScheme,
			final Binding[] bindings) throws IOException {
		// Print out debugging information, if requested.
		if (DEBUG) {
			System.out.println("BINDINGS >> Persisting active scheme '" //$NON-NLS-1$
					+ activeScheme.getId() + "'"); //$NON-NLS-1$
			System.out.println("BINDINGS >> Persisting bindings"); //$NON-NLS-1$
		}

		// Write the simple preference key to the UI preference store.
		writeActiveScheme(activeScheme);

		// Build the XML block for writing the bindings and active scheme.
		final XMLMemento xmlMemento = XMLMemento
				.createWriteRoot(EXTENSION_COMMANDS);
		if (activeScheme != null) {
			writeActiveScheme(xmlMemento, activeScheme);
		}
		if (bindings != null) {
			final int bindingsLength = bindings.length;
			for (int i = 0; i < bindingsLength; i++) {
				final Binding binding = bindings[i];
				if (binding.getType() == Binding.USER) {
					writeBinding(xmlMemento, binding);
				}
			}
		}

		// Write the XML block to the workbench preference store.
		final IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		final Writer writer = new StringWriter();
		try {
			xmlMemento.save(writer);
			preferenceStore.setValue(EXTENSION_COMMANDS, writer.toString());
		} finally {
			writer.close();
		}
	}

	/**
	 * Writes the active scheme to the memento. If the scheme is
	 * <code>null</code>, then all schemes in the memento are removed.
	 * 
	 * @param memento
	 *            The memento to which the scheme should be written; must not be
	 *            <code>null</code>.
	 * @param scheme
	 *            The scheme that should be written; must not be
	 *            <code>null</code>.
	 */
	private static final void writeActiveScheme(final IMemento memento,
			final Scheme scheme) {
		// Add this active scheme, if it is not the default.
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final String schemeId = scheme.getId();
		final String defaultSchemeId = store
				.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		if ((defaultSchemeId == null) ? (schemeId != null) : (!defaultSchemeId
				.equals(schemeId))) {
			final IMemento child = memento.createChild(ELEMENT_ACTIVE_SCHEME);
			child.putString(ATTRIBUTE_KEY_CONFIGURATION_ID, schemeId);
		}
	}

	/**
	 * Writes the active scheme to its own preference key. This key is used by
	 * RCP applications as part of their plug-in customization.
	 * 
	 * @param scheme
	 *            The scheme to write to the preference store. If the scheme is
	 *            <code>null</code>, then it is removed.
	 */
	private static final void writeActiveScheme(final Scheme scheme) {
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final String schemeId = (scheme == null) ? null : scheme.getId();
		final String defaultSchemeId = store
				.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		if ((defaultSchemeId == null) ? (scheme != null) : (!defaultSchemeId
				.equals(schemeId))) {
			store.setValue(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID,
					scheme.getId());
		} else {
			store
					.setToDefault(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		}
	}

	/**
	 * Writes the binding to the memento. This creates a new child element on
	 * the memento, and places the properties of the binding as its attributes.
	 * 
	 * @param parent
	 *            The parent memento for the binding element; must not be
	 *            <code>null</code>.
	 * @param binding
	 *            The binding to write; must not be <code>null</code>.
	 */
	private static final void writeBinding(final IMemento parent,
			final Binding binding) {
		final IMemento element = parent.createChild(ELEMENT_BINDING);
		element.putString(ATTRIBUTE_CONTEXT_ID, binding.getContextId());
		final ParameterizedCommand parameterizedCommand = binding
				.getParameterizedCommand();
		final String commandId = (parameterizedCommand == null) ? null
				: parameterizedCommand.getId();
		element.putString(ATTRIBUTE_COMMAND_ID, commandId);
		element
				.putString(ATTRIBUTE_KEY_CONFIGURATION_ID, binding
						.getSchemeId());
		element.putString(ATTRIBUTE_KEY_SEQUENCE, binding.getTriggerSequence()
				.toString());
		element.putString(ATTRIBUTE_LOCALE, binding.getLocale());
		element.putString(ATTRIBUTE_PLATFORM, binding.getPlatform());
		if (parameterizedCommand != null) {
			final Map parameterizations = parameterizedCommand
					.getParameterMap();
			final Iterator parameterizationItr = parameterizations.entrySet()
					.iterator();
			while (parameterizationItr.hasNext()) {
				final Map.Entry entry = (Map.Entry) parameterizationItr.next();
				final String id = (String) entry.getKey();
				final String value = (String) entry.getValue();
				final IMemento parameterElement = element
						.createChild(ELEMENT_PARAMETER);
				parameterElement.putString(ATTRIBUTE_ID, id);
				parameterElement.putString(ATTRIBUTE_VALUE, value);
			}
		}
	}

	/**
	 * This class should not be constructed.
	 */
	private BindingPersistence() {
		// Should not be called.
	}
}
