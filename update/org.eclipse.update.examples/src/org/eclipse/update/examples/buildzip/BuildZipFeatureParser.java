package org.eclipse.update.examples.buildzip;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 
 
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.update.core.ContentReference;
import org.eclipse.update.core.Version;
import org.eclipse.update.core.model.FeatureModel;
import org.eclipse.update.core.model.NonPluginEntryModel;
import org.eclipse.update.core.model.PluginEntryModel;
import org.eclipse.update.core.model.URLEntryModel;

/**
 * An example feature parser. It handles features packaged as
 * build zip's using the format used for integration and stable builds
 * posted on the downloads pages at www.eclipse.org
 * </p>
 * @since 2.0
 */

public class BuildZipFeatureParser {	
	
	private BuildZipFeatureFactory factory;
	private BuildZipPluginParser pluginParser;
	
	public BuildZipFeatureParser(BuildZipFeatureFactory factory) {
		this.factory = factory;
	}
	
	public FeatureModel parse(BuildZipContentProvider cp) throws Exception {				
		return parseFeature(cp);
	}
	
	private FeatureModel parseFeature(BuildZipContentProvider cp) throws Exception {		
		
		// get reference to the build manifest
		ContentReference manifestRef = cp.getFeatureBuildManifest();
		InputStream is = manifestRef.getInputStream();		
		
		// load buildmanifest.properties
		Properties manifest = new Properties();
		try {
			manifest.load(is);
		} finally {
			if (is!=null) try { is.close(); } catch(IOException e) {}
		}
		
		// populate feature model
		FeatureModel feature = factory.createFeatureModel();
		parseFeatureEntry(cp, feature, manifest);
		
		// parse plugin entries
		parsePluginEntries(cp, feature, manifest); 
		
		// parse non-plugin entries
		parseNonPluginEntries(cp, feature, manifest);
		
		// unpack feature entry content
		cp.unpackFeatureEntryContent(feature);
		
		return feature;
	}
	
	private void parseFeatureEntry(BuildZipContentProvider cp, FeatureModel feature, Properties manifest) throws Exception {
		
		// parse out build zip file name. It is assumed to be in the
		// form <package identifier>-<build date>-<target platform>.zip,
		// where
		//		<package identifier> is hyphen (-) separaterd token list
		//		<build date> is single all-numeric token
		//		<target platform> is hyphen (-) separaterd token list
		
		String pkgId = "";
		String target = "";
		String build = "";
		String state = "ID";
		boolean firstToken = true;
		String zipName = cp.getURL().getFile();
		int ix = zipName.lastIndexOf("/");
		if (ix != -1)
			zipName = zipName.substring(ix+1);
		if (zipName.endsWith(".zip")) {
			zipName = zipName.substring(0,zipName.length()-4);
		}
		StringTokenizer tokenizer = new StringTokenizer(zipName,"-",false);
		String token;
		while(tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			try {
				Long.parseLong(token);
				state = "TARGET";
				target = "";
				firstToken = true;
				build = token+".0.0";
			} catch (NumberFormatException e) {
				if (state.equals("ID")) {
					pkgId += (firstToken ? "" : " ") + token;
				} else {
					target += (firstToken ? "" : " ") + token;
				}
				firstToken = false;
			}
		}
		
		// generate base feature attributes
		feature.setFeatureIdentifier(pkgId.replace(' ','.'));
		feature.setFeatureVersion(build);
		feature.setLabel(pkgId.substring(0,1).toUpperCase() + pkgId.substring(1) + " for " + target);
		feature.setProvider("www.eclipse.org");
		feature.setImageURLString("splash/splash_full.bmp");
		feature.setOS(target.replace(' ','-'));	
		
		// add description
		URLEntryModel description = factory.createURLEntryModel();
		description.setURLString("readme/readme.html");
		feature.setDescriptionModel(description);
		
		// add license
		URLEntryModel license = factory.createURLEntryModel();
		license.setURLString("about.html");
		feature.setLicenseModel(license);
		
		// add copyright
		URLEntryModel copyright = factory.createURLEntryModel();
		copyright.setURLString("notice.html");
		feature.setCopyrightModel(copyright);
	}	
	
	private void parsePluginEntries(BuildZipContentProvider cp, FeatureModel feature, Properties manifest) throws Exception {
		
		Enumeration entries = manifest.keys();
		String plugin;
		String pluginId;
		String pluginVersion;
		String pluginBuildVersion;
		PluginEntryModel pluginEntry;
		while(entries.hasMoreElements()) {
			plugin = (String) entries.nextElement();
			if (plugin.startsWith("plugin@")) {
				pluginId = plugin.substring(7);
				pluginBuildVersion = manifest.getProperty(plugin);
				pluginEntry = factory.createPluginEntryModel();
				pluginVersion = parsePluginVersionInManifest(cp, pluginId, pluginEntry);
				if (pluginVersion != null) {
					pluginEntry.setPluginIdentifier(pluginId);
					pluginEntry.setPluginVersion((new Version(pluginVersion)).toString()+"."+pluginBuildVersion);
					feature.addPluginEntryModel(pluginEntry);
				}
			}
		}		
	}
	
	private void parseNonPluginEntries(BuildZipContentProvider cp, FeatureModel feature, Properties manifest) throws Exception {
		NonPluginEntryModel nonPluginEntry = factory.createNonPluginEntryModel();
		nonPluginEntry.setIdentifier("root");
		feature.addNonPluginEntryModel(nonPluginEntry);
	}
	
	private String parsePluginVersionInManifest(BuildZipContentProvider cp, String pluginId, PluginEntryModel pluginEntry) {

		if (pluginParser == null)
			pluginParser = new BuildZipPluginParser();
				
		InputStream is = null;				
		ContentReference pluginManifest;
		try {
			// try plugin.xml
			pluginManifest  = cp.getPluginManifest(pluginId, false);
			is = pluginManifest.getInputStream();
			return pluginParser.parse(is);
		} catch (Exception e) {
			try {
				// retry with feature.xml
				pluginManifest  = cp.getPluginManifest(pluginId, true);
				is = pluginManifest.getInputStream();
				String result = pluginParser.parse(is);
				pluginEntry.isFragment(true);
				return result;
			} catch (Exception e2) {
				return null;
			}
		} finally {
			if (is != null) try { is.close(); } catch (IOException e) {}
		}
	}
}
