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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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

		accountService.processNewAccount(signUpForm);

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

		account.completeSignUp();

		model.addAttribute("numberOfUser", accountRepository.count());
		model.addAttribute("nickname", account.getNickname());

		return view;
	}


}
