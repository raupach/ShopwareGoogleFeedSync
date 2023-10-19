package cc.raupach.sync.google.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
@Builder
public class GoogleFeedDto {

  private String tagName;
  private String name;
  private String description;
  private String productNumber;
  private String url;
  private String mediaLink;
  private Double price;
  private String currency;
  private String parentId;
  private String id;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
