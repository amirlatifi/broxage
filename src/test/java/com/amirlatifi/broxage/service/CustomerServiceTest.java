package com.amirlatifi.broxage.service;

import com.amirlatifi.broxage.model.Customer;
import com.amirlatifi.broxage.model.Role;
import com.amirlatifi.broxage.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@InjectMocks
	private CustomerService customerService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void registerCustomer_success() {
		String username = "testuser";
		String password = "password";
		String iban = "DE89370400440532013000";

		when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());
		when(bCryptPasswordEncoder.encode(password)).thenReturn("encodedPassword");
		when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Customer result = customerService.registerCustomer(username, password, iban);

		assertNotNull(result);
		assertEquals(username, result.getUsername());
		assertEquals("encodedPassword", result.getPassword());
		assertEquals(iban, result.getIban());
		assertEquals(Role.CUSTOMER, result.getRole());

		verify(customerRepository).save(any(Customer.class));
	}

	@Test
	void registerCustomer_usernameAlreadyExists_throwsException() {
		String username = "testuser";
		String password = "password";
		String iban = "DE89370400440532013000";

		when(customerRepository.findByUsername(username)).thenReturn(Optional.of(new Customer()));

		assertThrows(IllegalStateException.class, () ->
				customerService.registerCustomer(username, password, iban)
		);

		verify(customerRepository, never()).save(any(Customer.class));
	}

	@Test
	void registerAdmin_success() {
		String username = "adminuser";
		String password = "password";
		String iban = "DE89370400440532013000";

		when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());
		when(bCryptPasswordEncoder.encode(password)).thenReturn("encodedPassword");
		when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Customer result = customerService.registerAdmin(username, password, iban);

		assertNotNull(result);
		assertEquals(username, result.getUsername());
		assertEquals("encodedPassword", result.getPassword());
		assertEquals(iban, result.getIban());
		assertEquals(Role.ADMIN, result.getRole());

		verify(customerRepository).save(any(Customer.class));
	}

	@Test
	void loadUserByUsername_userNotFound_throwsException() {
		String username = "nonexistentuser";

		when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () ->
				customerService.loadUserByUsername(username)
		);
	}

	@Test
	void findByUsername_success() {
		String username = "testuser";
		Customer customer = new Customer();
		customer.setUsername(username);

		when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

		Customer result = customerService.findByUsername(username);

		assertNotNull(result);
		assertEquals(username, result.getUsername());
	}

	@Test
	void findByUsername_userNotFound_throwsException() {
		String username = "nonexistentuser";

		when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () ->
				customerService.findByUsername(username)
		);
	}

	@Test
	void findById_success() {
		Long id = 1L;
		Customer customer = new Customer();
		customer.setId(id);

		when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

		Customer result = customerService.findById(id);

		assertNotNull(result);
		assertEquals(id, result.getId());
	}

	@Test
	void findById_userNotFound_throwsException() {
		Long id = 1L;

		when(customerRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () ->
				customerService.findById(id)
		);
	}
}