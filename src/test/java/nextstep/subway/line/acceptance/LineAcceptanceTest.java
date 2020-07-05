package nextstep.subway.line.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    private static final Logger log = LoggerFactory.getLogger(LineAcceptanceTest.class);

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // when
        // 지하철_노선_생성_요청
        ExtractableResponse<Response> response = 지하철_노선_생성_요청();

        // then
        // 지하철_노선_생성됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }


    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLine2() {
        // given
        // 지하철_노선_등록되어_있음
        지하철_노선_생성_요청();

        final ExtractableResponse<Response> response = 지하철_노선_생성_요청();

        // then
        // 지하철_노선_생성_실패됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        // 지하철_노선_등록되어_있음
        // 지하철_노선_등록되어_있음
        지하철_노선_생성_요청();

        // when
        // 지하철_노선_목록_조회_요청
        final ExtractableResponse<Response> response = 지하철_노선_목록_조회_요청();

        // then
        // 지하철_노선_목록_응답됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        // 지하철_노선_목록_포함됨
        assertThat(response.body()).isNotNull();
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void getLine() {
        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청();

        // when
        // 지하철_노선_조회_요청
        final String uri = createResponse.header("Location");

        final ExtractableResponse<Response> response = 지하철_노선_조회_요청(uri);

        // then
        // 지하철_노선_응답됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.as(LineResponse.class)).isNotNull();
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        // 지하철_노선_등록되어_있음
        final ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청();

        // when
        // 지하철_노선_수정_요청
        final String uri = createResponse.header("Location");
        final String nameToUpdate = "분당당선";
        final Map<String, String> params = buildCreateLineRequestParam(
                nameToUpdate, "bg-red-600", LocalTime.of(05, 30), LocalTime.of(23, 30), "5"
        );

        final ExtractableResponse<Response> updateResponse = 지하철_노선_수정_요청(uri, params);

        // then
        // 지하철_노선_수정됨
        assertThat(updateResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        final ExtractableResponse<Response> response = 지하철_노선_조회_요청(uri);
        final LineResponse line = response.as(LineResponse.class);
        assertThat(line.getName()).isEqualTo(nameToUpdate);
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        // 지하철_노선_등록되어_있음
        final ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청();
        final String uri = createResponse.header("Location");

        // when
        // 지하철_노선_제거_요청
        final ExtractableResponse<Response> deleteResponse = 지하철_노선_제거_요청(uri);

        // then
        // 지하철_노선_삭제됨
        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    private ExtractableResponse<Response> 지하철_노선_제거_요청(String uri) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete(uri)
                .then()
                .log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철_노선_생성_요청() {
        final Map<String, String> params = buildCreateLineRequestParam(
                "신분당선",
                "bg-red-600",
                LocalTime.of(05, 30),
                LocalTime.of(23, 30),
                "5"
        );

        return RestAssured.given().log().all().
                contentType(MediaType.APPLICATION_JSON_VALUE).
                body(params).
                when().
                post("/lines").
                then().
                log().all().
                extract();
    }

    private ExtractableResponse<Response> 지하철_노선_목록_조회_요청() {
        return RestAssured.given().log().all()
                .when()
                .get("/lines")
                .then()
                .log().all().extract();
    }

    private ExtractableResponse<Response> 지하철_노선_조회_요청(String uri) {
        return RestAssured.given().log().all().
                accept(MediaType.APPLICATION_JSON_VALUE).
                when().
                get(uri).
                then().
                log().all().
                extract();
    }

    private ExtractableResponse<Response> 지하철_노선_수정_요청(String uri, Map<String, String> params) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(params)
                .when()
                .put(uri)
                .then()
                .log().all()
                .extract();
    }

    // 역명, 컬러, 첫차시간, 막차시간, 배차간격
    private Map<String, String>
    buildCreateLineRequestParam(String station, String color, LocalTime startTime, LocalTime endTime, String interval) {
        final Map<String, String> params = new HashMap<>();
        params.put("name", station);
        params.put("color", color);
        params.put("startTime", startTime.format(DateTimeFormatter.ISO_TIME));
        params.put("endTime", endTime.format(DateTimeFormatter.ISO_TIME));
        params.put("intervalTime", interval);
        return params;
    }
}
