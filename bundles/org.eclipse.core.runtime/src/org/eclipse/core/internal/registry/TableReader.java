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
package org.eclipse.core.internal.registry;

import java.io.*;
import java.util.HashMap;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

public class TableReader {
	//Markers in the cache 
	static final int NULL = 0;
	static final int OBJECT = 1;

	//The version of the cache
	static final int CACHE_VERSION = 1;

	//Informations representing the MAIN file
	static final String MAIN = ".mainData"; //$NON-NLS-1$
	static File mainDataFile;
	DataInputStream mainInput = null;
	//	int size;

	//Informations representing the EXTRA file
	static final String EXTRA = ".extraData"; //$NON-NLS-1$
	static File extraDataFile;
	DataInputStream extraInput = null;
	//	int sizeExtra;

	//The table file
	static final String TABLE = ".table"; //$NON-NLS-1$
	static File tableFile;

	//The namespace file
	static final String CONTRIBUTIONS = ".contributions"; //$NON-NLS-1$
	static File contributionsFile;

	//The orphan file
	static final String ORPHANS = ".orphans"; //$NON-NLS-1$
	static File orphansFile;

	//Status code
	private static final byte fileError = 0;
	private static final boolean DEBUG = false; //TODO need to change

	private boolean holdObjects = false;

	static void setMainDataFile(File main) {
		mainDataFile = main;
	}

	static void setExtraDataFile(File extra) {
		extraDataFile = extra;
	}

	static void setTableFile(File table) {
		tableFile = table;
	}

	static void setContributionsFile(File namespace) {
		contributionsFile = namespace;
	}

	static void setOrphansFile(File orphan) {
		orphansFile = orphan;
	}

	public TableReader() {
		openInputFile();
		openExtraFile();
	}

	private void openInputFile() {
		try {
			mainInput = new DataInputStream(new BufferedInputStream(new FileInputStream(mainDataFile)));
		} catch (FileNotFoundException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.unableToReadCache"), e)); //$NON-NLS-1$
		}
	}

	private void openExtraFile() {
		try {
			extraInput = new DataInputStream(new BufferedInputStream(new FileInputStream(extraDataFile)));
		} catch (FileNotFoundException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.unableToReadCache"), e)); //$NON-NLS-1$
		}
	}

	private void closeInputFile() {
		try {
			mainInput.close();
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.registryCacheReadProblems"), e)); //$NON-NLS-1$
		}

	}

	private void closeExtraFile() {
		try {
			extraInput.close();
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.registryCacheReadProblems"), e)); //$NON-NLS-1$
		}

	}

	public Object[] loadTables(long expectedTimestamp) {
		HashtableOfInt offsets;
		HashtableOfStringAndInt extensionPoints;

		DataInputStream tableInput = null;
		try {
			tableInput = new DataInputStream(new BufferedInputStream(new FileInputStream(tableFile)));
			if (!checkCacheValidity(tableInput, expectedTimestamp))
				return null;

			Integer nextId = new Integer(tableInput.readInt());
			offsets = new HashtableOfInt();
			offsets.load(tableInput);
			extensionPoints = new HashtableOfStringAndInt();
			extensionPoints.load(tableInput);
			return new Object[] {offsets, extensionPoints, nextId};
		} catch (IOException e) {
			if (tableInput != null)
				try {
					tableInput.close();
				} catch (IOException e1) {
					//Ignore
				}
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.registryCacheReadProblems"), e)); //$NON-NLS-1$
			return null;
		}

	}

	//	Check various aspect of the cache to see if it's valid 
	private boolean checkCacheValidity(DataInputStream in, long expectedTimestamp) {
		int version;
		try {
			version = in.readInt();
			if (version != CACHE_VERSION)
				return false;

			long installStamp = in.readLong();
			long registryStamp = in.readLong();
			long mainDataFileSize = in.readLong();
			long extraDataFileSize = in.readLong();
			long contributionsFileSize = in.readLong();
			long orphansFileSize = in.readLong();
			String osStamp = in.readUTF();
			String windowsStamp = in.readUTF();
			String localeStamp = in.readUTF();
			InternalPlatform info = InternalPlatform.getDefault();
			return ((expectedTimestamp == 0 || expectedTimestamp == registryStamp) && (installStamp == info.getStateTimeStamp()) && (osStamp.equals(info.getOS())) && (windowsStamp.equals(info.getWS())) && (localeStamp.equals(info.getNL())) && mainDataFileSize == mainDataFile.length() && extraDataFileSize == extraDataFile.length() && contributionsFileSize == contributionsFile.length() && orphansFileSize == orphansFile.length());
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.registryCacheInconsistent"), e)); //$NON-NLS-1$
			return false;
		}
	}

