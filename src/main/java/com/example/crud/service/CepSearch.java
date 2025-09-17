package com.example.crud.service;

import com.example.crud.domain.address.Address;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CepSearch {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CepSearch(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String searchCEP(String cep) {
        String url = "https://viacep.com.br/ws/{cep}/json/";

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("cep", cep);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, uriVariables);

        try {
            Address address = objectMapper.readValue(response.getBody(), Address.class);
            return address.getLocalidade();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Compara cidade obtida via CEP com o distribution center
    public boolean CepDistributionCenter(String cep, String distributionCenter) {
        String city = searchCEP(cep);
        if (city == null || distributionCenter == null) return false;
        return city.equalsIgnoreCase(distributionCenter);
    }
}
