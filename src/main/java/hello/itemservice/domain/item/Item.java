package hello.itemservice.domain.item;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;

@Data
// @ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000", message = "총합이 10000 이상이어야 합니다.")
public class Item {

    // @NotNull(groups = UpdateCheck.class) // 수정 요구사항 추가
    private Long id;

    // 빈 값, null, 공백 허용 안 함 (자동으로 기본 에러 메시지 생성)
    // @NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
    private String itemName;

    // null 허용 안 함, 범위 1000 ~ 1,000,000 (자동으로 기본 에러 메시지 생성)
    // @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    // @Range(min = 1000, max = 1000000, groups = {SaveCheck.class, UpdateCheck.class})
    private Integer price;

    // null 허용 안 함, 최대 9999
    // @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    // @Max(value = 9999, groups = SaveCheck.class)
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
