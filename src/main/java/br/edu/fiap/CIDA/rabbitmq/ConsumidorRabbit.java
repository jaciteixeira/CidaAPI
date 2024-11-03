package br.edu.fiap.CIDA.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumidorRabbit {

    @RabbitListener(queues = RabbitMQConfig.fila)
    public void envioArquivo() {
        System.out.println("Arquivo foi salvo na Azure");
    }

}
