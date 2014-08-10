# Frontend maven plugin with Tomcat integration

This plugin is based on [Eirik Sletteberg's frontend-maven-plugin](https://github.com/eirslett/frontend-maven-plugin). 
Use this plugin for integration tests against J2EE web applications when you need a fully deployed Servlet 
container to provide the backend of your application.

The plugin will start an embedded Tomcat 7 server and will deploy one or more WARs to the server. The
WARs can be configured via a "Context.xml" file, which allows you to specify the JNDI configuration and
to provide context-parameter if needed. These parameter will override any parameter given in the WAR
itself.

Use the original frontend-maven-plugin for all other Node related tasks, like downloading and installing
Node and NPM locally for your project, runs NPM install, Grunt and/or gulp and/or Karma.
It's supposed to work on Windows, OS X and Linux.

#### What is this plugin meant to do?
- Let you keep your frontend and backend builds as separate as possible, by
reducing the amount of interaction between them to the bare minimum; using only 1 plugin.
- Let you use Node.js and its libraries in your build process without installing Node/NPM
globally for your build system
- Let you ensure that the version of Node and NPM being run is the same in every build environment

#### What is this plugin not meant to do?
- Not meant to replace the developer version of Node - frontend developers will still install Node on their
laptops, but backend developers can run a clean build without even installing Node on their computer.
- Not meant to install Node for production uses. The Node usage is intended as part of a frontend build,
running common javascript tasks such as minification, obfuscation, compression, packaging, testing etc.

# Installing
Include the plugin as a dependency in your Maven project.

```xml
<plugins>
  <plugin>
    <groupId>com.rabbitstewdio.intrastructure</groupId>
    <artifactId>frontend-tomcat-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    </plugin>
</plugins>
```

# Usage
Have a look at the example project, to see how it should be set up!
https://github.com/eirslett/frontend-maven-plugin/blob/master/frontend-maven-plugin/src/it/example%20project/pom.xml

### Working directory
The default  The working directory is where you've put `package.json`, and either `Gruntfile.js` or `gulpfile.js`
etc. The default working directory is your project's base directory (the same directory as your `pom.xml`). You
can change the working directory if you want:

```xml
<plugin>
    <groupId>com.rabbitstewdio.intrastructure</groupId>
    <artifactId>frontend-tomcat-maven-plugin</artifactId>
    <version>...</version>

    <!-- optional -->
    <configuration>
        <workingDirectory>src/main/frontend</workingDirectory>
    </configuration>

    <executions>
      ...
    </executions>
</plugin>
```

### Configuring Tomcat
If you are building the WAR in your current project, all you need is to point Tomcat to your
WAR or the directory containing the WAR contents to start.

If your integration-tests are contained in a sub-module, you will need to publish the WAR as
part of your build and then download it to your project. You can download and deploy multiple
wars, which makes it possible to separate static content that is produced by your graphics
designer from the dynamic scripts produced by your development team.


```xml
  <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-dependency-plugin</artifactId>
      <version>2.8</version>
      <executions>
          <execution>
              <phase>process-test-resources</phase>
              <goals>
                  <goal>copy</goal>
              </goals>
              <configuration>
                  <outputDirectory>${project.build.directory}/dependency</outputDirectory>
                  <artifactItems>
                      <artifactItem>
                          <artifactId>assembly-war</artifactId>
                          <groupId>${project.groupId}</groupId>
                          <version>${project.version}</version>
                          <type>war</type>
                          <destFileName>assembly.war</destFileName>
                      </artifactItem>
                      <artifactItem>
                          <artifactId>content</artifactId>
                          <groupId>${project.groupId}</groupId>
                          <version>${project.version}</version>
                          <type>war</type>
                          <destFileName>content.war</destFileName>
                      </artifactItem>
                  </artifactItems>
              </configuration>
          </execution>
      </executions>
  </plugin>
```

Once you have either downloaded or built the WAR file or directory, you can configure
Tomcat:

```xml
    <configuration>
        <!-- Optional: 
          An implementation of an context-provider. This class will be called
          before and after the server is started. You can use this to provision
          databases or to perform preparation work.
          
          To implement a context-provider, implement the interface
           
          com.rabbitstewdio.build.maven.tomcat.ContextProvider
          
          on a class with a public default constructor.
         -->
        <contextProvider>com.mycompany.DatabaseSetupScript</contextProvider>
        <port>8080</port>
        <additionalContexts>
            <context>
                <!-- 
                  The context name under which the application will be available.
                  In this case it will be case: http://localhost:8080/assembly
                -->
                <contextRoot>assembly</contextRoot>
                
                <!-- 
                  The path to the WAR or directory of the web-application
                 -->
                <directory>${project.build.directory}/dependency/pentaho.war</directory>
                
                <!--
                  Optional:
                  Specify the path to a context.xml file. This file can be used to set up
                  JNDI the configuration. This file is Tomcat specific.
                 -->
                <contextXml>${project.basedir}/src/test/context/context.xml</contextXml>
                
                <!--
                  Optional:
                  Configure context properties. These properties will override any default
                  value set in the WAR's web-app.xml file
                 -->
                <properties>
                    <solution-path>${project.build.directory}/pentaho-solutions</solution-path>
                </properties>
            </context>
            <context>
                <contextRoot>style</contextRoot>
                <directory>${project.build.directory}/dependency/style.war</directory>
            </context>
        </additionalContexts>
        
        <!-- 
        -->
        <config>
            <docBase>${project.basedir}</docBase>
            <!-- 
              Defines whether a default mapping for static content exists at the root of the
              web-server. This mapping will serve all content from the directory configured in 
              the "docBase" property as well as content from any WebJar that is on the test-scope
              classpath. WebJars contain web-content in the META-INF/resources directory.
              -->
            <configureDefaultRoot>true</configureDefaultRoot>
        </config>
    </configuration>

```

The following properties are available to configure the built-in Tomcat server.

+ _configureDefaultRoot_ (Boolean)  
  Defines whether a default mapping for static content exists at the root of the web-server. 
  This mapping will serve all content from the directory configured in the "docBase" property as well as content from any 
  WebJar that is on the test-scope classpath. WebJars contain web-content in the META-INF/resources directory.
+ _docBase_ (String)  
  Defines the root directory of an automatically generated context. The automatic context can be enable via the
  `configureDefaultRoot` property.
+ _uriEncoding_ (String)  
  Specify URIEncoding for connector
+ _enableCompression_ (Boolean)  
   Enable GZIP compression on responses
+ _enableSSL_ (Boolean)  
  Enables SSL compression. This requires that the SSL certificates are available in a trust-store and that the
  TrustStore is correctly configured. Specify -Djavax.net.ssl.trustStore and -Djavax.net.ssl.trustStorePassword in JAVA_OPTS. 
  Note: should not be used if a reverse proxy is terminating SSL for you (such as on Heroku)
+ _enableNaming_ (Boolean)  
  Enables the JNDI subsystem. This parameter is implied if basic-auth is enabled.
+ _enableClientAuth_ (Boolean)  
  Verifies the client's identity as part of the SSL protocol handshake. 
  Specify -Djavax.net.ssl.keyStore and -Djavax.net.ssl.keyStorePassword in JAVA_OPTS
+ _enableBasicAuth_ (Boolean)  
  Secure the app with basic auth. Specify the user information via the `userDatabaseLocation` property. 
+ _userDatabaseLocation_ (String)  
  Location of the tomcat-users.xml file. Any relative path given will be interpreted as relative to the native 
  working directory, which may not be the project directory. The file format is described in the 
  [Tomcat Documentation](http://tomcat.apache.org/tomcat-7.0-doc/realm-howto.html#MemoryRealm).
+ _expandWar_ (Boolean)  
  Expand the war file and set it as source
+ _sessionTimeout_ (Int)
  The number of minutes of inactivity before a user's session is timed out.
+ _compressableMimeTypes_ (String)
  Comma delimited list of mime types that will be compressed when using GZIP compression.

### Running Grunt
It will run Grunt according to the `Gruntfile.js` in your working directory.
By default, no colors will be shown in the log.
```xml
<execution>
    <id>grunt build</id>
    <goals>
        <goal>grunt</goal>
    </goals>

    <!-- optional: the default phase is "generate-resources" -->
    <phase>generate-resources</phase>

    <configuration>
        <!-- optional: if not specified, it will run Grunt's default
        task (and you can remove this whole <configuration> section.) -->
        <arguments>build</arguments>
    </configuration>
</execution>
```

### Running gulp
Very similar to the Grunt execution. It will run gulp according to the `gulpfile.js` in your working directory.
By default, no colors will be shown in the log.
```xml
<execution>
    <id>gulp build</id>
    <goals>
        <goal>gulp</goal>
    </goals>

    <!-- optional: the default phase is "generate-resources" -->
    <phase>generate-resources</phase>

    <configuration>
        <!-- optional: if not specified, it will run gulp's default
        task (and you can remove this whole <configuration> section.) -->
        <arguments>build</arguments>
    </configuration>
</execution>
```

### Running Karma
```xml
<execution>
    <id>javascript tests</id>
    <goals>
        <goal>karma</goal>
    </goals>

    <!-- optional: the default plase is "test". Some developers
    choose to run karma in the "integration-test" phase. -->
    <phase>test</phase>

    <configuration>
        <!-- optional: the default is "karma.conf.js" in your working directory -->
        <karmaConfPath>src/test/javascript/karma.conf.ci.js</karmaConfPath>
    </configuration>
</execution>
```
__Skipping tests:__ If you run maven with the `-DskipTests` flag, karma tests will be skipped.

__Ignoring failed tests:__ If you want to ignore test failures run maven with the
`-Dmaven-frontend-plugin.testFailureIgnore` flag, karma test results will not stop the build but test
results will remain in test output files. Suitable for continuous integration tool builds.

__Why karma.conf.ci.js?__ When using Karma, you should have two separate
configurations: `karma.conf.js` and `karma.conf.ci.js`. (The second one should inherit configuration
from the first one, and override some options. The example project shows you how to set it up.)
The idea is that you use `karma.conf.js` while developing (using watch/livereload etc.), and
`karma.conf.ci.js` when building - for example, when building, it should only run karma once,
it should generate xml reports, it should run only in PhantomJS, and/or it should generate
code coverage reports.

__Running Karma through Grunt or gulp:__ You may choose to run Karma [directly through Grunt](https://github.com/karma-runner/grunt-karma)
or [through gulp](https://github.com/lazd/gulp-karma) instead, as part of the `grunt` or `gulp`
execution. That will help to separate your frontend and backend builds even more.


# Helper scripts
During development, it's convenient to have the "npm", "grunt", "gulp" and "karma" commands
available on the command line. If you want that, use
[those helper scripts](https://github.com/eirslett/frontend-maven-plugin/tree/master/frontend-maven-plugin/src/it/example%20project/helper-scripts)!

## To build this project:
`mvn clean install`

## Issues, Contributing
Please post any issues on the Github's Issue tracker. Pull requests are welcome!

### License
Apache 2.0
