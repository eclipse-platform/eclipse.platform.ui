/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;

/**
 * Failure reporting strategy:
 * - when reading an identified element (bundles, extension points and extensions),
 * catch any IOExceptions and rethrow them wrapped into a InvalidRegistryCacheException 
 * that describes where the error happened
 * - IOExceptions while reading non-identified elements (configuration elements and properties)
 * are just propagated to their callers
 * - the public entry points will catch any exceptions to ensure they are logged and return a valid
 * value instead (for instance, a null reference or an empty array)  
 */
public class RegistryCacheReader {

	Factory cacheFactory;
	// objectTable will be an array list of objects. The objects will be things
	// like a plugin descriptor, extension point, etc. The integer
	// index value will be used in the cache to allow cross-references in the
	// cached registry.
	protected List objectTable = null;
	private boolean lazilyLoadExtensions;
	protected File cacheFile;

	public static final byte REGISTRY_CACHE_VERSION = 6;
	public static final byte NULL = 0;
	public static final byte OBJECT = 1;
	public static final byte INDEX = 2;

	public RegistryCacheReader(File cacheFile, Factory factory, boolean lazilyLoadExtensions) {
		super();
		this.cacheFile = cacheFile;
		this.lazilyLoadExtensions = lazilyLoadExtensions;
		cacheFactory = factory;
		objectTable = new ArrayList();
	}

	public RegistryCacheReader(File cacheFile, Factory factory) {
		this(cacheFile, factory, false);
	}

	private int addToObjectTable(Object object) {
		objectTable.add(object);
		// return the index of the object just added (i.e. size - 1)
		return (objectTable.size() - 1);
	}

	private void debug(String msg) {
		System.out.println("RegistryCacheReader: " + msg); //$NON-NLS-1$
	}

