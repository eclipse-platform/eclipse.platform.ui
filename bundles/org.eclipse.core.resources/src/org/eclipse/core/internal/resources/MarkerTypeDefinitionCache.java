package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import java.util.*;

public class MarkerTypeDefinitionCache {

	// cache of the marker definitions
	protected HashMap definitions;

	/** Cache of marker type hierachies */
	protected HashMap lookup;
/** Constructs a new type cache.
 */
public MarkerTypeDefinitionCache() {
	initializeCache();
}
private void computeSuperTypes(String id) {
	Set entry = new HashSet(5);
	List queue = new ArrayList(5);
	queue.add(id);
	while (!queue.isEmpty()) {
		String type = (String) queue.remove(0);
		entry.add(type);
		MarkerTypeDefinition def = (MarkerTypeDefinition) definitions.get(type);
		if (def != null) {
			Set newEntries = def.getSuperTypes();
			if (newEntries != null)
				queue.addAll(newEntries);
		}
	}
	lookup.put(id, entry);
}
private void initializeCache() {
	loadDefinitions();
	lookup = new HashMap(definitions.size());
	for (Iterator i = definitions.keySet().iterator(); i.hasNext();)
		computeSuperTypes((String) i.next());
}
/**
 * Returns true if the given marker type is defined to be persistent.
 */
public boolean isPersistent(String type) {
	MarkerTypeDefinition def = (MarkerTypeDefinition) definitions.get(type);
	return def != null && def.persistent();
}
/**
 * Returns true if the given target class has the specified type as a super type.
 */
public boolean isSubtype(String type, String superType) {
	Set entry = (Set) lookup.get(type);
	return entry != null && entry.contains(superType);
}
private void loadDefinitions() {
	IExtensionPoint point = Platform.getPluginRegistry().getExtensionPoint(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_MARKERS);
	IExtension[] types = point.getExtensions();
	definitions = new HashMap(types.length);
	for (int i = 0; i < types.length; i++)
		definitions.put(types[i].getUniqueIdentifier(), new MarkerTypeDefinition(types[i]));
}
}
