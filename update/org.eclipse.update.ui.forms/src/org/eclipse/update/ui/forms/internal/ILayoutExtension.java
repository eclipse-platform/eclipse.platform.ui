package org.eclipse.update.ui.forms.internal;

import org.eclipse.swt.widgets.Composite;

/**
 * Classes that extend abstract class Layout and implement
 * this interface can take part in layout computation of
 * the HTMLTableLayout manager. The said layout uses
 * alternative algorithm that computes columns before rows.
 * It allows it to 'flow' wrapped text proportionally
 * (as in the popular web browsers). Custom layout managers
 * that implement this interface allow recursive reflow
 * to be performed.
 */
public interface ILayoutExtension {
	public int getMinimumWidth(Composite parent, boolean changed);
	public int getMaximumWidth(Composite parent, boolean changed); 
}
