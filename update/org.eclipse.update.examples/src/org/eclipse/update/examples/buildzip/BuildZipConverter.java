package org.eclipse.update.examples.buildzip;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.eclipse.update.core.ContentReference;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureContentProvider;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.JarContentReference;
import org.eclipse.update.internal.core.UpdateManagerUtils;

/**
 * Converts eclipse build .zip files into packaged feature
 */
public class BuildZipConverter {

	public static void main(String[] args) {
		if (args.length <=0) {
			System.out.println("usage: BuildZipConverter <url>");
			return;
		}
		try {
			URL buildzip = new URL("file:"+args[0]);
			BuildZipFeatureFactory factory = new BuildZipFeatureFactory();
			IFeature feature = factory.createFeature(buildzip, null);
			IFeatureContentProvider provider = feature.getFeatureContentProvider();
			
			ContentReference[] refs = provider.getFeatureEntryContentReferences(null);
			
			File site = new File((new File(args[0])).getParentFile(),".DefaultSite");
			site.mkdirs();
			File featuresDir = new File(site,"features");
			featuresDir.mkdirs();
			File pluginsDir = new File(site, "plugins");
			pluginsDir.mkdirs();
			
			File jarFile = new File(featuresDir,feature.getVersionIdentifier()+".jar");
			System.out.println("writing feature archive: "+feature.getVersionIdentifier()+".jar");
			writeJar(jarFile, refs, feature, null, null);
			JarContentReference jar = new JarContentReference("build.zip",buildzip);
			Properties manifest = getBuildManifest(jar);
			
			IPluginEntry[] plugins = feature.getPluginEntries();
			for (int i=0; i<plugins.length; i++) {
				refs = provider.getPluginEntryContentReferences(plugins[i], null);
				jarFile = new File(pluginsDir,plugins[i].getVersionIdentifier()+".jar");
				System.out.println("writing plugin archive: "+plugins[i].getVersionIdentifier()+".jar");
				writeJar(jarFile, refs, feature, plugins[i].getVersionIdentifier().getIdentifier(), manifest);
			}	
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
			
			// jar up the references
			for (int i=0; i<refs.length; i++) {
				String id = refs[i].getIdentifier();
				JarEntry entry = new JarEntry(id);
				jos.putNextEntry(entry);
				is = refs[i].getInputStream();
				UpdateManagerUtils.copy(is,jos,null);
				is.close(); is = null;
			}
			
			// for plugin jars, write build manifest
			if (pluginId != null && manifest != null) {
				String key = "plugin@"+pluginId;
				String value = manifest.getProperty(key);
				if (value != null) {
					if (value.equals("HEAD")) {
						value += "-" + feature.getVersionIdentifier().getVersion().getMajorComponent();
					}
					String buf = key + "=" + value;
					StringBufferInputStream sbis = new StringBufferInputStream(buf);
					JarEntry entry = new JarEntry("buildmanifest.properties");
					jos.putNextEntry(entry);
					UpdateManagerUtils.copy(sbis,jos,null);
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
