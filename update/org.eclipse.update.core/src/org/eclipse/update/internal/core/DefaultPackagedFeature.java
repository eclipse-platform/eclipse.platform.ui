package org.eclipse.update.internal.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.AbstractSite;
import org.eclipse.update.core.Assert;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.VersionedIdentifier;

public class DefaultPackagedFeature extends AbstractFeature {

	private JarFile currentOpenJarFile = null;

	public static final String JAR_EXTENSION = ".jar";

	/**
	 * Constructor for DefaultPackagedFeature
	 */
	public DefaultPackagedFeature(IFeature sourceFeature, ISite targetSite) {
		super(sourceFeature, targetSite);
	}

	/**
	 * Constructor for DefaultPackagedFeature
	 */
	public DefaultPackagedFeature(
		VersionedIdentifier identifier,
		ISite targetSite) {
		super(identifier, targetSite);
	}

	/**
	 * @see AbstractFeature#getContentReferenceToInstall(IPluginEntry[])
	 */
	public String[] getContentReferenceToInstall(IPluginEntry[] pluginsToInstall) {
		String[] names = null;
		if (pluginsToInstall != null) {
			names = new String[pluginsToInstall.length];
			for (int i = 0; i < pluginsToInstall.length; i++) {
				names[i] = getArchiveID(pluginsToInstall[i]);
			}
		}
		return names;
	}

