/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions.keybindings;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.actions.Util;

public final class Binding implements Comparable {

	public final static String ELEMENT = "binding"; //$NON-NLS-1$
	private final static int HASH_INITIAL = 37;
	private final static int HASH_FACTOR = 47;
	private final static String ATTRIBUTE_ACTION = "action"; //$NON-NLS-1$
	private final static String ATTRIBUTE_CONFIGURATION = "configuration"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_PLUGIN = "plugin"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_RANK = "rank"; //$NON-NLS-1$
	private final static String ATTRIBUTE_SCOPE = "scope"; //$NON-NLS-1$
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	public static Binding create(String action, String configuration, KeySequence keySequence, String plugin, int rank, String scope)
		throws IllegalArgumentException {
		return new Binding(action, configuration, keySequence, plugin, rank, scope);
	}

	public static Binding read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		String action = memento.getString(ATTRIBUTE_ACTION);
		String configuration = memento.getString(ATTRIBUTE_CONFIGURATION);
		
		if (configuration == null)
			configuration = ZERO_LENGTH_STRING;

		KeySequence keySequence = KeySequence.read(memento.getChild(KeySequence.ELEMENT));	
		String plugin = memento.getString(ATTRIBUTE_PLUGIN);
		Integer rank = memento.getInteger(ATTRIBUTE_RANK);
		
		if (rank == null) {
			rank = new Integer(0);	
		}
		
		String scope = memento.getString(ATTRIBUTE_SCOPE);

		if (scope == null)
			scope = ZERO_LENGTH_STRING;

		return Binding.create(action, configuration, keySequence, plugin, rank.intValue(), scope);
	}
	
	private String action;
	private String configuration;
	private KeySequence keySequence;
	private String plugin;
	private int rank;
	private String scope;

	private Binding(String action, String configuration, KeySequence keySequence, String plugin, int rank, String scope)
		throws IllegalArgumentException {
		super();
		
		if (configuration == null || keySequence == null || keySequence.getKeyStrokes().size() == 0 || rank < 0 || scope == null)
			throw new IllegalArgumentException();	
		
		this.action = action;	
		this.configuration = configuration;
		this.keySequence = keySequence;
		this.plugin = plugin;
		this.rank = rank;
		this.scope = scope;
	}

	public String getAction() {
		return action;
	}

	public String getConfiguration() {
		return configuration;
	}
	
	public KeySequence getKeySequence() {
		return keySequence;	
	}

	public String getPlugin() {
		return plugin;
	}

	public int getRank() {
		return rank;	
	}

	public String getScope() {
		return scope;
	}

	public void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		memento.putString(ATTRIBUTE_ACTION, action);
		memento.putString(ATTRIBUTE_CONFIGURATION, configuration);
		keySequence.write(memento.createChild(KeySequence.ELEMENT));
		memento.putString(ATTRIBUTE_PLUGIN, plugin);
		memento.putInteger(ATTRIBUTE_RANK, rank);
		memento.putString(ATTRIBUTE_SCOPE, scope);
	}
	
	public int compareTo(Object object) {
		Binding binding = (Binding) object;
		int compareTo = Util.compare(action, binding.action); 
		
		if (compareTo == 0) {
			compareTo = configuration.compareTo(binding.configuration);

			if (compareTo == 0) {		
				compareTo = keySequence.compareTo(binding.keySequence);

				if (compareTo == 0) {		
					compareTo = Util.compare(plugin, binding.plugin);

					if (compareTo == 0) {
						compareTo = rank - binding.rank;

						if (compareTo == 0)
							compareTo = scope.compareTo(binding.scope);
					}
				}
			}
		}

		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Binding))
			return false;
		
		Binding binding = (Binding) object;
		return Util.equals(action, binding.action) && configuration.equals(binding.configuration) && keySequence.equals(binding.keySequence) && 
			Util.equals(plugin, binding.plugin) && rank == binding.rank && scope.equals(binding.scope);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + Util.hashCode(action);		
		result = result * HASH_FACTOR + configuration.hashCode();
		result = result * HASH_FACTOR + keySequence.hashCode();		
		result = result * HASH_FACTOR + Util.hashCode(plugin);	
		result = result * HASH_FACTOR + rank;	
		result = result * HASH_FACTOR + scope.hashCode();
		return result;
	}
}
