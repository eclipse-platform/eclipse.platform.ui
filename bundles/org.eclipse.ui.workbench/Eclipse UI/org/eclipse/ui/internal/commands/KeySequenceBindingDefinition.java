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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

public final class KeySequenceBindingDefinition
	implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		KeySequenceBindingDefinition.class.getName().hashCode();

	static Map keySequenceBindingDefinitionsByCommandId(Collection keySequenceBindingDefinitions) {
		if (keySequenceBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = keySequenceBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, KeySequenceBindingDefinition.class);
			KeySequenceBindingDefinition keySequenceBindingDefinition =
				(KeySequenceBindingDefinition) object;
			String commandId = keySequenceBindingDefinition.getCommandId();

			if (commandId != null) {
				Collection keySequenceBindingDefinitions2 =
					(Collection) map.get(commandId);

				if (keySequenceBindingDefinitions2 == null) {
					keySequenceBindingDefinitions2 = new ArrayList();
					map.put(commandId, keySequenceBindingDefinitions2);
				}

				keySequenceBindingDefinitions2.add(
					keySequenceBindingDefinition);
			}
		}

		return map;
	}

	private String contextId;
	private String commandId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String keyConfigurationId;
	private KeySequence keySequence;
	private String locale;
	private String platform;
	private String sourceId;
	private transient String string;

	public KeySequenceBindingDefinition(
		String contextId,
		String commandId,
		String keyConfigurationId,
		KeySequence keySequence,
		String locale,
		String platform,
		String sourceId) {
		this.contextId = contextId;
		this.commandId = commandId;
		this.keyConfigurationId = keyConfigurationId;
		this.keySequence = keySequence;
		this.locale = locale;
		this.platform = platform;
		this.sourceId = sourceId;
	}

	public int compareTo(Object object) {
		KeySequenceBindingDefinition castedObject =
			(KeySequenceBindingDefinition) object;
		int compareTo = Util.compare(contextId, castedObject.contextId);

		if (compareTo == 0) {
			compareTo = Util.compare(commandId, castedObject.commandId);

			if (compareTo == 0) {
				compareTo =
					Util.compare(
						keyConfigurationId,
						castedObject.keyConfigurationId);

				if (compareTo == 0) {
					compareTo =
						Util.compare(keySequence, castedObject.keySequence);

					if (compareTo == 0) {
						compareTo = Util.compare(locale, castedObject.locale);

						if (compareTo == 0) {
							compareTo =
								Util.compare(platform, castedObject.platform);

							if (compareTo == 0)
								compareTo =
									Util.compare(
										sourceId,
										castedObject.sourceId);
						}
					}
				}
			}
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof KeySequenceBindingDefinition))
			return false;

		KeySequenceBindingDefinition castedObject =
			(KeySequenceBindingDefinition) object;
		boolean equals = true;
		equals &= Util.equals(contextId, castedObject.contextId);
		equals &= Util.equals(commandId, castedObject.commandId);
		equals
			&= Util.equals(keyConfigurationId, castedObject.keyConfigurationId);
		equals &= Util.equals(keySequence, castedObject.keySequence);
		equals &= Util.equals(locale, castedObject.locale);
		equals &= Util.equals(platform, castedObject.platform);
		equals &= Util.equals(sourceId, castedObject.sourceId);
		return equals;
	}

	public String getContextId() {
		return contextId;
	}

	public String getCommandId() {
		return commandId;
	}

	public String getKeyConfigurationId() {
		return keyConfigurationId;
	}

	public KeySequence getKeySequence() {
		return keySequence;
	}

	public String getLocale() {
		return locale;
	}

	public String getPlatform() {
		return platform;
	}

	public String getSourceId() {
		return sourceId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(contextId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(commandId);
			hashCode =
				hashCode * HASH_FACTOR + Util.hashCode(keyConfigurationId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(keySequence);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(locale);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(platform);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(contextId);
			stringBuffer.append(',');
			stringBuffer.append(commandId);
			stringBuffer.append(',');
			stringBuffer.append(keyConfigurationId);
			stringBuffer.append(',');
			stringBuffer.append(keySequence);
			stringBuffer.append(',');
			stringBuffer.append(locale);
			stringBuffer.append(',');
			stringBuffer.append(platform);
			stringBuffer.append(',');
			stringBuffer.append(sourceId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
