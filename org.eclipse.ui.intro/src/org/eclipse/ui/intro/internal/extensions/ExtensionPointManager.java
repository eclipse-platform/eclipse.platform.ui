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

package org.eclipse.ui.intro.internal.extensions;

import org.eclipse.ui.intro.internal.model.*;

/**
 * Manages all Intro plugin extension points. Currently, there is only one:
 * org.eclipse.ui.intro.config. <br>The model is lazily loaded on per need
 * basis. This happens when a page is asked for its children, or when the model
 * is trying to resolve an include.
 */

public class ExtensionPointManager extends BaseExtensionPointManager {

	// singleton instance. Can be retrieved from here or from the Intro Plugin.
	private static ExtensionPointManager inst = new ExtensionPointManager();

	// The root model class that represents a full/combined OOBBE config.
	private IntroModelRoot currentModel;

	// the id of the intro part who's model (config) we are trying to
	// load.
	private String introId;

	// cache if we loaded extension.
	private boolean currentModelLoaded;

	/*
	 * Prevent creation.
	 */
	private ExtensionPointManager() {
		super();
	}

	/**
	 * @return Returns the inst.
	 */
	public static ExtensionPointManager getInst() {
		return inst;
	}

	/**
	 * Load the intro model given the current intro id.
	 */
	private void loadCurrentModel() {
		currentModel = loadModel(CONFIG_INTRO_ID_ATTRIBUTE, this.introId);
		currentModelLoaded = true;
	}

	/**
	 * @return Returns the Intro Model root. NOte: Prefereed way of getting to
	 *         the intro model root is throught the intro plugin.
	 */
	public IntroModelRoot getCurrentModel() {
		if (!currentModelLoaded)
			// we load only once.
			loadCurrentModel();
		return currentModel;
	}

	/**
	 * Load an intro model given a config id.
	 * 
	 * @param configId
	 * @return
	 */
	public IntroModelRoot getModel(String configId) {
		IntroModelRoot model = getCachedModel(configId);
		if (model == null)
			// we never loaded this model before, or we tried before and we
			// failed. Load it. Get the correct config element based on
			// configId, and log any extra contributions.
			model = loadModel(ID_ATTRIBUTE, configId);
		return model;
	}

	/**
	 * @param introPartId
	 *            The introPartId to set.
	 */
	public void setIntroId(String introPartId) {
		this.introId = introPartId;
		// we do not have to clear model here. Just the current model instance.
		this.currentModelLoaded = false;
	}

	public void clear() {
		currentModel = null;
		currentModelLoaded = false;
		introModels.clear();
	}

}