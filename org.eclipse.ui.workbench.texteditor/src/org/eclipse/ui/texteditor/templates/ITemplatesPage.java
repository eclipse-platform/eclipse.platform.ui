/*******************************************************************************
 * Copyright (c) 2007, 2008 Dakshinamurthy Karra and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dakshinamurthy Karra (Jalian Systems) - Templates View - https://bugs.eclipse.org/bugs/show_bug.cgi?id=69581
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import org.eclipse.ui.part.IPageBookViewPage;


/**
 * Interface for a templates page. This interface defines the minimum requirement
 * for a page within the {@link TemplatesView}.
 * <p>
 * Clients can either implement this interface directly or subclass the {@link AbstractTemplatesPage}.
 * 
 * @see TemplatesView
 * @since 3.4
 */
public interface ITemplatesPage extends IPageBookViewPage {
}
