/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import org.eclipse.osgi.util.NLS;

/**
 * RegEx messages. Helper class to get NLSed messages.
 * 
 * @since 3.1
 */
final class RegExMessages extends NLS {

	private static final String BUNDLE_NAME= RegExMessages.class.getName();

	private RegExMessages() {
		// Do not instantiate
	}

	static {
		reloadMessages();
	}
	
	static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, RegExMessages.class);
	}
	
	//characters
	public static String displayString_bs_bs;
	public static String additionalInfo_bs_bs;
	public static String displayString_bs_0;
	public static String additionalInfo_bs_0;
	public static String displayString_bs_x;
	public static String additionalInfo_bs_x;
	public static String displayString_bs_u;
	public static String additionalInfo_bs_u;
	public static String displayString_bs_t;
	public static String additionalInfo_bs_t;
	public static String displayString_bs_n;
	public static String additionalInfo_bs_n;
	public static String displayString_bs_r;
	public static String additionalInfo_bs_r;
	public static String displayString_bs_f;
	public static String additionalInfo_bs_f;
	public static String displayString_bs_a;
	public static String additionalInfo_bs_a;
	public static String displayString_bs_e;
	public static String additionalInfo_bs_e;
	public static String displayString_bs_c;
	public static String additionalInfo_bs_c;
	
	//character classes
	public static String displayString_dot;
	public static String additionalInfo_dot;
	public static String displayString_bs_d;
	public static String additionalInfo_bs_d;
	public static String displayString_bs_D;
	public static String additionalInfo_bs_D;
	public static String displayString_bs_s;
	public static String additionalInfo_bs_s;
	public static String displayString_bs_S;
	public static String additionalInfo_bs_S;
	public static String displayString_bs_w;
	public static String additionalInfo_bs_w;
	public static String displayString_bs_W;
	public static String additionalInfo_bs_W;
	
	//boundary matchers
	public static String displayString_start;
	public static String additionalInfo_start;
	public static String displayString_end;
	public static String additionalInfo_end;
	public static String displayString_bs_b;
	public static String additionalInfo_bs_b;
	public static String displayString_bs_B;
	public static String additionalInfo_bs_B;
	public static String displayString_bs_A;
	public static String additionalInfo_bs_A;
	public static String displayString_bs_G;
	public static String additionalInfo_bs_G;
	public static String displayString_bs_Z;
	public static String additionalInfo_bs_Z;
	public static String displayString_bs_z;
	public static String additionalInfo_bs_z;
	
	//greedy quantifiers
	public static String displayString_quest;
	public static String additionalInfo_quest;
	public static String displayString_star;
	public static String additionalInfo_star;
	public static String displayString_plus;
	public static String additionalInfo_plus;
	public static String displayString_exact;
	public static String additionalInfo_exact;
	public static String displayString_least;
	public static String additionalInfo_least;
	public static String displayString_count;
	public static String additionalInfo_count;
	
	//lazy quantifiers
	public static String displayString_questLazy;
	public static String additionalInfo_questLazy;
	public static String displayString_starLazy;
	public static String additionalInfo_starLazy;
	public static String displayString_plusLazy;
	public static String additionalInfo_plusLazy;
	public static String displayString_exactLazy;
	public static String additionalInfo_exactLazy;
	public static String displayString_leastLazy;
	public static String additionalInfo_leastLazy;
	public static String displayString_countLazy;
	public static String additionalInfo_countLazy;
	
	//possessive quantifiers
	public static String displayString_questPoss;
	public static String additionalInfo_questPoss;
	public static String displayString_starPoss;
	public static String additionalInfo_starPoss;
	public static String displayString_plusPoss;
	public static String additionalInfo_plusPoss;
	public static String displayString_exactPoss;
	public static String additionalInfo_exactPoss;
	public static String displayString_leastPoss;
	public static String additionalInfo_leastPoss;
	public static String displayString_countPoss;
	public static String additionalInfo_countPoss;
	
	//alternative
	public static String displayString_alt;
	public static String additionalInfo_alt;
	
	//capturing groups
	public static String displayString_group;
	public static String additionalInfo_group;
	public static String displayString_bs_i;
	public static String additionalInfo_bs_i;
	
	//quoting
	public static String displayString_bs;
	public static String additionalInfo_bs;
	public static String displayString_bs_Q;
	public static String additionalInfo_bs_Q;
	public static String displayString_bs_E;
	public static String additionalInfo_bs_E;
	
	//character sets
	public static String displayString_set;
	public static String additionalInfo_set;
	public static String displayString_setExcl;
	public static String additionalInfo_setExcl;
	public static String displayString_setRange;
	public static String additionalInfo_setRange;
	public static String displayString_setInter;
	public static String additionalInfo_setInter;
	public static String displayString_posix;
	public static String additionalInfo_posix;
	public static String displayString_posixNot;
	public static String additionalInfo_posixNot;
	
	public static String displayString_flag;
	public static String additionalInfo_flag;
	public static String displayString_flagExpr;
	public static String additionalInfo_flagExpr;
	
	//noncapturing group
	public static String displayString_nonCap;
	public static String additionalInfo_nonCap;
	public static String displayString_atomicCap;
	public static String additionalInfo_atomicCap;
	
	//lookaraound
	public static String displayString_posLookahead;
	public static String additionalInfo_posLookahead;
	public static String displayString_negLookahead;
	public static String additionalInfo_negLookahead;
	public static String displayString_posLookbehind;
	public static String additionalInfo_posLookbehind;
	public static String displayString_negLookbehind;
	public static String additionalInfo_negLookbehind;
	
	//replace
	public static String displayString_dollar;
	public static String additionalInfo_dollar;
	public static String additionalInfo_replace_bs;
	public static String displayString_replace_bs;
	public static String displayString_tab;
	public static String additionalInfo_tab;
}
