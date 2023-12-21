

Rich Client Platform
====================

While the Eclipse platform is designed to serve as an open tools platform, it is architected so that its components could be used to build just about any client application. The minimal set of plug-ins needed to build a rich client application is collectively known as the **Rich Client Platform**.

Applications other than IDEs can be built using a subset of the platform. These rich applications are still based on a dynamic plug-in model, and the UI is built using the same toolkits and extension points. The layout and function of the workbench is under fine-grained control of the plug-in developer in this case.

For more details on what is included in the Rich Client Platform, see the [RCP FAQ](/RCP_FAQ "RCP FAQ").

Contents
--------

*   [1 Case Studies](#Case-Studies)
*   [2 FAQs](#FAQs)
*   [3 Books on Eclipse RCP using 4.x API](#Books-on-Eclipse-RCP-using-4.x-API)
*   [4 Tutorials for Eclipse RCP with Eclipse 4 API](#Tutorials-for-Eclipse-RCP-with-Eclipse-4-API)
*   [5 Tutorials covering older API levels](#Tutorials-covering-older-API-levels)
*   [6 Presentations](#Presentations)
*   [7 Help Topics](#Help-Topics)
*   [8 Newsgroups](#Newsgroups)
*   [9 Examples](#Examples)
*   [10 Applications](#Applications)
*   [11 Blogs](#Blogs)
*   [12 Other Resources](#Other-Resources)
*   [13 Elsewhere at eclipse.org](#Elsewhere-at-eclipse.org)
*   [14 Original Design Documents](#Original-Design-Documents)

Case Studies
------------

The Eclipse community has put together a set of [case studies](https://www.eclipse.org/community/rcpcp.php) highlighting the use of RCP technology in a variety of real-world settings. More are being added all the time so visit page from time to time to see what's new.

FAQs
----

*   The [RCP FAQ](/Rich_Client_Platform/FAQ "Rich Client Platform/FAQ")
*   The [SWT FAQ](http://www.eclipse.org/swt/faq.php)

Books on Eclipse RCP using 4.x API
----------------------------------

*   ["Eclipse 4 RCP"](http://www.vogella.com/books/eclipsercp.html) by Lars Vogel.

Tutorials for Eclipse RCP with Eclipse 4 API
--------------------------------------------

*   [Eclipse RCP Tutorial](https://www.vogella.com/tutorials/EclipseRCP/article.html), Tutorial by Lars Vogel
*   [Eclipse 4 (e4) RCP Tutorials](https://eclipsesource.com/blogs/2016/01/15/eclipse-4-e4-tutorials-updated/), Tutorials by Jonas Helming

Tutorials covering older API levels
-----------------------------------

*   [Eclipse 3.x RCP Tutorial](http://www.vogella.com/tutorials/Eclipse3RCP/article.html), Tutorial by Lars Vogel
*   [Eclipse RCP Tutorials](http://www.programcreek.com/develop-plug-ins-using-rcp/), Eclipse RCP Tutorials from Program Creek

Presentations
-------------

*   EclipseCon 2009 -[Advanced RCP (6.1 MB PDF)](http://www.toedter.com/download/eclipsecon/Advanced-RCP-EclipseCon-2009.pdf) by Kai Tödter
*   EclipseCon 2008 - [Advanced RCP](http://toedter.com/download/eclipsecon/Advanced_RCP-EclipseCon_2008.pdf) (2.75 MB PDF) by Kai Tödter
*   EclipseCon 2007 - A lot of great high-level presentations as well as certain development aspects ([RCP Track](http://www.eclipsecon.org/2007/index.php?page=sub/&area=rich-client))
*   EclipseCon 2006 - Rich Client Platform Tutorial by Jeff McAffer and Jean-Michel Lemieux ([PDF](http://wiki.eclipse.org/images/d/d9/EclipseCon_RCP_Tutorial_2006.pdf))
*   Screencast: [Why You Should Be Using Eclipse RCP](http://www.eclipsezone.com/eps/10minute-rcp/)
*   Smart Client development with the Eclipse Rich Client Platform ([PDF](http://eclipse.org/rcp/jaoo2005/slides/Smart%20Client%20Development%20with%20RCP.pdf)) ([PPT](http://eclipse.org/rcp/jaoo2005/slides/Smart%20Client%20Development%20with%20RCP.ppt)) by Nick Edgar and Pascal Rapicault, presented at [JAOO](http://jaoo.org) 2005.
    
    The source for the accompanying "Go Wild Travel - Adventure Builder RCP Client" example is being cleaned up, and will be posted shortly.
    
*   Developing for the Rich Client Platform ([PDF](http://www.eclipsecon.org/2005/presentations/EclipseCon2005_Tutorial26.pdf)) by Nick Edgar and Pascal Rapicault, from their tutorial presented at [EclipseCon](http://www.eclipsecon.org), February 28, 2005.
*   Eclipse Rich Client Applications - Overview of the Generic Workbench ([PDF](http://www.eclipsecon.org/2004/EclipseCon_2004_TechnicalTrackPresentations/11_Edgar.pdf)) ([PPT](http://eclipse.org/rcp/EclipseCon2004/RCP%20UI.ppt)) by Nick Edgar, presented at [EclipseCon](http://www.eclipsecon.org), February 2004.
*   Eclipse RCP Runtime ([PDF](http://www.eclipsecon.org/2004/EclipseCon_2004_TechnicalTrackPresentations/14_McAffer.pdf)) by Jeff McAffer, presented at [EclipseCon](http://www.eclipsecon.org) February 2004.
*   Eclipse Rich Client Platform ([Zip](http://www.rtpwug.org/download/2004.08/RCPPresentation_20040831.zip)) by Ed Burnette, presented at the RTP Websphere Users Group, August 2004.
*   The Eclipse Rich Client Platform ([PPT](http://eclipse.org/rcp/slides/RCP.ppt)) by various members of the Eclipse JDT and Platform teams, last updated November 5, 2004.
*   The Eclipse Rich Client Platform ([HTML](http://www.eclipsefaq.org/chris/LaffraEclipseRCP_files/v3_document.htm)) by [Chris Laffra](http://www.eclipsefaq.org/chris/), presented at the Colorado Software Summit, October 2004.
*   Eclipse-based Applications: Java on the Desktop Revisited ([PDF](https://www.eclipsecon.org/2004/EclipseCon_2004_TechnicalTrackPresentations/17_Williams_May_Dovich.pdf)) by Todd E. Williams, Paul May, and Giovanni Farris, presented at [EclipseCon](http://www.eclipsecon.org) February 2004.
*   Eclipse Provides Tool Integration Framework ([HTML](https://www.eetimes.com/eclipse-provides-tool-integration-framework/)) by Todd E. Williams and Mark R. Erickson, published in [EE Times](https://www.eetimes.com/eclipse-provides-tool-integration-framework/), September 2003.

Help Topics
-----------

The following are relevant help topics from the Platform Plug-in Developer Guide (from within the Eclipse IDE: Help > Help Contents > Platform Plug-in Developer Guide). The links below are to the online [Eclipse 3.1 help](http://help.eclipse.org/help31).

*   [Building a Rich Client Platform application](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/guide/rcp.htm)
*   [Defining a Product](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/guide/product_def.htm)
*   [Plugging into the workbench](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/guide/workbench.htm)
*   [Runtime overview](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/guide/runtime.htm)
*   [SWT - Standard Widget Toolkit](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/guide/swt.htm)
*   [JFace UI Framework](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/guide/jface.htm)
*   [Dialogs and wizards](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/guide/dialogs.htm)
*   [Advanced Workbench Concepts](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/guide/wrkAdv.htm)
*   [API rules of engagement](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/reference/misc/api-usage-rules.html)
*   [Runtime options](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/reference/misc/runtime-options.html)
*   [Map of Platform Plug-ins](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/reference/misc/overview-platform.html)
*   Eclipse [2.1->3.0](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/porting/eclipse_3_0_porting_guide.html) and [3.0->3.1](http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/porting/eclipse_3_1_porting_guide.html) Plug-in Migration Guides

Newsgroups
----------

The following [eclipse newsgroups](http://eclipse.org/newsgroups/index.html) have useful discussions, questions and answers relevant to the development of RCP applications.

If you have a question, please check the [RCP FAQ](/RCP_FAQ "RCP FAQ") before posting to the newsgroups. Kindly avoid posting to the developer (*-dev) mailing lists as these are intended for use by the development teams and others participating in the development of Eclipse itself.

*   [news://news.eclipse.org/eclipse.platform.rcp](news://news.eclipse.org/eclipse.platform.rcp) \- For questions specific to the development of RCP applications. Also available via [EclipseZone](http://www.eclipsezone.com/eclipse/forums/f18121.html).
*   [news://news.eclipse.org/eclipse.platform](news://news.eclipse.org/eclipse.platform) \- For general Workbench and JFace questions that could apply both to RCP applications and IDE plug-ins. Also available via [EclipseZone](http://www.eclipsezone.com/eclipse/forums/f18122.html).
*   [news://news.eclipse.org/eclipse.platform.swt](news://news.eclipse.org/eclipse.platform.swt) \- For SWT-specific questions. Also available via [EclipseZone](http://www.eclipsezone.com/eclipse/forums/f18120.html).

Examples
--------

The following are some examples showing how to build applications using RCP:

*   [RCP Browser Example](/RCP_Browser_Example "RCP Browser Example")
*   [RCP Text Editor Examples](/RCP_Text_Editor_Examples "RCP Text Editor Examples")[  
    ](/index.php?title=RCP_Web_UI_integration_Examples&action=edit&redlink=1 "RCP Web UI integration Examples (page does not exist)")
*   See also [SWT Snippets](http://www.eclipse.org/swt/snippets/) and [JFace Snippets](http://wiki.eclipse.org/index.php/JFaceSnippets).

Applications
------------

Several applications have been built using the Rich Client Platform.

*   The [RCP Applications](http://eclipse.org/community/rcp.php) section of the [Eclipse Community page](http://eclipse.org/community) lists several apps, including case studies of a few.
*   NASA/JPL is using Eclipse RCP as the foundation of their next version of Maestro, and more. See the [case study](http://eclipse.org/community/casestudies/NASAfinal.pdf), session [11.3 - "A Martian Eclipse"](http://www.eclipsecon.org/2005/sessions.php) at EclipseCon 2005, and Scott Schram's [blog entry](http://weblogs.java.net/blog/scottschram/archive/2005/03/nasa_explores_e.html) on the presentation. Jeff Norris from NASA/JPL also wrote a nice foreword to the [RCP Book](/RCP_Book "RCP Book").

Blogs
-----

*   [PlanetEclipse.org](http://planeteclipse.org/planet/)
*   [Java and Eclipse Tips](http://javarevisited.blogspot.de)

Other Resources
---------------

The following are other resources describing the Eclipse Rich Client Platform, or related subjects:

*   [EclipseZone](http://eclipsezone.org)
*   [RCP page](http://eclipsewiki.editme.com/RichClientPlatform) on the Eclipse Community Wiki
*   [Eclipse Tutorials](http://www.vogella.de/eclipse.html) Articles around Eclipse and Eclipse RCP
*   [ProgramCreek](http://www.programcreek.com) Articles and tutorials on the Eclipse RCP Topic

Elsewhere at eclipse.org
------------------------

Content is in the process of being migrated here from the old [RCP Home Page](http://www.eclipse.org/rcp).

Original Design Documents
-------------------------

The following are the original design documents for the RCP work done in Eclipse 3.0. They are somewhat out of date and are provided here mainly for historical interest. The tutorials and help topics above provide better materials for getting started with RCP.

*   Original plan item in Bugzilla: [Enable Eclipse to be used as a rich client platform](https://bugs.eclipse.org/bugs/show_bug.cgi?id=36967)
*   [Executive Summary of the Eclipse Rich Client Platform UI](http://eclipse.org/rcp/generic_workbench_summary.html) (includes excerpts from the documents below)
*   [Statement of Direction for the Eclipse Rich Client Platform UI](http://eclipse.org/rcp/generic_workbench_direction.html)
*   [Eclipse Rich Client Platform UI Proposed Approach](http://eclipse.org/rcp/generic_workbench_approach.html)
*   [Overview of the Generic Workbench](http://eclipse.org/rcp/generic_workbench_overview.html)
*   [Generic Workbench Plug-in Structure](http://eclipse.org/rcp/generic_workbench_structure.html)

