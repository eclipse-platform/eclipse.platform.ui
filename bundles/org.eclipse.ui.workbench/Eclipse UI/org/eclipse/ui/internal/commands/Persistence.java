/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.internal.commands.ws.HandlerProxy;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.ParseException;
import org.eclipse.ui.keys.SWTKeySupport;

final class Persistence {

    private static Map stringToValueMap = new TreeMap();

    static {
        stringToValueMap.put("BACKSPACE", new Integer(8)); //$NON-NLS-1$
        stringToValueMap.put("TAB", new Integer(9)); //$NON-NLS-1$
        stringToValueMap.put("RETURN", new Integer(13)); //$NON-NLS-1$
        stringToValueMap.put("ENTER", new Integer(13)); //$NON-NLS-1$
        stringToValueMap.put("ESCAPE", new Integer(27)); //$NON-NLS-1$
        stringToValueMap.put("ESC", new Integer(27)); //$NON-NLS-1$
        stringToValueMap.put("DELETE", new Integer(127)); //$NON-NLS-1$
        stringToValueMap.put("SPACE", new Integer(' ')); //$NON-NLS-1$
        stringToValueMap.put("ARROW_UP", new Integer(SWT.ARROW_UP)); //$NON-NLS-1$
        stringToValueMap.put("ARROW_DOWN", new Integer(SWT.ARROW_DOWN)); //$NON-NLS-1$
        stringToValueMap.put("ARROW_LEFT", new Integer(SWT.ARROW_LEFT)); //$NON-NLS-1$
        stringToValueMap.put("ARROW_RIGHT", new Integer(SWT.ARROW_RIGHT)); //$NON-NLS-1$
        stringToValueMap.put("PAGE_UP", new Integer(SWT.PAGE_UP)); //$NON-NLS-1$
        stringToValueMap.put("PAGE_DOWN", new Integer(SWT.PAGE_DOWN)); //$NON-NLS-1$
        stringToValueMap.put("HOME", new Integer(SWT.HOME)); //$NON-NLS-1$
        stringToValueMap.put("END", new Integer(SWT.END)); //$NON-NLS-1$
        stringToValueMap.put("INSERT", new Integer(SWT.INSERT)); //$NON-NLS-1$
        stringToValueMap.put("F1", new Integer(SWT.F1)); //$NON-NLS-1$
        stringToValueMap.put("F2", new Integer(SWT.F2)); //$NON-NLS-1$
        stringToValueMap.put("F3", new Integer(SWT.F3)); //$NON-NLS-1$
        stringToValueMap.put("F4", new Integer(SWT.F4)); //$NON-NLS-1$
        stringToValueMap.put("F5", new Integer(SWT.F5)); //$NON-NLS-1$
        stringToValueMap.put("F6", new Integer(SWT.F6)); //$NON-NLS-1$
        stringToValueMap.put("F7", new Integer(SWT.F7)); //$NON-NLS-1$
        stringToValueMap.put("F8", new Integer(SWT.F8)); //$NON-NLS-1$
        stringToValueMap.put("F9", new Integer(SWT.F9)); //$NON-NLS-1$
        stringToValueMap.put("F10", new Integer(SWT.F10)); //$NON-NLS-1$
        stringToValueMap.put("F11", new Integer(SWT.F11)); //$NON-NLS-1$
        stringToValueMap.put("F12", new Integer(SWT.F12)); //$NON-NLS-1$		
    }

    private final static String ALT = "Alt"; //$NON-NLS-1$

    private final static String COMMAND = "Command"; //$NON-NLS-1$

    private final static String CTRL = "Ctrl"; //$NON-NLS-1$

    private final static String MODIFIER_SEPARATOR = "+"; //$NON-NLS-1$

    final static String PACKAGE_BASE = "commands"; //$NON-NLS-1$

    final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$	

    final static String PACKAGE_FULL = PACKAGE_PREFIX + '.' + PACKAGE_BASE;

    private final static String SHIFT = "Shift"; //$NON-NLS-1$

    final static String TAG_ACTIVE_KEY_CONFIGURATION = "activeKeyConfiguration"; //$NON-NLS-1$

    final static String TAG_CATEGORY = "category"; //$NON-NLS-1$	

    final static String TAG_CATEGORY_ID = "categoryId"; //$NON-NLS-1$

