package org.eclipse.team.internal.ccvs.ui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.IRemoteRoot;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.model.Tag;

/**
 * This class is repsible for maintaining the UI's list of known repositories,
 * and a list of known tags within each of those repositories.
 */
public class RepositoryManager {
	private static final String STATE_FILE = ".repositoryManagerState";
	
	Hashtable repositories = new Hashtable();
	Hashtable tags = new Hashtable();
	
	List listeners = new ArrayList();
	
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
			Tag tag = new Tag("HEAD", result);
			addTag(result, tag);
			Iterator it = listeners.iterator();
			while (it.hasNext()) {
				IRepositoryListener listener = (IRepositoryListener)it.next();
				listener.repositoryAdded(result);
				listener.tagAdded(tag, result);
			}
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
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.tagAdded(tag, root);
		}
	}
	/**
	 * Remove the given tag from the list of known tags for the
	 * given remote root.
	 */
	public void removeTag(IRemoteRoot root, Tag tag) {
		Set set = (Set)tags.get(root);
		if (set == null) return;
		set.remove(tag);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.tagRemoved(tag, root);
		}
	}
	/**
	 * Remove the given root from the list of known remote roots.
	 * Also removed the tags defined for this root.
	 */
	public void removeRoot(IRemoteRoot root) {
		Tag[] tags = getKnownTags(root);
		Object o = repositories.remove(root.getName());
		if (o == null) return;
		this.tags.remove(root);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			for (int i = 0; i < tags.length; i++) {
				listener.tagRemoved(tags[i], root);
			}
			listener.repositoryRemoved(root);
		}
	}
	
	public void startup() throws TeamException {
		loadState();
	}
	
	public void shutdown() throws TeamException {
		saveState();
	}
	
	private void loadState() throws TeamException {
		IPath pluginStateLocation = CVSUIPlugin.getPlugin().getStateLocation().append(STATE_FILE);
		File file = pluginStateLocation.toFile();
		if (file.exists()) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(file));
				readState(dis);
				dis.close();
			} catch (IOException e) {
				throw new TeamException(new Status(Status.ERROR, CVSUIPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.ioException"), e));
			}
		}
	}
	
	private void saveState() throws TeamException {
		IPath pluginStateLocation = CVSUIPlugin.getPlugin().getStateLocation();
		File tempFile = pluginStateLocation.append(STATE_FILE + ".tmp").toFile();
		File stateFile = pluginStateLocation.append(STATE_FILE).toFile();
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
			writeState(dos);
			dos.close();
			if (stateFile.exists()) {
				stateFile.delete();
			}
			boolean renamed = tempFile.renameTo(stateFile);
			if (!renamed) {
				throw new TeamException(new Status(Status.ERROR, CVSUIPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.rename", tempFile.getAbsolutePath()), null));
			}
		} catch (IOException e) {
			throw new TeamException(new Status(Status.ERROR, CVSUIPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.save",stateFile.getAbsolutePath()), e));
		}
	}
	private void writeState(DataOutputStream dos) throws IOException {
		// Write the repositories
		Collection repos = repositories.values();
		dos.writeInt(repos.size());
		Iterator it = repos.iterator();
		while (it.hasNext()) {
			IRemoteRoot root = (IRemoteRoot)it.next();
			dos.writeUTF(root.getConnectionMethod());
			dos.writeUTF(root.getUser());
			dos.writeUTF(root.getHost());
			dos.writeUTF("" + root.getPort());
			dos.writeUTF(root.getRepositoryPath());
			// Don't store HEAD, as it is automatically created when reading.
			Tag[] tags = getKnownTags(root);
			dos.writeInt(tags.length - 1);
			for (int i = 0; i < tags.length - 1; i++) {
				if (!tags[i].getTag().equals("HEAD")) {
					dos.writeUTF(tags[i].getTag());
				}
			}
		}
	}
	private void readState(DataInputStream dis) throws IOException {
		int repoSize = dis.readInt();
		for (int i = 0; i < repoSize; i++) {
			Properties properties = new Properties();
			properties.setProperty("connection", dis.readUTF());
			properties.setProperty("user", dis.readUTF());
			properties.setProperty("host", dis.readUTF());
			String port = dis.readUTF();
			if (!port.equals("" + IRemoteRoot.DEFAULT_PORT)) {
				properties.setProperty("port", port);
			}
			properties.setProperty("root", dis.readUTF());
			IRemoteRoot root = getRoot(properties);
			int tagsSize = dis.readInt();
			for (int j = 0; j < tagsSize; j++) {
				String tag = dis.readUTF();
				addTag(root, new Tag(tag, root));
			}
		}
	}
	
	public void addRepositoryListener(IRepositoryListener listener) {
		listeners.add(listener);
	}
	
	public void remoteRepositoryListener(IRepositoryListener listener) {
		listeners.remove(listener);
	}
}
