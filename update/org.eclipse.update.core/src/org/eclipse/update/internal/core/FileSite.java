package org.eclipse.update.internal.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.AbstractSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISiteChangedListener;

public class FileSite extends URLSite {

	/**
	 * Constructor for FileSite
	 */
	public FileSite(URL siteReference) {
		super(siteReference);
	}


	private String getPath(){
		return getURL().getPath();
	}


	/**
	 * @see AbstractSite#createExecutableFeature(IFeature)
	 */
	public AbstractFeature createExecutableFeature(IFeature sourceFeature) {
		return new DefaultExecutableFeature(sourceFeature,this);
	}



	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(
		IPluginEntry pluginEntry,
		String contentKey,
		InputStream inStream) {
		try {
			// create plugin if doesn't exist
			String pluginPath = getPath()+pluginEntry.getIdentifier().toString();
			File pluginDirectory = new File(pluginPath);
			if (!pluginDirectory.exists()){
				pluginDirectory.mkdir();
			}

			// create directory if doesn't exist
			int lastSeparator = contentKey.lastIndexOf(File.separator);
			String path = contentKey.substring(0,lastSeparator);
			StringTokenizer tokenizer = new StringTokenizer(path,File.separator);
			String currentPath = pluginPath+File.separator;
			while (tokenizer.hasMoreTokens()){
				File currentDir = new File(currentPath+tokenizer.nextToken());
				if (!currentDir.exists()) currentDir.mkdir();
				currentPath = currentPath+currentDir+File.separator;
			}

			// create file
			FileOutputStream currentFile = new FileOutputStream(pluginPath+File.separator+contentKey);
			transferStreams(inStream,currentFile);

		} catch (IOException e) {
			//FIXME: 
			e.printStackTrace();
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {
			}
		}
	}

}