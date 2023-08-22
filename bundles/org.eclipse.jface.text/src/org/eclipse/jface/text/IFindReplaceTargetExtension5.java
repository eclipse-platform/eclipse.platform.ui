package org.eclipse.jface.text;

import org.eclipse.swt.widgets.Composite;

/**
 * Extension to search and replace using an "inline"-Find replace Bar. In order for this inline
 * find-replace bar to work, inheriting panes need to provide a way to inject a "toolbar".
 *
 * In the future, extension could be elevated to a "IInlineToolBarExtension", in case there are
 * other tools that want to profit from this extension
 *
 * @since 3.24
 */
public interface IFindReplaceTargetExtension5 {

	/**
	 * This is called every time the inline find-replace dialog is to be shown. The implementing
	 * class guarantees to then provide a composite onto which the inline search is drawn.
	 *
	 * The composite is expected to have a GridLayout with a single column. Neither the width nor
	 * the height are specified (XXX ?).
	 *
	 * TODO: I'm unhappy with the name, but it seems consistent with the current status Quo defined
	 * in IFindReplaceTargetExtension
	 *
	 * @return the composite the inline find-replace dialog may draw on
	 */
	public Composite beginInlineSession();

	/**
	 * Updates the layout of the implementing class. Is required, for example, when the
	 * "replace"-dropdown is opened/closed
	 *
	 * TODO: I'd like to avoid having this method. Is there a better way?
	 */
	public void updateLayout();

	/**
	 * The implementing class may now dispose of the composite created in beginInlineSession.
	 *
	 */
	public void endInlineSession();
}
