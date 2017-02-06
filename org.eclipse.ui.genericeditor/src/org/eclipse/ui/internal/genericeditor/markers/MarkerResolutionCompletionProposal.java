package org.eclipse.ui.internal.genericeditor.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

public class MarkerResolutionCompletionProposal implements ICompletionProposal {

	private IMarkerResolution markerResolution;
	private IMarker marker;

	public MarkerResolutionCompletionProposal(IMarker marker, IMarkerResolution markerResolution) {
		this.marker = marker;
		this.markerResolution = markerResolution;
	}

	@Override
	public void apply(IDocument document) {
		this.markerResolution.run(this.marker);
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		if (this.markerResolution instanceof IMarkerResolution2) {
			return ((IMarkerResolution2)this.markerResolution).getDescription();
		}
		return null;
	}

	@Override
	public String getDisplayString() {
		return this.markerResolution.getLabel();
	}

	@Override
	public Image getImage() {
		if (this.markerResolution instanceof IMarkerResolution2) {
			return ((IMarkerResolution2)this.markerResolution).getImage();
		}
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

}
