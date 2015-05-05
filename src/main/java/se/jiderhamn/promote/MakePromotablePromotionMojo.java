package se.jiderhamn.promote;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 */
@Mojo(name = MakePromotablePromotionMojo.NAME, requiresProject = true /* TODO , defaultPhase = LifecyclePhase.PROCESS_SOURCES */ )
public class MakePromotablePromotionMojo extends AbstractMojo {

  public static final String NAME = "make-promotable";

  /** The maven project */
  @Parameter(property = "project")
  private MavenProject project;

  public void execute() throws MojoExecutionException {
    final URI target = PromoteUtils.getTargetURI(project);

    Properties artifactInfo = new Properties();

    Artifact artifact = project.getArtifact();
    if(artifact != null) {
      getLog().info("Artifact: " + artifact.getId());
      Map<String, String> artifactProperties = PromoteUtils.toMap(artifact, "artifact", target);
      getLog().debug("Artifact properties: " + artifactProperties);
      artifactInfo.putAll(artifactProperties);
    }
    else
      getLog().debug("No main artifact found");

    List<Artifact> attachedArtifacts = project.getAttachedArtifacts();
    if(! attachedArtifacts.isEmpty()) {
      for(int i = 0; i < attachedArtifacts.size(); i++) {
        Artifact attachedArtifact = attachedArtifacts.get(0);
        getLog().info("Attached artifact: " + attachedArtifact.getId());
        Map<String, String> artifactProperties = PromoteUtils.toMap(attachedArtifact, "attached." + i, target);
        getLog().debug("Attached artifact properties: " + artifactProperties);
        artifactInfo.putAll(artifactProperties);
      }
    }

    try {
      File file = PromoteUtils.getPromotePropertiesFile(project);
      getLog().info("Writing artifact information to " + file);
      artifactInfo.store(new FileOutputStream(file), "Generated by promote-maven-plugin");
    }
    catch (IOException e) {
      throw new MojoExecutionException("Failure writing artifacts to file", e); // TODO MojoFailureException ?
    }

  }

}
