package cc.raupach.sync;

import cc.raupach.sync.google.GoogleService;
import cc.raupach.sync.google.dto.GoogleFeedDto;
import cc.raupach.sync.shopware.ShopwareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

@Service
@Slf4j
public class SyncService {


  @Autowired
  private ShopwareService shopwareService;

  @Autowired
  private GoogleService googleService;

  public void run() {

    Map<String, Collection<GoogleFeedDto>> tags = shopwareService.getAllTags()
      .flatMap(tag -> shopwareService.getProductsForTagId(tag))
      .log()
      .collectMultimap(GoogleFeedDto::getTagName, v -> v)
      .block();

    tags.keySet().forEach(tag -> {

      try {
        googleService.writeSheet(tag, tags.get(tag));
      } catch (IOException | URISyntaxException e) {
        throw new RuntimeException(e);
      }
    });

  }

}
