package com.ak.midentity.azuremidentity;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AzureBlobAdapter {

    private final BlobServiceClient blobServiceClient;

    public AzureBlobAdapter(@Value("${azure.client.id}") String clientId,
            @Value("${azure.client.secret}") String clientSecret,
            @Value("${azure.tenant.id}") String tenantId,
            @Value("${azure.storage.blob-endpoint}") String blobEndpoint) {

        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        blobServiceClient = new BlobServiceClientBuilder()
                .credential(clientSecretCredential)
                .endpoint(blobEndpoint)
                .buildClient();
    }

    /**
     * Upload file to Azure Blob Storage
     * 
     * @param containerName
     * @param file
     * @return
     */
    public String uploadFile(String containerName, MultipartFile file) {

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(file.getOriginalFilename());
        
        UserDelegationKey userDelegationKey = getUserDelegationKey();

        // get SAS token from delegation key
        String sasToken = generateSasToken(blobClient, userDelegationKey);

        // Upload file using SAS token
        BlobClient sasBlobClient = new BlobClientBuilder().endpoint(blobClient.getBlobUrl()).sasToken(sasToken)
                .buildClient();

        try {
            sasBlobClient.upload(file.getInputStream(), file.getSize());
            // return blob url with the sas token appended as query string
            return sasBlobClient.getBlobUrl() + "?" + sasToken;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private UserDelegationKey getUserDelegationKey() {
        // Get a user delegation key from Azure AD (valid for one day)
        OffsetDateTime keyStart = OffsetDateTime.now();
        OffsetDateTime keyExpiry = OffsetDateTime.now().plusDays(1);
        UserDelegationKey userDelegationKey = blobServiceClient.getUserDelegationKey(keyStart, keyExpiry);
        return userDelegationKey;
    }

    /**
     * List all the files in the Azure storage container
     * 
     * @return List of file urls
     */
    public List<String> listFiles() {

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("uploads");

        List<String> fileUrls = new ArrayList<>();
        for (BlobItem blobItem : containerClient.listBlobs()) {
            String blobUrl = containerClient.getBlobClient(blobItem.getName()).getBlobUrl();
            fileUrls.add(blobUrl);
        }
        return fileUrls;
    }

    /**
     * Generate SAS token for blob client, grant all read/write privileges
     * 
     * @param blobClient
     * @return
     */
    private String generateSasToken(BlobClient blobClient, UserDelegationKey userDelegationKey) {

        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusMinutes(5), //
                new BlobContainerSasPermission().setCreatePermission(true)
                        .setWritePermission(true)
                        .setReadPermission(true));

        return blobClient.generateUserDelegationSas(sasSignatureValues, userDelegationKey);

    }
}
