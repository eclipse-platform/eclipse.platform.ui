package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.update.internal.ui.model.IConfiguredFeatureAdapter;

public class FeatureStateAction extends Action {
	private IConfiguredFeatureAdapter adapter;

	public void setFeature(IConfiguredFeatureAdapter adapter) {
		this.adapter = adapter;
		if (adapter.isConfigured()) {
			setText("Disable");
		} else {
			setText("Enable");
		}
	}
	
	public void run() {
		super.run();
	}


}
