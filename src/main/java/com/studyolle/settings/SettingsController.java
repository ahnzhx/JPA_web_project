package com.studyolle.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.account.AccountService;
import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.*;
import com.studyolle.settings.validator.NicknameValidator;
import com.studyolle.settings.validator.PasswordFormValidator;
import com.studyolle.tag.TagRepository;
import com.studyolle.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    static final String SETTINGS_TAGS_VIEW_NAME = "settings/tags";
    static final String SETTINGS_TAGS_URL_NAME = "/settings/tags";
    static final String SETTINGS_ZONES_VIEW_NAME = "settings/zones";
    static final String SETTINGS_ZONES_URL_NAME = "/settings/zones";

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    // List 를 json으로 변환할 떄
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;

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

    @GetMapping(SETTINGS_TAGS_URL_NAME)
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        // set을 list 로 바꿔준다.
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        //whiteList
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return SETTINGS_TAGS_VIEW_NAME;
    }

    /**
     * ajax
     * @param account
     * @param tagForm
     * @return
     */
    @PostMapping(SETTINGS_TAGS_URL_NAME + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            tag = tagRepository.save(Tag.builder().title(title).build());
        }

        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();

    }
    @PostMapping(SETTINGS_TAGS_URL_NAME + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();

    }

    @GetMapping(SETTINGS_ZONES_URL_NAME)
    public String updateZoneForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return SETTINGS_ZONES_VIEW_NAME;
    }

    @PostMapping(SETTINGS_ZONES_URL_NAME+"/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvineName());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }
        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_ZONES_URL_NAME+"/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm){

        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvineName());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }
        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }
}
