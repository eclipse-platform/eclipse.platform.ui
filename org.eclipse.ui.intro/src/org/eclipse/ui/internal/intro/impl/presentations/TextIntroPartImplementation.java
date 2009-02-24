/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.presentations;

import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;
import org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.util.IntroModelSerializer;
import org.eclipse.ui.intro.config.IIntroContentProvider;

/**
 * This is an Text based implementation of an Intro Part. It simply walks the
 * model and prints the content of pages. It is used for debugging.
 */
public class TextIntroPartImplementation extends
        AbstractIntroPartImplementation {


    public void doStandbyStateChanged(boolean standby,
            boolean isStandbyPartNeeded) {
        // no-op
    }

    public void createPartControl(Composite container) {
        Text text = new Text(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        IntroModelRoot model = IntroPlugin.getDefault().getIntroModelRoot();
        IntroModelSerializer serializer = new IntroModelSerializer(model);
        text.setText(serializer.toString());
        addToolBarActions();
    }

    protected void updateNavigationActionsState() {
        // no-op
    }


    public void setFocus() {
        // no-op
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateBackward()
     */
    public boolean navigateBackward() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateForward()
     */
    public boolean navigateForward() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#handleRegistryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
     */
    protected void handleRegistryChanged(IRegistryChangeEvent event) {
        // no-op
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation#navigateHome()
     */
    public boolean navigateHome() {
        return false;
    }

    public void reflow(IIntroContentProvider provider, boolean incremental) {
        // no-op
    }
}
