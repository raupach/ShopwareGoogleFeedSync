package cc.raupach.sync.shopware.dto;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@Getter
@Setter
public class ProductAttribute {

    private String parentId;
    private String taxId;
    private String coverId;
    private String productNumber;
    private Integer stock;
    private String name;
    private String description;
    private List<String> optionIds;
    private List<ProductPrice> price;
    private Boolean shippingFree;


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
