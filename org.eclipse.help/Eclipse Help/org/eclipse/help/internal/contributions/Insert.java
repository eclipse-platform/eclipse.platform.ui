package org.eclipse.help.internal.contributions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.lang.*;

/**
 * Insert contribution
 */
public interface Insert extends Contribution {

	public String getMode();
	public String getSource();
	public String getTarget();
}
