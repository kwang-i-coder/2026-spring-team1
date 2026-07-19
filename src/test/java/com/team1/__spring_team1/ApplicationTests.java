package com.team1.__spring_team1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles({"local", "test"})
@SpringBootTest
@Transactional
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
