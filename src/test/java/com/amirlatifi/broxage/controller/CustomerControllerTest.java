package com.amirlatifi.broxage.controller;

import com.amirlatifi.broxage.model.Customer;
import com.amirlatifi.broxage.model.Role;
import com.amirlatifi.broxage.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerControllerTest {

	private MockMvc mockMvc;

	@Mock
	private CustomerService customerService;

	@InjectMocks
	private CustomerController customerController;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
	}

	@Test
	void registerCustomer_success() throws Exception {
		Customer customer = new Customer();
		customer.setId(1L);
		customer.setUsername("testuser");
		customer.setIban("DE89370400440532013000");
		customer.setRole(Role.CUSTOMER);

		when(customerService.registerCustomer(anyString(), anyString(), anyString())).thenReturn(customer);

		mockMvc.perform(post("/api/customers/register")
						.param("username", "testuser")
						.param("password", "testpass")
						.param("iban", "DE89370400440532013000")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.username").value("testuser"))
				.andExpect(jsonPath("$.iban").value("DE89370400440532013000"))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));
	}

	@Test
	void registerAdmin_success() throws Exception {
		Customer admin = new Customer();
		admin.setId(2L);
		admin.setUsername("adminuser");
		admin.setIban("DE89370400440532013001");
		admin.setRole(Role.ADMIN);

		when(customerService.registerAdmin(anyString(), anyString(), anyString())).thenReturn(admin);

		mockMvc.perform(post("/api/customers/register/admin")
						.param("username", "adminuser")
						.param("password", "adminpass")
						.param("iban", "DE89370400440532013001")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2))
				.andExpect(jsonPath("$.username").value("adminuser"))
				.andExpect(jsonPath("$.iban").value("DE89370400440532013001"))
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void getCustomerById_success() throws Exception {
		Customer customer = new Customer();
		customer.setId(1L);
		customer.setUsername("testuser");
		customer.setIban("DE89370400440532013000");
		customer.setRole(Role.CUSTOMER);

		when(customerService.findById(1L)).thenReturn(customer);

		mockMvc.perform(get("/api/customers/1")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.username").value("testuser"))
				.andExpect(jsonPath("$.iban").value("DE89370400440532013000"))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));
	}

	@Test
	void getCustomerByUsername_success() throws Exception {
		Customer customer = new Customer();
		customer.setId(1L);
		customer.setUsername("testuser");
		customer.setIban("DE89370400440532013000");
		customer.setRole(Role.CUSTOMER);

		when(customerService.findByUsername("testuser")).thenReturn(customer);

		mockMvc.perform(get("/api/customers/username/testuser")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.username").value("testuser"))
				.andExpect(jsonPath("$.iban").value("DE89370400440532013000"))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));
	}
}