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
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetectorExtension;

import org.eclipse.ui.internal.texteditor.HyperlinkDetectorDescriptor;


/**
 * Hyperlink detector registry that manages the detectors
 * contributed by the <code>org.eclipse.ui.workbench.texteditor.hyperlinkDetectors</code> extension point for
 * targets contributed by the <code>org.eclipse.ui.workbench.texteditor.hyperlinkDetectorTargets</code> extension point.
 * <p>
 * <em>This API is provisional and may change any time before the 3.3 API freeze.</em>
 * </p>
 * 
 * XXX: must take a preference store to disable detectors via preference page
 * 
 * @since 3.3
 */
public final class HyperlinkDetectorRegistry {
	
	/**
	 * Delegate for contributed hyperlink detectors.
	 */
	private static class HyperlinkDetectorDelegate implements IHyperlinkDetector, IHyperlinkDetectorExtension {
		
		private HyperlinkDetectorDescriptor fHyperlinkDescriptor;
		private AbstractHyperlinkDetector fHyperlinkDetector;
		private boolean fFailedDuringCreation= false;
		private IAdaptable fContext;

		
		private HyperlinkDetectorDelegate(HyperlinkDetectorDescriptor descriptor) {
			fHyperlinkDescriptor= descriptor;
		}

		/*
		 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
		 */
		public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
			if (fHyperlinkDetector == null && !fFailedDuringCreation) {
				try {
					fHyperlinkDetector= fHyperlinkDescriptor.createHyperlinkDetector();
				} catch (CoreException ex) {
					fFailedDuringCreation= true;
				}
				if (fHyperlinkDetector != null && fContext != null)
					fHyperlinkDetector.setContext(fContext);
			}
			if (fHyperlinkDetector != null)
				return fHyperlinkDetector.detectHyperlinks(textViewer, region, canShowMultipleHyperlinks);
			
			return null;
		}
		
		private void setContext(IAdaptable context) {
			fContext= context;
		}
		
		/*
		 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetectorExtension#dispose()
		 */
		public void dispose() {
			if (fHyperlinkDetector != null) {
				fHyperlinkDetector.dispose();
				fHyperlinkDetector= null;
			}
			fHyperlinkDescriptor= null;
			fContext= null;
		}
		
	}

	
	
	private HyperlinkDetectorDescriptor[] fHyperlinkDetectorDescriptors;
	
	
	/**
	 * Returns all Java editor text hovers contributed to the workbench.
	 * 
	 * @return an array of hyperlink detector descriptors
	 */
	private synchronized HyperlinkDetectorDescriptor[] getHyperlinkDetectorDescriptors() {
		if (fHyperlinkDetectorDescriptors == null)
			fHyperlinkDetectorDescriptors= HyperlinkDetectorDescriptor.getContributedHyperlinkDetectors();
		return fHyperlinkDetectorDescriptors;
	} 

	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetectorRegistry#createHyperlinkDetectors(java.lang.String[], org.eclipse.core.runtime.IAdaptable)
	 */
	public IHyperlinkDetector[] createHyperlinkDetectors(String targetId, IAdaptable context) {
		Assert.isLegal(targetId != null);
		
		List result= new ArrayList();
		for (int i= 0; i < getHyperlinkDetectorDescriptors().length; i++) {
			if (targetId.equals(getHyperlinkDetectorDescriptors()[i].getTargetId())) {
				HyperlinkDetectorDelegate detector= new HyperlinkDetectorDelegate(getHyperlinkDetectorDescriptors()[i]);
				result.add(detector);
				detector.setContext(context);
			}
		}
		return (IHyperlinkDetector[])result.toArray(new IHyperlinkDetector[result.size()]);
	}

}
