package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.ListenerList;

/**
 * A decorating label provider is a label provider which combines 
 * a nested label provider and an optional decorator.
 * The decorator decorates the label text and image provided by the nested label provider.
 */
public class DecoratingLabelProvider
	extends LabelProvider
	implements ICombinedLabelProvider {
	private ILabelProvider provider;
	private ILabelDecorator decorator;
	// Need to keep our own list of listeners
	private ListenerList listeners = new ListenerList();
	/**
	 * Creates a decorating label provider which uses the given label decorator
	 * to decorate labels provided by the given label provider.
	 *
	 * @param provider the nested label provider
	 * @param decorator the label decorator, or <code>null</code> if no decorator is to be used initially
	 */
	public DecoratingLabelProvider(
		ILabelProvider provider,
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
	public Image getImage(Object element) {
		Image image = provider.getImage(element);
		if (decorator != null) {
			Image decorated = decorator.decorateImage(image, element);
			if (decorated != null) {
				return decorated;
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
	public String getText(Object element) {
		String text = provider.getText(element);
		if (decorator != null) {
			String decorated = decorator.decorateText(text, element);
			if (decorated != null) {
				return decorated;
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
	public boolean isLabelProperty(Object element, String property) {
		if (provider.isLabelProperty(element, property))
			return true;
		if (decorator != null && decorator.isLabelProperty(element, property))
			return true;
		return false;
	}
	/**
	 * The <code>DecoratingLabelProvider</code> implementation of this <code>IBaseLabelProvider</code> method
	 * removes the listener from both the nested label provider and the label decorator.
	 *
	 * @param listener a label provider listener
	 */
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
			Object[] listeners = this.listeners.getListeners();
			if (oldDecorator != null) {
				for (int i = 0; i < listeners.length; ++i) {
					oldDecorator.removeListener((ILabelProviderListener) listeners[i]);
				}
			}
			this.decorator = decorator;
			if (decorator != null) {
				for (int i = 0; i < listeners.length; ++i) {
					decorator.addListener((ILabelProviderListener) listeners[i]);
				}
			}
			fireLabelProviderChanged(new LabelProviderChangedEvent(this));
		}
	}

	/**
	 * @see ICombinedLabelProvider#getCombinedLabel(Object)
	 */
	public CombinedLabel getCombinedLabel(Object element) {

		CombinedLabel label;
		if (provider instanceof ICombinedLabelProvider)
			label = ((ICombinedLabelProvider) provider).getCombinedLabel(element);
		else
			label =
				new CombinedLabel(provider.getText(element), provider.getImage(element));

		if (decorator != null) {
			if (decorator instanceof ICombinedLabelDecorator)
				 ((ICombinedLabelDecorator) decorator).decorateLabel(element, label);
			else {
				String decoratedText = decorator.decorateText(label.getText(), element);
				if (decoratedText != null) {
					label.setText(decoratedText);
				}
				Image decoratedImage = decorator.decorateImage(label.getImage(), element);
				if (decoratedImage != null) {
					label.setImage(decoratedImage);
				}
			}

		}
		return label;
	}

}