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
package org.eclipse.e4.internal.core.services.osgi;

import java.util.*;
import org.eclipse.e4.core.services.osgi.IServiceAliasRegistry;

/**
 * 
 */
public class ServiceAliasRegistryImpl implements IServiceAliasRegistry {
	private Map registry = Collections.synchronizedMap(new HashMap());

	public String findAlias(String serviceName) {
		//create reverse lookup map if needed for performance
		synchronized (registry) {
			for (Iterator it = registry.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				if (entry.getValue().equals(serviceName))
					return (String) entry.getKey();
			}
		}
		return serviceName;
	}

	public void registerAlias(String alias, String clazz) {
		registry.put(alias, clazz);
	}

	public String resolveAlias(String alias) {
		String result = (String) registry.get(alias);
		return result == null ? alias : result;
	}

	public void unregisterAlias(String alias) {
		registry.remove(alias);
	}

}
