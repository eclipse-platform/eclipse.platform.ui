/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.externaltools.internal.ant.antview.core.IAntViewConstants;
import org.eclipse.ui.externaltools.internal.ant.antview.preferences.Preferences;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntView;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;

public class MsgLvlAction extends Action {
	private String displayLevel;

	/**
	 * Constructor for Action
	 */
	public MsgLvlAction(String label, String displayLevel) {
		super(label);
		this.displayLevel = displayLevel;
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
        AntView antView = AntUtil.getAntView();
        if (antView == null) {
        	return;
        }
		Action prevLevelAction = (Action) antView.getMsgLevelAction(
		            Preferences.getString(IAntViewConstants.PREF_ANT_DISPLAY));
		if (null != prevLevelAction) prevLevelAction.setChecked(false);
		Preferences.setString(IAntViewConstants.PREF_ANT_DISPLAY, displayLevel);
		setChecked(true);
	}
}