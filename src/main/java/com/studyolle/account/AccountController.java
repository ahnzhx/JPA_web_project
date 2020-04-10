package com.studyolle.account;

import com.studyolle.domain.Account;
import javassist.Loader;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AccountController {

	private final SignUpFormValidator signUpFormValidator;
	private final AccountService accountService;
	private final AccountRepository accountRepository;

	@InitBinder("signUpForm")
	public void initBinder(WebDataBinder webDataBinder){
		webDataBinder.addValidators(signUpFormValidator);
	}

	@GetMapping("/sign-up")
	public String signUpForm(Model model) {
		model.addAttribute(new SignUpForm());
		return "account/sign-up";
	}

	@PostMapping("/sign-up")
	public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
		if(errors.hasErrors()){
			return "account/sign-up";
		}

		Account account = accountService.processNewAccount(signUpForm);
		accountService.login(account);

		return "redirect:/";
	}

	@GetMapping("/check-email-token")
	public String checkEmailToken(Model model, String token, String email){
		Account account = accountRepository.findByEmail(email);
		String view = "account/checked-email";
		if(account == null){
			model.addAttribute("error", "nonExisted.email");
			return view;
		}

		if(!account.isValidToken(token)){
			model.addAttribute("error", "different.token");
			return view;
		}

		accountService.completeSignUp(account);

		model.addAttribute("numberOfUser", accountRepository.count());
		model.addAttribute("nickname", account.getNickname());

		return view;
	}

	@GetMapping("/check-email")
	public String checkEmail(Model model, @CurrentUser Account account){
		model.addAttribute("email", account.getEmail());

		return "account/check-email";
	}

	@GetMapping("/resend-confirm-email")
	public String resendConfirmEmail(@CurrentUser Account account, Model model){
		if(!account.canSendConfirmEmail()){
			model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
			model.addAttribute("email", account.getEmail());
			return "account/check-email";
		}
		accountService.sendSignUpConfirmEmail(account);
		return "redirect:/";
	}

	@GetMapping("/profile/{nickname}")
	public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account){
		Account byNickname = accountRepository.findByNickname(nickname);
		if(nickname == null){
			throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
		}
		model.addAttribute(byNickname); // model 이름은 byNickname의 타입이 들어간다.
		model.addAttribute("isOwner", byNickname.equals(account));
		return "account/profile";

	}

	/**
	 * 이메일로 로그인하기
	 */
	@GetMapping("/email-login")
		public String emailLoginForm(){
			return "account/email-login";
		}

		@PostMapping("/email-login")
		public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes){
			Account account = accountRepository.findByEmail(email);
			if(account == null){
				model.addAttribute("error", "It is not a valid email");
				return "account/email-login";
			}
//		if(!account.canSendConfirmEmail()){
//			model.addAttribute("error", "you can email login 1 hour later");
//			return "account/email-login";
//		}
			accountService.sendLoginLink(account);
			attributes.addFlashAttribute("message", "Sent email-certificated email");
			return "redirect:/email-login";

	}

	@GetMapping("/login-by-email")
	public String loginByEmail(String token, String email, Model model){
		Account account = accountRepository.findByEmail(email);
		String view = "account/logged-in-by-email";
		if(account == null || !account.isValidToken(token)){
			model.addAttribute("error", "로그인할 수 없습니다.");
			return view;
		}
		accountService.login(account);
		return view;

	}
}
