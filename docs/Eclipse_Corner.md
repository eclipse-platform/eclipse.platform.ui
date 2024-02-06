Eclipse Corner
==============

These following articles have been written by members of the various project development teams and other members of the Eclipse community.

Contents
--------

*   [1 Eclipse setup instructions on a new Linux (or other OS) computer](#Eclipse-setup-instructions-on-a-new-Linux-.28or-other-OS.29-computer)
*   [2 Custom Drawing Table and Tree Items](#Custom-Drawing-Table-and-Tree-Items)
*   [3 Extending WTP Using Project Facets](#Extending-WTP-Using-Project-Facets)
*   [4 Implementing Model Integrity in EMF with EMFT OCL](#Implementing-Model-Integrity-in-EMF-with-EMFT-OCL)
*   [5 From Front End To Code - MDSD in Practice](#From-Front-End-To-Code---MDSD-in-Practice)
*   [6 Virtual Tables and Trees](#Virtual-Tables-and-Trees)
*   [7 A Shape Diagram Editor](#A-Shape-Diagram-Editor)
*   [8 Eclipse Platform Technical Overview](#Eclipse-Platform-Technical-Overview)
*   [9 Eclipse Workbench: Using the Selection Service](#Eclipse-Workbench-Using-the-Selection-Service)
*   [10 The Language Toolkit: An API for Automated Refactorings in Eclipse-based IDEs](#The-Language-Toolkit-An-API-for-Automated-Refactorings-in-Eclipse-based-IDEs)
*   [11 Creating Database Web Applications with Eclipse](#Creating-Database-Web-Applications-with-Eclipse)
*   [12 Teach Your Eclipse to Speak the Local Lingo](#Teach-Your-Eclipse-to-Speak-the-Local-Lingo)
*   [13 Java Application Profiling using TPTP](#Java-Application-Profiling-using-TPTP)
*   [14 The Eclipse Tabbed Properties View](#The-Eclipse-Tabbed-Properties-View)
*   [15 How to Correctly and Uniformly Use Progress Monitors](#How-to-Correctly-and-Uniformly-Use-Progress-Monitors)
*   [16 Eclipse Forms: Rich UI for the Rich Client](#Eclipse-Forms-Rich-UI-for-the-Rich-Client)
*   [17 Introducing the GMF Runtime](#Introducing-the-GMF-Runtime)
*   [18 Authoring with Eclipse](#Authoring-with-Eclipse)
*   [19 Inside the Workbench: A guide to the workbench internals](#Inside-the-Workbench-A-guide-to-the-workbench-internals)
*   [20 Plugging into SourceForge.net](#Plugging-into-SourceForge.net)
*   [21 Persisting EMF models with WTP](#Persisting-EMF-models-with-WTP)
*   [22 Extending The Visual Editor: Enabling support for a custom widget](#Extending-The-Visual-Editor-Enabling-support-for-a-custom-widget)
*   [23 Using GEF with EMF](#Using-GEF-with-EMF)
*   [24 Build and Test Automation for plug-ins and features](#Build-and-Test-Automation-for-plug-ins-and-features)
*   [25 Using OpenGL with SWT](#Using-OpenGL-with-SWT)
*   [26 Folding in Eclipse Text Editors](#Folding-in-Eclipse-Text-Editors)
*   [27 Eclipse User Interface Guidelines: Version 2.1](#Eclipse-User-Interface-Guidelines-Version-2.1)
*   [28 Modeling Rule-Based Systems with EMF](#Modeling-Rule-Based-Systems-with-EMF)
*   [29 Building Administrative Applications in Eclipse](#Building-Administrative-Applications-in-Eclipse)
*   [30 EMF goes RCP](#EMF-goes-RCP)
*   [31 Building a Database Schema Diagram Editor with GEF](#Building-a-Database-Schema-Diagram-Editor-with-GEF)
*   [32 On the Job: The Eclipse Jobs API](#On-the-Job-The-Eclipse-Jobs-API)
*   [33 Branding Your Application](#Branding-Your-Application)
*   [34 How to Write an Eclipse Debugger](#How-to-Write-an-Eclipse-Debugger)
*   [35 Viewing HTML pages with SWT Browser widget](#Viewing-HTML-pages-with-SWT-Browser-widget)
*   [36 Rich Client Tutorial](#Rich-Client-Tutorial)
*   [37 A Basic Image Viewer](#A-Basic-Image-Viewer)
*   [38 Mutatis mutandis - Using Preference Pages as Property Pages](#Mutatis-mutandis---Using-Preference-Pages-as-Property-Pages)
*   [39 A small cup of SWT: A bag of hints, tricks and recipes for developing SWT apps on the Pocket PC](#A-small-cup-of-SWT-A-bag-of-hints.2C-tricks-and-recipes-for-developing-SWT-apps-on-the-Pocket-PC)
*   [40 Taking a look at SWT Images](#Taking-a-look-at-SWT-Images)
*   [41 PDE Does Plug-ins](#PDE-Does-Plug-ins)
*   [42 How To Keep Up To Date](#How-To-Keep-Up-To-Date)
*   [43 JET Tutorial Part 2 (Write Code that Writes Code)](#JET-Tutorial-Part-2-.28Write-Code-that-Writes-Code.29)
*   [44 Launching Java Applications Programmatically](#Launching-Java-Applications-Programmatically)
*   [45 Adding Drag and Drop to an SWT Application](#Adding-Drag-and-Drop-to-an-SWT-Application)
*   [46 Display a UML Diagram using Draw2D](#Display-a-UML-Diagram-using-Draw2D)
*   [47 Drag and Drop in the Eclipse UI](#Drag-and-Drop-in-the-Eclipse-UI)
*   [48 Using Native Drag and Drop with GEF](#Using-Native-Drag-and-Drop-with-GEF)
*   [49 JET Tutorial Part 1 (Introduction to JET)](#JET-Tutorial-Part-1-.28Introduction-to-JET.29)
*   [50 Inside the Memory View: A Guide for Debug Providers](#Inside-the-Memory-View-A-Guide-for-Debug-Providers)
*   [51 Building and delivering a table editor with SWT/JFace](#Building-and-delivering-a-table-editor-with-SWT.2FJFace)
*   [52 Graphics Context - Quick on the draw](#Graphics-Context---Quick-on-the-draw)
*   [53 Notes on the Eclipse Plug-in Architecture](#Notes-on-the-Eclipse-Plug-in-Architecture)
*   [54 Designing Accessible Plug-ins in Eclipse](#Designing-Accessible-Plug-ins-in-Eclipse)
*   [55 Take control of your properties](#Take-control-of-your-properties)
*   [56 Project Builders and Natures](#Project-Builders-and-Natures)
*   [57 Understanding Decorators in Eclipse](#Understanding-Decorators-in-Eclipse)
*   [58 We Have Lift-off: The Launching Framework in Eclipse](#We-Have-Lift-off-The-Launching-Framework-in-Eclipse)
*   [59 Creating JFace Wizards](#Creating-JFace-Wizards)
*   [60 Using EMF](#Using-EMF)
*   [61 How You've Changed!: Responding to resource changes in the Eclipse workspace](#How-You.27ve-Changed.21-Responding-to-resource-changes-in-the-Eclipse-workspace)
*   [62 How to Internationalize your Eclipse Plug-In](#How-to-Internationalize-your-Eclipse-Plug-In)
*   [63 How to Test Your Internationalized Eclipse Plug-In](#How-to-Test-Your-Internationalized-Eclipse-Plug-In)
*   [64 Simplifying Preference Pages with Field Editors](#Simplifying-Preference-Pages-with-Field-Editors)
*   [65 Preferences in the Eclipse Workbench UI](#Preferences-in-the-Eclipse-Workbench-UI)
*   [66 Help Part 1: Contributing a Little Help](#Help-Part-1-Contributing-a-Little-Help)
*   [67 How to use the JFace Tree Viewer](#How-to-use-the-JFace-Tree-Viewer)
*   [68 Creating an Eclipse View](#Creating-an-Eclipse-View)
*   [69 Contributing Actions to the Eclipse Workbench](#Contributing-Actions-to-the-Eclipse-Workbench)
*   [70 Into the Deep End of the SWT StyledText Widget](#Into-the-Deep-End-of-the-SWT-StyledText-Widget)
*   [71 Using Perspectives in the Eclipse UI](#Using-Perspectives-in-the-Eclipse-UI)
*   [72 How to Use the Eclipse API](#How-to-Use-the-Eclipse-API)
*   [73 Getting Your Feet Wet with the SWT StyledText Widget](#Getting-Your-Feet-Wet-with-the-SWT-StyledText-Widget)
*   [74 SWT Color Model](#SWT-Color-Model)
*   [75 Using Images in the Eclipse UI](#Using-Images-in-the-Eclipse-UI)
*   [76 Mark My Words: Using markers to tell users about problems and tasks](#Mark-My-Words-Using-markers-to-tell-users-about-problems-and-tasks)
*   [77 Levels Of Integration: Five ways you can integrate with the Eclipse Platform](#Levels-Of-Integration-Five-ways-you-can-integrate-with-the-Eclipse-Platform)
*   [78 ActiveX Support In SWT](#ActiveX-Support-In-SWT)
*   [79 Creating Your Own Widgets using SWT](#Creating-Your-Own-Widgets-using-SWT)
*   [80 SWT: The Standard Widget Toolkit](#SWT-The-Standard-Widget-Toolkit)
*   [81 Understanding Layouts in SWT](#Understanding-Layouts-in-SWT)
*   [82 Introducing AJDT: The AspectJ Development Tools](#Introducing-AJDT-The-AspectJ-Development-Tools)
*   [83 Abstract Syntax Tree](#Abstract-Syntax-Tree)
*   [84 Running Web Service Scenarios using Ant](#Running-Web-Service-Scenarios-using-Ant)
*   [85 Unleashing the Power of Refactoring](#Unleashing-the-Power-of-Refactoring)
*   [86 Building Eclipse Plugins with Maven 2](#Building-Eclipse-Plugins-with-Maven-2)
*   [87 Using TPTP to Automate Functional Testing](#Using-TPTP-to-Automate-Functional-Testing)
*   [88 Simple Image Effects for SWT](#Simple-Image-Effects-for-SWT)
*   [89 Using the BIRT Chart Engine in Your Plug-in](#Using-the-BIRT-Chart-Engine-in-Your-Plug-in)
*   [90 Swing/SWT Integration](#Swing.2FSWT-Integration)
*   [91 How to process OCL Abstract Syntax Trees](#How-to-process-OCL-Abstract-Syntax-Trees)
*   [92 Branching with Eclipse and CVS](#Branching-with-Eclipse-and-CVS)
*   [93 Adding Help Support to a Rich Client Platform (RCP) Application](#Adding-Help-Support-to-a-Rich-Client-Platform-.28RCP.29-Application)
*   [94 Eclipse Forms: New in 3.3](#Eclipse-Forms-New-in-3.3)
*   [95 Defining Generics with UML Templates](#Defining-Generics-with-UML-Templates)
*   [96 Integrating EMF and GMF Generated Editors](#Integrating-EMF-and-GMF-Generated-Editors)
*   [97 Build your own textual DSL with Tools from the Eclipse Modeling Project](#Build-your-own-textual-DSL-with-Tools-from-the-Eclipse-Modeling-Project)
*   [98 Automating Eclipse PDE Unit Tests using Ant](#Automating-Eclipse-PDE-Unit-Tests-using-Ant)
*   [99 Adapters](#Adapters)
*   [100 Automating the embedding of Domain Specific Languages in Eclipse JDT](#Automating-the-embedding-of-Domain-Specific-Languages-in-Eclipse-JDT)
*   [101 Dynamic User Assistance in Eclipse Based Applications](#Dynamic-User-Assistance-in-Eclipse-Based-Applications)
*   [102 BIRT Extension Mechanism, Part 1: Custom Report Items](#BIRT-Extension-Mechanism.2C-Part-1-Custom-Report-Items)
*   [103 BIRT Extension Mechanism, Part 2](#BIRT-Extension-Mechanism.2C-Part-2)
*   [104 How to Fix a Bug in Eclipse](#How-to-Fix-a-Bug-in-Eclipse)
*   [105 Babel Pseudo Translations](#Babel-Pseudo-Translations)

Eclipse setup instructions on a new Linux (or other OS) computer
----------------------------------------------------------------

*   Direct download link is here: [Eclipse setup instructions on a new Linux (or other OS) computer.pdf](https://github.com/ElectricRCAircraftGuy/eRCaGuy_dotfiles/blob/master/eclipse/Eclipse%20setup%20instructions%20on%20a%20new%20Linux%20(or%20other%20OS)%20computer.pdf) (PDF).
*   It is part of this larger project [here](https://github.com/ElectricRCAircraftGuy/eRCaGuy_dotfiles).

  
Getting started with Eclipse as a beginner is difficult. This document _significantly_ eases that transition. Even for experienced Eclipse users, getting it to work perfectly and bug-free on large projects requires several advanced tricks, all of which are documented carefully in this document with the words "\[IMPORTANT TO PREVENT FREEZES\]". This is unofficial documentation to help Eclipse users get Eclipse installed and configured on any operating system, with a particular emphasis on using Eclipse on Linux. It details getting it to work well and crash-free on any OS when working with small or gigantic projects.

This document also introduces in detail the following concepts which are unique to Eclipse and very confusing for beginners:

1.  workspace
2.  project
3.  perspective
4.  working set

...and it introduces dozens of really useful shortcuts used daily by experienced Eclipse developers.

Here is a snapshot of its table of contents:

1.  Table of Contents: 1
2.  Install & Setup Steps: 1
3.  Plugins to Install: 7
4.  Configure “eclipse.ini”: 8
5.  Freezes, & Clearing Eclipse’s Cached .pdom Indexer File For Your Project: 10
6.  Project Resource Filters (adding resources, linked resources, excluded resources, virtual folders, etc): 10
    1.  Resource Filters (“Include only” or “Exclude all”) 10
    2.  “Virtual” Folders and “Links” to files or folders (ie: Linked Folders or Linked Files) 11
7.  Eclipse Usage, Workflow, Help, Tips & Tricks: 13

  

Custom Drawing Table and Tree Items
-----------------------------------

Populating a table or tree widget involves creating items and setting their attributes (eg.- texts, images, etc.), after which the table or tree takes responsibility for displaying the items. This approach makes item creation straightforward and visually consistent. As of Eclipse 3.2, clients of Table and Tree can now custom draw their items, enabling a wide range of potential visual appearances. This article explores the custom draw mechanism for Table and Tree.

*   [Custom Drawing Table and Tree Items](http://www.eclipse.org/articles/Article-CustomDrawingTableAndTreeItems/customDraw.htm)

  

Extending WTP Using Project Facets
----------------------------------

The Faceted Project Framework allows the plugin developer to think of Web Tools Platform (WTP) projects as composed of units of functionality, otherwise known as facets, that can be added and removed by the user. This tutorial walks you through an example of creating a couple of basic facets and in the process covers the majority of the framework's extension points. This tutorial has been written for version 1.5 of the Web Tools Platform.

*   [Extending WTP Using Project Facets](http://www.eclipse.org/articles/Article-BuildingProjectFacets/tutorial.html)

  

Implementing Model Integrity in EMF with EMFT OCL
-------------------------------------------------

This article illustrates how the EMFT OCL parser/interpreter technology adds to the value of EMF/JET code generation as a foundation for model-driven development (MDD). We will see, with fully functional examples, how a metamodel can be generated from an Ecore model without requiring any post-generation custom code, including complete implementations of invariant constraints, derived attributes and references, and operations.

*   [Implementing Model Integrity in EMF with EMFT OCL](http://www.eclipse.org/articles/article.php?file=Article-EMF-Codegen-with-OCL/index.html)

  

From Front End To Code - MDSD in Practice
-----------------------------------------

Model-driven software development (MDSD) is not just about generating code. Several additional challenges have to be mastered. These include: how to get usable graphical and textual editors for your domain specific language (DSL), how to validate your models against your metamodels, how to define model modifications and transformations and finally, how to write scalable, maintainable and extensible code generators. In this article we show how to tackle all these challenges, based on a collection of open source tools: Eclipse, Eclipse Modeling Framework (EMF), Graphical Modeling Framework (GMF) as well as openArchitectureWare. We believe that this tool chain provides a proven and stable stack for making MDSD a practical reality.

*   [From Front End To Code - MDSD in Practice](http://www.eclipse.org/articles/Article-FromFrontendToCode-MDSDInPractice/article.html)

  

Virtual Tables and Trees
------------------------

Virtual Tables and Trees allow developers to quickly create Tables and Trees with large amounts of data and populate them efficiently. This article is an overview of how to use virtual Tables and Trees within SWT applications.

*   [Virtual Tables and Trees](http://www.eclipse.org/articles/Article-SWT-Virtual/Virtual-in-SWT.html)

  

A Shape Diagram Editor
----------------------

Graphical Editing Framework (GEF) provides a powerful foundation for creating editors for visual editing of arbitrary models. Its effectiveness lies in a modular build, fitting use of design patterns, and decoupling of components that comprise a full, working editor. To a newcomer, the sheer number and variety of concepts and techniques present in GEF may feel intimidating. However, once learned and correctly used, they help to develop highly scalable and easy to maintain software. This article aims to provide a gentle yet comprehensive introduction to GEF. It describes a shape diagram editor - a small, fully functional test case of core concepts.

*   [A Shape Diagram Editor](http://www.eclipse.org/articles/Article-GEF-diagram-editor/shape.html)
*   [A Shape Diagram Editor](http://www.eclipse.org/articles/Article-GEF-diagram-editor/shape_cn.html)

  

Eclipse Platform Technical Overview
-----------------------------------

The Eclipse Platform is designed for building applications, integrated development environments (IDEs)and arbitrary tools. This paper is a general technical introduction to the Eclipse Platform. Part I presents a technical overview of its architecture. Part II is a case study of how the Eclipse Platform was used to build a full-featured Java development environment.

*   [Eclipse Platform Technical Overview](http://www.eclipse.org/articles/Whitepaper-Platform-3.1/eclipse-platform-whitepaper.html)
*   [Eclipse Platform Technical Overview](http://www.eclipse.org/articles/Whitepaper-Platform-3.1/eclipse-platform-whitepaper.pdf)

  

Eclipse Workbench: Using the Selection Service
----------------------------------------------

The selection service provided by the Eclipse workbench allows efficient linking of different parts within the workbench window. Knowing and using the existing selection mechanisms gives your plug-ins a clean design, smoothly integrates them into the workbench and opens them for future extensions.

*   [Eclipse Workbench: Using the Selection Service](http://www.eclipse.org/articles/Article-WorkbenchSelections/article.html)

  

The Language Toolkit: An API for Automated Refactorings in Eclipse-based IDEs
-----------------------------------------------------------------------------

Anyone who supports a programming language in an Eclipse-based IDE will be asked sooner or later to offer automated refactorings - similar to what is provided by the Java Development Tools (JDT). Since the release of Eclipse 3.1, at least part of this task--which is by no means simple--is supported by a language neutral API: the Language Toolkit (LTK). But how is this API used?

*   [The Language Toolkit: An API for Automated Refactorings in Eclipse-based IDEs](http://www.eclipse.org/articles/Article-LTK/ltk.html)

  

Creating Database Web Applications with Eclipse
-----------------------------------------------

The Eclipse Web Tools Project delivers a feature-rich environment for developing J2EE database-driven web applications. This tutorial walks you through the process of creating a simple database web application using Eclipse WTP, Tomcat, and the Derby database engine.

*   [Original article](http://www.eclipse.org/articles/Article-EclipseDbWebapps/2006-04-10/article.html)
*   [Updated, April 2008](http://www.eclipse.org/articles/article.php?file=Article-EclipseDbWebapps/index.html)

  

Teach Your Eclipse to Speak the Local Lingo
-------------------------------------------

Translations for the Eclipse Project and several top-level projects are contributed to the Eclipse Foundation in every major release of Eclipse. This article provides step-by-step instructions describing what is available, where to download them, how to install them, and how to launch Eclipse in different languages.

*   [Teach Your Eclipse to Speak the Local Lingo](http://www.eclipse.org/articles/Article-Speak-The-Local-Language/article.html)

  

Java Application Profiling using TPTP
-------------------------------------

This article demonstrates how to use the TPTP Profiling tool to profile a Java application for identifying execution related hot spots. It shows how to start the profiling session, use the various TPTP views to analyze the data, identify methods with high execution time then jump to the source code to fix the performance problem.

*   [Java Application Profiling using TPTP](http://www.eclipse.org/articles/Article-TPTP-Profiling-Tool/tptpProfilingArticle.html)

  

The Eclipse Tabbed Properties View
----------------------------------

The Eclipse workbench provides a properties view which is used to view (and/or edit) properties of a selected item. In this article, you will learn how to use the tabbed properties view to create an enhanced user interface for the properties view.

*   [The Eclipse Tabbed Properties View](http://www.eclipse.org/articles/Article-Tabbed-Properties/tabbed_properties_view.html)

  

How to Correctly and Uniformly Use Progress Monitors
----------------------------------------------------

Handling a progress monitor instance is deceptively simple. It seems to be straightforward but it is easy to make a mistake when using them. And, depending on numerous factors such as the underlying implementation, how it is displayed, the result can range from completely ok, mildly confusing or outright silliness. In this article we lay down a few ground rules that will help anyone use progress monitors in a way that will work with the explicit and implicit contract of IProgressMonitor. Also, understanding the usage side makes it easier to understand how to implement a monitor.

*   [How to Correctly and Uniformly Use Progress Monitors](http://www.eclipse.org/articles/Article-Progress-Monitors/article.html)

  

Eclipse Forms: Rich UI for the Rich Client
------------------------------------------

Spice up your rich client with rich user experience using Eclipse Forms. Written as a thin layer on top of SWT, Eclipse Forms allow you to achieve the Web look in your desktop applications without using the embedded browser. This allows you to retain full control of the widgets in the UI and to maintain portability across all operating systems Eclipse already runs on. This article will take you from baby steps to advanced topics of the rich user interface experience of Eclipse Forms.

*   [Eclipse Forms: Rich UI for the Rich Client](http://www.eclipse.org/articles/Article-Forms/article.html)

  

Introducing the GMF Runtime
---------------------------

Graphical Modeling Framework (GMF) is a new Eclipse project with the potential to become a keystone framework for the rapid development of standardized Eclipse graphical modeling editors. GMF is divided in two main components: the runtime, and the tooling used to generate editors capable of leveraging the runtime. Architects and developers involved in the development of graphical editors or of plug-ins integrating both EMF and GEF technologies should consider building their editors against the GMF Runtime component. This article is designed to help understand the benefits of the GMF Runtime by presenting its various value-added features.

*   [Introducing the GMF Runtime](http://www.eclipse.org/articles/Article-Introducing-GMF/article.html)

  

Authoring with Eclipse
----------------------

The topic of technical publishing is relatively new to the world of Eclipse. One can make the argument that technical publishing is just another collaborative development process involving several people with different backgrounds and skills. This article will show that the Eclipse platform is a viable platform for technical publishing by discussing how to write documents such as an article or a book within Eclipse. In fact, this article was written using Eclipse.

*   [Authoring with Eclipse](http://www.eclipse.org/articles/article.php?file=Article-Authoring-With-Eclipse/index.html)

  

Inside the Workbench: A guide to the workbench internals
--------------------------------------------------------

This article describes how the Eclipse 3.1 workbench works, in particular the infrastructure for views and editors. The goal is to teach you about important classes in the workbench, and how they interact. A familiarity with the basic workbench APIs for views, editors, action sets, and so forth is assumed.

*   [Inside the Workbench: A guide to the workbench internals](http://www.eclipse.org/articles/Article-UI-Workbench/workbench.html)

  

Plugging into SourceForge.net
-----------------------------

Congratulations on taking the plunge and writing an open source plug-in for the Eclipse platform. SourceForge.net can provide a good home your plug-in, but information on how best to set up an Eclipse project there is sparse. This article is an introduction to SourceForge for the Eclipse developer. You will learn the features available to the SourceForge.net open source developer community and be guided through the process, from creating a SourceForge project to hosting your Eclipse Update site.

*   [Plugging into SourceForge.net](http://www.eclipse.org/articles/Article-Plugging-into-SourceForge/sourceforge.html)

  

Persisting EMF models with WTP
------------------------------

This article guides you through an example where an EMF model is created without serialization and the serialization is done with the framework from the web tools plug-in org.eclipse.wst.common.emf.

*   [Persisting EMF models with WTP](http://www.eclipse.org/articles/Article-WTP-Persisting-EMF/persisting.html)

  

Extending The Visual Editor: Enabling support for a custom widget
-----------------------------------------------------------------

This tutorial shows how to extend the Visual Editor to support a custom widget. It covers topics such as adding to the Visual Editor's palette, building a BeanInfo class, and working with EMF .override files to introduce custom editor behavior.

*   [Extending The Visual Editor: Enabling support for a custom widget](http://www.eclipse.org/articles/Article-VE-Custom-Widget/customwidget.html)

  

Using GEF with EMF
------------------

The Graphical Editing Framework (GEF) provides a framework for creating visual editors while being model agnostic. In most cases, people bring their own model which tend to be based on Plain Old Java Objects (POJOs). An alternative using POJOs is the Eclipse Modeling Framework (EMF), which provides many features for manipulating models that aren't found in POJOs. The purpose of this article is to build upon the shapes example provided by GEF using the Eclipse Modeling Framework (EMF) and to provide an introduction using EMF based models in GEF based editors.

*   [Using GEF with EMF](http://www.eclipse.org/articles/Article-GEF-EMF/gef-emf.html)

  

Build and Test Automation for plug-ins and features
---------------------------------------------------

Eclipse offers the possibility to build plug-ins automatically outside the Eclipse IDE, which is called "headless build". Eclipse itself is built headless and since Eclipse is an assembly of plug-ins, this feature is also available for any other plug-in. Although the set up of automatic building and testing requires only a couple of files, it can be tedious work to do nonetheless. This article shares the experiences and lessons learned while setting up automatic building and testing for an Open-Source Eclipse plug-in called RDT, Ruby Development Tools.

*   [Build and Test Automation for plug-ins and features](http://www.eclipse.org/articles/Article-PDE-Automation/automation.html)

  

Using OpenGL with SWT
---------------------

OpenGL is a vendor-neutral, multi-platform standard for creating high-performance 2D and 3D graphics. Hardware and software implementations exist on various operating systems, including Windows, Linux and MacOS. OpenGL may be used to render simple 2D charts or complex 3D games. This article describes an experimental Eclipse plug-in that facilitates the use of OpenGL for drawing onto SWT widgets. A short history and overview of OpenGL is presented, followed by an example application.

*   [Using OpenGL with SWT](http://www.eclipse.org/articles/Article-SWT-OpenGL/opengl.html)

  

Folding in Eclipse Text Editors
-------------------------------

Starting with release 3.0, Eclipse allows folding in its text editor. In this article, I explain the new projection infrastructure introduced in the JFace Text framework and show how to extend the XML Editor example provided with Eclipse to allow folding of text.

*   [Folding in Eclipse Text Editors](http://www.eclipse.org/articles/Article-Folding-in-Eclipse-Text-Editors/folding.html)

  

Eclipse User Interface Guidelines: Version 2.1
----------------------------------------------

The Eclipse platform is very flexible and extensible, but this flexibility has a serious drawback. In particular, there is no way within the program to ensure user interface consistency between the registered components within the platform.

*   [Eclipse User Interface Guidelines: Version 2.1](http://www.eclipse.org/articles/Article-UI-Guidelines/Index.html)
*   [Eclipse User Interface Guidelines: Version 2.1](http://www.eclipse.org/articles/Article-UI-Guidelines/index_cn.html)

  

Modeling Rule-Based Systems with EMF
------------------------------------

There are examples of meta-models defined in ECore for modeling objects and relational data. However, not much has been said about how to model rules. This article will define a meta-model in ECore for modeling rule-based systems. We will then use the meta-model to model the solution of a logical problem. Then we will compose some JET templates and generate code from the model, run the generated code through a rule engine and see that the logical problem is correctly solved.

*   [Modeling Rule-Based Systems with EMF](//www.eclipse.org/articles/Article-Rule%20Modeling%20With%20EMF/article.html)

  

Building Administrative Applications in Eclipse
-----------------------------------------------

Eclipse is most commonly used as a platform for tools that allow the user to construct or assemble an end product out of development resources. It is less usual to use Eclipse as an administrative tool for monitoring existing runtime systems or applications. This article will describe some of the issues that arise in this case and illustrate possible solutions. It will show you can build an Eclipse perspective dedicated to the monitoring task. Running processes are shown in a dedicated view which always reflects their current state. You can start/stop the process, manage connections, invoke operations that the server exposes, examine server output and view events generated by the running applications.

*   [Building Administrative Applications in Eclipse](http://www.eclipse.org/articles/Article-Monitor/monitorArticle.html)

  

EMF goes RCP
------------

This article explains how you can use EMF to generate RCP applications. It assumes that you have already used EMF, or have at least read the articles and references available on the documentation section of the EMF web site.

*   [EMF goes RCP](http://www.eclipse.org/articles/Article-EMF-goes-RCP/rcp.html)

  

Building a Database Schema Diagram Editor with GEF
--------------------------------------------------

GEF is a very powerful framework for visually creating and editing models. With a small initial investment, even the relative Eclipse novice can be quickly up and running, building applications with graphical editing capabilities. To illustrate, this article uses a relational database schema diagram editor with a deliberately simplified underlying model, but with enough bells and whistles to show some of the interesting features of GEF at work.

*   [Building a Database Schema Diagram Editor with GEF](http://www.eclipse.org/articles/Article-GEF-editor/gef-schema-editor.html)

  

On the Job: The Eclipse Jobs API
--------------------------------

This article looks at the new Jobs API available as part of Eclipse 3.0. It describes the main portions of the Jobs API and the use of scheduling rules. It also describes some changes to Eclipse resource management including how the Resources plug-in integrates with the new API. Finally, it describes some new UI functionality that has been added to provide feedback to users about jobs that are run in the background.

*   [On the Job: The Eclipse Jobs API](http://www.eclipse.org/articles/Article-Concurrency/jobs-api.html)

  

Branding Your Application
-------------------------

In this article we look at how to create branding for your Eclipse-based application. Branding is how you change the high level visual elements of your product. This includes items such as the splash screen, the about dialog, and the program executable.

*   [Branding Your Application](http://www.eclipse.org/articles/Article-Branding/branding-your-application.html)

  

How to Write an Eclipse Debugger
--------------------------------

One of the major tasks of adding a new language to an Eclipse-based IDE is debugging support. A debugger needs to start and stop the program being debugged, suspend and resume, single-step, manage breakpoints and watch points, and so on. This article explains the Eclipse Platform debug framework and steps through a simple, yet illustrative, example of adding debug support for a new language.

*   [How to Write an Eclipse Debugger](http://www.eclipse.org/articles/Article-Debugger/how-to.html)

  

Viewing HTML pages with SWT Browser widget
------------------------------------------

This article explains how to add HTML viewing capability to an SWT application. The Browser widget provides an easy way to integrate rich HTML content into your application.

*   [Viewing HTML pages with SWT Browser widget](http://www.eclipse.org/articles/Article-SWT-browser-widget/browser.html)

  

Rich Client Tutorial
--------------------

The Rich Client Platform (RCP) is an exciting new way to build Java applications that can compete with native applications on any platform. This tutorial is designed to get you started building RCP applications quickly. It has been updated for Eclipse 3.1.2.

*   [Part 1](http://www.eclipse.org/articles/Article-RCP-1/tutorial1.html)
*   [Part 2](http://www.eclipse.org/articles/Article-RCP-2/tutorial2.html)
*   [Part 3](http://www.eclipse.org/articles/Article-RCP-3/tutorial3.html)

  

A Basic Image Viewer
--------------------

This article shows how to extend the SWT Canvas to implement a mini image viewer plug-in using Java2D transforms. The extended image canvas can be used to scroll and zoom large images, and can also be extended to apply other transforms. The implementation is based on SWT and the non-UI portions of AWT. The plug-in has been tested on Windows, Linux GTK, and Mac OS X Carbon with Eclipse 2.1 or better.

*   [A Basic Image Viewer](http://www.eclipse.org/articles/Article-Image-Viewer/Image_viewer.html)

  

Mutatis mutandis - Using Preference Pages as Property Pages
-----------------------------------------------------------

A common problem in the implementation of applications is the implementation of project-specific properties that override workbench-wide preferences on project or file level. The naive approach is to implement these pages from scratch. However, writing the same code twice is a boring task and leads to increased maintenance efforts. In this article we show how existing preferences pages (with or without field editors) can be easily converted into pages that can act as both preference and property pages. We demonstrate this by implementing the abstract class FieldEditorOverlayPage providing the necessary functionality.

*   [Mutatis mutandis - Using Preference Pages as Property Pages](http://www.eclipse.org/articles/Article-Mutatis-mutandis/overlay-pages.html)

  

A small cup of SWT: A bag of hints, tricks and recipes for developing SWT apps on the Pocket PC
-----------------------------------------------------------------------------------------------

Are you interested in developing applications for the Microsoft® Pocket PC? Are you a desktop developer curious about embedded user interfaces? A well-built embedded application is both user and resource friendly. User expectations are high. Resources are very limited...

*   [A small cup of SWT: A bag of hints, tricks and recipes for developing SWT apps on the Pocket PC](http://www.eclipse.org/articles/Article-small-cup-of-swt/pocket-PC.html)

  

Taking a look at SWT Images
---------------------------

SWT's Image class can be used to display images in a GUI. The most common source of images is to load from a standard file format such as GIF, JPEG, PNG, or BMP. Some controls, including Buttons and TreeItems, are able to display an Image directly through the setImage(Image) method, but any control's paint event allows images to be drawn through the callback's graphic context. SWT's ImageData class represents the raw data making up an SWT Image and determines the color for each pixel coordinate. This article shows the correct uses of ImageData and Image, shows how to load images from files, and how to achieve graphic effects such as transparency, alpha blending, animation, scaling, and custom cursors.

*   [Taking a look at SWT Images](http://www.eclipse.org/articles/Article-SWT-images/graphics-resources.html)

  

PDE Does Plug-ins
-----------------

The Plug-in Development Environment (PDE) provides a set of tools that assist the developer in every stage of plug-in development from genesis to deployment. This article chronicles the creation, development, testing, building, and deployment of a simple "Hello World" plug-in using a subset of these tools.

*   [PDE Does Plug-ins](http://www.eclipse.org/articles/Article-PDE-does-plugins/PDE-intro.html)

  

How To Keep Up To Date
----------------------

This article shows you how to create and publish bundles of plug-ins (called features) to an update site so that customers can download and install them directly into Eclipse using the Eclipse update manager. This has many advantages over the low tech way of delivering new or updated plug-ins in a zip file that someone manually unzips into the directory where Eclipse is installed.

*   [How To Keep Up To Date](http://www.eclipse.org/articles/Article-Update/keeping-up-to-date.html)

  

JET Tutorial Part 2 (Write Code that Writes Code)
-------------------------------------------------

In Part 2 of this JET (Java Emitter Templates) tutorial, we will take a look at the JET engine API. You will learn how to write plug-ins that use the classes in the JET package to generate Java source code.As a real-world example, we will create a plug-in that takes user input and generates a Typesafe Enumeration class. The generated source code is based on a JET template that can be distributed with the plug-in, allowing users of the plug-in to customize the generated code by editing the template. This article also provides a short reference to the JET API.

*   [JET Tutorial Part 2 (Write Code that Writes Code)](http://www.eclipse.org/articles/Article-JET2/jet_tutorial2.html)

  

Launching Java Applications Programmatically
--------------------------------------------

Application developers require the ability to run and debug code in order to test it. Tool developers require the ability to launch Java™ applications that assist in application development - for example, starting and stopping a Web server on which servlets, JSPs, and HTML pages can be tested; or launching a VM on which scrapbook evaluations can be performed. This article focuses on the high-level API provided by the Java launching plug-in that tool developers can leverage for the programmatic launching of local Java applications.

*   [Launching Java Applications Programmatically](http://www.eclipse.org/articles/Article-Java-launch/launching-java.html)

  

Adding Drag and Drop to an SWT Application
------------------------------------------

Drag and drop provides a quick and easy mechanism for users to re-order and transfer data within an application and between applications. This article is an overview of how to implement Drag and Drop and Clipboard data transfers within an SWT application.

*   [Adding Drag and Drop to an SWT Application](http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html)

  

Display a UML Diagram using Draw2D
----------------------------------

The Graphical Editing Framework (GEF) ships with a painting and layout plug-in called Draw2D. Draw2D provides figures and layout managers which form the graphical layer of a GEF application. This article focuses only on the use of Draw2D to render a simple UML class diagram. While Draw2D can be used for standalone purposes, it is not an editing framework. Most applications will use the GEF plug-in as the editing layer.

*   [Display a UML Diagram using Draw2D](http://www.eclipse.org/articles/Article-GEF-Draw2d/GEF-Draw2d.html)

  

Drag and Drop in the Eclipse UI
-------------------------------

In this article, we discuss the drag and drop facilities provided by JFace and the Eclipse platform UI. After reading this, you will know how to add drag and drop support to your own Eclipse views, and how that support will interact with the standard views in the Eclipse platform. Along the way, we'll also discuss that keyboard relative of drag and drop: cut and paste. You'll learn that putting your own custom objects on the clipboard is easy once you've figured out the basics of drag and drop.

*   [Drag and Drop in the Eclipse UI](http://www.eclipse.org/articles/Article-Workbench-DND/drag_drop.html)

  

Using Native Drag and Drop with GEF
-----------------------------------

Native drag and drop provides the ability to drag data from one GUI object to another GUI object, which could potentially be in another application. GEF allows access to the operating system's underlying drag and drop infrastructure through SWT. This article will provide an in-depth look at GEF's drag and drop functionality and show some simple examples of how to take advantage of this API.

*   [Using Native Drag and Drop with GEF](http://www.eclipse.org/articles/Article-GEF-dnd/GEF-dnd.html)

  

JET Tutorial Part 1 (Introduction to JET)
-----------------------------------------

Generating source code can save you time in your projects and can reduce the amount of tedious redundant programming. Generating source code can be powerful, but the program that writes the code can quickly become very complex and hard to understand. One way to reduce complexity and increase readability is to use templates. In this article you will learn how to create JET templates, how to use the JET Nature and JET Builder to automatically translate templates into Java classes, and how to use these classes to generate source code. This article also provides a short reference to the JET syntax.

*   [JET Tutorial Part 1 (Introduction to JET)](http://www.eclipse.org/articles/Article-JET/jet_tutorial1.html)

  

Inside the Memory View: A Guide for Debug Providers
---------------------------------------------------

There are many programming languages that allow access and discrete control of system memory. If you are a debug provider for one of these languages, then you probably have a requirement to provide support for debug-time memory inspection and manipulation. The Eclipse debug framework provides a Memory View, along with an extensible framework to simplify and standardize this task. This article introduces the Memory View and describes how to add your own customized memory support.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-MemoryView/index.html)

  

Building and delivering a table editor with SWT/JFace
-----------------------------------------------------

The JFace API provides several classes that can be used to build editable table views. In this article, we present a fairly extensive example that exercises the JFace and SWT classes needed to implement a table with cell editors for check-boxes, free text and combo-boxes. We also show how to package and deliver the classes into a stand-alone (non-Eclipse) Java application.

*   [Building and delivering a table editor with SWT/JFace](http://www.eclipse.org/articles/Article-Table-viewer/table_viewer.html)

  

Graphics Context - Quick on the draw
------------------------------------

The package `org.eclipse.swt.graphics` contains classes that allow management of graphics resources. Graphics can be drawn on anything that implements `org.eclipse.swt.graphics.Drawable`, which includes `org.eclipse.swt.widgets.Control` and `org.eclipse.swt.graphics.Image`. The class `org.eclipse.swt.graphics.GC` encapsulates all of the drawing API, including how to draw lines and shapes, draw text and images and fill shapes. This article shows how to use a GC to draw onto an Image, or onto a control through its paintEvent callback. The Canvas control, specifically designed for drawing operations, has a number of constructor style bits that allow you to determine when and how painting occurs, and the article shows how to use these.

*   [Graphics Context - Quick on the draw](http://www.eclipse.org/articles/Article-SWT-graphics/SWT_graphics.html)
*   [Graphics Context - Quick on the draw](http://www.eclipse.org/articles/article.php?charset=utf-8&file=Article-SWT-graphics/index_cn.html)

  

Notes on the Eclipse Plug-in Architecture
-----------------------------------------

Eclipse plug-ins embody an architectural pattern for building an application from constituent parts. This article presents an in-depth view of the participant roles and collaborations of this architectural pattern, as they exist in an instance of the Eclipse workbench. The goal is to provide an understanding of plug-ins, and of how plug-in extensions are defined and processed, independently of the mechanics of using the Eclipse workbench to produce plug-ins.

*   [Notes on the Eclipse Plug-in Architecture](http://www.eclipse.org/articles/Article-Plug-in-architecture/plugin_architecture.html)

  

Designing Accessible Plug-ins in Eclipse
----------------------------------------

Accessibility for disabled users is now a priority in application development as advances in techniques and support within operating systems have now made this possible. This article covers the Eclipse accessibility support, general tips for creating accessible plug-ins, and the types of disabilities that the Eclipse accessibility support assists. This is all illustrated using an example of making a view accessible.

*   [Updated for Eclipse Platform 3.5](http://www.eclipse.org/articles/article.php?file=Article-Accessibility351/index.html)

  

Take control of your properties
-------------------------------

The Eclipse workbench provides a properties view which is used to view (and/or edit) properties of a selected item. In this article, you will learn how to use the properties view to dynamically modify the properties of a GUI button.

*   [Take control of your properties](http://www.eclipse.org/articles/Article-Properties-View/properties-view.html)

  

Project Builders and Natures
----------------------------

This article discusses two central mechanisms that are associated with projects in an Eclipse workspace. The first of these is incremental project builders, which create some built state based on the project contents, and then keep that built state synchronized as the project contents change. The second is project natures, which define and manage the association between a given project and a particular plug-in or feature.

*   [Project Builders and Natures](http://www.eclipse.org/articles/Article-Builders/builders.html)

  

Understanding Decorators in Eclipse
-----------------------------------

Decorators, as the name suggests, are used for adorning/annotating resources with useful information. Decorators can be used by plug-ins to convey more information about a resource and other objects displayed in different workbench views. This article, with the help of a simple plug-in example, will illustrate the steps involved in decorating resources, along with some best practice approaches for decorating resources. Finally, we will discuss performance issues that may arise when enabling decorators, and briefly go over the new lightweight decorators found in Eclipse 2.1.

*   [Understanding Decorators in Eclipse](http://www.eclipse.org/articles/Article-Decorators/decorators.html)

  

We Have Lift-off: The Launching Framework in Eclipse
----------------------------------------------------

The ability to launch (run or debug) code under development is fundamental to an IDE. But because Eclipse is more of a tools platform than a tool itself, Eclipse's launching capabilities depend entirely on the current set of installed plug-ins. This article describes the API available to build launching plug-ins and works through developing an example launcher using this API.

*   [We Have Lift-off: The Launching Framework in Eclipse](http://www.eclipse.org/articles/Article-Launch-Framework/launch.html)

  

Creating JFace Wizards
----------------------

This article shows you how to implement a wizard using the JFace toolkit and how to contribute your wizard to the Eclipse workbench. A wizard whose page structure changes according to user input is implemented to demonstrate the flexibility of wizard support.

*   [Creating JFace Wizards](http://www.eclipse.org/articles/Article-JFaceWizards/wizardArticle.html)

  

Using EMF
---------

This article introduces EMF, the Eclipse Modeling Framework, and will help you get started using EMF in your own Eclipse plug-ins.

*   [Using EMF](http://www.eclipse.org/articles/Article-Using%20EMF/using-emf.html)

  

How You've Changed!: Responding to resource changes in the Eclipse workspace
----------------------------------------------------------------------------

Many tools and user interface elements are interested in processing resource changes as they happen. For example, the task list wants to update new or changed markers, the navigator wants to reflect added and deleted resources, and the Java compiler wants to recompile modified Java files. Such notifications are potentially costly to compute, manage and broadcast. The Eclipse Platform resource model includes a series of mechanisms for efficiently notifying clients of resource changes. This article outlines these facilities and gives some examples of their use.

*   [How You've Changed!: Responding to resource changes in the Eclipse workspace](http://www.eclipse.org/articles/Article-Resource-deltas/resource-deltas.html)

  

How to Internationalize your Eclipse Plug-In
--------------------------------------------

This article is a roadmap for writing Eclipse plug-ins destined for the international market. We'll begin with a brief review of the motivations and technical challenges of internationalization, followed by step-by-step instructions of how to internationalize your Eclipse plug-in.

*   [How to Internationalize your Eclipse Plug-In](http://www.eclipse.org/articles/Article-Internationalization/how2I18n.html)
*   [Ukranian Translation](http://softdroid.net/how2I18n-ua)

  

How to Test Your Internationalized Eclipse Plug-In
--------------------------------------------------

This article shows you how to validate your internationalized product and prepares you for the types of common problems you can expect during translation testing. It includes an Eclipse plug-in that defines a Properties File Compare view that can help your translation testers find errors more quickly.

*   [How to Test Your Internationalized Eclipse Plug-In](http://www.eclipse.org/articles/Article-TVT/how2TestI18n.html)

  

Simplifying Preference Pages with Field Editors
-----------------------------------------------

Even though preference pages can be simple to program, you can spend a lot of time getting them "just right." Field editors make this task faster and easier by providing the behavior for storing, loading, and validating preferences. Field editors also define some of the behavior for grouping and laying out widgets on a preference page.

*   [Simplifying Preference Pages with Field Editors](http://www.eclipse.org/articles/Article-Field-Editors/field_editors.html)

  

Preferences in the Eclipse Workbench UI
---------------------------------------

In the Eclipse Platform plug-in developers define preference pages for their plug-ins for use in the Workbench Preferences Dialog. This article explains when to use a preference and some of the features the Eclipse Platform provides to support preferences.

*   [Preferences in the Eclipse Workbench UI](http://www.eclipse.org/articles/Article-Preferences/preferences.htm)

  

Help Part 1: Contributing a Little Help
---------------------------------------

The Eclipse Platform's help system defines two extension points ("toc" and "contexts") that allow individual plug-ins to contribute online help and context-sensitive help for their components. In this article we will investigate the "toc" extension point and how you can use it to contribute documentation for your plug-in.

*   [Help Part 1: Contributing a Little Help](http://www.eclipse.org/articles/Article-Online%20Help%20for%202_0/help1.htm)

  

How to use the JFace Tree Viewer
--------------------------------

The goal of this article is to teach you how to use TreeViewers in your Eclipse plug-ins or stand-alone JFace/SWT applications. We'll start with a simple example and progressively add functionality.

*   [How to use the JFace Tree Viewer](http://www.eclipse.org/articles/Article-TreeViewer/TreeViewerArticle.htm)

  

Creating an Eclipse View
------------------------

In the Eclipse Platform a view is typically used to navigate a hierarchy of information, open an editor, or display properties for the active editor. In this article the design and implementation of a view will be examined in detail. You'll learn how to create a simple view based on SWT, and a more advanced view using the JFace viewer hierarchy. We'll also look at ways to achieve good integration with many of the existing features in the workbench, such as the window menu and toolbar, view linking, workbench persistence and action extension.

*   [Creating an Eclipse View](http://www.eclipse.org/articles/viewArticle/ViewArticle2.html)

  

Contributing Actions to the Eclipse Workbench
---------------------------------------------

The Eclipse Platform is an open and extensible platform. This article explains in detail how the Workbench can be extended to add new actions and provides guidance to the plug-in developers on how they can design for extensibility.

*   [Updated article](https://www.eclipse.org/articles/article.php?file=Article-action-contribution/index.html)

Into the Deep End of the SWT StyledText Widget
----------------------------------------------

The StyledText widget is a customizable widget that can be used to display and edit text with different colors and font styles. In this article we discuss why you might want to customize the StyledText widget and how you would do that.

*   [Into the Deep End of the SWT StyledText Widget](http://www.eclipse.org/articles/StyledText%202/article2.html)

  

Using Perspectives in the Eclipse UI
------------------------------------

In the Eclipse Platform a Perspective determines the visible actions and views within a window. Perspectives also go well beyond this by providing mechanisms for task oriented interaction with resources in the Eclipse Platform, multi-tasking and information filtering. In this article the concepts behind perspectives are examined. The process for perspective definition, extension and instantiation will also be covered in detail with coding examples and sample scenarios.

*   [Using Perspectives in the Eclipse UI](http://www.eclipse.org/articles/using-perspectives/PerspectiveArticle.html)

  

How to Use the Eclipse API
--------------------------

The Eclipse Platform offers a comprehensive API (Application Programmer Interface) to developers writing plug-ins. This article discusses the general ground rules for using the Eclipse Platform API, including how to tell API from non-API, and how to stay in the API "sweet spot" to avoid the risk of being broken as the platform and its APIs evolve. These general ground rules are also recommended practice for plug-ins that must declare API elements of their own.

*   [How to Use the Eclipse API](http://www.eclipse.org/articles/article.php?file=Article-API-Use/index.html)

  

Getting Your Feet Wet with the SWT StyledText Widget
----------------------------------------------------

The StyledText widget is a customizable widget that can be used to display and edit text with different colors and font styles. This article presents an overview of the concepts, issues, and rules that you should be aware of when using the StyledText widget.

*   [Getting Your Feet Wet with the SWT StyledText Widget](http://www.eclipse.org/articles/StyledText%201/article1.html)

  

SWT Color Model
---------------

The combination of platforms, display devices and color depth makes providing an easy to use yet powerful and portable color model an interesting challenge. In this article we will examine the color management models of Windows® and X/Motif and then dig into the makings of the SWT color model and its implications for client code.

*   [SWT Color Model](http://www.eclipse.org/articles/Article-SWT-Color-Model/swt-color-model.htm)

  

Using Images in the Eclipse UI
------------------------------

Managing images in a large graphical application can be a daunting task. Since modern operating systems such as Windows® only support a small number of images in memory at once, an application's icons and background images must be carefully managed and sometimes shared between widgets. This article describes the image management facilities provided by the Eclipse Platform, along with some best practice guidelines to keep in mind when writing your own Eclipse UI plug-ins. We assume the reader already has a basic understanding of Eclipse, the UI extension points defined by the Eclipse Platform, and the Standard Widget Toolkit (SWT).

*   [Using Images in the Eclipse UI](http://www.eclipse.org/articles/Article-Using%20Images%20In%20Eclipse/Using%20Images%20In%20Eclipse.html)

  

Mark My Words: Using markers to tell users about problems and tasks
-------------------------------------------------------------------

Eclipse workbench has a central mechanism for managing resource annotations. They are called markers. In this article, you will learn how to use markers to mark-up resources as well as how to define your own marker types and enhance the Tasks view to handle them in a special way.

*   [Mark My Words: Using markers to tell users about problems and tasks](http://www.eclipse.org/articles/Article-Mark%20My%20Words/mark-my-words.html)

  

Levels Of Integration: Five ways you can integrate with the Eclipse Platform
----------------------------------------------------------------------------

The types of problems web application developers face today require the use of a diverse set of tools that operate in many domains. In order to provide flexible tool integration, a tool integration platform must allow tool developers to target different levels or integration based on the desired level of investment, time to market, and specific tool needs. Each integration level determines how a tool must behave, and what end users can expect as a result. This article defines the different levels of tool integration supported by Eclipse, and gives an overview of how they work.

*   [Levels Of Integration: Five ways you can integrate with the Eclipse Platform](http://www.eclipse.org/articles/Article-Levels-Of-Integration/levels-of-integration.html)

  

ActiveX Support In SWT
----------------------

OLE Documents, such as Word, Excel or PowerPoint, and ActiveX Controls such as Internet Explorer are COM objects that can be embedded into other applications running on a Microsoft® Windows ® platform. This article provides an overview of integrating OLE Documents and ActiveX Controls into an application using SWT.

*   [ActiveX Support In SWT](//www.eclipse.org/articles/article.php?file=Article-ActivexSupportInSwt/index.html)

Creating Your Own Widgets using SWT
-----------------------------------

When writing applications, you typically use the standard widgets provided by SWT. On occasion, you will need to create your own custom widgets. For example, you might want to add a new type of widget not provided by the standard widgets, or extend the functionality of an existing widget. This article explains the different SWT extension strategies and shows you how to use them.

*   [Creating Your Own Widgets using SWT](http://www.eclipse.org/articles/Article-Writing%20Your%20Own%20Widget/Writing%20Your%20Own%20Widget.htm)

  

SWT: The Standard Widget Toolkit
--------------------------------

The two part series of articles describes the design ideas behind SWT. SWT is the software component that delivers native widget functionality for the Eclipse platform in an operating system independent manner. It is analogous to AWT/Swing in Java with a difference - SWT uses a rich set of native widgets. Even in an ideal situation, industrial strength cross platform widget libraries are very difficult to write and maintain. This is due to the inherent complexity of widget systems and the many subtle differences between platforms. There are several basic approaches that have helped significantly to reduce the complexity of the problem and deliver high quality libraries.

*   [Part 1: Implementation Strategy](http://www.eclipse.org/articles/Article-SWT-Design-1/SWT-Design-1.html)
*   [Part 2: Managing Operating System Resources](http://www.eclipse.org/articles/swt-design-2/swt-design-2.html)

  

Understanding Layouts in SWT
----------------------------

When writing applications in SWT, you may need to use layouts to give your windows a specific look. A layout controls the position and size of children in a Composite. Layout classes are subclasses of the abstract class Layout. This article shows you how to work with standard layouts, and write your own custom layout class.

*   [Understanding Layouts in SWT](http://www.eclipse.org/articles/article.php?file=Article-Understanding-Layouts/index.html)

  

Introducing AJDT: The AspectJ Development Tools
-----------------------------------------------

The AspectJ Development Tools (AJDT) project is an Eclipse Tools project which enables the development of AspectJ applications in Eclipse. This article gives an overview of the capabilities of AJDT 1.4 for Eclipse 3.2 by describing several scenarios, including adding aspects to existing Java® projects, working with aspects across multiple projects, and using the load-time weaving capabilities of AspectJ 5.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/Article-Introducing-AJDT/article.html)

  

Abstract Syntax Tree
--------------------

The Abstract Syntax Tree is the base framework for many powerful tools of the Eclipse IDE, including refactoring, Quick Fix and Quick Assist. The Abstract Syntax Tree maps plain Java source code in a tree form. This tree is more convenient and reliable to analyse and modify programmatically than text-based source. This article shows how you can use the Abstract Syntax Tree for your own applications.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/Article-JavaCodeManipulation_AST/index.html)

  

Running Web Service Scenarios using Ant
---------------------------------------

This tutorial shows how to run Web services scenarios (top down web service, bottom up web service and client generation) by way of an Ant task from within Eclipse.

*   [Web Tools Platform Article](http://www.eclipse.org/webtools/jst/components/ws/1.0/tutorials/WebServiceAntTask/WebServiceAntTask.html)

  

Unleashing the Power of Refactoring
-----------------------------------

In this article, Tobias Widmer sheds light on the services offered by the Eclipse Java Development Tools (JDT) and the Refactoring Language Toolkit (LTK) to support automated Java refactorings, explains how these services are used by refactorings to perform searches on the Java workspace, rewrite existing code and provide a rich user-interface to present the results of the refactoring. To demonstrate this combination of Java-specific and language-neutral frameworks, this article presents a simple but working refactoring implementation for an 'Introduce Indirection' refactoring designed to introduce an indirection method for existing method invocations.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-Unleashing-the-Power-of-Refactoring/index.html)

  

Building Eclipse Plugins with Maven 2
-------------------------------------

In a mature and agile development environment, it is vital that the developers are kept productive and that builds are done continuously and dependably. Eclipse is a great environment for developers and Maven 2 (in conjunction with Continuum or Cruise Control) is a great environment for continuous integration. As with most great software, both Eclipse and Maven 2 tend to be somewhat opinionated and the two don't always see eye to eye on how things should be done. This article describes how to use Maven 2 with Eclipse in general. In particular we will focus on how to develop, package and test Eclipse plugins using Eclipse (Callisto) and Maven 2 (2.0.4) efficiently.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-Eclipse-and-Maven2/index.html)

  

Using TPTP to Automate Functional Testing
-----------------------------------------

This article provides an overview and some details of the design of a functional test automation solution that was built using testing frameworks available within the TPTP Testing Tools Project.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-TPTPAutomateFunctionalTesting/index.html)

  

Simple Image Effects for SWT
----------------------------

This article explores simple emboss, blur, glow, and drop shadow algorithms that can be applied to images in SWT.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-SimpleImageEffectsForSWT/index.html)

  

Using the BIRT Chart Engine in Your Plug-in
-------------------------------------------

The BIRT Chart Engine is a powerful business chart generation tool that can be used as a standalone charting component. This article introduces the basic concepts of BIRT Chart Engine, explains what BIRT Chart Engine is composed of, and illustrates how to create a chart and use it as widget.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-BIRTChartEngine/index.html)

  

Swing/SWT Integration
---------------------

Swing and SWT are sometimes seen as strictly competing technologies. Some people have strong opinions on which UI toolkit to use exclusively for client applications. However, in the real world, ideological extremes are often impractical. Some valid use cases require both technologies to coexist in a single application. While mixing the two toolkits is not a simple task, it can be done, and it can be done such that the two toolkits are smoothly integrated. This article discusses the steps necessary to achieve good Swing/SWT integration. It focuses on the use case of embedding existing Swing components into an SWT-based Rich Client Platform application.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-Swing-SWT-Integration/index.html)

  

How to process OCL Abstract Syntax Trees
----------------------------------------

The Model Development Tools Object Constraint Language (MDT OCL) project provides the building blocks for Model-Driven tools to weave OCL declarative specifications into software artifacts. We showcase some of these possibilities, taking as starting point a plug-in to visualize OCL abstract syntax trees (ASTs) in the form of annotated trees. This example motivates some practical tips about patterns for OCL visitors, including using Java 5 generics and achieving conciseness by letting MDT OCL take care of the "walking" order. To really reap the benefits of OCL-enriched specifications, tools in our modeling chain have to be able to transform such expressions into the target software platform (e.g. compile into Java, translate into SQL). Work in this area is summarized, to ease jump-starting your own OCL processing project.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-HowToProcessOCLAbstractSyntaxTrees/index.html)

  

Branching with Eclipse and CVS
------------------------------

This two-part article describes how to use the branch and merge features provided by Eclipse's CVS support. Part one presents a brief branch-and-merge scenario designed to quickly illustrate some branch-and-merge features. Part two shows how to rebase a subbranch with changes from the main branch before merging the subbranch back into the main branch.

*   [Part 1](http://www.eclipse.org/articles/article.php?file=Article-BranchingWithEclipseAndCVS/article1.html)
*   [Part 2](http://www.eclipse.org/articles/article.php?file=Article-BranchingWithEclipseAndCVS/article2.html)

  

Adding Help Support to a Rich Client Platform (RCP) Application
---------------------------------------------------------------

In this article we show you how you can incorporate the Eclipse help system into your Rich Client Application in a series of easy to follow steps. We also show you how to set up context help and how to fine tune your help settings.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-AddingHelpToRCP/index.html)

  

Eclipse Forms: New in 3.3
-------------------------

Eclipse Forms is a layer on top of SWT that allows you to achieve a web-like feel inside your desktop applications without having to resort to an embedded browser. In this article, the new features added to Eclipse Forms in version 3.3 are discussed. Readers are expected to be familiar with Eclipse Forms and its concepts.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-Forms33/index.html)

  

Defining Generics with UML Templates
------------------------------------

Generics in Java have been around for a while but support for mapping generically specified artifacts in UML to their Ecore representation is new to UML2 2.1. This article will walk the reader through the details of the mapping process with the end goal of producing generically specified code. This article assumes some level of familiarity with generics and is not intended as a tutorial in Java generics.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-Defining-Generics-with-UML-Templates/index.html)

  

Integrating EMF and GMF Generated Editors
-----------------------------------------

This article provides a walk-through example how to combine the editor plug-ins generated by the Eclipse Modeling Framework (EMF) and the Graphical Modeling Framework (GMF) to create an integrated editor that supports graphical and list- oder tree-based editing of the same information. The approach outlined in this article can easily be used as a starting point for the implementation of arbitrary multi-page editor solutions that contain GMF-based graphical editors.

*   [Integrating EMF and GMF Generated Editors](http://www.eclipse.org/articles/article.php?file=Article-Integrating-EMF-GMF-Editors/index.html)

  

Build your own textual DSL with Tools from the Eclipse Modeling Project
-----------------------------------------------------------------------

Domain Specific Languages (DSLs) are a hot topic nowadays. While creating internal DSLs is no big deal, creating external DSLs have been said to be hard to create. In this tutorial we will show you how easy it is to create your own DSL with tools from the Eclipse Modeling Project (EMP) in less than one hour.

*   [Build your own textual DSL with Tools from the Eclipse Modeling Project](http://www.eclipse.org/articles/article.php?file=Article-BuildYourOwnDSL/index.html)

  

Automating Eclipse PDE Unit Tests using Ant
-------------------------------------------

This article outlines how to integrate your PDE unit tests into an Ant based automated build, using a simple Eclipse plug-in as an example, some simple java classes, basic Eclipse plug-in techniques and some standard Ant tasks.

*   [Automating Eclipse PDE Unit Tests using Ant](http://www.eclipse.org/articles/article.php?file=Article-PDEJUnitAntAutomation/index.html)

  

Adapters
--------

The adapter pattern is used extensively in Eclipse. The use of this pattern allows plug-ins to be loosely coupled, yet still be tightly integrated in the extremely dynamic Eclipse runtime environment. In this article, we show you how to use the adapter framework to make your own objects adaptable, and adapt other objects.

*   [Adapters](http://www.eclipse.org/articles/article.php?file=Article-Adapters/index.html)

  

Automating the embedding of Domain Specific Languages in Eclipse JDT
--------------------------------------------------------------------

The Eclipse Java Development Tools (JDT) excels at supporting the editing and navigation of Java code, setting the bar for newer IDEs, including those for Domain Specific Languages (DSLs). Although IDE generation keeps making progress, most developers still rely on traditional ways to encapsulate new language abstractions: frameworks and XML dialects. We explore an alternative path, Internal DSLs, by automating the generation of the required APIs from Ecore models describing the abstract syntax of the DSLs in question. To evaluate the approach, we present a case study (statecharts) and discuss the pros and cons with respect to other approaches.

*   [Eclipse Corner Article](http://www.eclipse.org/articles/article.php?file=Article-AutomatingDSLEmbeddings/index.html)

  

Dynamic User Assistance in Eclipse Based Applications
-----------------------------------------------------

Development environments have become very complex. As a result, providing users with context relevant assistance is critical to helping them succeed with your tools and applications. This article will introduce you to Eclipse's powerful Dynamic Assistance framework and provide examples that illustrate how you can take full advantage of its capabilities.

*   [Dynamic User Assistance in Eclipse Based Applications](http://www.eclipse.org/articles/article.php?file=Article-DynamicCSH/index.html)

  

BIRT Extension Mechanism, Part 1: Custom Report Items
-----------------------------------------------------

This article introduces the extension mechanism of BIRT report model, engine and designer, and shows how to create custom custom report items step-by-step.

*   [BIRT Extension Mechanism, Part 1: Custom Report Items](http://www.eclipse.org/articles/article.php?file=Article-BIRT-ExtensionTutorial1/index.html)

  

BIRT Extension Mechanism, Part 2
--------------------------------

This article introduces the extension mechanism of BIRT report model, engine and designer, and shows how to create custom extended report items step-by-step.

*   [BIRT Extension Mechanism, Part 2](http://www.eclipse.org/articles/article.php?file=Article-BIRT-ExtensionTutorial2/index.html)

  

How to Fix a Bug in Eclipse
---------------------------

In this article, the reader will be guided through the entire process of a search for an open bug within one of the Eclipse projects and the steps that may be required in order to implement a fix to be contributed back to the Eclipse community. This article assumes that the reader is familiar with using CVS and Subversion in Eclipse as well as the basics required to develop Eclipse plug-ins.

*   [How to Fix a Bug in Eclipse](http://www.eclipse.org/articles/article.php?file=Article-How-to-Fix-a-Bug-in-Eclipse/index.html)

  

Babel Pseudo Translations
-------------------------

Babel Pseudo Translations are a very useful tool for globalization testing of Eclipse projects. This article provides step-by-step instructions and examples describing what are the Babel Pseudo Translations, where to download them, how to install them, and how to launch Eclipse in Babel Pseudo Translations. Eclipse developers can use the Babel Pseudo Translations to verify the translatability of their projects and make sure Eclipse excels in national language support.

*   [Babel Pseudo Translations](http://www.eclipse.org/articles/article.php?file=Article-babel-pseudo-translations/article.html)

