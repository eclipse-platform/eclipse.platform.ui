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

	MultiStatus problems = null;
	// objectTable will be an array list of objects. The objects will be things
	// like a plugin descriptor, extension point, etc. The integer
	// index value will be used in the cache to allow cross-references in the
	// cached registry.
	protected List objectTable = null;
	private boolean lazilyLoadExtensions;
	private boolean flushableExtensions = true;
	// indicates we failed load configuration elements lazily
	private boolean failed;
	protected File cacheFile;

	public static final byte REGISTRY_CACHE_VERSION = 7;
	public static final byte NULL = 0;
	public static final byte OBJECT = 1;
	public static final byte INDEX = 2;

	public RegistryCacheReader(File cacheFile, MultiStatus problems, boolean lazilyLoadExtensions, boolean flushable) {
		super();
		this.cacheFile = cacheFile;
		this.problems = problems;
		this.lazilyLoadExtensions = lazilyLoadExtensions;
		this.flushableExtensions = flushable;
		objectTable = new ArrayList();
	}

	public RegistryCacheReader(File cacheFile, MultiStatus problems) {
		this(cacheFile, problems, false, true);
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
			InternalPlatform info = InternalPlatform.getDefault();
			return ((expectedTimestamp == 0 || expectedTimestamp == registryStamp) && (installStamp == info.getStateTimeStamp()) && (osStamp.equals(info.getOS())) && (windowsStamp.equals(info.getWS())) && (localeStamp.equals(info.getNL())));
		} catch (IOException e) {
			throw new InvalidRegistryCacheException(Policy.bind("meta.regCacheIOExceptionReading", "HeaderInformation"), e); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	private void skipConfigurationElement(RegistryModelObject parent, DataInputStream in) throws IOException {
		readCachedString(in, false); //read name
		skipString(in); //skip value

		int length = in.readInt();
		for (int i = 0; i < length; i++) {
			skipConfigurationProperty(in);
		}
		length = in.readInt();
		for (int i = 0; i < length; i++) {
			skipConfigurationElement(null, in);
		}
	}

	private ConfigurationElement readConfigurationElement(RegistryModelObject parent, DataInputStream in) throws IOException {
		ConfigurationElement result = new ConfigurationElement();
		result.setParent(parent);
		result.setName(readCachedString(in, false));
		result.setValue(readString(in, false));

		int length = in.readInt();
		ConfigurationProperty[] properties = new ConfigurationProperty[length];
		for (int i = 0; i < length; i++) {
			properties[i] = readConfigurationProperty(in);
		}
		result.setProperties(properties);

		length = in.readInt();
		IConfigurationElement[] elements = new ConfigurationElement[length];
		for (int i = 0; i < length; i++) {
			elements[i] = readConfigurationElement(result, in);
		}
		result.setChildren(elements);
		return result;
	}

	private void skipConfigurationProperty(DataInputStream in) throws IOException {
		readCachedString(in, false); //Read the name
		skipString(in); // skip the value
	}

	private ConfigurationProperty readConfigurationProperty(DataInputStream in) throws IOException {
		String name = readCachedString(in, false);
		ConfigurationProperty result = new ConfigurationProperty();
		result.setName(name);
		result.setValue(readString(in, false));
		return result;
	}

	private Extension readExtension(DataInputStream in) throws InvalidRegistryCacheException {
		Extension result = null;
		try {
			result = (Extension) readIndex(in);
			if (result != null)
				return result;
			result = flushableExtensions ? new FlushableExtension() : new Extension();
			addToObjectTable(result);
			result.setSimpleIdentifier(readString(in, false));
			result.setParent(readBundleModel(in));
			result.setName(readString(in, false));
			result.setExtensionPointIdentifier(readCachedString(in, false));
			result.setSubElements(readSubElements(result, in));
			return result;
		} catch (IOException e) {
			String extensionId = null;
			if (result != null && result.getParent() != null)
				extensionId = result.getParentIdentifier() + "." + result.getSimpleIdentifier(); //$NON-NLS-1$
			throw new InvalidRegistryCacheException(Policy.bind("meta.regCacheIOExceptionReading", "extension:  " + extensionId), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private ExtensionPoint readExtensionPoint(Namespace bundle, DataInputStream in) throws InvalidRegistryCacheException {
		ExtensionPoint result = null;
		try {
			result = (ExtensionPoint) readIndex(in);
			if (result != null)
				return result;
			result = new ExtensionPoint();
			addToObjectTable(result);
			result.setParent(bundle);
			result.setSimpleIdentifier(readString(in, true));
			result.setName(readString(in, false));
			result.setSchema(readString(in, false));

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

	private Namespace readBundleModel(DataInputStream in) throws InvalidRegistryCacheException {
		Namespace result = null;
		try {
			result = (Namespace) readIndex(in);
			if (result != null)
				return result;
			result = new Namespace();
			addToObjectTable(result);
			result.setUniqueIdentifier(readCachedString(in, true));
			result.setBundle(InternalPlatform.getDefault().getBundleContext().getBundle(in.readLong()));
			result.setParent(readRegistry(in));
			result.setHostIdentifier(readCachedString(in, false));

			// now do extension points
			int length = in.readInt();
			IExtensionPoint[] extensionPoints = new ExtensionPoint[length];
			for (int i = 0; i < length; i++)
				extensionPoints[i] = readExtensionPoint(result, in);
			result.setExtensionPoints(extensionPoints);

			// and then extensions
			length = in.readInt();
			IExtension[] extensions = flushableExtensions ? new FlushableExtension[length] : new Extension[length];
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

			result = new ExtensionRegistry();
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

	private ConfigurationElement[] readSubElements(Extension parent, DataInputStream in) throws IOException {
		int type = in.readByte();
		if (type == NULL)
			return null;

		//Here type is OBJECT
		// the first field is extension sub-elements data offset
		int offset = in.readInt();

		if (lazilyLoadExtensions) {
			Extension extension = parent;
			extension.setSubElementsCacheOffset(offset);
			skipBasicSubElements(parent, in);
			extension.setFullyLoaded(false);
			return null;
		}
		return readBasicSubElements(parent, in);
	}

	private void skipBasicSubElements(Extension parent, DataInputStream in) throws IOException {
		int length = in.readInt();
		for (int i = 0; i < length; i++) {
			skipConfigurationElement(parent, in);
		}
	}

	private ConfigurationElement[] readBasicSubElements(Extension parent, DataInputStream in) throws IOException {
		// read the number of sub elements to load
		int length = in.readInt();

		ConfigurationElement[] result = new ConfigurationElement[length];
		for (int i = 0; i < length; i++) {
			result[i] = readConfigurationElement(parent, in);
		}
		return result;
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

	private void skipString(DataInputStream in) throws IOException {
		byte type = in.readByte();
		if (type == NULL)
			return;
		int utfLength = in.readUnsignedShort();
		byte bytearr[] = new byte[utfLength];
		in.readFully(bytearr, 0, utfLength);
	}

	private String readCachedString(DataInputStream in, boolean intern) throws IOException {
		byte type = in.readByte();
		if (type == NULL)
			return null;

		if (type == INDEX)
			return (String) objectTable.get(in.readInt());

		String stringRead = null;
		if (intern)
			stringRead = in.readUTF().intern();
		else
			stringRead = in.readUTF();
		addToObjectTable(stringRead);
		return stringRead;
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
			InternalPlatform.getDefault().log(new Status(IStatus.WARNING, Platform.PI_RUNTIME, 0, message, exception));
		} catch (OutOfMemoryError oome) {
			// catch any OutOfMemoryErrors that may have been caused by corrupted data
			logError(oome);
		} catch (RuntimeException re) {
			// catch any ArrayIndexOutOfBounds/NullPointer/NegativeArraySize/... exceptions that may have been caused by corrupted data
			logError(re);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				// ignore
			}
		}
		// we only get here when we have problems
		failed = true;
		return new ConfigurationElement[0];
	}

	boolean hasFailed() {
		return failed;
	}

	private void logError(Throwable t) {
		// log general message
		String message = Policy.bind("meta.registryCacheReadProblems"); //$NON-NLS-1$						
		InternalPlatform.getDefault().log(new Status(IStatus.WARNING, Platform.PI_RUNTIME, 0, message, null));
		// log actual error			
		Throwable exceptionToLog = InternalPlatform.DEBUG_REGISTRY ? t : null;
		InternalPlatform.getDefault().log(new Status(IStatus.WARNING, Platform.PI_RUNTIME, 0, t.toString(), exceptionToLog));
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
			InternalPlatform.getDefault().log(new Status(IStatus.WARNING, Platform.PI_RUNTIME, 0, message, exception));
			return null;
		}
		try {
			return readCache(in, expectedTimestamp);
		} catch (InvalidRegistryCacheException e) {
			Throwable exception = InternalPlatform.DEBUG_REGISTRY ? e.getCause() : null;
			InternalPlatform.getDefault().log(new Status(IStatus.WARNING, Platform.PI_RUNTIME, 0, e.getMessage(), exception));
		} catch (OutOfMemoryError oome) {
			// catch any OutOfMemoryErrors that may have been caused by corrupted data
			logError(oome);
		} catch (RuntimeException re) {
			// catch any ArrayIndexOutOfBounds/NullPointer/NegativeArraySize/... exceptions that may have been caused by corrupted data
			logError(re);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				// ignore
			}
		}
		return null;
	}

	public class InvalidRegistryCacheException extends Exception {
		/**
		 * All serializable objects should have a stable serialVersionUID
		 */
		private static final long serialVersionUID = 1L;

		Throwable cause = null;

		public InvalidRegistryCacheException(String msg, Throwable cause) {
			super(msg);
			this.cause = cause;
		}

		public InvalidRegistryCacheException(String string) {
			super(string);
		}

		public Throwable getCause() {
			return cause;
		}
	}
}