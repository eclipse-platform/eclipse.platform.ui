/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.csm.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.internal.csm.commands.api.NotDefinedException;
import org.eclipse.ui.internal.csm.commands.api.ICommand;
import org.eclipse.ui.internal.csm.commands.api.ICommandEvent;
import org.eclipse.ui.internal.csm.commands.api.ICommandListener;
import org.eclipse.ui.internal.csm.commands.api.IKeySequenceBinding;
import org.eclipse.ui.internal.util.Util;

final class Command implements ICommand {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Command.class.getName().hashCode();

	private boolean active;
	private String categoryId;
	private List commandListeners;
	private CommandManager commandManager;
	private boolean defined;
	private String description;
	private boolean enabled;
	private String id;
	private List keySequenceBindings;
	private String name;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient IKeySequenceBinding[] keySequenceBindingsAsArray;
	private transient String string;
	
	Command(CommandManager commandManager, String id) {	
		if (commandManager == null || id == null)
			throw new NullPointerException();

		this.commandManager = commandManager;
		this.id = id;
	}

	public void addCommandListener(ICommandListener commandListener) {
		if (commandListener == null)
			throw new NullPointerException();
		
		if (commandListeners == null)
			commandListeners = new ArrayList();
		
		if (!commandListeners.contains(commandListener))
			commandListeners.add(commandListener);
		
		commandManager.getCommandsWithListeners().add(this);
	}

	public int compareTo(Object object) {
		Command command = (Command) object;
		int compareTo = Util.compare(active, command.active);

		if (compareTo == 0) {
			compareTo = Util.compare(categoryId, command.categoryId);
		
			if (compareTo == 0) {
				compareTo = Util.compare(defined, command.defined);
				
				if (compareTo == 0) {
					compareTo = Util.compare(description, command.description);
	
					if (compareTo == 0) {
						compareTo = Util.compare(enabled, command.enabled);
									
						if (compareTo == 0) {		
							compareTo = Util.compare(id, command.id);			
						
							if (compareTo == 0) {
								compareTo = Util.compare(name, command.name);

								if (compareTo == 0) 
									compareTo = Util.compare((Comparable[]) keySequenceBindingsAsArray, (Comparable[]) command.keySequenceBindingsAsArray); 
							}
						}
					}
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Command))
			return false;

		Command command = (Command) object;	
		boolean equals = true;
		equals &= Util.equals(active, command.active);
		equals &= Util.equals(categoryId, command.categoryId);
		equals &= Util.equals(defined, command.defined);
		equals &= Util.equals(description, command.description);
		equals &= Util.equals(enabled, command.enabled);
		equals &= Util.equals(id, command.id);
		equals &= Util.equals(keySequenceBindings, command.keySequenceBindings);		
		equals &= Util.equals(name, command.name);
		return equals;
	}

	public String getCategoryId()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return categoryId;
	}

	public String getDescription()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();
			
		return description;	
	}
	
	public String getId() {
		return id;	
	}

	public List getKeySequenceBindings() {
		return keySequenceBindings;
	}

	public String getName()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return name;
	}	
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(active);			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(categoryId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);	
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(enabled);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(keySequenceBindings);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public boolean isActive() {
		return active;
	}
	
	public boolean isDefined() {
		return defined;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void removeCommandListener(ICommandListener commandListener) {
		if (commandListener == null)
			throw new NullPointerException();

		if (commandListeners != null)
			commandListeners.remove(commandListener);
		
		if (commandListeners.isEmpty())
			commandManager.getCommandsWithListeners().remove(this);
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(active);
			stringBuffer.append(',');
			stringBuffer.append(categoryId);
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(enabled);
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
	
	void fireCommandChanged(ICommandEvent commandEvent) {
		if (commandEvent == null)
			throw new NullPointerException();
		
		if (commandListeners != null)
			for (int i = 0; i < commandListeners.size(); i++)
				((ICommandListener) commandListeners.get(i)).commandChanged(commandEvent);
	}
	
	boolean setActive(boolean active) {
		if (active != this.active) {
			this.active = active;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		

		return false;
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

	boolean setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			this.enabled = enabled;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		

		return false;
	}

	boolean setKeySequenceBindings(List keySequenceBindings) {
		keySequenceBindings = Util.safeCopy(keySequenceBindings, IKeySequenceBinding.class);
		
		if (!Util.equals(keySequenceBindings, this.keySequenceBindings)) {
			this.keySequenceBindings = keySequenceBindings;
			this.keySequenceBindingsAsArray = (IKeySequenceBinding[]) this.keySequenceBindings.toArray(new IKeySequenceBinding[this.keySequenceBindings.size()]);
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
}
