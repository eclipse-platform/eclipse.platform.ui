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
	private SortedSet contextBindings;
	private ICommandEvent commandEvent;
	private List commandListeners;
	private boolean defined;
	private String description;
	private String helpId;
	private String id;
	private SortedSet imageBindings;
	private SortedSet keyBindings;
	private String name;

	private transient IContextBinding[] contextBindingsAsArray;
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient IImageBinding[] imageBindingsAsArray;
	private transient IKeyBinding[] keyBindingsAsArray;
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
				compareTo = Util.compare((Comparable[]) contextBindingsAsArray, (Comparable[]) command.contextBindingsAsArray); 

				if (compareTo == 0) {
					compareTo = defined == false ? (command.defined == true ? -1 : 0) : 1;

					if (compareTo == 0) {		
						compareTo = Util.compare(description, command.description);	
	
						if (compareTo == 0) {
							compareTo = Util.compare(helpId, command.helpId);
	
							if (compareTo == 0) {
								compareTo = id.compareTo(command.id);	
	
								if (compareTo == 0) {	
									compareTo = Util.compare((Comparable[]) imageBindingsAsArray, (Comparable[]) command.imageBindingsAsArray);

									if (compareTo == 0)	{
										compareTo = Util.compare((Comparable[]) keyBindingsAsArray, (Comparable[]) command.keyBindingsAsArray);
											
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
		
		return compareTo;	
	}

	public boolean equals(Object object) {
		if (!(object instanceof Command))
			return false;

		Command command = (Command) object;	
		boolean equals = true;
		equals &= active == command.active;	
		equals &= Util.equals(categoryId, command.categoryId);
		equals &= contextBindings.equals(command.contextBindings);
		equals &= defined == command.defined;
		equals &= Util.equals(description, command.description);
		equals &= Util.equals(helpId, command.helpId);
		equals &= id.equals(command.id);
		equals &= imageBindings.equals(command.imageBindings);
		equals &= keyBindings.equals(command.keyBindings);
		equals &= name.equals(command.name);
		return equals;
	}

	public String getCategoryId()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();
			
		return categoryId;
	}

	public SortedSet getContextBindings()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return contextBindings;
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

	public SortedSet getImageBindings()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return imageBindings;
	}

	public SortedSet getKeyBindings()
		throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return keyBindings;
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
			hashCode = hashCode * HASH_FACTOR + contextBindings.hashCode();
			hashCode = hashCode * HASH_FACTOR + (defined ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(helpId);
			hashCode = hashCode * HASH_FACTOR + id.hashCode();
			hashCode = hashCode * HASH_FACTOR + imageBindings.hashCode();
			hashCode = hashCode * HASH_FACTOR + keyBindings.hashCode();
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

	public void removeCommandListener(ICommandListener commandListener) {
		if (commandListener == null)
			throw new NullPointerException();

		if (commandListeners != null)
			commandListeners.remove(commandListener);
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
			stringBuffer.append(contextBindings);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(helpId);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(imageBindings);
			stringBuffer.append(',');
			stringBuffer.append(keyBindings);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;
	}	

	void fireCommandChanged() {
		if (commandListeners != null) {
			for (int i = 0; i < commandListeners.size(); i++) {
				if (commandEvent == null)
					commandEvent = new CommandEvent(this);
							
				((ICommandListener) commandListeners.get(i)).commandChanged(commandEvent);
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

	boolean setContextBindings(SortedSet contextBindings) {
		contextBindings = Util.safeCopy(contextBindings, IContextBinding.class);
		
		if (!Util.equals(contextBindings, this.contextBindings)) {
			this.contextBindings = contextBindings;
			this.contextBindingsAsArray = (IContextBinding[]) this.contextBindings.toArray(new IContextBinding[this.contextBindings.size()]);
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

	boolean setImageBindings(SortedSet imageBindings) {
		imageBindings = Util.safeCopy(imageBindings, IImageBinding.class);
		
		if (!Util.equals(imageBindings, this.imageBindings)) {
			this.imageBindings = imageBindings;
			this.imageBindingsAsArray = (IImageBinding[]) this.imageBindings.toArray(new IImageBinding[this.imageBindings.size()]);
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}		
	
		return false;
	}

	boolean setKeyBindings(SortedSet keyBindings) {
		keyBindings = Util.safeCopy(keyBindings, IKeyBinding.class);
		
		if (!Util.equals(keyBindings, this.keyBindings)) {
			this.keyBindings = keyBindings;
			this.keyBindingsAsArray = (IKeyBinding[]) this.keyBindings.toArray(new IKeyBinding[this.keyBindings.size()]);
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
