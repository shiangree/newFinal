package org.jenkinsci.plugins.logparser;

import hudson.FilePath;
import hudson.plugins.logparser.Dependency;
import hudson.plugins.logparser.LogParserAction;
import hudson.plugins.logparser.LogParserPublisher;
import hudson.plugins.logparser.LogParserResult;
import hudson.plugins.logparser.DependencyDiffUtils;
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
import java.util.List;
/**
 * In this test suite we initialize the Job workspaces with a resource (maven-project1.zip) that contains a Maven
 * project.
 */
public class PomDiffTest {

    @Test
    public void Pomdifftest() throws Exception {
        BufferedReader br1 = new BufferedReader(new FileReader("src/test/resources/org/jenkinsci/plugins/logparser/pom1.xml"));
	BufferedReader br2 = new BufferedReader(new FileReader("src/test/resources/org/jenkinsci/plugins/logparser/pom2.xml"));
	List<String> contentlist1 = new ArrayList<String>();
	List<String> contentlist2 = new ArrayList<String>();
	String line="";
	while((line = br1.readLine())!=null)
	{
		contentlist1.add(line);
	}
	while((line = br2.readLine())!=null)
	{
		contentlist2.add(line);
	}
	
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        for (String s : contentlist1) {
                baos1.write(s.getBytes());
        }
        for (String s : contentlist2) {
                baos2.write(s.getBytes());
        }


        byte[] bytes1 = baos1.toByteArray();
        InputStream in1 = new ByteArrayInputStream(bytes1);
        ArrayList<Dependency> deplist1 = DependencyDiffUtils.parsePom(in1);
        byte[] bytes2 = baos2.toByteArray();
        InputStream in2 = new ByteArrayInputStream(bytes2);
        ArrayList<Dependency> deplist2 = DependencyDiffUtils.parsePom(in2);
        Map<String, ArrayList<Dependency>> difflist= DependencyDiffUtils.diff(deplist1, deplist2);
        ArrayList<Dependency> modified = difflist.get("Modified");
        assertEquals(modified.size(),0);
        ArrayList<Dependency> added = difflist.get("Added");
        assertEquals(added.size(),1);
        ArrayList<Dependency> deleted = difflist.get("Deleted");
        assertEquals(deleted.size(),0);   
    }
}
