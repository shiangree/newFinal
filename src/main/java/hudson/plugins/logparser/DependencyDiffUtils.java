package hudson.plugins.logparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import com.google.gson.Gson;
import org.apache.maven.artifact.Artifact;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import jenkins.model.ArtifactManager;
import javax.xml.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import hudson.XmlFile;

public class DependencyDiffUtils {
    public static Map<String, ArrayList<Dependency>> diff(ArrayList<Dependency> deplist1,
            ArrayList<Dependency> deplist2) throws ParserConfigurationException, SAXException, IOException {
        ArrayList<Dependency> dellist = new ArrayList<Dependency>();
        ArrayList<Dependency> addlist = new ArrayList<Dependency>();
        ArrayList<Dependency> modlist = new ArrayList<Dependency>();
        Map<String, ArrayList<Dependency>> retlist = new HashMap<String, ArrayList<Dependency>>();
        for (int i = 0; i < deplist2.size(); ++i) {
            Dependency dep2 = deplist2.get(i);
            String gid2 = dep2.getGroupId();
            String aid2 = dep2.getArtifactId();
            String ver2 = dep2.getVersion();
            boolean isinlist1 = false;
            for (Dependency dep1 : deplist1) {
                String gid1 = dep1.getGroupId();
                String aid1 = dep1.getArtifactId();
                String ver1 = dep1.getVersion();
                if (gid1.equals(gid2)) {
                    isinlist1 = true;
                    if (!aid1.equals(aid2) || !ver1.equals(ver2))
                        modlist.add(new Dependency(gid2, aid2, ver2));
                }
            }
            if (!isinlist1)
                addlist.add(new Dependency(gid2, aid2, ver2));
        }
        retlist.put("Modified", modlist);
        retlist.put("Added", addlist);
        for (int i = 0; i < deplist1.size(); ++i) {
            Dependency dep1 = deplist1.get(i);
            String gid1 = dep1.getGroupId();
            String aid1 = dep1.getArtifactId();
            String ver1 = dep1.getVersion();
            boolean isinlist2 = false;
            for (Dependency dep2 : deplist2) {
                String gid2 = dep2.getGroupId();
                if (gid2.equals(gid1)) {
                    isinlist2 = true;
                }
            }
            if (!isinlist2)
                dellist.add(new Dependency(gid1, aid1, ver1));

        }
        retlist.put("Deleted", dellist);
        return retlist;
    }

    public static StringBuilder setupCSS() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("<title></title>\n");
        sb.append("<style type=\"text/css\">\n");
        sb.append(".left { width: 30%; float: left; clear: right; background-color: #BCF5A9; }\n");
        sb.append(".center { width: 30%; float: left; clear: right; background-color: #81DAF5; }\n");
        sb.append(".right { width: 30%; float: left; clear: right; background-color: #FA5858; }\n");
        sb.append(".both { width: 100%; clear: both; background-color: #696969; }\n");
        sb.append("</style>\n");
        sb.append("</head>\n");
        return sb;
    }

    private static String getPrevVersion(ArrayList<Dependency> deplist, String groupId) {
        for (Dependency dep : deplist) {
            if (dep.getGroupId().equals(groupId))
                return dep.getVersion();
        }
        return "0";
    }

    public static String toHtml(ArrayList<Dependency> deplist1, ArrayList<Dependency> deplist2,
            Map<String, ArrayList<Dependency>> list, Run<?, ?> build1, Run<?, ?> build2) {
        ArrayList<Dependency> modified = list.get("Modified");
        ArrayList<Dependency> added = list.get("Added");
        ArrayList<Dependency> deleted = list.get("Deleted");
        StringBuilder html = setupCSS();

        html.append("<body>\n");
        html.append("<div>\n");
        html.append("<div><p><b><font size=\"3\">Comparing the current build #" + build2.getNumber() + " and build #"
                + build1.getNumber() + "</font></b></p>");
        html.append("<div class=\"left\">\n");
        html.append("<b>Dependency modified:</b><br />\n");
        for (Dependency dep : modified) {
            html.append("<br> groupId: " + dep.getGroupId() + "</br>\n");
            html.append("<br> artifactId: " + dep.getArtifactId() + "</br>\n");
            html.append("<br> build #" + build2.getNumber() + " dependency version: " + dep.getVersion() + "</br>\n");
            html.append("<br> build #" + build1.getNumber() + " dependency version: "
                    + getPrevVersion(deplist1, dep.getGroupId()));
            html.append("<hr>\n<br />\n");
        }
        html.append("</div>\n");
        html.append("<div class=\"center\">\n");
        html.append("<b>Dependency added:</b><br />\n");
        for (Dependency dep : added) {
            html.append("<br> groupId: " + dep.getGroupId() + "</br>\n");
            html.append("<br> artifactId: " + dep.getArtifactId() + "</br>\n");
            html.append("<br> version: " + dep.getVersion() + "</br>\n");
            html.append("<hr>\n<br />\n");
        }
        html.append("</div>\n");
        html.append("<div class=\"right\">\n");
        html.append("<b>Dependency deleted:</b><br />\n");
        for (Dependency dep : deleted) {
            html.append("<br> groupId: " + dep.getGroupId() + "</br>\n");
            html.append("<br> artifactId: " + dep.getArtifactId() + "</br>\n");
            html.append("<br> version: " + dep.getVersion() + "</br>\n");
            html.append("<hr>\n<br />\n");
        }
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>");
        return html.toString();
    }

    public static ArrayList<Dependency> parsePom(InputStream dir)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
        Document doc = null;
        doc = dbBuilder.parse(dir);
        NodeList list = doc.getElementsByTagName("dependency");
        ArrayList<Dependency> alist = new ArrayList<Dependency>();
        System.out.println(list.getLength());
        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);
            String groupId = element.getElementsByTagName("groupId").item(0).getFirstChild().getNodeValue();
            String artifactId = element.getElementsByTagName("artifactId").item(0).getFirstChild().getNodeValue();
            String version = element.getElementsByTagName("version").item(0).getFirstChild().getNodeValue();
            alist.add(new Dependency(groupId, artifactId, version));

        }
        return alist;
    }
}
