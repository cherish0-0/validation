package hello.itemservice.web.validation;

import hello.itemservice.web.validation.form.ItemSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API 요청을 처리하는 검증 컨트롤러
 * - @RestController: @Controller + @ResponseBody 기능이 합쳐진 어노테이션
 *   - 메서드의 반환 값을 HTTP 응답 본문(Response Body)에 직접 입력
 *   - JSON, XML 등의 형식으로 객체를 변환하여 응답
 * - API 방식에서는 클라이언트(웹 브라우저, 모바일 앱 등)가 검증 오류를 직접 처리해야 함
 * - 오류 정보를 JSON 형태로 반환하여 클라이언트가 적절히 표시할 수 있도록 함
 */
@Slf4j
@RestController
@RequestMapping("/validation/api/items")
public class ValidationItemApiController {

    /**
     * API 방식의 상품 추가 처리
     *
     * @param form 클라이언트에서 전송한 상품 저장 데이터 (JSON 형식)
     *
     * @RequestBody 어노테이션:
     * - HTTP 요청 본문의 내용을 자바 객체로 변환 (API JSON -> 자바 객체)
     * - 이 과정에서 객체 변환 오류가 발생하면 컨트롤러가 호출되지 않고 예외 발생 -> Validator 적용할 수 없음
     *   (예: JSON 형식 오류, 타입 미스매치 등)
     *
     * cf. @ModelAttribute 어노테이션:
     *  - 주로 POST Form, URL 쿼리 스트링 기반의 웹 애플리케이션에서 사용
     *  - HTTP 요청 파라미터를 자바 객체로 변환
     *  - 필드 단위로 정교한 바인딩 적용, 특정 필드 바인딩 안돼도 나머지는 정상 바인딩 -> Validator 사용한 검증 적용 가능
     *  - 이 경우는 API 방식이므로 @ModelAttribute 대신 @RequestBody 사용
     *
     * @Validated 어노테이션:
     * - ItemSaveForm 객체에 정의된 Bean Validation 검증 규칙에 따라 검증 수행
     * - 검증 결과는 bindingResult에 담김
     */
    @PostMapping("/add")
    public Object addItem(@RequestBody @Validated ItemSaveForm form, BindingResult bindingResult) {
        log.info("API 컨트롤러 호출");

        /**
         * API 검증 오류 처리
         * - bindingResult.hasErrors()로 검증 오류 발생 여부 확인
         * - 오류 발생 시 getAllErrors()로 모든 오류 정보를 반환
         * - 오류 정보는 JSON 형태로 변환되어 클라이언트에게 전달됨
         * - @RequestBody 검증 실패 시(JSON 파싱 오류 등)는 이 메서드가 호출되지 않고
         *   400 오류가 발생함에 주의
         */
        if (bindingResult.hasErrors()) {
            log.info("검증 오류 발생 errors={}", bindingResult);
            return bindingResult.getAllErrors();
        }

        /**
         * 검증 성공 시 처리 로직
         * - 실제 구현에서는 여기서 데이터베이스에 저장하는 등의 비즈니스 로직 수행
         * - 저장된 객체나 성공 메시지 등을 반환하여 클라이언트에게 전달
         * - 여기서는 단순히 입력받은 폼 객체를 그대로 반환 (JSON으로 변환됨)
         */
        log.info("성공 로직 실행");
        return form;
    }
}
