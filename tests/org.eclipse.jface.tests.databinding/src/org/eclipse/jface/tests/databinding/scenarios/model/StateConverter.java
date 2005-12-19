/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios.model;

import org.eclipse.jface.databinding.converter.IConverter;
 
public class StateConverter implements IConverter {
	
	/*
	 * State Abbreviations
	 *  
	 * From: http://www.usps.com/ncsc/lookups/usps_abbreviations.html
	 * 
	 * ALABAMA -> AL
	 * ALASKA -> AK
	 * AMERICAN SAMOA -> AS
	 * ARIZONA -> AZ
	 * ARKANSAS -> AR
	 * CALIFORNIA -> CA
	 * COLORADO -> CO
	 * CONNECTICUT -> CT
	 * DELAWARE -> DE
	 * DISTRICT OF COLUMBIA -> DC
	 * FEDERATED STATES OF MICRONESIA -> FM
	 * FLORIDA -> FL
	 * GEORGIA -> GA
	 * GUAM -> GU
	 * HAWAII -> HI
	 * IDAHO -> ID
	 * ILLINOIS -> IL
	 * INDIANA -> IN
	 * IOWA -> IA
	 * KANSAS -> KS
	 * KENTUCKY -> KY
	 * LOUISIANA -> LA
	 * MAINE -> ME
	 * MARSHALL ISLANDS -> MH
	 * MARYLAND -> MD
	 * MASSACHUSETTS -> MA
	 * MICHIGAN -> MI
	 * MINNESOTA -> MN
	 * MISSISSIPPI -> MS
	 * MISSOURI -> MO
	 * MONTANA -> MT
	 * NEBRASKA -> NE
	 * NEVADA -> NV
	 * NEW HAMPSHIRE -> NH
	 * NEW JERSEY -> NJ
	 * NEW MEXICO -> NM
	 * NEW YORK -> NY
	 * NORTH CAROLINA -> NC
	 * NORTH DAKOTA -> ND
	 * NORTHERN MARIANA ISLANDS -> MP
	 * OHIO -> OH
	 * OKLAHOMA -> OK
	 * OREGON -> OR
	 * PALAU -> PW
	 * PENNSYLVANIA -> PA
	 * PUERTO RICO -> PR
	 * RHODE ISLAND -> RI
	 * SOUTH CAROLINA -> SC
	 * SOUTH DAKOTA -> SD
	 * TENNESSEE -> TN
	 * TEXAS -> TX
	 * UTAH -> UT
	 * VERMONT -> VT
	 * VIRGIN ISLANDS -> VI
	 * VIRGINIA -> VA
	 * WASHINGTON -> WA
	 * WEST VIRGINIA -> WV
	 * WISCONSIN -> WI
	 * WYOMING -> WY
	 */
	public final static String[] STATE_LETTERS = new String[] {
			  "AL","AK","AZ","AR","CA","CO","CT","DE","DC","FL","GA","GU","HI",
			  "ID","IL","IN","IA","KS","KY","LA","ME","MD","MA","MI","MN","MS",
			  "MO","MT","NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR",
			  "PA","PR","RI","SC","SD","TN","TX","UT","VA","VI","WA","WV","WI", "WY" 
	};
	public final static String[] STATE_NAMES = new String[] {
			"Alabama","Alaska","Arizona","Arkansas","California","Colorado","CONNECTICUT","Delaware","Washington DC","Florida","Georgia","GUAM","Hawaii",
			"Idaho","Illinois","Indiana","Iowa","Kansas","KENTUCKY","LOUISIANA","MAINE","MARYLAND","Michigan","MASSACHUSETTS","Minnesota","Mississippi",
			"MISSOURI","MONTANA","NEBRASKA","Nevada","New Hampshire","New Jersey","New Mexico","New York","North Carolina","North Dakota","Ohio","Oklahmoa","Oregon",
			"Pensylvania","PUERTO RICO","Rhode Island","South Carolina","South Dakota","TENNESSEE","Texas","Utah","VERMONT", "VIRGIN ISLANDS","Virginia","Washington","West Virginia","Wisconsin", "Wyoming"
	};

	public Class getModelType() {
		return String.class;
	}
	public Class getTargetType() {
		return String.class;
	}
	public Object convertTargetToModel(Object targetObject) {
		String stateName = (String)targetObject;
		if(stateName != null){
			for (int i = 0; i < STATE_NAMES.length; i++) {
				if(STATE_NAMES[i].equalsIgnoreCase(stateName)){
					return STATE_LETTERS[i];
				}
			}
		}
		return null;
	}
	public Object convertModelToTarget(Object modelObject) {
		// Return the state name for the letter
		String stateLetter = (String)modelObject;
		if(stateLetter != null){
			for (int i = 0; i < STATE_LETTERS.length; i++) {
				if(STATE_LETTERS[i].equals(stateLetter)){
					return STATE_NAMES[i];
				}
			}
		}
		return "??" + STATE_LETTERS + "??";
	}
}
