/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.core.model;

import java.io.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.xml.sax.*;

/**
 * Default site model factory.
 * <p>
 * This class may be instantiated or subclassed by clients. However, in most 
 * cases clients should instead subclass the provided base implementation 
 * of this factory.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.update.core.BaseSiteFactory
 * @since 2.0
 */
public class SiteModelFactory {
	
	private static DefaultSiteParser parser = new DefaultSiteParser();

	/**
	 * Creates a default site factory.
	 * 
	 * @since 2.0
	 */
	public SiteModelFactory() {
		super();
	}

	/**
	 * Indicates whether this factory can handle the specified site type. This
	 * method is intended to be overridden by subclasses.
	 * 
	 * @param type site type identifier
	 * @return <code>true</code> if the type can be handled, otherwise <code>false</code>
	 * @since 2.0
	 */
	public boolean canParseSiteType(String type) {
		// return true if type was not specified (ie. is null or empty string)
		return (type == null || type.trim().equals("")); //$NON-NLS-1$
	}

	/**
	 * Creates and populates a default site from stream.
	 * The parser assumes the stream contains a default site manifest
	 * (site.xml) as documented by the platform.
	 * 
	 * @param stream site stream
	 * @return populated site model
	 * @exception CoreException
	 * @exception InvalidSiteTypeException
	 * @since 2.0
	 */
	public SiteModel parseSite(InputStream stream)
		throws CoreException, InvalidSiteTypeException {
		SiteModel result = null;
		try {
			parser.init(this);
			result = parser.parse(stream);
			if (parser.getStatus()!=null) {
				// some internalError were detected
				IStatus status = parser.getStatus();
				throw new CoreException(status);
			}
		} catch (SAXException e) {
			// invalid Site type
			if (e.getException() instanceof InvalidSiteTypeException) {
				throw (InvalidSiteTypeException) e.getException();
			}

			throw Utilities.newCoreException(Policy.bind("SiteModelObject.ErrorParsingSiteStream"),e); //$NON-NLS-1$
		} catch (IOException e){
			throw Utilities.newCoreException(Policy.bind("SiteModelObject.ErrorAccessingSiteStream"),e); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Create a default site model.
	 * 
	 * @see SiteModel
	 * @return site model
	 * @since 2.0
	 */
	public SiteModel createSiteMapModel() {
		return new SiteModel();
	}

	/**
	 * Create a default site feature reference model.
	 * 
	 * @see SiteFeatureReferenceModel
	 * @return site feature reference model
	 * @since 2.0
	 */
	public SiteFeatureReferenceModel createFeatureReferenceModel() {
		return new SiteFeatureReferenceModel();
	}

	/**
	 * Create a default archive reference model.
	 * 
	 * @see ArchiveReferenceModel
	 * @return archive reference model
	 * @since 2.0
	 */
	public ArchiveReferenceModel createArchiveReferenceModel() {
		return new ArchiveReferenceModel();
	}

	/**
	 * Create a default annotated URL model.
	 * 
	 * @see URLEntryModel
	 * @return annotated URL model
	 * @since 2.0
	 */
	public URLEntryModel createURLEntryModel() {
		return new URLEntryModel();
	}

	/**
	 * Create a default category model.
	 * 
	 * @see CategoryModel
	 * @return category model
	 * @since 2.0
	 */
	public CategoryModel createSiteCategoryModel() {
		return new CategoryModel();
	}
}
