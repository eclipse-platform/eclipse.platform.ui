/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Alexander Kurtakov <akurtako@redhat.com> - Bug 459761
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * A decorating label provider is a label provider which combines
 * a nested label provider and an optional decorator.
 * The decorator decorates the label text, image, font and colors provided by
 * the nested label provider.
 */
public class DecoratingLabelProvider extends LabelProvider implements IViewerLabelProvider, IColorProvider,
		IFontProvider, ITreePathLabelProvider {

	private final ILabelProvider provider;

	private ILabelDecorator decorator;

	// Need to keep our own list of listeners
	private final ListenerList<ILabelProviderListener> listeners = new ListenerList<>();

	private IDecorationContext decorationContext = DecorationContext.DEFAULT_CONTEXT;

	/**
	 * Creates a decorating label provider which uses the given label decorator
	 * to decorate labels provided by the given label provider.
	 *
	 * @param provider the nested label provider
	 * @param decorator the label decorator, or <code>null</code> if no decorator is to be used initially
	 */
	public DecoratingLabelProvider(ILabelProvider provider,
			ILabelDecorator decorator) {
		Assert.isNotNull(provider);
		this.provider = provider;
		this.decorator = decorator;
	}

	/**
	 * The <code>DecoratingLabelProvider</code> implementation of this <code>IBaseLabelProvider</code> method
	 * adds the listener to both the nested label provider and the label decorator.
	 *
	 * @param listener a label provider listener
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
		super.addListener(listener);
		provider.addListener(listener);
		if (decorator != null) {
			decorator.addListener(listener);
		}
		listeners.add(listener);
	}

	/**
	 * The <code>DecoratingLabelProvider</code> implementation of this <code>IBaseLabelProvider</code> method
	 * disposes both the nested label provider and the label decorator.
	 */
	@Override
	public void dispose() {
		provider.dispose();
		if (decorator != null) {
			decorator.dispose();
		}
	}

	/**
	 * The <code>DecoratingLabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns the image provided
	 * by the nested label provider's <code>getImage</code> method,
	 * decorated with the decoration provided by the label decorator's
	 * <code>decorateImage</code> method.
	 */
	@Override
	public Image getImage(Object element) {
		Image image = provider.getImage(element);
		if (decorator != null) {
			if (decorator instanceof LabelDecorator ld2) {
				Image decorated = ld2.decorateImage(image, element, getDecorationContext());
				if (decorated != null) {
					return decorated;
				}
			} else {
				Image decorated = decorator.decorateImage(image, element);
				if (decorated != null) {
					return decorated;
				}
			}
		}
		return image;
	}

	/**
	 * Returns the label decorator, or <code>null</code> if none has been set.
	 *
	 * @return the label decorator, or <code>null</code> if none has been set.
	 */
	public ILabelDecorator getLabelDecorator() {
		return decorator;
	}

	/**
	 * Returns the nested label provider.
	 *
	 * @return the nested label provider
	 */
	public ILabelProvider getLabelProvider() {
		return provider;
	}

	/**
	 * The <code>DecoratingLabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns the text label provided
	 * by the nested label provider's <code>getText</code> method,
	 * decorated with the decoration provided by the label decorator's
	 * <code>decorateText</code> method.
	 */
	@Override
	public String getText(Object element) {
		String text = provider.getText(element);
		if (decorator != null) {
			if (decorator instanceof LabelDecorator ld2) {
				String decorated = ld2.decorateText(text, element, getDecorationContext());
				if (decorated != null) {
					return decorated;
				}
			} else {
				String decorated = decorator.decorateText(text, element);
				if (decorated != null) {
					return decorated;
				}
			}
		}
		return text;
	}

	/**
	 * The <code>DecoratingLabelProvider</code> implementation of this
	 * <code>IBaseLabelProvider</code> method returns <code>true</code> if the corresponding method
	 * on the nested label provider returns <code>true</code> or if the corresponding method on the
	 * decorator returns <code>true</code>.
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		if (provider.isLabelProperty(element, property)) {
			return true;
		}
		if (decorator != null && decorator.isLabelProperty(element, property)) {
			return true;
		}
		return false;
	}

	/**
	 * The <code>DecoratingLabelProvider</code> implementation of this <code>IBaseLabelProvider</code> method
	 * removes the listener from both the nested label provider and the label decorator.
	 *
	 * @param listener a label provider listener
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
		super.removeListener(listener);
		provider.removeListener(listener);
		if (decorator != null) {
			decorator.removeListener(listener);
		}
		listeners.remove(listener);
	}

	/**
	 * Sets the label decorator.
	 * Removes all known listeners from the old decorator, and adds all known listeners to the new decorator.
	 * The old decorator is not disposed.
	 * Fires a label provider changed event indicating that all labels should be updated.
	 * Has no effect if the given decorator is identical to the current one.
	 *
	 * @param decorator the label decorator, or <code>null</code> if no decorations are to be applied
	 */
	public void setLabelDecorator(ILabelDecorator decorator) {
		ILabelDecorator oldDecorator = this.decorator;
		if (oldDecorator != decorator) {
			if (oldDecorator != null) {
				for (ILabelProviderListener l : listeners) {
					oldDecorator.removeListener(l);
				}
			}
			this.decorator = decorator;
			if (decorator != null) {
				for (ILabelProviderListener l : listeners) {
					decorator.addListener(l);
				}
			}
			fireLabelProviderChanged(new LabelProviderChangedEvent(this));
		}
	}

	@Override
	public void updateLabel(ViewerLabel settings, Object element) {

		ILabelDecorator currentDecorator = getLabelDecorator();
		String oldText = settings.getText();
		boolean decorationReady = true;
		if (currentDecorator instanceof IDelayedLabelDecorator delayedDecorator) {
			if (!delayedDecorator.prepareDecoration(element, oldText)) {
				// The decoration is not ready but has been queued for processing
				decorationReady = false;
			}
		}
		// update icon and label

		if (decorationReady || oldText == null
				|| settings.getText().isEmpty()) {
			settings.setText(getText(element));
		}

		Image oldImage = settings.getImage();
		if (decorationReady || oldImage == null) {
			settings.setImage(getImage(element));
		}

		if(decorationReady) {
			updateForDecorationReady(settings,element);
		}

	}

	/**
	 * Decoration is ready. Update anything else for the settings.
	 * @param settings The object collecting the settings.
	 * @param element The Object being decorated.
	 * @since 3.1
	 */
	protected void updateForDecorationReady(ViewerLabel settings, Object element) {

		if(decorator instanceof IColorDecorator colorDecorator){
			settings.setBackground(colorDecorator.decorateBackground(element));
			settings.setForeground(colorDecorator.decorateForeground(element));
		}

		if(decorator instanceof IFontDecorator) {
			settings.setFont(((IFontDecorator) decorator).decorateFont(element));
		}

	}

	@Override
	public Color getBackground(Object element) {
		if(provider instanceof IColorProvider) {
			return ((IColorProvider) provider).getBackground(element);
		}
		return null;
	}

	@Override
	public Font getFont(Object element) {
		if(provider instanceof IFontProvider) {
			return ((IFontProvider) provider).getFont(element);
		}
		return null;
	}

	@Override
	public Color getForeground(Object element) {
		if(provider instanceof IColorProvider) {
			return ((IColorProvider) provider).getForeground(element);
		}
		return null;
	}

	/**
	 * Return the decoration context associated with this label provider.
	 * It will be passed to the decorator if the decorator is an
	 * instance of {@link LabelDecorator}.
	 * @return the decoration context associated with this label provider
	 *
	 * @since 3.2
	 */
	public IDecorationContext getDecorationContext() {
		return decorationContext;
	}

	/**
	 * Set the decoration context that will be based to the decorator
	 * for this label provider if that decorator implements {@link LabelDecorator}.
	 * @param decorationContext the decoration context.
	 *
	 * @since 3.2
	 */
	public void setDecorationContext(IDecorationContext decorationContext) {
		Assert.isNotNull(decorationContext);
		this.decorationContext = decorationContext;
	}

	@Override
	public void updateLabel(ViewerLabel settings, TreePath elementPath) {
		ILabelDecorator currentDecorator = getLabelDecorator();
		String oldText = settings.getText();
		Object element = elementPath.getLastSegment();
		boolean decorationReady = true;
		if (currentDecorator instanceof LabelDecorator labelDecorator) {
			if (!labelDecorator.prepareDecoration(element, oldText, getDecorationContext())) {
				// The decoration is not ready but has been queued for processing
				decorationReady = false;
			}
		} else if (currentDecorator instanceof IDelayedLabelDecorator delayedDecorator) {
			if (!delayedDecorator.prepareDecoration(element, oldText)) {
				// The decoration is not ready but has been queued for processing
				decorationReady = false;
			}
		}
		settings.setHasPendingDecorations(!decorationReady);
		// update icon and label

		if (provider instanceof ITreePathLabelProvider pprov) {
			if (decorationReady || oldText == null
					|| settings.getText().isEmpty()) {
				pprov.updateLabel(settings, elementPath);
				decorateSettings(settings, elementPath);
			}
		} else {
			if (decorationReady || oldText == null
					|| settings.getText().isEmpty()) {
				settings.setText(getText(element));
			}

			Image oldImage = settings.getImage();
			if (decorationReady || oldImage == null) {
				settings.setImage(getImage(element));
			}

			if(decorationReady) {
				updateForDecorationReady(settings,element);
			}
		}

	}

	/**
	 * Decorate the settings
	 * @param settings the settings obtained from the label provider
	 * @param elementPath the element path being decorated
	 */
	private void decorateSettings(ViewerLabel settings, TreePath elementPath) {
		Object element = elementPath.getLastSegment();
		if (decorator != null) {
			if (decorator instanceof LabelDecorator labelDecorator) {
				String text = labelDecorator.decorateText(settings.getText(), element, getDecorationContext());
				if (text != null && text.length() > 0) {
					settings.setText(text);
				}
				Image image = labelDecorator.decorateImage(settings.getImage(), element, getDecorationContext());
				if (image != null) {
					settings.setImage(image);
				}

			} else {
				String text = decorator.decorateText(settings.getText(), element);
				if (text != null && text.length() > 0) {
					settings.setText(text);
				}
				Image image = decorator.decorateImage(settings.getImage(), element);
				if (image != null) {
					settings.setImage(image);
				}
			}
			if(decorator instanceof IColorDecorator colorDecorator){
				Color background = colorDecorator.decorateBackground(element);
				if (background != null) {
					settings.setBackground(background);
				}
				Color foreground = colorDecorator.decorateForeground(element);
				if (foreground != null) {
					settings.setForeground(foreground);
				}
			}

			if(decorator instanceof IFontDecorator) {
				Font font = ((IFontDecorator) decorator).decorateFont(element);
				if (font != null) {
					settings.setFont(font);
				}
			}
		}
	}
}
