package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.custom.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IInstallConfiguration;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.ISiteChangedListener;
import org.eclipse.update.core.VersionedIdentifier;
import java.util.*;
import org.eclipse.swt.graphics.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class LocalSiteView extends BaseTreeView {
	
class FolderObject {
	String label;
	
	public FolderObject(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	public String toString() {
		return getLabel();
	}
}

FolderObject myEclipse = new FolderObject("Installed Features");
FolderObject updates = new FolderObject("Available Updates");
ILocalSite fakeLocalSite;

Image eclipseImage;
Image updatesImage;
Image featureImage;
	
class LocalSiteProvider extends DefaultContentProvider 
						implements ITreeContentProvider {
	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof UpdateModel) {
			return new Object [] { myEclipse, updates };
		}
		if (parent == myEclipse) {
			return openLocalSite();
		}
		if (parent == updates) {
			return checkForUpdates();
		}
		return new Object[0];
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object child) {
		return null;
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object parent) {
		if (parent instanceof FolderObject) return true;
		return false;
	}
	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object input) {
		return getChildren(input);
	}
}

class LocalSiteLabelProvider extends LabelProvider {
	public Image getImage(Object obj) {
		if (obj.equals(myEclipse))
		   return eclipseImage;
		if (obj.equals(updates))
		   return updatesImage;
		if (obj instanceof IFeature)
		   return featureImage;
		return null;
	}
}

public LocalSiteView() {
	fakeLocalSite = createFakeLocalSite();
	eclipseImage = UpdateUIPluginImages.DESC_ECLIPSE_OBJ.createImage();
	updatesImage = UpdateUIPluginImages.DESC_UPDATES_OBJ.createImage();
	featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
}

public void initProviders() {
	viewer.setContentProvider(new LocalSiteProvider());
	viewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
	viewer.setLabelProvider(new LocalSiteLabelProvider());
}

private Object [] openLocalSite() {
	return fakeLocalSite.getFeatures();
}

private Object [] checkForUpdates() {
	BusyIndicator.showWhile(viewer.getTree().getDisplay(),
					new Runnable() {
		public void run() {
			try {
			   Thread.currentThread().sleep(2000);
			}
			catch (InterruptedException e) {
			}
		}
	});
	return new Object[0];
}

class FakeFeature implements IFeature {
	private VersionedIdentifier id;
	private ISite site;
	private String label;
	private URL infoURL;
	private String description;
	private String provider;
	/**
	 * @see IFeature#getIdentifier()
	 */
	public FakeFeature(ISite site, String id, 
		String version, String label, URL infoURL, String provider, 
		String description) {
		this.site = site;
		this.id = new VersionedIdentifier(id, version);
		this.label = label;
		this.provider = provider;
		this.description = description;
		this.infoURL = infoURL;
	}
	
	public String toString() {
		return label;
	}
	
	public VersionedIdentifier getIdentifier() {
		return id;
	}

	/**
	 * @see IFeature#getSite()
	 */
	public ISite getSite() {
		return site;
	}

	/**
	 * @see IFeature#getLabel()
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * @see IFeature#getUpdateURL()
	 */
	public IInfo getUpdateInfo() {
		return null;
	}
	
	
	public URL getURL() {
		return null;
	}
		
	public URL getInfoURL() {
		return infoURL;
	}
	
	public void setInfoURL(URL infoURL) {
		this.infoURL = infoURL;
	}

	public IInfo getLicense() {
		return null;
	}
	
	public IInfo getCopyright() {
		return null;
	}

	/**
	 * @see IFeature#getDiscoveryURLs()
	 */
	public IInfo[] getDiscoveryInfos() {
		return null;
	}

	/**
	 * @see IFeature#getProvider()
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * @see IFeature#getDescription()
	 */
	public IInfo getDescription() {
		return new IInfo() {
			public String getText() {
				return description;
			}
			public URL getURL() {
				return null;
			}
		};
	}

	/**
	 * @see IFeature#isExecutable()
	 */
	public boolean isExecutable() {
		return true;
	}

	/**
	 * @see IFeature#isInstallable()
	 */
	public boolean isInstallable() {
		return false;
	}

	/**
	 * @see IFeature#getContentReferences()
	 */
	public String[] getContentReferences() {
		return null;
	}

	/**
	 * @see IFeature#install(IFeature)
	 */
	public void install(IFeature targetFeature) {
	}

	/**
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		return null;
	}

	/**
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		return 0;
	}

	/**
	 * @see IPluginContainer#getDownloadSize(IPluginEntry)
	 */
	public int getDownloadSize(IPluginEntry entry) {
		return 0;
	}

