package navik.global.dto;

import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

/**
 * 커서 기반 페이지네이션 응답을 위한 DTO 클래스입니다.
 *
 * @param <T> 데이터 리스트의 타입
 */
@Getter
public class CursorResponseDto<T> {

    /**
     * 데이터 리스트
     */
    private List<T> content;

    /**
     * 현재 페이지의 데이터 개수
     */
    private int pageSize;

    /**
     * 다음 페이지를 위한 커서 값 (응답의 마지막 아이템의 식별자)
     */
    private String nextCursor;

    /**
     * 다음 페이지 존재 여부
     */
    private boolean hasNext;

    public CursorResponseDto(Slice<T> slice, String nextCursor) {
        this.content = slice.getContent();
        this.pageSize = slice.getNumberOfElements();
        this.nextCursor = nextCursor;
        this.hasNext = slice.hasNext();
    }
}