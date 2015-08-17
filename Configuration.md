# Firts step #

The clas GestorConfiguracion handles all configuration logic of your application, and thror the clas Configuracion allows you to lookup for any property.

# Details #

Just define a bean for the class GestorConfiguracion passing the rights arguments to it´s constructor. For more details of every property just see the JavaDoc.

An example is:



&lt;bean id="gestorConfiguracion" class="es.onlysolutions.arq.core.configuration.GestorConfiguracion"&gt;


> <!--DataSource to access the configuration table-->
> 

&lt;constructor-arg ref="dataSource"/&gt;


> > <!--Table name-->

> 

&lt;constructor-arg value="t\_configuration" /&gt;


> > <!--Column for the property name-->

> 

&lt;constructor-arg  value="propertyname" /&gt;


> > <!--Column value -->

> 

&lt;constructor-arg value="value" /&gt;


> > <!--Property file-->

> 

&lt;constructor-arg value="/myproperties.cfg" /&gt;


> > <!--Alternative log4j file-->

> 

&lt;constructor-arg value="/loggingLog4j.cfg" /&gt;


> > <!--App name-->

> 

&lt;constructor-arg value="MyAppName" /&gt;




&lt;/bean&gt;



With this configuration you tells the GestorConfiguracion that reads all properties from the database table "t\_configuration", and if the property is not found, will look at the properties file "myproperties.cfg", at the root of the classpath.

**All the properties are cached once they are read one time. You can clear all of them on the fly with the JMX bean**

## JMX ##

JArq provides a class called with can be registered as a normal MBean in the IoC container (see springframework documentation, JMX for more information).
This MBean has only one public operation, that clear all the properties cached, so they are re-read again when some code ask for any.