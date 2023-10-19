package cc.raupach.sync.shopware;

import cc.raupach.sync.config.ShopwareSyncProperties;
import cc.raupach.sync.shopware.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ShopwareHttpClient {

  private static final String PRODUCT = "product";
  private static final String PRODUCT_MEDIA = "product-media";
  private static final String PRODUCTS = "/products";
  private static final String TAG = "tag";
  private static final String CURRENCY = "currency";
  private static final String COVER = "/cover";
  private static final String SEO_URLS = "/seo-urls";
  private static final String MEDIA = "/media";

  @Autowired
  @Qualifier("shopware")
  private WebClient shopwareWebClient;

  @Autowired
  private ShopwareSyncProperties shopwareSyncProperties;


  public Mono<SingleProductResponse> getProductById(String id) {
    return shopwareWebClient.get()
      .uri(shopwareSyncProperties.getUrl() + PRODUCT + "/" + id)
      .retrieve()
      .bodyToMono(SingleProductResponse.class);
  }


  public Flux<ShopwareTag> getTags() {
    return shopwareWebClient.get()
      .uri(shopwareSyncProperties.getUrl() + TAG)
      .retrieve()
      .bodyToMono(TagResponse.class)
      .flatMapIterable(TagResponse::getData);
  }

  public Mono<SeoResponse> getSeoUrl(String id) {
    return shopwareWebClient.get()
      .uri(shopwareSyncProperties.getUrl() + PRODUCT + "/" + id + SEO_URLS)
      .retrieve()
      .bodyToMono(SeoResponse.class);
  }

  public Mono<CoverResponse> getProductCover(String id) {
    return shopwareWebClient.get()
      .uri(shopwareSyncProperties.getUrl() + PRODUCT + "/" + id + COVER)
      .retrieve()
      .bodyToMono(CoverResponse.class);
  }

  public Mono<MediaResponse> getMedia(String mediaId) {
    return shopwareWebClient.get()
      .uri(shopwareSyncProperties.getUrl() + PRODUCT_MEDIA + "/" + mediaId + MEDIA)
      .retrieve()
      .bodyToMono(MediaResponse.class);
  }

  public Flux<ShopwareProduct> getProductsForTagId(String tagId) {
    return shopwareWebClient.get()
      .uri(shopwareSyncProperties.getUrl() + TAG + "/" + tagId + PRODUCTS)
      .retrieve()
      .bodyToMono(ProductResponse.class)
      .flatMapIterable(ProductResponse::getData);
  }


  public Mono<CurrencyResponse> getCurrency(String id) {
    return shopwareWebClient.get()
      .uri(shopwareSyncProperties.getUrl() + CURRENCY + "/" + id)
      .retrieve()
      .bodyToMono(CurrencyResponse.class);
  }


}