    final static String TAG_COMMAND = "command"; //$NON-NLS-1$	

    final static String TAG_COMMAND_ID = "commandId"; //$NON-NLS-1$	

    final static String TAG_CONTEXT_ID = "contextId"; //$NON-NLS-1$	

    final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

    final static String TAG_HANDLER = "handlerSubmission"; //$NON-NLS-1$

    final static String TAG_ID = "id"; //$NON-NLS-1$

    final static String TAG_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$	

    final static String TAG_KEY_CONFIGURATION_ID = "keyConfigurationId"; //$NON-NLS-1$	

    final static String TAG_KEY_SEQUENCE = "keySequence"; //$NON-NLS-1$	

    // TODO keyBinding -> keySequenceBinding
    final static String TAG_KEY_SEQUENCE_BINDING = "keyBinding"; //$NON-NLS-1$

    final static String TAG_LOCALE = "locale"; //$NON-NLS-1$

    final static String TAG_NAME = "name"; //$NON-NLS-1$	

    final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$

    final static String TAG_PLATFORM = "platform"; //$NON-NLS-1$	

    final static String TAG_SOURCE_ID = "sourceId"; //$NON-NLS-1$

    private static KeySequence deprecatedSequenceToKeySequence(int[] sequence) {
        List keyStrokes = new ArrayList();

        for (int i = 0; i < sequence.length; i++)
            keyStrokes.add(deprecatedStrokeToKeyStroke(sequence[i]));

        return KeySequence.getInstance(keyStrokes);
    }

    private static KeyStroke deprecatedStrokeToKeyStroke(int stroke) {
        return SWTKeySupport.convertAcceleratorToKeyStroke(stroke);
    }

    private static int[] parseDeprecatedSequence(String string) {
        if (string == null)
            throw new NullPointerException();

        StringTokenizer stringTokenizer = new StringTokenizer(string);
        int length = stringTokenizer.countTokens();
        int[] strokes = new int[length];

        for (int i = 0; i < length; i++)
            strokes[i] = parseDeprecatedStroke(stringTokenizer.nextToken());

        return strokes;
    }

    private static int parseDeprecatedStroke(String string) {
        if (string == null)
            throw new NullPointerException();

        List list = new ArrayList();
        StringTokenizer stringTokenizer = new StringTokenizer(string,
                MODIFIER_SEPARATOR, true);

        while (stringTokenizer.hasMoreTokens())
            list.add(stringTokenizer.nextToken());

        int size = list.size();
        int value = 0;

        if (size % 2 == 1) {
            String token = (String) list.get(size - 1);
            Integer integer = (Integer) stringToValueMap.get(token
                    .toUpperCase());

            if (integer != null)
                value = integer.intValue();
            else if (token.length() == 1)
                value = token.toUpperCase().charAt(0);

            if (value != 0) {
                for (int i = 0; i < size - 1; i++) {
                    token = (String) list.get(i);

                    if (i % 2 == 0) {
                        if (token.equalsIgnoreCase(CTRL)) {
                            if ((value & SWT.CTRL) != 0)
                                return 0;

                            value |= SWT.CTRL;
                        } else if (token.equalsIgnoreCase(ALT)) {
                            if ((value & SWT.ALT) != 0)
                                return 0;

                            value |= SWT.ALT;
                        } else if (token.equalsIgnoreCase(SHIFT)) {
                            if ((value & SWT.SHIFT) != 0)
                                return 0;

                            value |= SWT.SHIFT;
                        } else if (token.equalsIgnoreCase(COMMAND)) {
                            if ((value & SWT.COMMAND) != 0)
                                return 0;

                            value |= SWT.COMMAND;
                        } else
                            return 0;
                    } else if (!MODIFIER_SEPARATOR.equals(token))
                        return 0;
                }
            }
        }

        return value;
    }
    
    /**
     * Retrieves the active key configuration from the preference store.
     * 
     * @return The active key configuration identifier stored in the preference
     *         store; never <code>null</code>.
     * @since 3.1
     */
    static final ActiveKeyConfigurationDefinition readActiveKeyConfigurationDefinition() {
        final IPreferenceStore store = PlatformUI.getPreferenceStore();
        if (!store.contains(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID)) {
            return null;
        }

        final String keyConfigurationId = store
                .getString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
        return new ActiveKeyConfigurationDefinition(keyConfigurationId, null);
    }

