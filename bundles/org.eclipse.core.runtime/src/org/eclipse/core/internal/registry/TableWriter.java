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
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;

public class TableWriter {
	private static final byte fileError = 0;

	static File mainDataFile;
	static File extraDataFile;
	static File tableFile;
	static File contributionsFile;
	static File orphansFile;

	static void setMainDataFile(File main) {
		mainDataFile = main;
	}

	static void setExtraDataFile(File extra) {
		extraDataFile = extra;
	}

	static void setTableFile(File table) {
		tableFile = table;
	}

	static void setContributionsFile(File fileName) {
		contributionsFile = fileName;
	}

	static void setOrphansFile(File orphan) {
		orphansFile = orphan;
	}

	DataOutputStream mainOutput;
	DataOutputStream extraOutput;

	private HashtableOfInt offsets;

	private int getExtraDataPosition() {
		return extraOutput.size();
	}

	public boolean saveCache(RegistryObjectManager objectManager, long timestamp) {
		try {
			if (!openFiles())
				return false;		
			try {
				saveExtensionRegistry(objectManager, timestamp);
			} catch (IOException io) {
				InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.registryCacheWriteProblems"), io)); //$NON-NLS-1$
				return false;
			}
		} finally {
			closeFiles();
		}
		return true;
	}

	private boolean openFiles() {
		try {
			mainOutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mainDataFile)));
			extraOutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(extraDataFile)));
		} catch (FileNotFoundException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.unableToCreateCache"), e)); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private void closeFiles() {
		try {
			if (mainOutput != null)
				mainOutput.close();
			if (extraOutput != null)
				extraOutput.close();
		} catch (IOException e) {
			InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, fileError, Policy.bind("meta.registryCacheWriteProblems"), e)); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	private void saveExtensionRegistry(RegistryObjectManager objectManager, long timestamp) throws IOException {
		ExtensionPointHandle[] points = objectManager.getExtensionPointsHandles();
		offsets = new HashtableOfInt(objectManager.getNextId());
		for (int i = 0; i < points.length; i++) {
			saveExtensionPoint(points[i]);
		}
		saveOrphans(objectManager);
		saveNamespaces(objectManager.getContributions());
		closeFiles();	//Close the files here so we can write the appropriate size information in the table file.
		saveTables(objectManager, timestamp); //Write the table last so if that is something went wrong we can know
	}

	private void saveNamespaces(KeyedHashSet[] contributions) throws IOException {
		DataOutputStream outputNamespace = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(contributionsFile)));
		KeyedElement[] newElements = contributions[0].elements();
		KeyedElement[] formerElements = contributions[1].elements();
		outputNamespace.writeInt(newElements.length + formerElements.length);
		for (int i = 0; i < newElements.length; i++) {
			Contribution elt = (Contribution) newElements[i];
			outputNamespace.writeLong(elt.getContributingBundle().getBundleId());
			saveArray(elt.getRawChildren(), outputNamespace);
		}
		for (int i = 0; i < formerElements.length; i++) {
			Contribution elt = (Contribution) formerElements[i];
			outputNamespace.writeLong(elt.getContributingBundle().getBundleId());
			saveArray(elt.getRawChildren(), outputNamespace);
		}
		outputNamespace.close();
	}

	private void saveTables(RegistryObjectManager objectManager, long registryTimeStamp) throws IOException {
		DataOutputStream outputTable = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tableFile)));
		writeCacheHeader(outputTable, registryTimeStamp);
		outputTable.writeInt(objectManager.getNextId());
		offsets.save(outputTable);
		objectManager.getExtensionPoints().save(outputTable);
		outputTable.close();
	}

	private void writeCacheHeader(DataOutputStream output, long registryTimeStamp) throws IOException {
		output.writeInt(TableReader.CACHE_VERSION);
		output.writeLong(InternalPlatform.getDefault().getStateTimeStamp());
		output.writeLong(registryTimeStamp);
		output.writeLong(mainDataFile.length());
		output.writeLong(extraDataFile.length());
		output.writeLong(contributionsFile.length());
		output.writeLong(orphansFile.length());
		InternalPlatform info = InternalPlatform.getDefault();
		output.writeUTF(info.getOS());
		output.writeUTF(info.getWS());
		output.writeUTF(info.getNL());
	}

	private void saveArray(int[] array, DataOutputStream out) throws IOException {
		if (array == null) {
			out.writeInt(0);
			return;
		}
		out.writeInt(array.length);
		for (int i = 0; i < array.length; i++) {
			out.writeInt(array[i]);
		}
	}

	private void saveExtensionPoint(ExtensionPointHandle xpt) throws IOException {
		//save the file position
		offsets.put(xpt.getId(), mainOutput.size());
		//save the extensionPoint
		mainOutput.writeInt(xpt.getId());
		saveArray(xpt.getObject().getRawChildren(), mainOutput);
		mainOutput.writeInt(getExtraDataPosition());
		saveExtensionPointData(xpt);

		saveExtensions(xpt.getExtensions(), mainOutput);
	}

	private void saveExtension(ExtensionHandle ext, DataOutputStream outputStream) throws IOException {
		offsets.put(ext.getId(), outputStream.size());
		outputStream.writeInt(ext.getId());
		writeStringOrNull(ext.getSimpleIdentifier(), outputStream);
		writeStringOrNull(ext.getNamespace(), outputStream);
		saveArray(ext.getObject().getRawChildren(), outputStream);
		outputStream.writeInt(getExtraDataPosition());
		saveExtensionData(ext);
	}

	private void writeStringArray(String[] array, DataOutputStream outputStream) throws IOException {
		outputStream.writeInt(array == null ? 0 : array.length);
		for (int i = 0; i < (array == null ? 0 : array.length); i++) {
			writeStringOrNull(array[i], outputStream);
		}
	}

	//Save Configuration elements depth first
	private void saveConfigurationElement(ConfigurationElementHandle element, DataOutputStream outputStream, DataOutputStream extraOutputStream, int depth) throws IOException {
		DataOutputStream currentOutput = outputStream;
		if (depth > 2)
			currentOutput = extraOutputStream;

		offsets.put(element.getId(), currentOutput.size());

		currentOutput.writeInt(element.getId());
		ConfigurationElement actualCe = (ConfigurationElement) element.getObject();

		currentOutput.writeLong(actualCe.getContributingBundle().getBundleId());
		writeStringOrNull(actualCe.getName(), currentOutput);
		currentOutput.writeInt(actualCe.parentId);
		currentOutput.writeByte(actualCe.parentType);
		currentOutput.writeInt(depth > 1 ? extraOutputStream.size() : -1);
		writeStringArray(actualCe.getPropertiesAndValue(), currentOutput);
		//save the children
		saveArray(actualCe.getRawChildren(), currentOutput);

		ConfigurationElementHandle[] childrenCEs = (ConfigurationElementHandle[]) element.getChildren();
		for (int i = 0; i < childrenCEs.length; i++) {
			saveConfigurationElement(childrenCEs[i], outputStream, extraOutputStream, depth + 1);
		}

	}

	private void saveExtensions(IExtension[] exts, DataOutputStream outputStream) throws IOException {
		for (int i = 0; i < exts.length; i++) {
			saveExtension((ExtensionHandle) exts[i], outputStream);
		}

		for (int i = 0; i < exts.length; i++) {
			IConfigurationElement[] ces = exts[i].getConfigurationElements();
			outputStream.writeInt(ces.length);
			for (int j = 0; j < ces.length; j++) {
				saveConfigurationElement((ConfigurationElementHandle) ces[j], outputStream, extraOutput, 1);
			}
		}
	}

	private void saveExtensionPointData(ExtensionPointHandle xpt) throws IOException {
		writeStringOrNull(xpt.getLabel(), extraOutput);
		writeStringOrNull(xpt.getSchemaReference(), extraOutput);
		writeStringOrNull(xpt.getUniqueIdentifier(), extraOutput);
		writeStringOrNull(xpt.getNamespace(), extraOutput);
		extraOutput.writeLong(((ExtensionPoint) xpt.getObject()).getBundleId());
	}

	private void saveExtensionData(ExtensionHandle extension) throws IOException {
		writeStringOrNull(extension.getLabel(), extraOutput);
		writeStringOrNull(extension.getExtensionPointUniqueIdentifier(), extraOutput);
	}

	private void writeStringOrNull(String string, DataOutputStream out) throws IOException {
		if (string == null)
			out.writeByte(TableReader.NULL);
		else {
			out.writeByte(TableReader.OBJECT);
			out.writeUTF(string);
		}
	}

	private void saveOrphans(RegistryObjectManager objectManager) throws IOException {
		Map orphans = objectManager.getOrphanExtensions();
		DataOutputStream outputOrphan = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(orphansFile)));
		outputOrphan.writeInt(orphans.size());
		Set elements = orphans.entrySet();
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			outputOrphan.writeUTF((String) entry.getKey());
			saveArray((int[]) entry.getValue(), outputOrphan);
		}
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			mainOutput.writeInt(((int[]) entry.getValue()).length);
			saveExtensions((IExtension[]) objectManager.getHandles((int[]) entry.getValue(), RegistryObjectManager.EXTENSION), mainOutput);
		}
		outputOrphan.close();
	}
}
