/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.update.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.ContentReference;
import org.eclipse.update.core.IFeatureContentConsumer;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.PluginEntry;
import org.eclipse.update.core.Site;

public class JarDeltaInstallHandler extends DeltaInstallHandler {

	
	protected void overlayPlugin(
			IPluginEntry oldPlugin,
			IPluginEntry newPlugin,
			IFeatureContentConsumer consumer)
			throws CoreException, IOException {
		
			if(newPlugin instanceof PluginEntry && ((PluginEntry)newPlugin).isUnpack()){
				// partial plug-ins (in patches) must always be unpacked
				super.overlayPlugin(oldPlugin, newPlugin, consumer);
			}
			
			ContentReference[] oldReferences = oldFeature.getFeatureContentProvider().getPluginEntryContentReferences(oldPlugin, null);
			ContentReference[] newReferences = feature.getFeatureContentProvider().getPluginEntryContentReferences(newPlugin, null);
			
			URI oldURI = null;
			try {
				oldURI = new URI(consumer.getFeature().getSite().getURL().getPath() + 
									 Site.DEFAULT_PLUGIN_PATH + 
									 oldPlugin.getVersionedIdentifier().toString());
			} catch (URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
			File oldJarFile = new File(oldURI);
			JarFile oldJar = new JarFile(oldJarFile);
			
			URI newURI = null;
			try {
				newURI = new URI(consumer.getFeature().getSite().getURL().getPath() + 
								 Site.DEFAULT_PLUGIN_PATH + 
								 newPlugin.getVersionedIdentifier().toString());
			} catch (URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
			File newJarFile = new File(newURI);
			JarFile newJar = new JarFile(newJarFile);

			String tempFileName = oldURI + "-" + (new Date()).getTime(); //$NON-NLS-1$
			File tempFile = new File(tempFileName);
			FileOutputStream fos = new FileOutputStream(tempFile);
			JarOutputStream jos = new JarOutputStream( fos);
			
			addToJar(jos, newJar);
			addToJar(jos, oldJar);
			
			jos.closeEntry();
			jos.finish();
			fos.close();
			newJar.close();
			oldJar.close();
			
			newJarFile = new File(newURI);
			newJarFile.delete();
			
			newJarFile.createNewFile();
			
			FileChannel in = new FileInputStream(tempFile).getChannel();
			FileChannel out = new FileOutputStream(newJarFile).getChannel();
			in.transferTo( 0, in.size(), out);
			
			in.close();
			out.close();
	}
	
	public static void addToJar(JarOutputStream jos, JarFile jf) throws IOException {
		Enumeration e = jf.entries();
		
		while(e.hasMoreElements()) {
			
			ZipEntry je = (ZipEntry)e.nextElement();
			InputStream io = jf.getInputStream(je);
			
			byte b[] = new byte[4096];
			int read = 0;
			try {
				jos.putNextEntry(je);
				while( ( read = io.read(b, 0, 4096)) != -1) {
					jos.write(b, 0, read);
				}
			} catch (ZipException ze) {
				//ze.printStackTrace();
				throw ze;
			}		
		}
	}
}
