package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.util.Assert;

public class DecoratingLabelProvider extends LabelProvider {
	private ILabelProvider provider;
	private ILabelDecorator decorator;
/**
 * Creates a decorating label provider which uses the given label decorator
 * to decorate labels provided by the given label provider.
 *
 * @param provider the nested label provider
 * @param decorator the label decorator
 */
public DecoratingLabelProvider(ILabelProvider provider, ILabelDecorator decorator) {
	Assert.isNotNull(provider);
	Assert.isNotNull(decorator);
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
	provider.addListener(listener);
	decorator.addListener(listener);
}
/**
 * The <code>DecoratingLabelProvider</code> implementation of this <code>IBaseLabelProvider</code> method
 * disposes both the nested label provider and the label decorator.
 */
public void dispose() {
	provider.dispose();
	decorator.dispose();
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
	Image decorated = decorator.decorateImage(image, element);
	return decorated != null ? decorated : image;
}
/**
 * Returns the label decorator.
 *
 * @return the label decorator
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
	String decorated = decorator.decorateText(text, element);
	return decorated != null ? decorated : text;
}
/**
 * The <code>DecoratingLabelProvider</code> implementation of this 
 * <code>IBaseLabelProvider</code> method returns <code>true</code> if the corresponding method
 * on the nested label provider returns <code>true</code> or if the corresponding method on the 
 * decorator returns <code>true</code>.
 */
public boolean isLabelProperty(Object element, String property) {
	return provider.isLabelProperty(element, property) || decorator.isLabelProperty(element, property);
}
/**
 * The <code>DecoratingLabelProvider</code> implementation of this <code>IBaseLabelProvider</code> method
 * removes the listener from both the nested label provider and the label decorator.
 *
 * @param listener a label provider listener
 */
public void removeListener(ILabelProviderListener listener) {
	provider.removeListener(listener);
	decorator.removeListener(listener);
}
}
