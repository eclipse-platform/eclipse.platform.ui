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

public final class SequenceBinding implements Comparable {

	private final static int HASH_FACTOR = 107;
	private final static int HASH_INITIAL = 117;
	
	public static SequenceBinding create(String configuration, String command, String locale, String platform, String plugin, int rank, String scope, Sequence sequence)
		throws IllegalArgumentException {
		return new SequenceBinding(configuration, command, locale, platform, plugin, rank, scope, sequence);
	}

	private String configuration;
	private String command;
	private String locale;
	private String platform;
	private String plugin;
	private int rank;
	private String scope;
	private Sequence sequence;

	private SequenceBinding(String configuration, String command, String locale, String platform, String plugin, int rank, String scope, Sequence sequence)
		throws IllegalArgumentException {
		super();
		
		if (configuration == null || locale == null || platform == null || rank < 0 || scope == null || sequence == null)
			throw new IllegalArgumentException();	
		
		this.configuration = configuration;
		this.command = command;	
		this.locale = locale;
		this.platform = platform;
		this.plugin = plugin;
		this.rank = rank;
		this.scope = scope;
		this.sequence = sequence;
	}

	public int compareTo(Object object) {
		SequenceBinding sequenceBinding = (SequenceBinding) object;
		int compareTo = configuration.compareTo(sequenceBinding.configuration); 
		
		if (compareTo == 0) {
			compareTo = Util.compare(command, sequenceBinding.command);

			if (compareTo == 0) {		
				compareTo = locale.compareTo(sequenceBinding.locale);

				if (compareTo == 0) {		
					compareTo = platform.compareTo(sequenceBinding.platform);

					if (compareTo == 0) {		
						compareTo = Util.compare(plugin, sequenceBinding.plugin);

						if (compareTo == 0) {		
							compareTo = rank - sequenceBinding.rank;
		
							if (compareTo == 0) {
								compareTo = scope.compareTo(sequenceBinding.scope);
		
								if (compareTo == 0)
									compareTo = sequence.compareTo(sequenceBinding.sequence);
							}
						}
					}
				}
			}
		}

		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof SequenceBinding))
			return false;
		
		SequenceBinding sequenceBinding = (SequenceBinding) object;
		return configuration.equals(sequenceBinding.configuration) && Util.equals(command, sequenceBinding.command) && locale.equals(sequenceBinding.locale) && 
			platform.equals(sequenceBinding.platform) && Util.equals(plugin, sequenceBinding.plugin) && rank == sequenceBinding.rank && scope.equals(sequenceBinding.scope) && sequence.equals(sequenceBinding.sequence);
	}

	public String getConfiguration() {
		return configuration;
	}
	
	public String getCommand() {
		return command;
	}

	public String getLocale() {
		return locale;
	}
	
	public String getPlatform() {
		return platform;
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

	public Sequence getSequence() {
		return sequence;	
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + configuration.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(command);		
		result = result * HASH_FACTOR + locale.hashCode();
		result = result * HASH_FACTOR + platform.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(plugin);	
		result = result * HASH_FACTOR + rank;	
		result = result * HASH_FACTOR + scope.hashCode();
		result = result * HASH_FACTOR + sequence.hashCode();		
		return result;
	}
}
