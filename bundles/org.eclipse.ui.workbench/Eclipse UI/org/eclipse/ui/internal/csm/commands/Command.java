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
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.internal.csm.commands.api.CommandNotDefinedException;
import org.eclipse.ui.internal.csm.commands.api.ICommand;
import org.eclipse.ui.internal.csm.commands.api.ICommandEvent;
import org.eclipse.ui.internal.csm.commands.api.ICommandListener;
import org.eclipse.ui.internal.csm.commands.api.IPatternBinding;
import org.eclipse.ui.internal.util.Util;

final class Command implements ICommand {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Command.class.getName().hashCode();

	private boolean active;
	private List commandListeners;
	private CommandManager commandManager;
	private boolean defined;
	private String description;
	private boolean enabled;
	private String id;
	private String name;
	private String parentId;
	private List patternBindings;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient IPatternBinding[] patternBindingsAsArray;
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
			compareTo = Util.compare(defined, command.defined);
			
			if (compareTo == 0) {
				compareTo = Util.compare(description, command.description);

				if (compareTo == 0) {
					compareTo = Util.compare(enabled, command.enabled);
								
					if (compareTo == 0) {		
						compareTo = Util.compare(id, command.id);			
					
						if (compareTo == 0) {
							compareTo = Util.compare(name, command.name);

							if (compareTo == 0) {
								compareTo = Util.compare(parentId, command.parentId);

								if (compareTo == 0) 
									compareTo = Util.compare((Comparable[]) patternBindingsAsArray, (Comparable[]) command.patternBindingsAsArray); 
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
		equals &= Util.equals(defined, command.defined);
		equals &= Util.equals(description, command.description);
		equals &= Util.equals(enabled, command.enabled);
		equals &= Util.equals(id, command.id);
		equals &= Util.equals(name, command.name);
		equals &= Util.equals(parentId, command.parentId);
		equals &= Util.equals(patternBindings, command.patternBindings);		
		return equals;
	}

	public String getDescription()
		throws CommandNotDefinedException {
		if (!defined)
			throw new CommandNotDefinedException();
			
		return description;	
	}
	
	public String getId() {
		return id;	
	}
	
	public String getName()
		throws CommandNotDefinedException {
		if (!defined)
			throw new CommandNotDefinedException();

		return name;
	}	

	public String getParentId()
		throws CommandNotDefinedException {
		if (!defined)
			throw new CommandNotDefinedException();

		return parentId;
	}			
	
	public List getPatternBindings() {
		return patternBindings;
	}		
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(active);			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);	
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(enabled);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(patternBindings);
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

	public boolean match(String string) {
		boolean match = false;
			
		if (isDefined())
			for (Iterator iterator = patternBindings.iterator(); iterator.hasNext();) {
				IPatternBinding patternBinding = (IPatternBinding) iterator.next();
			
				if (patternBinding.isInclusive() && !match)
					match = patternBinding.getPattern().matcher(string).matches();
				else if (!patternBinding.isInclusive() && match)
					match = !patternBinding.getPattern().matcher(string).matches();
			}

		return match;
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
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(enabled);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(',');
			stringBuffer.append(parentId);
			stringBuffer.append(',');
			stringBuffer.append(patternBindings);
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

	boolean setParentId(String parentId) {
		if (!Util.equals(parentId, this.parentId)) {
			this.parentId = parentId;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		

		return false;
	}	
	
	boolean setPatternBindings(List patternBindings) {
		patternBindings = Util.safeCopy(patternBindings, IPatternBinding.class);
		
		if (!Util.equals(patternBindings, this.patternBindings)) {
			this.patternBindings = patternBindings;
			this.patternBindingsAsArray = (IPatternBinding[]) this.patternBindings.toArray(new IPatternBinding[this.patternBindings.size()]);
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		
	
		return false;
	}	
}
