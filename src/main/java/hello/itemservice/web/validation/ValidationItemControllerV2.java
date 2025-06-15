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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
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
    private final ItemValidator itemValidator;

    /**
     * 컨트롤러에서 검증기(Validator) 등록
     * WebDataBinder는 요청 데이터를 객체에 바인딩하는 역할을 담당
     * 이 메서드에서 등록한 검증기는 해당 컨트롤러 내에서 @Validated 어노테이션이 붙은 객체에 대해 자동으로 검증을 수행함
     */
    @InitBinder
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(itemValidator);
    }

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
    // @PostMapping("/add")
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

    /**
     * 상품 추가 처리 - 사용자 입력값 유지 기능 추가 (V2)
     */
    // @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

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
             * FieldError 생성자 파라미터 (V2 개선 버전):
             * 1. objectName: 오류가 발생한 객체명 ("item")
             * 2. field: 오류 필드명 ("itemName")
             * 3. rejectedValue: 사용자가 입력한 값(거절된 값) - item.getItemName()
             * 4. bindingFailure: 타입 오류 같은 바인딩 실패인지 여부 (여기선 false)
             * 5. codes: 메시지 코드 (null 사용)
             * 6. arguments: 메시지에서 사용하는 인자 (null 사용)
             * 7. defaultMessage: 기본 오류 메시지
             *
             * 사용자가 입력한 값(rejectedValue)을 보관하고 있어 오류 발생 시에도 입력 폼에 이전 값을 유지할 수 있음
             * 타입 오류로 바인딩 실패 시 스프링은 자동으로 FieldError를 생성하며 사용자 입력값을 넣어둠 -> 오류를 BingingResult에 담아 컨트롤러를 호출
             *  => 바인딩 실패 시에도 오류 메시지 정상 출력
             */
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다."));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, null, null, "가격은 1,000 ~ 1,000,000 사이여야 합니다."));
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, null, null, "수량은 최대 9,999 까지 허용합니다."));


        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                /**
                 * ObjectError 생성자 파라미터 (V2 개선 버전):
                 * 1. objectName: 오류가 발생한 객체명 ("item")
                 * 2. codes: 메시지 코드 (null 사용)
                 * 3. arguments: 메시지에서 사용하는 인자 (null 사용)
                 * 4. defaultMessage: 기본 오류 메시지
                 *
                 * FieldError와 마찬가지로 메시지 코드와 인자를 지원함
                 */
                bindingResult.addError(new ObjectError("item", null, null, "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
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

    /**
     * 상품 추가 처리 - 오류 메시지 외부화 (V3)
     * errors.properties 파일의 메시지 코드를 사용하여 오류 메시지 관리
     */
    // @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

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
             * FieldError 생성자 파라미터 (V2 개선 버전):
             * 1. objectName: 오류가 발생한 객체명 ("item")
             * 2. field: 오류 필드명 ("itemName")
             * 3. rejectedValue: 사용자가 입력한 값(거절된 값) - item.getItemName()
             * 4. bindingFailure: 타입 오류 같은 바인딩 실패인지 여부 (여기선 false)
             * 5. codes: 메시지 코드 배열 - errors.properties에 정의된 메시지 코드
             * 6. arguments: 메시지에서 사용하는 인자 (null 사용)
             * 7. defaultMessage: 기본 오류 메시지 (메시지 코드로 메시지를 찾을 수 없는 경우 사용)
             *
             * 메시지 코드("required.item.itemName")를 사용하여 errors.properties에서 메시지를 찾음
             * 메시지 소스에서 메시지를 찾을 수 없는 경우 defaultMessage를 사용 (여기서는 null)
             */
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, new String[] {"range.item.price"}, new Object[]{1000, 1000000}, null));
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));


        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                /**
                 * ObjectError 생성자 파라미터 (V3 개선 버전):
                 * 1. objectName: 오류가 발생한 객체명 ("item")
                 * 2. codes: 메시지 코드 배열 - "totalPriceMin"
                 * 3. arguments: 메시지에서 사용하는 인자 - Object[]{10000, resultPrice}
                 * 4. defaultMessage: 기본 오류 메시지 (null)
                 *
                 * 전체 가격 검증을 위한 글로벌 오류 메시지에도 코드와 인자 적용
                 * ex) totalPriceMin=전체 가격은 {0}원 이상이어야 합니다. 현재 값 = {1}
                 */
                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
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

    /**
     * 상품 추가 처리 - rejectValue(), reject() 메서드 사용 (V4)
     * FieldError, ObjectError 직접 생성 대신 편리한 rejectValue, reject 메서드 사용
     */
    // @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        /**
         * BindingResult는 검증 대상 바로 다음에 온다는 점에서 검증 대상을 알고 있음
         * 따라서 target(item)에 대한 정보를 이미 갖고 있으므로 objectName을 생략할 수 있는
         * rejectValue(), reject() 메서드를 제공함
         */

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
            bindingResult.rejectValue("itemName", "required");
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
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
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
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

    /**
     * 상품 추가 처리 - ItemValidator 사용 (V5)
     * 별도로 분리한 검증 클래스(ItemValidator)를 사용하여 검증 로직 처리
     */
    // @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        /**
         * ItemValidator를 직접 호출하여 검증 로직 실행
         * validate(Object target, Errors errors) 메서드 호출
         * - item: 검증 대상 객체
         * - bindingResult: 검증 오류를 담을 객체(Errors의 하위 인터페이스)
         *
         * 별도의 검증 클래스를 사용하면 다음과 같은 장점이 있음:
         * 1. 검증 로직 재사용 가능
         * 2. 관심사 분리(검증 로직과 컨트롤러 로직 분리)
         * 3. 코드 가독성 향상
         */
        itemValidator.validate(item, bindingResult);

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
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

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

