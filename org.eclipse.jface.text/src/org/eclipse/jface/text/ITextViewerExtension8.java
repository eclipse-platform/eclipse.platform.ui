/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.swt.custom.StyledTextPrintOptions;


/**
 * Extension interface for {@link org.eclipse.jface.text.ITextViewer}. Adds the
 * ability to print and set whether to allow moving the mouse into a hover.
 * 
 * @since 3.4
 */
public interface ITextViewerExtension8 {
	
	/**
	 * Print the text viewer contents using the given options.
	 * 
	 * @param options the print options
	 */
	void print(StyledTextPrintOptions options);

	/**
	 * Sets whether this viewer allows to move the mouse into a hover i.e. don't
	 * close it automatically.
	 * 
	 * @param state <code>true</code> to enable, <code>false</code>
	 *            otherwise
	 */
	void setAllowMoveIntoHover(boolean state);

	/**
	 * Sets when hovers should be enriched once the mouse is
	 * moved into them.
	 * <p>
	 * Only applicable when {@link #setAllowMoveIntoHover(boolean)} has been
	 * called with <code>true</code>.</p>
	 * 
	 * @param mode the enrich mode
	 */
	void setHoverEnrichMode(EnrichMode mode);
	
	
	/**
	 * Type-safe enum of the available enrich modes.
	 */
	public static final class EnrichMode {

		/**
		 * Enrich the hover shortly after the mouse has been moved into it and
		 * stopped moving.
		 * 
		 * @see ITextViewerExtension8#setHoverEnrichMode(org.eclipse.jface.text.ITextViewerExtension8.EnrichMode)
		 */
		public static final EnrichMode AFTER_DELAY= new EnrichMode("after delay"); //$NON-NLS-1$

		/**
		 * Enrich the hover immediately when the mouse is moved into it.
		 * 
		 * @see ITextViewerExtension8#setHoverEnrichMode(org.eclipse.jface.text.ITextViewerExtension8.EnrichMode)
		 */
		public static final EnrichMode IMMEDIATELY= new EnrichMode("immediately"); //$NON-NLS-1$

		/**
		 * Enrich the hover on explicit mouse click.
		 * 
		 * @see ITextViewerExtension8#setHoverEnrichMode(org.eclipse.jface.text.ITextViewerExtension8.EnrichMode)
		 */
		public static final EnrichMode ON_CLICK= new EnrichMode("on click"); //$NON-NLS-1$;

		
		private String fName;

		private EnrichMode(String name) {
			fName= name;
		}

		/*
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return fName;
		}
	}

}
