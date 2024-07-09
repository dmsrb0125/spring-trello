package com.sparta.springtrello.domain.user.entity;



import com.sparta.springtrello.common.Timestamped;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Entity
@NoArgsConstructor
@Table(name="users")
public class User extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String username;

    @Setter
    @NotNull
    @Column
    private String password;

    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column
    private UserStatusEnum userStatus;

    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column
    private UserRoleEnum userRole;

    @Setter
    @Column
    private String refreshToken;

    @Setter
    @Column
    private String nickname;

    @Setter
    @Column
    private String introduce;

    @Setter
    @Column
    private String pictureUrl;

    @Column
    private Long kakaoId;

    public User(String username, String password, UserStatusEnum userStatus) {
        this.username = username;
        this.password = password;
        this.userStatus = userStatus;
    }

    public User(Long kakaoId, String username, String pictureUrl, String password) {
        this.kakaoId = kakaoId;
        this.pictureUrl = pictureUrl;
        this.username = username;
        this.password = password;
        this.userStatus = UserStatusEnum.STATUS_NORMAL;
    }

}