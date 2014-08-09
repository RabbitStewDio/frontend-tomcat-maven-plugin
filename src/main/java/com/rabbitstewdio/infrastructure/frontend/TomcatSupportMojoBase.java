package com.rabbitstewdio.infrastructure.frontend;

import com.rabbitstewdio.build.maven.tomcat.Context;
import com.rabbitstewdio.build.maven.tomcat.ProjectClassLoaderFactory;
import com.rabbitstewdio.build.maven.tomcat.ServerManager;
import com.rabbitstewdio.build.maven.tomcat.TomcatConfiguration;
import com.rabbitstewdio.build.maven.tomcat.TomcatConfigurator;
import com.rabbitstewdio.build.maven.tomcat.TomcatServerManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public abstract class TomcatSupportMojoBase extends AbstractMojo implements TomcatConfiguration {

  private ServerManager serverManager;

  @Parameter
  protected String contextProvider;

  @Parameter(defaultValue = "${project}", readonly = true)
  protected MavenProject mavenProject;

  @Parameter
  protected Properties config = new Properties();

  /**
   * <p>Used by the <code>jasmine:bdd</code> goal to specify port to run the server under.</p>
   * <p/>
   * <p>The <code>jasmine:test</code> goal always uses a random available port so this property is ignored.</p>
   *
   * @since 1.1.0
   */
  @Parameter(property = "tomcat.serverPort", defaultValue = "8234")
  protected int serverPort;

  /**
   * <p>Specify additional contexts to make available.</p>
   * <pre>
   * &lt;additionalContexts&gt;
   *   &lt;context&gt;
   *     &lt;contextRoot&gt;lib&lt;/contextRoot&gt;
   *     &lt;directory&gt;${project.basedir}/src/main/lib&lt;/directory&gt;
   *   &lt;/context&gt;
   *   &lt;context&gt;
   *     &lt;contextRoot&gt;test/lib&lt;/contextRoot&gt;
   *     &lt;directory&gt;${project.basedir}/src/test/lib&lt;/directory&gt;
   *   &lt;/context&gt;
   * &lt;/additionalContexts&gt;
   * </pre>
   *
   * @since 1.3.1.5
   */
  @Parameter
  private List<Context> additionalContexts = Collections.emptyList();

  public TomcatSupportMojoBase() {
  }


  @Override
  public List<Context> getContexts() {
    List<Context> contexts = new ArrayList<Context>();
    contexts.addAll(additionalContexts);
    return contexts;
  }

  @Override
  public File getBasedir() {
    return this.mavenProject.getBasedir();
  }

  @Override
  public ClassLoader getProjectClassLoader() {
    return new ProjectClassLoaderFactory(mavenProject.getArtifacts()).create();
  }

  public int getServerPort() {
    return serverPort;
  }

  public String getContextProvider() {
    return contextProvider;
  }

  public MavenProject getProject() {
    return mavenProject;
  }

  @Override
  public Properties getConfiguration() {
    return config;
  }

  protected void preExecution() throws MojoFailureException {
    try {
      serverManager = this.createServerManager();
      serverManager.start(getServerPort());
    } catch (Exception e) {
      throw new MojoFailureException("Failed to launch Tomcat server", e);
    }
  }

  protected void postExecution() {
    if (serverManager != null) {
      try {
        serverManager.stop();
      } catch (Exception e) {
        getLog().warn("Failed to stop Tomcat server", e);
      }
    }
  }
  protected ServerManager createServerManager() {
    TomcatConfigurator configurator = new TomcatConfigurator(this, getLog());
    return new TomcatServerManager(configurator);
  }

}
