package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.InputStream;

/**
 * An object which can create install related model objects (typically when
 * parsing feature manifest files and site maps).
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */

public class SiteModelFactory {
	
	/**
	 * Creates a factory which can be used to create install model objects.
	 */
	public SiteModelFactory() {
		super();
	}
	
	/**
	 * @since 2.0
	 */
	public boolean canParseSiteType(String type) {
		// return true if type was not specified (ie. is null or empty string)
		return (type == null || type.trim().equals(""));
	}
	 
	/**
	 * Constructs a feature model from stream
	 * 
	 * @param stream feature stream
	 */
	public SiteMapModel parseSite(InputStream stream) throws Exception {
		DefaultSiteParser parser = new DefaultSiteParser(this);
		return parser.parse(stream);
	}

	/**
	 * Returns a new site map model which is not initialized.
	 *
	 * @return a new site map model
	 */
	public SiteMapModel createSiteMapModel(){
		return new SiteMapModel();
	}
	
	/**
	 * Returns a new feture reference model which is not initialized.
	 *
	 * @return a new feature reference model
	 */
	public FeatureReferenceModel createFeatureReferenceModel() {
		return new FeatureReferenceModel();
	}

	/**
	 * Returns a new archive reference model which is not initialized.
	 *
	 * @return a new archive reference model
	 */
	public ArchiveReferenceModel createArchiveReferenceModel() {
		return new ArchiveReferenceModel();
	}

	/**
	 * Returns a new URL Entry model which is not initialized.
	 *
	 * @return a new URL Entry model
	 */
	public URLEntryModel createURLEntryModel() {
		return new URLEntryModel();
	}

	/**
	 * Returns a new site category model which is not initialized.
	 *
	 * @return a new site category model
	 */
	public SiteCategoryModel createSiteCategoryModel() {
		return new SiteCategoryModel();
	}
}

