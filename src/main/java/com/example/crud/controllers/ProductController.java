package com.example.crud.controllers;

import com.example.crud.domain.product.Product;
import com.example.crud.domain.product.ProductRepository;
import com.example.crud.domain.category.RequestCategory;
import com.example.crud.domain.product.RequestProduct;
import com.example.crud.service.AddressSearch;
import com.example.crud.service.CepSearch; // import no service
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductRepository repository;
    private final AddressSearch addressSearch;
    private final CepSearch cepSearch; // atributo do service

    @Autowired
    public ProductController(ProductRepository repository, AddressSearch addressSearch, CepSearch cepSearch) {
        this.repository = repository;
        this.addressSearch = addressSearch;
        this.cepSearch = cepSearch;
    }

    @GetMapping
    public ResponseEntity getAllProducts(){
        var allProducts = repository.findAllByActiveTrue();
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/cep")
    public ResponseEntity verifyAvailability(@RequestParam String state, @RequestParam String city, @RequestParam String street){
        String cep = addressSearch.searchAddress(state, city, street);
        return ResponseEntity.ok(cep);
    }

    // Endpoint da atividade: verifica se a cidade do CEP é igual ao distribution_center do produto
    @GetMapping("/distribution")
    public ResponseEntity verifyCity(@RequestParam String cep, @RequestParam String productId){
        Product product = repository.getReferenceById(productId);
        boolean sameCity = cepSearch.CepDistributionCenter(cep, product.getDistribution_center());
        return ResponseEntity.ok(sameCity);
    }

    @GetMapping("/endpoint1") //products from only one category
    public ResponseEntity getAllProducts1(@RequestParam String categoryAsParam){
        var allProducts = repository.findAllByCategory(categoryAsParam);
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/endpoint2/{id}") //only one product
    public ResponseEntity getProduct(@PathVariable String id){
        Product product = repository.getReferenceById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/endpoint3/top5byprice") // top 5 product by price
    public ResponseEntity getAllProducts3(){
        var allProducts = repository.findAllByActiveTrue();

        List<Product> topFive = allProducts
                .stream()
                .sorted(Comparator.comparingInt(Product::getPrice).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return ResponseEntity.ok(topFive);
    }

    @GetMapping("/category/{categoryAsPath}") //all REST Components
    public ResponseEntity getProductsByCategory(
            @RequestHeader String categoryAsHeader,
            @PathVariable String categoryAsPath,
            @RequestBody @Valid RequestCategory categoryAsBody,
            @RequestParam String categoryAsParam
    ){
        var allProducts = repository.findAllByActiveTrue();
        List<Product> filteredProducts = new ArrayList<>();

        for (int i = 0; i < allProducts.size(); i++) {
            Product product = allProducts.get(i);
            if (categoryAsParam.equals(product.getCategory())) {
                filteredProducts.add(product);
            }
        }
        return ResponseEntity.ok(filteredProducts);
    }

    @PostMapping
    public ResponseEntity registerProduct(@RequestBody @Valid RequestProduct data){
        Product newProduct = new Product(data);
        repository.save(newProduct);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    @Transactional
    public ResponseEntity updateProduct(@RequestBody @Valid RequestProduct data){
        Product product = repository.getReferenceById(data.id());
        product.setName(data.name());
        product.setPrice(data.price());
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deleteProduct(@PathVariable String id){
        Product product = repository.getReferenceById(id);
        product.setActive(false);
        return ResponseEntity.noContent().build();
    }

}
