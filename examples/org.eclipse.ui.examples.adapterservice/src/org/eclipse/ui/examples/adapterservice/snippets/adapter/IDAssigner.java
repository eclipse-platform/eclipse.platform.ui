package org.eclipse.ui.examples.adapterservice.snippets.adapter;

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
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Object adaptableObject, Class adapterType) {
		if (adapterType.equals(ThingWithId.class)) {
			if (!assignedIds.containsKey(adaptableObject)) {
				String id = Integer.toString(currentId);
				currentId++;
				assignedIds.put(adaptableObject, id);
			}
			return new ThingWithId() {
				@Override
				public String getUniqueId() {
					return assignedIds.get(adaptableObject);
				}
			};
		}
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ThingWithId.class };
	}

}