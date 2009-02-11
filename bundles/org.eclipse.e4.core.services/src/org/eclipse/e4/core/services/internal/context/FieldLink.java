/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import java.lang.reflect.Field;

import org.eclipse.e4.core.services.context.IEclipseContext;

public class FieldLink extends ObjectLink {
	
	private Field field;	// TBD this should be wrapped into a WeakReference but then class gets unloaded 
	// due to the current part implementation
	
	public FieldLink(Object userObject, IEclipseContext context, String candidateName, Field field) {
		super(userObject, context, candidateName);
		this.field = field;
	}
	
	public void run() {
//		Field field = (Field)fieldRef.get();
//		if (field == null)
//			return;
		String key = findKey(candidateName);
		if (key == null) { // value not set in the context
			if (isSet) { // value has been removed from the context
				setField(field, null);
				isSet = false;
			}
			return;
		}
		Object value = context.get(key);
		isSet = setField(field, value);
	}

}
