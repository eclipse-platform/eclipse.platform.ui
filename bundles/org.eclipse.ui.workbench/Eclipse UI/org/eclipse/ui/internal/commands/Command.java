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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.commands.CommandEvent;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandListener;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.commands.NotHandledException;
import org.eclipse.ui.internal.util.Util;

final class Command implements ICommand {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = Command.class.getName().hashCode();

    private String categoryId;

    private List commandListeners;

    private Set commandsWithListeners;

    private boolean defined;

    private String description;

    private IHandler handler;

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private String id;

    private List keySequenceBindings;

    private IKeySequenceBinding[] keySequenceBindingsAsArray;

    private String name;

    private transient String string;

    Command(Set commandsWithListeners, String id) {
        if (commandsWithListeners == null || id == null)
                throw new NullPointerException();
        this.commandsWithListeners = commandsWithListeners;
        this.id = id;
    }

    public void addCommandListener(ICommandListener commandListener) {
        if (commandListener == null) throw new NullPointerException();
        if (commandListeners == null) commandListeners = new ArrayList();
        if (!commandListeners.contains(commandListener))
                commandListeners.add(commandListener);
        commandsWithListeners.add(this);
    }

    public int compareTo(Object object) {
        Command castedObject = (Command) object;
        int compareTo = Util.compare(categoryId, castedObject.categoryId);
        if (compareTo == 0) {
            compareTo = Util.compare(defined, castedObject.defined);
            if (compareTo == 0) {
                compareTo = Util.compare(description, castedObject.description);
                if (compareTo == 0) {
                    compareTo = Util.compare(handler, castedObject.handler);
                    if (compareTo == 0) {
                        compareTo = Util.compare(id, castedObject.id);
                        if (compareTo == 0) {
                            compareTo = Util.compare(name, castedObject.name);
                        }
                    }
                }
            }
        }
        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Command)) return false;
        Command castedObject = (Command) object;
        boolean equals = true;
        equals &= Util.equals(categoryId, castedObject.categoryId);
        equals &= Util.equals(defined, castedObject.defined);
        equals &= Util.equals(description, castedObject.description);
        equals &= Util.equals(handler, castedObject.handler);
        equals &= Util.equals(id, castedObject.id);
        equals &= Util.equals(keySequenceBindings,
                castedObject.keySequenceBindings);
        equals &= Util.equals(name, castedObject.name);
        return equals;
    }

    public Object execute(Map parameterValuesByName) throws ExecutionException,
            NotHandledException {
        IHandler handler = this.handler;
        
        // Debugging output
        if (MutableCommandManager.DEBUG_COMMAND_EXECUTION) {
            System.out.print("KEYS >>>     executing "); //$NON-NLS-1$
            if (handler == null) {
                System.out.print("no handler"); //$NON-NLS-1$
            } else {
                System.out.print("'"); //$NON-NLS-1$
                System.out.print(handler.getClass().getName());
                System.out.print("' ("); //$NON-NLS-1$
                System.out.print(handler.hashCode());
                System.out.print(")"); //$NON-NLS-1$
            }
            System.out.println();
        }
        
        // Perform the execution, if there is a handler.
        if (handler != null)
            return handler.execute(parameterValuesByName);
        else {
            throw new NotHandledException("There is no handler to execute."); //$NON-NLS-1$
        }
    }

    void fireCommandChanged(CommandEvent commandEvent) {
        if (commandEvent == null) throw new NullPointerException();
        if (commandListeners != null)
                for (int i = 0; i < commandListeners.size(); i++)
                    ((ICommandListener) commandListeners.get(i))
                            .commandChanged(commandEvent);
    }

    public Map getAttributeValuesByName() throws NotHandledException {
        IHandler handler = this.handler;
        if (handler != null)
            return handler.getAttributeValuesByName();
        else
            throw new NotHandledException(
                    "There is no handler from which to retrieve attributes."); //$NON-NLS-1$
    }

    public String getCategoryId() throws NotDefinedException {
        if (!defined)
                throw new NotDefinedException(
                        "Cannot get category identifier from an undefined command."); //$NON-NLS-1$
        return categoryId;
    }

    public String getDescription() throws NotDefinedException {
        if (!defined)
                throw new NotDefinedException(
                        "Cannot get a description from an undefined command."); //$NON-NLS-1$
        return description;
    }

    public String getId() {
        return id;
    }

    public List getKeySequenceBindings() {
        return keySequenceBindings;
    }

    public String getName() throws NotDefinedException {
        if (!defined)
                throw new NotDefinedException(
                        "Cannot get the name from an undefined command."); //$NON-NLS-1$
        return name;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(categoryId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(handler);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(keySequenceBindings);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
            hashCodeComputed = true;
        }
        return hashCode;
    }

    public boolean isDefined() {
        return defined;
    }

    public boolean isHandled() {
        if (handler == null) return false;
        Map attributeValuesByName = handler.getAttributeValuesByName();
        if (attributeValuesByName.containsKey("handled") //$NON-NLS-1$
                && !Boolean.TRUE.equals(attributeValuesByName.get("handled"))) //$NON-NLS-1$
            return false;
        else
            return true;
    }

    public void removeCommandListener(ICommandListener commandListener) {
        if (commandListener == null) throw new NullPointerException();
        if (commandListeners != null) {
            commandListeners.remove(commandListener);
            if (commandListeners.isEmpty()) commandsWithListeners.remove(this);
        }
    }

    boolean setCategoryId(String categoryId) {
        if (!Util.equals(categoryId, this.categoryId)) {
            this.categoryId = categoryId;
            hashCodeComputed = false;
            hashCode = 0;
            string = null;
            return true;
        }
        return false;
    }

    boolean setDefined(boolean defined) {
        if (defined != this.defined) {
            this.defined = defined;
            hashCodeComputed = false;
            hashCode = 0;
            string = null;
            return true;
        }
        return false;
    }

    boolean setDescription(String description) {
        if (!Util.equals(description, this.description)) {
            this.description = description;
            hashCodeComputed = false;
            hashCode = 0;
            string = null;
            return true;
        }
        return false;
    }

    boolean setHandler(IHandler handler) {
        if (handler != this.handler) {
            this.handler = handler;
            hashCodeComputed = false;
            hashCode = 0;
            string = null;
            
            // Debugging output
            if ((MutableCommandManager.DEBUG_HANDLERS)
                    && ((MutableCommandManager.DEBUG_HANDLERS_COMMAND_ID == null) || (MutableCommandManager.DEBUG_HANDLERS_COMMAND_ID
                            .equals(id)))) {
                System.out.print("HANDLERS >>> Command('" + id //$NON-NLS-1$
                        + "' has changed to "); //$NON-NLS-1$
                if (handler == null) {
                    System.out.println("no handler"); //$NON-NLS-1$
                } else {
                    System.out.print("'"); //$NON-NLS-1$
                    System.out.print(handler);
                    System.out.println("' as its handler"); //$NON-NLS-1$
                }
            }
            
            return true;
        }
        return false;
    }

    boolean setKeySequenceBindings(List keySequenceBindings) {
        keySequenceBindings = Util.safeCopy(keySequenceBindings,
                IKeySequenceBinding.class);
        if (!Util.equals(keySequenceBindings, this.keySequenceBindings)) {
            this.keySequenceBindings = keySequenceBindings;
            this.keySequenceBindingsAsArray = (IKeySequenceBinding[]) this.keySequenceBindings
                    .toArray(new IKeySequenceBinding[this.keySequenceBindings.size()]);
            hashCodeComputed = false;
            hashCode = 0;
            string = null;
            return true;
        }
        return false;
    }

    boolean setName(String name) {
        if (!Util.equals(name, this.name)) {
            this.name = name;
            hashCodeComputed = false;
            hashCode = 0;
            string = null;
            return true;
        }
        return false;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append('[');
            stringBuffer.append(categoryId);
            stringBuffer.append(',');
            stringBuffer.append(defined);
            stringBuffer.append(',');
            stringBuffer.append(description);
            stringBuffer.append(',');
            stringBuffer.append(handler);
            stringBuffer.append(',');
            stringBuffer.append(id);
            stringBuffer.append(',');
            stringBuffer.append(keySequenceBindings);
            stringBuffer.append(',');
            stringBuffer.append(name);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }
        return string;
    }
}
