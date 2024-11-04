# CIDA - Consulting Insights With Deep Analysis

## INTEGRANTES

| INTEGRANTES                    | RM      |
|--------------------------------|---------|
| Cauã Alencar Rojas Romero      | RM98638 | 
| Jaci Teixeira Santos           | RM99627 | 
| Leonardo dos Santos Guerra     | RM99738 | 
| Maria Eduarda Ferreira da Mata | RM99004 | 

## LINK DO REPOSITORIO NO GITHUB
[Link](https://github.com/jaciteixeira/CidaAPI.git)

## RESUMO DA PROPOSTA
Num cenário onde a crescente dependência em dados impõe desafios às empresas, surge a problemática da interpretação de vastos conjuntos de dados desorganizados. O custo e a imprevisibilidade associados às consultorias tradicionais, que dependem de mão de obra qualificada, tornam-se barreiras significativas. A ineficiência na análise desses dados pode resultar em perda de competitividade e na tomada de decisões inadequadas.

Para enfrentar esse desafio, surge a solução inovadora: a Consulting Insights with Deep Analysis (CIDA), uma IA projetada para processar dados e gerar insights empresariais de forma rápida e previsível. A CIDA opera em duas fases distintas: inicialmente, processa dados brutos e os refina, e em seguida, utiliza IA generativa para analisá-los e fornecer insights acionáveis e recomendações para melhorias internas.

Comparada às consultorias tradicionais, a CIDA oferece diversas vantagens, incluindo custo reduzido, ausência de supervisão humana, facilidade de uso e tempos de processamento mais rápidos. Seu público-alvo principal são as pequenas e médias empresas (PMEs) e startups, que podem não ter estruturas de documentação interna estabelecidas ou recursos para consultorias tradicionais. Além disso, empreendedores, departamentos individuais e empresas de consultoria também podem se beneficiar da plataforma.

Embora existam ferramentas de Business Intelligence (BI) semelhantes no mercado, como Board e ThoughtSpot, a CIDA se destaca por sua capacidade de processar dados brutos e gerar insights sem exigir entrada manual de dados ou integração complexa com sistemas de BI.

O potencial de mercado para a CIDA é vasto, especialmente considerando que a maioria das empresas no Brasil são PMEs. Com milhões de possíveis clientes, a CIDA tem a oportunidade de impactar positivamente o panorama empresarial, capacitando empresas de todos os tamanhos a utilizar dados de forma mais eficaz para impulsionar o crescimento e a competitividade.

Em resumo, a CIDA representa uma abordagem inovadora e acessível para transformar dados em insights acionáveis, oferecendo uma solução ágil e eficaz para as necessidades de análise de dados das empresas modernas.

## INSTRUÇÕES PARA RODAR E TESTAR A APLICAÇÃO

### Configuração de ambiente
1. Adicionar a `api key` da OpenIA (fornecida pelo professor), no arquivo [application.yaml](src/main/resources/application.yaml) no campo `api-key:`
   ```
   'api key ...'
   ```
2. Rodar comando para subir RabbitMQ no docker localmente
    ``` bash
    docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
    ``` 
3. #### ***OBSERVAÇÕES***
   Para testar a funcionalidade de geração de `INSIGHTS`, você deve rodar a API construída pela CIDA em Python. Instruções para configurar e rodar a API estão disponíveis no repositório [CIDA-Python](https://github.com/Open-Group-Fiap/CIDA-Python).


## LOGIN E ARQUIVOS PARA TESTES

**email**: cida.teste@email.com  
**senha**: Test@1234!

### Arquivos para teste da aplicação
[Link para arquivos de teste](files_to_test)

### Variaveis para azure
Utilize as seguintes variáveis para rodar a aplicação e testar suas funcionalidades:
````yaml
azure:
  storage:
    account-name: "cidastore"
    account-key: "OofPa4ovwX8556wyVanY8OPUP5pnxAHLCdYTPoujBqhpU/2Aty6uQfbnoaTA0Rcgb1cRVsq/oPwQ+AStTpvISg=="
    connection-string: "DefaultEndpointsProtocol=https;AccountName=cidastore;AccountKey=OofPa4ovwX8556wyVanY8OPUP5pnxAHLCdYTPoujBqhpU/2Aty6uQfbnoaTA0Rcgb1cRVsq/oPwQ+AStTpvISg==;EndpointSuffix=core.windows.net"
````

### Link Video de Demostração da solução
[Link](https://www.youtube.com/watch?v=w9ewLdvzBNs)
