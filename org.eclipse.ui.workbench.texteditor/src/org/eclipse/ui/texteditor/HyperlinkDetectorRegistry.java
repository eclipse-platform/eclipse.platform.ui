/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetectorExtension;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetectorExtension2;



/**
 * Hyperlink detector registry that manages the detectors
 * contributed by the <code>org.eclipse.ui.workbench.texteditor.hyperlinkDetectors</code> extension point for
 * targets contributed by the <code>org.eclipse.ui.workbench.texteditor.hyperlinkDetectorTargets</code> extension point.
 *
 * @since 3.3
 */
public final class HyperlinkDetectorRegistry {


	/**
	 * Delegate for contributed hyperlink detectors.
	 */
	private class HyperlinkDetectorDelegate implements IHyperlinkDetector, IHyperlinkDetectorExtension, IHyperlinkDetectorExtension2 {

		private HyperlinkDetectorDescriptor fHyperlinkDescriptor;
		private IHyperlinkDetector fHyperlinkDetector;
		private boolean fFailedDuringCreation= false;
		private IAdaptable fContext;
		private int fStateMask;
		private boolean fIsEnabled;


		private HyperlinkDetectorDelegate(HyperlinkDetectorDescriptor descriptor) {
			fHyperlinkDescriptor= descriptor;
			if (fPreferenceStore != null) {
				fStateMask= fPreferenceStore.getInt(fHyperlinkDescriptor.getId() +  HyperlinkDetectorDescriptor.STATE_MASK_POSTFIX);
				fIsEnabled= !fPreferenceStore.getBoolean(fHyperlinkDescriptor.getId());
			}
		}

		@Override
		public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
			if (!isEnabled())
				return null;

			if (!fFailedDuringCreation && fHyperlinkDetector == null) {
				try {
					fHyperlinkDetector= fHyperlinkDescriptor.createHyperlinkDetectorImplementation();
				} catch (CoreException ex) {
					fFailedDuringCreation= true;
				}
				if (fContext != null && fHyperlinkDetector instanceof AbstractHyperlinkDetector)
					((AbstractHyperlinkDetector)fHyperlinkDetector).setContext(fContext);
			}
			if (fHyperlinkDetector != null)
				return fHyperlinkDetector.detectHyperlinks(textViewer, region, canShowMultipleHyperlinks);

			return null;
		}

		private boolean isEnabled() {
			return fIsEnabled;
		}

		private void setContext(IAdaptable context) {
			fContext= context;
		}

		@Override
		public void dispose() {
			if (fHyperlinkDetector instanceof AbstractHyperlinkDetector)
				((AbstractHyperlinkDetector)fHyperlinkDetector).dispose();

			fHyperlinkDetector= null;
			fHyperlinkDescriptor= null;
			fContext= null;
		}

		@Override
		public int getStateMask() {
			return fStateMask;
		}

	}


	private HyperlinkDetectorDescriptor[] fHyperlinkDetectorDescriptors;
	private IPreferenceStore fPreferenceStore;


	/**
	 * Creates a new hyperlink detector registry.
	 */
	public HyperlinkDetectorRegistry() {
	}

	/**
	 * Creates a new hyperlink detector registry that controls
	 * hyperlink enablement via the given preference store.
	 * <p>
	 * The hyperlink detector id is used as preference key.
	 * The value is of type <code>Boolean</code> where
	 * <code>false</code> means that the hyperlink detector is active.
	 * </p>
	 *
	 * @param preferenceStore the preference store to be used
	 */
	public HyperlinkDetectorRegistry(IPreferenceStore preferenceStore) {
		fPreferenceStore= preferenceStore;
	}

	/**
	 * Returns all hyperlink detectors contributed to the workbench.
	 *
	 * @return an array of hyperlink detector descriptors
	 */
	public synchronized HyperlinkDetectorDescriptor[] getHyperlinkDetectorDescriptors() {
		initHyperlinkDetectorDescriptors();
		HyperlinkDetectorDescriptor[] result= new HyperlinkDetectorDescriptor[fHyperlinkDetectorDescriptors.length];
		System.arraycopy(fHyperlinkDetectorDescriptors, 0, result, 0, fHyperlinkDetectorDescriptors.length);
		return result;
	}

	/**
	 * Initializes the hyperlink detector descriptors.
	 */
	private synchronized void initHyperlinkDetectorDescriptors() {
		if (fHyperlinkDetectorDescriptors == null)
			fHyperlinkDetectorDescriptors= HyperlinkDetectorDescriptor.getContributedHyperlinkDetectors();
	}

	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetectorRegistry#createHyperlinkDetectors(java.lang.String[], org.eclipse.core.runtime.IAdaptable)
	 */
	public IHyperlinkDetector[] createHyperlinkDetectors(String targetId, IAdaptable context) {
		Assert.isLegal(targetId != null);
		initHyperlinkDetectorDescriptors();

		List<HyperlinkDetectorDelegate> result= new ArrayList<>();
		for (int i= 0; i < fHyperlinkDetectorDescriptors.length; i++) {
			if (targetId.equals(fHyperlinkDetectorDescriptors[i].getTargetId())) {
				HyperlinkDetectorDelegate detector= new HyperlinkDetectorDelegate(fHyperlinkDetectorDescriptors[i]);
				result.add(detector);
				detector.setContext(context);
			}
		}
		return result.toArray(new IHyperlinkDetector[result.size()]);
	}

}
