package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.xml.sax.SAXException;

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
	 * @exception ParsingException when a parsing error occured
	 * @exception IOException when an IOException occured in the Stream
	 * @exception InvalidSiteTypeException when the type of the site is different from the one expected
	 */
	public SiteModel parseSite(InputStream stream) throws ParsingException, IOException, InvalidSiteTypeException {
		SiteModel result = null;
		try {
			DefaultSiteParser parser = new DefaultSiteParser(this);
			result = parser.parse(stream);
			if (parser.getStatus().getChildren().length>0){
				// some internalError were detected
				IStatus[] children = parser.getStatus().getChildren();
				String error = "";
				for (int i = 0; i < children.length; i++) {
					error = error + "\r\n"+children[i].getMessage();
				}
				throw new ParsingException(new Exception(error));
			}
		} catch (SAXException e){
			if (e instanceof SAXException){
				SAXException exception = (SAXException) e;
				// invalid Site type
				if(exception.getException() instanceof InvalidSiteTypeException){
					throw (InvalidSiteTypeException)exception.getException();
				}
				
				// invalid XML file (i.e we are parsing a directory stream)
				if(exception.getException() instanceof ParsingException){
					throw (ParsingException)exception.getException();
				}
				
			}
			
			throw new ParsingException(e);
		}
		return result;
	}

	/**
	 * Returns a new site map model which is not initialized.
	 *
	 * @return a new site map model
	 */
	public SiteModel createSiteMapModel(){
		return new SiteModel();
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
	public CategoryModel createSiteCategoryModel() {
		return new CategoryModel();
	}
}

