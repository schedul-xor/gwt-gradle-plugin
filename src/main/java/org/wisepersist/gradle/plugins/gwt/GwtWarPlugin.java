/**
 * Copyright (C) 2013-2017 Steffen Schaefer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wisepersist.gradle.plugins.gwt;

import java.io.File;
import java.util.concurrent.Callable;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.War;

public class GwtWarPlugin implements Plugin<Project> {

  public static final String TASK_WAR_TEMPLATE = "warTemplate";
  public static final String TASK_DRAFT_WAR = "draftWar";
  public static final String TASK_GWT_DEV = "gwtDev";
  public static final String TASK_GWT_SUPER_DEV = "gwtSuperDev";

  private static final Logger logger = Logging.getLogger(GwtWarPlugin.class);

  @Override
  public void apply(final Project project) {
    project.getPlugins().apply(WarPlugin.class);
    final GwtBasePlugin gwtBasePlugin = project.getPlugins().apply(
        GwtBasePlugin.class);

    final GwtPluginExtension extension = gwtBasePlugin.getExtension();

    final GwtCompile compileTask = (GwtCompile) project.getTasks()
        .getByName(GwtCompilerPlugin.TASK_COMPILE_GWT);

    final GwtDraftCompile draftCompileTask = (GwtDraftCompile) project
        .getTasks().getByName(GwtCompilerPlugin.TASK_DRAFT_COMPILE_GWT);

    final War warTask = (War) project.getTasks().getByName(
        WarPlugin.WAR_TASK_NAME);

    logger.debug("Configuring war plugin with GWT settings");

    project.afterEvaluate(p -> {
      String modulePathPrefix = extension.getModulePathPrefix();
      if (modulePathPrefix == null || modulePathPrefix.isEmpty()) {
        warTask.from(compileTask.getWar());
        return;
      }

      warTask.into(modulePathPrefix == null ? "" : modulePathPrefix, spec -> {
        spec.from(compileTask.getWar());
        spec.exclude("WEB-INF");
      });
      warTask.into("", spec -> {
        spec.from(compileTask.getWar());
        spec.include("WEB-INF");
      });
    });

    final WarPluginConvention warPluginConvention = (WarPluginConvention) project
        .getConvention().getPlugins().get("war");

    final ExplodedWar warTemplateTask = project.getTasks().create(
        TASK_WAR_TEMPLATE, ExplodedWar.class);
    warTemplateTask.setGroup(GwtBasePlugin.GWT_TASK_GROUP);
    warTemplateTask.from(
        (Callable<File>) () -> warPluginConvention.getWebAppDir());
    warTemplateTask.dependsOn(
        (Callable<FileCollection>) () -> project.getConvention()
            .getPlugin(JavaPluginConvention.class).getSourceSets().getByName(
                SourceSet.MAIN_SOURCE_SET_NAME).getRuntimeClasspath());
    warTemplateTask.classpath(new Object[]{
        (Callable<FileCollection>) () -> warTask.getClasspath()});
    ((IConventionAware) warTemplateTask).getConventionMapping()
        .map("destinationDir", (Callable<File>) () -> extension.getDevWar());
    warTemplateTask
        .setDescription(
            "Creates an exploded web application template to be used by GWT dev mode and eclipse to ensure src/main/webapp stays clean");

    final GwtDev devModeTask = project.getTasks().create(TASK_GWT_DEV,
        GwtDev.class);
    devModeTask.setDescription("Runs the GWT development mode");
    ((IConventionAware) devModeTask).getConventionMapping()
        .map("war", (Callable<File>) () -> extension.getDevWar());

    final War draftWar = project.getTasks().create(TASK_DRAFT_WAR,
        War.class);
    draftWar.from(draftCompileTask.getOutputs());

    String appendix = "draft";
    draftWar.getArchiveAppendix().convention(appendix);
    draftWar.getArchiveAppendix().set(appendix);
    draftWar.setDescription("Creates a war using the output of the task "
        + GwtCompilerPlugin.TASK_DRAFT_COMPILE_GWT);

    devModeTask.dependsOn(JavaPlugin.CLASSES_TASK_NAME);
    devModeTask.dependsOn(warTemplateTask);

  }

}
