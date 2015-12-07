package org.jenkinsci.plugins.logparser;

import hudson.FilePath;
import hudson.plugins.logparser.Dependency;
import hudson.plugins.logparser.LogParserAction;
import hudson.plugins.logparser.LogParserPublisher;
import hudson.plugins.logparser.LogParserResult;
import hudson.plugins.logparser.RootPomDiff;
import hudson.tasks.Maven;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.google.gson.Gson;

import java.io.*;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * In this test suite we initialize the Job workspaces with a resource (maven-project1.zip) that contains a Maven
 * project.
 */
public class PomDiffTest {

    @Test
    public void Pomdifftest() throws Exception {
        ArrayList<Dependency> deplist1 = RootPomDiff.parsePom("src/test/resources/org/jenkinsci/plugins/logparser/pom1.xml");
        ArrayList<Dependency> deplist2 = RootPomDiff.parsePom("src/test/resources/org/jenkinsci/plugins/logparser/pom2.xml");
        Map<String, ArrayList<Dependency>> difflist= RootPomDiff.diff(deplist1, deplist2);
        ArrayList<Dependency> modified = difflist.get("Modified");
        assertEquals(modified.size(),0);
        ArrayList<Dependency> added = difflist.get("Added");
        assertEquals(added.size(),1);
        ArrayList<Dependency> deleted = difflist.get("Deleted");
        assertEquals(deleted.size(),0);   
    }
}
