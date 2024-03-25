Rich Client Platform
====================

While the Eclipse platform is designed to serve as an open tools platform, it is architected so that its components could be used to build just about any client application. 
The minimal set of plug-ins needed to build a rich client application is collectively known as the **Rich Client Platform**.

Applications other than IDEs can be built using a subset of the platform. 
These rich applications are still based on a dynamic plug-in model, and the UI is built using the same toolkits and extension points. 
The layout and function of the workbench is under fine-grained control of the plug-in developer in this case.

For more details on what is included in the Rich Client Platform, see the [RCP FAQ](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform/Rich_Client_Platform_FAQ.md).

Contents
--------

*   [1 Case Studies](#Case-Studies)
*   [2 FAQs](#FAQs)
*   [3 Books on Eclipse RCP using 4.x API](#books-on-eclipse-rcp-using-4x-api)
*   [4 Tutorials for Eclipse RCP with Eclipse 4 API](#Tutorials-for-Eclipse-RCP-with-Eclipse-4-API)
*   [5 Tutorials covering older API levels](#Tutorials-covering-older-API-levels)
*   [6 Presentations](#Presentations)
*   [7 Help Topics](#Help-Topics)
*   [8 Examples](#Examples)
*   [9 Text Editor Examples](#Text-Editor-Examples)
*   [10 Applications](#Applications)
*   [11 Blogs](#Blogs)
*   [12 Other Resources](#Other-Resources)
*   [13 Original Design Documents](#Original-Design-Documents)

Case Studies
------------

The Eclipse community has put together a set of [case studies](https://www.eclipse.org/community/rcpcp.php) highlighting the use of RCP technology in a variety of real-world settings. 
More are being added all the time so visit page from time to time to see what's new.

FAQs
----

*   The [RCP FAQ](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform/Rich_Client_Platform_FAQ.md)
*   The [SWT FAQ](https://www.eclipse.org/swt/faq.php)

Books on Eclipse RCP using 4.x API
----------------------------------

*   [Eclipse 4 RCP](https://www.vogella.com/books/eclipsercp.html) by Lars Vogel.

Tutorials for Eclipse RCP with Eclipse 4 API
--------------------------------------------

*   [Eclipse RCP Tutorial](https://www.vogella.com/tutorials/EclipseRCP/article.html), Tutorials by Lars Vogel
*   [Eclipse 4 (e4) RCP Tutorials](https://eclipsesource.com/blogs/2016/01/15/eclipse-4-e4-tutorials-updated/), Tutorials by Jonas Helming

Tutorials covering older API levels
-----------------------------------

*   [Eclipse 3.x RCP Tutorial](https://www.vogella.com/tutorials/Eclipse3RCP/article.html), Tutorials by Lars Vogel
*   [Eclipse RCP Tutorials](https://www.programcreek.com/develop-plug-ins-using-rcp/), Eclipse RCP Tutorials from Program Creek

Presentations
-------------

*   EclipseCon 2009 -[Advanced RCP (6.1 MB PDF)](http://www.toedter.com/download/eclipsecon/Advanced-RCP-EclipseCon-2009.pdf) by Kai Tödter
*   EclipseCon 2007 - A lot of great high-level presentations as well as certain development aspects ([RCP Track](http://www.eclipsecon.org/2007/index.php?page=sub/&area=rich-client))
*   EclipseCon 2006 - Rich Client Platform Tutorial by Jeff McAffer and Jean-Michel Lemieux ([PDF](http://wiki.eclipse.org/images/d/d9/EclipseCon_RCP_Tutorial_2006.pdf))
*   Eclipse Provides Tool Integration Framework ([HTML](https://www.eetimes.com/eclipse-provides-tool-integration-framework/)) by Todd E. Williams and Mark R. Erickson, published in [EE Times](https://www.eetimes.com/eclipse-provides-tool-integration-framework/), September 2003.

Help Topics
-----------

You find additional information in the Platform Plug-in Developer Guide (from within the Eclipse IDE: Help > Help Contents > Platform Plug-in Developer Guide) in the online [Eclipse help](https://help.eclipse.org/latest).

Examples
--------

The usage of SWT and JFace is demonstrated in the following snippets:

*   [SWT Snippets](https://www.eclipse.org/swt/snippets/) and [JFace Snippets](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/JFaceSnippets.md).

Text Editor Examples
--------------------

The [RCP text editor examples](http://www.eclipse.org/eclipse/platform-text/development/rcp/examples/index.html) are available from the [Platform Text Component Development Resources page](http://www.eclipse.org/eclipse/platform-text/development/dev.html).

It illustrates how to use the [Platform Text](http://www.eclipse.org/eclipse/platform-text/index.php) components in a Rich Client Platform application.



Applications
------------

Several applications have been built using the Rich Client Platform.

*   The RCP Catalog section of the [Eclipse Community page](http://eclipse.org/community) lists several apps, including case studies of a few.
*   NASA/JPL is using Eclipse RCP as the foundation of their next version of Maestro, and more. See the [case study](http://eclipse.org/community/casestudies/NASAfinal.pdf). Jeff Norris from NASA/JPL also wrote a nice foreword to the [RCP Book](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform/Rich_Client_Platform_Book.md).

Blogs and other Resources
-------------------------

*   [PlanetEclipse.org](https://planeteclipse.org/planet/)
*   [Eclipse Tutorials](https://www.vogella.com/eclipse.html) Articles around Eclipse and Eclipse RCP

Original Design Documents
-------------------------

The following are the original design documents for the RCP work done in Eclipse 3.0. They are somewhat out of date and are provided here mainly for historical interest. The tutorials and help topics above provide better materials for getting started with RCP.

*   Original plan item in Bugzilla: [Enable Eclipse to be used as a rich client platform](https://bugs.eclipse.org/bugs/show_bug.cgi?id=36967)

