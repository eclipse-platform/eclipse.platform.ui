package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.core.model.SiteModel;
import org.eclipse.update.internal.core.Policy;

/**
 *
 * 
 */

public class InternalFeatureReference extends FeatureReference implements IWritable {
	
	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {
		String gap = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap += " "; //$NON-NLS-1$
		String increment = ""; //$NON-NLS-1$
		for (int i = 0; i < IWritable.INDENT; i++)
			increment += " "; //$NON-NLS-1$

		w.print(gap + "<feature "); //$NON-NLS-1$
		// feature type
		if (getType() != null) {
			w.print("type=\"" + Writer.xmlSafe(getType() + "\"")); //$NON-NLS-1$ //$NON-NLS-2$
			w.print(" "); //$NON-NLS-1$
		}

		// feature URL
		String URLInfoString = null;
		if (getURL() != null) {
			URLInfoString = UpdateManagerUtils.getURLAsString(getSite().getURL(), getURL());
			w.print("url=\"" + Writer.xmlSafe(URLInfoString) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		w.println(">"); //$NON-NLS-1$

		String[] categoryNames = getCategoryNames();
		for (int i = 0; i < categoryNames.length; i++) {
			String element = categoryNames[i];
			w.println(gap + increment + "<category name=\"" + Writer.xmlSafe(element) + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		w.println("</feature>"); //$NON-NLS-1$
	}

}