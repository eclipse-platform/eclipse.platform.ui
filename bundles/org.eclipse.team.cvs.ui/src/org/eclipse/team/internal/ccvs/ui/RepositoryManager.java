package org.eclipse.team.internal.ccvs.ui;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.IRemoteRoot;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.model.Tag;

/**
 * This class is repsible for maintaining the UI's list of known repositories,
 * and a list of known tags within each of those repositories.
 */
public class RepositoryManager {
	Hashtable repositories = new Hashtable();
	Hashtable tags = new Hashtable();
	
	/**
	 * Answer an array of all known remote roots.
	 */
	public IRemoteRoot[] getKnownRoots() {
		return (IRemoteRoot[])repositories.values().toArray(new IRemoteRoot[0]);
	}
	/**
	 * Answer the root corresponding with the given properties.
	 * If the root is in the list of known roots, it is returned.
	 * If it is not in the list of known roots, it is created and
	 * added.
	 */
	public IRemoteRoot getRoot(Properties properties) {
		StringBuffer keyBuffer = new StringBuffer();
		keyBuffer.append(":");
		keyBuffer.append(properties.getProperty("connection"));
		keyBuffer.append(":");
		keyBuffer.append(properties.getProperty("user"));
		keyBuffer.append("@");
		keyBuffer.append(properties.getProperty("host"));
		String port = properties.getProperty("port");
		if (port != null) {
			keyBuffer.append("#");
			keyBuffer.append(port);
		}
		keyBuffer.append(":");
		keyBuffer.append(properties.getProperty("root"));
		String key = keyBuffer.toString();
		
		IRemoteRoot result = (IRemoteRoot)repositories.get(key);
		if (result != null) {
			return result;
		}
		try {
			result = CVSTeamProvider.getRemoteRoot(properties);
			repositories.put(result.getName(), result);
			addTag(result, new Tag("HEAD", result));
		} catch (TeamException e) {
			CVSUIPlugin.log(e.getStatus());
			return null;
		}
		return result;
	}
	/**
	 * Get the list of known tags for a given remote root.
	 */
	public Tag[] getKnownTags(IRemoteRoot root) {
		Set set = (Set)tags.get(root);
		if (set == null) return new Tag[0];
		return (Tag[])set.toArray(new Tag[0]);
	}
	/**
	 * Add the given tag to the list of known tags for the given
	 * remote root.
	 */
	public void addTag(IRemoteRoot root, Tag tag) {
		Set set = (Set)tags.get(root);
		if (set == null) {
			set = new HashSet();
			tags.put(root, set);
		}
		set.add(tag);
	}
	/**
	 * Remove the given tag from the list of known tags for the
	 * given remote root.
	 */
	public void removeTag(IRemoteRoot root, Tag tag) {
		Set set = (Set)tags.get(root);
		if (set == null) return;
		set.remove(tag);
	}
	/**
	 * Remove the given root from the list of known remote roots.
	 */
	public void removeRoot(IRemoteRoot root) {
		repositories.remove(root.getName());
	}
}
