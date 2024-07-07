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

    public User(String username, String password, UserStatusEnum userStatus) {
        this.username = username;
        this.password = password;
        this.userStatus = userStatus;
    }

}