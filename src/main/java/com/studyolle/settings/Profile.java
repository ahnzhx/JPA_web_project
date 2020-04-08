package com.studyolle.settings;

import com.studyolle.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class Profile {

    @Length(max = 35)
    private String bio;
    @Length(max = 50)
    private String url;
    @Length(max = 50)
    private String occupation;
    @Length(max = 50)
    private String location;

    private String profileImage;

    /**
     * account 가 비어있기 때문에 기본생성자(profile)를 만들어줘야 한다.
     *
     * @param account
     */
    public Profile(Account account) {
        this.bio = account.getBio();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
        this.profileImage = account.getProfileImage();
    }
}
