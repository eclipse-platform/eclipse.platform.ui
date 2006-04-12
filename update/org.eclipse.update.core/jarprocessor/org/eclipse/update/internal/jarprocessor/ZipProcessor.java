/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.jarprocessor;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * @author aniefer
 *
 */
public class ZipProcessor {

	private IProcessStep signStep = null;
	private IProcessStep packStep = null;
	private IProcessStep packUnpackStep = null;
	private IProcessStep unpackStep = null;

	private String workingDirectory = null;
	private Properties properties = null;
	private Set packExclusions = null;
	private Set signExclusions = null;
	private String command = null;
	private boolean packing = false;
	private boolean signing = false;
	private boolean repacking = false;
	private boolean unpacking = false;

	public void setWorkingDirectory(String dir) {
		workingDirectory = dir;
	}

	public String getWorkingDirectory() {
		if (workingDirectory == null)
			workingDirectory = "."; //$NON-NLS-1$
		return workingDirectory;
	}

	public void setSignCommand(String command) {
		this.command = command;
		this.signing = (command != null);
	}

	public void setPack(boolean pack) {
		this.packing = pack;
	}

	public void setRepack(boolean repack) {
		this.repacking = repack;
	}

	public void setUnpack(boolean unpack) {
		this.unpacking = unpack;
	}

	public void processZip(File zipFile) throws ZipException, IOException {
		ZipFile zip = new ZipFile(zipFile);
		initialize(zip);

		String extension = unpacking ? "pack.gz" : ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
		File tempDir = new File(getWorkingDirectory(), "temp_" + zipFile.getName()); //$NON-NLS-1$
		JarProcessor processor = new JarProcessor();
		processor.setWorkingDirectory(tempDir.getCanonicalPath());
		if (unpacking) {
			processor.addProcessStep(unpackStep);
		}

		File outputFile = new File(getWorkingDirectory(), zipFile.getName() + ".temp"); //$NON-NLS-1$
		File parent = outputFile.getParentFile();
		if (!parent.exists())
			parent.mkdirs();
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputFile));
		Enumeration entries = zip.entries();
		if (entries.hasMoreElements()) {
			for (ZipEntry entry = (ZipEntry) entries.nextElement(); entry != null; entry = entries.hasMoreElements() ? (ZipEntry) entries.nextElement() : null) {
				String name = entry.getName();

				InputStream entryStream = zip.getInputStream(entry);

				boolean pack = packing && !packExclusions.contains(name);
				boolean sign = signing && !signExclusions.contains(name);
				boolean repack = repacking && !packExclusions.contains(name);

				File extractedFile = null;

				if (entry.getName().endsWith(extension) && (pack || sign || repack || unpacking)) {
					extractedFile = new File(tempDir, name);
					parent = extractedFile.getParentFile();
					if (!parent.exists())
						parent.mkdirs();
					FileOutputStream extracted = new FileOutputStream(extractedFile);
					Utils.transferStreams(entryStream, extracted, true);

					if (unpacking) {
						processor.processJar(extractedFile);
						name = name.substring(0, name.length() - Utils.PACKED_SUFFIX.length());
						extractedFile = new File(tempDir, name);
					} else {
						processor.clearProcessSteps();
						if (repack)
							processor.addProcessStep(packUnpackStep);
						if (sign)
							processor.addProcessStep(signStep);
						processor.processJar(extractedFile);
						extractedFile = new File(tempDir, extractedFile.getName());
						if (pack) {
							processor.clearProcessSteps();
							processor.addProcessStep(packStep);
							processor.processJar(extractedFile);

							File modifiedFile = new File(tempDir, extractedFile.getName() + Utils.PACKED_SUFFIX);
							ZipEntry zipEntry = new ZipEntry(name + Utils.PACKED_SUFFIX);
							entryStream = new FileInputStream(modifiedFile);
							zipOut.putNextEntry(zipEntry);
							Utils.transferStreams(entryStream, zipOut, false);
							entryStream.close();
							Utils.clear(modifiedFile);
						}
					}
					entryStream = new FileInputStream(extractedFile);
				}
				ZipEntry newEntry = new ZipEntry(name);
				zipOut.putNextEntry(newEntry);
				Utils.transferStreams(entryStream, zipOut, false);
				zipOut.closeEntry();
				entryStream.close();

				if (extractedFile != null)
					Utils.clear(extractedFile);
			}

		}
		zipOut.close();
		zip.close();

		File finalFile = new File(getWorkingDirectory(), zipFile.getName());
		if (finalFile.exists())
			finalFile.delete();
		outputFile.renameTo(finalFile);
		Utils.clear(tempDir);
	}

	private void initialize(ZipFile zip) {
		ZipEntry entry = zip.getEntry("pack.properties"); //$NON-NLS-1$
		properties = new Properties();
		if (entry != null) {
			InputStream stream = null;
			try {
				stream = zip.getInputStream(entry);
				properties.load(stream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				Utils.close(stream);
			}
		}

		packExclusions = Utils.getPackExclusions(properties);
		signExclusions = Utils.getSignExclusions(properties);

		packUnpackStep = new PackUnpackStep(properties);
		packStep = new PackStep(properties);
		signStep = new SignCommandStep(properties, command);
		unpackStep = new UnpackStep(properties);
	}
}
