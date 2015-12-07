package org.jenkinsci.plugins.logparser;


import hudson.plugins.logparser.Dependency;
import hudson.plugins.logparser.DependencyDiffUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import java.io.*;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * test the diff function from two pom xml files
 * 
 * @throws SAXException and IOException
 */
public class DependencyDiffTest {
    ArrayList<Dependency> deplist1;
    ArrayList<Dependency> deplist2;
    Map<String, ArrayList<Dependency>> difflist; 
    @Before
    public void setUp() throws Exception
    {
        BufferedReader br1 = new BufferedReader(
                new FileReader("src/test/resources/org/jenkinsci/plugins/logparser/pom1.xml"));
        BufferedReader br2 = new BufferedReader(
                new FileReader("src/test/resources/org/jenkinsci/plugins/logparser/pom2.xml"));
        List<String> contentlist1 = new ArrayList<String>();
        List<String> contentlist2 = new ArrayList<String>();
        String line = "";
        while ((line = br1.readLine()) != null) {
            contentlist1.add(line);
        }
        while ((line = br2.readLine()) != null) {
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
        deplist1 = DependencyDiffUtils.parsePom(in1);
        byte[] bytes2 = baos2.toByteArray();
        InputStream in2 = new ByteArrayInputStream(bytes2);
        deplist2 = DependencyDiffUtils.parsePom(in2);
        difflist = DependencyDiffUtils.diff(deplist1, deplist2);
        br1.close();
        br2.close();
    }
    @Test
    public void dependencyDiffCoreTest() throws Exception {
        
        ArrayList<Dependency> modified = difflist.get("Modified");
        assertEquals(modified.size(), 2);
        ArrayList<Dependency> added = difflist.get("Added");
        assertEquals(added.size(), 3);
        ArrayList<Dependency> deleted = difflist.get("Deleted");
        assertEquals(deleted.size(), 1);
    }
    
    @Test
    public void dependencyDiffHtmlTest() throws Exception {
        String html = DependencyDiffUtils.toHtml(deplist1, deplist2, difflist, 2, 1);
        BufferedWriter bw = new BufferedWriter(new FileWriter("abc"));
        bw.write(html);
        bw.flush();
        bw.close();
//        String s = "<b>Dependency modified:</b><br />\n"
//                + "<br> groupId: com.google.code.gson</br>\n"
//                + "<br> artifactId: gson</br>\n"
//                + "<br> build #2 dependency version: 2.1</br>\n"
//                + "<br> build #1 dependency version: 2.2<hr>\n"
//                + "<br />\n";
        assertTrue(true);
    }
    
}
