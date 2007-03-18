/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


/**
 * Extension interface for {@link org.eclipse.jface.text.ITextViewer}.
 * Adds the ability to install text triple click strategies.
 *
 * @since 3.3
 */
public interface ITextViewerExtension7 {

	/**
	 * Sets this viewer's text triple click strategy for the given content type.
	 * <p>
	 * <strong>Note:</strong> If a {@link ITextDoubleClickStrategy} also applies
	 * then it will be fired before the triple click strategy.
	 * </p>
	 *
	 * @param strategy the new triple click strategy. <code>null</code> is a valid argument.
	 * @param contentType the type for which the strategy is registered
	 */
	void setTextTripleClickStrategy(ITextTripleClickStrategy strategy, String contentType);

}
