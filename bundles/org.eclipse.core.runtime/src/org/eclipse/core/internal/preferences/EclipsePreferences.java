/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.preferences;

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Represents a node in the Eclipse preference node hierarchy. This class
 * is used as a default implementation/super class for those nodes which
 * belong to scopes which are contributed by the Platform.
 * 
 * Implementation notes:
 * 
 *  - For thread safety, we always synchronize on the node object when writing
 * the children or properties fields.  Must ensure we don't synchronize when calling
 * client code such as listeners.
 * 
 * @since 3.0
 */
public class EclipsePreferences implements IEclipsePreferences, IScope {

	public static final String DEFAULT_PREFERENCES_DIRNAME = ".settings"; //$NON-NLS-1$
	public static final String PREFS_FILE_EXTENSION = "prefs"; //$NON-NLS-1$
	protected static final IEclipsePreferences[] EMPTY_NODE_ARRAY = new IEclipsePreferences[0];
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String FALSE = "false"; //$NON-NLS-1$
	private static final String TRUE = "true"; //$NON-NLS-1$
	protected static final String VERSION_KEY = "eclipse.preferences.version"; //$NON-NLS-1$
	protected static final String VERSION_VALUE = "1"; //$NON-NLS-1$

	private String cachedPath;
	protected Map children;
	protected boolean dirty = false;
	protected boolean loading = false;
	protected final String name;
	protected ListenerList nodeListeners;
	protected final IEclipsePreferences parent;
	protected ListenerList preferenceListeners;
	protected Properties properties;
	protected boolean removed = false;

	public EclipsePreferences() {
		this(null, null);
	}

