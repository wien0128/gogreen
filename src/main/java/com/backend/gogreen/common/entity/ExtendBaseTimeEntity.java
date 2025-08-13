package com.backend.gogreen.common.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>카디널리티(데이터 고유성) 높이기 위해 사용</p>
 * <p>
 *     LocalDateTime createdAt 은 sequential 하지만 카디널리티가 낮아 DB 부하 증가<br>
 *      -> 트래픽이 몰릴 시 동일 타임스탬프 값이 대량 발생<br>
 *      -> Hot Spot에 데이터가 몰려 B-Tree 인덱스가 편향적으로 성장<br>
 *      -> 인덱스 재정렬 비용, 페이지 분할 비용 증가<br>
 *      -> 조회 및 정렬 시간 비용 증가
 * </p>
 * <p>createdAt = 정렬, 세부 시각 조회용 = 소팅/페이징 최적화<br>
 * createDate = 필터, 집계 조회용 = 일 단우 검색 최적화</p>
 * <p>단, 컬럼이 늘어남에 따라 공간 효율성은 떨어짐<br>
 * 오히러 대량의 데이터에서 성능이 비약적으로 상승</p>
 */

@MappedSuperclass
@Getter
public class ExtendBaseTimeEntity extends BaseTimeEntity {

    protected LocalDate createDate;

    @PrePersist
    protected  void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        this.createDate = this.createdAt.toLocalDate();
    }
}
