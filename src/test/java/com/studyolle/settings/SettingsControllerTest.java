package com.studyolle.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.account.SignUpForm;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.TagForm;
import com.studyolle.settings.form.ZoneForm;
import com.studyolle.tag.TagRepository;
import com.studyolle.zone.ZoneRepository;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("test시").province("test 주").build();

    @BeforeEach
    void beforeEach(){
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("sonnie")
    @DisplayName("계정의 지역정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ZONES_URL_NAME))
                .andExpect(view().name(SettingsController.SETTINGS_ZONES_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("sonnie")
    @DisplayName("계정의 지역정보 등록")
    @Test
    void addZonesForm() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL_NAME+"/add")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(zoneForm))
        .with(csrf()))
        .andExpect(status().isOk());

        Account sonnie = accountRepository.findByNickname("sonnie");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(sonnie.getZones().contains(zone));
    }

    @WithAccount("sonnie")
    @DisplayName("계정의 지역정보 삭제")
    @Test
    void removeZonesForm() throws Exception {
        Account sonnie = accountRepository.findByNickname("sonnie");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(sonnie, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL_NAME+"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(sonnie.getZones().contains(zone));
    }

    @WithAccount("sonnie")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform((get(SettingsController.SETTINGS_TAGS_URL_NAME)))
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount("sonnie")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception{
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL_NAME+"/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());
        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account sonnie = accountRepository.findByNickname("sonnie"); // detached 상태임, @transactional 처리 해줘야함
        assertTrue(sonnie.getTags().contains(newTag));

    }

    @WithAccount("sonnie")
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception{
        Account sonnie = accountRepository.findByNickname("sonnie");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(sonnie, newTag);
        assertTrue(sonnie.getTags().contains(newTag));


        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL_NAME+"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());


        assertFalse(sonnie.getTags().contains(newTag));

    }


    @WithAccount("sonnie")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {

        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL_NAME))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

    }

    @WithAccount("sonnie")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {

        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL_NAME)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL_NAME))
                .andExpect(flash().attributeExists("message"));

        Account sonnie = accountRepository.findByNickname("sonnie");
        assertEquals(bio, sonnie.getBio());
    }

    @WithAccount("sonnie")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {

        String bio = "길게 소개를 수정하는 경우.길게 소개를 수정하는 경우.길게 소개를 수정하는 경우.길게 소개를 수정하는 경우.길게 소개를 수정하는 경우.";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL_NAME)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account sonnie = accountRepository.findByNickname("sonnie");
        assertNull(sonnie.getBio());
    }

    @WithAccount("sonnie")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL_NAME))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }
    @WithAccount("sonnie")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL_NAME)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL_NAME))
                .andExpect(flash().attributeExists("message"));

        Account sonnie = accountRepository.findByNickname("sonnie");
        assertTrue(passwordEncoder.matches("12345678",sonnie.getPassword()));
    }

    @WithAccount("sonnie")
    @DisplayName("패스워드 수정 - 입력값 에러 - 패스워드 불일치")
    @Test
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL_NAME)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "11111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

    }
}