    static ActiveKeyConfigurationDefinition readActiveKeyConfigurationDefinition(
            IMemento memento, String sourceIdOverride) {
        if (memento == null)
            throw new NullPointerException();

        String keyConfigurationId = memento.getString(TAG_KEY_CONFIGURATION_ID);

        // TODO deprecated start
        if (keyConfigurationId == null)
            keyConfigurationId = memento.getString("value"); //$NON-NLS-1$ 
        // TODO deprecated end

        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);

        // TODO deprecated start
        if (sourceIdOverride == null && sourceId == null)
            sourceId = memento.getString("plugin"); //$NON-NLS-1$ 
        // TODO deprecated end

        return new ActiveKeyConfigurationDefinition(keyConfigurationId,
                sourceId);
    }

    static List readActiveKeyConfigurationDefinitions(IMemento memento,
            String name, String sourceIdOverride) {
        if (memento == null || name == null)
            throw new NullPointerException();

        IMemento[] mementos = memento.getChildren(name);

        if (mementos == null)
            throw new NullPointerException();

        List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++)
            list.add(readActiveKeyConfigurationDefinition(mementos[i],
                    sourceIdOverride));
        
        // Add the standalone preference key.
        final ActiveKeyConfigurationDefinition activeKeyConfigurationDefinition = readActiveKeyConfigurationDefinition();
        if (activeKeyConfigurationDefinition != null) {
            list.add(activeKeyConfigurationDefinition);
        }

        return list;
    }

    static CategoryDefinition readCategoryDefinition(IMemento memento,
            String sourceIdOverride) {
        if (memento == null)
            throw new NullPointerException();

        String description = memento.getString(TAG_DESCRIPTION);
        String id = memento.getString(TAG_ID);
        String name = memento.getString(TAG_NAME);
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);

        // TODO deprecated start
        if (sourceIdOverride == null && sourceId == null)
            sourceId = memento.getString("plugin"); //$NON-NLS-1$ 
        // TODO deprecated end

        return new CategoryDefinition(description, id, name, sourceId);
    }

    static List readCategoryDefinitions(IMemento memento, String name,
            String sourceIdOverride) {
        if (memento == null || name == null)
            throw new NullPointerException();

        IMemento[] mementos = memento.getChildren(name);

        if (mementos == null)
            throw new NullPointerException();

        List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++)
            list.add(readCategoryDefinition(mementos[i], sourceIdOverride));

        return list;
    }

    static CommandDefinition readCommandDefinition(IMemento memento,
            String sourceIdOverride) {
        if (memento == null)
            throw new NullPointerException();

        String categoryId = memento.getString(TAG_CATEGORY_ID);

        // TODO deprecated start
        if (categoryId == null)
            categoryId = memento.getString("category"); //$NON-NLS-1$ 
        // TODO deprecated end

        String description = memento.getString(TAG_DESCRIPTION);
        String id = memento.getString(TAG_ID);
        String name = memento.getString(TAG_NAME);
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);

        // TODO deprecated start
        if (sourceIdOverride == null && sourceId == null)
            sourceId = memento.getString("plugin"); //$NON-NLS-1$ 
        // TODO deprecated end

        return new CommandDefinition(categoryId, description, id, name,
                sourceId);
    }

    static List readCommandDefinitions(IMemento memento, String name,
            String sourceIdOverride) {
        if (memento == null || name == null)
            throw new NullPointerException();

        IMemento[] mementos = memento.getChildren(name);

        if (mementos == null)
            throw new NullPointerException();

        List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++)
            list.add(readCommandDefinition(mementos[i], sourceIdOverride));

        return list;
    }

    private static int[] readDeprecatedSequence(IMemento memento) {
        if (memento == null)
            throw new NullPointerException();

        IMemento[] mementos = memento.getChildren("stroke"); //$NON-NLS-1$ 

        if (mementos == null)
            throw new NullPointerException();

        int[] strokes = new int[mementos.length];

        for (int i = 0; i < mementos.length; i++)
            strokes[i] = readDeprecatedStroke(mementos[i]);

        return strokes;
    }

    private static int readDeprecatedStroke(IMemento memento) {
        if (memento == null)
            throw new NullPointerException();

        Integer value = memento.getInteger("value"); //$NON-NLS-1$
        return value != null ? value.intValue() : 0;
    }

    /**
     * Reads the handler from XML, and creates a proxy to contain it. The proxy
     * will only instantiate the handler when the handler is first asked for
     * information.
     * 
     * @param configurationElement
     *            The configuration element to read; must not be
     *            <code>null</code>.
     * @return The handler proxy for the given definition; never
     *         <code>null</code>.
     */
    static IHandler readHandlerSubmissionDefinition(
            IConfigurationElement configurationElement) {
        final String commandId = configurationElement
                .getAttribute(TAG_COMMAND_ID);

        return new HandlerProxy(commandId, configurationElement);
    }

    static KeyConfigurationDefinition readKeyConfigurationDefinition(
            IMemento memento, String sourceIdOverride) {
        if (memento == null)
            throw new NullPointerException();

        String description = memento.getString(TAG_DESCRIPTION);
        String id = memento.getString(TAG_ID);
        String name = memento.getString(TAG_NAME);
        String parentId = memento.getString(TAG_PARENT_ID);

        // TODO deprecated start
        if (parentId == null)
            parentId = memento.getString("parent"); //$NON-NLS-1$ 
        // TODO deprecated end

        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);

        // TODO deprecated start
        if (sourceIdOverride == null && sourceId == null)
            sourceId = memento.getString("plugin"); //$NON-NLS-1$ 
        // TODO deprecated end

        return new KeyConfigurationDefinition(description, id, name, parentId,
                sourceId);
    }

    static List readKeyConfigurationDefinitions(IMemento memento, String name,
            String sourceIdOverride) {
        if (memento == null || name == null)
            throw new NullPointerException();

        IMemento[] mementos = memento.getChildren(name);

        if (mementos == null)
            throw new NullPointerException();

        List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++)
            list.add(readKeyConfigurationDefinition(mementos[i],
                    sourceIdOverride));

        return list;
    }

    static KeySequenceBindingDefinition readKeySequenceBindingDefinition(
            IMemento memento, String sourceIdOverride) {
        if (memento == null)
            throw new NullPointerException();

        String contextId = memento.getString(TAG_CONTEXT_ID);

        // TODO deprecated start
        if (contextId == null)
            contextId = memento.getString("scope"); //$NON-NLS-1$

        if ("org.eclipse.ui.globalScope".equals(contextId)) //$NON-NLS-1$
            contextId = null;
        // TODO deprecated end

        String commandId = memento.getString(TAG_COMMAND_ID);

        // TODO deprecated start
        if (commandId == null)
            commandId = memento.getString("command"); //$NON-NLS-1$ 

        if (commandId == null)
            commandId = memento.getString("id"); //$NON-NLS-1$ 
        // TODO deprecated end

        String keyConfigurationId = memento.getString(TAG_KEY_CONFIGURATION_ID);

        // TODO deprecated start
        if (keyConfigurationId == null)
            keyConfigurationId = memento.getString("configuration"); //$NON-NLS-1$
        // TODO deprecated end

        KeySequence keySequence = null;
        String keySequenceAsString = memento.getString(TAG_KEY_SEQUENCE);

        if (keySequenceAsString != null)
            try {
                keySequence = KeySequence.getInstance(keySequenceAsString);
            } catch (ParseException eParse) {
            }
        // TODO deprecated start
        else {
            IMemento mementoSequence = memento.getChild("sequence"); //$NON-NLS-1$

            if (mementoSequence != null)
                keySequence = deprecatedSequenceToKeySequence(readDeprecatedSequence(mementoSequence));
            else {
                String string = memento.getString("string"); //$NON-NLS-1$

                if (string != null)
                    keySequence = deprecatedSequenceToKeySequence(parseDeprecatedSequence(string));
            }
            // TODO deprecated end
        }

        String locale = memento.getString(TAG_LOCALE);
        String platform = memento.getString(TAG_PLATFORM);
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);

        // TODO deprecated start
        if (sourceIdOverride == null && sourceId == null)
            sourceId = memento.getString("plugin"); //$NON-NLS-1$ 
        // TODO deprecated end

        // We treat null context identifiers as the window context.
        if (contextId == null) {
            contextId = KeySequenceBinding.DEFAULT_CONTEXT_ID;
        }

        return new KeySequenceBindingDefinition(contextId, commandId,
                keyConfigurationId, keySequence, locale, platform, sourceId);
    }

    static List readKeySequenceBindingDefinitions(IMemento memento,
            String name, String sourceIdOverride) {
        if (memento == null || name == null)
            throw new NullPointerException();

        IMemento[] mementos = memento.getChildren(name);

        if (mementos == null)
            throw new NullPointerException();

        List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++)
            list.add(readKeySequenceBindingDefinition(mementos[i],
                    sourceIdOverride));

        return list;
    }
    
    /**
     * This writes the active key configuration to its own preference key. This
     * key is used by RCP applications as part of their plug-in customization.
     * 
     * @param activeKeyConfigurationDefinition
     *            The key configuration identifier to write to the preference
     *            store.
     * @since 3.1
     */
    static final void writeActiveKeyConfigurationDefinition(
            final ActiveKeyConfigurationDefinition activeKeyConfigurationDefinition) {
        final IPreferenceStore store = PlatformUI.getPreferenceStore();
        store.setValue(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID,
                activeKeyConfigurationDefinition.getKeyConfigurationId());
    }

    static void writeActiveKeyConfigurationDefinition(IMemento memento,
            ActiveKeyConfigurationDefinition activeKeyConfigurationDefinition) {
        if (memento == null || activeKeyConfigurationDefinition == null)
            throw new NullPointerException();

        memento.putString(TAG_KEY_CONFIGURATION_ID,
                activeKeyConfigurationDefinition.getKeyConfigurationId());
        memento.putString(TAG_SOURCE_ID, activeKeyConfigurationDefinition
                .getSourceId());
    }

    static void writeActiveKeyConfigurationDefinitions(IMemento memento,
            String name, List activeKeyConfigurationDefinitions) {
        if (memento == null || name == null
                || activeKeyConfigurationDefinitions == null)
            throw new NullPointerException();

        activeKeyConfigurationDefinitions = new ArrayList(
                activeKeyConfigurationDefinitions);
        Iterator iterator = activeKeyConfigurationDefinitions.iterator();

        while (iterator.hasNext())
            Util.assertInstance(iterator.next(),
                    ActiveKeyConfigurationDefinition.class);

        final IPreferenceStore store = PlatformUI.getPreferenceStore();
        final String defaultConfig = store
                .getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);

        iterator = activeKeyConfigurationDefinitions.iterator();
        while (iterator.hasNext()) {
            final ActiveKeyConfigurationDefinition activeKeyConfigurationDefinition = (ActiveKeyConfigurationDefinition) iterator
                    .next();
            final String currentConfig = activeKeyConfigurationDefinition
                    .getKeyConfigurationId();
            if ((defaultConfig == null) ? (currentConfig != null)
                    : (!defaultConfig.equals(currentConfig))) {
                writeActiveKeyConfigurationDefinition(
                        memento.createChild(name),
                        activeKeyConfigurationDefinition);
                writeActiveKeyConfigurationDefinition(activeKeyConfigurationDefinition);
            } else {
                store
                        .setToDefault(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
            }
        }
    }

    static void writeCategoryDefinition(IMemento memento,
            CategoryDefinition categoryDefinition) {
        if (memento == null || categoryDefinition == null)
            throw new NullPointerException();

        memento.putString(TAG_DESCRIPTION, categoryDefinition.getDescription());
        memento.putString(TAG_ID, categoryDefinition.getId());
        memento.putString(TAG_NAME, categoryDefinition.getName());
        memento.putString(TAG_SOURCE_ID, categoryDefinition.getSourceId());
    }

    static void writeCategoryDefinitions(IMemento memento, String name,
            List categoryDefinitions) {
        if (memento == null || name == null || categoryDefinitions == null)
            throw new NullPointerException();

        categoryDefinitions = new ArrayList(categoryDefinitions);
        Iterator iterator = categoryDefinitions.iterator();

        while (iterator.hasNext())
            Util.assertInstance(iterator.next(), CategoryDefinition.class);

        iterator = categoryDefinitions.iterator();

        while (iterator.hasNext())
            writeCategoryDefinition(memento.createChild(name),
                    (CategoryDefinition) iterator.next());
    }

    static void writeCommandDefinition(IMemento memento,
            CommandDefinition commandDefinition) {
        if (memento == null || commandDefinition == null)
            throw new NullPointerException();

        memento.putString(TAG_CATEGORY_ID, commandDefinition.getCategoryId());
        memento.putString(TAG_DESCRIPTION, commandDefinition.getDescription());
        memento.putString(TAG_ID, commandDefinition.getId());
        memento.putString(TAG_NAME, commandDefinition.getName());
        memento.putString(TAG_SOURCE_ID, commandDefinition.getSourceId());
    }

    static void writeCommandDefinitions(IMemento memento, String name,
            List commandDefinitions) {
        if (memento == null || name == null || commandDefinitions == null)
            throw new NullPointerException();

        commandDefinitions = new ArrayList(commandDefinitions);
        Iterator iterator = commandDefinitions.iterator();

        while (iterator.hasNext())
            Util.assertInstance(iterator.next(), CommandDefinition.class);

        iterator = commandDefinitions.iterator();

        while (iterator.hasNext())
            writeCommandDefinition(memento.createChild(name),
                    (CommandDefinition) iterator.next());
    }

    static void writeKeyConfigurationDefinition(IMemento memento,
            KeyConfigurationDefinition keyConfigurationDefinition) {
        if (memento == null || keyConfigurationDefinition == null)
            throw new NullPointerException();

        memento.putString(TAG_DESCRIPTION, keyConfigurationDefinition
                .getDescription());
        memento.putString(TAG_ID, keyConfigurationDefinition.getId());
        memento.putString(TAG_NAME, keyConfigurationDefinition.getName());
        memento.putString(TAG_PARENT_ID, keyConfigurationDefinition
                .getParentId());
        memento.putString(TAG_SOURCE_ID, keyConfigurationDefinition
                .getSourceId());
    }

    static void writeKeyConfigurationDefinitions(IMemento memento, String name,
            List keyConfigurationDefinitions) {
        if (memento == null || name == null
                || keyConfigurationDefinitions == null)
            throw new NullPointerException();

        keyConfigurationDefinitions = new ArrayList(keyConfigurationDefinitions);
        Iterator iterator = keyConfigurationDefinitions.iterator();

        while (iterator.hasNext())
            Util.assertInstance(iterator.next(),
                    KeyConfigurationDefinition.class);

        iterator = keyConfigurationDefinitions.iterator();

        while (iterator.hasNext())
            writeKeyConfigurationDefinition(memento.createChild(name),
                    (KeyConfigurationDefinition) iterator.next());
    }

    static void writeKeySequenceBindingDefinition(IMemento memento,
            KeySequenceBindingDefinition keySequenceBindingDefinition) {
        if (memento == null || keySequenceBindingDefinition == null)
            throw new NullPointerException();

        memento.putString(TAG_CONTEXT_ID, keySequenceBindingDefinition
                .getContextId());
        memento.putString(TAG_COMMAND_ID, keySequenceBindingDefinition
                .getCommandId());
        memento.putString(TAG_KEY_CONFIGURATION_ID,
                keySequenceBindingDefinition.getKeyConfigurationId());
        memento.putString(TAG_KEY_SEQUENCE, keySequenceBindingDefinition
                .getKeySequence() != null ? keySequenceBindingDefinition
                .getKeySequence().toString() : null);
        memento.putString(TAG_LOCALE, keySequenceBindingDefinition.getLocale());
        memento.putString(TAG_PLATFORM, keySequenceBindingDefinition
                .getPlatform());
        memento.putString(TAG_SOURCE_ID, keySequenceBindingDefinition
                .getSourceId());
    }

    static void writeKeySequenceBindingDefinitions(IMemento memento,
            String name, List keySequenceBindingDefinitions) {
        if (memento == null || name == null
                || keySequenceBindingDefinitions == null)
            throw new NullPointerException();

        keySequenceBindingDefinitions = new ArrayList(
                keySequenceBindingDefinitions);
        Iterator iterator = keySequenceBindingDefinitions.iterator();

        while (iterator.hasNext())
            Util.assertInstance(iterator.next(),
                    KeySequenceBindingDefinition.class);

        iterator = keySequenceBindingDefinitions.iterator();

        while (iterator.hasNext())
            writeKeySequenceBindingDefinition(memento.createChild(name),
                    (KeySequenceBindingDefinition) iterator.next());
    }

    private Persistence() {
    }
}