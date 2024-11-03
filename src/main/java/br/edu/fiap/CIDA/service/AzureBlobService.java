package br.edu.fiap.CIDA.service;
import br.edu.fiap.CIDA.rabbitmq.RabbitMQConfig;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.PublicAccessType;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.Collections;

@Service
public class AzureBlobService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    private BlobServiceClient blobServiceClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }


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

            BlobClient blobClient = containerClient.getBlobClient(file.getOriginalFilename());
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            rabbitTemplate.convertAndSend(RabbitMQConfig.roteador,
                    RabbitMQConfig.chave_rota,
                    containerName);
            System.out.println("Arquivo enviado para o container: " + containerName);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload para o Azure Blob Storage", e);
        }
    }
}
