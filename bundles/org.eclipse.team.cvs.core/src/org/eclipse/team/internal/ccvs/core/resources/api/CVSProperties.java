package org.eclipse.team.internal.ccvs.core.resources.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * The CVSProperties are a way to store Properties about a file
 * or a folder. The CVSProperties have a predefined and not
 * changing set of keys (you can not set on a key that is not 
 * predefined).<br>
 * This class is for overloading, also it could be instanceated.
 */
public class CVSProperties {
	
	private Map properties = new HashMap();
	
	/**
	 * @param supportedKeys the set of predefined keys
	 * 			to be set by a subclass
	 */
	public CVSProperties(String[] supportedKeys) {
		for (int i=0; i<supportedKeys.length; i++) {
			properties.put(supportedKeys[i],null);
		}
	}
	
	/**
	 * Get the property with the name key.
	 * @return null if key is not in supportedKeys
	 */
	public String getProperty(String key) {
		return (String) properties.get(key);
	}

	/**
	 * Set the value of the property key.
	 * @throws IllegalArgumentException if key is not in 
	 * 			supportedKeys
	 */
	public String putProperty(String key, String value) throws 
			IllegalArgumentException {
		
		Assert.isLegal(properties.containsKey(key),
					   Policy.bind("CVSProperties.IllegalKey"));

		return (String) properties.put(key,value);
	}

	/**
	 * Gives all supported keys as a Set.
	 */
	public Set keySet() {
		return properties.keySet();
	}

	/**
	 * CVSProperties are equal, when keys and values are equal.
	 */
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof CVSProperties)) {
			return false;
		} else {
			return properties.equals(((CVSProperties)o).properties);
		}
	}
}

