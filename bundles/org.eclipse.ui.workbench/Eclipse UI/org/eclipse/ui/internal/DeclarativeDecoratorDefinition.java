package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * The DeclarativeDecoratorDefinition is a decorator 
 * definition that is defined entirely from xml and
 * will not require the activation of its defining 
 * plug-in.
 */
class DeclarativeDecoratorDefinition extends DecoratorDefinition {

	/**
	 * The DeclarativeDecorator is the internal decorator
	 * supplied by the decorator definition.
	 */
	private ILabelDecorator decorator = new ILabelDecorator() {

		/**
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
		 */
		public Image decorateImage(Image image, Object element) {
			return image;
		}

		/**
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
		 */
		public String decorateText(String text, Object element) {
			return text;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
		}

	};
	private String quadrant;
	private String iconLocation;

	DeclarativeDecoratorDefinition(
		String identifier,
		String label,
		String decoratorDescription,
		ActionExpression expression,
		boolean isAdaptable,
		boolean initEnabled,
		String quadrantValue,
		String iconPath) {
		super(
			identifier,
			label,
			decoratorDescription,
			expression,
			isAdaptable,
			initEnabled);
		this.iconLocation = iconPath;
		this.quadrant = quadrantValue;
	}

	/**
	 * @see org.eclipse.ui.internal.DecoratorDefinition#internalGetDecorator()
	 */
	protected ILabelDecorator internalGetDecorator() throws CoreException {
		return decorator;
	}

}
