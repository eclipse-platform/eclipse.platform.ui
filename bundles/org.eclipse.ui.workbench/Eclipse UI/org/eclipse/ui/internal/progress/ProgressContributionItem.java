package org.eclipse.ui.internal.progress;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class ProgressContributionItem extends ContributionItem {

	private ProgressControl control;
	
	

	public ProgressContributionItem(String id) {
		super(id);
		
	}

	public void fill(Composite parent) {
		if (control == null) {
			
			control = new ProgressControl();
			control.createCanvas(parent);

			AnimatedCanvas canvas = control.getCanvas();
			StatusLineLayoutData data = new StatusLineLayoutData();
			Rectangle bounds = canvas.getImage().getBounds();
			data.widthHint = bounds.width;
			data.heightHint = bounds.height;
			canvas.getControl().setLayoutData(data);
		}
	}

}