package org.eclipse.update.core;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractSite implements ISite {

	
	private ListenersList listeners = new ListenersList();
	private URL siteURL;
	private IFeature[] features;

	/**
	 * Constructor for AbstractSite
	 */
	public AbstractSite(URL siteReference) {
		super();
		this.siteURL = siteReference;
	}
	
	/**
	 * @see ISite#addSiteChangedListener(ISiteChangedListener)
	 */
	public void addSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners){
			listeners.add(listener);
		}
	}
	
	/**
	 * @see ISite#removeSiteChangedListener(ISiteChangedListener)
	 */
	public void removeSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners){
			listeners.remove(listener);
		}
	}	

	/**
	 * 
	 */
	public abstract AbstractFeature createExecutableFeature(IFeature sourceFeature);

	/**
	 *
	 */
	public abstract InputStream getInputStream(
		IFeature sourceFeature,
		String streamKey);

	/**
	 * Gets the siteURL
	 * @return Returns a URL
	 */
	public URL getURL() {
		return siteURL;
	}

	/**
	* This method also closes both streams.
	* Taken from FileSystemStore
	*/
	protected void transferStreams(InputStream source, OutputStream destination)
		throws IOException {
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
	 * Gets the features
	 * @return Returns a IFeature[]
	 */
	public IFeature[] getFeatures() {
		return features;
	}
	/**
	 * Sets the features
	 * @param features The features to set
	 */
	protected void setFeatures(IFeature[] features) {
		this.features = features;
	}

}