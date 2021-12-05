package cc.raupach.sync.shopware.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SeoAttribute {

    private String routeName;
    private String pathInfo;
    private String seoPathInfo;
    private Date createdAt;
    private Boolean isCanonical;
    private Boolean isDeleted;
    private String url;

}
