package org.eclipse.update.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.FeatureModelFactory;
import org.xml.sax.SAXException;
import org.eclipse.update.internal.core.Policy;

public class FeaturePackagedFactory extends BaseFeatureFactory {

	/*
	 * @see IFeatureFactory#createFeature(URL,ISite)
	 */
	public IFeature createFeature(URL url,ISite site) throws CoreException {
		Feature feature = null;
		InputStream featureStream = null;
		
		try {		
			
			IFeatureContentProvider contentProvider = new FeaturePackagedContentProvider(url);		
			ContentReference manifest = contentProvider.getFeatureManifestReference(null/*IProgressMonitor*/);
			featureStream = manifest.getInputStream();
			feature = (Feature)parseFeature(featureStream);
			feature.setFeatureContentProvider(contentProvider);
			feature.setSite(site);						
			URL baseUrl = null;
			try {
				baseUrl = new URL(manifest.asURL(),"."); // make sure we have URL to feature directory //$NON-NLS-1$
			} catch(IOException e) {
			}
			feature.resolve(baseUrl, getResourceBundle(baseUrl));
			feature.markReadOnly();			
			
		} catch (IOException e) {
			// if we cannot find the feature or the feature.xml...
			// We should not stop the execution 
			// but we must Log it all the time, not only when debugging...
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "IOException opening the 'feature.xml' stream in the feature archive:"  + url.toExternalForm(), e); //$NON-NLS-1$
			UpdateManagerPlugin.getPlugin().getLog().log(status);
		} catch (Exception e) { 
			// other errors, we stop execution
			if (e instanceof CoreException) {
				throw (CoreException)e;
			};
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, Policy.bind("FeatureFactory.ParsingError", url.toExternalForm()), e); //$NON-NLS-1$
			throw new CoreException(status);
		}finally {
			try {
				if (featureStream!=null)
					featureStream.close();
			} catch (Exception e) {
			}
		}
		return feature;
	}

}
