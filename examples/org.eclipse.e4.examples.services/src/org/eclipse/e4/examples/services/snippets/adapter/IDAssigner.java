package org.eclipse.e4.examples.services.snippets.adapter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;

public class IDAssigner implements IAdapterFactory {
	int currentId;
	Map<Object, String> assignedIds = new HashMap<Object, String>(); // Object->its
																		// id

	public IDAssigner() {
		currentId = 1000;
	}
	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Object adaptableObject, Class adapterType) {
		if (adapterType.equals(ThingWithId.class)) {
			if (!assignedIds.containsKey(adaptableObject)) {
				String id = Integer.toString(currentId);
				currentId++;
				assignedIds.put(adaptableObject, id);
			}
			return new ThingWithId() {
				public String getUniqueId() {
					return assignedIds.get(adaptableObject);
				}
			};
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ThingWithId.class };
	}

}