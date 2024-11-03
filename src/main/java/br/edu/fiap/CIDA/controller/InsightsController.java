package br.edu.fiap.CIDA.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import br.edu.fiap.CIDA.entity.Arquivo;
import br.edu.fiap.CIDA.entity.Insight;
import br.edu.fiap.CIDA.repository.InsightRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import br.edu.fiap.CIDA.repository.ArquivoRepository;
import br.edu.fiap.CIDA.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import com.fasterxml.jackson.core.type.TypeReference;

@Controller
public class InsightsController {
	
	private RestTemplate restTemplate;
	

	public InsightsController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    private InsightRepository insightRepo;
	@Autowired
	private UsuarioRepository userRepo;
	@Autowired
	private ArquivoRepository arquivoRepo;

	@GetMapping("/{id}/analisar")
    public ModelAndView retornaViewListaInsights(@PathVariable("id") Long id, HttpSession session) {

        var arquivos = arquivoRepo.findByUsuario(userRepo.findById(id).get());

//        if (session.getAttribute("usuarioRequest") != null) {
//            return new ModelAndView("redirect:/home");
//        }

        ModelAndView mv = new ModelAndView("lista_insights")
                .addObject("id", id)
                .addObject("arquivo", arquivos);
        System.out.println(mv);

        return mv;
    }


    @PostMapping("/{id}/analisar")
    public ModelAndView gerarInsight(@PathVariable("id") Long id) {
        var user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        var listArquivos = arquivoRepo.findByUsuario(user);

        // Extrair apenas os nomes dos arquivos
        List<String> nomesArquivos = listArquivos.stream()
                .map(Arquivo::getNome)
                .collect(Collectors.toList());

        Map<String, Object> request = new HashMap<>();
        request.put("container", user.getNomeContainer());
        request.put("file_names", nomesArquivos);

        String url = "http://localhost:8000/analyze";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // Use ObjectMapper to parse the response body
        ObjectMapper objectMapper = new ObjectMapper();
        String descricao = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);

        String dataGeracao = response.getHeaders().getFirst(HttpHeaders.DATE);

        try {
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
                descricao = (String) responseBody.get("insight");

                var insightGerado = Insight.builder()
                        .dataGeracao(LocalDateTime.parse(dataGeracao, formatter))
                        .descricao(descricao)
                        .usuario(user)
                        .build();

                insightRepo.save(insightGerado);

                return new ModelAndView("redirect:/{id}/insights/listar")
                        .addObject("id", id);
            } else {
                return new ModelAndView("error").addObject("Erro ao processar a análise.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ModelAndView("error").addObject("Erro ao processar a resposta da API.");
        }
    }

    @GetMapping("/{id}/insights/listar")
    public ModelAndView listarInsights(@PathVariable("id") Long id) {
        var user = userRepo.findById(id).get();

        var insights = insightRepo.findByUsuario(user);

        return new ModelAndView("lista_insights")
                .addObject("insights", insights);
    }

    @GetMapping("/{id}/insight/detalhes")
    public ModelAndView detalhesInsight(@PathVariable("id") Long id){
        var insight = insightRepo.findById(id);
        if (insight.isPresent()){
            return new  ModelAndView("detalhes_insight").addObject("insight",insight.get());
        }
        return new ModelAndView("error").addObject("message", "Arquivo não encontrado.");
    }


}
