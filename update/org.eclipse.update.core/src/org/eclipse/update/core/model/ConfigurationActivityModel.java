package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.PrintWriter;
import java.util.Date;

import org.eclipse.update.core.IActivity;

public class ConfigurationActivityModel extends ModelObject{
	
	private String label;
	private int action;
	private Date date;
	private int status;
	

	/**
	 * Constructor for ConfigurationActivityModel.
	 */
	public ConfigurationActivityModel() {
		super();
	}

	/**
	 * @since 2.0
	 */
	public int getAction() {
		return action;
	}

	/**
	 * @since 2.0
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @since 2.0
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Sets the date.
	 * @param date The date to set
	 */
	public void setDate(Date date) {
		assertIsWriteable();
		this.date = date;
	}

	/**
	 * Sets the status.
	 * @param status The status to set
	 */
	public void setStatus(int status) {
		assertIsWriteable();
		this.status = status;
	}

	/**
	 * @since 2.0
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 * @param label The label to set
	 */
	public void setLabel(String label) {
		assertIsWriteable();
		this.label = label;
	}

	/**
	 * Sets the action.
	 * @param action The action to set
	 */
	public void setAction(int action) {
		assertIsWriteable();
		this.action = action;
	}

}

