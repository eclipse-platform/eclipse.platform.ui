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

import java.net.URL;

import org.eclipse.update.core.*;
 
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
			ContentReference[] refs = feature.getFeatureContentProvider().getFeatureEntryContentReferences(null);
			for (int i=0; i< refs.length; i++) {
				System.out.println("   "+refs[i].getIdentifier());
			}
			
			System.out.println("");
			System.out.println("Plug-in entry references");
			IPluginEntry[] entry = feature.getPluginEntries();
			refs = feature.getFeatureContentProvider().getPluginEntryContentReferences(entry[0], null);
			for (int i=0; i< refs.length; i++) {
				System.out.println("   "+refs[i].getIdentifier());
			}			
			
			System.out.println("");
			System.out.println("Non-plug-in entry references");
			INonPluginEntry[] data = feature.getNonPluginEntries();
			refs = feature.getFeatureContentProvider().getNonPluginEntryArchiveReferences(data[0], null);
			for (int i=0; i< refs.length; i++) {
				System.out.println("   "+refs[i].getIdentifier());
			}			
			
			System.out.println("Done ...");
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
