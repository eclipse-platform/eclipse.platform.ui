/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.bindings;

/**
 * <p>
 * A binding is a link between user input and the triggering of a particular
 * command. The most common example of a binding is a keyboard shortcut, but
 * there are also mouse and gesture bindings.
 * </p>
 * <p>
 * Bindings are linked to particular conditions within the application. Some of
 * these conditions change infrequently (e.g., locale, scheme), while some will
 * tend to change quite frequently (e.g., context). This allows the bindings to
 * be tailored to particular situations. For example, you may want a particular
 * set of bindings to become available when in a text editor. Or, perhaps, you
 * do not want to interfere with the Input Method Editor (IME) on Chinese
 * locales.
 * </p>
 * <p>
 * It is also possible to remove a particular binding. This is typically done as
 * part of user configuration (e.g., user changing keyboard shortcuts). However,
 * it can also be helpful when trying to change a binding on a particular locale
 * or platform. An "unbinding" is really just a binding with no command
 * identifier. For it to unbind a particular binding, it must match that binding
 * in its context identifier and scheme identifier. Subclasses (e.g.,
 * <code>KeyBinding</code>) may require other properties to match (e.g.,
 * <code>keySequence</code>). If these properties match, then this is an
 * unbinding. Note: the locale and platform can be different.
 * </p>
 * <p>
 * An example might be helpful. For the example, I will use the KeyBinding
 * concrete subclass. Image you have a key binding that looks like this:
 * <code><pre>
 * KeyBinding(command, scheme, context, &quot;Ctrl+Shift+F&quot;)
 * </pre></code> Now, on GTK+, the "Ctrl+Shift+F" interferes with some native
 * behaviour. So we decide we need to change it. <code><pre>
 * 
 *  
 *   
 *    
 *     
 *      
 *       
 *        
 *         
 *          
 *           
 *            	KeyBinding(null,scheme,context,&quot;Ctrl+Shift+F&quot;,gtk)
 *             	KeyBinding(command,scheme,context,&quot;Esc Ctrl+F&quot;,gtk)
 *             
 *            
 *           
 *          
 *         
 *        
 *       
 *      
 *     
 *    
 *   
 *  
 * </pre></code>
 * </p>
 * <p>
 * Bindings are intended to be immutable objects.
 * </p>
 * 
 * @since 3.1
 */
public abstract class Binding {

    /**
     * The type of binding that is defined by the system (i.e., by the
     * application developer). In the case of an application based on the
     * Eclipse workbench, this is the registry.
     */
    public static final int SYSTEM = 0;

    /**
     * The type of binding that is defined by the user (i.e., by the end user of
     * the application). In the case of an application based on the Eclipse
     * workbench, this is the preference store.
     */
    public static final int USER = 1;

    /**
     * The identifier of the command to which this binding applies. This value
     * may be <code>null</code> if this binding is meant to "unbind" an
     * existing binding.
     */
    private final String commandId;

    /**
     * The context identifier to which this binding applies. This context must
     * be active before this key binding becomes active. This value will never
     * be <code>null</code>.
     */
    private final String contextId;

    /**
     * The locale in which this binding applies. This value may be
     * <code>null</code> if this binding is meant to apply to all locales.
     * This string should be in the same format returned by
     * <code>Locale.getDefault().toString()</code>.
     */
    private final String locale;

    /**
     * The platform on which this binding applies. This value may be
     * <code>null</code> if this binding is meant to apply to all platforms.
     * This string should be in the same format returned by
     * <code>SWT.getPlatform</code>.
     */
    private final String platform;

    /**
     * The identifier of the scheme in which this binding applies. This value
     * will never be <code>null</code>.
     */
    private final String schemeId;

    /**
     * The string representation of this binding. This string is for debugging
     * purposes only, and is not meant to be displayed to the user. This value
     * is computed lazily.
     */
    protected transient String string = null;

    /**
     * The type of binding this represents. This is used to distinguish between
     * different priority levels for bindings. For example, in our case,
     * <code>USER</code> bindings override <code>SYSTEM</code> bindings.
     */
    private final int type;