	/**
	 * @see AbstractFeature#getInputStreamFor(String)
	 */
	public InputStream getInputStreamFor(IPluginEntry pluginEntry, String name) {
		URL siteURL = getSite().getURL();
		InputStream result = null;
		try {
			// default			
			String filePath =
				siteURL.getPath()
					+ AbstractSite.DEFAULT_PLUGIN_PATH
					+ getArchiveID(pluginEntry);
			URL fileURL =
				((AbstractSite) getSite()).getArchiveURLfor(getArchiveID(pluginEntry));
			if (fileURL != null) {
				// has to be local, file ?
				filePath = fileURL.getPath();
			}

			if (currentOpenJarFile != null) {
				if (!currentOpenJarFile.getName().equals(filePath)) {
					currentOpenJarFile.close();
					currentOpenJarFile = new JarFile(filePath);
				} else {
					// same file do nothing
				}
			} else {
				currentOpenJarFile = new JarFile(filePath);
			}

			if (!(new File(filePath)).exists())
				throw new IOException("The File:" + filePath + "does not exist.");
			ZipEntry entry = currentOpenJarFile.getEntry(name);
			result = currentOpenJarFile.getInputStream(entry);
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		} catch (IOException e) {
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames(IPluginEntry)
	 */
	public String[] getStorageUnitNames(IPluginEntry pluginEntry) {
		URL siteURL = getSite().getURL();
		JarFile jarFile = null;
		String[] result = null;
		try {
			// get the URL of the JAR file that contains teh plugin entry
			URL jarURL =
				((AbstractSite) getSite()).getArchiveURLfor(getArchiveID(pluginEntry));
			if (jarURL == null) {
				// default path
				jarURL =
					new URL(siteURL, AbstractSite.DEFAULT_PLUGIN_PATH + getArchiveID(pluginEntry));
			}
			jarFile = new JarFile(jarURL.getPath());
			result = new String[jarFile.size()];
			Enumeration enum = jarFile.entries();
			int loop = 0;
			while (enum.hasMoreElements()) {
				ZipEntry nextEntry = (ZipEntry) enum.nextElement();
				result[loop] = (String) nextEntry.getName();
				loop++;
			}
			jarFile.close();
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		} catch (IOException e) {
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getFeatureInputStream()
	 * The feature url is pointing at the JAR
	 * download the JAR in the TEMP dir 
	 * Change the URL to be JAR url jar:file://<filename>!/
	 * and get the feature.xml
	 */
	public InputStream getFeatureInputStream() throws IOException {

		InputStream result = null;
		transferLocally();
		// get the stream inside the JAR
		URL insideURL = null;
		try {
			String newURLString = getURL() + FEATURE_XML;
			insideURL = new URL("jar:file://" + getURL().getPath() + "!/" + FEATURE_XML);
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		}

		return insideURL.openStream();
	}

	/**
	 * Transfer feature.jar file locally
	 */
	private void transferLocally() throws IOException {
		// 
		//
		if (!(getURL() == null || getURL().getProtocol().equals("file"))) {
			InputStream sourceContentReferenceStream =
				getURL() == null ? null : getURL().openStream();
			if (sourceContentReferenceStream != null) {
				// install in DEFAULT PATH for feature
				// as we OWN the temp site

				AbstractSite tempSite = (AbstractSite) SiteManager.getTempSite();
				String newFile =
					tempSite.getURL().getPath()
						+ tempSite.DEFAULT_FEATURE_PATH
						+ getIdentifier().toString()
						+ JAR_EXTENSION;

				// FIXME: better solution ?
				File dir =
					new File(tempSite.getURL().getPath() + tempSite.DEFAULT_FEATURE_PATH);
				if (!dir.exists())
					dir.mkdirs();

				FileOutputStream localContentReferenceStream = new FileOutputStream(newFile);
				transferStreams(sourceContentReferenceStream, localContentReferenceStream);
				this.setURL(new URL("file://" + newFile));

				//TRACE:
				System.out.println("-----------------------" + newFile);

			} else {
				throw new IOException("Couldn\'t find the file: " + getURL().toExternalForm());
			}
		}
	}

	/**
	 * This method also closes both streams.
	 * Taken from FileSystemStore
	 */
	private void transferStreams(InputStream source, OutputStream destination)
		throws IOException {

		Assert.isNotNull(source);
		Assert.isNotNull(destination);

		try {
			byte[] buffer = new byte[8192];
			while (true) {
				int bytesRead = source.read(buffer);
				if (bytesRead == -1)
					break;
				destination.write(buffer, 0, bytesRead);
			}
		} finally {
			try {
				source.close();
			} catch (IOException e) {
			}
			try {
				destination.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * return the archive ID for a plugin
	 */
	private String getArchiveID(IPluginEntry entry) {
		return entry.getIdentifier().toString() + JAR_EXTENSION;
	}

	/**
	 * @see AbstractFeature#getContentReferences()
	 */
	public String[] getContentReferences() {
		String[] names = new String[getPluginEntryCount()];
		for (int i = 0; i < getPluginEntryCount(); i++) {
			names[i] = getArchiveID(getPluginEntries()[i]);
		}
		return names;
	}

	/**
	 * @see AbstractFeature#isInstallable()
	 */
	public boolean isInstallable() {
		return true;
	}

	/**
	 * @see AbstractFeature#getInputStreamFor(String)
	 */
	protected InputStream getInputStreamFor(String name) {
		URL siteURL = getSite().getURL();
		InputStream result = null;

		try {
			transferLocally();

			// default			
			String filePath =
				siteURL.getPath()
					+ AbstractSite.DEFAULT_FEATURE_PATH
					+ getIdentifier().toString()
					+ JAR_EXTENSION;
			URL fileURL = getURL();
			if (fileURL != null) {
				// has to be local, file ?
				filePath = fileURL.getPath();
			}

			if (currentOpenJarFile != null) {
				if (!currentOpenJarFile.getName().equals(filePath)) {
					currentOpenJarFile.close();
					currentOpenJarFile = new JarFile(filePath);
				} else {
					// same file do nothing
				}
			} else {
				currentOpenJarFile = new JarFile(filePath);
			}

			if (!(new File(filePath)).exists())
				throw new IOException("The File:" + filePath + "does not exist.");
			ZipEntry entry = currentOpenJarFile.getEntry(name);
			result = currentOpenJarFile.getInputStream(entry);
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		} catch (IOException e) {
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @see AbstractFeature#getStorageUnitNames()
	 */
	protected String[] getStorageUnitNames() {
		URL siteURL = getSite().getURL();
		JarFile jarFile = null;
		String[] result = null;

		try {

			transferLocally();

			// get the URL of the feature JAR file
			URL jarURL = getURL();
			if (jarURL == null) {
				// default path
				jarURL =
					new URL(
						siteURL,
						AbstractSite.DEFAULT_FEATURE_PATH + getIdentifier().toString() + JAR_EXTENSION);
			}
			jarFile = new JarFile(jarURL.getPath());
			result = new String[jarFile.size()];
			Enumeration enum = jarFile.entries();
			int loop = 0;
			while (enum.hasMoreElements()) {
				ZipEntry nextEntry = (ZipEntry) enum.nextElement();
				result[loop] = (String) nextEntry.getName();
				loop++;
			}
			jarFile.close();
		} catch (MalformedURLException e) {
			//FIXME:
			e.printStackTrace();
		} catch (IOException e) {
			//FIXME:
			e.printStackTrace();
		}
		return result;
	}

}