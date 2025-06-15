package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.web.validation.form.ItemSaveForm;
import hello.itemservice.web.validation.form.ItemUpdateForm;
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
     * 상품 추가 처리 - 폼 객체 분리 버전
     *
     * @param form 상품 저장을 위한 폼 객체. @ModelAttribute("item")을 사용해 뷰에서 접근할 이름을 "item"으로 지정
     *
     * 폼 객체(ItemSaveForm) 사용의 특징:
     * 1. 폼 데이터를 전용 객체로 받아 검증 진행
     * 2. 검증 통과 후 도메인 객체(Item)로 변환하여 비즈니스 로직 처리
     * 3. 저장 시 필요한 필드만 포함하여 명확성 향상
     * 4. 필요한 검증 어노테이션만 적용 가능(groups 속성 불필요)
     */
    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // 특정 필드가 아닌 복합 룰 검증
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
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
        /**
         * 폼 객체에서 도메인 객체로 데이터 복사
         */
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setPrice(form.getPrice());
        item.setQuantity(form.getQuantity());

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
     * 상품 수정 처리 - 폼 객체 분리 버전
     *
     * @param form 상품 수정을 위한 폼 객체. @ModelAttribute("item")을 사용해 뷰에서 접근할 이름을 "item"으로 지정
     *
     * 수정용 폼 객체(ItemUpdateForm) 사용의 특징:
     * 1. ID를 필수 값으로 포함하여 수정 대상을 명확히 식별
     * 2. 수정 시 필요한 필드와 검증 로직만 포함
     * 3. 저장용 폼 객체와 명확히 구분되어 의도 파악이 용이
     */
    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form , BindingResult bindingResult) {
        // 특정 필드가 아닌 복합 룰 검증
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
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

        /**
         * 폼 객체에서 도메인 객체로 데이터 복사
         * 수정 처리 시에는 ID를 직접 설정하지 않고 파라미터로 받은 itemId 사용
         */
        Item itemParam = new Item();
        itemParam.setItemName(form.getItemName());
        itemParam.setPrice(form.getPrice());
        itemParam.setQuantity(form.getQuantity());

        itemRepository.update(itemId, itemParam);
        return "redirect:/validation/v4/items/{itemId}";
    }

}

