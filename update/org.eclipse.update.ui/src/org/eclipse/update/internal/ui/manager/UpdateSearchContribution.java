package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.internal.ui.UpdateUIPluginImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

public class UpdateSearchContribution extends ControlContribution {
	private Label label;
	private Image image;
	private String tooltip;
	
	public UpdateSearchContribution(String id) {
		super(id);
	}
	protected Control createControl(Composite parent) {
		label = new Label(parent, SWT.NULL);
		if (image==null)
		   image = UpdateUIPluginImages.DESC_UPDATES_OBJ.createImage();
		label.setImage(image);
		if (tooltip!=null)
		   label.setToolTipText(tooltip);
		return label;
	}
	
	public void setToolTipText(String text) {
		this.tooltip = text;
		if (label!=null)
		   label.setToolTipText(text);
	}
	
	public void dispose() {
		if (label!=null && !label.isDisposed()) {
			label.dispose();
			label = null;
		}
		if (image!=null) {
			image.dispose();
			image = null;
		}
	}
}