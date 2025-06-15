package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {

    // 검증기가 여러 개일 경우 supports() 메서드가 구분해줌
    // (여기서는 파라미터에 Item.class가 들어옴 -> 결과는 true -> validate() 호출)
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
        // item == clazz
        // item == subItem
    }

    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target;

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            /**
             * rejectValue() 메서드 파라미터:
             * 1. field: 오류 필드명 ("itemName")
             * 2. errorCode: 오류 코드 (메시지 프로퍼티의 코드) - "required"
             * 3. errorArgs: 오류 메시지에서 사용할 인자 (옵션)
             * 4. defaultMessage: 오류 메시지를 찾을 수 없을 때 사용할 기본 메시지 (옵션)
             *
             * 내부적으로 FieldError를 생성하고 rejectedValue, bindingFailure 등 처리
             * 메시지 코드는 errorCode를 기반으로 다양한 메시지 코드를 생성하여 시도
             * required.item.itemName, required.itemName, required.java.lang.String, required
             */
            errors.rejectValue("itemName", "required");
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            errors.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                /**
                 * reject() 메서드 파라미터: (글로벌 오류에 사용)
                 * 1. errorCode: 오류 코드 - "totalPriceMin"
                 * 2. errorArgs: 오류 메시지에서 사용할 인자 - Object[]{10000, resultPrice}
                 * 3. defaultMessage: 오류 메시지를 찾을 수 없을 때 사용할 기본 메시지
                 *
                 * ObjectError를 직접 생성하는 대신 사용할 수 있는 편리한 메서드
                 */
                errors.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }
    }
}
