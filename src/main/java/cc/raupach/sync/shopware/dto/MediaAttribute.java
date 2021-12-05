package cc.raupach.sync.shopware.dto;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

@Getter
@Setter
public class MediaAttribute {

    private String userId;
    private String mediaFolderId;
    private String mimeType;
    private Date uploadedAt;
    private String fileName;
    private Integer fileSize;
    private String url;
    private String alt;
    private String title;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
