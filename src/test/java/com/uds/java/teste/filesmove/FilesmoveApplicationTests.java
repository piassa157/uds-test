package com.uds.java.teste.filesmove;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.uds.java.teste.filesmove.repository.UserJpaRepository;

@SpringBootTest(
	properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
			+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
			+ "org.springframework.boot.session.jdbc.autoconfigure.JdbcSessionAutoConfiguration"
	}
)
class FilesmoveApplicationTests {

	@MockBean
	private UserJpaRepository userJpaRepository;

	@Test
	void contextLoads() {
	}

}
