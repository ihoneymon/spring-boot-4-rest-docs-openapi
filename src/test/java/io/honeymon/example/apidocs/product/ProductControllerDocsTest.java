package io.honeymon.example.apidocs.product;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import io.honeymon.example.apidocs.error.ApiExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {ProductController.class, ApiExceptionHandler.class})
@AutoConfigureRestDocs
class ProductControllerDocsTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-29T01:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void createProduct() throws Exception {
        given(productService.create(new CreateProductRequest("허니 버터", "FOOD", 8500)))
                .willReturn(new Product(1L, "허니 버터", "FOOD", 8500, CREATED_AT));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "허니 버터",
                                  "category": "FOOD",
                                  "price": 8500
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/products/1"))
                .andDo(document("products-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("상품명 (최대 100자)"),
                                fieldWithPath("category").description("상품 카테고리 (최대 50자)"),
                                fieldWithPath("price").description("상품 가격 (0 이상)")),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("생성된 상품의 URI")),
                        responseFields(productFields()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Products")
                                .summary("상품 생성")
                                .description("새 상품을 생성합니다.")
                                .requestFields(
                                        fieldWithPath("name").description("상품명 (최대 100자)"),
                                        fieldWithPath("category").description("상품 카테고리 (최대 50자)"),
                                        fieldWithPath("price").description("상품 가격 (0 이상)"))
                                .responseFields(productFields())
                                .build())));
    }

    @Test
    void getProduct() throws Exception {
        given(productService.findById(1L))
                .willReturn(new Product(1L, "허니 버터", "FOOD", 8500, CREATED_AT));

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andDo(document("products-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("상품 ID")),
                        responseFields(productFields()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Products")
                                .summary("상품 단건 조회")
                                .description("ID로 상품 하나를 조회합니다.")
                                .pathParameters(parameterWithName("id").description("상품 ID"))
                                .responseFields(productFields())
                                .build())));
    }

    @Test
    void listProducts() throws Exception {
        given(productService.findAll("FOOD"))
                .willReturn(List.of(new Product(1L, "허니 버터", "FOOD", 8500, CREATED_AT)));

        mockMvc.perform(get("/api/products").queryParam("category", "FOOD"))
                .andExpect(status().isOk())
                .andDo(document("products-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(parameterWithName("category").optional().description("카테고리 필터")),
                        responseFields(listProductFields()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Products")
                                .summary("상품 목록 조회")
                                .description("상품을 조회하며 카테고리로 필터링할 수 있습니다.")
                                .queryParameters(parameterWithName("category").optional().description("카테고리 필터"))
                                .responseFields(listProductFields())
                                .build())));
    }

    @Test
    void productNotFound() throws Exception {
        given(productService.findById(999L)).willThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andDo(document("products-get-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("상품 ID")),
                        responseFields(
                                fieldWithPath("code").description("오류 코드"),
                                fieldWithPath("message").description("오류 메시지"),
                                fieldWithPath("timestamp").description("오류 발생 시각")),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Products")
                                .summary("상품 단건 조회 - 찾을 수 없음")
                                .description("존재하지 않는 상품 ID를 조회한 경우입니다.")
                                .pathParameters(parameterWithName("id").description("상품 ID"))
                                .responseFields(
                                        fieldWithPath("code").description("오류 코드"),
                                        fieldWithPath("message").description("오류 메시지"),
                                        fieldWithPath("timestamp").description("오류 발생 시각"))
                                .build())));
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[] productFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("상품 ID"),
                fieldWithPath("name").type(JsonFieldType.STRING).description("상품명"),
                fieldWithPath("category").type(JsonFieldType.STRING).description("상품 카테고리"),
                fieldWithPath("price").type(JsonFieldType.NUMBER).description("상품 가격"),
                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성 시각(UTC, ISO-8601)")
        };
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[] listProductFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
                fieldWithPath("[]").description("상품 목록"),
                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
                fieldWithPath("[].name").type(JsonFieldType.STRING).description("상품명"),
                fieldWithPath("[].category").type(JsonFieldType.STRING).description("상품 카테고리"),
                fieldWithPath("[].price").type(JsonFieldType.NUMBER).description("상품 가격"),
                fieldWithPath("[].createdAt").type(JsonFieldType.STRING).description("생성 시각(UTC, ISO-8601)")
        };
    }
}
