/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.examples.buildzip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.eclipse.update.core.*;
import org.eclipse.update.core.ContentReference;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureContentProvider;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.JarContentReference;
import org.eclipse.update.core.model.InstallAbortedException;

/**
 * Converts eclipse build .zip files into packaged feature
 */
public class BuildZipConverter {

	public static void main(String[] args) {
		if (args.length <=0) {
			System.out.println("Usage:     BuildZipConverter <path to the zip file>.\r\nExample: BuildZipConverter d:/download/integration-eclipse-SDK-20020109-win32.zip");
			return;
		}
		try {
			URL buildzip = new URL(("file:"+args[0]).replace(File.separatorChar, '/'));
			BuildZipFeatureFactory factory = new BuildZipFeatureFactory();
			IFeature feature = factory.createFeature(buildzip, null);
			IFeatureContentProvider provider = feature.getFeatureContentProvider();
			
			ContentReference[] refs = provider.getFeatureEntryContentReferences(null);
			
			File site = new File((args[0]+".packaged/").replace('/',File.separatorChar));
			site.mkdirs();
			File featuresDir = new File(site,"features");
			featuresDir.mkdirs();
			File pluginsDir = new File(site, "plugins");
			pluginsDir.mkdirs();
			
			File jarFile = new File(featuresDir,feature.getVersionedIdentifier()+".jar");
			System.out.println("writing feature archive: "+feature.getVersionedIdentifier()+".jar");
			writeJar(jarFile, refs, feature, null, null);
			JarContentReference jar = new JarContentReference("build.zip",buildzip);
			Properties manifest = getBuildManifest(jar);
			
			IPluginEntry[] plugins = feature.getPluginEntries();
			for (int i=0; i<plugins.length; i++) {
				refs = provider.getPluginEntryContentReferences(plugins[i], null);
				jarFile = new File(pluginsDir,plugins[i].getVersionedIdentifier()+".jar");
				System.out.println("writing plugin archive: "+plugins[i].getVersionedIdentifier()+".jar");
				writeJar(jarFile, refs, feature, plugins[i].getVersionedIdentifier().getIdentifier(), manifest);
			}
			
			writeSiteManifest(site, feature);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeJar(File jarFile,ContentReference[] refs, IFeature feature, String pluginId, Properties manifest) {
			
		JarOutputStream jos = null;
		FileOutputStream os = null;
		InputStream is = null;
		
		try {
			os = new FileOutputStream(jarFile);
			jos = new JarOutputStream(os);
			JarEntry entry = null;
			String buf = null;
			StringBufferInputStream sbis = null;
			
			// write jar manifest (first entry in output stream)
			buf = "Manifest-Version: 1.0\nCreated-By: 2.0.0 (www.eclipse.org Feature Packager)\n";
			sbis = new StringBufferInputStream(buf);
			entry = new JarEntry("META-INF/MANIFEST.MF");
			jos.putNextEntry(entry);
			Utilities.copy(sbis,jos,null);
			sbis.close();
			
			// jar up the references
			for (int i=0; i<refs.length; i++) {
				String id = refs[i].getIdentifier();
				entry = new JarEntry(id);
				jos.putNextEntry(entry);
				is = refs[i].getInputStream();
				Utilities.copy(is,jos,null);
				is.close(); is = null;
			}
			
			// for plugin jars, write build manifest
			if (pluginId != null && manifest != null) {
				String key = "plugin@"+pluginId;
				String value = manifest.getProperty(key);
				if (value != null) {
					if (value.equals("HEAD")) {
						value += "-" + feature.getVersionedIdentifier().getVersion().getMajorComponent();
					}
					buf = key + "=" + value;
					sbis = new StringBufferInputStream(buf);
					entry = new JarEntry("buildmanifest.properties");
					jos.putNextEntry(entry);
					Utilities.copy(sbis,jos,null);
					sbis.close();
				}
			}
			
			// close jar
			jos.close(); jos = null; os = null;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) 
				try {is.close();} catch(IOException e) {}
				
			if (jos != null) 
				try {jos.close();} catch(IOException e) {}
			else if (os != null)
				try {os.close();} catch(IOException e) {}
		}		
	}
	
	public static void writeSiteManifest(File site, IFeature feature) throws IOException, InstallAbortedException {
		File manifest = new File(site, "site.xml");
		FileOutputStream os = new FileOutputStream(manifest);
		String siteXML = "<site>\n   <feature url=\"features/"+feature.getVersionedIdentifier().toString()+".jar\"/>\n</site>";
		StringBufferInputStream sbis = new StringBufferInputStream(siteXML);
		Utilities.copy(sbis,os,null);
		os.close();
		sbis.close();
	}
	
	public static Properties getBuildManifest(JarContentReference jar) throws IOException {
		ContentReference manifestEntry = jar.peek("eclipse/buildmanifest.properties",null/*ContentSelector*/, null/*ProgressMonitor*/);
		InputStream is = null;
		Properties props = null;
		try {
			props = new Properties();
			is = manifestEntry.getInputStream();
			props.load(is);
		} finally {
			if (is != null) try{ is.close(); } catch(IOException e) {}
		}
		return props;
	}
}
