/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.incubator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 * 
 */
public abstract class AbstractElement {

	private static final int[][] EMPTY_INDICES = new int[0][0];
	private AbstractProvider provider;

	/**
	 * @param provider
	 */
	public AbstractElement(AbstractProvider provider) {
		super();
		this.provider = provider;
	}

	/**
	 * @return a string containing the first character of every word for camel
	 *         case checking.
	 */
	private static String getCamelCase(String label) {
		StringTokenizer tokenizer = new StringTokenizer(label);
		StringBuffer camelCase = new StringBuffer();
		while (tokenizer.hasMoreTokens()) {
			String word = tokenizer.nextToken();
			camelCase.append(word.charAt(0));
		}
		return camelCase.toString().toLowerCase();
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
	 * AbstractProvider that provided this element.
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
	public AbstractProvider getProvider() {
		return provider;
	}

	/**
	 * @param filter
	 * @return
	 */
	public QuickAccessEntry match(String filter, AbstractProvider providerForMatching) {
		String sortLabel = getSortLabel().toLowerCase();
		int index = sortLabel.indexOf(filter);
		if (index != -1) {
			return new QuickAccessEntry(this, providerForMatching, new int[][] { {
					index, index + filter.length() - 1 } }, EMPTY_INDICES);
		}
		String combinedLabel = (providerForMatching.getName() + " " + getLabel()).toLowerCase(); //$NON-NLS-1$
		index = combinedLabel.indexOf(filter);
		if (index != -1) {
			int lengthOfElementMatch = index + filter.length()
					- providerForMatching.getName().length();
			if (lengthOfElementMatch > 0) {
				return new QuickAccessEntry(this, providerForMatching,
						new int[][] { { 0, lengthOfElementMatch - 1 } },
						new int[][] { { index, index + filter.length() - 1 } });
			}
			return new QuickAccessEntry(this, providerForMatching, EMPTY_INDICES,
					new int[][] { { index, index + filter.length() - 1 } });
		}
		String camelCase = getCamelCase(sortLabel);
		index = camelCase.indexOf(filter);
		if (index != -1) {
			int[][] indices = getCamelCaseIndices(sortLabel, index, filter
					.length());
			return new QuickAccessEntry(this, providerForMatching, indices,
					EMPTY_INDICES);
		}
		String combinedCamelCase = getCamelCase(combinedLabel);
		index = combinedCamelCase.indexOf(filter);
		if (index != -1) {
			String providerCamelCase = getCamelCase(providerForMatching
					.getName());
			int lengthOfElementMatch = index + filter.length()
					- providerCamelCase.length();
			if (lengthOfElementMatch > 0) {
				return new QuickAccessEntry(
						this,
						providerForMatching,
						getCamelCaseIndices(sortLabel, 0, lengthOfElementMatch),
						getCamelCaseIndices(providerForMatching.getName(), index, filter
								.length()
								- lengthOfElementMatch));
			}
			return new QuickAccessEntry(this, providerForMatching, EMPTY_INDICES,
					getCamelCaseIndices(providerForMatching.getName(), index, filter
							.length()));
		}
		return null;
	}

	/**
	 * @param camelCase
	 * @param filter
	 * @param index
	 * @return
	 */
	private int[][] getCamelCaseIndices(String original, int start, int length) {
		List result = new ArrayList();
		int index = 0;
		while (start > 0) {
			index = original.indexOf(' ', index);
			while (original.charAt(index) == ' ') {
				index++;
			}
			start--;
		}
		while (length > 0) {
			result.add(new int[] { index, index });
			index = original.indexOf(' ', index);
			if (index != -1) {
				while (index < original.length()
						&& original.charAt(index) == ' ') {
					index++;
				}
			}
			length--;
		}
		return (int[][]) result.toArray(new int[result.size()][]);
	}
}
