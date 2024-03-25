

Common Navigator Framework
==========================

The Common Navigator Framework (CNF) is designed to help you integrate document models into a navigator experience, integrate content that isn't specific to the workbench, allow you to absorb other content seamlessly (in particular resource and Java(tm) models), and mitigate your expense in time and effort to absorb incremental enhancements from release to release from layers beneath you.

The CNF began as a product solution for a general problem in IBM Rational Application Developer v6.0, and has been contributed to the open source Eclipse Platform in 3.2 to allow the community to better integrate their navigational viewers and provide a more cohesive user experience across software layers and products.

To begin using the CNF, developers should consult the schema and API documentation in the Platform Help (Window > Help > Platform Developer's Guide > Reference > Schema + API).

Also have a look at the following resources for the CNF:

*   [Michael Elder's CNF Tutorials](http://scribbledideas.blogspot.com/)


Use Cases
=========

The Common Navigator Framework (CNF) can be used both within the IDE (it's the Project Explorer) and in RCP applications.

In order to provide the best support for the 3.5 release, we would like to understand the current and desired uses of the CNF, so please update this page and include them here.

IDE
===

**Data Mapper Plugin** \- (as described in the RCP section)

**Project Explorer** \- The Project Explorer is the default project view in the Resource perspective replacing the Navigator view. Project Explorer is also reused by CDT as default project view with custom content extensions. There are still some deficiencies compared to Navigator and Package Explorer view which hinders acceptance as a shared project view, e.g.

*   There is no "Other Projects" working set on Project Explorer - [196595](http://bugs.eclipse.org/196595)
*   \[CommonNavigator\] add "Sort by" view menu - [208801](http://bugs.eclipse.org/208801)

\-\- [Anton Leherbauer, CDT Committer](mailto:anton.leherbauer@windriver.com)

  
**Data Source Explorer** \- The Data Source Explorer (DSE) is a part of the Data Tools Platform (DTP) Project's Data Development perspective. It extends the CommonNavigator to display custom content equating to categories, connection profiles, and then once a profile is connected, it displays the content of the connection profile. Content is typically database-related (database/catalog, schema, table, stored procedure, columns, etc...), but can be resource-based as well (like the sample File connection profile). -- [Brian Fitzpatrick, DTP PMC Lead/Committer](mailto:brianf@sybase.com)

RCP - With Resources
====================

**Data Mapper** \- This RCP application uses the CNF with resources to manage data structure definitions, map definitions and other objects. The resources can exist in the local file system, or using EFS in a zip file or plugin. There is a set of model objects representing the structures, maps, etc and these are managed by an internal dependency manager so that changes in the resources (through the CNF) cause the model objects to be updated. Contact: Francis Upton (francisu@ieee.org).

**Interactive Processing Environment** \- Think of it as an IDE for seismic processors. We use CNF for our custom project explorer. We've added a number of filters, sorters, etc. With Michael's blog I found CNF easy to get up and running. See [Oil and Gas Industry Using Eclipse](http://richclientplatform.blogspot.com/2007/12/oil-and-gas-industry-using-eclipse.html) for screen captures. Contact: David Kyle (davidk@kelman.com).

RCP - Objects Other than Resources
==================================

Please tell us about the objects you would use the CNF with, and also indicate why you did not choose to use resources. The CNF in conjunction with resources provides considerable benefit; we would like to hear about cases where this combination will not work for whatever reason.

**Hibernate Data model browser** \- We wanted to use CNF for Hibernate tools to show the static data model and for browsing the object graph/query result. Neither of these are in any way mappable to resources. The problem we got into was that we could not find a way to lazily load children with visual feedback in the tree. Contact: Max Rydahl Andersen (max.andersen@jboss.com).

**Business Object Navigation** \- I've worked on several projects which used the CNF to navigate business objects not accessible via resources. One example was a logistics application which managed supervisors, trucks, routes, and shipping destinations. The data was retrieved using web services, but in other cases the data has been loaded by Hibernate or whatever. The important thing is that the CNF should be easy to use in situations where business objects are being retrieved in some way.

It's actually not so bad right now, but it could be easier. 
Currently you must subclass the CommonNavigator class and override the getInitialInput method to supply some root object that the business objects can attach to. The idea of using the page input as the root of the navigator doesn't work in this scenario and is confusing to new developers. It would be nice if you could just create a navigator extension and then just start adding any type of content provider to it. Perhaps there could be an attribute on the navigator extension point indicating whether the page input should be used as the root. If it should not, then some type of generic root node would need to be created for content providers to attach to. Contact: Patrick Paulin (patrick@rcpquickstart.com).


  

