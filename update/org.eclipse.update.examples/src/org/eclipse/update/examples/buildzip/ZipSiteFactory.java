package org.eclipse.update.examples.buildzip;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.FeatureReference;

public class ZipSiteFactory extends BaseSiteFactory {

	private Site site;

	public class ZipFilter implements FilenameFilter {
		/*
		 * @see FilenameFilter#accept(File, String)
		 */
		public boolean accept(File dir, String name) {
			return (name.toLowerCase().startsWith("eclipse") && name.toLowerCase().endsWith("zip"));
		}

	}

	/*
	 * @see ISiteFactory#createSite(URL, boolean)
	 */
	public ISite createSite(URL url, boolean forceCreation) throws CoreException, InvalidSiteTypeException {

		try {
			// create site and add category
			this.site = (Site) createSiteMapModel();
			SiteCategoryModel category = createSiteCategoryModel();
			category.setName("eclipse-drops");
			category.setLabel("Eclipse Build drops");
			site.addCategoryModel(category);
			
			// set content provider
			url = removeSiteXML(url);
			ZipSiteContentProvider contentProvider = new ZipSiteContentProvider(url);
			site.setSiteContentProvider(contentProvider);
						
			// get all matching zip files on teh site			
			File file = new File(url.getFile());
			String[] listOfZips = file.list(new ZipFilter());

			FeatureReferenceModel ref = null;
			for (int i = 0; i < listOfZips.length; i++) {
				ref = createFeatureReferenceModel();
				ref.setType("org.eclipse.update.examples.zip");
				ref.setSiteModel(this.site);

				ref.setURLString(listOfZips[i]);
				ref.resolve(file.toURL(),null);
				ref.setCategoryNames(new String[]{"eclipse-drops"});
				this.site.addFeatureReferenceModel(ref);
			}
		} catch (MalformedURLException e) {
			// throw exception ?
			e.printStackTrace();
		}

		return this.site;
	}

	/**
	 * removes site.xml from the URL
	 */
	private URL removeSiteXML(URL url) throws MalformedURLException {
		URL result = url;
		if (url != null && url.getFile().endsWith(Site.SITE_XML)) {
			int index = url.getFile().lastIndexOf(Site.SITE_XML);
			result = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile().substring(0, index));
		}
		return result;
	}

}