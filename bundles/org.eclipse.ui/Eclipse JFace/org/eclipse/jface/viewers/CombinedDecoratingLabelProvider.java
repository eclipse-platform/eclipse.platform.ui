package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Image;

/**
 * The CombinedDecoratingLabelProvider is a Labelprovider that
 * implements ICombinedLabelProvider.
 */

public class CombinedDecoratingLabelProvider
	extends DecoratingLabelProvider
	implements ICombinedLabelProvider {

	/**
	 * Construct for CombinedDecoratingLabelProvider.
	 * @param provider
	 * @param decorator
	 */
	public CombinedDecoratingLabelProvider(
		ILabelProvider provider,
		ILabelDecorator decorator) {
		super(provider, decorator);
	}
	/**
	 * @see ICombinedLabelProvider#getCombinedLabel(Object)
	 */
	public CombinedLabel getCombinedLabel(Object element) {

		CombinedLabel label;
		ILabelProvider provider = getLabelProvider();
		ILabelDecorator decorator = getLabelDecorator();
		if (provider instanceof ICombinedLabelProvider)
			label = ((ICombinedLabelProvider) provider).getCombinedLabel(element);
		else
			label =
				new CombinedLabel(provider.getText(element), provider.getImage(element));

		if (getLabelDecorator() != null) {
			if (getLabelDecorator() instanceof ICombinedLabelDecorator)
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