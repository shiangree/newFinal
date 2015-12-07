package hudson.plugins.logparser;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.lang.Integer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import hudson.model.Action;
import hudson.model.Run;
import hudson.model.AbstractProject;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Job;

/**
 * DependencyDiffAction is the action to diff dependencies in POM.xml between
 * two builds.
 */
public class DependencyDiffAction implements Action {
    private Run<?,?> thisBuild;
    private String thatBuild;
    public String html;
    private final Run<?, ?> owner;
    public String fileName;
    public DependencyDiffAction(Job<?, ?> job, int build1, int build2, Launcher launcher,
            FilePath workspace) throws Exception {
this.owner = job.getBuildByNumber(build1);
        Map<String, List<String>> pomcontent1 = SCMUtils.getFilesFromBuild("pom.xml", (AbstractProject<?, ?>) job, build1, launcher, workspace);
    	Map<String, List<String>> pomcontent2 = SCMUtils.getFilesFromBuild("pom.xml", (AbstractProject<?, ?>) job, build2, launcher, workspace);
	this.html = "";
	
        String configPath = job.getConfigFile().getFile().getAbsolutePath();
        BufferedReader br = new BufferedReader(new FileReader(configPath));
	String line;
	String pomPath="null";
	while((line = br.readLine()) != null)
	{
		if(line.contains("pom.xml"))
		{
			pomPath =line.substring(line.indexOf("<rootPOM>")+9,line.indexOf("</rootPOM>")).trim();
			break;
		}
	} 
	List<String> contentlist1 = pomcontent1.get(pomPath);
	List<String> contentlist2 = pomcontent2.get(pomPath);

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
	br.close();
	this.html+=DependencyDiffUtils.toHtml(deplist1, deplist2, DependencyDiffUtils.diff(deplist1, deplist2));
   	this.fileName = "dependency_diff.html";
    
    }

    public Run<?, ?> getOwner() {
        return this.owner;
    }

    public String getPrevBuild() {
        return this.thatBuild;
    }

    public String getHtml() {
        return this.html;
    }

    @JavaScriptMethod
    public String exportHtml() {
    	return this.html;
    }

    @JavaScriptMethod
    public String exportFileName() {
	return this.fileName;
    }
    @Override
    public String getIconFileName() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "DependencyDiff Page";
    }

    @Override
    public String getUrlName() {
        return "dependencyDiff";
    }
}
