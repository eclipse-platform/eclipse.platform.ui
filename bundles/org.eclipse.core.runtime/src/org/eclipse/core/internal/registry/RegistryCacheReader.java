/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
import org.eclipse.core.runtime.registry.*;
import org.eclipse.osgi.service.environment.EnvironmentInfo;

public class RegistryCacheReader {

	Factory cacheFactory;
	// objectTable will be an array list of objects. The objects will be things
	// like a plugin descriptor, extension point, etc. The integer
	// index value will be used in the cache to allow cross-references in the
	// cached registry.
	protected List objectTable = null;
	private boolean lazilyLoadExtensions;
	private MultiStatus cacheReadProblems = null;
	protected File cacheFile;

	public static final byte REGISTRY_CACHE_VERSION = 2;
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

	private boolean readHeaderInformation(DataInputStream in) {
		try {
			if (in.readInt() != REGISTRY_CACHE_VERSION)
				return false;
			long installStamp = in.readLong();
			String osStamp = in.readUTF();
			String windowsStamp = in.readUTF();
			String localeStamp = in.readUTF();
			//TODO We need to save the state number for which the registry has been derived
			// Save the current plugin timestamp for writing to the
			// cache when we shutdown
			//			InternalPlatform.setRegistryCacheTimeStamp(BootLoader.getCurrentPlatformConfiguration().getPluginsChangeStamp());

			EnvironmentInfo info = InternalPlatform.getDefault().getEnvironmentInfoService();
			//			return ((installStamp == InternalPlatform.getRegistryCacheTimeStamp()) &&
			return ((osStamp.equals(info.getOS())) && (windowsStamp.equals(info.getWS())) && (localeStamp.equals(info.getNL())));
			// TODO need to read/compare the OS, WS and NL etc.
		} catch (IOException ioe) {
			cacheReadProblems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOException", "HeaderInformation"), ioe)); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}

	private ConfigurationElement readConfigurationElement(RegistryModelObject parent, DataInputStream in) {
		try {
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
		} catch (IOException ioe) {
			cacheReadProblems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOException", "ConfigurationElement"), ioe)); //$NON-NLS-1$
			return null;
		}
	}
	private ConfigurationProperty readConfigurationProperty(DataInputStream in) {
		try {
			ConfigurationProperty result = cacheFactory.createConfigurationProperty();
			result.setName(readString(in, true));
			result.setValue(readString(in, true));
			return result;
		} catch (IOException ioe) {
			cacheReadProblems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOException", "ConfigurationProperty"), ioe)); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}
	private Extension readExtension(DataInputStream in) {
		try {
			Extension result = (Extension) readIndex(in);
			if (result != null)
				return result;
			result = cacheFactory.createExtension();
			addToObjectTable(result);
			result.setSimpleIdentifier(readString(in, true));
			result.setParent(readBundleModel(in));
			result.setExtensionPointIdentifier(readString(in, true));
			result.setSubElements(readSubElements(result, in));
			return result;
		} catch (IOException ioe) {
			cacheReadProblems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOException", "Extension"), ioe)); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	private ExtensionPoint readExtensionPoint(DataInputStream in) {
		try {
			ExtensionPoint result = (ExtensionPoint) readIndex(in);
			if (result != null)
				return result;
			result = cacheFactory.createExtensionPoint();
			addToObjectTable(result);
			result.setSimpleIdentifier(readString(in, true));
			result.setSchema(readString(in, true));
			result.setParent(readBundleModel(in));

			// Now do the extensions.
			int length = in.readInt();
			IExtension[] extensions = new Extension[length];
			for (int i = 0; i < length; i++)
				extensions[i] = readExtension(in);
			result.setExtensions(extensions);
			return result;
		} catch (IOException ioe) {
			cacheReadProblems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOException", "ExtensionPoint"), ioe)); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}
	private BundleModel readBundleModel(DataInputStream in) {
		try {
			BundleModel result = (BundleModel) readIndex(in);
			if (result != null)
				return result;
			result = cacheFactory.createBundle();
			addToObjectTable(result);
			result.setUniqueIdentifier(readString(in, true));
			result.setId(in.readLong());
			result.setParent(readRegistry(in));
			result.setHostIdentifier(readString(in, true));

			// now do extension points
			int length = in.readInt();
			IExtensionPoint[] extensionPoints = new ExtensionPoint[length];
			for (int i = 0; i < length; i++)
				extensionPoints[i] = readExtensionPoint(in);
			result.setExtensionPoints(extensionPoints);

			// and then extensions
			length = in.readInt();
			IExtension[] extensions = new Extension[length];
			for (int i = 0; i < length; i++)
				extensions[i] = readExtension(in);
			result.setExtensions(extensions);

			// and then fragments
			length = in.readInt();
			BundleModel[] bundles = new BundleModel[length];
			for (int i = 0; i < length; i++)
				bundles[i] = readBundleModel(in);
			result.setFragments(bundles);
			return result;
		} catch (IOException ioe) {
			cacheReadProblems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOException", "Bundle"), ioe)); //$NON-NLS-1$
			return null;
		}
	}

	private ExtensionRegistry readCache(DataInputStream in) {
		if (cacheReadProblems == null)
			cacheReadProblems = new MultiStatus(IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.registryCacheReadProblems"), null); //$NON-NLS-1$
		if (!readHeaderInformation(in)) {
			if (InternalPlatform.DEBUG_REGISTRY)
				debug("Cache header information out of date - ignoring cache"); //$NON-NLS-1$
			return null;
		}
		return readRegistry(in);
	}

	private ExtensionRegistry readRegistry(DataInputStream in) {
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
		} catch (IOException ioe) {
			cacheReadProblems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOException", "ExtensionRegistry"), ioe)); //$NON-NLS-1$
			return null;
		}
	}

	private ConfigurationElement[] readSubElements(Extension parent, DataInputStream in) throws IOException {
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
		for (int i = 0; i < length; i++) {
			result[i] = readConfigurationElement(parent, in);
			if (result[i] == null) {
				String message = "Unable to read subelement #" + i + " for extension " + parent.getName(); //$NON-NLS-1$ //$NON-NLS-2$ 
				throw new InvalidRegistryCacheException(message);
			}
		}
		// skip checksum
		in.readLong();
		return result;
	}
	private void checkSubElements(Extension parent, DataInputStream in) throws IOException {
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
	public class InvalidRegistryCacheException extends IOException {
		public InvalidRegistryCacheException() {
			super();
		}
		public InvalidRegistryCacheException(String string) {
			super(string);
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
		try {
			DataInputStream in = openCacheFile();
			try {
				in.skipBytes(offset);
				in.readInt(); // skip the offset itself
				return readBasicSubElements(parent, in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			// an I/O failure would keep the extension elements unloaded
			//TODO: log this exception?
			if (InternalPlatform.DEBUG_REGISTRY)
				e.printStackTrace(System.err);
		}
		return null;
	}
	public final ExtensionRegistry loadCache() {
		try {
			DataInputStream in = openCacheFile();
			try {
				return readCache(in);
			} finally {
				in.close();
			}
		} catch (Throwable t) {
			// an I/O failure would keep the extension elements unloaded
			//TODO: log this exception?
			if (InternalPlatform.DEBUG_REGISTRY)
				System.err.println("Error opening cache file: " + t.getMessage()); //$NON-NLS-1$
		}
		return null;
	}

}
