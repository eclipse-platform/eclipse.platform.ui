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

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandEvent;
import org.eclipse.ui.commands.ICommandListener;
import org.eclipse.ui.commands.IContextBinding;
import org.eclipse.ui.commands.IImageBinding;
import org.eclipse.ui.commands.IKeyBinding;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class Command implements ICommand {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Command.class.getName().hashCode();

	private boolean active;
	private String categoryId;
	private SortedSet contextBindingSet;
	private ICommandEvent commandEvent;
	private List commandListeners;
	private boolean defined;
	private String description;
	private String helpId;
	private String id;
	private SortedSet imageBindingSet;
	private boolean inContext;
	private SortedSet keyBindingSet;
	private String name;

	private transient IContextBinding[] contextBindingSetAsArray;
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient IImageBinding[] imageBindingSetAsArray;
	private transient IKeyBinding[] keyBindingSetAsArray;
	private transient String string;
	
	Command(String id) {	
		if (id == null)
			throw new NullPointerException();

		this.id = id;
	}

	public void addCommandListener(ICommandListener commandListener) {
		if (commandListener == null)
			throw new NullPointerException();
		
		if (commandListeners == null)
			commandListeners = new ArrayList();
		
		if (!commandListeners.contains(commandListener))
			commandListeners.add(commandListener);
	}

	public int compareTo(Object object) {
		Command command = (Command) object;
		int compareTo = active == false ? (command.active == true ? -1 : 0) : 1;
		
		if (compareTo == 0) {
			compareTo = Util.compare(categoryId, command.categoryId);

			if (compareTo == 0) {	
				compareTo = Util.compare((Comparable[]) contextBindingSetAsArray, (Comparable[]) command.contextBindingSetAsArray); 

				if (compareTo == 0) {
					compareTo = defined == false ? (command.defined == true ? -1 : 0) : 1;

					if (compareTo == 0) {		
						compareTo = Util.compare(description, command.description);	
	
						if (compareTo == 0) {
							compareTo = Util.compare(helpId, command.helpId);
	
							if (compareTo == 0) {
								compareTo = id.compareTo(command.id);	
	
								if (compareTo == 0) {	
									compareTo = Util.compare((Comparable[]) imageBindingSetAsArray, (Comparable[]) command.imageBindingSetAsArray);

									if (compareTo == 0) {	
										compareTo = inContext == false ? (command.inContext == true ? -1 : 0) : 1;
				
										if (compareTo == 0)	{
											compareTo = Util.compare((Comparable[]) keyBindingSetAsArray, (Comparable[]) command.keyBindingSetAsArray);
											
											if (compareTo == 0)
												compareTo = name.compareTo(command.name);	
										}
									}
								}
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
		equals &= active == command.active;	
		equals &= Util.equals(categoryId, command.categoryId);
		equals &= contextBindingSet.equals(command.contextBindingSet);
		equals &= defined == command.defined;
		equals &= Util.equals(description, command.description);
		equals &= Util.equals(helpId, command.helpId);
		equals &= id.equals(command.id);
		equals &= imageBindingSet.equals(command.imageBindingSet);
		equals &= inContext == command.inContext;	
		equals &= keyBindingSet.equals(command.keyBindingSet);
		equals &= name.equals(command.name);
		return equals;
	}

	public String getCategoryId()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();
			
		return categoryId;
	}

	public SortedSet getContextBindingSet()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return contextBindingSet;
	}

	public String getDescription()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();
			
		return description;	
	}

	public String getHelpId()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return helpId;	
	}
	
	public String getId() {
		return id;	
	}

	public SortedSet getImageBindingSet()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return imageBindingSet;
	}

	public SortedSet getKeyBindingSet()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return keyBindingSet;
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
			hashCode = hashCode * HASH_FACTOR + (active ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(categoryId);
			hashCode = hashCode * HASH_FACTOR + contextBindingSet.hashCode();
			hashCode = hashCode * HASH_FACTOR + (defined ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(helpId);
			hashCode = hashCode * HASH_FACTOR + id.hashCode();
			hashCode = hashCode * HASH_FACTOR + imageBindingSet.hashCode();
			hashCode = hashCode * HASH_FACTOR + (inContext ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
			hashCode = hashCode * HASH_FACTOR + keyBindingSet.hashCode();
			hashCode = hashCode * HASH_FACTOR + name.hashCode();
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

	public boolean isInContext() {
		return inContext;
	}

	public void removeCommandListener(ICommandListener commandListener) {
		if (commandListener == null)
			throw new NullPointerException();

		if (commandListeners != null) {
			commandListeners.remove(commandListener);
			
			if (commandListeners.isEmpty())
				commandListeners = null;
		}
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(active);
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(categoryId);
			stringBuffer.append(',');
			stringBuffer.append(contextBindingSet);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(helpId);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(imageBindingSet);
			stringBuffer.append(',');
			stringBuffer.append(inContext);
			stringBuffer.append(',');
			stringBuffer.append(keyBindingSet);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;
	}	

	void fireCommandChanged() {
		if (commandListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(commandListeners).iterator();			
			
			if (iterator.hasNext()) {
				if (commandEvent == null)
					commandEvent = new CommandEvent(this);
				
				while (iterator.hasNext())	
					((ICommandListener) iterator.next()).commandChanged(commandEvent);
			}							
		}			
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

	boolean setContextBindingSet(SortedSet contextBindingSet) {
		contextBindingSet = Util.safeCopy(contextBindingSet, IContextBinding.class);
		
		if (!Util.equals(contextBindingSet, this.contextBindingSet)) {
			this.contextBindingSet = contextBindingSet;
			this.contextBindingSetAsArray = (IContextBinding[]) this.contextBindingSet.toArray(new IContextBinding[this.contextBindingSet.size()]);
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

	boolean setHelpId(String helpId) {
		if (!Util.equals(helpId, this.helpId)) {
			this.helpId = helpId;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		

		return false;
	}

	boolean setImageBindingSet(SortedSet imageBindingSet) {
		imageBindingSet = Util.safeCopy(imageBindingSet, IImageBinding.class);
		
		if (!Util.equals(imageBindingSet, this.imageBindingSet)) {
			this.imageBindingSet = imageBindingSet;
			this.imageBindingSetAsArray = (IImageBinding[]) this.imageBindingSet.toArray(new IImageBinding[this.imageBindingSet.size()]);
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		
	
		return false;
	}

	boolean setInContext(boolean inContext) {
		if (inContext != this.inContext) {
			this.inContext = inContext;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		

		return false;
	}

	boolean setKeyBindingSet(SortedSet keyBindingSet) {
		keyBindingSet = Util.safeCopy(keyBindingSet, IKeyBinding.class);
		
		if (!Util.equals(keyBindingSet, this.keyBindingSet)) {
			this.keyBindingSet = keyBindingSet;
			this.keyBindingSetAsArray = (IKeyBinding[]) this.keyBindingSet.toArray(new IKeyBinding[this.keyBindingSet.size()]);
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		
	
		return false;
	}

	boolean setName(String name) {
		if (name == null)
			throw new NullPointerException();
		
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
