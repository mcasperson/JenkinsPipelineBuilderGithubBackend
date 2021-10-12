import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;
import hudson.tools.*

def inst = Jenkins.getInstance()
def desc = (hudson.plugins.octopusdeploy.OctoInstallation.DescriptorImpl) inst.getDescriptor(hudson.plugins.octopusdeploy.OctoInstallation.class)
def octoInstall = new hudson.plugins.octopusdeploy.OctoInstallation("LinuxOcto", "/usr/bin/octo")
desc.setInstallations(octoInstall)
desc.save()