	/**
	 * @see IPluginContainer#getInstallSize(IPluginEntry)
	 */
	public int getInstallSize(IPluginEntry entry) {
		return 0;
	}

	/**
	 * @see IPluginContainer#addPluginEntry(IPluginEntry)
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
	}

	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(
		IPluginEntry pluginEntry,
		String contentKey,
		InputStream inStream) {
	}

}

class FakeLocalSite implements ILocalSite {
	private Vector features = new Vector();
	
	public FakeLocalSite() {
		URL ibmURL;
		URL otiURL;
		
		try {
			ibmURL = new URL("http://www.ibm.com");
			otiURL = new URL("http://www.oti.com");
		}
		catch (Exception e) {
			ibmURL = null;
			otiURL = null;
		}
		features.add(
		new FakeFeature(this, "org.eclipse.platform", 
		"1.0.0", "Eclipse Base Platform", 
		otiURL, "Object Technology International Inc.",
"The Platform project provides the core frameworks and "+
"services upon which all plug-in extensions are created. "+
"It also provides the runtime in which plug-ins are loaded, "+
"integrated, and executed. The Platform project's purpose is "+
"to enable other tool developers to build and deliver really "+
"nice integrated tools."
		));
		features.add(
		new FakeFeature(this, "org.eclipse.jdt", 
		"1.0.0", "Java Development Tooling (JDT)", 
		otiURL, "Object Technology International Inc.",
"The JDT project provides tool plug-ins that implement a Java "+
"IDE that supports the development of Java applications including "+
"Eclipse plug-ins. The JDT project allows Eclipse to be a "+
"development environment for itself."
		));
		features.add(
		new FakeFeature(this, "org.eclipse.pde", 
		"1.0.0", "Plug-in Development Environment (PDE)", 
		ibmURL, "International Business Machines Corp.",
"The PDE project extends the Platform and the JDT to provide "+
"views and editors that make it easier to build plug-ins for "+
"Eclipse. The PDE helps you figure out what extension points "+
"are available, how to plug into them, and helps you put together "+
"your code in plug-in format. The PDE makes integrating plug-ins "+
"easy and fun."
		));
	}
	/**
	 * @see ILocalSite#getCurrentConfiguration()
	 */
	public IInstallConfiguration getCurrentConfiguration() {
		return null;
	}

	/**
	 * @see ILocalSite#getConfigurationHistory()
	 */
	public IInstallConfiguration[] getConfigurationHistory() {
		return null;
	}

	/**
	 * @see ISite#getFeatures()
	 */
	public IFeature[] getFeatures() {
		return (IFeature[])features.toArray(new IFeature[features.size()]);
	}

	/**
	 * @see ISite#install(IFeature, IProgressMonitor)
	 */
	public void install(IFeature feature, IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor monitor)
		throws CoreException {
	}

	/**
	 * @see ISite#addSiteChangedListener(ISiteChangedListener)
	 */
	public void addSiteChangedListener(ISiteChangedListener listener) {
	}

	/**
	 * @see ISite#removeSiteChangedListener(ISiteChangedListener)
	 */
	public void removeSiteChangedListener(ISiteChangedListener listener) {
	}

	/**
	 * @see ISite#getURL()
	 */
	public URL getURL() {
		return null;
	}
	
	public ICategory [] getCategories() {
		return null;
	}

	/**
	 * @see IPluginContainer#getPluginEntries()
	 */
	public IPluginEntry[] getPluginEntries() {
		return null;
	}
	
	public URL getInfoURL() {
		return null;
	}

	/**
	 * @see IPluginContainer#getPluginEntryCount()
	 */
	public int getPluginEntryCount() {
		return 0;
	}

	/**
	 * @see IPluginContainer#getDownloadSize(IPluginEntry)
	 */
	public int getDownloadSize(IPluginEntry entry) {
		return 0;
	}

	/**
	 * @see IPluginContainer#getInstallSize(IPluginEntry)
	 */
	public int getInstallSize(IPluginEntry entry) {
		return 0;
	}

	/**
	 * @see IPluginContainer#addPluginEntry(IPluginEntry)
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
	}

	/**
	 * @see IPluginContainer#store(IPluginEntry, String, InputStream)
	 */
	public void store(
		IPluginEntry pluginEntry,
		String contentKey,
		InputStream inStream) {
	}
}

private ILocalSite createFakeLocalSite() {
	return new FakeLocalSite();
}
}
