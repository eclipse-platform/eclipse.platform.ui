/*****************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/

package org.eclipse.jface.text.formatter;

/**
 * Keys used by <code>IFormattingContext</code> objects to register specific
 * properties needed during the formatting process of a content formatter
 * implementing <code>IContentFormatterExtension2</code>.
 * 
 * @see IFormattingContext
 * @see IFormattingStrategyExtension
 * @see IContentFormatterExtension2
 * @since 3.0
 */
public class FormattingContextProperties {
	
	/**
	 * Ensure that this class cannot be instantiated.
	 */
	private FormattingContextProperties() {
	}

	/**
	 * Property key of the indentation property.
	 * The property must implement <code>java.lang#String</code>.
	 * <p>
	 * Value: <code>"formatting.context.indentation"</code>
	 */
	public static final String CONTEXT_INDENTATION= "formatting.context.indentation"; //$NON-NLS-1$

	/**
	 * Property key of the partition property.
	 * The property must implement <code>org.eclipse.jface.text#TypedPosition</code>.
	 * <p>
	 * Value: <code>"formatting.context.partition"</code>
	 */
	public static final String CONTEXT_PARTITION= "formatting.context.partition"; //$NON-NLS-1$

	/**
	 * Property key of the positions property.
	 * The property must implement <code>int[]</code>.
	 * <p>
	 * Value: <code>"formatting.context.positions"</code>
	 */
	public static final String CONTEXT_POSITIONS= "formatting.context.positions"; //$NON-NLS-1$

	/**
	 * Property key of the preferences property.
	 * The property must implement <code>java.util#Map</code>.
	 * <p>
	 * Value: <code>"formatting.context.preferences"</code>
	 */
	public static final String CONTEXT_PREFERENCES= "formatting.context.preferences"; //$NON-NLS-1$

	/**
	 * Property key of the document property.
	 * The property must implement <code>java.lang#Boolean</code>.
	 * <p>
	 * Value: <code>"formatting.context.document"</code>
	 */
	public static final String CONTEXT_DOCUMENT= "formatting.context.document"; //$NON-NLS-1$

	/**
	 * Property key of the region property.
	 * The property must implement <code>org.eclipse.jface.text#IRegion</code>.
	 * <p>
	 * Value: <code>"formatting.context.region"</code>
	 */
	public static final String CONTEXT_REGION= "formatting.context.region"; //$NON-NLS-1$
}
