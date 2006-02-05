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
	public static String SharedIntroConfigurer_overview_name;
	public static String SharedIntroConfigurer_overview_alt;
	public static String SharedIntroConfigurer_overview_tooltip;
	public static String SharedIntroConfigurer_firststeps_name;
	public static String SharedIntroConfigurer_firststeps_alt;
	public static String SharedIntroConfigurer_firststeps_tooltip;
	public static String SharedIntroConfigurer_tutorials_name;
	public static String SharedIntroConfigurer_tutorials_alt;
	public static String SharedIntroConfigurer_tutorials_tooltip;
	public static String SharedIntroConfigurer_samples_name;
	public static String SharedIntroConfigurer_samples_alt;
	public static String SharedIntroConfigurer_samples_tooltip;
	public static String SharedIntroConfigurer_whatsnew_name;
	public static String SharedIntroConfigurer_whatsnew_alt;
	public static String SharedIntroConfigurer_whatsnew_tooltip;
	public static String SharedIntroConfigurer_migrate_name;
	public static String SharedIntroConfigurer_migrate_alt;
	public static String SharedIntroConfigurer_migrate_tooltip;
	public static String SharedIntroConfigurer_webresources_name;
	public static String SharedIntroConfigurer_webresources_alt;
	public static String SharedIntroConfigurer_webresources_tooltip;
	public static String SharedIntroConfigurer_overview_nav;
	public static String SharedIntroConfigurer_firststeps_nav;
	public static String SharedIntroConfigurer_tutorials_nav;
	public static String SharedIntroConfigurer_samples_nav;
	public static String SharedIntroConfigurer_whatsnew_nav;
	public static String SharedIntroConfigurer_migrate_nav;
	public static String SharedIntroConfigurer_webresources_nav;
	public static String WelcomeCustomizationPreferencePage_available;
	public static String WelcomeCustomizationPreferencePage_left;
	public static String WelcomeCustomizationPreferencePage_right;
	public static String WelcomeCustomizationPreferencePage_bottom;
	public static String WelcomeCustomizationPreferencePage_background;
	public static String WelcomeCustomizationPreferencePage_preview;
	public static String WelcomeCustomizationPreferencePage_rootpages;
	public static String WelcomeCustomizationPreferencePage_overview;
	public static String WelcomeCustomizationPreferencePage_firststeps;
	public static String WelcomeCustomizationPreferencePage_tutorials;
	public static String WelcomeCustomizationPreferencePage_samples;
	public static String WelcomeCustomizationPreferencePage_whatsnew;
	public static String WelcomeCustomizationPreferencePage_webresources;
	public static String WelcomeCustomizationPreferencePage_migrate;
	public static String WelcomeCustomizationPreferencePage_applyToAll;
	public static String ExtensionData_callout;
	public static String ExtensionData_low;
	public static String ExtensionData_medium;
	public static String ExtensionData_high;
	public static String ExtensionData_new;
	public static String WelcomeCustomizationPreferencePage_serialize;
	public static String WelcomeCustomizationPreferencePage_serializeTitle;
	public static String WelcomeCustomizationPreferencePage_serializeMessage;




}
