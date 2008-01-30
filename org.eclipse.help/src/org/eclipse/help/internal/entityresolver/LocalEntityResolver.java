/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.entityresolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.internal.HelpPlugin;
import org.osgi.framework.Bundle;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LocalEntityResolver implements EntityResolver {

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		int index = systemId.lastIndexOf("/"); //$NON-NLS-1$
		if (index >= 0) {
		    Bundle helpBundle = HelpPlugin.getDefault().getBundle();
		    String dtdPath = "dtds/internal" + systemId.substring(index); //$NON-NLS-1$
		    URL dtdURL = FileLocator.find(helpBundle, new Path(dtdPath), null);
		    if (dtdURL != null) {
		    	InputStream stream = dtdURL.openStream();
		    	if (stream != null) {
					InputSource is = new InputSource(stream);
		        	is.setSystemId(systemId);
		        	is.setPublicId(publicId);
					return is;
				}
		    }
		}
	    return new InputSource(new StringReader("")); //$NON-NLS-1$
	}

}
