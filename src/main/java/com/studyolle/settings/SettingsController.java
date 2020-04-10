package com.studyolle.settings;

import com.studyolle.account.AccountService;
import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import com.studyolle.settings.form.NicknameForm;
import com.studyolle.settings.form.Notifications;
import com.studyolle.settings.form.PasswordForm;
import com.studyolle.settings.form.Profile;
import com.studyolle.settings.validator.NicknameValidator;
import com.studyolle.settings.validator.PasswordFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {



    static final String SETTINGS_PROFILE_VIEW_NAME="settings/profile";
    static final String SETTINGS_PROFILE_URL_NAME = "/settings/profile";
    static final String SETTINGS_PASSWORD_VIEW_NAME="settings/password";
    static final String SETTINGS_PASSWORD_URL_NAME = "/settings/password";
    static final String SETTINGS_NOTIFICATIONS_VIEW_NAME="settings/notifications";
    static final String SETTINGS_NOTIFICATIONS_URL_NAME = "/settings/notifications";
    static final String SETTINGS_ACCOUNT_VIEW_NAME="settings/account";
    static final String SETTINGS_ACCOUNT_URL_NAME = "/settings/account";

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping(SETTINGS_PROFILE_URL_NAME)
    public String profileUpdateForm(@CurrentUser Account account, Model model){

        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));
        return SETTINGS_PROFILE_VIEW_NAME;
    }

    /**
     * RedirectAttributes : 한번만 쓸 alert 메시지를 쓸 때 쓴다. 한번 쓰고 없어진다.
     */
    @PostMapping(SETTINGS_PROFILE_URL_NAME)
    public String updateProfile(@CurrentUser Account account, @Valid Profile profile, Errors errors
            , Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }
        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        /**
         * 사용자가 화면을 refresh 하더라도 form submit을 하지 않도록 redirect 해준다
         */
        return "redirect:" + SETTINGS_PROFILE_URL_NAME;
    }

    @GetMapping(SETTINGS_PASSWORD_URL_NAME)
    public String updatePasswordForm(Model model, @CurrentUser Account account){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS_PASSWORD_VIEW_NAME;

    }

    @PostMapping(SETTINGS_PASSWORD_URL_NAME)
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm,
                                 Errors errors, Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS_PASSWORD_VIEW_NAME;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "Successfully changed password");
        return "redirect:" + SETTINGS_PASSWORD_URL_NAME;
    }

    @GetMapping(SETTINGS_NOTIFICATIONS_URL_NAME)
    public String updateNotificationForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS_NOTIFICATIONS_VIEW_NAME;

    }

    @PostMapping(SETTINGS_NOTIFICATIONS_URL_NAME)
    public String updateNotifications(@CurrentUser Account account, @Valid Notifications notifications,
                                      Errors errors, Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS_NOTIFICATIONS_VIEW_NAME;
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "Updated Notifications settings");
        return "redirect:" + SETTINGS_NOTIFICATIONS_URL_NAME;
    }

    @GetMapping(SETTINGS_ACCOUNT_URL_NAME)
    public String updateAccountForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS_ACCOUNT_VIEW_NAME;
    }

    @PostMapping(SETTINGS_ACCOUNT_URL_NAME)
    public String updateAccount(@CurrentUser Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS_ACCOUNT_VIEW_NAME;

        }
        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "Successfully updated nickname");
        return "redirect:"+SETTINGS_ACCOUNT_URL_NAME;

    }
}
