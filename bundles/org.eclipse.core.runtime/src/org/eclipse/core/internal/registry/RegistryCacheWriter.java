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
import java.util.HashMap;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;

public class RegistryCacheWriter {
	// See RegistryCacheReader for constants commonly used here too.

	// objectTable will be a hashmap of objects. The objects will be things
	// like a plugin descriptor, extension, extension point, etc. The integer
	// index value will be used in the cache to allow cross-references in the
	// cached registry.
	protected HashMap objectTable = null;
	protected MultiStatus problems = null;
	protected File cacheFile;

	public RegistryCacheWriter(File cacheFile) {
		super();
		this.cacheFile = cacheFile;
	}
	private int addToObjectTable(Object object) {
		if (objectTable == null) {
			objectTable = new HashMap();
		}
		objectTable.put(object, new Integer(objectTable.size()));
		// return the index of the object just added (i.e. size - 1)
		return (objectTable.size() - 1);
	}
	private int getFromObjectTable(Object object) {
		if (objectTable != null) {
			Object objectResult = objectTable.get(object);
			if (objectResult != null) {
				return ((Integer) objectResult).intValue();
			}
		}
		return -1;
	}

	public void writeConfigurationElement(ConfigurationElement object, DataOutputStream out) {
		try {
			writeStringOrNull(object.getName(), out);
			writeStringOrNull(object.getValue(), out);

			ConfigurationProperty[] properties = object.getProperties();
			int length = (properties == null) ? 0 : properties.length;
			out.writeInt(length);
			for (int i = 0; i < length; i++)
				writeConfigurationProperty(properties[i], out);

			IConfigurationElement[] elements = object.getChildren();
			length = (elements == null) ? 0 : elements.length;
			out.writeInt(length);
			for (int i = 0; i < length; i++)
				writeConfigurationElement((ConfigurationElement) elements[i], out);
		} catch (IOException ioe) {
			problems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOExceptionWriting", "ConfigruationElement"), ioe)); //$NON-NLS-1$
		}
	}
	public void writeConfigurationProperty(ConfigurationProperty object, DataOutputStream out) {
		try {
			writeStringOrNull(object.getName(), out);
			writeStringOrNull(object.getValue(), out);
		} catch (IOException ioe) {
			problems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOExceptionWriting", "ConfigurationProperty"), ioe)); //$NON-NLS-1$
		}
	}
	public void writeExtension(Extension object, DataOutputStream out) {
		try {
			if (writeIndex(object, out))
				return;

			// add this object to the object table first
			addToObjectTable(object);
			out.writeByte(RegistryCacheReader.OBJECT);
			writeStringOrNull(object.getSimpleIdentifier(), out);
			writeBundleModel((BundleModel) object.getParent(), out);
			writeStringOrNull(object.getName(), out);
			writeStringOrNull(object.getExtensionPointIdentifier(), out);
			writeSubElements(object, out);
		} catch (IOException ioe) {
			problems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOExceptionWriting", "Extension"), ioe)); //$NON-NLS-1$
		}
	}
	public void writeSubElements(Extension object, DataOutputStream out) throws IOException {
		IConfigurationElement[] subElements = object.getConfigurationElements();
		if (subElements == null) {
			out.writeByte(RegistryCacheReader.NULL);
			return;
		}
		out.writeByte(RegistryCacheReader.OBJECT);
		// write the offset for sub-elements data
		out.writeInt(out.size());
		// write the sub-elements data to a memory stream...
		// so we can prefix that data with its size (to easily skip it)
		ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
		CheckedOutputStream checkedOut = new CheckedOutputStream(arrayOut, new CRC32());
		DataOutputStream tempOut = new DataOutputStream(checkedOut);
		tempOut.writeInt(subElements.length);
		for (int i = 0; i < subElements.length; i++)
			writeConfigurationElement((ConfigurationElement) subElements[i], tempOut);
		tempOut.close();
		// write the block size, the data and a checksum
		out.writeInt(arrayOut.size());
		out.write(arrayOut.toByteArray());
		out.writeLong(checkedOut.getChecksum().getValue());
	}

