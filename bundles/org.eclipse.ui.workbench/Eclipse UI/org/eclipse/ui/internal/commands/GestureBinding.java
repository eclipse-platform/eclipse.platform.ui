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

public final class GestureBinding implements Comparable {

	private final static int HASH_INITIAL = 37;
	private final static int HASH_FACTOR = 47;
	
	public static GestureBinding create(String command, String gestureConfiguration, GestureSequence gestureSequence, String plugin, int rank, String scope)
		throws IllegalArgumentException {
		return new GestureBinding(command, gestureConfiguration, gestureSequence, plugin, rank, scope);
	}

	private String command;
	private String gestureConfiguration;
	private GestureSequence gestureSequence;
	private String plugin;
	private int rank;
	private String scope;

	private GestureBinding(String command, String gestureConfiguration, GestureSequence gestureSequence, String plugin, int rank, String scope)
		throws IllegalArgumentException {
		super();
		
		if (gestureConfiguration == null || gestureSequence == null || gestureSequence.getGestureStrokes().size() == 0 || rank < 0 || scope == null)
			throw new IllegalArgumentException();	
		
		this.command = command;	
		this.gestureConfiguration = gestureConfiguration;
		this.gestureSequence = gestureSequence;
		this.plugin = plugin;
		this.rank = rank;
		this.scope = scope;
	}

	public int compareTo(Object object) {
		GestureBinding gestureBinding = (GestureBinding) object;
		int compareTo = Util.compare(command, gestureBinding.command); 
		
		if (compareTo == 0) {
			compareTo = gestureConfiguration.compareTo(gestureBinding.gestureConfiguration);

			if (compareTo == 0) {		
				compareTo = gestureSequence.compareTo(gestureBinding.gestureSequence);

				if (compareTo == 0) {		
					compareTo = Util.compare(plugin, gestureBinding.plugin);

					if (compareTo == 0) {
						compareTo = rank - gestureBinding.rank;

						if (compareTo == 0)
							compareTo = scope.compareTo(gestureBinding.scope);
					}
				}
			}
		}

		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof GestureBinding))
			return false;
		
		GestureBinding gestureBinding = (GestureBinding) object;
		return Util.equals(command, gestureBinding.command) && gestureConfiguration.equals(gestureBinding.gestureConfiguration) && gestureSequence.equals(gestureBinding.gestureSequence) && 
			Util.equals(plugin, gestureBinding.plugin) && rank == gestureBinding.rank && scope.equals(gestureBinding.scope);
	}

	public String getCommand() {
		return command;
	}

	public String getGestureConfiguration() {
		return gestureConfiguration;
	}
	
	public GestureSequence getGestureSequence() {
		return gestureSequence;	
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
		result = result * HASH_FACTOR + gestureConfiguration.hashCode();
		result = result * HASH_FACTOR + gestureSequence.hashCode();		
		result = result * HASH_FACTOR + Util.hashCode(plugin);	
		result = result * HASH_FACTOR + rank;	
		result = result * HASH_FACTOR + scope.hashCode();
		return result;
	}
}
