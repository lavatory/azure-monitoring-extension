package com.appdynamics.monitors.azure.statsCollector;

import com.appdynamics.monitors.azure.request.AzureHttpsClient;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class DiskStatsCollector extends AbstractStatsCollector {

    private static final Logger LOG = Logger.getLogger(DiskStatsCollector.class);

    private static final String DISKS_REST = "https://management.core.windows.net/%s/services/disks";
    private static final String METRIC_PATH = "DISK|%s|Disk Size|";

    private final AzureHttpsClient azureHttpsClient;

    public DiskStatsCollector(AzureHttpsClient azureHttpsClient) {
        this.azureHttpsClient = azureHttpsClient;
    }

    @Override
    public Map<String, Number> collectStats(String keyStorePath, String keyStorePassword, String subscriptionId, String restApiVersion, Properties displayProperties) {
        URL url = azureHttpsClient.buildRequestUrl(DISKS_REST, subscriptionId);

        Document document = azureHttpsClient.processGetRequest(url, restApiVersion, keyStorePath, keyStorePassword);

        Map<String, Number> diskStatsMap = new LinkedHashMap<String, Number>();

        NodeList imageNameNodeList = document.getElementsByTagName("Disk");

        for (int i = 0; i < imageNameNodeList.getLength(); i++) {
            Element element = (Element) imageNameNodeList.item(i);
            String diskName = element.getElementsByTagName("Name").item(0).getTextContent();
            String diskSizeString = element.getElementsByTagName("LogicalDiskSizeInGB").item(0).getTextContent();

            Integer statValue = null;
            try {
                statValue = Integer.parseInt(diskSizeString);
                diskStatsMap.put(String.format(METRIC_PATH, diskName), statValue);
            } catch (NumberFormatException nfe) {
                LOG.error("Unable to parse disk size " + diskSizeString + " to integer");
            }
        }

        return diskStatsMap;
    }
}
