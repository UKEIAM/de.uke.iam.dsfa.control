# DSFA Controller

## Installation
#### 1. PostgreSQL Contrib Module
Before deploying this module, you need to enable postgres UUID support.
As this is a contrib module, it may not be installed on the server by default.

The package to install is called:
- in Ubuntu 18.04 LTS: postgresql-contrib
- in CentOS7: postgresql10-contrib (for PostgreSQL 10, for other versions change the 10 in the name)

Install the package with either ```apt install postgresql-contrib``` or ```yum install postgresql10-contrib```

#### 2. Configuration file
Provide a ```dsfa.control.config.xml``` configuration file with the necessary details as shown in  ```/resources/dsfa.control.config.xml.example```.
Put the file either in resources or in one of the directories explained below.

##### On Linux and Mac:
Store the file in the directory ```/etc/dsfa/``` or in ```/usr/local/share/dsfa/```

##### On Windows:
###### As service
Provide the path of the directory in which the config file is located with the Windows
registration entry ```HKLM\SOFTWARE\DSFA\ConfDir``` or ```HKLM\SOFTWARE\DSFA\ConfDistDir```

###### As normal user
If you're only deploying as a Windows user, you can also create the subdirectories ```C:\Users\USERNAME\.config\dsfa\```
Replace USERNAME with the username and use the appropriate drive if you installed on other than C:


#### 3. Deploy DSFA-Control
Now you can deploy the controller in your tomcat-webapp directory.
### To build for development

#### Database Configuration
Enable a maven profile overwriting the database-connection values
```
  <properties>
    <dsfa.database.url>jdbc:postgresql://servername:serverhost/databasename</dsfa.database.url>
	<dsfa.database.schema>public</dsfa.database.schema>
	<dsfa.database.user>username</dsfa.database.user>
	<dsfa.database.password>password</dsfa.database.password>
  </properties>
```
Then generate the database with FlyWay:
```mvn flyway:migrate```

#### DSFA Control Configuration
Build the necessary java model classes with:
```mvn jaxb2:generate```

Now create a dsfa.control.config.xml file from the provided example file and update the content.
This file can be stored in different locations. Check documentation of Samply-Common-Config.

#### JOOQ Model Classes
Finally, build the jooq model with: ``` mvn jooq-codegen:generate ``