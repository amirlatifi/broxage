package com.amirlatifi.broxage.controller;

import com.amirlatifi.broxage.model.Customer;
import com.amirlatifi.broxage.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@PostMapping("/register")
	public ResponseEntity<Customer> registerCustomer(@RequestParam String username,
													 @RequestParam String password,
													 @RequestParam String iban) {
		Customer customer = customerService.registerCustomer(username, password, iban);
		return ResponseEntity.ok(customer);
	}

	@PostMapping("/register/admin")
	public ResponseEntity<Customer> registerAdmin(@RequestParam String username,
												  @RequestParam String password,
												  @RequestParam String iban) {
		Customer admin = customerService.registerAdmin(username, password, iban);
		return ResponseEntity.ok(admin);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
		Customer customer = customerService.findById(id);
		return ResponseEntity.ok(customer);
	}

	@GetMapping("/username/{username}")
	public ResponseEntity<Customer> getCustomerByUsername(@PathVariable String username) {
		Customer customer = customerService.findByUsername(username);
		return ResponseEntity.ok(customer);
	}
}