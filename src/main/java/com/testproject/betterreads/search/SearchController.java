package com.testproject.betterreads.search;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Controller
public class SearchController {
    private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

    private final WebClient webClient;
    // open library api will be used to search for books, configuring web client
    public SearchController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build()).baseUrl("http://openlibrary.org/search.json").build();
    }

    @GetMapping(value = "/search")
    public String getSearchResults(@RequestParam String query, Model model) {
        //System.out.println("Inside search");
        Mono<SearchResult> resultMono = this.webClient.get().uri("?q={query}", query).retrieve()
                .bodyToMono(SearchResult.class);

        SearchResult searchResult = resultMono.block();
        //System.out.println(searchResult.getDocs().size());
        // Show only first 10 books to the user
        List<SearchResultBook> docs = searchResult.getDocs()
                .stream().limit(10)
                .map(bookResult -> {
                    bookResult.setKey(bookResult.getKey().replace("/works/", ""));
                    String coverId = bookResult.getCover_i();
                    // If no image is associated with the book default image will be shown
                    if (StringUtils.hasText(coverId)) {
                        coverId = COVER_IMAGE_ROOT + coverId + "-M.jpg";
                    } else {
                        coverId = "/images/no-image.png";
                    }
                    bookResult.setCover_i(coverId);
                    System.out.println(bookResult);
                    return bookResult;
                }).collect(Collectors.toList());

        model.addAttribute("searchResults", docs);
        return "search";

    }

}
