package cc.raupach.sync.shopware;

import cc.raupach.sync.config.ShopwareSyncProperties;
import cc.raupach.sync.google.dto.GoogleFeedDto;
import cc.raupach.sync.shopware.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ShopwareService {

  @Autowired
  private ShopwareHttpClient shopwareHttpClient;

  @Autowired
  private ShopwareSyncProperties shopwareSyncProperties;


  public Flux<ShopwareTag> getAllTags() {
    Flux<ShopwareTag> tags = shopwareHttpClient.getTags()
      .filter(tag -> shopwareSyncProperties.getTagsForFeeds().containsKey(tag.getAttributes().getName()));

    tags.subscribe(m -> log.info(">>> {}", m.getAttributes().getName()));

    return tags;
  }

  public Flux<GoogleFeedDto> getProductsForTagId(ShopwareTag tag) {

    return shopwareHttpClient.getProductsForTagId(tag.getId())
      .flatMap(response -> toGoogleFeedDto(response, tag.getAttributes().getName())
        .flatMap(this::getParentProduct))
      .flatMap(this::getMediaUrl);

  }

  private Mono<GoogleFeedDto> getMediaUrl(GoogleFeedDto dto) {
    return shopwareHttpClient.getProductCover(dto.getId())

      .flatMap(cover -> shopwareHttpClient.getMedia(cover.getData().stream().findFirst().orElseThrow().getId()))
      .flatMap(media -> {
        MediaAttribute mediaAttr = media.getData().stream().findFirst().orElseThrow().getAttributes();
        dto.setMediaLink(mediaAttr.getUrl());
        return Mono.just(dto);
      });

  }

  private Mono<GoogleFeedDto> getParentProduct(GoogleFeedDto product) {
    if (StringUtils.isBlank(product.getParentId())) {
      return Mono.just(product);
    } else {
      return shopwareHttpClient.getProductById(product.getParentId())
        .flatMap(xx -> toGoogleFeedDto2(product, xx));
    }
  }

  private Mono<GoogleFeedDto> toGoogleFeedDto2(GoogleFeedDto product, SingleProductResponse singleProductResponse) {

    product.setDescription(singleProductResponse.getData().getAttributes().getDescription() == null ? null : Jsoup.parse(singleProductResponse.getData().getAttributes().getDescription()).text());
    product.setName(singleProductResponse.getData().getAttributes().getName());

    if (product.getPrice() == null) {
      ProductPrice productPrice = singleProductResponse.getData().getAttributes().getPrice().stream().findFirst().orElseThrow();
      product.setPrice(productPrice.getGross());

      Mono<CurrencyResponse> currencyResponse = shopwareHttpClient.getCurrency(productPrice.getCurrencyId());

      return Mono.zip(Mono.just(product), currencyResponse)
        .flatMap(tuple -> {
          GoogleFeedDto dto = tuple.getT1();
          dto.setCurrency(tuple.getT2().getData().getAttributes().getIsoCode());
          return Mono.just(dto);
        });
    } else {
      return Mono.just(product);
    }
  }


  private Mono<GoogleFeedDto> toGoogleFeedDto(ShopwareProduct response, String name) {

    ProductPrice productPrice = null;
    if (response.getAttributes().getPrice() != null) {
      productPrice = response.getAttributes().getPrice().stream().findFirst().orElse(null);
    }

    Mono<GoogleFeedDto> dto = Mono.just(GoogleFeedDto.builder()
      .description(response.getAttributes().getDescription() == null ? null : Jsoup.parse(response.getAttributes().getDescription()).text())
      .name(response.getAttributes().getName())
      .productNumber(response.getAttributes().getProductNumber())
      .price(productPrice == null ? null : productPrice.getGross())
      .parentId(response.getAttributes().getParentId())
      .id(response.getId())
      .tagName(name)
      .build());

    Mono<SeoResponse> seoResponse = shopwareHttpClient.getSeoUrl(response.getId());
    if (productPrice != null) {
      Mono<CurrencyResponse> currencyResponse = shopwareHttpClient.getCurrency(productPrice.getCurrencyId());
      return Mono.zip(dto, seoResponse, currencyResponse)
        .flatMap(tuple -> {
          GoogleFeedDto d = tuple.getT1();
          d.setUrl(tuple.getT2().getData().stream().findFirst().orElseThrow().getAttributes().getSeoPathInfo());
          d.setCurrency(tuple.getT3().getData().getAttributes().getIsoCode());
          return Mono.just(d);
        });
    } else {
      return Mono.zip(dto, seoResponse)
        .flatMap(tuple -> {
          GoogleFeedDto d = tuple.getT1();
          d.setUrl(tuple.getT2().getData().stream().findFirst().orElseThrow().getAttributes().getSeoPathInfo());
          return Mono.just(d);
        });
    }
  }
}