	public Object loadConfigurationElement(int offset) {
		try {
			goToInputFile(offset);
			return basicLoadConfigurationElement(mainInput, null);
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.regCacheIOExceptionReading"), e)); //$NON-NLS-1$
			if (DEBUG)
				InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, "Error reading a configuration element (" + offset + ") from the registry cache", e)); //$NON-NLS-1$//$NON-NLS-2$
			return null;
		} finally {
			closeInputFile();
			closeExtraFile();
		}
	}

	private ConfigurationElement basicLoadConfigurationElement(DataInputStream is, Bundle actualContributingBundle) throws IOException {
		int self = is.readInt();
		long contributingBundle = is.readLong();
		String name = readStringOrNull(is, false);
		int parentId = is.readInt();
		byte parentType = is.readByte();
		int misc = is.readInt();//this is set in second level CEs, to indicate where in the extra data file the children ces are
		String[] propertiesAndValue = readPropertiesAndValue(is);
		int[] children = readArray(is);
		if (actualContributingBundle == null)
			actualContributingBundle = getBundle(contributingBundle);
		return new ConfigurationElement(self, actualContributingBundle, name, propertiesAndValue, children, misc, parentId, parentType);
	}

	public Object loadThirdLevelConfigurationElements(int offset, RegistryObjectManager objectManager) {
		try {
			goToExtraFile(offset);
			return loadConfigurationElementAndChildren(null, extraInput, 3, Integer.MAX_VALUE, objectManager, null);
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.regCacheIOExceptionReading"), e)); //$NON-NLS-1$
			if (DEBUG)
				InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, "Error reading a third level configuration element (" + offset + ") from the registry cache", e)); //$NON-NLS-1$//$NON-NLS-2$
			return null;
		} finally {
			closeInputFile();
			closeExtraFile();
		}
	}

	//Read a whole configuration element subtree
	private ConfigurationElement loadConfigurationElementAndChildren(DataInputStream is, DataInputStream extraIs, int depth, int maxDepth, RegistryObjectManager objectManager, Bundle actualContributingBundle) throws IOException {
		DataInputStream currentStream = is;
		if (depth > 2)
			currentStream = extraIs;

		ConfigurationElement ce = basicLoadConfigurationElement(currentStream, actualContributingBundle);
		if (actualContributingBundle == null)
			actualContributingBundle = ce.getContributingBundle();
		int[] children = ce.getRawChildren();
		if (depth + 1 > maxDepth)
			return ce;

		for (int i = 0; i < children.length; i++) {
			ConfigurationElement tmp = loadConfigurationElementAndChildren(currentStream, extraIs, depth + 1, maxDepth, objectManager, actualContributingBundle);
			objectManager.add(tmp, holdObjects);
		}
		return ce;
	}

	private String[] readPropertiesAndValue(DataInputStream inputStream) throws IOException {
		int numberOfProperties = inputStream.readInt();
		if (numberOfProperties == 0)
			return RegistryObjectManager.EMPTY_STRING_ARRAY;
		String[] properties = new String[numberOfProperties];
		for (int i = 0; i < numberOfProperties; i++) {
			properties[i] = readStringOrNull(inputStream, false);
		}
		return properties;
	}

	public Object loadExtension(int offset) {
		try {
			goToInputFile(offset);
			return basicLoadExtension(mainInput);
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.regCacheIOExceptionReading"), e)); //$NON-NLS-1$
			if (DEBUG)
				InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, "Error reading an extension (" + offset + ") from the registry cache", e)); //$NON-NLS-1$//$NON-NLS-2$
		} finally {
			closeExtraFile();
			closeInputFile();
		}
		return null;
	}

	private Bundle getBundle(long id) {
		return InternalPlatform.getDefault().getBundleContext().getBundle(id);
	}

	private Extension basicLoadExtension(DataInputStream inputStream) throws IOException {
		int self = inputStream.readInt();
		String simpleId = readStringOrNull(mainInput, false);
		String namespace = readStringOrNull(mainInput, false);
		int[] children = readArray(mainInput);
		int extraData = mainInput.readInt();
		return new Extension(self, simpleId, namespace, children, extraData);
	}

	public ExtensionPoint loadExtensionPointTree(int offset, RegistryObjectManager objects) {
		try {
			ExtensionPoint xpt = (ExtensionPoint) loadExtensionPoint(offset);
			int[] children = xpt.getRawChildren();
			int nbrOfExtension = children.length;
			for (int i = 0; i < nbrOfExtension; i++) {
				Extension loaded = basicLoadExtension(mainInput);
				objects.add(loaded, holdObjects);
			}

			for (int i = 0; i < nbrOfExtension; i++) {
				int nbrOfCe = mainInput.readInt();
				for (int j = 0; j < nbrOfCe; j++) {
					objects.add(loadConfigurationElementAndChildren(mainInput, extraInput, 1, 2, objects, null), holdObjects);
				}
			}
			return xpt;
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.regCacheIOExceptionReading"), e)); //$NON-NLS-1$
			if (DEBUG)
				InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, "Error reading an extension point tree (" + offset + ") from the registry cache", e)); //$NON-NLS-1$//$NON-NLS-2$
			return null;
		} finally {
			closeExtraFile();
			closeInputFile();
		}
	}

	private Object loadExtensionPoint(int offset) {
		try {
			goToInputFile(offset);
			return basicLoadExtensionPoint();
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.regCacheIOExceptionReading"), e)); //$NON-NLS-1$
			if (DEBUG)
				InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, "Error reading an extension point (" + offset + ") from the registry cache", e)); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	private ExtensionPoint basicLoadExtensionPoint() throws IOException {
		int self = mainInput.readInt();
		int[] children = readArray(mainInput);
		int extraData = mainInput.readInt();
		return new ExtensionPoint(self, children, extraData);
	}

	private int[] readArray(DataInputStream in) throws IOException {
		int arraySize = in.readInt();
		if (arraySize == 0)
			return RegistryObjectManager.EMPTY_INT_ARRAY;
		int[] result = new int[arraySize];
		for (int i = 0; i < arraySize; i++) {
			result[i] = in.readInt();
		}
		return result;
	}

	private void goToInputFile(int offset) throws IOException {
		mainInput.skipBytes(offset);
	}

	private void goToExtraFile(int offset) throws IOException {
		extraInput.skipBytes(offset);
	}

	private String readStringOrNull(DataInputStream in, boolean intern) throws IOException {
		byte type = in.readByte();
		if (type == NULL)
			return null;
		if (intern)
			return in.readUTF().intern();
		return in.readUTF();
	}

	public String[] loadExtensionExtraData(int dataPosition) {
		try {
			goToExtraFile(dataPosition);
			return basicLoadExtensionExtraData();
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.regCacheIOExceptionReading"), e)); //$NON-NLS-1$
			if (DEBUG)
				InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, "Error reading extension label (" + dataPosition + ") from the registry cache", e)); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		} finally {
			closeExtraFile();
			closeInputFile();
		}
	}

	private String[] basicLoadExtensionExtraData() throws IOException {
		return new String[] {readStringOrNull(extraInput, false), readStringOrNull(extraInput, false)};
	}

	public String[] loadExtensionPointExtraData(int offset) {
		try {
			goToExtraFile(offset);
			return basicLoadExtensionPointExtraData();
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.regCacheIOExceptionReading"), e)); //$NON-NLS-1$
			if (DEBUG)
				InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, "Error reading extension point data (" + offset + ") from the resgistry cache", e)); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		} finally {
			closeExtraFile();
			closeInputFile();
		}
	}

	private String[] basicLoadExtensionPointExtraData() throws IOException {
		String[] result = new String[5];
		result[0] = readStringOrNull(extraInput, false); //the label
		result[1] = readStringOrNull(extraInput, false); //the schema
		result[2] = readStringOrNull(extraInput, false); //the fully qualified name
		result[3] = readStringOrNull(extraInput, false); //the namespace
		result[4] = Long.toString(extraInput.readLong());
		return result;
	}

	public KeyedHashSet loadNamespaces() {
		DataInputStream namespaceInput = null;
		try {
			namespaceInput = new DataInputStream(new BufferedInputStream(new FileInputStream(contributionsFile)));
			int size = namespaceInput.readInt();
			KeyedHashSet result = new KeyedHashSet(size);
			for (int i = 0; i < size; i++) {
				Contribution n = new Contribution(namespaceInput.readLong());
				n.setRawChildren(readArray(namespaceInput));
				result.add(n);
			}
			return result;
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.regCacheIOExceptionReading", contributionsFile.toString()), e)); //$NON-NLS-1$
			return null;
		} finally {
			if (namespaceInput != null)
				try {
					namespaceInput.close();
				} catch (IOException e1) {
					//Ignore
				}
		}
	}

	private void loadAllOrphans(RegistryObjectManager objectManager) throws IOException {
		//Read the extensions and configuration elements of the orphans
		int orphans = objectManager.getOrphanExtensions().size();
		for (int k = 0; k < orphans; k++) {
			int numberOfOrphanExtensions = mainInput.readInt();
			for (int i = 0; i < numberOfOrphanExtensions; i++) {
				loadFullExtension(objectManager);
			}
			for (int i = 0; i < numberOfOrphanExtensions; i++) {
				int nbrOfCe = mainInput.readInt();
				for (int j = 0; j < nbrOfCe; j++) {
					objectManager.add(loadConfigurationElementAndChildren(mainInput, extraInput, 1, Integer.MAX_VALUE, objectManager, null), true);
				}
			}
		}
	}

	public boolean readAllCache(RegistryObjectManager objectManager) {
		try {
			int size = objectManager.getExtensionPoints().size();
			for (int i = 0; i < size; i++) {
				objectManager.add(readAllExtensionPointTree(objectManager), holdObjects);
			}
			loadAllOrphans(objectManager);
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.regCacheIOExceptionReading"), e)); //$NON-NLS-1$
			return false;
		} finally {
			closeExtraFile();
			closeInputFile();
		}
		return true;
	}

	public ExtensionPoint readAllExtensionPointTree(RegistryObjectManager objectManager) throws IOException {
		ExtensionPoint xpt = loadFullExtensionPoint();
		int[] children = xpt.getRawChildren();
		int nbrOfExtension = children.length;
		for (int i = 0; i < nbrOfExtension; i++) {
			loadFullExtension(objectManager);
		}

		for (int i = 0; i < nbrOfExtension; i++) {
			int nbrOfCe = mainInput.readInt();
			for (int j = 0; j < nbrOfCe; j++) {
				objectManager.add(loadConfigurationElementAndChildren(mainInput, extraInput, 1, Integer.MAX_VALUE, objectManager, null), true);
			}
		}
		return xpt;
	}

	private ExtensionPoint loadFullExtensionPoint() throws IOException { //TODO I don't like this. 
		ExtensionPoint xpt = basicLoadExtensionPoint();
		String[] tmp = basicLoadExtensionPointExtraData();
		xpt.setLabel(tmp[0]);
		xpt.setSchema(tmp[1]);
		xpt.setUniqueIdentifier(tmp[2]);
		xpt.setNamespace(tmp[3]);
		xpt.setBundleId(Long.parseLong(tmp[4]));
		return xpt;
	}

	private Extension loadFullExtension(RegistryObjectManager objectManager) throws IOException {
		String[] tmp;
		Extension loaded = basicLoadExtension(mainInput);
		tmp = basicLoadExtensionExtraData();
		loaded.setLabel(tmp[0]);
		loaded.setExtensionPointIdentifier(tmp[1]);
		objectManager.add(loaded, holdObjects);
		return loaded;
	}

	public HashMap loadOrphans() {
		DataInputStream orphanInput = null;
		try {
			orphanInput = new DataInputStream(new BufferedInputStream(new FileInputStream(orphansFile)));
			int size = orphanInput.readInt();
			HashMap result = new HashMap(size);
			for (int i = 0; i < size; i++) {
				String key = orphanInput.readUTF();
				int[] value = readArray(orphanInput);
				result.put(key, value);
			}
			return result;
		} catch (IOException e) {
			return null;
		} finally {
			if (orphanInput != null)
				try {
					orphanInput.close();
				} catch (IOException e1) {
					//ignore
				}
		}
	}

	public void setHoldObjects(boolean holdObjects) {
		this.holdObjects = holdObjects;
	}
}
