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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

public final class ContextBindingDefinition
	implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ContextBindingDefinition.class.getName().hashCode();

	static Map contextBindingDefinitionsByCommandId(Collection contextBindingDefinitions) {
		if (contextBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = contextBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ContextBindingDefinition.class);
			ContextBindingDefinition contextBindingDefinition =
				(ContextBindingDefinition) object;
			String commandId = contextBindingDefinition.getCommandId();

			if (commandId != null) {
				Collection contextBindingDefinitions2 =
					(Collection) map.get(commandId);

				if (contextBindingDefinitions2 == null) {
					contextBindingDefinitions2 = new ArrayList();
					map.put(commandId, contextBindingDefinitions2);
				}

				contextBindingDefinitions2.add(contextBindingDefinition);
			}
		}

		return map;
	}

	private String contextId;
	private String commandId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String sourceId;
	private transient String string;

	public ContextBindingDefinition(
		String contextId,
		String commandId,
		String sourceId) {
		this.contextId = contextId;
		this.commandId = commandId;
		this.sourceId = sourceId;
	}

	public int compareTo(Object object) {
		ContextBindingDefinition castedObject =
			(ContextBindingDefinition) object;
		int compareTo = Util.compare(contextId, castedObject.contextId);

		if (compareTo == 0) {
			compareTo = Util.compare(commandId, castedObject.commandId);

			if (compareTo == 0)
				compareTo = Util.compare(sourceId, castedObject.sourceId);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ContextBindingDefinition))
			return false;

		ContextBindingDefinition castedObject =
			(ContextBindingDefinition) object;
		boolean equals = true;
		equals &= Util.equals(contextId, castedObject.contextId);
		equals &= Util.equals(commandId, castedObject.commandId);
		equals &= Util.equals(sourceId, castedObject.sourceId);
		return equals;
	}

	public String getContextId() {
		return contextId;
	}

	public String getCommandId() {
		return commandId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(contextId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(commandId);
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
			stringBuffer.append(sourceId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