	public void writeExtensionPoint(ExtensionPoint object, DataOutputStream out) {
		try {
			if (writeIndex(object, out))
				return;
			// add this object to the object table first
			addToObjectTable(object);
			out.writeByte(RegistryCacheReader.OBJECT);
			writeStringOrNull(object.getSimpleIdentifier(), out);
			writeStringOrNull(object.getName(), out);
			writeStringOrNull(object.getSchema(), out);

			// Now do the extensions.
			IExtension[] extensions = object.getExtensions();
			int length = extensions == null ? 0 : extensions.length;
			out.writeInt(length);
			for (int i = 0; i < length; i++)
				writeExtension((Extension) extensions[i], out);
		} catch (IOException ioe) {
			problems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOExceptionWriting", "ExtensionPoint"), ioe)); //$NON-NLS-1$
		}
	}
	public void writeHeaderInformation(long registryStamp, DataOutputStream out) {
		try {
			out.writeInt(RegistryCacheReader.REGISTRY_CACHE_VERSION);
			out.writeLong(InternalPlatform.getDefault().getStateTimeStamp());
			out.writeLong(registryStamp);			
			IPlatform info = InternalPlatform.getDefault();
			out.writeUTF(info.getOS());
			out.writeUTF(info.getWS());
			out.writeUTF(info.getNL());
		} catch (IOException ioe) {
			problems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOExceptionWriting", "HeaderInformation"), ioe)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void writeBundleModel(BundleModel object, DataOutputStream out) {
		try {
			if (writeIndex(object, out))
				return;

			// add this object to the object table first
			addToObjectTable(object);
			out.writeByte(RegistryCacheReader.OBJECT);
			writeStringOrNull(object.getUniqueIdentifier(), out);
			out.writeLong(object.getId());
			writeRegistry((ExtensionRegistry) object.getParent(), out);
			writeStringOrNull(object.getHostIdentifier(), out);

			// need to worry about cross links here
			// now do extension points
			IExtensionPoint[] extensionPoints = object.getExtensionPoints();
			int length = (extensionPoints == null) ? 0 : extensionPoints.length;
			out.writeInt(length);
			for (int i = 0; i < length; i++)
				writeExtensionPoint((ExtensionPoint) extensionPoints[i], out);

			// and then extensions
			IExtension[] extensions = object.getExtensions();
			length = (extensions == null) ? 0 : extensions.length;
			out.writeInt(length);
			for (int i = 0; i < length; i++)
				writeExtension((Extension) extensions[i], out);
		} catch (IOException ioe) {
			problems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOExceptionWriting", "Bundle"), ioe)); //$NON-NLS-1$
		}
	}

	public void writeCache(ExtensionRegistry object, long registryStamp, DataOutputStream out) {
		if (problems == null)
			problems = new MultiStatus(IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.registryCacheWriteProblems"), null); //$NON-NLS-1$
		writeHeaderInformation(registryStamp, out);
		writeRegistry(object, out);
	}

	public void writeRegistry(ExtensionRegistry object, DataOutputStream out) {
		try {
			if (writeIndex(object, out))
				return;
			// add this object to the object table first
			addToObjectTable(object);
			out.writeByte(RegistryCacheReader.OBJECT);
			String[] ids = object.getElementIdentifiers();
			out.writeInt(ids.length);
			for (int i = 0; i < ids.length; i++)
				writeBundleModel((BundleModel) object.getElement(ids[i]), out);
		} catch (IOException ioe) {
			problems.add(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, IPlatform.PARSE_PROBLEM, Policy.bind("meta.regCacheIOExceptionWriting", "ExtensionRegisry"), ioe)); //$NON-NLS-1$
		}
	}

	private void writeStringOrNull(String string, DataOutputStream out) throws IOException {
		if (string == null)
			out.writeByte(RegistryCacheReader.NULL);
		else {
			out.writeByte(RegistryCacheReader.OBJECT);
			out.writeUTF(string);
		}
	}

	private boolean writeIndex(Object object, DataOutputStream out) throws IOException {
		if (object == null) {
			out.writeByte(RegistryCacheReader.NULL);
			return true;
		}
		int index = getFromObjectTable(object);
		if (index == -1)
			return false;
		out.writeByte(RegistryCacheReader.INDEX);
		out.writeInt(index);
		return true;
	}

	public void saveCache(ExtensionRegistry registry, long registryStamp) {
		try {
			DataOutputStream out = openCacheFile();
			try {
				writeCache(registry, registryStamp, out);
			} finally {
				out.close();
			}
		} catch (IOException e) {
			// an I/O failure would keep the extension elements unloaded
			//TODO: log this exception?
			if (InternalPlatform.DEBUG_REGISTRY)
				e.printStackTrace();
		}
	}
	public void saveCache(ExtensionRegistry registry) {
		this.saveCache(registry, 0);
	}

	private DataOutputStream openCacheFile() throws IOException {
		return new DataOutputStream(new SafeFileOutputStream(cacheFile));	//TODO Do we need to use the safeFile? Failing would not really matter anyway. If this is removed then we can consider deleting the SafeFileOutputStream class
	}

}
