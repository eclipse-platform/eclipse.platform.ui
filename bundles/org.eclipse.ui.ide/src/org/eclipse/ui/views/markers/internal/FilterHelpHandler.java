package org.eclipse.ui.views.markers.internal;

import org.eclipse.ui.views.markers.IFilterHelpHandler;

public class FilterHelpHandler implements IFilterHelpHandler {

	@Override
	public void handleHelpClick() {
		System.out.println("its a help"); //$NON-NLS-1$
	}

}
