package org.eclipse.update.internal.ui.forms;
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
import org.eclipse.swt.events.*;

public class UpdateSearchContribution extends ContributionItem {
	private static final int SLEEP = 500;
	private Label label;
	private Image [] images;
	private String tooltip;
	private Thread animationThread;
	private int imageCounter=0;
	private boolean active;
	
	public UpdateSearchContribution(String id) {
		super(id);
		loadImages();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof UpdateSearchContribution)) return false;
		UpdateSearchContribution c = (UpdateSearchContribution)obj;
		return c.getId().equals(getId());
	}
	
	public boolean isVisible() {
		return true;
	}
	
	private void loadImages() {
		images = new Image[4];
		images[0] = UpdateUIPluginImages.DESC_SEARCH_PROGRESS_0.createImage();
		images[1] = UpdateUIPluginImages.DESC_SEARCH_PROGRESS_1.createImage();
		images[2] = UpdateUIPluginImages.DESC_SEARCH_PROGRESS_2.createImage();
		images[3] = UpdateUIPluginImages.DESC_SEARCH_PROGRESS_3.createImage();
	}
	
	protected Control createControl(Composite parent) {
		label = new Label(parent, SWT.NULL);
		label.setData(this);
		label.setImage(images[0]);
		label.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		return label;
	}
	
	public void startAnimation() {
		if (active) return;

		active = true;
		final Display display = label.getDisplay();
		final Runnable [] timer = new Runnable [1];
		timer [0] = new Runnable () {
			public void run () {
				if (!active) return;
				imageCounter++;
				if (imageCounter==images.length)
					imageCounter = 0;
				label.setImage(images[imageCounter]);
				display.timerExec (SLEEP, timer [0]);
			}
		};
		display.timerExec (SLEEP, timer [0]);
	}
	
	public void stopAnimation() {
		active = false;
	}
	
	public void setToolTipText(String text) {
		this.tooltip = text;
		if (label!=null && !label.isDisposed())
		   label.setToolTipText(text);
	}
	
	public void fill(Composite parent) {
		createControl(parent);
		if (tooltip!=null)
		   label.setToolTipText(tooltip);
	}
	
	public void dispose() {
		if (label!=null && !label.isDisposed()) {
			label.dispose();
			label = null;
		}
		for (int i=0; i<images.length; i++) {
			images[i].dispose();
		}
	}
}