	protected EclipsePreferences(IEclipsePreferences parent, String name) {
		super();
		this.parent = parent;
		this.name = name;
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#absolutePath()
	 */
	public String absolutePath() {
		if (cachedPath == null)
			cachedPath = parent == null ? Path.ROOT.toString() : new Path(parent.absolutePath()).append(name()).toString();
		return cachedPath;
	}

	public void accept(IPreferenceNodeVisitor visitor) throws BackingStoreException {
		if (!visitor.visit(this))
			return;
		IEclipsePreferences[] toVisit = getChildren();
		for (int i = 0; i < toVisit.length; i++)
			toVisit[i].accept(visitor);
	}

	protected synchronized void addChild(String childName, IEclipsePreferences child) {
		//Thread safety: synchronize method to protect modification of children field
		if (children == null)
			children = Collections.synchronizedMap(new HashMap());
		children.put(childName, child == null ? (Object) childName : child);
	}

	/*
	 * @see org.eclipse.core.runtime.IEclipsePreferences#addNodeChangeListener(org.eclipse.core.runtime.IEclipsePreferences.INodeChangeListener)
	 */
	public void addNodeChangeListener(INodeChangeListener listener) {
		checkRemoved();
		if (nodeListeners == null)
			nodeListeners = new ListenerList();
		nodeListeners.add(listener);
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Added preference node change listener: " + listener + " to: " + absolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * @see org.eclipse.core.runtime.IEclipsePreferences#addPreferenceChangeListener(org.eclipse.core.runtime.IEclipsePreferences.IPreferenceChangeListener)
	 */
	public void addPreferenceChangeListener(IPreferenceChangeListener listener) {
		checkRemoved();
		if (preferenceListeners == null)
			preferenceListeners = new ListenerList();
		preferenceListeners.add(listener);
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Added preference property change listener: " + listener + " to: " + absolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private IEclipsePreferences calculateRoot() {
		IEclipsePreferences result = this;
		while (result.parent() != null)
			result = (IEclipsePreferences) result.parent();
		return result;
	}

	/*
	 * Convenience method for throwing an exception when methods
	 * are called on a removed node.
	 */
	protected void checkRemoved() {
		if (removed) {
			String message = Policy.bind("preferences.removedNode", name); //$NON-NLS-1$
			throw new IllegalStateException(message);
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#childrenNames()
	 */
	public String[] childrenNames() {
		// illegal state if this node has been removed
		checkRemoved();
		return internalChildNames();
	}

	protected String[] internalChildNames() {
		Map temp = children;
		if (temp == null || temp.size() == 0)
			return EMPTY_STRING_ARRAY;
		return (String[]) temp.keySet().toArray(EMPTY_STRING_ARRAY);
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#clear()
	 */
	public void clear() {
		// illegal state if this node has been removed
		checkRemoved();
		Properties temp = properties;
		if (temp == null)
			return;
		// call each one separately (instead of Properties.clear) so
		// clients get change notification
		String[] keys = (String[]) temp.keySet().toArray(EMPTY_STRING_ARRAY);
		for (int i = 0; i < keys.length; i++)
			remove(keys[i]);
		//Thread safety: protect against concurrent modification
		synchronized (this) {
			properties = null;
		}
		makeDirty();
	}

	protected String[] computeChildren(IPath root) {
		if (root == null)
			return EMPTY_STRING_ARRAY;
		IPath dir = root.append(DEFAULT_PREFERENCES_DIRNAME);
		File file = dir.toFile();
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File directory, String child) {
				if (new File(directory, child).isDirectory())
					return false;
				return child.endsWith('.' + PREFS_FILE_EXTENSION);
			}
		};
		String[] result = file.list(filter);
		return result == null ? EMPTY_STRING_ARRAY : result;
	}

	protected IPath computeLocation(IPath root, String qualifier) {
		return root == null ? null : root.append(DEFAULT_PREFERENCES_DIRNAME).append(qualifier).addFileExtension(PREFS_FILE_EXTENSION);
	}

	protected void convertFromProperties(Properties table) {
		if (!VERSION_VALUE.equals(table.get(VERSION_KEY))) {
			legacyConvertFromProperties(table);
			return;
		}
		table.remove(VERSION_KEY);
		for (Iterator i = table.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = table.getProperty(key);
			if (value != null) {
				IPath childPath = new Path(key);
				if (!childPath.isAbsolute() && childPath.segmentCount() > 0) {
					key = childPath.lastSegment();
					IPath child = childPath.removeLastSegments(1);
					//use internal methods to avoid notifying listeners
					EclipsePreferences childNode = (EclipsePreferences) internalNode(child, false, null);
					if (InternalPlatform.DEBUG_PREFERENCES)
						Policy.debug("Setting preference: " + childNode.absolutePath() + '/' + key + '=' + value); //$NON-NLS-1$
					childNode.internalPut(key, value);
					childNode.makeDirty();
				} else {
					if (InternalPlatform.DEBUG_PREFERENCES)
						Policy.debug("Ignoring value: " + value + " for key: " + childPath + " for node: " + absolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
	}

	/* 
	 * Helper method to convert this node to a Properties file suitable
	 * for persistence.
	 */
	protected Properties convertToProperties(Properties result, IPath prefix) throws BackingStoreException {
		// add the key/value pairs from this node
		Properties temp = properties;
		if (temp != null) {
			synchronized (temp) {
				String[] keys = (String[]) temp.keySet().toArray(EMPTY_STRING_ARRAY);
				for (int i = 0; i < keys.length; i++) {
					String value = temp.getProperty(keys[i], null);
					if (value != null)
						result.put(prefix.append(keys[i]).toString(), value);
				}
			}
		}
		// recursively add the child information
		IEclipsePreferences[] childNodes = getChildren();
		for (int i = 0; i < childNodes.length; i++) {
			EclipsePreferences child = (EclipsePreferences) childNodes[i];
			child.convertToProperties(result, prefix.append(child.name()));
		}
		return result;
	}

	/*
	 * @see org.eclipse.core.runtime.preferences.IScope#create(org.eclipse.core.runtime.preferences.IEclipsePreferences)
	 */
	public IEclipsePreferences create(IEclipsePreferences nodeParent, String nodeName) {
		return create(nodeParent, nodeName, null);
	}

	public IEclipsePreferences create(IEclipsePreferences nodeParent, String nodeName, Plugin context) {
		EclipsePreferences result = internalCreate(nodeParent, nodeName, context);
		((EclipsePreferences) nodeParent).addChild(nodeName, result);
		IEclipsePreferences loadLevel = result.getLoadLevel();

		// if this node or a parent node is not the load level then return
		if (loadLevel == null)
			return result;

		// if the result node is not a load level, then a child must be
		if (result != loadLevel)
			return result;

		// the result node is a load level
		if (isAlreadyLoaded(result))
			return result;
		if (loading)
			return result;
		try {
			loading = true;
			result.loadLegacy();
			result.load();
			result.loaded();
			result.flush();
		} catch (BackingStoreException e) {
			IPath location = result.getLocation();
			String message = Policy.bind("preferences.loadException", location == null ? "" : location.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e);
			InternalPlatform.getDefault().log(status);
		} finally {
			loading = false;
		}
		return result;
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#flush()
	 */
	public void flush() throws BackingStoreException {
		// illegal state if this node has been removed
		checkRemoved();

		IEclipsePreferences loadLevel = getLoadLevel();

		// if this node or a parent is not the load level, then flush the children
		if (loadLevel == null) {
			String[] childrenNames = childrenNames();
			for (int i = 0; i < childrenNames.length; i++)
				node(childrenNames[i]).flush();
			return;
		}

		// a parent is the load level for this node
		if (this != loadLevel) {
			loadLevel.flush();
			return;
		}

		// this node is a load level
		// any work to do?
		if (!dirty)
			return;
		//remove dirty bit before saving, to ensure that concurrent 
		//changes during save mark the store as dirty
		dirty = false;
		try {
			save();
		} catch (BackingStoreException e) {
			//mark it dirty again because the save failed
			dirty = true;
			throw e;
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#get(java.lang.String, java.lang.String)
	 */
	public String get(String key, String defaultValue) {
		String value = internalGet(key);
		return value == null ? defaultValue : value;
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#getBoolean(java.lang.String, boolean)
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		String value = internalGet(key);
		return value == null ? defaultValue : TRUE.equalsIgnoreCase(value);
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#getByteArray(java.lang.String, byte[])
	 */
	public byte[] getByteArray(String key, byte[] defaultValue) {
		String value = internalGet(key);
		return value == null ? defaultValue : Base64.decode(value.getBytes());
	}

	/**
	 * Thread safe way to obtain a child for a given key. Returns the child
	 * that matches the given key, or null if there is no matching child
	 */
	protected synchronized IEclipsePreferences getChild(String key, Plugin context) {
		if (children == null)
			return null;
		Object value = children.get(key);
		if (value == null)
			return null;
		if (value instanceof IEclipsePreferences)
			return (IEclipsePreferences) value;
		value = create(this, key, context);
		addChild(key, (IEclipsePreferences) value);
		return (IEclipsePreferences) value;
	}

	/**
	 * Thread safe way to obtain all children of this node. Never returns null.
	 */
	protected synchronized IEclipsePreferences[] getChildren() {
		ArrayList result = new ArrayList();
		String[] names = internalChildNames();
		for (int i = 0; i < names.length; i++)
			result.add(getChild(names[i], null));
		return (IEclipsePreferences[]) result.toArray(EMPTY_NODE_ARRAY);
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#getDouble(java.lang.String, double)
	 */
	public double getDouble(String key, double defaultValue) {
		String value = internalGet(key);
		double result = defaultValue;
		if (value != null)
			try {
				result = Double.parseDouble(value);
			} catch (NumberFormatException e) {
				// use default
			}
		return result;
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#getFloat(java.lang.String, float)
	 */
	public float getFloat(String key, float defaultValue) {
		String value = internalGet(key);
		float result = defaultValue;
		if (value != null)
			try {
				result = Float.parseFloat(value);
			} catch (NumberFormatException e) {
				// use default
			}
		return result;
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#getInt(java.lang.String, int)
	 */
	public int getInt(String key, int defaultValue) {
		String value = internalGet(key);
		int result = defaultValue;
		if (value != null)
			try {
				result = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// use default
			}
		return result;
	}

	protected IEclipsePreferences getLoadLevel() {
		return null;
	}

	/*
	 * Subclasses to over-ride
	 */
	protected IPath getLocation() {
		return null;
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#getLong(java.lang.String, long)
	 */
	public long getLong(String key, long defaultValue) {
		String value = internalGet(key);
		long result = defaultValue;
		if (value != null)
			try {
				result = Long.parseLong(value);
			} catch (NumberFormatException e) {
				// use default
			}
		return result;
	}

	protected EclipsePreferences internalCreate(IEclipsePreferences nodeParent, String nodeName, Plugin context) {
		return new EclipsePreferences(nodeParent, nodeName);
	}

	/**
	 * Returns the existing value at the given key, or null if
	 * no such value exists.
	 */
	protected String internalGet(String key) {
		// throw NPE if key is null
		if (key == null)
			throw new NullPointerException();
		// illegal state if this node has been removed
		checkRemoved();
		//Thread safety: copy field reference in case of concurrent modification
		Properties temp = properties;
		if (temp == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Getting preference value: " + absolutePath() + '/' + key + "->null"); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		String result = temp.getProperty(key);
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Getting preference value: " + absolutePath() + '/' + key + "->" + result); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}

	/**
	 * Implements the node(IPath) method, and optionally notifies listeners.
	 */
	protected IEclipsePreferences internalNode(IPath path, boolean notify, Plugin context) {
		// use the root relative to this node instead of the global root
		// in case we have a different hierarchy. (e.g. export)
		if (path.isAbsolute())
			return calculateRoot().node(path.makeRelative());

		// TODO: handle relative paths correctly (.. refs)

		// illegal state if this node has been removed
		checkRemoved();

		// short circuit this node
		if (path.isEmpty())
			return this;

		String key = path.segment(0);
		boolean added = false;
		IEclipsePreferences child;
		synchronized (this) {
			child = getChild(key, context);
			if (child == null) {
				child = create(this, key, context);
				added = true;
			}
		}
		// notify listeners if a child was added
		if (added && notify)
			nodeAdded(child);
		return child.node(path.removeFirstSegments(1));
	}

	/**
	 * Stores the given (key,value) pair, performing lazy initialization of the
	 * properties field if necessary. Returns the old value for the given key,
	 * or null if no value existed.
	 */
	protected synchronized String internalPut(String key, String newValue) {
		// illegal state if this node has been removed
		checkRemoved();
		if (properties == null)
			properties = new Properties();
		String oldValue = properties.getProperty(key);
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Setting preference: " + absolutePath() + '/' + key + '=' + newValue); //$NON-NLS-1$
		properties.setProperty(key, newValue);
		return oldValue;
	}

	private void internalRemove(String key, Object oldValue) {
		boolean wasRemoved = false;
		//Thread safety: synchronize when modifying the properties field
		synchronized (this) {
			if (properties == null)
				return;
			wasRemoved = properties.remove(key) != null;
			if (properties.size() == 0)
				properties = null;
			if (wasRemoved)
				makeDirty();
		}
		if (wasRemoved)
			preferenceChanged(key, oldValue, null);
	}

	/*
	 * Subclasses to over-ride.
	 */
	protected boolean isAlreadyLoaded(IEclipsePreferences node) {
		return true;
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#keys()
	 */
	public String[] keys() {
		// illegal state if this node has been removed
		checkRemoved();
		Properties temp = properties;
		if (temp == null || temp.size() == 0)
			return EMPTY_STRING_ARRAY;
		return (String[]) temp.keySet().toArray(EMPTY_STRING_ARRAY);
	}

	private void legacyConvertFromProperties(Properties table) {
		IPath fullPath = new Path(absolutePath());
		for (Iterator i = table.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = table.getProperty(key);
			if (value != null) {
				IPath childPath = new Path(key);
				if (childPath.segmentCount() > 0) {
					key = childPath.lastSegment();
					IPath child = childPath.removeLastSegments(1);
					// calculate the node relative to this node
					if (fullPath.isPrefixOf(childPath)) {
						child = child.removeFirstSegments(fullPath.segmentCount());
						//use internal methods to avoid notifying listeners
						EclipsePreferences childNode = (EclipsePreferences) internalNode(child, false, null);
						if (InternalPlatform.DEBUG_PREFERENCES)
							Policy.debug("Setting preference: " + childNode.absolutePath() + '/' + key + '=' + value); //$NON-NLS-1$
						childNode.internalPut(key, value);
						childNode.makeDirty();
					} else {
						if (InternalPlatform.DEBUG_PREFERENCES)
							Policy.debug("Ignoring value: " + value + " for key: " + childPath + " for node: " + fullPath); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			}
		}
	}

	protected void load() throws BackingStoreException {
		load(getLocation());
	}

	protected void load(IPath location) throws BackingStoreException {
		if (location == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Unable to determine location of preference file for node: " + absolutePath()); //$NON-NLS-1$
			return;
		}
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Loading preferences from file: " + location); //$NON-NLS-1$
		InputStream input = null;
		Properties fromDisk = new Properties();
		try {
			input = new BufferedInputStream(new FileInputStream(location.toFile()));
			fromDisk.load(input);
		} catch (FileNotFoundException e) {
			// file doesn't exist but that's ok.
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Preference file does not exist: " + location); //$NON-NLS-1$
			return;
		} catch (IOException e) {
			String message = Policy.bind("preferences.loadException", location.toString()); //$NON-NLS-1$
			log(new Status(IStatus.INFO, Platform.PI_RUNTIME, IStatus.INFO, message, e));
			throw new BackingStoreException(message);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
		}
		convertFromProperties(fromDisk);
	}

	protected void loaded() {
		// do nothing
	}

	protected void loadLegacy() {
		// sub-classes to over-ride if necessary
	}

	protected void log(IStatus status) {
		InternalPlatform.getDefault().log(status);
	}

	protected void makeDirty() {
		EclipsePreferences node = this;
		while (node != null && !node.removed) {
			node.dirty = true;
			node = (EclipsePreferences) node.parent();
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#name()
	 */
	public String name() {
		return name;
	}

	/*
	 * @see org.eclipse.core.runtime.IEclipsePreferences#node(org.eclipse.core.runtime.IPath)
	 */
	public IEclipsePreferences node(IPath path) {
		return internalNode(path, true, null);
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#node(java.lang.String)
	 */
	public Preferences node(String pathName) {
		return internalNode(new Path(pathName), true, null);
	}

	protected void nodeAdded(IEclipsePreferences child) {
		if (nodeListeners == null)
			return;
		Object[] listeners = nodeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final NodeChangeEvent event = new NodeChangeEvent(this, child);
			final INodeChangeListener listener = (INodeChangeListener) listeners[i];
			ISafeRunnable job = new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// already logged in Platform#run()
				}

				public void run() throws Exception {
					listener.added(event);
				}
			};
			Platform.run(job);
		}
	}

	/*
	 * @see org.eclipse.core.runtime.IEclipsePreferences#nodeExists(org.eclipse.core.runtime.IPath)
	 */
	public boolean nodeExists(IPath path) throws BackingStoreException {
		// use the root relative to this node instead of the global root
		// in case we have a different hierarchy. (e.g. export)
		if (path.isAbsolute())
			return calculateRoot().nodeExists(path.makeRelative());

		// TODO: handle relative paths correctly (.. refs)

		// short circuit for checking this node
		if (path.isEmpty())
			return !removed;
		// illegal state if this node has been removed
		checkRemoved();
		IEclipsePreferences child = getChild(path.segment(0), null);
		if (child == null)
			return false;
		return child.nodeExists(path.removeFirstSegments(1));
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#nodeExists(java.lang.String)
	 */
	public boolean nodeExists(String pathName) throws BackingStoreException {
		return nodeExists(new Path(pathName));
	}

	protected void nodeRemoved(IEclipsePreferences child) {
		if (nodeListeners == null)
			return;
		final Object[] listeners = nodeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final NodeChangeEvent event = new NodeChangeEvent(this, child);
			final INodeChangeListener listener = (INodeChangeListener) listeners[i];
			ISafeRunnable job = new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// already being logged in Platform#run()
				}

				public void run() throws Exception {
					listener.removed(event);
				}
			};
			Platform.run(job);
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#parent()
	 */
	public Preferences parent() {
		// illegal state if this node has been removed
		checkRemoved();
		return parent;
	}

	/*
	 * Convenience method for notifying preference change listeners.
	 */
	protected void preferenceChanged(String key, Object oldValue, Object newValue) {
		if (preferenceListeners == null)
			return;
		Object[] listeners = preferenceListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final PreferenceChangeEvent event = new PreferenceChangeEvent(this, key, oldValue, newValue);
			final IPreferenceChangeListener listener = (IPreferenceChangeListener) listeners[i];
			ISafeRunnable job = new ISafeRunnable() {
				public void handleException(Throwable exception) {
					// already logged in Platform#run()
				}

				public void run() throws Exception {
					listener.preferenceChange(event);
				}
			};
			Platform.run(job);
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#put(java.lang.String, java.lang.String)
	 */
	public void put(String key, String newValue) {
		String oldValue = internalPut(key, newValue);
		if (!newValue.equals(oldValue)) {
			makeDirty();
			preferenceChanged(key, oldValue, newValue);
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#putBoolean(java.lang.String, boolean)
	 */
	public void putBoolean(String key, boolean value) {
		String newValue = value ? TRUE : FALSE;
		String oldValue = internalPut(key, newValue);
		if (!newValue.equals(oldValue)) {
			makeDirty();
			preferenceChanged(key, oldValue == null ? null : new Boolean(oldValue), value ? Boolean.TRUE : Boolean.FALSE);
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#putByteArray(java.lang.String, byte[])
	 */
	public void putByteArray(String key, byte[] value) {
		String newValue = new String(Base64.encode(value));
		String oldValue = internalPut(key, newValue);
		if (!newValue.equals(oldValue)) {
			makeDirty();
			preferenceChanged(key, oldValue == null ? null : Base64.decode(oldValue.getBytes()), value);
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#putDouble(java.lang.String, double)
	 */
	public void putDouble(String key, double value) {
		String newValue = Double.toString(value);
		Object oldValue = internalPut(key, newValue);
		if (!newValue.equals(oldValue)) {
			makeDirty();
			if (oldValue != null)
				try {
					oldValue = new Double((String) oldValue);
				} catch (NumberFormatException e) {
					// ignore and let oldValue be a String
				}
			preferenceChanged(key, oldValue, new Double(value));
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#putFloat(java.lang.String, float)
	 */
	public void putFloat(String key, float value) {
		String newValue = Float.toString(value);
		Object oldValue = internalPut(key, newValue);
		if (!newValue.equals(oldValue)) {
			makeDirty();
			if (oldValue != null)
				try {
					oldValue = new Float((String) oldValue);
				} catch (NumberFormatException e) {
					// ignore and let oldValue be a String
				}
			preferenceChanged(key, oldValue, new Float(value));
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#putInt(java.lang.String, int)
	 */
	public void putInt(String key, int value) {
		String newValue = Integer.toString(value);
		Object oldValue = internalPut(key, newValue);
		if (!newValue.equals(oldValue)) {
			makeDirty();
			if (oldValue != null)
				try {
					oldValue = new Integer((String) oldValue);
				} catch (NumberFormatException e) {
					// ignore and let oldValue be a String
				}
			preferenceChanged(key, oldValue, new Integer(value));
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#putLong(java.lang.String, long)
	 */
	public void putLong(String key, long value) {
		String newValue = Long.toString(value);
		Object oldValue = internalPut(key, newValue);
		if (!newValue.equals(oldValue)) {
			makeDirty();
			if (oldValue != null)
				try {
					oldValue = new Long((String) oldValue);
				} catch (NumberFormatException e) {
					// ignore and let oldValue be a String
				}
			preferenceChanged(key, oldValue, new Long(value));
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#remove(java.lang.String)
	 */
	public void remove(String key) {
		String oldValue = internalGet(key);
		if (oldValue != null)
			internalRemove(key, oldValue);
	}

	/*
	 * Added so the backwards compatibility layer (PreferenceForwarder)
	 * gets preference change events of the correct types.
	 */
	void removeBoolean(String key) {
		String oldValue = internalGet(key);
		if (oldValue != null)
			internalRemove(key, Boolean.valueOf(oldValue));
	}

	/*
	 * Added so the backwards compatibility layer (PreferenceForwarder)
	 * gets preference change events of the correct types.
	 */
	void removeDouble(String key) {
		Object oldValue = internalGet(key);
		if (oldValue != null) {
			try {
				oldValue = Double.valueOf((String) oldValue);
			} catch (NumberFormatException e) {
				// ignore - oldValue will be null
			}
			internalRemove(key, oldValue);
		}
	}

	/*
	 * Added so the backwards compatibility layer (PreferenceForwarder)
	 * gets preference change events of the correct types.
	 */
	void removeFloat(String key) {
		Object oldValue = internalGet(key);
		if (oldValue != null) {
			try {
				oldValue = Float.valueOf((String) oldValue);
			} catch (NumberFormatException e) {
				// ignore - oldValue will be null
			}
			internalRemove(key, oldValue);
		}
	}

	/*
	 * Added so the backwards compatibility layer (PreferenceForwarder)
	 * gets preference change events of the correct types.
	 */
	void removeInt(String key) {
		Object oldValue = internalGet(key);
		if (oldValue != null) {
			try {
				oldValue = Integer.valueOf((String) oldValue);
			} catch (NumberFormatException e) {
				// ignore - oldValue will be null
			}
			internalRemove(key, oldValue);
		}
	}

	/*
	 * Added so the backwards compatibility layer (PreferenceForwarder)
	 * gets preference change events of the correct types.
	 */
	void removeLong(String key) {
		Object oldValue = internalGet(key);
		if (oldValue != null) {
			try {
				oldValue = Long.valueOf((String) oldValue);
			} catch (NumberFormatException e) {
				// ignore - oldValue will be null
			}
			internalRemove(key, oldValue);
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#removeNode()
	 */
	public void removeNode() throws BackingStoreException {
		// illegal state if this node has been removed
		checkRemoved();
		// clear all the property values. do it "the long way" so 
		// everyone gets notification
		String[] keys = keys();
		for (int i = 0; i < keys.length; i++)
			remove(keys[i]);
		// don't remove the scope root from the parent but
		// remove all its children
		if (!(parent instanceof RootPreferences)) {
			// remove the node from the parent's collection and notify listeners
			if (parent instanceof EclipsePreferences) {
				removed = true;
				((EclipsePreferences) parent).removeNode(this);
			} else {
				String message = Policy.bind("preferences.invalidParentClass", absolutePath(), parent.getClass().getName()); //$NON-NLS-1$
				throw new BackingStoreException(message);
			}
		}
		IEclipsePreferences[] childNodes = getChildren();
		for (int i = 0; i < childNodes.length; i++)
			try {
				childNodes[i].removeNode();
			} catch (IllegalStateException e) {
				// ignore since we only get this exception if we have already
				// been removed. no work to do.
			}
	}

	/*
	 * Remove the child from the collection and notify the listeners if something
	 * was actually removed.
	 */
	protected void removeNode(IEclipsePreferences child) {
		boolean wasRemoved = false;
		synchronized (this) {
			if (children != null) {
				wasRemoved = children.remove(child.name()) != null;
				if (wasRemoved)
					makeDirty();
				if (children.isEmpty())
					children = null;
			}
		}
		if (wasRemoved)
			nodeRemoved(child);
	}

	/*
	 * @see org.eclipse.core.runtime.IEclipsePreferences#removeNodeChangeListener(org.eclipse.core.runtime.IEclipsePreferences.removeNodeChangeListener)
	 */
	public void removeNodeChangeListener(INodeChangeListener listener) {
		checkRemoved();
		if (nodeListeners == null)
			return;
		nodeListeners.remove(listener);
		if (nodeListeners.size() == 0)
			nodeListeners = null;
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Removed preference node change listener: " + listener + " from: " + absolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * @see org.eclipse.core.runtime.IEclipsePreferences#removePreferenceChangeListener(org.eclipse.core.runtime.IEclipsePreferences.IPreferenceChangeListener)
	 */
	public void removePreferenceChangeListener(IPreferenceChangeListener listener) {
		checkRemoved();
		if (preferenceListeners == null)
			return;
		preferenceListeners.remove(listener);
		if (preferenceListeners.size() == 0)
			preferenceListeners = null;
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Removed preference property change listener: " + listener + " from: " + absolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void save() throws BackingStoreException {
		save(getLocation());
	}

	protected void save(IPath location) throws BackingStoreException {
		if (location == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Unable to determine location of preference file for node: " + absolutePath()); //$NON-NLS-1$
			return;
		}
		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Saving preferences to file: " + location); //$NON-NLS-1$
		Properties table = convertToProperties(new Properties(), Path.EMPTY);
		if (table.isEmpty()) {
			// nothing to save. delete existing file if one exists.
			if (location.toFile().exists() && !location.toFile().delete()) {
				String message = Policy.bind("preferences.failedDelete", location.toString()); //$NON-NLS-1$
				log(new Status(IStatus.WARNING, Platform.PI_RUNTIME, IStatus.WARNING, message, null));
			}
			return;
		}
		table.put(VERSION_KEY, VERSION_VALUE);
		OutputStream output = null;
		try {
			// create the parent dirs if they don't exist
			File parentFile = location.toFile().getParentFile();
			if (parentFile == null)
				return;
			parentFile.mkdirs();
			// set append to be false so we overwrite current settings.
			output = new BufferedOutputStream(new FileOutputStream(location.toFile(), false));
			table.store(output, null);
		} catch (IOException e) {
			String message = Policy.bind("preferences.saveException", location.toString()); //$NON-NLS-1$
			log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e));
			throw new BackingStoreException(message);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					// ignore
				}
		}
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#sync()
	 */

	public void sync() throws BackingStoreException {
		// illegal state if this node has been removed
		checkRemoved();
		IEclipsePreferences node = getLoadLevel();
		if (node == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Preference node is not a load root: " + absolutePath()); //$NON-NLS-1$
			return;
		}
		if (node instanceof EclipsePreferences) {
			((EclipsePreferences) node).load();
			node.flush();
		}
	}

	public String toDeepDebugString() {
		final StringBuffer buffer = new StringBuffer();
		IPreferenceNodeVisitor visitor = new IPreferenceNodeVisitor() {
			public boolean visit(IEclipsePreferences node) throws BackingStoreException {
				buffer.append(node);
				buffer.append('\n');
				String[] keys = node.keys();
				for (int i = 0; i < keys.length; i++) {
					buffer.append(node.absolutePath());
					buffer.append(IPath.SEPARATOR);
					buffer.append(keys[i]);
					buffer.append('=');
					buffer.append(node.get(keys[i], "*default*")); //$NON-NLS-1$
					buffer.append('\n');
				}
				return true;
			}
		};
		try {
			accept(visitor);
		} catch (BackingStoreException e) {
			System.out.println("Exception while calling #toDeepDebugString()"); //$NON-NLS-1$
			e.printStackTrace();
		}
		return buffer.toString();
	}

	public String toString() {
		return absolutePath();
	}
}