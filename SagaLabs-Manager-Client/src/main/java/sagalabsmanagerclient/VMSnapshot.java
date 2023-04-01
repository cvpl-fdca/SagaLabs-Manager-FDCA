package sagalabsmanagerclient;

import com.azure.core.util.Context;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.*;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VMSnapshot extends AzureMethods {

    public static void takeGeneralizedSnapshot(String resourceGroupName, String vmName, String imageDefinitionPrefix) {
        // Get the Azure resource manager instance
        AzureResourceManager azure = AzureLogin.getAzure();

        // Get the gallery resource ID from the Azure Key Vault secret
        String vmImageGalleryResourceGroup = "SL-vmImages";
        String vmImageGalleryName = "SagalabsVM";

        // Find the VM
        VirtualMachine vm = azure.virtualMachines().getByResourceGroup(resourceGroupName, vmName);

        if (vm == null) {
            System.out.println("VM not found");
            return;
        }
        String customImageName = vm.name() + "-image";

        // Create an image definition
        String imageName = imageDefinitionPrefix + "-" + vm.name();

        Gallery gallery = azure.galleries().getByResourceGroup(vmImageGalleryResourceGroup, vmImageGalleryName);

        GalleryImage imageDefinition = azure.galleryImages().getByGallery(vmImageGalleryResourceGroup,vmImageGalleryName,imageName);
        if (imageDefinition == null) {
            if (vm.osType() == OperatingSystemTypes.WINDOWS) {
                imageDefinition = azure.galleryImages().define(imageName)
                        .withExistingGallery(gallery)
                        .withLocation(vm.region())
                        .withIdentifier("sagalabs", "sagalabs", vmName)
                        .withGeneralizedWindows()
                        .create();
            } else {
                imageDefinition = azure.galleryImages().define(imageName)
                        .withExistingGallery(gallery)
                        .withLocation(vm.region())
                        .withIdentifier("sagalabs", "sagalabs", vmName)
                        .withGeneralizedLinux()
                        .create();
            }
        }

        // Deallocate and generalize the virtual machine
        vm.deallocate();
        vm.generalize();

        // Get the managed disk ID of the OS disk of the virtual machine
        String osDiskId = vm.osDiskId();

        // Create an image from the managed disk ID of the OS disk

        VirtualMachineCustomImage customImage = azure.virtualMachineCustomImages().define(customImageName)
                .withRegion(vm.region())
                .withExistingResourceGroup(resourceGroupName)
                .fromVirtualMachine(vm)
                .create();

        // Find the latest image version
        String latestImageVersion = azure.galleryImageVersions()
                .listByGalleryImage(resourceGroupName, vmName, imageDefinition.name())
                .stream()
                .max(Comparator.comparing(GalleryImageVersion::name))
                .map(GalleryImageVersion::name)
                .orElse("0.0.0");

        // Increment the latest image version
        String newImageVersion = incrementVersion(latestImageVersion);

        // Create a new image version
        GalleryImageVersion imageVersion = azure.galleryImageVersions().define(newImageVersion)
                .withExistingImage(vmImageGalleryResourceGroup, vmImageGalleryName, imageName)
                .withLocation(vm.region())
                .withSourceCustomImage(customImage.id())
                .create();

        System.out.println("Snapshot taken and image version created: " + imageVersion.name());
    }

    private static String incrementVersion(String version) {
        Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        Matcher matcher = pattern.matcher(version);

        if (matcher.matches()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = Integer.parseInt(matcher.group(3));

            patch++;

            return major + "." + minor + "." + patch;
        }

        return "0.0.1";
    }
    public static void takeSpecializedSnapshotV2(String resourceGroupName, String vmName, String imageDefinitionPrefix) {
        // Get the Azure resource manager instance
        AzureResourceManager azure = AzureLogin.getAzure();

        // Get the gallery resource ID from the Azure Key Vault secret
        String vmImageGalleryResourceGroup = "SL-vmImages";
        String vmImageGalleryName = "SagalabsVM";

        // Find the VM
        VirtualMachine vm = azure.virtualMachines().getByResourceGroup(resourceGroupName, vmName);

        if (vm == null) {
            System.out.println("VM not found");
            return;
        }
        String customImageName = vm.name() + "-image";

        // Create an image definition
        String imageName = imageDefinitionPrefix + "-" + vm.name();

        Gallery gallery = azure.galleries().getByResourceGroup(vmImageGalleryResourceGroup, vmImageGalleryName);

        GalleryImage imageDefinition = azure.galleryImages().getByGallery(vmImageGalleryResourceGroup, vmImageGalleryName, imageName);
        if (imageDefinition == null) {
            if (vm.osType() == OperatingSystemTypes.WINDOWS) {
                imageDefinition = azure.galleryImages().define(imageName)
                        .withExistingGallery(gallery)
                        .withLocation(vm.region())
                        .withIdentifier("sagalabs", "sagalabs", vmName)
                        .withWindows(OperatingSystemStateTypes.SPECIALIZED)
                        .create();
            } else {
                imageDefinition = azure.galleryImages().define(imageName)
                        .withExistingGallery(gallery)
                        .withLocation(vm.region())
                        .withIdentifier("sagalabs", "sagalabs", vmName)
                        .withLinux(OperatingSystemStateTypes.SPECIALIZED)
                        .create();
            }
        }

        // Deallocate the virtual machine
        vm.deallocate();

        // Get the managed disk ID of the OS disk of the virtual machine
        String osDiskId = vm.osDiskId();

        // Create an image from the managed disk ID of the OS disk
        VirtualMachineCustomImage customImage = azure.virtualMachineCustomImages().define(customImageName)
                .withRegion(vm.region())
                .withExistingResourceGroup(resourceGroupName)
                .fromVirtualMachine(vm)
                .create();

        // Find the latest image version
        String latestImageVersion = azure.galleryImageVersions()
                .listByGalleryImage(resourceGroupName, vmName, imageDefinition.name())
                .stream()
                .max(Comparator.comparing(GalleryImageVersion::name))
                .map(GalleryImageVersion::name)
                .orElse("0.0.0");

        // Increment the latest image version
        String newImageVersion = incrementVersion(latestImageVersion);

        // Create a new image version
        GalleryImageVersion imageVersion = azure.galleryImageVersions().define(newImageVersion)
                .withExistingImage(vmImageGalleryResourceGroup, vmImageGalleryName, imageName)
                .withLocation(vm.region())
                .withSourceCustomImage(customImage.id())
                .create();

        System.out.println("Snapshot taken and image version created: " + imageVersion.name());
    }

}
