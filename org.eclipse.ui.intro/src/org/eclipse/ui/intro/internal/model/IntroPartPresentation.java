/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.intro.internal.model;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.intro.internal.util.*;

/**
 * This class models the presentation element contributed to a config extension
 * point. The Presentation class delegates UI creation to the actual
 * Implementation class, and passes the IntroPart along to this implementation
 * class.
 */
public class IntroPartPresentation extends IntroElement {

	protected static final String PRESENTATION_ELEMENT = "presentation";

	private static final String IMPLEMENTATION_ELEMENT = "implementation";
	private static final String TITLE_ATTRIBUTE = "title";
	private static final String STYLE_ATTRIBUTE = "style";
	private static final String OS_ATTRIBUTE = "os";
	private static final String HOME_PAGE_ID_ATTRIBUTE = "home-page-id";

	private String title;
	private String style;
	private String homePageId;

	// config element representing the implementation element contributed in
	// this presentation. If null, it means no valid implementation found.
	private IConfigurationElement implementationElement;

	private AbstractIntroPart implementation;

	// CustomizableIntroPart instance. Passed to the Implementation classes.
	private IIntroPart introPart;

	/**
	 *  
	 */
	IntroPartPresentation(IConfigurationElement element) {
		super(element);
		title = element.getAttribute(TITLE_ATTRIBUTE);
		homePageId = element.getAttribute(HOME_PAGE_ID_ATTRIBUTE);
		implementationElement = getImplementationElement(element);
		if (implementationElement != null) {
			style = implementationElement.getAttribute(STYLE_ATTRIBUTE);
			// reset id to be implementation ID.
			id = implementationElement.getAttribute(ID_ATTRIBUTE);
			// Resolve.
			style = IntroModelRoot.resolveURL(style, element);
		}
	}

	/**
	 * @param introPart
	 * @throws PartInitException
	 */
	public void init(IIntroPart introPart) throws PartInitException {
		// REVISIT: Called when the actual UI needs to be created. Incomplete
		// separation of model / UI. Will change later.
		// should not get here if there is no valid implementation.
		this.introPart = introPart;
		implementation = createIntroPartImplementation(implementationElement);
		implementation.init(introPart);
	}

	/**
	 * Returns the style associated with the Presentation. May be null if no
	 * shared presentation style is needed, or in the case of static HTML OOBE.
	 * 
	 * @return Returns the style.
	 */
	public String getStyle() {
		return style;
	}

	public String getId() {
		return id;
	}

	/**
	 * Returns the title associated with the Presentation. May be null if no
	 * title is defined
	 * 
	 * @return Returns the presentation title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Creates the UI based on the implementation class. .
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		implementation.createPartControl(parent);
	}

	/**
	 * Retruns the implementation element of the config. Choose correct
	 * implementation element based on os atrributes. Rules: get current OS,
	 * choose first contributrion, with os that matches OS. Otherwise, choose
	 * first contribution with no os. Returns null if no valid implementation
	 * is found.
	 */
	private IConfigurationElement getImplementationElement(IConfigurationElement configElement) {
		try {
			// There can be more than one implementation contribution. Pick one
			// depending on OS.
			IConfigurationElement[] implementationElements =
				configElement.getChildren(IMPLEMENTATION_ELEMENT);
			IConfigurationElement implementationElement = null;

			if (implementationElements.length == 0)
				// no contributions. done.
				return null;

			String currentOS = Platform.getOS();
			// first loop through all to find one with matching OS.
			for (int i = 0; i < implementationElements.length; i++) {
				String os =
					implementationElements[i].getAttribute(OS_ATTRIBUTE);
				if (os == null)
					// no os, no match.
					continue;
				if (os.equalsIgnoreCase(currentOS)) {
					// found implementation with correct OS.
					implementationElement = implementationElements[i];
					break;
				}
			}

			// if implementation element is still null, it means no
			// contribution matches OS. Pick first one, that does not have an
			// OS.
			if (implementationElement == null)
				for (int i = 0; i < implementationElements.length; i++) {
					String os =
						implementationElements[i].getAttribute(OS_ATTRIBUTE);
					if (os == null)
						// no os, choose this one.
						implementationElement = implementationElements[i];
				}

			if (implementationElement != null)
				Logger.logInfo("Loaded the Config implementation.");
			return implementationElement;

		} catch (Exception e) {
			Util.handleException(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Creates the actual implementation class, and primes it with the into
	 * part.
	 * 
	 * @return Returns the partConfigElement representing the single Intro
	 *         config.
	 */
	private AbstractIntroPart createIntroPartImplementation(IConfigurationElement configElement) {
		if (configElement == null)
			return null;
		AbstractIntroPart implementation = null;
		try {
			implementation =
				(AbstractIntroPart) configElement.createExecutableExtension(
					"class");
		} catch (Exception e) {
			Util.handleException(
				"Could not instantiate implementation class "
					+ configElement.getAttribute("class"),
				e);
		}
		return implementation;
	}

	/**
	 * @return Returns the introPart.
	 */
	public IIntroPart getIntroPart() {
		return introPart;
	}

	public void setFocus() {
		if (implementation != null)
			implementation.setFocus();
	}

	/**
	 * Called when the IntroPart is disposed. Forwards the call to the
	 * implementation class.
	 */
	public void dispose() {
		if (implementation != null)
			implementation.dispose();
	}

	/**
	 * Forwards the call to the implementation class.
	 */
	public void updateHistory(String pageId) {
		if (implementation != null)
			implementation.updateHistory(pageId);
	}

	/**
	 * @return Returns the implementationElement.
	 */
	IConfigurationElement getImplementationElement() {
		return implementationElement;
	}

	/**
	 * @return Returns the homePageId.
	 */
	public String getHomePageId() {
		return homePageId;
	}

	/**
	 * @param homePageId
	 *            The homePageId to set.
	 */
	public void setHomePageId(String homePageId) {
		this.homePageId = homePageId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.intro.internal.model.IntroElement#getType()
	 */
	public int getType() {
		return IntroElement.PRESENTATION;
	}

}
