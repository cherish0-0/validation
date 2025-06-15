package hello.itemservice.domain.item;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class Item {

    private Long id;

    // 빈 값, null, 공백 허용 안 함 (자동으로 기본 에러 메시지 생성)
    @NotBlank
    private String itemName;

    // null 허용 안 함, 범위 1000 ~ 1,000,000 (자동으로 기본 에러 메시지 생성)
    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;

    // null 허용 안 함, 최대 9999
    @NotNull
    @Max(9999)
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
