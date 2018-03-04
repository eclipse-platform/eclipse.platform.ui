/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tips.core.TipImage;
import org.eclipse.tips.core.TipManager;
import org.eclipse.tips.core.TipProvider;
import org.eclipse.tips.core.TipProviderListener;
import org.eclipse.tips.core.internal.LogUtil;
import org.eclipse.tips.ui.internal.util.ImageUtil;
import org.eclipse.tips.ui.internal.util.ResourceManager;
import org.eclipse.tips.ui.internal.util.SWTResourceManager;

@SuppressWarnings("restriction")
public class Slider extends Composite {

	private Composite fScroller;
	private TipProvider fSelectedProvider;
	private int fSpacing = 5;
	private int fSliderIndex = 0;
	private List<ProviderSelectionListener> fListeners = new ArrayList<>();
	private TipManager fTipManager;
	private Button fLeftButton;
	private Button fRightButton;
	private TipProviderListener fProviderListener;
	private Composite fSelectedProviderButton;
	private HashMap<String, Image> fProviderImageCache = new HashMap<>();
	private int fIconSize = 48;

	/**
	 * Constructor for the Slider widget.
	 *
	 * @param parent
	 *            the parent
	 * @param style
	 *            the SWT style bits
	 */
	public Slider(Composite parent, int style) {
		super(parent, style);

		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		setLayout(layout);

		fLeftButton = new Button(this, SWT.FLAT);
		GridData gd_leftButton = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_leftButton.widthHint = fIconSize / 2 + 8;
		gd_leftButton.heightHint = fIconSize;
		fLeftButton.setLayoutData(gd_leftButton);
		fLeftButton.setImage(getImage("icons/" + fIconSize + "/aleft.png"));
		fLeftButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scrollLeft();
			}
		});

		fScroller = new Composite(this, SWT.DOUBLE_BUFFERED);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		layoutData.heightHint = fIconSize + 4;
		fScroller.setLayoutData(layoutData);

		fRightButton = new Button(this, SWT.FLAT);
		GridData gd_rightButton = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_rightButton.widthHint = fIconSize / 2 + 8;
		gd_rightButton.heightHint = fIconSize;
		fRightButton.setLayoutData(gd_rightButton);
		fRightButton.setImage(getImage("icons/" + fIconSize + "/aright.png"));
		fRightButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scrollRight();
			}
		});

		setupDisposeListener();
		setupProviderListener();
	}

	private void setupDisposeListener() {
		addListener(SWT.Dispose, event -> {
			fTipManager.getListenerManager().removeProviderListener(fProviderListener);
			fProviderImageCache.values().forEach(img -> {
				if (!img.isDisposed()) {
					img.dispose();
				}
			});
		});
	}

	private void setupProviderListener() {
		fProviderListener = provider -> getDisplay().asyncExec(() -> load());
	}

	private static Image getImage(String icon) {
		return ResourceManager.getPluginImage("org.eclipse.tips.ui", icon);
	}

	/**
	 * Loads or reloads the provider list. If you want to update the read count of
	 * the current button or all buttons then check the {@link #updateButton()} and
	 * {@link #updateButtons()} methods.
	 *
	 */
	public void load() {
		if (isDisposed() || fScroller.isDisposed()) {
			return;
		}
		Arrays.stream(fScroller.getChildren()).filter(control -> !control.isDisposed())
				.forEach(control -> control.dispose());
		List<TipProvider> providers = fTipManager.getProviders();
		int spaceCount = Math.floorDiv(fScroller.getBounds().width, (fIconSize + fSpacing));
		int providerCount = providers.size();

		if (fSliderIndex > 0 && spaceCount >= providerCount) {
			fSliderIndex = 0;
		}

		if (spaceCount >= providerCount) {
			if (fRightButton.isEnabled()) {
				fRightButton.setEnabled(false);
				fLeftButton.setEnabled(false);
				fLeftButton.setImage(getImage("icons/" + fIconSize + "/aright.png"));
				fRightButton.setImage(getImage("icons/" + fIconSize + "/aleft.png"));
			}
		} else {
			if (!fRightButton.isEnabled()) {
				fRightButton.setEnabled(true);
				fLeftButton.setEnabled(true);
				fLeftButton.setImage(getImage("icons/" + fIconSize + "/aleft.png"));
				fRightButton.setImage(getImage("icons/" + fIconSize + "/aright.png"));
			}
		}

		int emptyPixelsLeft = Math.floorMod(fScroller.getBounds().width, (fIconSize + fSpacing));
		int newSpacing = fSpacing + (emptyPixelsLeft / (spaceCount + 1));
		for (int i = 0; i < Math.min(providerCount - fSliderIndex, spaceCount); i++) {
			TipProvider provider = providers.get(i + fSliderIndex);
			if (fSelectedProvider == null && !provider.getTips(true).isEmpty()) {
				fSelectedProvider = provider;
				notifyListeners(fSelectedProvider);
			}
			createProviderButton(providers.get(i + fSliderIndex), newSpacing, i);
		}
	}

	private Composite createProviderButton(TipProvider provider, int spacing, int index) {
		Composite button = new Composite(fScroller, SWT.DOUBLE_BUFFERED);
		button.setToolTipText(provider.getDescription());
		button.setBackground(fScroller.getBackground());
		button.setSize(fIconSize + 4, fIconSize + 4);
		button.setLocation((index * (fIconSize + spacing) + spacing - fSpacing), 2);
		button.addPaintListener(e -> {
			if (fSelectedProvider == provider) {
				fSelectedProviderButton = button;
			}
			paintButton(e.gc, button, provider);
		});
		button.addListener(SWT.MouseEnter, event -> button.redraw());
		button.addListener(SWT.MouseExit, event -> button.redraw());
		button.addListener(SWT.MouseUp, event -> {
			if (fSelectedProvider == provider) {
				return;
			}
			fSelectedProvider = provider;
			if (fSelectedProviderButton != null && !fSelectedProviderButton.isDisposed()) {
				fSelectedProviderButton.redraw();
			}
			fSelectedProviderButton = button;
			fSelectedProviderButton.redraw();
			notifyListeners(provider);
		});
		if (fSelectedProvider == provider) {
			fSelectedProviderButton = button;
		}
		return button;
	}

	/**
	 * Updates the read count of the currently selected button.
	 */
	public void updateButton() {
		if (fSelectedProviderButton != null && !fSelectedProviderButton.isDisposed()) {
			getDisplay().asyncExec(() -> fSelectedProviderButton.redraw());
		}
	}

	/**
	 * Calls redraw on all buttons to update the badge counter.
	 */
	public void updateButtons() {
		if (!isDisposed()) {
			getDisplay().asyncExec(() -> {
				if (!fScroller.isDisposed()) {
					for (Control control : fScroller.getChildren()) {
						if (control instanceof Composite && !control.isDisposed()) {
							control.redraw();
						}
					}
				}
			});
		}
	}

	/**
	 * Sets the {@link TipManager}.
	 *
	 * @param tipManager
	 *            the {@link TipManager}
	 * @return this
	 */
	public Slider setTipManager(TipManager tipManager) {
		fTipManager = tipManager;
		fTipManager.getListenerManager().addProviderListener(fProviderListener);
		fIconSize = 48;
		load();
		return this;
	}

	private void notifyListeners(TipProvider provider) {
		fListeners.forEach(listener -> {
			try {
				listener.selected(provider);
			} catch (Exception e) {
				fTipManager.log(LogUtil.error(getClass(), e));
			}
		});
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point point = new Point(fIconSize * 3, fIconSize + fSpacing + fSpacing);
		return point;
	}

	protected void scrollRight() {
		if (fSliderIndex < fTipManager.getProviders().size() - 1) {
			fSliderIndex++;
			load();
		}
	}

	protected void scrollLeft() {
		if (fSliderIndex > 0) {
			fSliderIndex--;
			load();
		}
	}

	/**
	 * @return the current selected {@link TipProvider}.
	 */
	public TipProvider getTipProvider() {
		return fSelectedProvider;
	}

	/**
	 * Sets the passed provider as the selected provider in the slider.
	 *
	 * @param provider
	 *            the new provider for the slider.
	 *
	 * @return this
	 */
	public Slider setTipProvider(TipProvider provider) {
		fSelectedProvider = provider;
		updateButtons();
		return this;
	}

	/**
	 * Adds the listener to the list of listeners to be called when an event with a
	 * {@link TipProvider} occurs.
	 *
	 * @param listener
	 *            the {@link ProviderSelectionListener}
	 * @return this
	 */
	public Slider addTipProviderListener(ProviderSelectionListener listener) {
		fListeners.add(listener);
		return this;
	}

	/**
	 * Removes the listener from the list.
	 *
	 * @param listener
	 *            the {@link ProviderSelectionListener}
	 * @return this
	 */
	public Slider removeTipProviderListener(ProviderSelectionListener listener) {
		fListeners.remove(listener);
		return this;
	}

	private void paintButton(GC gc, Composite providerButton, TipProvider provider) {
		gc.setAdvanced(true);
		if (fSelectedProvider.equals(provider)) {
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
			gc.drawRectangle(0, 0, fIconSize + 3, fIconSize + 3);
		} else {
			gc.setForeground(fLeftButton.getForeground());
			gc.setBackground(fLeftButton.getBackground());
			boolean mouseIn = getDisplay().getCursorControl() == providerButton;
			if (mouseIn) {
				gc.drawRectangle(0, 0, fIconSize + 3, fIconSize + 3);
			} else {
				gc.setBackground(fScroller.getBackground());
			}
		}
		gc.fillRectangle(2, 2, fIconSize, fIconSize);
		Image overlay = getUnreadOverlay(providerButton, provider);
		gc.drawImage(overlay, 2, 2);
		if (overlay != getProviderImage(provider, selectProviderImage(provider))) {
			overlay.dispose();
		}
	}

	private TipImage selectProviderImage(TipProvider provider) {
		return provider.getImage();
	}

	private Image getProviderImage(TipProvider provider, TipImage image) {
		if (!fProviderImageCache.containsKey(provider.getID())) {
			try {
				fProviderImageCache.put(provider.getID(),
						new Image(getDisplay(), ImageUtil.decodeToImage(image.getBase64Image())));
			} catch (Exception e) {
				fTipManager.log(LogUtil.error(getClass(), e));
				return null;
			}
		}
		return fProviderImageCache.get(provider.getID());
	}

	private Image getUnreadOverlay(Composite providerButton, TipProvider provider) {
		if (provider.getTips(true).isEmpty()) {
			return getProviderImage(provider, selectProviderImage(provider));
		}
		GC gc2 = new GC(providerButton);
		gc2.setAdvanced(true);
		gc2.setFont(SWTResourceManager.getBoldFont(gc2.getFont()));
		int tipCount = provider.getTips(true).size();
		Point textExtent = gc2.textExtent(tipCount + "");
		gc2.dispose();

		Image image = null;
		if (tipCount > 9) {
			image = new Image(getDisplay(), textExtent.x + 8, textExtent.y + 5);
		} else {
			image = new Image(getDisplay(), textExtent.x + 15, textExtent.y + 5);
		}
		ImageData data = image.getImageData();
		data.transparentPixel = data.getPixel(0, 0);
		image.dispose();
		image = new Image(getDisplay(), data);
		GC gc = new GC(image);
		gc.setAdvanced(true);
		if (fTipManager.mustServeReadTips()) {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
		} else {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
		}
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.setFont(SWTResourceManager.getBoldFont(gc.getFont()));
		gc.setAlpha(210);
		gc.setTextAntialias(SWT.ON);
		if (tipCount > 9) {
			gc.fillOval(0, 0, textExtent.x + 8, textExtent.y + 5);
			gc.drawText(tipCount + "", 4, 2, true);
		} else {
			gc.fillOval(0, 0, textExtent.x + 15, textExtent.y + 5);
			gc.drawText(tipCount + "", 8, 2, true);
		}
		Image result = ResourceManager.decorateImage(getProviderImage(provider, selectProviderImage(provider)), image,
				SWTResourceManager.TOP_RIGHT);
		image.dispose();
		gc.dispose();
		return result;
	}
}