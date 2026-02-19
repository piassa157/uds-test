package com.uds.java.teste.filesmove;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.uds.java.teste.filesmove.repository.UserJpaRepository;

@SpringBootTest(
	properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
			+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
			+ "org.springframework.boot.session.jdbc.autoconfigure.JdbcSessionAutoConfiguration"
	}
)
class FilesmoveApplicationTests {

	@MockitoBean
	private UserJpaRepository userJpaRepository;

	@Test
	void contextLoads() {
	}

}
