package br.edu.fiap.CIDA.service;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.PublicAccessType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;

@Service
public class AzureBlobService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    private BlobServiceClient blobServiceClient;

    @PostConstruct
    public void init() {
        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

//    DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
//    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
//            .endpoint("https://<storage-account-name>.blob.core.windows.net/")
//            .credential(defaultCredential)
//            .buildClient();
    public void uploadFile(MultipartFile file, String containerName) {

        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            containerClient.createIfNotExists();
            BlobSignedIdentifier identifier = new BlobSignedIdentifier()
                    .setId("name")
                    .setAccessPolicy(new BlobAccessPolicy()
                            .setStartsOn(OffsetDateTime.now())
                            .setExpiresOn(OffsetDateTime.now().plusDays(7))
                            .setPermissions("rw"));

            containerClient.setAccessPolicy(PublicAccessType.CONTAINER, Collections.singletonList(identifier));
//            BlobContainerClient blobContainerClient = blobServiceClient.createBlobContainer(containerName);

//            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
//            if (!containerClient.exists()) {
//                BlobSignedIdentifier identifier = new BlobSignedIdentifier()
//                        .setAccessPolicy(new BlobAccessPolicy()
////                            .setStartsOn(OffsetDateTime.now())
////                            .setExpiresOn(OffsetDateTime.now().plusDays(7))
//                                .setPermissions("rwl"));
////                containerClient.setAccessPolicy(PublicAccessType.BLOB);
//                containerClient.setAccessPolicy(PublicAccessType.CONTAINER, Collections.singletonList(identifier));
//
//                containerClient.create();
//            }
            BlobClient blobClient = containerClient.getBlobClient(file.getOriginalFilename());
            blobClient.upload(file.getInputStream(), file.getSize(), true);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload para o Azure Blob Storage", e);
        }
    }

    public void listarArquivos(String nomeContainer) {
        try {

            // Obtém o cliente do container
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(nomeContainer);

            // Lista os blobs (arquivos) no container
            System.out.println("Arquivos no container " + nomeContainer + ":");
            for (BlobItem blobItem : containerClient.listBlobs()) {
                System.out.println(" - " + blobItem.getName());
            }

        } catch (Exception e) {
            System.err.println("Erro ao listar arquivos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
