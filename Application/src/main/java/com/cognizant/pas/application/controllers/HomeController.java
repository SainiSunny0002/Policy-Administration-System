package com.cognizant.pas.application.controllers;

import feign.FeignException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cognizant.pas.application.payload.request.BusinessPropertyRequest;
import com.cognizant.pas.application.payload.request.ConsumerBusinessRequest;
import com.cognizant.pas.application.payload.request.CreatePolicyRequest;
import com.cognizant.pas.application.payload.request.IssuePolicyRequest;
import com.cognizant.pas.application.payload.response.BusinessPropertyDetails;
import com.cognizant.pas.application.payload.response.ConsumerBusinessDetails;
import com.cognizant.pas.application.payload.response.MessageResponse;
import com.cognizant.pas.application.payload.response.PolicyDetailsResponse;
import com.cognizant.pas.application.payload.response.Property;
import com.cognizant.pas.application.payload.response.QuotesDetailsResponse;
import com.cognizant.pas.application.restclients.FeignConsumerClient;
import com.cognizant.pas.application.restclients.FeignPolicyClient;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class HomeController {

	private final FeignConsumerClient feignConsumerClient;
	private final FeignPolicyClient feignPolicyClient;

	@Autowired
	public HomeController(FeignConsumerClient feignConsumerClient, FeignPolicyClient feignPolicyClient) {
		this.feignConsumerClient = feignConsumerClient;
		this.feignPolicyClient = feignPolicyClient;
	}

	@GetMapping(value = "/")
	@ApiOperation(value = "Displaying the home page", response = String.class)
	public String displayHomePage() {
		log.info("{}, Information: Displaying the home page", this.getClass().getSimpleName());
		return "home";
	}
	
	//business
	@GetMapping(value = "/conbusiness/create.html")
	@ApiOperation(value = "Displaying the create business page", response = String.class)
	public String displayConbusinessCreatePage() {
		log.info("{}, Information: Displaying create business page", this.getClass().getSimpleName());
		return "/conbusiness/create";
	}
	
	@GetMapping(value = "/conbusiness/view.html")
	@ApiOperation(value = "Displaying the View business page", response = String.class)
	public String displayConbusinessViewPage() {
		log.info("{}, Information: Displaying view business page", this.getClass().getSimpleName());
		return "/conbusiness/view";
	}
	
	//business property
	@GetMapping(value = "/busprop/create.html")
	@ApiOperation(value = "Displaying the create business property page", response = String.class)
	public String businessCreatePage() {
		log.info("{}, Information: Displaying create property page", this.getClass().getSimpleName());
		return "/busprop/create";
	}
	
	@GetMapping(value = "/busprop/view.html")
	@ApiOperation(value = "Displaying the View business property page", response = String.class)
	public String displayBusinessPropertyViewPage() {
		log.info("{}, Information: Displaying view property page", this.getClass().getSimpleName());
		return "/busprop/view";
	}
	
	//policy
	@GetMapping(value = "/policy/create.html")
	@ApiOperation(value = "Displaying the create policy page", response = String.class)
	public String policyCreatePage() {
		log.info("{}, Information: Displaying create policy page", this.getClass().getSimpleName());
		return "/policy/create";
	}
	
	@GetMapping(value = "/policy/issue.html")
	@ApiOperation(value = "Displaying the Issue policy page", response = String.class)
	public String displayPolicyIssuePage() {
		log.info("{}, Information: Displaying issue policy page", this.getClass().getSimpleName());
		return "/policy/issue";
	}
	
	@GetMapping(value = "/policy/view.html")
	@ApiOperation(value = "Displaying the view policy page", response = String.class)
	public String displayPolicyViewPage() {
		log.info("{}, Information: Displaying view policy page", this.getClass().getSimpleName());
		return "/policy/view";
	}
	
	//quotes
	@GetMapping(value = "/quotes/view.html")
	@ApiOperation(value = "Displaying the View quotes page", response = String.class)
	public String displayQuotesViewPage() {
		log.info("{}, Information: Displaying view quotes page", this.getClass().getSimpleName());
		return "/quotes/view";
	}

	@GetMapping(value = "/viewPolicy")
	@ApiOperation(value = "View Policy", response = String.class)
	public String viewPolicy(HttpSession session, Model model,
			@ApiParam(name = "Consumer Id") @RequestParam Long consumerid,
			@ApiParam(name = "Policy Id") @RequestParam String policyid) {
		try {
			PolicyDetailsResponse policyDetailsResponse = feignPolicyClient
					.viewPolicy("Bearer " + session.getAttribute("token").toString(), consumerid, policyid);
			model.addAttribute("policyDetailsResponse", policyDetailsResponse);
			model.addAttribute("details", new PolicyDetailsResponse());
			log.info("{}, Information: displaying Policy Details", this.getClass().getSimpleName());
		} catch (FeignException e) {
			log.debug("{}, Information: Attempting to visit invalid Policy Details", this.getClass().getSimpleName());
			return "Policy404";
		}

		return "policy";
	}

	@GetMapping(value = "/getQuotes")
	@ApiOperation(value = "Get Quotes", response = String.class)
	public String getQuotes(HttpSession session, Model model,
			@ApiParam(name = "Business Value") @RequestParam Long businessValue,
			@ApiParam(name = "Property Value") @RequestParam Long propertyValue,
			@ApiParam(name = "Property Type") @RequestParam String propertyType,
			RedirectAttributes redirectAttributes) {
		try {
			QuotesDetailsResponse quotesDetailsResponse = feignPolicyClient.getQuotes(
					"Bearer " + session.getAttribute("token").toString(), businessValue, propertyValue, propertyType);
			String quotes = quotesDetailsResponse.getQuotes();
			redirectAttributes.addFlashAttribute("quotes", quotes);
			log.info("{},Information: {}", this.getClass().getSimpleName(), quotes);
		} catch (FeignException e) {
			log.debug("{}, Information: Attempting to visit invalid Quotes", this.getClass().getSimpleName());
			return "404";
		}	

		return "redirect:/quotes/view.html";
	}

	@PostMapping(value = "/createPolicy")
	@ApiOperation(value = "Create Policy Details", response = String.class)
	public String createPolicy(HttpSession session,
			@ModelAttribute("createPolicyRequest")  @RequestBody CreatePolicyRequest createPolicyRequest,
			RedirectAttributes redirectAttributes) {
		try {
			MessageResponse messageResponse = feignPolicyClient
					.createPolicy("Bearer " + session.getAttribute("token").toString(), createPolicyRequest);
			String policystatus = messageResponse.getMessage();
			redirectAttributes.addFlashAttribute("policystatus", policystatus);
			log.info("{}, Information: {}", this.getClass().getSimpleName(), policystatus);
		} catch (FeignException e) {
			log.debug("{}, Information: Attempting to create invalid Policy", this.getClass().getSimpleName());
			return "404";
		}
		return "redirect:/policy/create.html";
	}

	@PostMapping(value = "/issuePolicy")
	@ApiOperation(value = "Issue Policy Details", response = String.class)
	public String issuePolicy(HttpSession session,
			@ModelAttribute("createPolicyRequest") @RequestBody IssuePolicyRequest issuePolicyRequest,
			RedirectAttributes redirectAttributes) {
		try {
			MessageResponse messageResponse = feignPolicyClient
					.issuePolicy("Bearer " + session.getAttribute("token").toString(), issuePolicyRequest);
			String issuePolicyStatus = messageResponse.getMessage();
			redirectAttributes.addFlashAttribute("issuePolicyStatus", issuePolicyStatus);
			log.info("{}, Information: {}", this.getClass().getSimpleName(), issuePolicyStatus);
		} catch (FeignException e) {
			log.debug("{}, Information: Attempting to issue invalid Policy", this.getClass().getSimpleName());
			return "404";
		}
		return "redirect:/policy/issue.html";
	}

	@GetMapping(value = "/viewConsumerBusiness")
	@ApiOperation(value = "View Consumer Business", response = String.class)
	public String viewConsumerBusiness(HttpSession session, Model model,
			@ApiParam(name = "Consumer Id") @RequestParam Long consumerid) {
		try {
			ConsumerBusinessDetails consumerBusinessDetails = feignConsumerClient
					.viewConsumerBusiness("Bearer " + session.getAttribute("token").toString(), consumerid);
			model.addAttribute("consumerBusinessDetails", consumerBusinessDetails);
			model.addAttribute("details", new ConsumerBusinessDetails());
			log.info("{}, Information: displaying ConsumerBusinessDetails", this.getClass().getSimpleName());
		} catch (FeignException e) {
			log.debug("{}, Information: Attempting to visit invalid ConsumerBusinessDetails",
					this.getClass().getSimpleName());
			return "consumerbusiness404";
		}
		return "consumerbusiness";
	}

	@GetMapping(value = "/viewConsumerProperty")
	@ApiOperation(value = "View Consumer Property", response = String.class)
	public String viewConsumerProperty(HttpSession session, Model model,
			@ApiParam(name = "Consumer Id") @RequestParam Long consumerid,
			@ApiParam(name = "Property Id") @RequestParam Long propertyid) {
		try {
			Property property = feignConsumerClient
					.viewConsumerProperty("Bearer " + session.getAttribute("token").toString(), consumerid,propertyid);
			model.addAttribute("property", property);
			model.addAttribute("details", new Property());
			log.info("{}, Information: displaying ConsumerPropertyDetails", this.getClass().getSimpleName());
		} catch (FeignException e) {
			log.debug("{}, Information: Attempting to visit invalid ConsumerPropertyDetails",
					this.getClass().getSimpleName());
			return "businessproperty404";
		}
		return "businessproperty";
	}
	
	@PostMapping(value = "/createConsumerBusiness")
	@ApiOperation(value = "Create Consumer Business", response = String.class)
	public String createConsumerBusiness(HttpSession session,
			@ModelAttribute("consumerBusinessRequest") @RequestBody ConsumerBusinessRequest consumerBusinessRequest,
			RedirectAttributes redirectAttributes) {
		try {
			MessageResponse messageResponse = feignConsumerClient
					.createConsumerBusiness("Bearer " + session.getAttribute("token").toString(), consumerBusinessRequest);
			String consumerbusinessstatus = messageResponse.getMessage();
			redirectAttributes.addFlashAttribute("consumerbusinessstatus", consumerbusinessstatus);
			log.info("{}, Information: {}", this.getClass().getSimpleName(), consumerbusinessstatus);
		} catch (FeignException e) {
			log.debug("{}, Information: Attempting to create invalid Consumer Business", this.getClass().getSimpleName());
			return "404";
		}
		return "redirect:/conbusiness/create.html";
	}

	@PostMapping(value = "/createBusinessProperty")
	@ApiOperation(value = "Create Business Property", response = String.class)
	public String createBusinessProperty(HttpSession session,
			@ModelAttribute("businessPropertyRequest") @RequestBody BusinessPropertyRequest businessPropertyRequest,
			RedirectAttributes redirectAttributes) {
		try {
			MessageResponse messageResponse = feignConsumerClient
					.createBusinessProperty("Bearer " + session.getAttribute("token").toString(), businessPropertyRequest);
			String businessproperystatus = messageResponse.getMessage();
			redirectAttributes.addFlashAttribute("businessproperystatus", businessproperystatus);
			log.info("{}, Information: {}", this.getClass().getSimpleName(), businessproperystatus);
		} catch (FeignException e) {
			log.debug("{}, Information: Attempting to create invalid Business Property", this.getClass().getSimpleName());
			return "404";
		}
		return "redirect:/busprop/create.html";
	}
	
}
