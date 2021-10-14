import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;
import hudson.tools.*

def inst = Jenkins.getInstance()
def desc = (hudson.plugins.octopusdeploy.OctopusDeployPlugin.DescriptorImpl) inst.getDescriptor(hudson.plugins.octopusdeploy.OctopusDeployPlugin.class)
def octoServer = new hudson.plugins.octopusdeploy.OctopusDeployServer("Octopus", "http://octopus:8080", System.getenv("OCTOPUS_API_KEY"), true)
desc.setOctopusDeployServers(new ArrayList<hudson.plugins.octopusdeploy.OctopusDeployServer>([octoServer]))
desc.save()