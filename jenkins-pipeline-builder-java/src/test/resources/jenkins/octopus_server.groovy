import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;
import hudson.tools.*

def inst = Jenkins.getInstance()
def desc = inst.getDescriptorByType(hudson.plugins.octopusdeploy.OctopusDeployPlugin.DescriptorImpl)
def octoServer = new hudson.plugins.octopusdeploy.OctopusDeployServer("Default", "http://octopus:8080", System.getenv("OCTOPUS_API_KEY"), true, true)
desc.setOctopusDeployServers([octoServer] as hudson.plugins.octopusdeploy.OctopusDeployServer[])
desc.save()