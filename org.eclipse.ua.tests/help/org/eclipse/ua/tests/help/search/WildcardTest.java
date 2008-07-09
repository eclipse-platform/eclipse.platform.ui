/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.search;


import junit.framework.TestCase;


public class WildcardTest extends TestCase {

    public void testSearchWithStar() {
    	SearchTestUtils.searchAllLocales("jehc*qpfjs", new String[] { "/org.eclipse.ua.tests/data/help/search/test1.xhtml" });
    }
    
    public void testSearchWithTwoStars() {
    	SearchTestUtils.searchAllLocales("jehc*qp*js", new String[] { "/org.eclipse.ua.tests/data/help/search/test1.xhtml" });
    }
    
    public void testSearchWithStarReplacingThreeChars() {
    	SearchTestUtils.searchAllLocales("jehc*fjs", new String[] { "/org.eclipse.ua.tests/data/help/search/test1.xhtml" });
    } 
    
    // Test that a star does not match spaces
    public void testSearchWithStarReplacingSpace() {
    	SearchTestUtils.searchAllLocales("jehcyqpfjs*vkrhjewiwh", new String[0]);
    }

    public void testSearchWithQuestionMark() {
    	SearchTestUtils.searchAllLocales("jehc?qpfjs", new String[] { "/org.eclipse.ua.tests/data/help/search/test1.xhtml" });
    }

    public void testSearchWithTwoQuestionMarks() {
    	SearchTestUtils.searchAllLocales("j?hc?qpfjs", new String[] { "/org.eclipse.ua.tests/data/help/search/test1.xhtml" });
    }

    public void testSearchQuestionMarkCannotReplaceTwoChars() {
    	SearchTestUtils.searchAllLocales("jehc?pfjs", new String[0] );
    }

    public void testSearchSuccessiveQuestionMarks() {
    	SearchTestUtils.searchAllLocales("jehc??pfjs", new String[] { "/org.eclipse.ua.tests/data/help/search/test1.xhtml" } );
    }

    public void testSearchLeadingStar() {
    	SearchTestUtils.searchAllLocales("*hcyqpfjs", new String[] { "/org.eclipse.ua.tests/data/help/search/test1.xhtml" });
    }
    
    public void testSearchLeadingQuestionMark() {
    	SearchTestUtils.searchAllLocales("?ehcyqpfjs", new String[] { "/org.eclipse.ua.tests/data/help/search/test1.xhtml" });
    }

}
