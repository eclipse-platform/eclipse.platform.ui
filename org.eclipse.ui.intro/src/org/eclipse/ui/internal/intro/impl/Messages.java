/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String INTRO_RESOURCE_BUNDLE = "org.eclipse.ui.internal.intro.impl.IntroPluginResources"; //$NON-NLS-1$

    static {
        initializeMessages(INTRO_RESOURCE_BUNDLE, Messages.class);
    }

    public static String Browser_homeButton_tooltip;
    public static String Browser_forwardButton_tooltip;
    public static String Browser_backwardButton_tooltip;
    public static String Browser_invalidConfig;

    // Dialogs
    // --------
    public static String MessageDialog_errorTitle;
    public static String MessageDialog_warningTitle;
    public static String MessageDialog_infoTitle;

    public static String CustomizableIntroPart_configNotFound;
    public static String StandbyPart_returnToIntro;
    public static String StandbyPart_returnTo;
    public static String EmptyStandbyContentPart_text;
    public static String StandbyPart_failedToCreate;
    public static String StandbyPart_nonDefined;
    public static String StandbyPart_canNotRestore;

    public static String IntroURL_failedToDecode;
    public static String IntroURL_badCommand;
    public static String HyperlinkAdapter_urlIs;
    public static String HTML_embeddedLink;
    public static String StaticHTML_welcome;

    public static String IntroLaunchBar_close_label;
    public static String IntroLaunchBar_close_tooltip;
    public static String IntroLaunchBar_restore_tooltip;


    // Misc
    // -------
    public static String IntroPart_showContentButton_tooltip;




}
