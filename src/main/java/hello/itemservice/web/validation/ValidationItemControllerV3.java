package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
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
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }


    /**
     * 상품 추가 처리 - @Validated 어노테이션 사용 (V6)
     * 스프링 프레임워크에서 제공하는 @Validated를 사용하여 검증 자동화
     *
     * @param item : @Validated 어노테이션이 붙은 객체는 앞서 등록한 검증기가 자동으로 적용됨
     *
     * @Validated 어노테이션 작동 원리:
     * 1. @InitBinder에 등록한 검증기 중 item 객체를 지원하는 검증기를 찾음(ItemValidator의 supports() 메서드 호출)
     * 2. 해당 검증기의 validate() 메서드를 자동으로 호출하여 검증 수행
     * 3. 검증 결과는 bindingResult에 담김
     */
    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // 검증 실패 시 다시 입력 폼으로
        //뷰에서 오류 메시지 표시 가능 (스프링이 model.addAttribute("BindingResult.item", bindingResult) 코드를 자동 수행)
        if (bindingResult.hasErrors()) {
            log.info("errors= {}", bindingResult);
            return "validation/v3/addForm";
        }

        // 검증 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }




    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}

