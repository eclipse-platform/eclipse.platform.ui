/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

public final class KeyBinding implements Comparable {

	private final static int HASH_INITIAL = 37;
	private final static int HASH_FACTOR = 47;
	
	public static KeyBinding create(String command, String keyConfiguration, KeySequence keySequence, String plugin, int rank, String scope)
		throws IllegalArgumentException {
		return new KeyBinding(command, keyConfiguration, keySequence, plugin, rank, scope);
	}

	private String command;
	private String keyConfiguration;
	private KeySequence keySequence;
	private String plugin;
	private int rank;
	private String scope;

	private KeyBinding(String command, String keyConfiguration, KeySequence keySequence, String plugin, int rank, String scope)
		throws IllegalArgumentException {
		super();
		
		if (keyConfiguration == null || keySequence == null || rank < 0 || scope == null)
			throw new IllegalArgumentException();	
		
		this.command = command;	
		this.keyConfiguration = keyConfiguration;
		this.keySequence = keySequence;
		this.plugin = plugin;
		this.rank = rank;
		this.scope = scope;
	}

	public int compareTo(Object object) {
		KeyBinding keyBinding = (KeyBinding) object;
		int compareTo = Util.compare(command, keyBinding.command); 
		
		if (compareTo == 0) {
			compareTo = keyConfiguration.compareTo(keyBinding.keyConfiguration);

			if (compareTo == 0) {		
				compareTo = keySequence.compareTo(keyBinding.keySequence);

				if (compareTo == 0) {		
					compareTo = Util.compare(plugin, keyBinding.plugin);

					if (compareTo == 0) {
						compareTo = rank - keyBinding.rank;

						if (compareTo == 0)
							compareTo = scope.compareTo(keyBinding.scope);
					}
				}
			}
		}

		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyBinding))
			return false;
		
		KeyBinding keyBinding = (KeyBinding) object;
		return Util.equals(command, keyBinding.command) && keyConfiguration.equals(keyBinding.keyConfiguration) && keySequence.equals(keyBinding.keySequence) && 
			Util.equals(plugin, keyBinding.plugin) && rank == keyBinding.rank && scope.equals(keyBinding.scope);
	}

	public String getCommand() {
		return command;
	}

	public String getKeyConfiguration() {
		return keyConfiguration;
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

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + Util.hashCode(command);		
		result = result * HASH_FACTOR + keyConfiguration.hashCode();
		result = result * HASH_FACTOR + keySequence.hashCode();		
		result = result * HASH_FACTOR + Util.hashCode(plugin);	
		result = result * HASH_FACTOR + rank;	
		result = result * HASH_FACTOR + scope.hashCode();
		return result;
	}
}
