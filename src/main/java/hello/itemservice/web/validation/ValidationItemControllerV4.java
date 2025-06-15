package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
    }


    /**
     * 상품 추가 처리 - Bean Validation 사용 (기본 버전)
     * 스프링 부트는 자동으로 글로벌 Validator로 Bean Validator를 등록함
     *
     * @Validated 어노테이션은 스프링 전용 검증 어노테이션으로 내부적으로 다음 과정을 수행:
     * 1. Item 객체에 대해 Bean Validation을 실행
     * 2. 검증 오류는 BindingResult에 담김
     * 3. javax.validation 패키지의 @Valid 어노테이션도 동일한 기능 제공 (자바 표준)
     */
    // @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

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
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // 검증 실패 시 다시 입력 폼으로
        // 뷰에서 오류 메시지 표시 가능 (스프링이 model.addAttribute("BindingResult.item", bindingResult) 코드를 자동 수행)
        if (bindingResult.hasErrors()) {
            log.info("errors= {}", bindingResult);
            return "validation/v4/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }



    /**
     * 상품 추가 처리 - 검증 그룹 기능 적용 버전
     *
     * @Validated(SaveCheck.class) 어노테이션 설명:
     * - SaveCheck.class 그룹에 속한 검증 규칙만 적용
     * - Item 클래스의 필드에 @NotNull(groups = SaveCheck.class) 같은 방식으로 그룹을 지정해야 함
     * - 그룹을 지정하지 않은 검증 규칙은 적용되지 않음
     *
     * 검증 그룹 기능의 장점:
     * 1. 동일한 객체에 대해 상황별로 다른 검증 규칙 적용 가능
     * 2. 저장/수정 시 검증 로직 분리 가능
     * 3. 컨트롤러에서 검증 로직을 분리하여 도메인 객체에 응집 가능
     */
    @PostMapping("/add")
    public String addItemV2(@Validated(SaveCheck.class) @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

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
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // 검증 실패 시 다시 입력 폼으로
        // 뷰에서 오류 메시지 표시 가능 (스프링이 model.addAttribute("BindingResult.item", bindingResult) 코드를 자동 수행)
        if (bindingResult.hasErrors()) {
            log.info("errors= {}", bindingResult);
            return "validation/v4/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }




    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/editForm";
    }

    /**
     * 상품 수정 처리 - 기본 버전
     * 모든 Bean Validation 규칙 적용
     */
    // @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute Item item , BindingResult bindingResult) {
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
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("errors= {}", bindingResult);
            return "validation/v4/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v4/items/{itemId}";
    }


    /**
     * 상품 수정 처리 - 검증 그룹 기능 적용 버전
     *
     * @Validated(UpdateCheck.class) 어노테이션 설명:
     * - UpdateCheck.class 그룹에 속한 검증 규칙만 적용
     * - Item 클래스의 필드에 @NotNull(groups = UpdateCheck.class) 같은 방식으로 그룹을 지정해야 함
     * - 수정 시에는 ID가 필수이지만 저장 시에는 ID가 생성되므로 불필요
     * - 이런 상황별 제약조건 차이를 검증 그룹으로 해결
     */
    @PostMapping("/{itemId}/edit")
    public String editV2(@PathVariable Long itemId, @Validated(UpdateCheck.class) @ModelAttribute Item item , BindingResult bindingResult) {
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
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("errors= {}", bindingResult);
            return "validation/v4/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v4/items/{itemId}";
    }


}

