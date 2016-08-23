/*******************************************************************************
 * Copyright (c) 2016 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eugen Neufeld - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;

/**
 * Wrapper for {@link MModelFragments} that contains additional information
 * needed while merging the {@link MModelFragment fragments}.
 */
public final class ModelFragmentWrapper {
	private MModelFragment modelFragment;
	private MModelFragments fragmentContainer;
	private boolean checkExists;
	private String contributorName;
	private String contributorURI;

	/**
	 * Constructor.
	 *
	 * @param fragmentContainer
	 *            the fragment container
	 * @param modelFragment
	 *            the fragment to be merged
	 * @param contributorName
	 *            the name of the element contributing the fragment
	 * @param contributorURI
	 *            the URI of the element contributing the fragment
	 * @param checkExists
	 *            specifies whether we should check that the application model
	 *            doesn't already contain the elements contributed by the
	 *            fragment before merging it
	 */
	public ModelFragmentWrapper(MModelFragments fragmentContainer, MModelFragment modelFragment, String contributorName,
			String contributorURI, boolean checkExists) {
		super();
		this.modelFragment = modelFragment;
		this.fragmentContainer = fragmentContainer;
		this.checkExists = checkExists;
		this.contributorName = contributorName;
		this.contributorURI = contributorURI;
	}

	/**
	 * @return the modelFragment
	 */
	public MModelFragment getModelFragment() {
		return modelFragment;
	}

	/**
	 * @param modelFragment
	 *            the modelFragment to set
	 */
	public void setModelFragment(MModelFragment modelFragment) {
		this.modelFragment = modelFragment;
	}

	/**
	 * @return the fragmentContainer
	 */
	public MModelFragments getFragmentContainer() {
		return fragmentContainer;
	}

	/**
	 * @param fragmentContainer
	 *            the fragmentContainer to set
	 */
	public void setFragmentContainer(MModelFragments fragmentContainer) {
		this.fragmentContainer = fragmentContainer;
	}

	/**
	 * @return the checkExists
	 */
	public boolean isCheckExists() {
		return checkExists;
	}

	/**
	 * @param checkExists
	 *            the checkExists to set
	 */
	public void setCheckExists(boolean checkExists) {
		this.checkExists = checkExists;
	}

	/**
	 * @return the contributorName
	 */
	public String getContributorName() {
		return contributorName;
	}

	/**
	 * @param contributorName
	 *            the contributorName to set
	 */
	public void setContributorName(String contributorName) {
		this.contributorName = contributorName;
	}

	/**
	 * @return the contributorURI
	 */
	public String getContributorURI() {
		return contributorURI;
	}

	/**
	 * @param contributorURI
	 *            the contributorURI to set
	 */
	public void setContributorURI(String contributorURI) {
		this.contributorURI = contributorURI;
	}
}
