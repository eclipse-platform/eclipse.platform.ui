package org.eclipse.ui.internal.console;

import org.eclipse.jface.text.Position;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleHyperlink;

public class IOConsoleHyperlinkPosition extends Position {
	
	public static final String HYPER_LINK_CATEGORY = ConsolePlugin.getUniqueIdentifier() + ".HYPER_LINK"; //$NON-NLS-1$
	
	private IConsoleHyperlink fLink = null;

	public IOConsoleHyperlinkPosition(IConsoleHyperlink link, int offset, int length) {
		super(offset, length);
		fLink = link;
	}
	
	public IConsoleHyperlink getHyperLink() {
		return fLink;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg) {
		return arg instanceof IOConsoleHyperlinkPosition && super.equals(arg) && getHyperLink().equals(((IOConsoleHyperlinkPosition)arg).getHyperLink());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() + getHyperLink().hashCode();
	}

}