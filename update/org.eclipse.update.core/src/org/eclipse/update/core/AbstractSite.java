package org.eclipse.update.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.internal.core.DefaultSiteParser;


public abstract class AbstractSite implements ISite {

	private static final String SITE_XML= "site.xml";
	private boolean isManageable = false;
	private DefaultSiteParser parser;
	
	/**
	 * teh tool will create the directories on the file 
	 * system if needed.
	 */
	public static boolean CREATE_PATH = true;

	private ListenersList listeners = new ListenersList();
	private URL siteURL;
	private List features;

	/**
	 * Constructor for AbstractSite
	 */
	public AbstractSite(URL siteReference) {
		super();
		this.siteURL = siteReference;
		init();
	}
	
	/**
	 * Initializes the site by reading the site.xml file
	 */
	private void init(){
		InputStream inStream = null;
		try {
			URL siteXml = new URL(siteURL,"site.xml");
			parser = new DefaultSiteParser(siteXml.openStream(),this);
			isManageable = true;		 	
		} catch (org.xml.sax.SAXException e){
			//FIXME: who should handle XML exception  the parser ? 
			// ok but what should it do ? just put null in result ?
			// send and transform teh exception ?
			e.printStackTrace();
		} catch (FileNotFoundException e){
			// log not manageable site
			if (UpdateManagerPlugin.getDefault().DEBUG && UpdateManagerPlugin.getDefault().DEBUG_SHOW_WARNINGS){
				System.out.println(siteURL.toExternalForm()+" is not manageable by Update Manager: Couldn't find the site.xml file.");
			}
		} catch (MalformedURLException e){
			// FIXME:
			e.printStackTrace();
		} catch (IOException e){
			// FIXME:
			e.printStackTrace();
		} finally {
			try {
			 inStream.close();
			} catch (Exception e){}
		}
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
	 * @see ISite#install(IFeature, IProgressMonitor)
	 */
	public void install(IFeature sourceFeature, IProgressMonitor monitor) throws CoreException {
		// should start UOW and manage Progress Monitor
		AbstractFeature localFeature = createExecutableFeature(sourceFeature);
		sourceFeature.install(localFeature);
		this.addFeature(localFeature);
	}
	
	/**
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor monitor)
		throws CoreException {
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
		IFeature[] result = null;
		if (isManageable){
			if (features==null) initializeFeatures();
			//FIXME: I do not like this pattern.. List or Array ???
			if (!features.isEmpty()){
				result = (IFeature[])features.toArray(new IFeature[features.size()]);
			}
		}
		return result;
	}
	
	/**
	 * Read the Features from the XML file
	 */
	private void initializeFeatures(){
		features = parser.getFeatures();
	}
	
	/**
	 * adds a feature
	 * @param feature The feature to add
	 */
	private void addFeature(IFeature feature) {
		if (features==null){
			features = new ArrayList(0);
		}
		this.features.add(feature);
	}

}