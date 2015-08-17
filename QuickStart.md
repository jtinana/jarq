#This section will giev you a brief description of how to quickly deploy a new app with JArq.

# Firts steps #

Just download the latest version of JArq in a blank application from http://jarq.es/downloads/blank.zip.

Then unzip it in some place of your hard disk and point your IDE to the root directory.
Then add the root _build.xml_ as an Ant build file and just start to coding you app.


# Details #

Just pay attention to some details to easily coding your webapp.

  * All your controllers should extends the GenericController class unless you know what are you doing.

  * It´s recommended that you follow the patterns that JArq gives you at the starting point. Naming all facade`s method with clarify names and starting the read-only ones with **get** prefix (also in the DAO's ones). This will make it easier you work to introduce later some AOP funcionallity, like the Memcache Advice.

  * Define your facade interfaces in different packages from the implementations. The txt AOP and others and so much efficient if they only needs to envolve the interfaces.


This is only a initial guide to people who are very familiar with Spring, Hibernate and other projects in wich JArq is based.
In this wiki you will find pages for every module of JAr, and how to configure it in detail.