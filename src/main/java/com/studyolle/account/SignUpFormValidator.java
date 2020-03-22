package com.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor // private final type의 생성자 만들어줌
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;


    /**
     * Check which Class?
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }


    /**
     * What kind of validation do you need?
     */
    @Override
    public void validate(Object object, Errors errors) {
        SignUpForm signUpForm = (SignUpForm)object;
        if(accountRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email", "invalid.Email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }
        if(accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname", "invalid.Nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }
    }
}
