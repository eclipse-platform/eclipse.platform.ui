package org.eclipse.update.internal.ui.search;

import org.eclipse.update.core.IFeature;
import java.util.ArrayList;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.core.Version;
import java.io.PrintWriter;
import org.w3c.dom.Node;

public class UpdatesQuery implements ISearchQuery {
	private ArrayList candidates;
	
	public UpdatesQuery(ArrayList candidates) {
		this.candidates = candidates;
	}

	/**
	 * @see ISearchQuery#matches(IFeature)
	 */
	public boolean matches(IFeature feature) {
		for (int i=0; i<candidates.size(); i++) {
			IFeature candidate = (IFeature)candidates.get(i);
			if (isNewerVersion(candidate, feature))
				return true;
		}
		return false;
	}
	
	private boolean isNewerVersion(IFeature feature, IFeature candidate) {
		VersionedIdentifier fvi = feature.getVersionedIdentifier();
		VersionedIdentifier cvi = candidate.getVersionedIdentifier();
		Version fv = fvi.getVersion();
		Version cv = cvi.getVersion();
		return cv.compare(fv) > 0;
	}
	public void parse(Node node) {
	}
	public void write(String indent, PrintWriter writer) {
	}
}