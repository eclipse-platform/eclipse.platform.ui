package org.eclipse.help.internal.contributions1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.lang.*;

/**
 * Insert contribution
 */
public interface Insert extends Contribution {

	public int    getMode();
	public String getSource();
	public String getTarget();
}
