package com.example.fake_coupang.room;

import com.example.fake_coupang.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "room")
@Getter @Setter
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId; // 방 ID (PK)

    // 방 소유자 (User 엔티티와 1:1 관계, UNIQUE 제약, NOT NULL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Version // JPA 낙관적 락을 활성화하여 동시 저장 충돌을 자동으로 감지 (409 Conflict 대응)
    @Column(name = "version", nullable = false)
    private Integer version = 0; // 저장 버전 (기본값 0)

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 마지막 저장 시각

    // 엔티티가 저장되거나 수정될 때 updatedAt을 현재 시간으로 자동 갱신
    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}