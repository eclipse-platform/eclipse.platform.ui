package org.eclipse.urischeme.internal.registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistryWriterMock implements IRegistryWriter {

	List<String> addedSchemes = new ArrayList<String>();
	List<String> removedSchemes = new ArrayList<String>();
	Map<String, String> schemeToHandlerPath = new HashMap<String, String>();

	@Override
	public void addScheme(String scheme) throws IllegalArgumentException {
		addedSchemes.add(scheme);
	}

	@Override
	public void removeScheme(String scheme) throws IllegalArgumentException {
		removedSchemes.add(scheme);
	}

	@Override
	public String getRegisteredHandlerPath(String scheme) {
		return schemeToHandlerPath.get(scheme);
	}

}
