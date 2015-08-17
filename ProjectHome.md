# Welcome to JArq, a J2EE software development framework #

**_Initially, this site will provide you all the English documentation, but you can visit our Spanish site just pointing your browser to http://jarq-req-tool.appspot.com_**

This projects will try to give you an entire scaffold for your web applications, but, it will not try to avoid the code development, just reduce it to only the necessary.
This project is the evolution of many years of professional development and the generalization of the most commons problems is every applications and some more sophisticated that have been found interesting through all the projects were JArq was incubated.

Some of the features of JArq are the following ones:

## Autentication ##
Every petition that was made to the server will be intercep by our controller and check for a valid user. A configurable page is allow of course, but as many other autentication frameworks doesn´t have, just can add to the session objet as many information an objects you want. JArq will handle this objects automatically.

## Autorisation ##
Every facade´s method can be autorized throw a simple configuration file (we are working on annotation feature that will be ready soon). JArq will check in every execution if the user logged is autorized to do this bussines operation.

## Automatically listing ##
For every Hibernate entity that implements a simple interface, you can automatically list it with a paginated list in our DisplayTag only with a little configuration in the IoC container.

## Automatically updates/creates/deletes ##
Like the paginated lists, for every update, create or delete page you need you can implement them all in one just with a XML definition in the IoC.

## JASON RPC calls ##
JArq provides a easy interface to do AJAX calls through Jasorb project, only with a simple configuracion of a bridge an defining the method you want to publish for every facade.

## Compile and package ##
The simple the best. In the initial zip of the application you will find an Ant script to deploy you entire app if you follow the initial schema of directories. We don´t want to complicate it so much so its very easy the IDE configuration, of course, you can integrate it easily with Maven of your own Ant scripts by your own.

## Excel module ##
Nowadays so many applications needs to generate Excels reports or just put in a workbook some information. The problem is that this task gets complicated when you have to introduce formulas and complicated date. This JArq module mades you life easy, as automatically recalculates formulas if some other data moves, and works easy with regions. That´s will make the maintain of the code so much easy and clear.

## Configuration ##
JArq underlying configuration is handle by commons-configuration. But we will provide you a cached configuration from properties and a database table (with have preference to fetch properties) and a JMX bean to control the load of propeties on the fly. All prepared to large load of petitions and clusters configurations.

## Memcache Advice ##
This component could be used standalone, as it doesn't have any dependecies with the Core.
This aspect allows you to increase the performance of your application using the memcache
daemon transparently. Just configure the package, or classes you want to be cached (should be at DAO level) and the advice will use an existing daemon of Memcache to store it and intercept it.

**JArq doesn´t try to reinvent the wheel. It´s just a tier over Spring, Hibernate, commons, and some other Open Source projects. But it doesn't close any functionality, so you just use it, and if you don´t like some feature, just implement it by your own or ignore it!**