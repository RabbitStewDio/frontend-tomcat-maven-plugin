package com.rabbitstewdio.infrastructure.frontend;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.TaskRunnerException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "gulp", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public final class GulpTomcatMojo extends TomcatSupportMojoBase {

  /**
   * The base directory for running all Node commands. (Usually the directory that contains package.json)
   */
  @Parameter(defaultValue = "${basedir}", property = "workingDirectory", required = false)
  private File workingDirectory;

  /**
   * Gulp arguments. Default is empty (runs just the "gulp" command).
   */
  @Parameter(property = "arguments")
  private String arguments;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      preExecution();

      MojoUtils.setSLF4jLogger(getLog());
      new FrontendPluginFactory(workingDirectory).getGulpRunner()
              .execute(arguments);
    } catch (TaskRunnerException e) {
      throw new MojoFailureException("Failed to run task", e);
    }
    finally {
      postExecution();
    }
  }
}