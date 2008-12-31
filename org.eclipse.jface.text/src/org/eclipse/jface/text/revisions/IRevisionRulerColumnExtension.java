/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.revisions;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.viewers.ISelectionProvider;


/**
 * Extension interface for {@link IRevisionRulerColumn}.
 * <p>
 * Introduces the ability to register a selection listener on revisions and configurable rendering
 * modes.
 * </p>
 *
 * @see IRevisionRulerColumn
 * @since 3.3
 */
public interface IRevisionRulerColumnExtension {

	/**
	 * Rendering mode type-safe enum.
	 */
	final class RenderingMode {
		private final String fName;
		private RenderingMode(String name) {
			Assert.isLegal(name != null);
			fName= name;
		}
		/**
		 * Returns the name of the rendering mode.
		 * @return the name of the rendering mode
		 */
		public String name() {
			return fName;
		}
	}

	/**
	 * Rendering mode that assigns a unique color to each revision author.
	 */
	RenderingMode AUTHOR= new RenderingMode("Author"); //$NON-NLS-1$
	/**
	 * Rendering mode that assigns colors to revisions by their age.
	 * <p>
	 * Currently the most recent revision is red, the oldest is a faint yellow.
	 * The coloring scheme can change in future releases.
	 * </p>
	 */
	RenderingMode AGE= new RenderingMode("Age"); //$NON-NLS-1$
	/**
	 * Rendering mode that assigns unique colors per revision author and
	 * uses different color intensity depending on the age.
	 * <p>
	 * Currently it selects lighter colors for older revisions and more intense
	 * colors for more recent revisions.
	 * The coloring scheme can change in future releases.
	 * </p>
	 */
	RenderingMode AUTHOR_SHADED_BY_AGE= new RenderingMode("Both"); //$NON-NLS-1$

	/**
	 * Changes the rendering mode and triggers redrawing if needed.
	 *
	 * @param mode the rendering mode
	 */
	void setRevisionRenderingMode(RenderingMode mode);

	/**
	 * Enables showing the revision id.
	 *
	 * @param show <code>true</code> to show the revision, <code>false</code> to hide it
	 */
	void showRevisionId(boolean show);

	/**
	 * Enables showing the revision author.
	 *
	 * @param show <code>true</code> to show the author, <code>false</code> to hide it
	 */
	void showRevisionAuthor(boolean show);

	/**
	 * Returns the revision selection provider.
	 *
	 * @return the revision selection provider
	 */
	ISelectionProvider getRevisionSelectionProvider();

	/**
	 * Adds a revision listener that will be notified when the displayed revision information
	 * changes.
	 *
	 * @param listener the listener to add
	 */
	void addRevisionListener(IRevisionListener listener);

	/**
	 * Removes a previously registered revision listener; nothing happens if <code>listener</code>
	 * was not registered with the receiver.
	 *
	 * @param listener the listener to remove
	 */
	void removeRevisionListener(IRevisionListener listener);
}
