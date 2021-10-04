import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;
import hudson.tools.*

def inst = Jenkins.getInstance()
def desc = inst.getDescriptor("hudson.tasks.Maven")
def minst = new hudson.tasks.Maven.MavenInstallation("Maven", "/usr/bin/mvn");
desc.setInstallations(minst)
desc.save()