	private boolean readHeaderInformation(DataInputStream in, long expectedTimestamp) throws InvalidRegistryCacheException {
		try {
			if (in.readInt() != REGISTRY_CACHE_VERSION)
				return false;
			long installStamp = in.readLong();
			long registryStamp = in.readLong();
			String osStamp = in.readUTF();
			String windowsStamp = in.readUTF();
			String localeStamp = in.readUTF();
			IPlatform info = InternalPlatform.getDefault();
			return ((expectedTimestamp == 0 || expectedTimestamp == registryStamp) && (installStamp == InternalPlatform.getDefault().getStateTimeStamp()) && (osStamp.equals(info.getOS())) && (windowsStamp.equals(info.getWS())) && (localeStamp.equals(info.getNL())));
		} catch (IOException e) {
			throw new InvalidRegistryCacheException(Policy.bind("meta.regCacheIOExceptionReading", "HeaderInformation"), e); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	private ConfigurationElement readConfigurationElement(RegistryModelObject parent, DataInputStream in) throws IOException {
		ConfigurationElement result = cacheFactory.createConfigurationElement();
		result.setParent(parent);
		result.setName(readString(in, true));
		result.setValue(readString(in, true));

		int length = in.readInt();
		ConfigurationProperty[] properties = new ConfigurationProperty[length];
		for (int i = 0; i < length; i++)
			properties[i] = readConfigurationProperty(in);
		result.setProperties(properties);

		length = in.readInt();
		IConfigurationElement[] elements = new ConfigurationElement[length];
		for (int i = 0; i < length; i++)
			elements[i] = readConfigurationElement(result, in);
		result.setChildren(elements);
		return result;
	}

	private ConfigurationProperty readConfigurationProperty(DataInputStream in) throws IOException {
		ConfigurationProperty result = cacheFactory.createConfigurationProperty();
		result.setName(readString(in, true));
		result.setValue(readString(in, true));
		return result;
	}

	private Extension readExtension(DataInputStream in) throws InvalidRegistryCacheException {
		Extension result = null;
		try {
			result = (Extension) readIndex(in);
			if (result != null)
				return result;
			result = cacheFactory.createExtension();
			addToObjectTable(result);
			result.setSimpleIdentifier(readString(in, true));
			result.setParent(readBundleModel(in));
			result.setName(readString(in, false));
			result.setExtensionPointIdentifier(readString(in, true));
			result.setSubElements(readSubElements(result, in));
			return result;
		} catch (IOException e) {
			String extensionId = null;
			if (result != null && result.getParent() != null)
				extensionId = result.getParentIdentifier() + "." + result.getSimpleIdentifier();
			throw new InvalidRegistryCacheException(Policy.bind("meta.regCacheIOExceptionReading", "extension:  " + extensionId), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private ExtensionPoint readExtensionPoint(BundleModel bundle, DataInputStream in) throws InvalidRegistryCacheException {
		ExtensionPoint result = null;
		try {
			result = (ExtensionPoint) readIndex(in);
			if (result != null)
				return result;
			result = cacheFactory.createExtensionPoint();
			addToObjectTable(result);
			result.setParent(bundle);
			result.setSimpleIdentifier(readString(in, true));
			result.setName(readString(in, false));
			result.setSchema(readString(in, true));

			// Now do the extensions.
			int length = in.readInt();
			IExtension[] extensions = new Extension[length];
			for (int i = 0; i < length; i++)
				extensions[i] = readExtension(in);
			result.setExtensions(extensions);
			return result;
		} catch (IOException e) {
			String extensionPointId = null;
			if (result != null && result.getParent() != null)
				extensionPointId = result.getUniqueIdentifier();
			throw new InvalidRegistryCacheException(Policy.bind("meta.regCacheIOExceptionReading", "extension point: " + extensionPointId), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private BundleModel readBundleModel(DataInputStream in) throws InvalidRegistryCacheException {
		BundleModel result = null;
		try {
			result = (BundleModel) readIndex(in);
			if (result != null)
				return result;
			result = cacheFactory.createBundle();
			addToObjectTable(result);
			result.setUniqueIdentifier(readString(in, true));
			result.setBundle(InternalPlatform.getDefault().getBundleContext().getBundle(in.readLong()));
			result.setParent(readRegistry(in));
			result.setHostIdentifier(readString(in, true));

			// now do extension points
			int length = in.readInt();
			IExtensionPoint[] extensionPoints = new ExtensionPoint[length];
			for (int i = 0; i < length; i++)
				extensionPoints[i] = readExtensionPoint(result, in);
			result.setExtensionPoints(extensionPoints);

			// and then extensions
			length = in.readInt();
			IExtension[] extensions = new Extension[length];
			for (int i = 0; i < length; i++)
				extensions[i] = readExtension(in);
			result.setExtensions(extensions);
			return result;
		} catch (IOException e) {
			String bundleId = (result == null || result.getUniqueIdentifier() == null) ? "<not available>" : result.getUniqueIdentifier(); //$NON-NLS-1$
			throw new InvalidRegistryCacheException(Policy.bind("meta.regCacheIOExceptionReading", "plugin: " + bundleId), e); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	private ExtensionRegistry readCache(DataInputStream in, long expectedTimestamps) throws InvalidRegistryCacheException {
		if (!readHeaderInformation(in, expectedTimestamps)) {
			if (InternalPlatform.DEBUG_REGISTRY)
				debug("Cache header information out of date - ignoring cache"); //$NON-NLS-1$
			return null;
		}
		return readRegistry(in);
	}

	private ExtensionRegistry readRegistry(DataInputStream in) throws InvalidRegistryCacheException {
		try {
			ExtensionRegistry result = (ExtensionRegistry) readIndex(in);
			if (result != null)
				return result;

			result = cacheFactory.createRegistry();
			if (lazilyLoadExtensions)
				result.setCacheReader(this);
			addToObjectTable(result);
			// if there are no plugins in the registry, return null instead of
			// an empty registry?
			int length = in.readInt();
			if (length == 0)
				return null;
			for (int i = 0; i < length; i++)
				result.basicAdd(readBundleModel(in), false);
			if (lazilyLoadExtensions)
				result.setCacheReader(this);
			return result;
		} catch (IOException e) {
			throw new InvalidRegistryCacheException(Policy.bind("meta.regCacheIOExceptionReading", "ExtensionRegistry"), e); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	private ConfigurationElement[] readSubElements(Extension parent, DataInputStream in) throws IOException, InvalidRegistryCacheException {
		int type = in.readByte();
		if (type == NULL)
			return null;

		// the first field is extension sub-elements data offset
		int offset = in.readInt();

		if (lazilyLoadExtensions) {
			Extension extension = parent;
			extension.setSubElementsCacheOffset(offset);
			checkSubElements(parent, in);
			extension.setFullyLoaded(false);
			return null;
		}
		return readBasicSubElements(parent, in);
	}

	private ConfigurationElement[] readBasicSubElements(Extension parent, DataInputStream in) throws IOException {
		// skip the byte count of the elements data 
		in.readInt();
		// read the number of sub elements to load
		int length = in.readInt();
		ConfigurationElement[] result = new ConfigurationElement[length];
		for (int i = 0; i < length; i++)
			result[i] = readConfigurationElement(parent, in);
		// skip checksum
		in.readLong();
		return result;
	}

	private void checkSubElements(Extension parent, DataInputStream in) throws IOException, InvalidRegistryCacheException {
		int subElementsDataLength = in.readInt();
		CheckedInputStream checkedIn = new CheckedInputStream(in, new CRC32());
		if (checkedIn.skip(subElementsDataLength) != subElementsDataLength) {
			String message = null;
			if (InternalPlatform.DEBUG_REGISTRY)
				message = "EOF checking sub-elements data for extension " + parent.getName(); //$NON-NLS-1$ 
			throw new InvalidRegistryCacheException(message);
		}
		long checksum = in.readLong();
		if (checksum != checkedIn.getChecksum().getValue()) {
			String message = null;
			if (InternalPlatform.DEBUG_REGISTRY)
				message = "Checksum error checking sub-elements data for extension " + parent.getName(); //$NON-NLS-1$ 
			throw new InvalidRegistryCacheException(message);
		}
	}

	private String readString(DataInputStream in, boolean intern) throws IOException {
		byte type = in.readByte();
		if (type == NULL)
			return null;
		if (intern)
			return in.readUTF().intern();
		else
			return in.readUTF();
	}

	private Object readIndex(DataInputStream in) throws IOException {
		byte type = in.readByte();
		return type == INDEX ? objectTable.get(in.readInt()) : null;
	}

	private DataInputStream openCacheFile() throws IOException {
		return new DataInputStream(new BufferedInputStream(new FileInputStream(cacheFile), 2048));
	}

	/**
	 * Lazily loads an extension model's sub-elements.
	 */
	public final ConfigurationElement[] loadConfigurationElements(Extension parent, int offset) {
		DataInputStream in = null;
		try {
			in = openCacheFile();
			in.skipBytes(offset);
			in.readInt(); // skip the offset itself
			return readBasicSubElements(parent, in);
		} catch (IOException e) {
			Throwable exception = InternalPlatform.DEBUG_REGISTRY ? e : null;
			String message = Policy.bind("meta.unableToReadCache"); //$NON-NLS-1$
			InternalPlatform.getDefault().log(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, message, exception));
		} catch (Throwable t) {
			// catch any OutOfMemoryErrors/NullPointerExceptions that may have been caused by corrupted data
			// log general message
			String message = Policy.bind("meta.registryCacheReadProblems"); //$NON-NLS-1$						
			InternalPlatform.getDefault().log(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, message, null));
			// log actual error			
			Throwable exceptionToLog = InternalPlatform.DEBUG_REGISTRY ? t : null;
			InternalPlatform.getDefault().log(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, t.toString(), exceptionToLog));
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				Throwable exception = InternalPlatform.DEBUG_REGISTRY ? e : null;
				String message = Policy.bind("meta.registryCacheReadProblems"); //$NON-NLS-1$
				InternalPlatform.getDefault().log(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, message, exception));
			}
		}
		return new ConfigurationElement[0];
	}

	public final ExtensionRegistry loadCache() {
		return this.loadCache(0);
	}

	/*
	 * If expectedTimestamp != 0, check it against the registry timestamp if the header. 
	 */
	public final ExtensionRegistry loadCache(long expectedTimestamp) {
		DataInputStream in = null;
		try {
			in = openCacheFile();
		} catch (IOException e) {
			Throwable exception = InternalPlatform.DEBUG_REGISTRY ? e : null;
			String message = Policy.bind("meta.unableToReadCache"); //$NON-NLS-1$
			InternalPlatform.getDefault().log(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, message, exception));
			return null;
		}
		try {
			return readCache(in, expectedTimestamp);
		} catch (InvalidRegistryCacheException e) {
			Throwable exception = InternalPlatform.DEBUG_REGISTRY ? e.getCause() : null;
			InternalPlatform.getDefault().log(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, e.getMessage(), exception));
		} catch (Throwable t) {
			// catch any OutOfMemoryErrors/NullPointerExceptions that may have been caused by corrupted data
			// log general message
			String message = Policy.bind("meta.registryCacheReadProblems"); //$NON-NLS-1$						
			InternalPlatform.getDefault().log(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, message, null));
			// log actual error			
			Throwable exceptionToLog = InternalPlatform.DEBUG_REGISTRY ? t : null;
			InternalPlatform.getDefault().log(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, t.toString(), exceptionToLog));
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				Throwable exception = InternalPlatform.DEBUG_REGISTRY ? e : null;
				String message = Policy.bind("meta.registryCacheReadProblems"); //$NON-NLS-1$
				InternalPlatform.getDefault().log(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, message, exception));
			}
		}
		return null;
	}

	public class InvalidRegistryCacheException extends Exception {
		public InvalidRegistryCacheException(String msg, Throwable cause) {
			super(msg, cause);
		}

		public InvalidRegistryCacheException(String string) {
			super(string);
		}
	}
}