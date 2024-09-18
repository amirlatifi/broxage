package com.amirlatifi.broxage.service;

import com.amirlatifi.broxage.model.Customer;
import com.amirlatifi.broxage.model.Role;
import com.amirlatifi.broxage.repository.CustomerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService implements UserDetailsService {

	private final CustomerRepository customerRepository;

	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public CustomerService(CustomerRepository customerRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.customerRepository = customerRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	@Transactional
	public Customer registerCustomer(String username, String password, String iban) {
		return createUser(username, password, iban, Role.CUSTOMER);
	}

	@Transactional
	public Customer registerAdmin(String username, String password, String iban) {
		return createUser(username, password, iban, Role.ADMIN);
	}

	private Customer createUser(String username, String password, String iban, Role role) {
		if (customerRepository.findByUsername(username).isPresent()) {
			throw new IllegalStateException("Username already exists");
		}
		Customer customer = new Customer();
		customer.setUsername(username);
		customer.setPassword(bCryptPasswordEncoder.encode(password));
		customer.setIban(iban);
		customer.setRole(role);
		return customerRepository.save(customer);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return customerRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
	}

	public Customer findByUsername(String username) {
		return customerRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
	}

	public Customer findById(Long id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
	}
}