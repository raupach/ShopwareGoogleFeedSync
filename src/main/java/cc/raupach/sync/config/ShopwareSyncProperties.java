package cc.raupach.sync.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Setter
@Getter
@PropertySource("classpath:application.properties")
public class ShopwareSyncProperties {

    @Value("${sync.shopware.url}")
    private String url;

    @Value("#{${sync.shopware.tags}}")
    private Map<String,String> tagsForFeeds;

}
