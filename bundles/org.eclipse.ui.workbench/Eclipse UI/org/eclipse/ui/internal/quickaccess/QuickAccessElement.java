/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;


import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 * 
 */
public abstract class QuickAccessElement {

	private static final int[][] EMPTY_INDICES = new int[0][0];
	private QuickAccessProvider provider;

	/**
	 * @param provider
	 */
	public QuickAccessElement(QuickAccessProvider provider) {
		super();
		this.provider = provider;
	}

	/**
	 * Returns the label to be displayed to the user.
	 * 
	 * @return the label
	 */
	public abstract String getLabel();

	/**
	 * Returns the image descriptor for this element.
	 * 
	 * @return an image descriptor, or null if no image is available
	 */
	public abstract ImageDescriptor getImageDescriptor();

	/**
	 * Returns the id for this element. The id has to be unique within the
	 * QuickAccessProvider that provided this element.
	 * 
	 * @return the id
	 */
	public abstract String getId();

	/**
	 * Executes the associated action for this element.
	 */
	public abstract void execute();

	/**
	 * Return the label to be used for sorting and matching elements.
	 * 
	 * @return the sort label
	 */
	public String getSortLabel() {
		return getLabel();
	}

	/**
	 * @return Returns the provider.
	 */
	public QuickAccessProvider getProvider() {
		return provider;
	}

	/**
	 * @param filter
	 * @return
	 */
	public QuickAccessEntry match(String filter,
			QuickAccessProvider providerForMatching) {
		String sortLabel = getLabel();
		int index = sortLabel.toLowerCase().indexOf(filter);
		if (index != -1) {
			return new QuickAccessEntry(this, providerForMatching,
					new int[][] { { index, index + filter.length() - 1 } },
					EMPTY_INDICES);
		}
		String combinedLabel = (providerForMatching.getName() + " " + getLabel()); //$NON-NLS-1$
		index = combinedLabel.toLowerCase().indexOf(filter);
		if (index != -1) {
			int lengthOfElementMatch = index + filter.length()
					- providerForMatching.getName().length() - 1;
			if (lengthOfElementMatch > 0) {
				return new QuickAccessEntry(this, providerForMatching,
						new int[][] { { 0, lengthOfElementMatch - 1 } },
						new int[][] { { index, index + filter.length() - 1 } });
			}
			return new QuickAccessEntry(this, providerForMatching,
					EMPTY_INDICES, new int[][] { { index,
							index + filter.length() - 1 } });
		}
		String camelCase = CamelUtil.getCamelCase(sortLabel);
		index = camelCase.indexOf(filter);
		if (index != -1) {
			int[][] indices = CamelUtil.getCamelCaseIndices(sortLabel, index, filter
					.length());
			return new QuickAccessEntry(this, providerForMatching, indices,
					EMPTY_INDICES);
		}
		String combinedCamelCase = CamelUtil.getCamelCase(combinedLabel);
		index = combinedCamelCase.indexOf(filter);
		if (index != -1) {
			String providerCamelCase = CamelUtil.getCamelCase(providerForMatching
					.getName());
			int lengthOfElementMatch = index + filter.length()
					- providerCamelCase.length();
			if (lengthOfElementMatch > 0) {
				return new QuickAccessEntry(
						this,
						providerForMatching,
						CamelUtil.getCamelCaseIndices(sortLabel, 0, lengthOfElementMatch),
						CamelUtil.getCamelCaseIndices(providerForMatching.getName(),
								index, filter.length() - lengthOfElementMatch));
			}
			return new QuickAccessEntry(this, providerForMatching,
					EMPTY_INDICES, CamelUtil.getCamelCaseIndices(providerForMatching
							.getName(), index, filter.length()));
		}
		return null;
	}
}
