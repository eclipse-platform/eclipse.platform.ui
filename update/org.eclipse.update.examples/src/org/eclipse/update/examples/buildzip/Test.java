package org.eclipse.update.examples.buildzip;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.net.URL;

import org.eclipse.update.core.ContentReference;
import org.eclipse.update.core.Feature;
import org.eclipse.update.core.INonPluginEntry;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.model.DefaultModelWriter;
 
/**
 * unit test harness
 */

public class Test {

	public static void main(String[] args) {
		
		try {
			URL url = new URL("file:d:/downloads/eclipse 2.0/integration-eclipse-SDK-20020109-win32.zip");
			BuildZipFeatureFactory factory = new BuildZipFeatureFactory();
			Feature feature = (Feature) factory.createFeature(url,null/*ISite*/);
			DefaultModelWriter w = new DefaultModelWriter(feature);
			w.writeFeatureManifest(System.out);
			
			System.out.println("");
			System.out.println("Feature entry references");
			ContentReference[] refs = feature.getFeatureContentProvider().getFeatureEntryContentReferences();
			for (int i=0; i< refs.length; i++) {
				System.out.println("   "+refs[i].getIdentifier());
			}
			
			System.out.println("");
			System.out.println("Plug-in entry references");
			IPluginEntry[] entry = feature.getPluginEntries();
			refs = feature.getFeatureContentProvider().getPluginEntryContentReferences(entry[0]);
			for (int i=0; i< refs.length; i++) {
				System.out.println("   "+refs[i].getIdentifier());
			}			
			
			System.out.println("");
			System.out.println("Non-plug-in entry references");
			INonPluginEntry[] data = feature.getNonPluginEntries();
			refs = feature.getFeatureContentProvider().getNonPluginEntryArchiveReferences(data[0]);
			for (int i=0; i< refs.length; i++) {
				System.out.println("   "+refs[i].getIdentifier());
			}			
			
			System.out.println("Done ...");
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
