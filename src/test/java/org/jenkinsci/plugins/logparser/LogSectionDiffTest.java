
package org.jenkinsci.plugins.logparser;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.FilePath;
import hudson.model.Run;
import hudson.plugins.logparser.LogSectionDiffWorker;

/**
 * * In this test suite we initialize the Job workspaces with a resource
 * (maven-project1.zip) that contains a Maven * project.
 */
public class LogSectionDiffTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private WorkflowJob job;
    private Run<?, ?> build1, build2;

    // private static Maven.MavenInstallation mavenInstallation;
    // @BeforeClass
    // public static void init() throws Exception {
    // mavenInstallation = jenkinsRule.configureMaven3();
    // }

    @Before
    public void setUp() throws Exception {
        job = jenkinsRule.jenkins.createProject(WorkflowJob.class,
                "logParserPublisherWorkflowStep");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        workspace.unzipFrom(getClass().getResourceAsStream("./maven-project1.zip"));
        job.setDefinition(
                new CpsFlowDefinition("" + "node {\n" + "  sh \"/usr/bin/mvn clean install\"\n"
                        + "  step([$class: 'LogParserPublisher', projectRulePath: 'logparser-rules.txt', useProjectRule: true])\n"
                        + "}\n", true));

        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        
        build1 = job.getBuildByNumber(1);
        build2 = job.getBuildByNumber(2);
    }


    /**
     * test log section diff functionality
     */
    @Test
    public void testLogSectionDiff2() throws Exception {
        File build1File = build1.getLogFile();
        Files.write(build1File.toPath(), "[INFO] aaa".getBytes(), StandardOpenOption.APPEND);
        
        LogSectionDiffWorker lsdw = new LogSectionDiffWorker(build1, build2);
        String diffResult = lsdw.writeSectionDiffToHTMLs();
        assertTrue(diffResult.contains("[INFO] aaa"));
    }
    
    /**
     * test log section diff functionality
     */
    @Test
    public void testLogSectionDiff3() throws Exception {
        File build1File = build1.getLogFile();
        Files.write(build1File.toPath(), "[WARNING] bbb".getBytes(), StandardOpenOption.APPEND);
        
        LogSectionDiffWorker lsdw = new LogSectionDiffWorker(build1, build2);
        String diffResult = lsdw.writeSectionDiffToHTMLs();
        assertTrue(diffResult.contains("[WARNING] bbb"));
    }
    
    /**
     * test log section diff functionality
     */
    @Test
    public void testLogSectionDiff4() throws Exception {
        File build2File = build2.getLogFile();
        Files.write(build2File.toPath(), "[ERROR] ccc".getBytes(), StandardOpenOption.APPEND);
        
        LogSectionDiffWorker lsdw = new LogSectionDiffWorker(build1, build2);
        String diffResult = lsdw.writeSectionDiffToHTMLs();
        assertTrue(diffResult.contains("[ERROR] ccc"));
    }
}
