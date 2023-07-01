/*******************************************************************************
 * Copyright (c) 2012, 2015 IBM Corporation and others.
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
 *     Sopot Cela <scela@redhat.com> - Bug 472761
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.ControlElement;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.widgets.ImageBasedFrame;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class CSSRenderingUtils {

	private static final String DRAG_HANDLE = "org.eclipse.e4.ui.workbench.swt.DRAG_HANDLE";
	private static final String DRAG_HANDLE_ROTATED = "org.eclipse.e4.ui.workbench.swt.DRAG_HANDLE-ROTATED";

	private static final String FRAME_IMAGE_PROP = "frame-image";

	private static final String FRAME_IMAGE_ROTATE_PROP = "frame-image-rotated";

	private static final String HANDLE_IMAGE_PROP = "handle-image";

	private static final String HANDLE_IMAGE_ROTATE_PROP = "handle-image-rotated";

	// NOTE: The CSS engine 'owns' the image it returns (it caches it)
	// so we have to cache any rotated versions to match
	private Map<Image, Image> rotatedImageMap = new HashMap<>();

	public Control frameMeIfPossible(Control toFrame, String classId,
			boolean vertical, boolean draggable) {
		Integer[] frameInts = new Integer[4];
		Image frameImage = createImage(toFrame, classId, FRAME_IMAGE_PROP,
				frameInts);
		if (vertical && frameImage != null) {
			Image frameImageRotated = createImage(toFrame, classId, FRAME_IMAGE_ROTATE_PROP, frameInts);
			if (frameImageRotated != null) {
				frameImage = frameImageRotated;
				if (frameInts != null) {
					int tmp;
					tmp = frameInts[0];
					frameInts[0] = frameInts[2];
					frameInts[2] = tmp;
					tmp = frameInts[1];
					frameInts[1] = frameInts[3];
					frameInts[3] = tmp;
				}
			} else {
				frameImage = rotateImage(toFrame.getDisplay(), frameImage, frameInts);
			}
		}

		Image handleImage = createImage(toFrame, classId, HANDLE_IMAGE_PROP,
				null);

		if ((handleImage == null) && (draggable)) {
			// need to feed default image otherwise the toolbar DnD won't work
			// see bug 472761
			handleImage = JFaceResources.getImage(DRAG_HANDLE);
			if (handleImage == null) {
				initDragHandleResource();
				handleImage = JFaceResources.getImage(DRAG_HANDLE);
			}

		}

		if (vertical && handleImage != null) {
			Image handleImageRotated = createImage(toFrame, classId, HANDLE_IMAGE_ROTATE_PROP, null);
			if (handleImageRotated != null) {
				handleImage = handleImageRotated;
			} else {
				handleImage = JFaceResources.getImage(DRAG_HANDLE_ROTATED);
				if (handleImage == null) {
					initDragHandleResource();
					handleImage = JFaceResources.getImage(DRAG_HANDLE_ROTATED);
				}
			}
		}
		if (frameImage != null) {
			ImageBasedFrame frame = new ImageBasedFrame(toFrame.getParent(),
					toFrame, vertical, draggable);
			frame.setImages(frameImage, frameInts, handleImage);
			addFrameImageDisposedListener(frame, toFrame, classId, vertical);
			return frame;
		} else if (handleImage != null) {
			ImageBasedFrame frame = new ImageBasedFrame(toFrame.getParent(),
					toFrame, vertical, draggable);
			frame.setImages(null, null, handleImage);
			addHandleImageDisposedListener(frame, toFrame, classId, vertical);
			return frame;
		}

		return toFrame;
	}

	private void initDragHandleResource() {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		IPath path = IPath.fromOSString("$ws$/images/dragHandle.png");
		URL url = FileLocator.find(bundle, path, null);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		if (desc != null)
			JFaceResources.getImageRegistry().put(DRAG_HANDLE, desc);
		path = IPath.fromOSString("$ws$/images/dragHandle-rotated.png");
		url = FileLocator.find(bundle, path, null);
		ImageDescriptor desc_rotated = ImageDescriptor.createFromURL(url);
		if (desc_rotated != null)
			JFaceResources.getImageRegistry().put(DRAG_HANDLE_ROTATED, desc_rotated);
	}

	private Image rotateImage(Display display, Image image, Integer[] frameInts) {
		// Swap the widths / heights of the 'cuts'
		if (frameInts != null) {
			int tmp;
			tmp = frameInts[0];
			frameInts[0] = frameInts[2];
			frameInts[2] = tmp;
			tmp = frameInts[1];
			frameInts[1] = frameInts[3];
			frameInts[3] = tmp;
		}

		if (rotatedImageMap.get(image) != null)
			return rotatedImageMap.get(image);

		// rotate 90 degrees,,,
		Rectangle bounds = image.getBounds();
		ImageData imageData = new ImageData(bounds.height, bounds.width, 32,
				new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
		Image rotatedImage = new Image(display, imageData);
		GC gc = new GC(rotatedImage);
		RGB rgb = new RGB(0x7d, 0, 0);
		Color offRed = new Color(display, rgb);
		gc.setBackground(offRed);
		gc.fillRectangle(0, 0, bounds.height, bounds.width);
		Transform t = new Transform(display);
		int w = image.getBounds().height;
		int offset = 0; // (w+1) % 2;
		t.translate(w - offset, 0);
		t.rotate(90);
		gc.setTransform(t);
		gc.drawImage(image, 0, 0);
		gc.dispose();
		t.dispose();
		ImageData alphaData = rotatedImage.getImageData();
		rotatedImage.dispose();
		int transparentPix = alphaData.palette.getPixel(rgb);
		for (int i = 0; i < alphaData.width; i++) {
			for (int j = 0; j < alphaData.height; j++) {
				if (alphaData.getPixel(i, j) != transparentPix) {
					alphaData.setAlpha(i, j, 255);
				}
			}
		}
		rotatedImage = new Image(display, alphaData);
		// ...and cache it
		rotatedImageMap.put(image, rotatedImage);

		// Return the new one
		return rotatedImage;
	}

	public CSSValue getCSSValue(Control styleControl, String className,
			String attributeName) {
		CSSEngine csseng = WidgetElement.getEngine(styleControl);
		if (csseng == null) {
			return null;
		}
		ControlElement tempEment = (ControlElement) csseng
				.getElement(styleControl);
		if (tempEment == null) {
			return null;
		}

		// super hack
		if (className != null)
			WidgetElement.setCSSClass(styleControl, className);

		CSSStyleDeclaration styleDeclarations = csseng.getViewCSS()
				.getComputedStyle(tempEment, ""); //$NON-NLS-1$

		if (styleDeclarations == null)
			return null;

		return styleDeclarations.getPropertyCSSValue(attributeName);
	}

	public Image createImage(Control styleControl, String classId,
			String attName, Integer[] frameInts) {
		Image image = null;

		CSSEngine csseng = WidgetElement.getEngine(styleControl);
		if (csseng == null) {
			return null;
		}
		ControlElement tempEment = (ControlElement) csseng
				.getElement(styleControl);
		if (tempEment == null) {
			return null;
		}
		if (classId != null)
			ControlElement.setCSSClass(styleControl, classId);

		CSSStyleDeclaration styleDeclarations = csseng.getViewCSS()
				.getComputedStyle(tempEment, "");
		if (styleDeclarations == null)
			return null;

		CSSValue imagePath = styleDeclarations.getPropertyCSSValue(attName);
		if (imagePath == null)
			return null;

		if (imagePath.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			// String imageString = ((CSSPrimitiveValue) imagePath)
			// .getStringValue();
			// System.out.println("here" + imageString);
			try {
				image = (Image) csseng.convert(imagePath, Image.class,
						styleControl.getDisplay());
				if (image != null && frameInts != null) {
					CSSValue value = styleDeclarations
							.getPropertyCSSValue("frame-cuts"); //$NON-NLS-1$
					if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
						CSSValueList valueList = (CSSValueList) value;
						if (valueList.getLength() != 4)
							return null;

						for (int i = 0; i < valueList.getLength(); i++) {
							CSSValue val = valueList.item(i);
							if ((val.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)
									&& ((CSSPrimitiveValue) val)
											.getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
								frameInts[i] = (int) ((CSSPrimitiveValue) val)
										.getFloatValue(CSSPrimitiveValue.CSS_PX);
							} else {
								return null;
							}
						}

						// System.out.println("Results " + frameInts);
					}
				}
			} catch (Exception e1) {
			}
		}
		return image;
	}

	private void addHandleImageDisposedListener(
			ImageBasedFrame imageBasedFrame, final Control toFrame,
			final String classId, final boolean vertical) {
		final Listener listener = event -> {
			if (!(event.widget instanceof ImageBasedFrame)) {
				return;
			}

			ImageBasedFrame frame = (ImageBasedFrame) event.widget;
			if (!isImagesRefreshRequired(frame)) {
				return;
			}

			Image handleImage = createImage(toFrame, classId,
					HANDLE_IMAGE_PROP, null);
			if (vertical && handleImage != null) {
				Image handleImageRotated = createImage(toFrame, classId, HANDLE_IMAGE_ROTATE_PROP, null);
				if (handleImageRotated != null) {
					handleImage = handleImageRotated;
				} else {
					handleImage = rotateImage(toFrame.getDisplay(), handleImage, null);
				}
			}
			if (handleImage != null) {
				frame.setImages(null, null, handleImage);
			}
		};

		toFrame.getDisplay().addListener(SWT.Skin, listener);

		imageBasedFrame.addDisposeListener(e -> e.widget.getDisplay().removeListener(SWT.Skin, listener));
	}

	private void addFrameImageDisposedListener(ImageBasedFrame imageBasedFrame,
			final Control toFrame, final String classId, final boolean vertical) {
		final Listener listener = event -> {
			if (!(event.widget instanceof ImageBasedFrame)) {
				return;
			}

			ImageBasedFrame frame = (ImageBasedFrame) event.widget;
			if (!isImagesRefreshRequired(frame)) {
				return;
			}

			Integer[] frameInts = new Integer[4];
			Image frameImage = createImage(toFrame, classId,
					FRAME_IMAGE_PROP, frameInts);
			if (vertical && frameImage != null) {
				Image frameImageRotated = createImage(toFrame, classId, FRAME_IMAGE_ROTATE_PROP,
						frameInts);
				if (frameImageRotated != null) {
					frameImage = frameImageRotated;
					if (frameInts != null) {
						int tmp;
						tmp = frameInts[0];
						frameInts[0] = frameInts[2];
						frameInts[2] = tmp;
						tmp = frameInts[1];
						frameInts[1] = frameInts[3];
						frameInts[3] = tmp;
					}
				} else {
					frameImage = rotateImage(toFrame.getDisplay(), frameImage, frameInts);
				}
			}

			Image handleImage = createImage(toFrame, classId,
					HANDLE_IMAGE_PROP, null);
			if (vertical && handleImage != null) {
				Image handleImageRotated = createImage(toFrame, classId, HANDLE_IMAGE_ROTATE_PROP, null);
				if (handleImageRotated != null) {
					handleImage = handleImageRotated;
				} else {
					handleImage = rotateImage(toFrame.getDisplay(), handleImage, null);
				}
			}
			if (frameImage != null) {
				frame.setImages(frameImage, frameInts, handleImage);
			}
		};

		toFrame.getDisplay().addListener(SWT.Skin, listener);

		imageBasedFrame.addDisposeListener(e -> e.widget.getDisplay().removeListener(SWT.Skin, listener));
	}

	private boolean isImagesRefreshRequired(ImageBasedFrame frame) {
		Object handleImage = frame.getData("handleImage");
		if (handleImage instanceof Image && ((Image) handleImage).isDisposed()) {
			return true;
		}

		Object frameImage = frame.getData("frameImage");
		if (frameImage instanceof Image && ((Image) frameImage).isDisposed()) {
			return true;
		}

		return false;
	}
}
