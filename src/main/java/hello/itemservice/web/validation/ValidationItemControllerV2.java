package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

    /**
     * 상품 추가 처리 - BindingResult를 이용한 검증
     *
     * @PostMapping("/add") : POST 메서드로 "/add" URL 요청을 처리
     *
     * @param item : @ModelAttribute로 HTTP 요청 파라미터를 Item 객체에 바인딩
     * @param bindingResult : 데이터 바인딩 및 검증 오류 정보를 보관하는 객체
     * @param redirectAttributes : 리다이렉트 시 사용할 속성 값 설정
     */
    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        /**
         * BindingResult는 스프링이 제공하는 검증 오류를 보관하는 객체로 다음과 같은 특징이 있다:
         * 1. 반드시 @ModelAttribute 바로 다음 파라미터로 선언해야 함 (순서 중요)
         * 2. 검증할 대상 객체(item)의 바인딩 결과를 담고 있음
         * 3. 필드 단위 오류와 객체 단위 오류를 모두 저장할 수 있음
         * 4. 뷰에 자동으로 오류 정보가 넘어가 타임리프 등에서 활용 가능 (model 객체 자동 생성)
         * 5. 바인딩 실패 시에도 컨트롤러가 호출됨 (검증 로직 실행 가능)
         */

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            /**
             * FieldError 생성자 파라미터:
             * 1. objectName: 오류가 발생한 객체명 ("item")
             * 2. field: 오류 필드명 ("itemName")
             * 3. defaultMessage: 오류 기본 메시지
             *
             * FieldError는 특정 필드에 대한 오류를 나타냄
             */
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 사이여야 합니다."));
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));

        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                /**
                 * ObjectError 생성자 파라미터:
                 * 1. objectName: 오류가 발생한 객체명 ("item")
                 * 2. defaultMessage: 오류 기본 메시지
                 *
                 * ObjectError는 특정 필드가 아닌 객체 자체에 대한 글로벌 오류를 나타냄
                 */
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        // 검증 실패 시 다시 입력 폼으로
        //뷰에서 오류 메시지 표시 가능 (스프링이 model.addAttribute("BindingResult.item", bindingResult) 코드를 자동 수행)
        if (bindingResult.hasErrors()) {
            log.info("errors= {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

