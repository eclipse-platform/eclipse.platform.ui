/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class Preview {

	private static int topImageMargin = 16;
	private static int bottomImageMargin = 16;
	private static int frameWidth = 6;

	private final Composite parent;
	private Image currentImage;

	private IObservableValue parentSize;
	private IObservableValue inputFile;
	private IObservableValue rawImageData;
	private IObservableValue scaledImageData;
	private final Realm bgRealm;
	private Color borderColor;
	
	@Inject
	public Preview(final Composite parentComposite, Realm backgroundRealm) {
		parent = new Composite(parentComposite, SWT.NONE);
		parent.setData("org.eclipse.e4.ui.css.id", "preview");

		this.bgRealm = backgroundRealm;
		this.inputFile = new WritableValue(bgRealm);
		this.rawImageData = new ComputedValue(bgRealm) {
			protected Object calculate() {
				IFile file = (IFile) inputFile.getValue();
				if (file == null) {
					return null;
				}
				InputStream contents;
				try {
					contents = new BufferedInputStream(file.getContents());
					try {
						return new ImageData(contents);
					}catch (SWTException e) {
						// expected - most likely unsupported file format
					} finally {
						contents.close();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
		this.parentSize = new WritableValue(bgRealm);
		this.scaledImageData = new ComputedValue(bgRealm) {
			protected Object calculate() {
				ImageData rawData = (ImageData) rawImageData.getValue();
				Point maxSize = (Point) parentSize.getValue();
				if (rawData == null || maxSize == null) {
					return null;
				}
				Point targetSize = getBestSize(rawData.width, rawData.height,
						maxSize.x, maxSize.y);
				return rawData.scaledTo(targetSize.x, targetSize.y);
			}
		};
		scaledImageData.addChangeListener(new IChangeListener() {
			public void handleChange(ChangeEvent event) {
				final ImageData imageData = (ImageData) scaledImageData
						.getValue();
				parent.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (imageData == null)
							return;
						if (currentImage != null) {
							currentImage.dispose();
							currentImage = null;
						}
						currentImage = new Image(parent.getDisplay(), imageData);

						parent.redraw();
					}
				});
			}
		});
		parent.setLayout(new FillLayout());
		parent.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (e.gc.isDisposed())
					return;
				
				e.gc.fillRectangle(new Rectangle(e.x, e.y, e.width, e.height));
				Image i = currentImage;
				if (i != null && !i.isDisposed()) {
					Rectangle imageBounds = currentImage.getBounds();
					int x = (parent.getBounds().width - imageBounds.width) / 2;
					int y = bottomImageMargin;
					if(borderColor == null)
						borderColor = new Color(e.gc.getDevice(), 229, 229, 229);
					Color lastBackground = e.gc.getBackground();
					e.gc.setBackground(borderColor);
					e.gc.fillRoundRectangle(x, y, imageBounds.width + (frameWidth * 2), imageBounds.height + (frameWidth * 2), frameWidth, frameWidth);
					e.gc.setBackground(lastBackground);
					e.gc.drawImage(i, x + frameWidth, y + frameWidth);
				}
			}
		});
		parent.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				final Point newSize = parent.getSize();
				bgRealm.asyncExec(new Runnable() {
					public void run() {
						parentSize.setValue(newSize);
					}
				});
			}
		});
	}


	@Inject @Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) final IFile input) {
		bgRealm.asyncExec(new Runnable() {
			public void run() {
				inputFile.setValue(input);
			}
		});
	}
	
	@Focus
	void setFocus() {
		parent.setFocus();
	}

	private Point getBestSize(int originalX, int originalY, int maxX, int maxY) {
		double widthRatio = (double) originalX / (double) maxX;
		double heightRatio = (double) originalY / (double) maxY;

		double bestRatio = widthRatio > heightRatio ? widthRatio : heightRatio;

		int newWidth = (int) ((double) originalX / bestRatio) - (frameWidth * 2);
		int newHeight = (int) ((double) originalY / bestRatio)  - (topImageMargin + bottomImageMargin + (frameWidth * 2));

		return new Point(newWidth, newHeight);
	}

}