    /**
     * Constructs a new instance of <code>Binding</code>.
     * 
     * @param commandId
     *            The command to which this binding applies; this value may be
     *            <code>null</code> if the binding is meant to "unbind" (no
     *            op).
     * @param schemeId
     *            The scheme to which this binding belongs; this value must not
     *            be <code>null</code>.
     * @param contextId
     *            The context to which this binding applies; this value must not
     *            be <code>null</code>.
     * @param locale
     *            The locale to which this binding applies; this value may be
     *            <code>null</code> if it applies to all locales.
     * @param platform
     *            The platform to which this binding applies; this value may be
     *            <code>null</code> if it applies to all platforms.
     * @param windowManager
     *            The window manager to which this binding applies; this value
     *            may be <code>null</code> if it applies to all window
     *            managers. This value is currently ignored.
     * @param type
     *            The type of binding. This should be either <code>SYSTEM</code>
     *            or <code>USER</code>.
     */
    protected Binding(final String commandId, final String schemeId,
            final String contextId, final String locale, final String platform,
            final String windowManager, final int type) {
        if (schemeId == null) {
            throw new NullPointerException("The scheme cannot be null"); //$NON-NLS-1$
        }

        if (contextId == null) {
            throw new NullPointerException("The context cannot be null"); //$NON-NLS-1$
        }

        if ((type != SYSTEM) && (type != USER)) {
            throw new IllegalArgumentException(
                    "The type must be SYSTEM or USER"); //$NON-NLS-1$
        }

        this.commandId = commandId;
        this.schemeId = schemeId;
        this.contextId = contextId;
        this.locale = locale;
        this.platform = platform;
        this.type = type;
    }

    /**
     * Tests whether this binding is equal to another object. Bindings are only
     * equal to other bindings with equivalent values.
     * 
     * @param object
     *            The object with which to compare; may be <code>null</code>.
     * @return <code>true</code> if the object is the binding with equivalent
     *         values for all of its properties; <code>false</code> otherwise.
     */
    public abstract boolean equals(final Object object);

    /**
     * Returns the identifier of the command to which this binding applies. If
     * the identifier is <code>null</code>, then this binding is "unbinding"
     * an existing binding.
     * 
     * @return The command identifier; may be <code>null</code>.
     */
    public final String getCommandId() {
        return commandId;
    }

    /**
     * Returns the identifier of the context in which this binding applies.
     * 
     * @return The context identifier; never <code>null</code>.
     */
    public final String getContextId() {
        return contextId;
    }

    /**
     * Returns the locale in which this binding applies. If the locale is
     * <code>null</code>, then this binding applies to all locales. This
     * string is the same format as returned by
     * <code>Locale.getDefault().toString()</code>.
     * 
     * @return The locale; may be <code>null</code>.
     */
    public final String getLocale() {
        return locale;
    }

    /**
     * Returns the platform on which this binding applies. If the platform is
     * <code>null</code>, then this binding applies to all platforms. This
     * string is the same format as returned by <code>SWT.getPlatform()</code>.
     * 
     * @return The platform; may be <code>null</code>.
     */
    public final String getPlatform() {
        return platform;
    }

    /**
     * Returns the identifier of the scheme in which this binding applies.
     * 
     * @return The scheme identifier; never <code>null</code>.
     */
    public final String getSchemeId() {
        return schemeId;
    }

    /**
     * Returns the sequence of trigger for a given binding. The triggers can be
     * anything, but above all it must be hashable. This trigger sequence is
     * used by the binding manager to distinguish between different bindings.
     * 
     * @return The object representing an input event that will trigger this
     *         binding; must not be <code>null</code>.
     */
    public abstract TriggerSequence getTriggerSequence();

    /**
     * Returns the type for this binding. As it stands now, this value will
     * either by <code>SYSTEM</code> or <code>USER</code>. In the future,
     * more types might be added.
     * 
     * @return The type for this binding.
     */
    public final int getType() {
        return type;
    }

    /**
     * Computes the hash code for this binding based on all of its attributes.
     * 
     * @return The hash code for this binding.
     */
    public abstract int hashCode();

    /**
     * The string representation of this binding -- for debugging purposes only.
     * This string should not be shown to an end user. This should be overridden
     * by subclasses that add properties.
     * 
     * @return The string representation; never <code>null</code>.
     */
    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Binding("); //$NON-NLS-1$
            stringBuffer.append(commandId);
            stringBuffer.append(',');
            stringBuffer.append(contextId);
            stringBuffer.append(',');
            stringBuffer.append(schemeId);
            stringBuffer.append(',');
            stringBuffer.append(locale);
            stringBuffer.append(',');
            stringBuffer.append(platform);
            stringBuffer.append(')');
            string = stringBuffer.toString();
        }

        return string;
    }
}
